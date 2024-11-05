package com.gitgle.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gitgle.config.GithubAuthToken;
import com.gitgle.constant.GithubRestApi;
import com.gitgle.convert.GithubContributorConvert;
import com.gitgle.convert.GithubRepoConvert;
import com.gitgle.response.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Github Api 请求工具类
 */
@Component
@Slf4j
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

    /**
     * 负载均衡获取token
     */
    private String loadBalanceAuthToken() {
        String token = githubAuthToken.getList().get(loadBalanceIndex);
        loadBalanceIndex = (loadBalanceIndex + 1) % githubAuthToken.getList().size();
        return "Bearer " + token;
    }

    /**
     * 搜索用户
     */
    public GithubUserResponse searchUsers(Map<String, String> queryParams) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.SEARCH_USERS.getAddress()).newBuilder();
        buildQueryParams(queryParams, urlBuilder);
        String url = urlBuilder.build().toString();
        Response response = httpClient.newCall(buildRequest(url)).execute();
        GithubUserResponse githubUserResponse = new GithubUserResponse();
        if (!response.isSuccessful()) {
            throw new IOException("Github API 搜索用户失败：" + response.body().string());
        }
        JSONObject responseBody = JSON.parseObject(response.body().string());
        List<GithubUser> githubUserList = JSON.parseArray(responseBody.getJSONArray("items").toString(), GithubUser.class);
        githubUserResponse.setGithubUserList(githubUserList);
        return githubUserResponse;
    }

    public Integer getGithubUserTotal() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("q", "type:user");
        params.put("page", "1");
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.SEARCH_USERS.getAddress()).newBuilder();
        buildQueryParams(params, urlBuilder);
        String url = urlBuilder.build().toString();
        Response response = httpClient.newCall(buildRequest(url)).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Github API 获取用户总数失败: " + response.body().string());
        }
        JSONObject responseBody = JSON.parseObject(response.body().string());
        return responseBody.getInteger("total_count");
    }

    /**
     * 搜索仓库
     */
    public Response searchRepos(Map<String, String> queryParams) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.SEARCH_REPOS.getAddress()).newBuilder();
        buildQueryParams(queryParams, urlBuilder);
        String url = urlBuilder.build().toString();
        return httpClient.newCall(buildRequest(url)).execute();
    }

    /**
     * 搜索代码
     */
    public Response searchCode(Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.SEARCH_CODE.getAddress()).newBuilder();
        buildQueryParams(params, urlBuilder);
        String url = urlBuilder.build().toString();
        return httpClient.newCall(buildRequest(url)).execute();
    }

    /**
     * 搜索提交记录
     */
    public Response searchCommits(Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.SEARCH_COMMIT.getAddress()).newBuilder();
        buildQueryParams(params, urlBuilder);
        String url = urlBuilder.build().toString();
        return httpClient.newCall(buildRequest(url)).execute();
    }

    /**
     * 获取一个仓库信息
     */
    public GithubRepos getOneRepo(String owner, String repoName) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_ONE_REPO.getAddress() + "/" + owner + "/" + repoName).newBuilder();
        String url = urlBuilder.build().toString();
        Response response = httpClient.newCall(buildRequest(url)).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Github API 获取仓库信息失败： " + response.body().string());
        }
        JSONObject responseBody = JSON.parseObject(response.body().string());
        return GithubRepoConvert.convert(responseBody);
    }

    /**
     * 列出仓库提交记录
     */
    public Response listCommit(String owner, String repoName, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_ONE_REPO.getAddress() + "/" + owner + "/" + repoName + "/commits").newBuilder();
        buildQueryParams(params, urlBuilder);
        String url = urlBuilder.build().toString();
        return httpClient.newCall(buildRequest(url)).execute();
    }

    /**
     * 获取仓库文件内容
     */
    public JSONObject getRepoContent(String owner, String repoName, String path) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_ONE_REPO.getAddress() + "/" + owner + "/" + repoName + "/contents/" + path).newBuilder();
        String url = urlBuilder.build().toString();
        Response response = httpClient.newCall(buildRequest(url)).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Github API 获取仓库文件内容失败: " + response.body().string());
        }
        JSONObject responseBody = JSON.parseObject(response.body().string());
        return responseBody;
    }

    /**
     * 列出用户的粉丝
     */
    public Response getUserFollowers(String login, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_USERS.getAddress() + "/" + login + "/followers").newBuilder();
        buildQueryParams(params, urlBuilder);
        String url = urlBuilder.build().toString();
        return httpClient.newCall(buildRequest(url)).execute();
    }

    /**
     * 列出用户的组织
     */
    public Response listOrganizationForUser(String username, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_USERS.getAddress() + "/" + username + "/orgs").newBuilder();
        buildQueryParams(params, urlBuilder);
        String url = urlBuilder.build().toString();
        return httpClient.newCall(buildRequest(url)).execute();
    }

    /**
     * 列出用户关注的人
     */
    public Response listUserFollowing(String login, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_USERS.getAddress() + "/" + login + "/following").newBuilder();
        buildQueryParams(params, urlBuilder);
        String url = urlBuilder.build().toString();
        return httpClient.newCall(buildRequest(url)).execute();
    }

    /**
     * 根据login获取用户信息
     */
    public GithubUser getUserByUsername(String username) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_USERS.getAddress() + "/" + username).newBuilder();
        String url = urlBuilder.build().toString();
        Response response = httpClient.newCall(buildRequest(url)).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Github API 根据Login获取用户信息失败: " + response.body().string());
        }
        JSONObject responseBody = JSON.parseObject(response.body().string());
        return JSON.parseObject(responseBody.toString(), GithubUser.class);
    }

    /**
     * 根据github的id查询用户详细信息
     */
    public GithubUser getUserByAccountId(Integer accountId) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_USER.getAddress() + "/" + accountId).newBuilder();
        String url = urlBuilder.build().toString();
        Response response = httpClient.newCall(buildRequest(url)).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Github 根据AccountId获取用户信息失败: " + response.body().string());
        }
        JSONObject responseBody = JSON.parseObject(response.body().string());
        return JSON.parseObject(responseBody.toString(), GithubUser.class);
    }

    /**
     * 列出用户的仓库
     */
    public GithubReposResponse listUserRepos(String owner, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_USERS.getAddress() + "/" + owner + "/repos").newBuilder();
        GithubReposResponse githubReposResponse = new GithubReposResponse();
        List<GithubRepos> githubProjectList = new ArrayList<>();
        params.put("per_page", "100");
        Integer page = 1;
        while (true) {
            params.put("page", String.valueOf(page));
            buildQueryParams(params, urlBuilder);
            String url = urlBuilder.build().toString();
            Response response = httpClient.newCall(buildRequest(url)).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Github API 列出用户仓库失败: " + response.body().string());
            }
            JSONArray responseBody = JSON.parseArray(response.body().string());
            for (int i = 0; i < responseBody.size(); i++) {
                JSONObject item = responseBody.getJSONObject(i);
                GithubRepos githubRepos = GithubRepoConvert.convert(item);
                githubProjectList.add(githubRepos);
            }
            if (responseBody.size() < 100) {
                break;
            }
            page++;
        }
        githubReposResponse.setGithubProjectList(githubProjectList);
        return githubReposResponse;
    }

    /**
     * 获取仓库贡献者
     */
    public GithubContributorResponse listRepoContributors(String owner, String repoName, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_ONE_REPO.getAddress() + "/" + owner + "/" + repoName + "/contributors").newBuilder();
        GithubContributorResponse githubContributorResponse = new GithubContributorResponse();
        List<GithubContributor> githubContributorList = new ArrayList<>();
        params.put("per_page", "100");
        params.put("anon", "false");
        Integer page = 1;
        while (true) {
            params.put("page", String.valueOf(page));
            buildQueryParams(params, urlBuilder);
            String url = urlBuilder.build().toString();
            Response response = httpClient.newCall(buildRequest(url)).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Github API 列出仓库贡献者失败: " + response.body().string());
            }
            JSONArray responseBody = JSON.parseArray(response.body().string());
            for (int i = 0; i < responseBody.size(); i++) {
                JSONObject item = responseBody.getJSONObject(i);
                GithubContributor githubContributor = GithubContributorConvert.convert(item, repoName, owner);
                githubContributorList.add(githubContributor);
            }
            if (responseBody.size() < 100) {
                break;
            }
            page++;
        }
        githubContributorResponse.setGithubContributorList(githubContributorList);
        return githubContributorResponse;
    }

    /**
     * 获取仓库语言
     */
    public GithubLanguagesResponse listRepoLanguages(String owner, String repoName) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.GET_ONE_REPO.getAddress() + "/" + owner + "/" + repoName + "/languages").newBuilder();
        GithubLanguagesResponse githubLanguagesResponse = new GithubLanguagesResponse();
        String url = urlBuilder.build().toString();
        Response response = httpClient.newCall(buildRequest(url)).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Github API 列出仓库语言失败: " + response.body().string());
        }
        JSONObject responseBody = JSON.parseObject(response.body().string());
        Map<String, Integer> githubLanguagesMap = responseBody.getInnerMap().entrySet().stream()
                .filter(entry -> entry.getValue() instanceof Number) // 过滤出值为数字的键值对
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> ((Number) entry.getValue()).intValue()  // 将值转换为 Integer 类型
                ));
        githubLanguagesResponse.setLanguagesMap(githubLanguagesMap);
        return githubLanguagesResponse;
    }

    private void buildQueryParams(Map<String, String> params, HttpUrl.Builder urlBuilder) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
    }

    private Request buildRequest(String url) {
        return new Request.Builder()
                .header(ACCEPT, APPLICATION_VND_GITHUB_JSON)
                .header(AUTHORIZATION, loadBalanceAuthToken())
                .header(X_GITHUB_API_VERSION_KEY, X_GITHUB_API_VERSION)
                .url(url)
                .build();
    }
}
