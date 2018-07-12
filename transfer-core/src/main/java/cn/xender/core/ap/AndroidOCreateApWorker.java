package cn.xender.core.ap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;

import cn.xender.core.ap.service.OAPService;
import cn.xender.core.log.Logger;

/**
 * Created by Administrator on 2015/9/17.
 */
public class AndroidOCreateApWorker extends BaseCreateApWorker {

    private Handler _handler = new Handler(Looper.getMainLooper());

    @Override
    public void closeAp() {
        stopApServices();
    }

    @Override
    public String getApPassword() {
        return password;
    }

    @Override
    public String getApName() {
        return ssid;
    }


    public AndroidOCreateApWorker(Context context){
        super(context);

    }



    @Override
    void doOpenApOpt(String ssid, String password) {

        startOpenApServices();

    }


    @Override
    void notifyTimeout(){
        super.notifyTimeout();

        stopApServices();
    }


    @Override
    void notifyFailed(){
        super.notifyFailed();

        stopApServices();
    }

    @Override
    void apStatusDisabled() {
        super.apStatusDisabled();
        stopApServices();
    }

    @Override
    void apStatusEnabled() {

        mApStatusReceiver.state = State.ON;
        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Logger.r) {
                    Logger.c(TAG, "Ap enabled,  on android O ,ssid is :" + ssid);
                }
                if(mApStatusReceiver.state == State.ON && !TextUtils.isEmpty(ssid)){
                    fetchApIp();
                }else{
                    if(creating.get()){
                        _handler.postDelayed(this,100);
                    }
                }
            }
        },100);

    }

    private Messenger oapService;
    private ServiceConnection mApServiceConnection;
    private boolean isConnected;


    private void stopApServices() {

        if(mApServiceConnection != null){
            context.unbindService(mApServiceConnection);
        }
        mApServiceConnection = null;
        isConnected = false;
    }

    private void startOpenApServices() {
        isConnected = false;
        mApServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                // Called when the connection is made.
                oapService = new Messenger(service);
                isConnected = true;

                sendOpenApMessageForAndroidO();
            }

            public void onServiceDisconnected(ComponentName className) {
                // Received when the service unexpectedly disconnects.

                oapService = null;
                isConnected = false;
            }
        };

        context.bindService(new Intent(context, OAPService.class), mApServiceConnection, Context.BIND_AUTO_CREATE|Context.BIND_WAIVE_PRIORITY);
    }


    private void sendOpenApMessageForAndroidO(){

        sendMessageForAndroidO(OAPService.MSG_OPEN_AP);
    }

    private void sendGetApInfoMessageForAndroidO(){

        sendMessageForAndroidO(OAPService.MSG_GET_AP_INFO);
    }


    private void sendMessageForAndroidO(int what){
        try {
            Message send = new Message();
            send.what = what;
            send.replyTo = messenger;
            oapService.send(send);
        } catch (Exception e) {
        }
    }

    private static final int REPEAT_GET_AP_INFO = 11;

    private Messenger messenger = new Messenger(new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == OAPService.MSG_OPEN_AP){

                Bundle data = msg.getData();
                boolean result = data.getBoolean("result");

                if(result && creating.get()){

                    sendGetApInfoMessageForAndroidO();
                }

            }else if (msg.what == OAPService.MSG_GET_AP_INFO) {
                Bundle data = msg.getData();
                ssid = data.getString("ssid");
                password = data.getString("password");
                boolean openFailed = data.getBoolean("fail");
                if (Logger.r) {
                    Logger.d(TAG,"ssid:"+ssid + ",password:"+password);
                }

                if(openFailed){
                    notifyFailed();
                }else if(TextUtils.isEmpty(ssid) && creating.get()){
                    sendEmptyMessageDelayed(REPEAT_GET_AP_INFO,200);
                }
            }else if(msg.what == REPEAT_GET_AP_INFO){
                if (Logger.r) {
                    Logger.d(TAG,"what is REPEAT_GET_AP_INFO:");
                }

                if(creating.get()){
                    sendGetApInfoMessageForAndroidO();
                }
            }
        }
    });
}
