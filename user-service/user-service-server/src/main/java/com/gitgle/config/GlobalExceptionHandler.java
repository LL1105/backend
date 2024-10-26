package com.gitgle.config;

import cn.dev33.satoken.exception.NotLoginException;

import com.gitgle.result.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获 NotLoginException 异常
     * @param e NotLoginException
     * @return R
     */
    @ExceptionHandler(NotLoginException.class)
    public R handleNotLoginException(NotLoginException e) {
        // 可以根据异常信息来定制返回的错误信息
        if(e.getType().equals(NotLoginException.NOT_TOKEN)){
            return R.Failed("未提供Token");
        }else if(e.getType().equals(NotLoginException.INVALID_TOKEN)){
            return R.Failed("未提供有效Token");
        }else if(e.getType().equals(NotLoginException.TOKEN_TIMEOUT)){
            return R.Failed("登录信息已过期，请重新登录");
        }else if(e.getType().equals(NotLoginException.BE_REPLACED)){
            return R.Failed("您的账户已在另一台设备上登录，如非本人操作，请立即修改密码");
        }else if(e.getType().equals(NotLoginException.KICK_OUT)){
            return R.Failed("已被系统强制下线");
        }else{
            return R.Failed();
        }

    }

    // 可以添加更多的异常处理器方法，处理其他类型的异常
}
