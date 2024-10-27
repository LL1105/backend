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
 * @TableName repos
 */
@TableName(value ="repos")
@Data
public class Repos implements Serializable {
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
    private String name;

    /**
     * 
     */
    private Boolean orPrivate;

    /**
     * 
     */
    private String ownerlogin;

    /**
     * 
     */
    private Integer starsCount;

    /**
     * 
     */
    private Integer forksCount;

    /**
     * 
     */
    private Integer issueCount;

    /**
     * 
     */
    private String createAt;

    /**
     * 
     */
    private String updateAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}