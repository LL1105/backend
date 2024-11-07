package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitgle.constant.RedisConstant;
import com.gitgle.entity.UserDomain;
import com.gitgle.mapper.UserDomainMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserDomainService implements com.gitgle.service.UserDomainService{

    @Resource
    private UserDomainMapper userDomainMapper;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public IPage<UserDomain> pageUserDomainByDomainId(List<Integer> domainId, Integer page, Integer size) {
        Page<UserDomain> page1 = new Page<>(page, size);
        return userDomainMapper.selectMaxTalentRankByLogin(page1);
    }

    @Override
    public List<UserDomain> getUserDomainByLogin(String login) {
        return userDomainMapper.selectList(Wrappers.lambdaQuery(UserDomain.class).eq(UserDomain::getLogin, login));
    }

    @Override
    public void updateUserDomainTalentRank(String login, Double talentRank) {
        RLock lock = redissonClient.getLock(RedisConstant.USER_DOMAIN_LOGIN_LOCK + login);
        try {
            lock.lock(10, TimeUnit.SECONDS);
            List<UserDomain> userDomains = userDomainMapper.selectList(Wrappers.lambdaQuery(UserDomain.class).eq(UserDomain::getLogin, login));
            for(UserDomain userDomain: userDomains){
                userDomain.setTalentRank(talentRank);
                userDomainMapper.updateById(userDomain);
            }
        }catch (Exception e){
            log.error("更新UserDomain表的TalentRank失败，login：{}, talentRank:{}", login, talentRank);
        }finally {
            lock.unlock();
        }
    }

}
