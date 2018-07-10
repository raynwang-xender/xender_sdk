package cn.xender.core.ap;



public interface ICoreApManager {

    void createAp(int requestCode, CoreCreateApCallback callback);

    void createAp(String ssid, String password, long timeout, int requestCode, CoreCreateApCallback callback);

    void retryCreateAp(String ssid, String password, long timeout, int requestCode, CoreCreateApCallback callback);

    void shutdownAp();

    boolean isApEnabled();

    //7.1 需要手动控制创建失败状态
    void createFailed();

    int getCreateRequestCode();

    String getApName();

    String getApPassword();

    void startScanAp(SSIDFilter f, SSIDDecoder decoder, long timeout, CoreScanApCallback callback, int requestCode);

    void stopScanAp();

    void joinAp(ScanResultItem item, String password, String static_ip, long timeout, CoreJoinApCallback callback);


}
