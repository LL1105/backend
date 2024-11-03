package com.gitgle.config;

import com.gitgle.constant.RedisConstant;
import io.netty.util.internal.ObjectUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "github.auth-token")
public class GithubAuthToken {

    private List<String> list;

    @Resource
    StringRedisTemplate redisTemplate;

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        if(ObjectUtils.isEmpty(list)) {
            list = redisTemplate.opsForList().range(RedisConstant.GITHUB_TOKEN, 0, -1);
        }
        this.list = list;
    }
}
