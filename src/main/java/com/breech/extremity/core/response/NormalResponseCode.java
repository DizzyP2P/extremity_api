package com.breech.extremity.core.response;

public enum NormalResponseCode{
    SUCCESS(200, "SUCCESS"),
    FAIL(400, "访问失败"),
    UNAUTHENTICATED(401, "签名错误"),
    UNAUTHORIZED(403, "用户无权限"),
    NOT_FOUND(404, "此接口不存在"),
    INTERNAL_SERVER_ERROR(500, "系统繁忙,请稍后再试"),
    INVALID_PARAM(666, "参数错误"),
    UNKNOWN_ACCOUNT(777, "未知账号"),
    INCORRECT_ACCOUNT_OR_PASSWORD(888, "账号或密码错误");

    private final int code;
    private final String message;

    NormalResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
