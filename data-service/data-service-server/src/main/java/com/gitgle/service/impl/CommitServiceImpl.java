package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitgle.constant.RedisConstant;
import com.gitgle.convert.GithubCommitConvert;
import com.gitgle.dao.Commit;
import com.gitgle.mapper.CommitMapper;
import com.gitgle.response.GithubCommit;
import com.gitgle.service.CommitService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommitServiceImpl implements CommitService {

    @Resource
    private CommitMapper commitMapper;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void writeGithubCommit2Commit(GithubCommit githubCommit) {
        RLock lock = redissonClient.getLock(RedisConstant.GITHUB_COMMIT_LOCK + githubCommit.getSha());
        try {
            lock.lock(5, TimeUnit.SECONDS);
            // 先根据sha查询数据库中是否存在
            Commit commit = commitMapper.selectOne(Wrappers.lambdaQuery(Commit.class).eq(Commit::getSha, githubCommit.getSha()));
            if(ObjectUtils.isNotEmpty(commit)){
                return;
            }
            // 如果没有则入库
            commit = GithubCommitConvert.convert2Commit(githubCommit);
            commitMapper.insert(commit);
        }catch (Exception e){
            log.error("CommitServiceImpl.writeGithubCommit2Commit error: {}", e.getMessage());
        }finally {
            lock.unlock();
        }
    }

    @Override
    public List<GithubCommit> readCommit2GithubCommit(String login) {
        List<Commit> commitList = commitMapper.selectList(Wrappers.lambdaQuery(Commit.class).eq(Commit::getAuthorLogin, login));
        if(ObjectUtils.isEmpty(commitList)){
            return null;
        }
        return commitList.stream().map(commit -> GithubCommitConvert.convert(commit)).collect(Collectors.toList());
    }
}
