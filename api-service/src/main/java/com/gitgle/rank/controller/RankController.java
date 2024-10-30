package com.gitgle.rank.controller;

import com.gitgle.result.Result;
import com.gitgle.service.UserService;
import com.gitgle.service.vo.req.RankReq;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rank")
public class RankController {

    @DubboReference
    UserService userService;


    @PostMapping()
    public Result sendEmail(@RequestParam("size") Integer size, @RequestParam("current") Integer current, @RequestBody RankReq req) {
        return userService.conditionCheckRank(size, current, req);
    }
}
