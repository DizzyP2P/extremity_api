package com.breech.extremity.core.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalResultGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalResultGenerator.class);
    public static <T> GlobalResult<T> genResult(boolean success, T data, String message) {
        GlobalResult<T> result = GlobalResult.newInstance();
        result.setSuccess(success);
        result.setData(data);
        result.setMessage(message);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("generate rest result:{}", result);
        }
        return result;
    }

    public static <T> GlobalResult<T> genSuccessResult(T data) {

        return genResult(true, data, null);
    }

    public static <T> GlobalResult<T> genErrorResult(String message) {
        return genResult(false, null, message);
    }

    public static GlobalResult genSuccessResult() {
        return genSuccessResult(null);
    }

    public static <T> GlobalResult<T> genSuccessResult(String message) {

        return genResult(true, null, message);
    }

}
