package com.gitgle.convert;

import com.gitgle.entity.GithubUser;
import com.gitgle.response.GithubRepos;
import com.gitgle.service.resp.GithubUserResp;
import com.gitgle.service.resp.SearchUser;

import java.util.List;

public class GithubUserConvert {

    public static SearchUser convert2SearchUser(GithubUser githubUser, List<String> domains) {
        SearchUser searchUser = new SearchUser();
        searchUser.setLogin(githubUser.getLogin());
        searchUser.setAvatar(githubUser.getAvatar());
        searchUser.setTalentRank(String.valueOf(githubUser.getTalentRank()));
        searchUser.setDomains(domains);
        searchUser.setNation(githubUser.getNation());
        searchUser.setLocation(githubUser.getLocation());
        searchUser.setNationEnglish(githubUser.getNationEnglish());
        return searchUser;
    }

    public static GithubUserResp convert2GithubUserResp(com.gitgle.response.GithubUser githubUser1, GithubUser githubUser, List<String> domains) {
        GithubUserResp githubUserResp = new GithubUserResp();
        githubUserResp.setId(githubUser1.getId());
        githubUserResp.setLogin(githubUser1.getLogin());
        githubUserResp.setAvatarUrl(githubUser1.getAvatarUrl());
        githubUserResp.setLocation(githubUser1.getLocation());
        githubUserResp.setBio(githubUser1.getBio());
        githubUserResp.setCompany(githubUser1.getCompany());
        githubUserResp.setEmail(githubUser1.getEmail());
        githubUserResp.setPublicRepos(githubUser1.getPublicRepos());
        githubUserResp.setFollowers(githubUser1.getFollowers());
        githubUserResp.setFollowing(githubUser1.getFollowing());
        githubUserResp.setCreatedAt(githubUser1.getCreatedAt());
        githubUserResp.setNation(githubUser.getNation());
        githubUserResp.setNationEnglish(githubUser.getNationEnglish());
        githubUserResp.setDomains(domains);
        githubUserResp.setTalentRank(String.valueOf(githubUser.getTalentRank()));
        return githubUserResp;
    }
}
