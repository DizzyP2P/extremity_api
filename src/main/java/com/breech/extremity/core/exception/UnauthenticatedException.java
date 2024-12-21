package com.breech.extremity.core.exception;

public class UnauthenticatedException extends AuthenticationException{
    public UnauthenticatedException() {
        super("用户未登陆");
    }
}
