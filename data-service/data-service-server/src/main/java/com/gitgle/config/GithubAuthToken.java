package com.gitgle.config;

import com.gitgle.constant.RedisConstant;
import io.netty.util.internal.ObjectUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

@Component
public class GithubAuthToken {

    @Getter
    private List<String> list;

    @Autowired
    @Qualifier("stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void init() {
        this.list = stringRedisTemplate.opsForList().range(RedisConstant.GITHUB_TOKEN, 0, -1);
    }
}
