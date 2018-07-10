package cn.xender.core.server.utils;

public class Port {

    private static int WEB_PORT = 6528;

    public static void setWebPort(int webPort) {
        WEB_PORT = webPort;
    }

    public static int getWebPort() {
        return WEB_PORT;
    }

    public static void setToDefault(){
        WEB_PORT = 6528;
    }
}
