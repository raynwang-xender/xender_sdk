package cn.xender.core.ap;

/**
 * Created by wangshangang on 2018/6/21.
 */
public interface ICreateApWorker {


    String TAG = "open_ap";

    boolean isApEnabled();

    void createAp(String ssid, String password, long timeout, int requestCode, CoreCreateApCallback callback);

    void retryCreateAp(String ssid, String password, long timeout, int requestCode, CoreCreateApCallback callback);

    int getCurrentRequestCode();

    void closeAp();

    void createFailed();

    String getApPassword();

    String getApName();

}
