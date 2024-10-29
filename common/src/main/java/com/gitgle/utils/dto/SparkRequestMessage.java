package com.gitgle.utils.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SparkRequestMessage implements Serializable {

    private String role;

    private String content;
}
