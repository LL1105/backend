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
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

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
    private String login;

    /**
     * 
     */
    private String avatarUrl;

    /**
     * 
     */
    private String locationn;

    /**
     * 
     */
    private String bio;

    /**
     * 
     */
    private String company;

    /**
     * 
     */
    private String htmlUrl;

    /**
     * 
     */
    private String createdAt;

    /**
     * 
     */
    private String email;

    /**
     * 
     */
    private Integer publicRepos;

    /**
     * 
     */
    private Integer accountId;

    /**
     * 
     */
    private Integer followers;

    /**
     * 
     */
    private Integer following;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}