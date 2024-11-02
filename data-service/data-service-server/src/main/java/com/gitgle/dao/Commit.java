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
 * @TableName commit
 */
@TableName(value ="commit")
@Data
public class Commit implements Serializable {
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
    private String authorLogin;

    /**
     * 
     */
    private Integer reposId;

    /**
     * 
     */
    private String reposName;

    /**
     * 
     */
    private String reposOwner;

    /**
     * 
     */
    private String commitDateTime;

    /**
     * 
     */
    private String sha;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}