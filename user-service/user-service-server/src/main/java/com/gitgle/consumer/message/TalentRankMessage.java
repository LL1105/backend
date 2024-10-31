package com.gitgle.consumer.message;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TalentRankMessage implements Serializable {

    private String login;

    private BigDecimal talentRank;

}
