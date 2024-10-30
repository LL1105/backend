package com.gitgle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@TableName(value ="nation")
@Data
public class Nation implements Serializable {

    private Integer id;

    private String nation;

    private String avatarNation;
}
