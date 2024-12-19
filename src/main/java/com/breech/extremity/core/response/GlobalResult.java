package com.breech.extremity.core.response;

import lombok.Data;
@Data
public class GlobalResult<T> {
    private boolean success = false;
    private T data;
    private int code;
    private String message;

    public GlobalResult() {
    }

    public GlobalResult(NormalResponseCode resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public static <T> GlobalResult<T> newInstance() {
        return new GlobalResult<>();
    }
}
