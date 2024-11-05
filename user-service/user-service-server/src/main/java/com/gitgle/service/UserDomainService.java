package com.gitgle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitgle.entity.UserDomain;

import java.util.List;

public interface UserDomainService {

    Page<UserDomain> pageUserDomainByDomainId(List<Integer> domainId, Integer page, Integer size);

    List<UserDomain> getUserDomainByLoginAndDomainId(String login, Integer domainId);

    List<UserDomain> getUserDomainByLogin(String login);
}
