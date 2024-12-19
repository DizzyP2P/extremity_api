package com.breech.extremity.core.exception;
public class NicknameOccupyException extends BusinessException {
    private static final long serialVersionUID = 3206744387536223284L;
    public NicknameOccupyException() {
    }
    public NicknameOccupyException(String message) {
        super(message);
    }
    public NicknameOccupyException(String message, Throwable cause) {
        super(message, cause);
    }
    public NicknameOccupyException(Throwable cause) {
        super(cause);
    }
    public NicknameOccupyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
