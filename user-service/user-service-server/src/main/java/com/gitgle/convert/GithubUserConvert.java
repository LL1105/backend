package com.gitgle.convert;

import com.gitgle.entity.GithubUser;
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
}
