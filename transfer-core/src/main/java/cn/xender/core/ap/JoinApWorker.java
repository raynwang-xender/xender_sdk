package cn.xender.core.ap;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.text.TextUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.xender.core.ap.utils.ConnectWifiUtil;
import cn.xender.core.ap.utils.FilterManager;
import cn.xender.core.ap.utils.WifiAPUtil;
import cn.xender.core.log.Logger;

/**
 * Created by Administrator on 2015/9/21.
 */
public class JoinApWorker {

    private static final String TAG = "JoinApWorker";

    private WifiManager wifiManager;

    private static AtomicBoolean connecting = new AtomicBoolean(false);

    private CoreJoinApCallback callback;

    private static int oldNetworkId = -1;

    private Context context;

    private Timer timer = null;

    //增加一个状态记录。防止发出多次EXIT事件。
    private boolean joined = false;

    public JoinApWorker(Context context){
        this.context = context.getApplicationContext();

        wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        if (WifiAPUtil.isWifiConnected(context) && oldNetworkId == -1) {
            String ssid = WifiAPUtil.getWifiSSID(context);
            if (!TextUtils.isEmpty(ssid) && !FilterManager.acceptSSID(ssid)) {
                oldNetworkId = WifiAPUtil.getNetWorkID(context,wifiManager);
            }
        }
        connecting.set(false);
    }

    public void startJoin(String ssid,String bssid,String password,String static_ip,long timeout,CoreJoinApCallback callback){

        if(!connecting.compareAndSet(false, true)) return;

        this.callback = callback;

        if(netWorkStatusReceiver != null){
            netWorkStatusReceiver.reset();
        }
        joined = false;
        new Thread(new DoConnectWifi(context,ssid,bssid,password,static_ip,timeout)).start();
    }


    class DoConnectWifi implements Runnable {

        Context context;
        String ssid,bssid,password,static_ip;
        long timeout;

        public DoConnectWifi(Context context,String ssid,String bssid,String password,String static_ip,long timeout) {
            this.context = context;
            this.ssid = ssid;
            this.bssid = bssid;
            this.password = password;
            this.static_ip = static_ip;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            if (Logger.r) {
                Logger.c(TAG, "--connect wifi?");
            }
            Looper.prepare();
            ensureNetworkInStableState();

            registerNetworkReceiver();
            if(!TextUtils.isEmpty(password)){
                registerSupplicantStateReceiver();
            }
            netWorkStatusReceiver.setSsidToConnected(ssid);

            if (Logger.r) {
                Logger.c(TAG, "start connect AP,and timeout = " + timeout);
            }
            cancelTimer();
            startTimer(timeout);
            boolean success = ConnectWifiUtil.connect(context, wifiManager, ssid, bssid, password, static_ip);
            if(!success){
                cancelTimer();
                if (Logger.r) {
                    Logger.c(TAG, "conneted to ap failed.");
                }

                if(callback != null){
                    callback.callback(new JoinApEvent(false,false, JoinApEvent.JOIN));
                }
                connecting.set(false);
            }
            Looper.loop();
        }

        private void ensureNetworkInStableState() {
            NetworkInfo activeNetInfo = WifiAPUtil.getActiveNetworkInfo(context);// 获取网络的连接情况

            NetworkInfo.State netstatus = null;
            if (activeNetInfo != null) {
                netstatus = activeNetInfo.getState();
            }


            WifiInfo info = wifiManager.getConnectionInfo();
            if(info != null && info.getSSID() != null){
                if (Logger.r) {
                    Logger.c(TAG, "current wifiInfo ssid:" + info.getSSID() + "joined:" + joined);
                }
                if((info.getSSID().equals(ssid) || info.getSSID().equals("\""+ssid+"\"") ) && !joined ){
                    if (Logger.r) {
                        Logger.c(TAG, "already connected to " + ssid + " disable first");
                    }
                    wifiManager.disableNetwork(info.getNetworkId());

                    netstatus = waitUntilDisconnected(context, netstatus);
                }
            }

            waitUntilNotConnectingOrDisconnecting(context, netstatus);
        }

        private void waitUntilNotConnectingOrDisconnecting(Context context, NetworkInfo.State netstatus) {
            NetworkInfo activeNetInfo;
            int count = 0;
            while (netstatus == NetworkInfo.State.CONNECTING || netstatus == NetworkInfo.State.DISCONNECTING) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                activeNetInfo = WifiAPUtil.getActiveNetworkInfo(context);
                if(activeNetInfo == null) break;
                netstatus = activeNetInfo.getState();

                if (count++ > 500) {
                    break;
                }
            }
        }

        private NetworkInfo.State waitUntilDisconnected(Context context, NetworkInfo.State netstatus) {
            NetworkInfo activeNetInfo;
            int count = 0;
            while (netstatus == NetworkInfo.State.CONNECTED) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                activeNetInfo = WifiAPUtil.getActiveNetworkInfo(context);
                if(activeNetInfo == null) break;
                netstatus = activeNetInfo.getState();

                if (count++ > 500) {
                    break;
                }
            }
            return netstatus;
        }

    }

    private void cancelTimer() {
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }

    private void startTimer(long timeout) {
        try {
            if(timer == null){
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (Logger.r) {
                            Logger.c(TAG, "Join time out");
                        }
                        if(netWorkStatusReceiver != null){
                            netWorkStatusReceiver.reset();
                        }
                        joined = false;
                        if(callback != null){
                            callback.callback(new JoinApEvent(false,false, JoinApEvent.JOIN));
                        }
                        connecting.set(false);
                    }
                }, timeout);

            }
        }catch (Exception e){
        }
    }

    NetWorkStatusReceiver netWorkStatusReceiver;

    private void registerNetworkReceiver() {
        if (netWorkStatusReceiver == null) {
            netWorkStatusReceiver = new NetWorkStatusReceiver();
            if (Logger.r) {
                Logger.c(TAG, "register NetWorkStatusReceiver");
            }
            context.registerReceiver(netWorkStatusReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        }
    }

    public class NetWorkStatusReceiver extends BroadcastReceiver {

        public void setSsidToConnected(String ssidToConnected) {
            this.ssidToConnected = ssidToConnected;
        }

        public final static String NULL_SSID = "$$NULL$$SSID";
        String ssidToConnected;

        String current_connect_ap;

        long connectedTime = 0;

        public void reset(){
            ssidToConnected = NULL_SSID;
            current_connect_ap = null;
            connectedTime = 0;
        }

        @Override
        public void onReceive(Context c, Intent intent) {

            String action = intent.getAction();

            NetworkInfo info = (NetworkInfo) intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State netstatus = info.getState();

            /*if(netstatus == NetworkInfo.State.CONNECTED){
                ssid = WifiAPUtil.getWifiSSID(context);
            }*/
            if (Logger.r) {
                Logger.c(TAG, "NetWorkStatusReceiver,action=" + action + ",NetWorkStatus=" + netstatus);
                Logger.c(TAG, "NetWorkStatusReceiver,extras=" +intent.getExtras());
            }
            String bssid = intent.getStringExtra(WifiManager.EXTRA_BSSID);

            WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            if (Logger.r) {
                Logger.c(TAG, "BSSID " + bssid + " SSID: " + (wifiInfo == null ? "null" : wifiInfo.getSSID()));
                Logger.c(TAG, "current_connect_ap " + current_connect_ap + " ,ssidToConnected = " + ssidToConnected + ", and joined :" + joined);
            }

            if(netstatus == NetworkInfo.State.CONNECTING || netstatus == NetworkInfo.State.DISCONNECTING){
                return;
            }

            String ssid = "";

            if(netstatus == NetworkInfo.State.CONNECTED){
                ssid = WifiAPUtil.getWifiSSID(context);
                bssid = WifiAPUtil.getWifiBSSID(context);
            }
            if (netstatus == NetworkInfo.State.CONNECTED && TextUtils.equals(ssidToConnected,ssid) && !TextUtils.isEmpty(bssid)) {

                /*onlineAp(ConnectMainActivity.this);*/

                //在Nexus上测试发现，CONNECTED之后会很快出现CONNECTING事件，随和再跟随一个CONNECTED事件。
                //为了防止发送两次JoinApEvent,这里通过判断两次CONNECTED事件的时间间隔来滤掉后面的一个事件。
                if(System.currentTimeMillis() - connectedTime > 500 || current_connect_ap == null){
                    current_connect_ap = ssid;
                    if (Logger.r) {
                        Logger.c(TAG, "POST JoinAPEvent type: Join");
                    }
                    cancelTimer();
                    joined = true;
                    if (Logger.r) {
                        Logger.c(TAG, "Connected, joined:" + joined);
                    }
                    ssidToConnected = NULL_SSID;
                    if(callback != null){
                        callback.callback(new JoinApEvent(true,false, JoinApEvent.JOIN));//成功，开始连接协议
                    }
                    connectedTime = System.currentTimeMillis();
                    unregisterSupplicantStateReceiver();
                    connecting.set(false);

                }
            }else{

                //if( !TextUtils.isEmpty(current_connect_ap) && netstatus != NetworkInfo.State.CONNECTING)
                if(  !TextUtils.isEmpty(current_connect_ap) && !TextUtils.equals(current_connect_ap, ssid) && joined/*只有已连接后，才可能发出EXIT事件*/){
                    //断开了，从组内退出了

                    if (Logger.r) {
                        Logger.c(TAG, "POST JoinAPEvent type: EXIT");
                    }
                    joined = false;
                    if(callback != null){
                        callback.callback(new JoinApEvent(true,false, JoinApEvent.EXIT));
                    }
                    connecting.set(false);
                    current_connect_ap = null;
                }else{
                    //我们并没有加入组，不用做任何操作
                }
            }
        }
    }

    private void registerSupplicantStateReceiver(){
        unregisterSupplicantStateReceiver();
        if(receiver == null){
            receiver = new SupplicantStateReceiver();
            context.registerReceiver(receiver, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
        }
    }

    private void unregisterSupplicantStateReceiver(){
        try {
            if(receiver != null) {
                if (Logger.r) {
                    Logger.c("pwd_action", "unregisterReceiver");
                }
                context.unregisterReceiver(receiver);
                receiver = null;
            }
        }catch (Exception e){
        }
    }

    SupplicantStateReceiver receiver;
    class SupplicantStateReceiver extends BroadcastReceiver{
        private int firstValue = -505;

        SupplicantStateReceiver(){
            initFirstValue();
        }

        private void initFirstValue(){
            firstValue = -505;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Logger.r) {
                Logger.c("pwd_action", "action is " + action);
            }
            if(!WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)){
                return;
            }
            int value = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0);
//            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            if (Logger.r) {
                Logger.c("pwd_action", "value is " + value);
            }
//            Logger.c("pwd_action", "state is " + state);
            if(firstValue == -505){
                firstValue = value;
            }
            if(firstValue == WifiManager.ERROR_AUTHENTICATING){//处理缓存的情况，上一次因为密码问题连接失败，这一次第一个的状态是密码错误
                initFirstValue();
                return;
            }

            if(value == WifiManager.ERROR_AUTHENTICATING){//密码错误
                if(callback != null){
                    callback.callback(new JoinApEvent(false,true, JoinApEvent.JOIN));
                }
                connecting.set(false);
                unregisterSupplicantStateReceiver();
            }

        }
    }

}
