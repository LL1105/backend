package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.dao.Organization;
import com.gitgle.response.GithubOrganization;
import com.gitgle.service.OrganizationService;
import com.gitgle.mapper.OrganizationMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author maojunjun
* @description 针对表【organization】的数据库操作Service实现
* @createDate 2024-10-28 21:57:33
*/
@Service
public class OrganizationServiceImpl implements OrganizationService{

    @Resource
    private OrganizationMapper organizationMapper;

    @Override
    public void writeGithubOrganization2Organization(GithubOrganization githubOrganization) {
        Organization organization = organizationMapper.selectOne(Wrappers.lambdaQuery(Organization.class).eq(Organization::getLogin, githubOrganization.getLogin()));
        if(ObjectUtils.isNotEmpty(organization)){
            return;
        }
        organization = new Organization();
        organization.setLogin(githubOrganization.getLogin());
        organization.setAvatarUrl(githubOrganization.getAvatarUrl());
        organization.setDescription(githubOrganization.getDescription());
        organizationMapper.insert(organization);
    }

    @Override
    public List<GithubOrganization> readOrganization2GithubOrganization(String login) {
        List<Organization> organizations = organizationMapper.selectList(Wrappers.lambdaQuery(Organization.class).eq(Organization::getLogin, login));
        return organizations.stream().map(organization -> {
            GithubOrganization githubOrganization = new GithubOrganization();
            githubOrganization.setLogin(organization.getLogin());
            githubOrganization.setAvatarUrl(organization.getAvatarUrl());
            githubOrganization.setDescription(organization.getDescription());
            return githubOrganization;
        }).collect(Collectors.toList());
    }
}




