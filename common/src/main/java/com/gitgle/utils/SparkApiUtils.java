package com.gitgle.utils;

import com.gitgle.constant.SparkApi;
import com.gitgle.utils.dto.SparkRequest;
import com.gitgle.utils.dto.SparkRequestMessage;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class SparkApiUtils {

    @Resource
    private OkHttpClient httpClient;

    private static final String CONTENT_TYPE_KEY = "Content-Type";

    private static final String CONTENT_TYPE = "application/json";

    private static final String AUTHORIZATION_KEY = "Authorization";

    private static final String AUTHORIZATION = "Bearer 123456";

    public Response doRequest(String content) throws IOException {
        SparkRequest sparkRequest = new SparkRequest();
        sparkRequest.setModel("generalv3.5");
        sparkRequest.setUser("32bccb4c");
        SparkRequestMessage sparkRequestMessage = new SparkRequestMessage();
        sparkRequestMessage.setRole("user");
        sparkRequestMessage.setContent("你好");
        HttpUrl.Builder urlBuilder = HttpUrl.parse(SparkApi.WEB_API.getUrl()).newBuilder();
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .header(CONTENT_TYPE_KEY, CONTENT_TYPE)
                .header(AUTHORIZATION_KEY, AUTHORIZATION)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }
}
