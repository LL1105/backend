package com.gitgle.service;

import com.gitgle.dao.RepoLanguage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author maojunjun
* @description 针对表【repo_language】的数据库操作Service
* @createDate 2024-11-02 21:50:11
*/
public interface RepoLanguageService{

    Map<String, Integer> readRepoLanguages(String repoName, String owner);

    void writeRepoLanguages(String owner, String repoName, Map<String, Integer> languagesMap);
}
