package cn.xender.core.ap;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.xender.core.ap.utils.AndroidOApName;
import cn.xender.core.ap.utils.WifiAPUtil;
import cn.xender.core.ap.utils.WifiApManager;
import cn.xender.core.log.Logger;

/**
 * Created by Administrator on 2015/9/21.
 */
public class ScanApWorker {

    private static final String TAG = "scan_work";
    private final WifiApManager wifiApManager;

    private WifiManager mWifiManager;

    private static AtomicBoolean scaning = new AtomicBoolean(false);

    private CoreScanApCallback callback;

    public ScanApWorker(Context context){
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiApManager = new WifiApManager(context, mWifiManager);
    }



    public void startScan(SSIDFilter SSIDFilter, SSIDDecoder decoder, long timeout, CoreScanApCallback callback,int requestCode){

        if(!scaning.compareAndSet(false, true)) return;

        this.callback = callback;

        new Thread(new DoScan(SSIDFilter,decoder, timeout,requestCode)).start();
    }

    public void stopScan(){
        scaning.set(false);
    }

    class DoScan implements Runnable{

        private SSIDFilter SSIDFilter = null;
        private SSIDDecoder SSIDDecoder;
        private int requestCode;

        long timeout = 0;
        DoScan(SSIDFilter SSIDFilter, SSIDDecoder decoder, long timeout,int requestCode){
            if(SSIDFilter == null)
                throw new IllegalArgumentException("SSIDFilter is null");
            if(timeout <= 0)
                throw new IllegalArgumentException("timeout must greater than 0. timeout: " + timeout);
            this.SSIDFilter = SSIDFilter;
            this.SSIDDecoder = decoder;
            this.timeout = timeout;
            this.requestCode = requestCode;
        }
        @Override
        public void run() {

            ensureWifiState();

            long startTime = System.currentTimeMillis();

            while (scaning.get()){

                if(System.currentTimeMillis() - startTime > timeout){
                    //超时了
                    if(callback != null){
                        callback.callback(new ScanApEvent(true,requestCode));
                    }
                    scaning.compareAndSet(true,false);
                    break;
                }


                boolean result = mWifiManager.startScan();
                if (Logger.r) {
                    Logger.c(TAG,"start scan result:"+result);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(scaning.get()){
                    List<ScanResultItem> r = new ArrayList<>();
                    boolean hasAndroidOAp = installListAndReturnHasAndroidOAp(r);
                    if(callback != null){
                        callback.callback(new ScanApEvent(r,hasAndroidOAp,requestCode));
                    }
                }


            }
        }

        private void ensureWifiState(){

            if(wifiApManager.isWifiApEnabled()){

                wifiApManager.setWifiApEnabledForBelowAndroidN(null,null,false);
            }

            try {
                if(!mWifiManager.isWifiEnabled()){
                    mWifiManager.setWifiEnabled(true);
                }
            }catch (Exception e){
                if(Logger.r) Logger.e(TAG, "option wifi failure ");
            }

        }

        private boolean installListAndReturnHasAndroidOAp(List<ScanResultItem> r) {
            List<ScanResult> result = null;
            try {
                result = mWifiManager.getScanResults();
            }catch (Exception e){
            }
            if(result == null){
                if (Logger.r) {
                    Logger.c(TAG,"scan result is null");
                }
                return false;
            }
            boolean hasAndroidOAp = false;
            ScanResultItem data;
            for (ScanResult tmp: result) {
                if(SSIDFilter.accept(tmp.SSID)){
                    if (Logger.r) {
                        Logger.c(TAG,"scan list:"+tmp.toString());
                    }
                    data = new ScanResultItem();
                    data.setSSID(tmp.SSID);
                    data.setBSSID(tmp.BSSID);
                    if(AndroidOApName.startWithAndroidOFix(tmp.SSID)){
                        hasAndroidOAp = true;
                    }
                    if(SSIDDecoder != null){
                        String[] fixAndName = SSIDDecoder.decode( tmp.SSID);
                        data.setProfix(fixAndName[0]);
                        data.setSsid_nickname(fixAndName[1]);
                    }else{
                        data.setSsid_nickname(tmp.SSID);
                    }
                    if (WifiAPUtil.hasKeyManagerment(tmp.capabilities)) {
                        data.setKeyMgmt("wpa-psk");
                    }

                    r.add(data);
                }
            }

            return hasAndroidOAp;
        }

    }

}
