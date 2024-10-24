package com.gitgle.utils;

import com.gitgle.constant.GithubRestApi;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * Github Api 请求工具类
 */
@Component
public class GithubApiRequestUtils {

    @Resource
    private OkHttpClient httpClient;

    public Response searchUsers(Map<String, String> queryParams) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GithubRestApi.SEARCH_USERS.getAddress()).newBuilder();
        for(Map.Entry<String, String> entry : queryParams.entrySet()){
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header("accept","application/vnd.github+json")
                .header("Authorization","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")
                .header("X-GitHub-Api-Version","2022-11-28")
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }
}
