package com.gitgle.constant;

public enum RpcResultCode {

    SUCCESS(200, "SUCCESS"),
    Github_RESPONSE_FAILED(510, "Github Response Failed"),
    REQUEST_SPARK_FAILED(511, "Request Spark Failed"),
    FAILED(500, "FAILED");

    private final int code;
    private final String msg;

    RpcResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
