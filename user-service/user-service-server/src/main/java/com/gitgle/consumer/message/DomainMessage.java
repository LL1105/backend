package com.gitgle.consumer.message;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DomainMessage implements Serializable {

    private String login;

    private String domain;

    private BigDecimal confidence;
}
