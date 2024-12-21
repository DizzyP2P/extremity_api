package com.breech.extremity.core.exception;

public class AccountErrorException extends RuntimeException {
    public AccountErrorException(String message) {super(message);}
    public AccountErrorException() {super("账户名或密码错误");}
}
