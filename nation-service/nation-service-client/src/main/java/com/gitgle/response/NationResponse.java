package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class NationResponse implements Serializable {

    private String location;

    private String nation;

    private String nationEnglish;

    private Double confidence;

}
