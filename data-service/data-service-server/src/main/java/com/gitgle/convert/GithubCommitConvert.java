package com.gitgle.convert;

import com.alibaba.fastjson.JSONObject;
import com.gitgle.dao.Commit;
import com.gitgle.response.GithubCommit;

import java.time.LocalDateTime;

public class GithubCommitConvert {

    public static GithubCommit convert(JSONObject jsonObject){
        GithubCommit githubCommit = new GithubCommit();
        githubCommit.setAuthorLogin(jsonObject.getJSONObject("author").getString("login"));
        githubCommit.setReposId(jsonObject.getJSONObject("repository").getInteger("id"));
        githubCommit.setReposName(jsonObject.getJSONObject("repository").getString("name"));
        githubCommit.setCommitDataTime(jsonObject.getJSONObject("commit").getJSONObject("committer").getString("date"));
        githubCommit.setReposOwner(jsonObject.getJSONObject("repository").getJSONObject("owner").getString("login"));
        githubCommit.setSha(jsonObject.getJSONObject("commit").getJSONObject("tree").getString("sha"));
        return githubCommit;
    }

    public static Commit convert2Commit(GithubCommit githubCommit){
        Commit commit = new Commit();
        commit.setCommitDateTime(githubCommit.getCommitDataTime());
        commit.setAuthorLogin(githubCommit.getAuthorLogin());
        commit.setReposId(githubCommit.getReposId());
        commit.setReposName(githubCommit.getReposName());
        commit.setReposOwner(githubCommit.getReposOwner());
        commit.setSha(githubCommit.getSha());
        commit.setCreateTime(LocalDateTime.now());
        commit.setUpdateTime(LocalDateTime.now());
        return commit;
    }

    public static GithubCommit convert(Commit commit){
        GithubCommit githubCommit = new GithubCommit();
        githubCommit.setSha(commit.getSha());
        githubCommit.setAuthorLogin(commit.getAuthorLogin());
        githubCommit.setReposId(commit.getReposId());
        githubCommit.setReposName(commit.getReposName());
        githubCommit.setReposOwner(commit.getReposOwner());
        githubCommit.setCommitDataTime(commit.getCommitDateTime().toString());
        return githubCommit;
    }
}
