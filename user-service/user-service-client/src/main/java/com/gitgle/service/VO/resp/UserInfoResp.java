package com.gitgle.service.VO.resp;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfoResp implements Serializable {

    private Integer id;

    private String username;

    private String email;

    private String githubId;
}
