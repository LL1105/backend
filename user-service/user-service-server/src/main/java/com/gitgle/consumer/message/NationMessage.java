package com.gitgle.consumer.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class NationMessage implements Serializable {

    private String login;

    private String nation;

    private String nationEnglish;

    private Double confidence;

    private String location;

}
