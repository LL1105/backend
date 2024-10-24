package com.gitgle.constant;

/**
 * Github Api 枚举
 */
public enum GithubRestApi {

    SEARCH_USERS("https://api.github.com/search/users", "GET");

    private final String address;

    private final String method;

    GithubRestApi(String address, String method)
    {
        this.address = address;
        this.method = method;
    }

    public String getAddress()
    {
        return address;
    }

    public String getMethod()
    {
        return method;
    }
}
