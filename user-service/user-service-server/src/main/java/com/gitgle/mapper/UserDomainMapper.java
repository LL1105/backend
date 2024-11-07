package com.gitgle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitgle.entity.UserDomain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
* @author maojunjun
* @description 针对表【user_domain】的数据库操作Mapper
* @createDate 2024-10-28 11:36:54
* @Entity com.gitgle.dao.UserDomain
*/
@Mapper
public interface UserDomainMapper extends BaseMapper<com.gitgle.entity.UserDomain> {

    @Select("SELECT login, MAX(talent_rank) AS maxTalentRank " +
            "FROM user_domain " +
            "WHERE domain_id IN (1,2,3,4,5,8,9,10,11,12) " +
            "GROUP BY login " +
            "ORDER BY maxTalentRank DESC")
    IPage<UserDomain> selectMaxTalentRankByLogin(Page<?> page);


}




