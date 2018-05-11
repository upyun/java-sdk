package com.upyun;

public class Result {
    public boolean isSucceed() {
        return succeed;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private boolean succeed;
    private int code;
    private String msg;

    @Override
    public String toString() {
        return "Result{" +
                "succeed=" + succeed +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}

