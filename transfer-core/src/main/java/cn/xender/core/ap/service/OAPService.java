package cn.xender.core.ap.service;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import cn.xender.core.ap.utils.WifiApManager;
import cn.xender.core.log.Logger;


/**
 * Created by wangshangang on 2017/12/27.
 */

public class OAPService extends Service{

    public static final int MSG_OPEN_AP = 2032;
    public static final int MSG_GET_AP_INFO = 2034;


    private String ssid;
    private String password;
    private boolean openFailed;

    /**
     * 在Service处理Activity传过来消息的Handler
     */
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_OPEN_AP){

                if(Logger.r) Logger.d("OAPService","message :"+msg.replyTo);

                boolean result = getWifiApManager().setWifiApEnabledOnAndroidO(true,callback);
                Bundle data = new Bundle();
                data.putBoolean("result",result);
                replyToClientMessage(msg,data);
            }else if(msg.what == MSG_GET_AP_INFO){
                Bundle data = new Bundle();
                data.putString("ssid",ssid);
                data.putString("password",password);
                data.putBoolean("fail",openFailed);
                replyToClientMessage(msg,data);
            }
        }
    }

    private void replyToClientMessage(Message received,Bundle data){
        Message replyMsg = new Message();
        replyMsg.what = received.what;
        replyMsg.setData(data);
        try {
            received.replyTo.send(replyMsg);
        } catch (RemoteException e) {
        }
    }


    /**
     * 这个Messenger可以关联到Service里的Handler，Activity用这个对象发送Message给Service，Service通过Handler进行处理。
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());


    private WifiManager.LocalOnlyHotspotReservation reservation;

    private WifiManager.LocalOnlyHotspotCallback callback;

    private WifiManager wifiManager;

    public WifiManager getWifiManager() {
        if(wifiManager == null){
            wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        }
        return wifiManager;
    }

    private WifiApManager wifiApManager;

    public WifiApManager getWifiApManager() {
        if(wifiApManager == null){
            wifiApManager = new WifiApManager(this,getWifiManager());
        }
        return wifiApManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        openFailed = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            callback = new WifiManager.LocalOnlyHotspotCallback(){
                @Override
                public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                    super.onStarted(reservation);
                    OAPService.this.reservation = reservation;

                    if(!reservation.getWifiConfiguration().allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)){
                        password = reservation.getWifiConfiguration().preSharedKey;
                    }
                    if(Logger.r) Logger.d("OAPService","doCreateApOnAndroidO ap on started "+reservation.getWifiConfiguration().preSharedKey);
                    ssid = reservation.getWifiConfiguration().SSID;

                }

                @Override
                public void onFailed(int reason) {
                    super.onFailed(reason);
                    openFailed = true;
                }

                @Override
                public void onStopped() {
                    super.onStopped();
                    if(Logger.r) Logger.d("OAPService","ap stopped");
                }
            };
        }

    }



    @Override
    public IBinder onBind(Intent intent) {


        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {

        if(Logger.r) Logger.d("OAPService","on unbind ,i want to close ap");
        //关闭热点
        getWifiApManager().setWifiApEnabledOnAndroidO(false,null);

        return super.onUnbind(intent);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        //关闭热点
        boolean result = getWifiApManager().setWifiApEnabledOnAndroidO(false,null);

        if(Logger.r) Logger.d("OAPService","on destroy and exit this process");
        //彻底退出进程
        System.exit(0);

    }


}
