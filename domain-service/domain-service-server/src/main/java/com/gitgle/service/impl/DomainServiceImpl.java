package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitgle.dao.Domain;
import com.gitgle.mapper.DomainMapper;
import com.gitgle.service.DomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class DomainServiceImpl implements DomainService {

    @Resource
    private DomainMapper domainMapper;

    @Override
    public List<Domain> readAllDomain() {
        List<Domain> domainList = domainMapper.selectList(Wrappers.lambdaQuery(Domain.class));
        return domainList;
    }

    @Override
    public Integer getDomainId(String domain) {
        return domainMapper.selectOne(Wrappers.lambdaQuery(Domain.class).eq(Domain::getDomain, domain)).getId();
    }
}
