package com.gitgle.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitgle.constant.SparkApi;
import com.gitgle.utils.dto.SparkRequest;
import com.gitgle.utils.dto.SparkRequestMessage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SparkApiUtils {

    @Resource
    private OkHttpClient httpClient;

    private static final String MODEL = "generalv3.5";

    private static final String APPID = "5645e836";

    private static final String CONTENT_TYPE_KEY = "Content-Type";

    private static final String CONTENT_TYPE = "application/json";

    private static final String AUTHORIZATION_KEY = "Authorization";

    private static final String AUTHORIZATION = "Bearer YfEqSHwUBXQFoERichVg:sWSsXOrOExmCfmZGfjCu";

    public Response doRequest(String content) throws IOException {
        SparkRequest sparkRequest = new SparkRequest();
        sparkRequest.setModel(MODEL);
        sparkRequest.setUser(APPID);
        SparkRequestMessage sparkRequestMessage = new SparkRequestMessage();
        sparkRequestMessage.setRole("user");
        sparkRequestMessage.setContent(content);
        List<SparkRequestMessage> sparkRequestMessageList = new ArrayList<>();
        sparkRequestMessageList.add(sparkRequestMessage);
        sparkRequest.setMessages(sparkRequestMessageList);
        String json = new ObjectMapper().writeValueAsString(sparkRequest);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(SparkApi.WEB_API.getUrl()).newBuilder();
        String url = urlBuilder.build().toString();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.Builder()
                .header(CONTENT_TYPE_KEY, CONTENT_TYPE)
                .header(AUTHORIZATION_KEY, AUTHORIZATION)
                .post(requestBody)
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response;
    }
}
