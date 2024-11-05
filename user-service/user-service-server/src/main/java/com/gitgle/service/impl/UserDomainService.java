package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitgle.entity.UserDomain;
import com.gitgle.mapper.UserDomainMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class UserDomainService implements com.gitgle.service.UserDomainService{

    @Resource
    private UserDomainMapper userDomainMapper;

    @Override
    public Page<UserDomain> pageUserDomainByDomainId(List<Integer> domainId, Integer page, Integer size) {
        Page<UserDomain> page1 = new Page<>(page, size);
        Page<UserDomain> userDomainPage = userDomainMapper.selectPage(page1, Wrappers.lambdaQuery(UserDomain.class).in(UserDomain::getDomainId, domainId));
        return userDomainPage;
    }

    @Override
    public List<UserDomain> getUserDomainByLoginAndDomainId(String login, Integer domainId) {
        return Collections.emptyList();
    }

    @Override
    public List<UserDomain> getUserDomainByLogin(String login) {
        return userDomainMapper.selectList(Wrappers.lambdaQuery(UserDomain.class).eq(UserDomain::getLogin, login));
    }

}
