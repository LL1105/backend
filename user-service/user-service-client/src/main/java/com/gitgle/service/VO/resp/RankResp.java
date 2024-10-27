package com.gitgle.service.VO.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RankResp implements Serializable {

    private Integer userId;

    private String userName;

    private String nation;

    private String domain;

    private BigDecimal talentRank;

    private String githubId;

}
