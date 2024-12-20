package com.breech.extremity.core.service.redis;


import com.breech.extremity.model.BaseDO;

import java.util.List;

public class RedisResult<T> extends BaseDO {

    private boolean exist = false;

    private T result;

    private List<T> listResult;

    private boolean keyExists = false;

    private T resultObj;

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public List<T> getListResult() {
        return listResult;
    }

    public void setListResult(List<T> listResult) {
        this.listResult = listResult;
    }

    public void setKeyExists(boolean keyExists) {
        this.keyExists = keyExists;
    }

    public boolean isKeyExists() {
        return keyExists;
    }

    public T getResultObj() {
        return resultObj;
    }

    public void setResultObj(T resultObj) {
        this.resultObj = resultObj;
    }
}
