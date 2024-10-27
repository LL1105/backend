package com.gitgle.rank.controller;

import com.gitgle.result.R;
import com.gitgle.service.UserService;
import com.gitgle.service.VO.req.RankSeq;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rank")
public class RankController {

    @DubboReference
    UserService userService;


    @PostMapping("/")
    public R sendEmail(@RequestParam("size") Integer size, @RequestParam("current") Integer current, @RequestBody RankSeq req) {
        return userService.conditionCheckRank(size, current, req);
    }
}
