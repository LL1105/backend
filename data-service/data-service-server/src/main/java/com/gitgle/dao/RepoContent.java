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
 * @TableName repo_content
 */
@TableName(value ="repo_content")
@Data
public class RepoContent implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String path;

    /**
     * 
     */
    private String sha;

    /**
     * 
     */
    private String content;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}