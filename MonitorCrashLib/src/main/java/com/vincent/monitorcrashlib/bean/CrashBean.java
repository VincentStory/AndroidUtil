package com.vincent.monitorcrashlib.bean;

/**
 * @author : wangwenbo
 * @date : 2022/9/28
 * Desc :
 */
public class CrashBean {
    private String crashName;
    private String stackTrace;
    private String crashHead;

    public String getCrashName() {
        return crashName;
    }

    public void setCrashName(String crashName) {
        this.crashName = crashName;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getCrashHead() {
        return crashHead;
    }

    public void setCrashHead(String crashHead) {
        this.crashHead = crashHead;
    }

    @Override
    public String toString() {
        return "CrashBean{" +
                "crashName='" + crashName + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", crashHead='" + crashHead + '\'' +
                '}';
    }
}
