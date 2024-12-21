package com.breech.extremity.core.exception;

public class UnknownAccountException extends AuthenticationException{
    public UnknownAccountException(String message) {
        super("未知账号");
    }
}
