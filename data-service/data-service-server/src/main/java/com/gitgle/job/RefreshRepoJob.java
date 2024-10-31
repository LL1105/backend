package com.gitgle.job;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitgle.constant.RedisConstant;
import com.gitgle.produce.KafkaProducer;
import com.gitgle.response.GithubContributor;
import com.gitgle.response.GithubContributorResponse;
import com.gitgle.response.GithubRepos;
import com.gitgle.response.GithubReposResponse;
import com.gitgle.service.ContributorService;
import com.gitgle.service.ReposService;
import com.gitgle.service.UserService;
import com.gitgle.utils.GithubApiRequestUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class RefreshRepoJob {

    @Resource
    private KafkaProducer kafkaProducer;

    @Resource
    private ContributorService contributorService;

    @Resource
    private UserService userService;

    @Resource
    private ReposService reposService;

    @Resource
    private RedisTemplate<String, Integer> redisTemplate;

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    private Integer startIndex = 1;

    private Integer perSize = 100;

    private Integer limitTotalPage = 10;

    /**
     * 刷新仓库和贡献度
     */
    @XxlJob("refresh-repo-job")
    public void refresh(){
        log.info("执行刷新Repo任务...");
        // 根据热门用户刷新仓库
        for(int i=startIndex; i<limitTotalPage;i++){
            List<Integer> githubAccountIdList = redisTemplate.opsForList().range(RedisConstant.GITHUB_ACCOUNT_ID, i * perSize, (i + 1) * perSize);
            for(Integer githubAccountId : githubAccountIdList){
                try {
                    // 获取用户login
                    String login = userService.getLoginByAccountId(githubAccountId);
                    if(StringUtils.isNotEmpty(login)){
                        Map<String, String> param = new HashMap<>();
                        GithubReposResponse githubReposResponse = githubApiRequestUtils.listUserRepos(login, param);
                        for(GithubRepos githubRepos : githubReposResponse.getGithubProjectList()){
                            GithubRepos repos = reposService.readRepos2GithubRepos(login, githubRepos.getRepoName());
                            if(githubRepos.equals(repos)){
                                continue;
                            }
                            // 异步写库
                            CompletableFuture.runAsync(()->{
                                reposService.writeGithubRepos2Repos(githubRepos);
                            });
                            // 刷新仓库贡献者
                            GithubContributorResponse githubContributorResponse = githubApiRequestUtils.listRepoContributors(login, githubRepos.getRepoName(), param);
                            // 计算仓库贡献总值
                            Integer totalContributions = 0;
                            for(GithubContributor githubContributor : githubContributorResponse.getGithubContributorList()){
                                totalContributions += githubContributor.getContributions();
                            }
                            // 计算旧的仓库贡献总值
                            Integer oldTotalContributions = 0;
                            List<GithubContributor> oldGithubContributotList = contributorService.readContributor2GithubContributor(githubContributorResponse.getGithubContributorList().get(0).getRepoName(),
                                    githubContributorResponse.getGithubContributorList().get(0).getRepoOwner());
                            for(GithubContributor githubContributor : oldGithubContributotList){
                                oldTotalContributions += githubContributor.getContributions();
                            }
                            // 如果新旧值不一致，则发送消息
                            if(!totalContributions.equals(oldTotalContributions)){
                                // 异步写库
                                CompletableFuture.runAsync(()->{
                                    contributorService.writeGithubContributor2Contributor(githubContributorResponse.getGithubContributorList());
                                });
                                kafkaProducer.sendMessage(login, "TalentRank");
                                continue;
                            }
                            // 如果仓库指标不一致，则发送消息
                            if(!githubRepos.getStarsCount().equals(repos.getStarsCount()) || !githubRepos.getForksCount().equals(repos.getForksCount()) || !githubRepos.getIssueCount().equals(repos.getIssueCount())){
                                kafkaProducer.sendMessage(login, "TalentRank");
                            }
                        }
                    }
                    // 发送消息
                } catch (Exception e) {
                    log.error("获取用户所有仓库失败，用户ID为：" + githubAccountId);
                }
            }
        }
    }
}
