package com.breech.extremity.core.exception;


import com.breech.extremity.core.response.NormalResponseCode;

/**
 * 服务（业务）异常如“ 账号或密码错误 ”，该异常只做INFO级别的日志记录 @see WebMvcConfigurer
 *
 * @author ronger
 */
public class ServiceException extends RuntimeException {
    private int code;
    private String extraMessage;

    public ServiceException(NormalResponseCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(int code, String message, String extraMessage, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.extraMessage = extraMessage;
    }


    public ServiceException(NormalResponseCode resultCode, String extraMessage) {
        this(resultCode.getCode(), resultCode.getMessage(), extraMessage, null);
    }

    public ServiceException(String extraMessage) {
        this(NormalResponseCode.INVALID_PARAM, extraMessage);
    }

    public int getCode() {
        return code;
    }

    public String getExtraMessage() {
        return extraMessage;
    }
}
