package com.gitgle.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitgle.entity.UserDomain;

import java.util.List;

public interface UserDomainService {

    IPage<UserDomain> pageUserDomainByDomainId(List<Integer> domainId, Integer page, Integer size);

    List<UserDomain> getUserDomainByLogin(String login);

    void updateUserDomainTalentRank(String login, Double talentRank);
}
