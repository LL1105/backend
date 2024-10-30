package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class GithubLanguagesResponse implements Serializable {

    private Map<String, Integer> languagesMap;
}
