package com.gitgle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.dao.Organization;
import com.gitgle.response.GithubOrganization;
import com.gitgle.service.OrganizationService;
import com.gitgle.mapper.OrganizationMapper;
import org.springframework.stereotype.Service;

/**
* @author maojunjun
* @description 针对表【organization】的数据库操作Service实现
* @createDate 2024-10-28 21:57:33
*/
@Service
public class OrganizationServiceImpl implements OrganizationService{

    @Override
    public void writeGithubOrganization2Organization(GithubOrganization githubOrganization) {

    }

    @Override
    public GithubOrganization readOrganization2GithubOrganization(String login) {
        return null;
    }
}




