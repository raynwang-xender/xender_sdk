package cn.xender.core.ap;

import android.content.Context;
import android.text.TextUtils;

import cn.xender.core.SDKSpf;
import cn.xender.core.ap.utils.UserApConfigurationUtil;
import cn.xender.core.log.Logger;

/**
 * Created by Administrator on 2015/9/17.
 */
public class AndroidN_MR1CreateApWorker extends BaseCreateApWorker {


    public AndroidN_MR1CreateApWorker(Context context) {
        super(context);

    }


    @Override
    void restoreNetworkStatus() {
        super.restoreNetworkStatus();

        //恢复AP设置
        UserApConfigurationUtil.restoreSpecifiedApConfig(context);

    }

    @Override
    void setUpTimer(long timeout) {
        if(!needManualOpenAp()){
            super.setUpTimer(timeout);
        }
    }

    @Override
    void apStatusEnabled() {

        if(needManualOpenAp()){
            if(callback != null){
                callback.callback(CreateApEvent.apEnabled25Event(requestCode));
            }
        }

        super.apStatusEnabled();
    }

    @Override
    void handleNoIpQuestionAfterFetchApIp() {

        notifyRetryForAndroid71();

    }

    @Override
    public void closeAp() {
        super.closeAp();

        new Thread(new Runnable(){
            @Override
            public void run() {


                getWifiApManager().setWifiApEnabledForAndroidN_MR1("", "", false,needManualOpenAp());
                if (Logger.r) {
                    Logger.c(TAG, "close ap-------");
                }

            }
        },"closeAp-mr1-thread").start();
    }

    @Override
    void doOpenApOpt(String ssid, String password) {

        super.doOpenApOpt(ssid,password);

        getOldConfig();

        //7.1系统在有流量的情况下，设置热点信息可能会失败，所以稍微延迟一下再开始
        if(needManualOpenAp() && !TextUtils.isEmpty(ssid)){
            if (Logger.r) {
                Logger.d(TAG,"gprs is open,sleep one second");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        getWifiApManager().setWifiApEnabledForAndroidN_MR1(ssid, password, true,needManualOpenAp());

        if(needManualOpenAp() && creating.get()){
            if(callback != null){
                callback.callback(CreateApEvent.save25ConfigEvent(requestCode));
            }
        }

    }

    private boolean needManualOpenAp(){
        return SDKSpf.getBoolean(context,"ap_need_manual",false);
    }


    private void notifyRetryForAndroid71(){
        SDKSpf.putBoolean(context,"ap_need_manual",true);
        if (Logger.r) {
            Logger.c(TAG, "notifyRetryForAndroid71 ");
        }
        creating.compareAndSet(true, false);
        cancelTimer();

        if(callback != null){
            callback.callback(CreateApEvent.okButNoIpOn25(requestCode));
        }
    }

}
