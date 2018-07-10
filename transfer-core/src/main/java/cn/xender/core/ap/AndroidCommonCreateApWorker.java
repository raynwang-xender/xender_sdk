package cn.xender.core.ap;

import android.content.Context;

import cn.xender.core.ap.utils.UserApConfigurationUtil;
import cn.xender.core.log.Logger;

/**
 * Created by Administrator on 2015/9/17.
 */
public class AndroidCommonCreateApWorker extends BaseCreateApWorker {

    public AndroidCommonCreateApWorker(Context context) {
        super(context);
    }

    @Override
    void restoreNetworkStatus() {
        super.restoreNetworkStatus();

        //恢复AP设置
        UserApConfigurationUtil.restoreSpecifiedApConfig(context);
    }

    @Override
    void doOpenApOpt(String ssid, String password) {
        super.doOpenApOpt(ssid, password);

        getOldConfig();

        boolean result = getWifiApManager().setWifiApEnabledForBelowAndroidN(ssid, password, true);

        if (Logger.r) {
            Logger.d(TAG,"set wifi ap enabled,result:"+result);
        }

    }

    @Override
    public void closeAp() {
        super.closeAp();

        new Thread(new Runnable(){
            @Override
            public void run() {


                getWifiApManager().setWifiApEnabledForBelowAndroidN("", "", false);
                if (Logger.r) {
                    Logger.c(TAG, "close ap-------");
                }

            }
        },"closeAp-thread").start();
    }



}
