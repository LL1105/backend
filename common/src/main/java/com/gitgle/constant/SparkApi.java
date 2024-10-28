package com.gitgle.constant;

/**
 * 星火大模型接口
 */
public enum SparkApi {

    WEB_API("https://spark-api-open.xf-yun.com/v1/chat/completions","POST");

    private final String url;
    private final String method;

    SparkApi(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }
}
