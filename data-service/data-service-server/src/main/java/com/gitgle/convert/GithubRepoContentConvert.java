package com.gitgle.convert;

import com.alibaba.fastjson.JSONObject;
import com.gitgle.request.GithubRequest;
import com.gitgle.response.GithubReposContent;
import org.apache.commons.codec.binary.Base64;

public class GithubRepoContentConvert {

    public static GithubReposContent convert(JSONObject responseBody, GithubRequest githubRequest){
        GithubReposContent githubReposContent = new GithubReposContent();
        githubReposContent.setPath(responseBody.getString("path"));
        githubReposContent.setName(responseBody.getString("name"));
        githubReposContent.setSha(responseBody.getString("sha"));
        githubReposContent.setType(responseBody.getString("type"));
        githubReposContent.setEncoding(responseBody.getString("encoding"));
        githubReposContent.setSize(responseBody.getInteger("size"));
        byte[] decodedBytes = Base64.decodeBase64(responseBody.getString("content"));
        githubReposContent.setRepoName(githubRequest.getRepoName());
        githubReposContent.setRepoOwner(githubRequest.getOwner());
        githubReposContent.setContent(new String(decodedBytes));
        return githubReposContent;
    }
}
