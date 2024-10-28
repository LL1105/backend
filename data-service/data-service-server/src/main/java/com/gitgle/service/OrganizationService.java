package com.gitgle.service;

import com.gitgle.dao.Organization;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gitgle.response.GithubOrganization;

/**
* @author maojunjun
* @description 针对表【organization】的数据库操作Service
* @createDate 2024-10-28 21:57:33
*/
public interface OrganizationService{

    void writeGithubOrganization2Organization(GithubOrganization githubOrganization);

    GithubOrganization readOrganization2GithubOrganization(String login);

}
