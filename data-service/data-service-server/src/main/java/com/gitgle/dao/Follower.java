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
 * @TableName follower
 */
@TableName(value ="follower")
@Data
public class Follower implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 关注用户的用户名
     */
    private String login;

    /**
     * 用户的用户名（就是被关注的）
     */
    private String username;

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
    private String avatarUrl;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}