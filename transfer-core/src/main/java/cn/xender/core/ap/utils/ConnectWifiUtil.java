package cn.xender.core.ap.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import cn.xender.core.log.Logger;

/**
 * Created by Administrator on 2015/9/21.
 */
public class ConnectWifiUtil {
    private static final String TAG = "ConnectWifiUtil";
    private ConnectWifiUtil(){}

    public static boolean connect(Context context,WifiManager manager,String ssid,String bssid,String password,String static_ip) {
        if (Logger.r) {
            Logger.c(TAG, "connect() --------connect=" + static_ip);
        }
        if (Build.VERSION.SDK_INT < 11 || TextUtils.isEmpty(static_ip)) {// || !TextUtils.isEmpty(password)
            return connectapWithDhcp(context,manager, ssid, password);
        } else {
            return connectapWithStatic(context, manager, ssid, password, static_ip);
        }
    }

    private static boolean connectapWithStatic(Context context,WifiManager manager,String ssid,String password, String static_ip) {

        if (TextUtils.isEmpty(ssid)) {
            return false;
        }
        if (Logger.r) {
            Logger.c(TAG, "Try to connect to " + ssid);
        }

        WifiConfiguration andou = removeNetworkIfFailedReturn(context,manager,ssid);

        if(andou == null) {

            andou = createWifiConfiguration(ssid,password);
            if(!TextUtils.isEmpty(static_ip)){
                try {
                    connectionSet("STATIC", context, andou, static_ip);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            andou.networkId = manager.addNetwork(andou);
            if (Logger.r) {
                Logger.d(TAG,"my WifiConfiguration:" + andou);
            }
        }

        boolean b = connectAp(context,manager,andou,andou.networkId,true);

        if (Logger.r) {
            Logger.c(TAG, "connect ap from static ip: " + static_ip + " result:" + b);
        }

        return true;
    }

    private static WifiConfiguration removeNetworkIfFailedReturn(Context context,WifiManager manager,String needToConnectSsid){
        WifiConfiguration andou = null;
        try{
            List<WifiConfiguration> nets = new ArrayList<>();

            if (Logger.r) {
                Logger.c(TAG, "getAllConfiguredNetworks start");
            }
            WifiAPUtil.getAllConfiguredNetworks(context,nets);

            if (Logger.r) {
                Logger.c(TAG, "getAllConfiguredNetworks end and list size:" + nets.size());
            }

            for (WifiConfiguration net:nets) {
                if (Logger.r) {
                    Logger.c(TAG, "SSID: " + net.SSID + " id:" + net.networkId + " BSSID:" + net.BSSID);
                }
                if(TextUtils.equals(net.SSID, needToConnectSsid) || TextUtils.equals(net.SSID,"\""+needToConnectSsid+"\"")){
                    if (Logger.r) {
                        Logger.c(TAG, "removeNetwork id:" + net.networkId);
                    }
                    boolean success = manager.removeNetwork(net.networkId);
                    if(!success){
                        if (Logger.r) {
                            Logger.c(TAG, "removeNetwork failure:" + net.networkId);
                        }
                        andou = net;
                    }
                    break;
                }
            }
        }catch (Throwable e){

        }

        return andou;
    }

    public static boolean connectAp(Context context,WifiManager wifiManager, WifiConfiguration config,int networkId,boolean isStaticAssignment){
        boolean result = false;
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                connect(context,wifiManager, config, networkId);
                result = true;
                return true;
            }
            throw new NoSuchMethodException();
        } catch (NoSuchMethodException e) {
            if (Logger.r) {
                Logger.c(TAG, "connect by enableNetwork ");
            }
            if(networkId == -1){
                networkId = wifiManager.addNetwork(config);
            }
            if(networkId == -1){
                result=false;
                return false;
            }
            if(!isStaticAssignment){
                wifiManager.enableNetwork(networkId, false);
                config.networkId = networkId;
                wifiManager.updateNetwork(config);
                wifiManager.saveConfiguration();
            }

            result = wifiManager.enableNetwork(networkId, true);

            if(!isStaticAssignment){
                wifiManager.reassociate();
            }

            if(result){
                bindToNetwork(context);
            }

            return result;
        }finally {
//            if(result && networkId>0){
//                bindTheWifi(networkId);
//            }
        }
        /*if(networkId == -1){
            networkId = wifiManager.addNetwork(config);
        }
        if(networkId == -1) return false;
        boolean b = wifiManager.enableNetwork(networkId, true);
        return b;*/
    }

//    public static void bindTheWifi(int networkId){
//        try {
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                ConnectivityManager connectivityManager = WifiAPUtil.getConnectivityManager(MainContext.getInstance());
//
//
//                connectivityManager.bindProcessToNetwork(getNetworkByNetworkId(networkId));
//            }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//
//                ConnectivityManager.setProcessDefaultNetwork(getNetworkByNetworkId(networkId));
//            }else {
//                //nothing
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//
//    }

    private static Network getNetworkByNetworkId(int networkId){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Object network = Network.class
                        .getConstructor()
                        .newInstance(networkId);
                return (Network) network;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void connect(Context context,WifiManager wifiManager, WifiConfiguration config,int networkId) throws NoSuchMethodException {
       try {
           if (Logger.r) {
               Logger.d(TAG, "connect to " + config.SSID);
           }
           Method[] ms = WifiManager.class.getDeclaredMethods();
           for (Method mtd: ms) {
               if(mtd.getName().equals("connect")){
                   Class<?>[] types = mtd.getParameterTypes();
                   if(networkId == -1){
                       if (types!=null && types.length == 2 && types[0].getName().indexOf("WifiConfiguration") >= 0){
                           mtd.invoke(wifiManager,config,createActionListenerProxy(context,types[1]));
                           return;
                       }
                   }else {
                       if (types!=null && types.length == 2 && types[0].getName().indexOf("WifiConfiguration") < 0){
                           mtd.invoke(wifiManager,networkId,createActionListenerProxy(context,types[1]));
                           return;
                       }
                   }
               }
           }

           throw new NoSuchMethodException();
       }catch (Exception e){
           if (Logger.r) {
               Logger.d(TAG, "connect failed ", e);
           }
           throw new NoSuchMethodException();
       }
    }

    public static Object createActionListenerProxy(final Context context, Class c){
        Object actionListener = Proxy.newProxyInstance(ConnectWifiUtil.class.getClassLoader(), new Class[]{c}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("onSuccess")) {

                    bindToNetwork(context);

                    if (Logger.r) {
                        Logger.c(TAG, "connect successed");
                    }
                } else {
                    if (Logger.r) {
                        Logger.c(TAG, "connect failed");
                    }
                }
                return null;
            }
        });
        return actionListener;
    }

    private static boolean connectapWithDhcp(Context context,WifiManager manager,String ssid,String password) {

        if (TextUtils.isEmpty(ssid) || manager == null) {
            return false;
        }

        WifiConfiguration andou = removeNetworkIfFailedReturn(context,manager,ssid);

        if (Logger.r) {
            Logger.c(TAG, "i want to one connectap, the ap_name=" + ssid);
        }

        if(andou == null){
            andou = createWifiConfiguration(ssid,password);
            andou.networkId = manager.addNetwork(andou);
        }

        boolean b = connectAp(context,manager,andou,andou.networkId,false);

        if (Logger.r) {
            Logger.c(TAG, "connectap? result=" + b);
        }
        return b;

    }

    private static WifiConfiguration createWifiConfiguration(String ssid,String password){
        WifiConfiguration andou = new WifiConfiguration();
        andou.allowedAuthAlgorithms.clear();
        andou.allowedGroupCiphers.clear();
        andou.allowedKeyManagement.clear();
        andou.allowedPairwiseCiphers.clear();
        andou.allowedProtocols.clear();

        andou.SSID = ("\"" + ssid + "\"");
        andou.status = WifiConfiguration.Status.ENABLED;
        andou.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        andou.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        if (TextUtils.isEmpty(password)) {
            andou.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else {
            andou.preSharedKey = "\"" + password + "\"";
            andou.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            andou.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            andou.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            andou.hiddenSSID = true;
            andou.status = WifiConfiguration.Status.ENABLED;

        }
        andou.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        andou.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        andou.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            andou.isHomeProviderNetwork = true;
        }
        return andou;
    }


    private static Object getFieldValue(Object paramObject, String paramString)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        return paramObject.getClass().getField(paramString).get(paramObject);
    }

    private static Object getDeclaredFieldValue(Object paramObject, String paramString)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field localField = paramObject.getClass().getDeclaredField(paramString);
        localField.setAccessible(true);
        return localField.get(paramObject);
    }

    private static void connectionSet(String net_type, Context paramContext,
                                     WifiConfiguration paramWifiConfiguration, String static_ip)
            throws SecurityException, IllegalArgumentException,
            NoSuchFieldException, IllegalAccessException, UnknownHostException,
            NoSuchMethodException, ClassNotFoundException,
            InstantiationException, InvocationTargetException {
        if ((net_type.equals("DHCP")) || (TextUtils.isEmpty(static_ip))) {
            setwifi(net_type, paramWifiConfiguration);
            return;
        }

        if(Build.VERSION.SDK_INT < 11){
            return;
        }

        String str = static_ip.substring(0, static_ip.lastIndexOf(".")) + ".1";
        if(Build.VERSION.SDK_INT >= 21){
            setwifiOnVersion5(net_type, paramWifiConfiguration,static_ip,str);
        }else{
            try {
                setwifi(net_type, paramWifiConfiguration);
            } catch (Exception e) {
                if (Logger.r) {
                    Logger.e(TAG, "set STATIC TYPE failure", e);
                }
            }

            try {
                setLinkAddress(paramWifiConfiguration, static_ip);
            } catch (Exception e) {
                if (Logger.r) {
                    Logger.e(TAG, "set ip failure", e);
                }
            }

            try {
                setRoute(paramWifiConfiguration, str);
            } catch (Exception e) {
                if (Logger.r) {
                    Logger.e(TAG,"set route failure",e);
                }
            }

            try {
                setDns(paramWifiConfiguration, str);
            } catch (Exception e) {
                if (Logger.r) {
                    Logger.e(TAG,"set dns failure",e);
                }
            }

            try {
                setGateway(paramWifiConfiguration, str);
            } catch (Exception e) {
                if (Logger.r) {
                    Logger.e(TAG,"set gateway failure",e);
                }
            }

        }

        //Log.d("test", "-------------------");

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            ContentResolver localContentResolver = paramContext.getContentResolver();
//            Logger.d(TAG,"read before:"+Settings.Global.getString(localContentResolver,"captive_portal_detection_enabled"));
//            Settings.Global.putString(localContentResolver, "captive_portal_detection_enabled", "0");
//            Logger.d(TAG,"read after:"+Settings.Global.getString(localContentResolver,"captive_portal_detection_enabled"));
//        }


    }


    private static void setRoute(WifiConfiguration paramWifiConfiguration, String str) throws UnknownHostException, NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {

        if (Build.VERSION.SDK_INT >= 14) {
            Object localObject4 = getFieldValue(paramWifiConfiguration,
                    "linkProperties");
            if (localObject4 != null) {
                Object localObject5 = Class
                        .forName("android.net.RouteInfo")
                        .getConstructor(new Class[] { InetAddress.class })
                        .newInstance(new Object[] { InetAddress.getByName(str) });//gateway
                ArrayList localArrayList3 = (ArrayList) getDeclaredFieldValue(localObject4,
                        "mRoutes");
                localArrayList3.clear();
                localArrayList3.add(localObject5);
            }
        }
    }


    public static void setwifi(String paramString,
                               WifiConfiguration paramWifiConfiguration) throws SecurityException,
            IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException {
        Field localField = paramWifiConfiguration.getClass().getField(
                "ipAssignment");
        localField.set(paramWifiConfiguration,
                Enum.valueOf((Class<Enum>) (localField.getType()), paramString));
    }

    public static void setwifiOnVersion5(String paramString,
                                         WifiConfiguration paramWifiConfiguration,String static_ip,String gateway_ip) throws SecurityException,
            IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, UnknownHostException {

        Object mIpConfiguration = getDeclaredFieldValue(paramWifiConfiguration,
                "mIpConfiguration");
        if (mIpConfiguration != null) {
            if (Logger.r) {
                Logger.ce("connect_wifi", "mIpConfiguration was got the obj " + mIpConfiguration);
            }

            Field localField = mIpConfiguration.getClass().getField("ipAssignment");
            localField.set(mIpConfiguration,
                    Enum.valueOf((Class<Enum>)(localField.getType()), paramString));


            Object mStaticIpConfiguration = Class
                    .forName("android.net.StaticIpConfiguration")
                    .getConstructor()
                    .newInstance();


            setLinkAddressOn5(mStaticIpConfiguration, static_ip);

            setGatewayOn5(mStaticIpConfiguration, gateway_ip);

            setDnsServersOn5(mStaticIpConfiguration,gateway_ip);


            Field staticConfigField = mIpConfiguration.getClass().getField("staticIpConfiguration");
            staticConfigField.set(mIpConfiguration, mStaticIpConfiguration);

            if (Logger.r) {
                Logger.d("connect_wifi", "mStaticIpConfiguration was got the obj after changed " + paramWifiConfiguration);
            }
        }

    }


    private static void setLinkAddress(WifiConfiguration paramWifiConfiguration, String static_ip) throws UnknownHostException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        InetAddress localInetAddress1 = InetAddress.getByName(static_ip);
        Object localObject1 = getFieldValue(paramWifiConfiguration, "linkProperties");
        if (localObject1 != null) {
            Class localClass = Class.forName("android.net.LinkAddress");

            Class[] arrayOfClass = new Class[2];
            arrayOfClass[0] = InetAddress.class;
            arrayOfClass[1] = Integer.TYPE;

            Constructor localConstructor = localClass.getConstructor(arrayOfClass);
            Object[] arrayOfObject = new Object[2];
            arrayOfObject[0] = localInetAddress1;
            arrayOfObject[1] = Integer.valueOf(24);
            Object localObject6 = localConstructor.newInstance(arrayOfObject);

            ArrayList localArrayList4 = (ArrayList) getDeclaredFieldValue(localObject1,
                    "mLinkAddresses");
            localArrayList4.clear();
            localArrayList4.add(localObject6);
        }
    }

    private static void setLinkAddressOn5(Object mStaticIpConfiguration,String static_ip) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException, UnknownHostException {

        InetAddress localInetAddress1 = InetAddress.getByName(static_ip);

        Class localClass = Class.forName("android.net.LinkAddress");

        Class[] arrayOfClass = new Class[2];
        arrayOfClass[0] = InetAddress.class;
        arrayOfClass[1] = Integer.TYPE;

        Constructor localConstructor = localClass.getConstructor(arrayOfClass);
        Object[] arrayOfObject = new Object[2];
        arrayOfObject[0] = localInetAddress1;
        arrayOfObject[1] = Integer.valueOf(24);

        Object localObject6 = localConstructor.newInstance(arrayOfObject);

        Field ipAddress = mStaticIpConfiguration.getClass().getField("ipAddress");
        ipAddress.set(mStaticIpConfiguration, localObject6);
    }

    private static void setGateway(WifiConfiguration paramWifiConfiguration, String str) throws UnknownHostException, NoSuchFieldException, IllegalAccessException {
        InetAddress localInetAddress3 = InetAddress.getByName(str);
        Object localObject2 = getFieldValue(paramWifiConfiguration,
                "linkProperties");
        if (localObject2 != null) {
            ArrayList localArrayList1 = (ArrayList) getDeclaredFieldValue(localObject2,
                    "mGateways");
            localArrayList1.clear();
            localArrayList1.add(localInetAddress3);
        }
    }

    private static void setGatewayOn5(Object mStaticIpConfiguration,String gateway_ip) throws NoSuchFieldException, IllegalAccessException, UnknownHostException {
        InetAddress gateway_addr = InetAddress.getByName(gateway_ip);
        Field ipAddress = mStaticIpConfiguration.getClass().getField("gateway");
        ipAddress.set(mStaticIpConfiguration,gateway_addr);
    }

    private static void setDns(WifiConfiguration paramWifiConfiguration, String str) throws UnknownHostException, NoSuchFieldException, IllegalAccessException {

        Object localObject3 = getFieldValue(paramWifiConfiguration,
                "linkProperties");
        if (localObject3 != null){
            ArrayList localArrayList2 = (ArrayList) getDeclaredFieldValue(localObject3,
                    "mDnses");
            localArrayList2.clear();
            localArrayList2.add(InetAddress.getByName(str));
//            localArrayList2.add(InetAddress.getByName("8.8.8.8"));
//            localArrayList2.add(InetAddress.getByName("4.4.4.4"));
        }
    }

    private static void setDnsServersOn5(Object mStaticIpConfiguration,String gateway_ip) throws NoSuchFieldException, IllegalAccessException {
        try {
            ArrayList localArrayList2 = (ArrayList) getDeclaredFieldValue(mStaticIpConfiguration,"dnsServers");
            localArrayList2.clear();
            localArrayList2.add(InetAddress.getByName(gateway_ip));
//            localArrayList2.add(InetAddress.getByName("8.8.8.8"));
//            localArrayList2.add(InetAddress.getByName("4.4.4.4"));
        } catch (UnknownHostException e) {

        }
    }

    private static void setProxy(Object mIpConfiguration) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {

        Field proxySettings = mIpConfiguration.getClass().getField("proxySettings");
        proxySettings.set(mIpConfiguration,Enum.valueOf((Class<Enum>)(proxySettings.getType()), "PAC"));

        Object httpProxy;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            httpProxy = ProxyInfo.buildPacProxy(Uri.parse("http://127.0.0.1:6789/pac"));
        }else{

            httpProxy = Class
                    .forName("android.net.ProxyInfo")
                    .getConstructor(Uri.class)
                    .newInstance(Uri.parse("http://127.0.0.1:6789/pac"));
        }

        Field staticConfigField = mIpConfiguration.getClass().getField("httpProxy");
        staticConfigField.set(mIpConfiguration, httpProxy);
    }


    private static ConnectivityManager.NetworkCallback networkCallback;

    private static void bindToNetwork(Context context){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ConnectivityManager  connectivityManager = WifiAPUtil.getConnectivityManager(context);

                NetworkRequest.Builder builder = new  NetworkRequest.Builder();
//                builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN);
//                builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);
//                builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED);
                builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

                connectivityManager.registerNetworkCallback(builder.build(),getNetworkCallback(context));
            }
        }catch (Exception e){
            if (Logger.r)
                Logger.e(TAG,"####bindProcessToNetwork error"+e.toString());
        }
   }

   @TargetApi(Build.VERSION_CODES.M)
   private static ConnectivityManager.NetworkCallback getNetworkCallback(final Context context){
       if(networkCallback == null){
           networkCallback = new ConnectivityManager.NetworkCallback(){
               @Override
               public void onAvailable(Network network){
                   try {
                       String ssid = WifiAPUtil.getWifiSSID(context);

                       if (Logger.r)
                           Logger.d(TAG,"onAvailable :"+network.toString() + " ,and ssid:"+ssid);
                       if(FilterManager.acceptSSID(ssid)){
                           WifiAPUtil.getConnectivityManager(context).bindProcessToNetwork(network);
                       }
                   }catch (Exception e){

                   }

               }

//               @Override
//               public void  onLost(Network network){
//                   try {
//                       ConnectivityManager  connectivityManager =  WifiAPUtil.getConnectivityManager(MainContext.getInstance());
//
//                       connectivityManager.bindProcessToNetwork(null);
//                       connectivityManager.unregisterNetworkCallback(this);
//                   }catch (Exception e){
//
//                   }
//                   if (Logger.r)
//                       Logger.e(TAG,"onLost :"+network.toString());
//               }
           };
       }

       return networkCallback;
   }

    public static void unbindNetwork(Context context){

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if(networkCallback != null){

                    ConnectivityManager  connectivityManager =  WifiAPUtil.getConnectivityManager(context);

                    connectivityManager.bindProcessToNetwork(null);

                    connectivityManager.unregisterNetworkCallback(networkCallback);

                    networkCallback = null;

                    if (Logger.r)
                        Logger.d(TAG,"unbind network ");
                }

            }
        }catch (Exception e){

        }

    }

}
