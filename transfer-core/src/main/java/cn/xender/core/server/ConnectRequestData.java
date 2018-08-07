package cn.xender.core.server;


import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import org.json.JSONObject;

import cn.xender.core.server.utils.Port;
import cn.xender.core.ap.utils.WifiAPUtil;

/**
 * Created by Administrator on 2015/9/25.
 */

public class ConnectRequestData {

    private String nickname;
    private String imei;
    private String ip;
    private String device_type;

    private String init_chn;
    private String curt_chn;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private int port;


    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDevice_type() {
        return device_type;
    }

    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }

    public String getInit_chn() {
        return init_chn;
    }

    public void setInit_chn(String init_chn) {
        this.init_chn = init_chn;
    }

    public String getCurt_chn() {
        return curt_chn;
    }

    public void setCurt_chn(String curt_chn) {
        this.curt_chn = curt_chn;
    }




    @Override
    public String toString() {

        try {

            return toJsonObj().toString();
        }catch (Exception e){

        }

        return nickname;
    }

    public JSONObject toJsonObj(){
        try {

            JSONObject object = new JSONObject();
            object.put("nickname",nickname);
            object.put("imei",imei);
            object.put("ip",ip);
            object.put("device_type",device_type);
            object.put("port",port);
            object.put("init_chn",init_chn);
            object.put("curt_chn",curt_chn);

            return object;
        }catch (Exception e){

        }
        return null;
    }


    public static ConnectRequestData getMyConnectRequestData(Context context) {

        ConnectRequestData data = new ConnectRequestData();
        data.setNickname(Build.MODEL);
        data.setImei(getAndroidId(context));
        data.setIp(WifiAPUtil.getIpOnWifiAndAP(context));
        data.setDevice_type("android");
        data.setPort(Port.getWebPort());
        data.setCurt_chn(ClientManager.TRANSFER_SDK_CHANNEL);
        data.setInit_chn(ClientManager.TRANSFER_SDK_CHANNEL);
        return data;

    }

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    public static ConnectRequestData fromJSON(String json){

        ConnectRequestData item = new ConnectRequestData();

        try {

            JSONObject object = new JSONObject(json);
            item.setNickname(object.getString("nickname"));
            item.setIp(object.getString("ip"));
            item.setImei(object.getString("imei"));
            item.setDevice_type(object.getString("device_type"));
            item.setPort(object.getInt("port"));
            item.setCurt_chn(object.getString("curt_chn"));
            item.setInit_chn(object.getString("init_chn"));
        }catch (Exception e){

        }

        return item;

    }

}
