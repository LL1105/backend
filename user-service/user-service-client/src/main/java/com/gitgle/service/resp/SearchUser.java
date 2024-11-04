package com.gitgle.service.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchUser implements Serializable {

    private String login;

    private String avatar;

    private String talentRank;

    private List<String> domains;

    private String nation;

    private String location;

    private String nationEnglish;

}
