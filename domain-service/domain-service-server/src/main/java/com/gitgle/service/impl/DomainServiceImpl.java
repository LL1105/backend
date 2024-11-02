package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitgle.dao.Domain;
import com.gitgle.mapper.DomainMapper;
import com.gitgle.service.DomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

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
        Domain domainOne = domainMapper.selectOne(Wrappers.lambdaQuery(Domain.class).select(Domain::getId).eq(Domain::getDomain, domain));
        if(Objects.nonNull(domainOne)){
            return domainOne.getId();
        }
        return 0;
    }
}
