package com.gitgle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @TableName github_user
 */
@TableName(value ="github_user")
@Data
public class GithubUser implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *
     */
    private String login;

    /**
     *
     */
    private String avatar;

    private BigDecimal talentRank;

    private String nation;

    private BigDecimal nationConfidence;

    private String nationEnglish;

    private String location;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}