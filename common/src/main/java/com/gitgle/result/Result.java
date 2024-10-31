package com.gitgle.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用接口返回格式，包括响应码，响应消息，响应数据
 * @param <T>
 */
@Data
public final class Result<T> implements Serializable {
    private int code;//响应码
    private String msg;//响应消息
    private T data;

    public Result() {

    }

    public Result(int code){
        this.code = code;
        this.msg = "";
        this.data = null;
    }

    public Result(int code, String msg){
        this.code = code;
        this.msg = msg;
        this.data = null;
    }

    public Result(int code, String msg, T data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static Result Success(Object data){
        return new Result(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage(),data);
    }

    public static Result Success(String message, Object data ){
        return new Result(ResultCodeEnum.SUCCESS.getCode(),message,data);
    }

    public static Result Success() {
        return Success("");
    }

    public static Result Failed(String msg) {
        return new Result(ResultCodeEnum.SYSTEM_EXCEPTION.getCode(), msg);
    }

    public static Result Failed() {
        return Failed("Failed");
    }

    public static Result Failed(int code, String msg) {
        return new Result(code, msg);
    }

}
