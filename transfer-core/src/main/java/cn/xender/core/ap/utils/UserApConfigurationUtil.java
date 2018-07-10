package cn.xender.core.ap.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.xender.core.SDKSpf;
import cn.xender.core.ap.CoreApManager;
import cn.xender.core.log.Logger;

/**
 * Created by Administrator on 2017/3/30.
 */

public class UserApConfigurationUtil {

    private static final String TAG = "UserApConfigurationUtil";

    private static WifiConfiguration oldWifiConfiguration;

    public static void saveWifiApConfig(Context context,WifiConfiguration configuration){

        oldWifiConfiguration = configuration;

        String save = getNeedSaveStr(configuration);

        if(!TextUtils.isEmpty(save)){

            SDKSpf.putString(context,"old_ap_config",save);
        }

    }


    private static String getNeedSaveStr(WifiConfiguration configuration){


        JSONObject json = new JSONObject();

        try {
            json.put("ssid",configuration.SSID);
            json.put("pwd",configuration.preSharedKey);
            json.put("allowedKeyManagement",getConfigurationAllowedKeyManagrment(configuration));
        } catch (JSONException e) {
            return "";
        }
        return json.toString();

    }




    public static void restoreSpecifiedApConfigSync(Context context){
        try {
            if(CoreApManager.getInstance().isApEnabled()){
                //ap还在开启中，不恢复
                return;
            }

            WifiManager manager = WifiAPUtil.getWifiManager(context);
            if(manager == null){
                return;
            }

            WifiConfiguration currentConfig = WifiApManager.getWifiApConfiguration(manager);

            if(currentConfig == null || FilterManager.acceptSSID(currentConfig.SSID)){
                WifiConfiguration oldWifiConf = getOldWifiConfiguration(context);
                if(oldWifiConf == null) return;
                if (Logger.r) {
                    Logger.c(TAG, "restore ApConfig ssid:" + oldWifiConf.SSID);
                }

                WifiApManager.setWifiApConfiguration(WifiAPUtil.getWifiManager(context),oldWifiConf);
            }
        }catch (Exception e){

        }

    }

    public static void restoreSpecifiedApConfig(final Context context){

        new Thread(new Runnable() {
            @Override
            public void run() {
                restoreSpecifiedApConfigSync(context);
            }
        },"restoreSpecifiedApConfig-thread").start();

    }

    private static WifiConfiguration getOldWifiConfiguration(Context context){
        if(oldWifiConfiguration == null){
            oldWifiConfiguration = getSavedWifiConfiguration(context);
        }
        return oldWifiConfiguration;
    }


    private static WifiConfiguration getSavedWifiConfiguration(Context context){
        String saved = SDKSpf.getString(context,"old_ap_config","");

        if(TextUtils.isEmpty(saved)){
            return null;
        }

        try {
            JSONObject json = new JSONObject(saved);
            String ssid = json.getString("ssid");
            String pwd = json.getString("pwd");
            int allowedKeyManagement = json.getInt("allowedKeyManagement");

            return createMyWifiApConfiguration(ssid,pwd,allowedKeyManagement);
        } catch (JSONException e) {
            return null;
        }

    }

    private static int getConfigurationAllowedKeyManagrment(WifiConfiguration configuration){
        for(int i = 0;i <WifiConfiguration.KeyMgmt.strings.length;i++){
            if(configuration.allowedKeyManagement.get(i)){
                return i;
            }
        }
        return WifiConfiguration.KeyMgmt.NONE;
    }


    //这里创建开启ap所使用的wificonfiguration,里面包括热点名称的创建
    private static WifiConfiguration createMyWifiApConfiguration(String ssid,String pwd,int allowedKeyManagement){

        WifiConfiguration apConfig = new WifiConfiguration();

        apConfig.SSID = ssid;   //9
        apConfig.allowedKeyManagement.set(allowedKeyManagement);
        apConfig.preSharedKey = pwd;


        if (WifiApManager.isHtc()) {
            try {

                Object localObject = WifiApFieldUtils.getFieldValue(apConfig, "mWifiApProfile");
                if(localObject != null){
                    WifiApFieldUtils.setFieldValue(localObject, "SSID", ssid);
                    WifiApFieldUtils.setFieldValue(localObject, "secureType", "open");
                    WifiApFieldUtils.setFieldValue(localObject, "dhcpEnable", 1);
                    WifiApFieldUtils.setFieldValue(localObject, "maxConns", 8);
                    WifiApFieldUtils.setFieldValue(localObject, "maxDhcpClients", 8);
                }

            } catch (Exception e) {
            }

        }

        return apConfig;
    }


}
