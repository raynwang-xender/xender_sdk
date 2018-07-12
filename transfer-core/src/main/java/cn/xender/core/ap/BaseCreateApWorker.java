package cn.xender.core.ap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.TextUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.xender.core.ap.utils.FilterManager;
import cn.xender.core.ap.utils.UserApConfigurationUtil;
import cn.xender.core.ap.utils.WIFI_AP_STATE;
import cn.xender.core.ap.utils.WifiAPUtil;
import cn.xender.core.ap.utils.WifiApManager;
import cn.xender.core.log.Logger;

/**
 * Created by Administrator on 2015/9/17.
 */
public class BaseCreateApWorker implements ICreateApWorker {


    private WifiApManager apManager;
    private WifiManager wifiManager;
    protected String ssid;
    protected String password;

    protected Context context;

    CoreCreateApCallback callback;

    private Timer timer;

    private boolean wifiInit;

    static AtomicBoolean creating = new AtomicBoolean(false);

    static AtomicBoolean newCreateWaiting = new AtomicBoolean(false);

    private static AtomicBoolean apStatusReceiverRegistered = new AtomicBoolean(false);

    static ApStatusReceiver mApStatusReceiver;

    public static int  FETCH_APIP_TIMEOUT = 8000;  //5s

    int requestCode;

    @SuppressWarnings("unused")
    enum State{
        INIT,OFF, ON, OFF_R, ON_R, FAILURE
    }


    public BaseCreateApWorker(Context context){
        this.context = context;
        registerApStatusReceiver();

    }

    @Override
    public boolean isApEnabled() {
        return getWifiApManager().isWifiApEnabled();
    }

    @Override
    public void createAp(String ssid, String password, long timeout, int requestCode, CoreCreateApCallback callback) {

        if(!creating.compareAndSet(false, true)){
            return;
        }

        this.callback = callback;

        this.requestCode =requestCode;

        setUpTimer(timeout);

        wifiInit = WifiAPUtil.isWifiEnabled(getWifiManager());

        doReadyWorkForCreateApAndDoIt(ssid, password);
    }

    @Override
    public void retryCreateAp(String ssid, String password, long timeout, int requestCode, CoreCreateApCallback callback) {

        this.callback = callback;

        if(!creating.compareAndSet(false, true)){
            return;
        }

        this.requestCode =requestCode;
        setUpTimer(timeout);
        doRetryCreateAp(ssid, password);

    }

    @Override
    public int getCurrentRequestCode() {
        return this.requestCode;
    }

    @Override
    public void closeAp() {
    }

    @Override
    public void createFailed() {
        notifyFailed();
    }


    @Override
    public String getApPassword() {
        return password;
    }

    @Override
    public String getApName() {
        return getWifiApManager().getWifiApSSID();
    }


    WifiManager getWifiManager(){
        if(wifiManager == null){
            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }
        return wifiManager;
    }

    WifiApManager getWifiApManager(){
        if(apManager == null){
            apManager = new WifiApManager(context, getWifiManager());
        }
        return  apManager;
    }



    private void doReadyWorkForCreateApAndDoIt(final String ssid, final String password) {
        this.ssid = ssid;
        this.password = password;

        if (Logger.r) {
            Logger.c(TAG,"do create ap ,and the password is " + password);
        }


        if (WifiAPUtil.isWifiEnabled(getWifiManager())) {
            if (Logger.r) {
                Logger.c(TAG, "wifi enabled, turn it off, and waiting " + ssid);
            }

            newCreateWaiting.compareAndSet(false,true);

            mApStatusReceiver.init();

            ensureWifiDisabled(new Runnable() {
                @Override
                public void run() {

                    newCreateWaiting.set(false);

                    if (Logger.r) {
                        Logger.c(TAG, "wifi disabled,changed the state is " + mApStatusReceiver.state);
                    }

                    doOpenApOpt(ssid, password);
                }
            });

        } else if(getWifiApManager().isWifiApEnabled()){
            if (Logger.r) {
                Logger.c(TAG, "wifi disabled,ap enable createButtonClicked " + ssid);
            }
            mApStatusReceiver.tryTurnOffAp();
            doRetryCreateAp(ssid, password);
        }else{
            if (Logger.r) {
                Logger.c(TAG, "wifi disabled,createButtonClicked " + ssid + ",ap state:"+mApStatusReceiver.state);
            }
            newCreateWaiting.compareAndSet(false,true);

            ensureWifiApDisabled(new Runnable() {
                @Override
                public void run() {

                    newCreateWaiting.set(false);

                    mApStatusReceiver.init();

                    if (Logger.r) {
                        Logger.c(TAG, "wifi disabled,changed the state is " + mApStatusReceiver.state);
                    }
                    doOpenApOpt(ssid, password);
                }
            });
        }
    }



    private void ensureWifiApDisabled(final Runnable runnable){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mApStatusReceiver.state != State.INIT && mApStatusReceiver.state != State.OFF_R && creating.get() && newCreateWaiting.get()){
                    if (Logger.r) {
                        Logger.c(TAG, "wifi disabled,ap disabling,waiting for ap disabled " + ssid);
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(!creating.get()) return;
                if(runnable != null){
                    runnable.run();
                }
            }
        }).start();
    }

    private void ensureWifiDisabled(final Runnable callback){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    getWifiManager().setWifiEnabled(false);
                }catch (Exception e){
                    if (Logger.r) {
                        Logger.ce(TAG, "wifi enabled, turn it off, failure ");
                    }
                }


                while (getWifiManager().getWifiState() != WifiManager.WIFI_STATE_DISABLED && creating.get()){

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                    if (Logger.r) {
                        Logger.c(TAG, "wifi disabling,waiting for wifi disabled ");
                    }
                }

                if(!creating.get()) return;
                if(callback != null){
                    callback.run();
                }

            }
        }).start();
    }

    void setUpTimer(long timeout) {

        if(timeout < 0) return;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                notifyTimeout();
            }
        }, timeout);
    }


    void doOpenApOpt(final String ssid, final String password) {




    }


    private void doRetryCreateAp(String ssid, String password) {
        this.ssid = ssid;
        this.password = password;
        if(mApStatusReceiver != null){
            mApStatusReceiver.retry();
        }
    }


//    private class WiFiStateBroadcastReceiver extends BroadcastReceiver {
//        String password;
//        String ssid;
//        public WiFiStateBroadcastReceiver(String ssid, String password){
//            this.ssid = ssid;
//            this.password = password;
//        }
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (Logger.r) {
//                Logger.c(TAG, "wifi state receiver,intent: " + intent);
//            }
//            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
//                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
//                if (Logger.r) {
//                    Logger.c(TAG, "Wifi State Changed. state: " + wifiState);
//                }
//
//                if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
//                    try {
//                        BaseCreateApWorker.this.context.unregisterReceiver(this);
//                    } catch (Exception e) {
//                        if (Logger.r) {
//                            Logger.ce(TAG, "unregisterReceiver wifiReceiver failure :" + e.getCause());
//                        }
//                    }
//
//                    if (Logger.r) {
//                        Logger.c(TAG, "Wifi disabled, ceateAp");
//                    }
//
//                    doReadyWorkForCreateApAndDoIt(ssid,password);
//                }
//            }
//        }
//    }



    private void registerApStatusReceiver() {

        if(! apStatusReceiverRegistered.compareAndSet(false, true)){
            return;
        }
        mApStatusReceiver = new ApStatusReceiver();
        context.registerReceiver(mApStatusReceiver, new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED"));
    }


    public class ApStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {

            WIFI_AP_STATE status = getWifiApManager().getWifiApState();

            if (Logger.r) {
                Logger.c(TAG, "ap status is " + status);
            }

            switch (status) {
                case WIFI_AP_STATE_DISABLING:
                    break;
                case WIFI_AP_STATE_DISABLED:
                    apDisabled();
                    break;
                case WIFI_AP_STATE_ENABLING:
                    break;
                case WIFI_AP_STATE_ENABLED:

                    apEnabled();
                    break;
                case WIFI_AP_STATE_FAILED:
                    notifyFailed();
                    break;
                default:
                    break;
            }

        }

        State state = State.INIT;

        public void apEnabled(){
            if (Logger.r) {
                Logger.c(TAG,"Ap enabled, state :" + state);
            }
            if(state == State.OFF || state == State.OFF_R){

                apStatusEnabled();
            }
        }
        public void retry(){
            if(state == State.ON){
                state = State.ON_R;
                //
                closeAp();
            }
        }

        public void apDisabled(){

            if (Logger.r) {
                Logger.c(TAG,"Ap disabled, state :" + state + " ,and creating is " + creating.get() + ",new create waiting:"+newCreateWaiting.get());
            }
            if(state == State.ON){
                if(newCreateWaiting.get()){

                    state = State.INIT;

                }else {

                    cancelTimer();
                    state = State.OFF;
                    if(creating.get()){
                        if(callback != null){
                            callback.callback(CreateApEvent.offEvent(requestCode));
                        }
                    }
                    state = State.INIT;

                    creating.set(false);

                    //2. 恢复Wifi状态
                    restoreNetworkStatus();

                    apStatusDisabled();
                }
            }else if(state == State.ON_R){
                state = State.OFF_R;
                //再次打开
                doReadyWorkForCreateApAndDoIt(ssid,password);
            }

        }

        public void init(){
            if(mApStatusReceiver.state == State.INIT)
                mApStatusReceiver.state = State.OFF;
        }

        public void tryTurnOffAp(){
            mApStatusReceiver.state = State.ON;
        }
    }

    void apStatusDisabled(){

    }

    void apStatusEnabled(){
        //只有自己申请创建的热点成功后才能认为真正创建成功

        String getSsid = getWifiApManager().getWifiApSSID();
        if (Logger.r) {
            Logger.c(TAG, "Ap enabled, i record ssid :" + ssid + ",and get ssid from system:" + getSsid);
        }
        if(TextUtils.isEmpty(ssid)){//这种情况是处理sdk中不需要修改热点名字，传入的ssid是空的。如果是这样，需要在这里给ssid赋值
            ssid = getSsid;
            password = getWifiApManager().getWifiApPwd();
        }
        if (TextUtils.equals(getSsid, ssid) ||TextUtils.equals(getSsid, "\"" + ssid+"\"")){
            mApStatusReceiver.state = State.ON;
            fetchApIp();

        }

    }

    void restoreNetworkStatus() {
        if (Logger.r) {
            Logger.c(TAG, "restorNetworkStatus");
        }
        try {
            getWifiManager().setWifiEnabled(wifiInit);
        }catch (Exception e){
            if (Logger.r) {
                Logger.ce(TAG, "restoreNetworkStatus  failure ");
            }
        }

    }



    void fetchApIp() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Logger.r) {
                    Logger.c(TAG, "Ap created, fetching Ap Ip");
                }
                String ip = getApIp();

                if(TextUtils.isEmpty(ip)){

                    handleNoIpQuestionAfterFetchApIp();
                }else{
                    notifySuccessed(ip);
                }

            }
        }).start();


    }

    void handleNoIpQuestionAfterFetchApIp(){

        notifyFailed();

    }


    void notifyTimeout(){
        // 重置状态
        creating.set(false);
        cancelTimer();
        if(mApStatusReceiver != null){
            mApStatusReceiver.state = State.INIT;
        }
        if(callback != null){
            callback.callback(CreateApEvent.errorEvent(requestCode));
        }
    }


    void notifyFailed(){
        if (Logger.r) {
            Logger.c(TAG, "createButtonClicked failed");
        }
        creating.compareAndSet(true, false);
        cancelTimer();
        if(mApStatusReceiver != null){
            mApStatusReceiver.state = State.INIT;
        }
        restoreNetworkStatus();

        if(callback != null){
            callback.callback(CreateApEvent.errorEvent(requestCode));
        }
    }


    void cancelTimer() {
        if(timer != null){
            timer.cancel();
        }
    }

    private void notifySuccessed(String ip){
        if (Logger.r) {
            Logger.c(TAG, "createButtonClicked successed ip: " + ip);
        }
        creating.compareAndSet(true, false);
        cancelTimer();
        if(callback != null){

            callback.callback(CreateApEvent.okEvent(ssid, ip,requestCode,password));
        }
    }

    private String getApIp() {
        String ip = WifiAPUtil.getGroupLocalIp(context);
        int sleeptime  =20;
        boolean timeout = false;
        long initTime = System.currentTimeMillis();
        long currentTime;
        while (TextUtils.isEmpty(ip) && !timeout) {
            try {
                Thread.sleep(sleeptime);
                currentTime = System.currentTimeMillis();
                timeout = (currentTime - initTime) > FETCH_APIP_TIMEOUT;
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

            ip = WifiAPUtil.getGroupLocalIp(context);
        }

        return ip;
    }


    void getOldConfig(){

        WifiConfiguration configuration = WifiApManager.getWifiApConfiguration(getWifiManager());
        if(configuration != null && !FilterManager.acceptSSID(configuration.SSID)){
            UserApConfigurationUtil.saveWifiApConfig(context,configuration);
            if(Logger.r) Logger.c(TAG, "save old ApConfig ssid: " + configuration.SSID);
        }

    }

}
