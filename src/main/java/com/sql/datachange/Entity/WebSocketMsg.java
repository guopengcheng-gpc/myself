package com.sql.datachange.Entity;

public class WebSocketMsg {
    //消息类型 1为直接发送2为替换字段
    private Integer messageType;
    //1为继续 2为停止
    private Integer code;
    //消息内容
    private String message;

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
