package cn.xender.core.ap.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

import cn.xender.core.log.Logger;


public class WifiAPUtil {

    public static final String TAG = "wifiAP";


    public static String getSegmentByIp(String ip){

        String[] part = ip.split("\\.");
        if (part.length == 4) {
            return part[2];
        }
        return "";

    }

    public static void main(String[] args){
        long longip = ip2Long("192.168.43.12");
        System.out.println(longip);
        System.out.println(long2Ip(longip));
    }


    public static long ip2Long(String strIp){
        long[] ip = new long[4];
        String[] part = strIp.split("\\.");

        if(part.length == 4){
            //将每个.之间的字符串转换成整型
            ip[0] = Long.parseLong(part[0]);
            ip[1] = Long.parseLong(part[1]);
            ip[2] = Long.parseLong(part[2]);
            ip[3] = Long.parseLong(part[3]);
        }

        return (ip[3] << 24) + (ip[2] << 16) + (ip[1] << 8) + ip[0];
    }

    public static String long2Ip(long longip){
        return (longip & 0xFF) + "." + ((longip >> 8) & 0xFF) + "."
                + ((longip >> 16) & 0xFF) + "." + ((longip >> 24) & 0xFF);
    }

    public static boolean isAP(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
            return (boolean) (Boolean) method.invoke(wifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 得到wifi下的ssID
    public static String getWifiSSID(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            if (info != null) {
                String ssid = info.getSSID();
                if (!TextUtils.isEmpty(ssid)) {
                    ssid = ssid.replace("\"", "");
                }
                return ssid;
            }
        }catch (Exception e){

        }
        return null;

    }



    public static WifiManager getWifiManager(Context context){
        try {
            return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }catch (Exception e){

        }
        return null;
    }


    public static String getIpOnWifiAndAP(Context context) {
        if (isAP(context)) {
            return getGroupLocalIp(context);
        }

        String ssid = getWifiSSID(context);
        if (TextUtils.isEmpty(ssid)){
            return "";
        }

        return getWifiIp(context);

    }

    //在已经确认wifi已连接的情况下，才用到
    public static String getWifiIp(Context context){

        try {
            WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            int ip = info.getIpAddress();
            if(Logger.r) Logger.c(TAG, "ip=" + ip);

            int count = 0;
            if (ip == 0) {
                while (count < 10) {
                    count++;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    info = wifi.getConnectionInfo();
                    ip = info.getIpAddress();

                    if (ip != 0) {
                        break;
                    }
                }
            }

            if (ip == 0) {
                return "";
            }

            return long2Ip(ip);
        } catch (Exception e) {
            if(Logger.r) Logger.ce(TAG, "IP SocketException (getLocalIPAddress) " + e.toString());
            // return ("xxx.xxx.xxx.xxx");
            return "";
        }
    }

    public static String getGroupLocalIp(Context context) {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();


            while (en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();
//                if(Logger.r) Logger.i(TAG, "InetAddress inetAddress :" + intf);
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if(Logger.r) Logger.i(TAG, "InetAddress inetAddress :" + inetAddress.toString());
                    if (isReservedAddr(inetAddress, context) && inetAddress instanceof Inet4Address) {
                        //if(Logger.r) Logger.i(TAG, "isReservedAddr=ture?");
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            if(Logger.r) Logger.ce(TAG, ex.toString());
        }
        return "";
    }

    private static boolean isReservedAddr(InetAddress inetAddr, Context context) {
        if (inetAddr == null) return false;

        if (!inetAddr.isSiteLocalAddress()) {
            return false;
        }
        String host = inetAddr.getHostAddress();
        if(Logger.r) Logger.c(TAG, "filter of ap ip :" + host);

        if (TextUtils.isEmpty(host)) return false;

        if(Build.VERSION.SDK_INT <= 27){

            if (host.startsWith("192.168.") && host.endsWith(".1")) {
                return true;
            }
        }else{
            if (host.startsWith("192.168.")) {//从9.0开始热点的ip地址不再是固定的192.168.43.1
                return true;
            }
        }

        return false;
    }



    public static NetworkInfo getActiveNetworkInfo(Context context){
        try {

            ConnectivityManager connectivityManager = getConnectivityManager(context);//获取系统的连接服务
            return connectivityManager.getActiveNetworkInfo();//获取网络的连接情况
        }catch (Exception e){

        }
        return null;
    }

    public static ConnectivityManager getConnectivityManager(Context context){
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static boolean isWifiEnabled(WifiManager manager){
        try {
            return manager.isWifiEnabled();
        }catch (Exception e){}
        return false;
    }

}
