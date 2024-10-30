package com.gitgle.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName contributor
 */
@TableName(value ="contributor")
@Data
public class Contributor implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private Integer contributorId;

    /**
     * 
     */
    private Integer contributions;

    /**
     * 
     */
    private LocalDateTime createTime;

    /**
     * 
     */
    private LocalDateTime updateTime;

    /**
     * 
     */
    private String repoName;

    /**
     * 
     */
    private String repoOwner;

    /**
     * 
     */
    private String login;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}