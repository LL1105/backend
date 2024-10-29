package com.gitgle.utils;

import com.gitgle.config.GithubAuthToken;
import com.gitgle.constant.GithubRestApi;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Github Api 请求工具类
 */
@Component
public class GithubApiRequestUtils {

    @Resource
    private GithubAuthToken githubAuthToken;

    private static Integer loadBalanceIndex = 0;

    private static final String AUTHORIZATION = "Authorization";

    private static final String ACCEPT = "Accept";

    private static final String APPLICATION_VND_GITHUB_JSON = "application/vnd.github+json";

    private static final String X_GITHUB_API_VERSION_KEY = "X-GitHub-Api-Version";

    private static final String X_GITHUB_API_VERSION = "2022-11-28";

    @Resource
    private OkHttpClient httpClient;

    private String loadBalanceAuthToken() {
        String token = githubAuthToken.getList().get(loadBalanceIndex);
        loadBalanceIndex = (loadBalanceIndex + 1) % githubAuthToken.getList().size();
        return token;
    }

    public Response searchUsers(Map<String, String> queryParams) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.SEARCH_USERS.getAddress()).newBuilder();
        for(Map.Entry<String, String> entry : queryParams.entrySet()){
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header(ACCEPT,APPLICATION_VND_GITHUB_JSON)
                .header(AUTHORIZATION,loadBalanceAuthToken())
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
                .header(AUTHORIZATION,loadBalanceAuthToken())
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
                .header(AUTHORIZATION,loadBalanceAuthToken())
                .header(X_GITHUB_API_VERSION_KEY, X_GITHUB_API_VERSION)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }

    public Response getOneRepo(String owner, String repoName) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_ONE_REPO.getAddress() + "/" + owner + "/" + repoName).newBuilder();
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header(ACCEPT,APPLICATION_VND_GITHUB_JSON)
                .header(AUTHORIZATION,loadBalanceAuthToken())
                .header(X_GITHUB_API_VERSION_KEY, X_GITHUB_API_VERSION)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }

    public Response listCommit(String owner, String repoName, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_ONE_REPO.getAddress() + "/" + owner + "/" + repoName + "/commits").newBuilder();
        for(Map.Entry<String, String> entry : params.entrySet()){
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header(ACCEPT,APPLICATION_VND_GITHUB_JSON)
                .header(AUTHORIZATION,loadBalanceAuthToken())
                .header(X_GITHUB_API_VERSION_KEY, X_GITHUB_API_VERSION)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }

    public Response getRepoContent(String owner, String repoName, String path) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_ONE_REPO.getAddress() + "/" + owner + "/" + repoName + "/contents/" + path).newBuilder();
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header(ACCEPT,APPLICATION_VND_GITHUB_JSON)
                .header(AUTHORIZATION,loadBalanceAuthToken())
                .header(X_GITHUB_API_VERSION_KEY, X_GITHUB_API_VERSION)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }

    public Response getUserFollowers(String login, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_USERS.getAddress() + "/" + login + "/followers").newBuilder();
        for(Map.Entry<String, String> entry : params.entrySet()){
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header(ACCEPT,APPLICATION_VND_GITHUB_JSON)
                .header(AUTHORIZATION,loadBalanceAuthToken())
                .header(X_GITHUB_API_VERSION_KEY, X_GITHUB_API_VERSION)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }

    public Response listOrganizationForUser(String username, Map<String, String> param) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_USERS.getAddress() + "/" + username + "/orgs").newBuilder();
        for (Map.Entry<String, String> entry : param.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header(ACCEPT,APPLICATION_VND_GITHUB_JSON)
                .header(AUTHORIZATION,loadBalanceAuthToken())
                .header(X_GITHUB_API_VERSION_KEY, X_GITHUB_API_VERSION)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }

    public Response listUserFollowing(String login, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_USERS.getAddress() + "/" + login + "/following").newBuilder();
        for(Map.Entry<String, String> entry : params.entrySet()){
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header(ACCEPT,APPLICATION_VND_GITHUB_JSON)
                .header(AUTHORIZATION,loadBalanceAuthToken())
                .header(X_GITHUB_API_VERSION_KEY, X_GITHUB_API_VERSION)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }

    public Response getUserByUsername(String username) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_USERS.getAddress() + "/" + username).newBuilder();
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header(ACCEPT,APPLICATION_VND_GITHUB_JSON)
                .header(AUTHORIZATION,loadBalanceAuthToken())
                .header(X_GITHUB_API_VERSION_KEY, X_GITHUB_API_VERSION)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }
}
