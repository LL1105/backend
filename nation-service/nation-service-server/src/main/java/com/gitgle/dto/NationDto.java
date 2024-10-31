package com.gitgle.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class NationDto implements Serializable {

    private String nation;

    private String login;

    private String nationEnglish;

    private Double confidence;
}
