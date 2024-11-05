package com.gitgle.service;

import java.util.Map;

public interface RepoLanguageService{

    Map<String, Integer> readRepoLanguages(String repoName, String owner);

    void writeRepoLanguages(String owner, String repoName, Map<String, Integer> languagesMap);
}
