package com.gitgle.result;

import lombok.Data;

/**
 * 通用接口返回格式，包括响应码，响应消息，响应数据
 * @param <T>
 */
@Data
public final class R<T> {
    private int code;//响应码
    private String msg;//响应消息
    private T data;

    public R() {

    }

    public R(int code){
        this.code = code;
        this.msg = "";
        this.data = null;
    }

    public R(int code, String msg){
        this.code = code;
        this.msg = msg;
        this.data = null;
    }

    public R(int code, String msg, T data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static R Success(Object data){
        return new R(ResultCodeEnum.SUCCESS.getCode(),ResultCodeEnum.SUCCESS.getMessage(),data);
    }

    public static R Success(String message, Object data ){
        return new R(ResultCodeEnum.SUCCESS.getCode(),message,data);
    }

    public static R Success() {
        return Success("");
    }

    public static R Failed(String msg) {
        return new R(ResultCodeEnum.SYSTEM_EXCEPTION.getCode(), msg);
    }

    public static R Failed() {
        return Failed("Failed");
    }

    public static R Failed(int code, String msg) {
        return new R(code, msg);
    }

}
