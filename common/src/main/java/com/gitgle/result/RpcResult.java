package com.gitgle.result;

import com.gitgle.constant.RpcResultCode;
import lombok.Data;

import java.io.Serializable;

/**
 * RPC响应对象封装类
 * @param <T>
 */
@Data
public class RpcResult<T> implements Serializable {

    private RpcResultCode code;
    private String msg;
    private T data;
}
