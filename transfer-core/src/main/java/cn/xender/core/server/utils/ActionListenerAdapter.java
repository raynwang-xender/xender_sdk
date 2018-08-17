package cn.xender.core.server.utils;

public abstract class ActionListenerAdapter implements ActionListener{

    @Override
    public void someoneOnline() {

    }

    @Override
    public void someoneOffline() {

    }


    @Override
    public void transferSuccess(String android_id, String channel, String filePath) {

    }

    @Override
    public void transferFailure(String filePath) {

    }

    @Override
    public void transferAll(String android_id, String channel) {

    }
}