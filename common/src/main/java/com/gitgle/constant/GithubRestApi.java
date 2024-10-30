package com.gitgle.constant;

/**
 * Github Api 枚举
 */
public enum GithubRestApi {

    SEARCH_USERS("https://api.github.com/search/users", "GET"),
    SEARCH_REPOS("https://api.github.com/search/repositories", "GET"),
    SEARCH_COMMIT("https://api.github.com/search/commits","GET"),
    GET_USERS("https://api.github.com/users","GET"),
    GET_USER("https://api.github.com/user","GET"),
    GET_ONE_REPO("https://api.github.com/repos","GET");

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
