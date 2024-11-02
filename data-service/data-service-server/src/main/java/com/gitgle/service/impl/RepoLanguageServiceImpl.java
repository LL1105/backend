package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitgle.constant.RedisConstant;
import com.gitgle.dao.RepoLanguage;
import com.gitgle.service.RepoLanguageService;
import com.gitgle.mapper.RepoLanguageMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author maojunjun
* @description 针对表【repo_language】的数据库操作Service实现
* @createDate 2024-11-02 21:50:11
*/
@Slf4j
@Service
public class RepoLanguageServiceImpl implements RepoLanguageService{

    @Resource
    private RepoLanguageMapper repoLanguageMapper;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public Map<String, Integer> readRepoLanguages(String repoName, String owner) {
        List<RepoLanguage> repoLanguages = repoLanguageMapper.selectList(Wrappers.lambdaQuery(RepoLanguage.class).eq(RepoLanguage::getRepoName, repoName).eq(RepoLanguage::getRepoOwner, owner));
        if(ObjectUtils.isEmpty(repoLanguages)){
            return null;
        }
        return repoLanguages.stream().collect(Collectors.toMap(RepoLanguage::getLanguage, RepoLanguage::getLanguageTotal));
    }

    @Override
    public void writeRepoLanguages(String owner, String repoName, Map<String, Integer> languagesMap) {
        RLock lock = redissonClient.getLock(RedisConstant.GITHUB_REPO_LANGUAGE_LOCK + owner + ":" + repoName);
        try {
            lock.lock(5, TimeUnit.SECONDS);
            List<RepoLanguage> repoLanguages = repoLanguageMapper.selectList(Wrappers.lambdaQuery(RepoLanguage.class).eq(RepoLanguage::getRepoName, repoName).eq(RepoLanguage::getRepoOwner, owner));
            if(ObjectUtils.isNotEmpty(repoLanguages)){
                for(RepoLanguage repoLanguage : repoLanguages){
                    if(languagesMap.containsKey(repoLanguage.getLanguage())){
                        repoLanguage.setLanguageTotal(languagesMap.get(repoLanguage.getLanguage()));
                        repoLanguageMapper.updateById(repoLanguage);
                        languagesMap.remove(repoLanguage.getLanguage());
                    }
                }
            }
            for(Map.Entry<String, Integer> entry : languagesMap.entrySet()){
                RepoLanguage repoLanguage = new RepoLanguage();
                repoLanguage.setRepoName(repoName);
                repoLanguage.setRepoOwner(owner);
                repoLanguage.setLanguage(entry.getKey());
                repoLanguage.setLanguageTotal(entry.getValue());
                repoLanguage.setCreateTime(LocalDateTime.now());
                repoLanguage.setUpdateTime(LocalDateTime.now());
                repoLanguageMapper.insert(repoLanguage);
            }
        }catch (Exception e){
            log.error("writeRepoLanguages error: {}", e.getMessage());
        }finally {
            lock.unlock();
        }
    }
}




