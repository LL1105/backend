package com.gitgle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitgle.entity.GithubUser;
import com.gitgle.service.req.SearchReq;
import com.gitgle.service.resp.SearchResp;
import com.gitgle.service.resp.SearchUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GithubUserMapper extends BaseMapper<GithubUser> {

    List<SearchUser> searchByCondition(int current, int size, SearchReq req);

    Integer searchCount(SearchReq searchReq);
}
