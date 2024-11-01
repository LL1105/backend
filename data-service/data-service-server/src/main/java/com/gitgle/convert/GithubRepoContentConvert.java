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
        String content = new String(decodedBytes);
        if(content.length() > 1000){
            content = content.substring(0, 1000);
        }
        githubReposContent.setRepoName(githubRequest.getRepoName());
        githubReposContent.setRepoOwner(githubRequest.getOwner());
        githubReposContent.setContent(content);
        return githubReposContent;
    }
}
