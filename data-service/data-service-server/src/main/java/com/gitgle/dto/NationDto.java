package com.gitgle.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class NationDto implements Serializable {

    private String location;

    private String login;
}
