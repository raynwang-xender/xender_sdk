package cn.xender.core.server.utils;

public interface ActionListener{

    void someoneOnline();

    void someoneOffline();

    void transferSuccess();

    void transferFailure();
}