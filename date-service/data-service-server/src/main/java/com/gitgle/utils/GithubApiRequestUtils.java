package com.gitgle.utils;

import com.gitgle.constant.GithubRestApi;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * Github Api 请求工具类
 */
@Component
public class GithubApiRequestUtils {

    @Value("${github.auth-token}")
    private String authToken;

    private static final String AUTHORIZATION = "Authorization";

    private static final String ACCEPT = "Accept";

    private static final String APPLICATION_VND_GITHUB_JSON = "application/vnd.github+json";

    private static final String X_GITHUB_API_VERSION_KEY = "X-GitHub-Api-Version";

    private static final String X_GITHUB_API_VERSION = "2022-11-28";

    @Resource
    private OkHttpClient httpClient;

    public Response searchUsers(Map<String, String> queryParams) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.SEARCH_USERS.getAddress()).newBuilder();
        for(Map.Entry<String, String> entry : queryParams.entrySet()){
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header(ACCEPT,APPLICATION_VND_GITHUB_JSON)
                .header(AUTHORIZATION,authToken)
                .header(X_GITHUB_API_VERSION_KEY, X_GITHUB_API_VERSION)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }

    public Response searchRepos(Map<String, String> queryParams) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.SEARCH_REPOS.getAddress()).newBuilder();
        for(Map.Entry<String, String> entry : queryParams.entrySet()){
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header(ACCEPT,APPLICATION_VND_GITHUB_JSON)
                .header(AUTHORIZATION,authToken)
                .header(X_GITHUB_API_VERSION_KEY, X_GITHUB_API_VERSION)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }

    public Response searchCommits(Map<String, String> queryParams) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.SEARCH_COMMIT.getAddress()).newBuilder();
        for(Map.Entry<String, String> entry : queryParams.entrySet()){
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header(ACCEPT,APPLICATION_VND_GITHUB_JSON)
                .header(AUTHORIZATION,authToken)
                .header(X_GITHUB_API_VERSION_KEY, X_GITHUB_API_VERSION)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }
}
