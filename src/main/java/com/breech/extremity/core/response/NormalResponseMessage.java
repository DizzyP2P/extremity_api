package com.breech.extremity.core.response;

public enum NormalResponseMessage {
    SUCCESS("操作成功！"),
    FAIL("操作失败！");

    private String message;
    NormalResponseMessage(String message) {
        this.message = message;
    }

}
