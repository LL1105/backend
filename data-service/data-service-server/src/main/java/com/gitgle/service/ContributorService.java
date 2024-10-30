package com.gitgle.service;

import com.gitgle.dao.Contributor;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gitgle.response.GithubContributor;

import java.util.List;

public interface ContributorService{

    void writeGithubContributor2Contributor(List<GithubContributor> githubContributor);

    List<GithubContributor> readContributor2GithubContributor(String repoName, String repoOwner);
}
