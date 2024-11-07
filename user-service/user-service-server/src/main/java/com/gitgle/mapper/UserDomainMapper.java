package com.gitgle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitgle.entity.UserDomain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author maojunjun
* @description 针对表【user_domain】的数据库操作Mapper
* @createDate 2024-10-28 11:36:54
* @Entity com.gitgle.dao.UserDomain
*/
@Mapper
public interface UserDomainMapper extends BaseMapper<com.gitgle.entity.UserDomain> {

    @Select("<script>" +
            "SELECT login, MAX(talent_rank) AS maxTalentRank " +
            "FROM user_domain " +
            "WHERE domain_id IN " +
            "<foreach collection='domainIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "GROUP BY login " +
            "ORDER BY maxTalentRank DESC" +
            "</script>")
    IPage<UserDomain> selectMaxTalentRankByLogin(Page<?> page, @Param("domainIds") List<Integer> domainIds);



}




