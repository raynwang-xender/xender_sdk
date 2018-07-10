package cn.xender.core.ap.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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


    public static String getApIpByLocalIp(String localIp) {
        String ip = "";

        String[] part = localIp.split("\\.");
        if (part.length == 4) {
            ip = part[0] + "." + part[1] + "." + part[2] + "." + "1";
        }
        return ip;

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

    // 得到wifi下的ssID
    public static String getWifiBSSID(Context context) {
        WifiInfo info = getWifiInfo(context);
        if (info != null) {
            return info.getBSSID();
        }
        return "";

    }

    public static WifiInfo getWifiInfo(Context context){
        try {
            return getWifiManager(context).getConnectionInfo();
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

    // 得到wifi下的NetWorkID
    public static int getNetWorkID(Context context, WifiManager manager) {
        if (isWifiConnected(context)) {
            WifiInfo info = manager.getConnectionInfo();
            if(info != null){
                return info.getNetworkId();
            }
        }
        return -1;
    }

    public static boolean isWifiConnected(Context context) {
        try {

            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);// 获取系统的连接服务
            NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

//		if(Logger.r) Logger.i(TAG, "activeNetInfo="+activeNetInfo+"-----NetworkInfo isConnected=" + activeNetInfo.isConnected());
            return activeNetInfo != null && activeNetInfo.isConnected();
        }catch (Exception e){
            return false;
        }
    }

    public static boolean isMobileDataConnected(Context context) {
        try {

            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);// 获取系统的连接服务
            NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            return activeNetInfo != null && activeNetInfo.isConnected();
        }catch (Exception e){

        }
        return false;
    }


    public static String getIpOnWifiAndAP(Context context) {
        if (isAP(context)) {
            return getGroupLocalIp(context);
        }

//        boolean wificonnected = isWifiConnected(context);
//        if (!wificonnected) {
//            try {
//                Thread.sleep(100);
//                wificonnected = isWifiConnected(context);
//            } catch (InterruptedException e1) {
//                e1.printStackTrace();
//                return "";
//            }
//        }
//
//        if (!wificonnected) {
//            return "";
//        }
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

            return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
                    + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
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

        if (host.startsWith("192.168.") && host.endsWith(".1")) {
            return true;
        }

        return false;
    }


    public static boolean isNetAvailable(Context context) {
        NetworkInfo activeNetInfo = getActiveNetworkInfo(context);

        if (activeNetInfo == null) {
            return false;
        }

        if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI && activeNetInfo.isConnected()) {
            return true;
        } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE && activeNetInfo.isConnected()) {
            return true;
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


    public static boolean hasKeyManagerment(String capabilities) {
        if (TextUtils.isEmpty(capabilities)) return false;
        if (capabilities.toLowerCase().contains("wpa-psk") || capabilities.toLowerCase().contains("wpa2-psk")) {
            return true;
        }
        return false;
    }


    public static String getMacAddress()
    {

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            byte[] b;

            while (en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();

                if(intf == null){
                    continue;
                }
                if(!TextUtils.equals("wlan0",intf.getName())){
                    continue;
                }

                b = intf.getHardwareAddress();

                if(b == null){
                    continue;
                }
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < b.length; i++)
                {
                    if (i > 0)
                    {
                        buffer.append(':');
                    }
                    String str = Integer.toHexString(b[i] & 0xFF);
                    buffer.append(str.length() == 1 ? 0 + str : str);
                }
                String strMacAddr = buffer.toString();

                if(Logger.r) Logger.d(TAG,"mac addr:"+strMacAddr + " name:"+intf.getDisplayName());
                return strMacAddr;
            }
        } catch (Exception ex) {
            if(Logger.r) Logger.e(TAG, ex.toString());
        }
        return "";

    }


    public static void getAllConfiguredNetworks(final Context context, final List<WifiConfiguration> nets) throws Exception {

        if( Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId()){ //这个方法不能在主线程运行
            throw new Exception("xender WifiApUtil getConfiguredNetworks cannot in the main thread");
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    WifiManager wifiManager = getWifiManager(context);

                    if (wifiManager != null) {
                        if (Logger.r) {
                            Logger.c("ConnectWifiUtil", "system getConfiguredNetworks start");
                        }
                        List<WifiConfiguration> lists = wifiManager.getConfiguredNetworks();

                        if (Logger.r) {
                            Logger.c("ConnectWifiUtil", "system getConfiguredNetworks end");
                        }
                        if(lists != null){
                            nets.addAll(lists);
                        }

                    }
                }catch (Exception e){
                }
            }
        },"getAllConfiguredNetworks-thread");

        thread.start();
        try {
            //等这个线程3秒，如果没结果，也不再继续
            thread.join(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isWifiEnabled(WifiManager manager){
        try {
            return manager.isWifiEnabled();
        }catch (Exception e){}
        return false;
    }

}
