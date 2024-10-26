package com.gitgle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitgle.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    //插入用户数据
    @Insert("insert into user (username, password, email) values (#{username}, #{password}, #{email})")
    int insertUser(User user);

}