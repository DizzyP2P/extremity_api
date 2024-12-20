package com.breech.extremity.core.exception;

public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException() {
        super("用户无此接口权限");
    }
}
