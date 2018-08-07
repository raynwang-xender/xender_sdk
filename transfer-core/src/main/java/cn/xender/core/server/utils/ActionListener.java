package cn.xender.core.server.utils;

public interface ActionListener{

    void someoneOnline();

    void someoneOffline();

    void transferSuccess(String android_id,String channel);

    void transferFailure();
}