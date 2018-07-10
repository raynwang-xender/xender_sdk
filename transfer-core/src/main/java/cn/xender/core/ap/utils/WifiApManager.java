package cn.xender.core.ap.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import cn.xender.core.log.Logger;

public class WifiApManager {
	private Context _context;
	private final WifiManager mWifiManager;
	// private static final String ADM_APAP = "AAA";
	public static final String TAG = "wifiAP";
	public String CREATE_AP_NAME="NULL";   
	public String brand="";
	public String brand_base="";
	private static Boolean mIsSupport;
	
	private static boolean isHtc = false;
	
	private static final String METHOD_GET_WIFI_AP_STATE = "getWifiApState";
    private static final String METHOD_SET_WIFI_AP_ENABLED = "setWifiApEnabled";
    private static final String METHOD_GET_WIFI_AP_CONFIG = "getWifiApConfiguration";
    private static final String METHOD_IS_WIFI_AP_ENABLED = "isWifiApEnabled";

    private static final Map<String, Method> methodMap = new HashMap<String, Method>();
	
	public static boolean isSupport() {
        if (mIsSupport != null) {
            return mIsSupport;
        }

        boolean result = Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO;
        if (result) {
            try {
                Field field = WifiConfiguration.class.getDeclaredField("mWifiApProfile");
                isHtc = field != null;
            } catch (Exception e) {
            }
        }
        if(Logger.r) Logger.c(TAG, "is htc? " + isHtc);
		if (result) {
            try {
                String name = METHOD_GET_WIFI_AP_STATE;
                Method method = WifiManager.class.getMethod(name);
                methodMap.put(name, method);
                result = method != null;
            } catch (SecurityException e) {
                if(Logger.r) Logger.ce(TAG, "SecurityException" + e);
            } catch (NoSuchMethodException e) {
            	if(Logger.r) Logger.ce(TAG, "NoSuchMethodException" + e);
            }
        }

        if (result) {
            try {
                String name = "";
                Method method = null;
				name = METHOD_SET_WIFI_AP_ENABLED;
				method = WifiManager.class.getMethod(name, WifiConfiguration.class, boolean.class);
                methodMap.put(name, method);
                result = method != null;
            } catch (SecurityException e) {
            	if(Logger.r) Logger.ce(TAG, "SecurityException" + e);
            } catch (NoSuchMethodException e) {
            	if(Logger.r) Logger.ce(TAG, "NoSuchMethodException" + e);
            }
        }

        if (result) {
            try {
                String name = METHOD_GET_WIFI_AP_CONFIG;
                Method method = WifiManager.class.getMethod(name);
                methodMap.put(name, method);
                result = method != null;
            } catch (SecurityException e) {
            	if(Logger.r) Logger.ce(TAG, "SecurityException" + e);
            } catch (NoSuchMethodException e) {
            	if(Logger.r) Logger.ce(TAG, "NoSuchMethodException" + e);
            }
        }

        if (result) {
            try {
                String name = getSetWifiApConfigName();
                Method method = WifiManager.class.getMethod(name, WifiConfiguration.class);
                methodMap.put(name, method);
                result = method != null;
            } catch (SecurityException e) {
            	if(Logger.r) Logger.ce(TAG, "SecurityException" + e);
            } catch (NoSuchMethodException e) {
            	if(Logger.r) Logger.ce(TAG, "NoSuchMethodException" + e);
            }
        }

        if (result) {
            try {
                String name = METHOD_IS_WIFI_AP_ENABLED;
                Method method = WifiManager.class.getMethod(name);
                methodMap.put(name, method);
                result = method != null;
            } catch (SecurityException e) {
            	if(Logger.r) Logger.ce(TAG, "SecurityException" + e);
            } catch (NoSuchMethodException e) {
            	if(Logger.r) Logger.ce(TAG, "NoSuchMethodException" + e);
            }
        }

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			try {
				String name = "cancelLocalOnlyHotspotRequest";
				Method method = WifiManager.class.getMethod(name);
				methodMap.put(name, method);
				result = method != null;
			} catch (SecurityException e) {
				if(Logger.r) Logger.ce(TAG, "SecurityException" + e);
			} catch (NoSuchMethodException e) {
				if(Logger.r) Logger.ce(TAG, "NoSuchMethodException" + e);
			}
		}
        mIsSupport = result;
        return result;
    }
	
	public static boolean isHtc() {
        return isHtc;
    }
    
    private static String getSetWifiApConfigName() {
        return isHtc? "setWifiApConfig": "setWifiApConfiguration";
    }
	
	public WifiApManager(Context context, WifiManager manager) {
		if (!isSupport()) {
			if(Logger.r) Logger.ce(TAG, "Unsupport Ap!");
        }
		_context = context;
		mWifiManager = manager;
		init(context);
	}
	
	private void init(Context context) {

		try{
			brand = Build.BRAND.substring(0, 3);
		}catch(StringIndexOutOfBoundsException e){
			brand = "";
		}
		
		brand_base= Base64.encodeToString(brand.getBytes(), Base64.URL_SAFE);
		
		try{
			brand_base = brand_base.substring(0, brand_base.length() - 1); 
		}catch(StringIndexOutOfBoundsException e){
			brand_base = "";
		}
	}


	// @jiang
	// 得到Ap下的SSID
	public String getWifiApSSID() {
		try {
			
			if (!("NULL".equalsIgnoreCase(CREATE_AP_NAME))) {
				return CREATE_AP_NAME;
			}
			
			if(Logger.r) Logger.c(TAG, "getWifiApSSID CREATE_AP_NAME:"+CREATE_AP_NAME);
			
			WifiConfiguration apConfig = getWifiApConfiguration(mWifiManager);
			
			if(Logger.r) Logger.c(TAG, "getWifiApSSID, apConfig=" + apConfig);
			
			
			if (apConfig == null) { //HHH的bug 
				return CREATE_AP_NAME;   
				//return null; 
			}

			if(Logger.r) Logger.c(TAG, "getWifiApSSID, ssid=" + apConfig.SSID);

			if (apConfig.SSID==null) {
				
				return CREATE_AP_NAME;  
			}
			
			return apConfig.SSID;

		} catch (SecurityException e) {
			if(Logger.r) Logger.ce(TAG, "getWifiApSSID,e1=" + e);
			return null;
		} catch (IllegalArgumentException e) {
			if(Logger.r) Logger.ce(TAG, "getWifiApSSID,e2=" + e);
			return null;
		} 
	}

	// @jiang 得到自己的地址，连接到别人的wifi下，或者 自己是热点！
	public String getWifiApIP() {

		if (isWifiApEnabled()) {
			return "" + getLocalIp();
		} else {
			return null;
		}

	}

	
	 private  String getLocalIp() 
	    {
	        try
	        {
	            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
	            {
	                NetworkInterface intf = en.nextElement(); 
	                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
	                {
	                    InetAddress inetAddress = enumIpAddr.nextElement(); 
	                    
	                    if(Logger.r) Logger.c(TAG, "getLocalIp,111=" + inetAddress.getHostAddress());
	                    
	                    if (!isReservedAddr(inetAddress))
	                    {
	                    	if(Logger.r) Logger.c(TAG, "getLocalIp,222");
	                        return inetAddress.getHostAddress();   
	                    }
	                } 
	            }
	        }
	        catch (SocketException ex) 
	        {
	            if(Logger.r) Logger.ce("IP SocketException ", ex.toString());
	        }
	        return "xxx";
	    }
	 
	 private  boolean isReservedAddr(InetAddress inetAddr) {
         if (inetAddr.isAnyLocalAddress()
                         || inetAddr.isLinkLocalAddress() 
                         || inetAddr.isLoopbackAddress()){ 
                 return true;  
         } 
          
         if( isWifiApEnabled() && !inetAddr.getHostAddress().endsWith(".1")){ 
             return true; 
         } 
         
         return false;      
	 }
	


	public boolean isWifiConnected() {
		try {
			NetworkInfo activeNetInfo = WifiAPUtil.getActiveNetworkInfo(_context);// 获取网络的连接情况

			if (activeNetInfo == null) {
				return false;
			}

			if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				return true;
			} else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				return false;
			}
		}catch (Exception e){
		}
		return false;

	}
	
	

	/**
	 * Start AccessPoint mode with the specified configuration. If the radio is
	 * already running in AP mode, update the new configuration Note that
	 * starting in access point mode disables station mode operation
	 * 
	 *
	 * @return {@code true} if the operation succeeds, {@code false} otherwise
	 */
	public boolean setWifiApEnabledForBelowAndroidN(String ssid, String pwd, boolean enabled) {
		try {
			if (enabled) { // disable WiFi in any case
				if(Logger.r) Logger.c(TAG, "setWifiApEnabled,open AP, by invoke method。。。123,Build.BRAND=" + Build.MANUFACTURER);

				WifiConfiguration apConfig = null;
				if(!TextUtils.isEmpty(ssid)){
					apConfig = createMyWifiApConfiguration(pwd,ssid);
				}

				// @jiang
				if(Logger.r) Logger.c(TAG, "try----setWifiApEnabled");
				Boolean bb = setMyWifiApEnabled(apConfig, enabled);
				if(Logger.r) Logger.c(TAG, "----setWifiApEnabled.bb=" + bb);
				if (bb && apConfig != null) {

					CREATE_AP_NAME = apConfig.SSID ; 
				}
				return bb; 

			} else {
				
				boolean bb = setMyWifiApEnabled(null, enabled);
				
				if(Logger.r) Logger.c(TAG, "----setWifiApEnabled false.bb=" + bb);
				return bb;
			}

		} catch (Exception e) {

			if(Logger.r) Logger.ce(TAG, "this.getClass().toString(),e=" + e);
			return false;
		}
	}


	public boolean setWifiApEnabledForAndroidN_MR1(String ssid, String pwd, boolean enabled,boolean needOpenManual) {
		try {
			if (enabled) { // disable WiFi in any case
				if(Logger.r) Logger.c(TAG, "setWifiApEnabled,open AP, by invoke method。。。123,Build.BRAND=" + Build.MANUFACTURER);


				WifiConfiguration apConfig = null;
				if(!TextUtils.isEmpty(ssid)){
					apConfig = createMyWifiApConfiguration(pwd,ssid);
				}
				if(needOpenManual){

					if(apConfig != null){

						boolean result = setWifiApConfiguration(apConfig);

						if(result){
							CREATE_AP_NAME = apConfig.SSID ;
						}

						return result;
					}else{
						//这里apConfig==null 这样就不用设置，直接开始手动开启热点
						return true;
					}

				}else{

					// @jiang
					if(Logger.r) Logger.c(TAG, "try----setWifiApEnabled");
					Boolean bb = setMyWifiApEnabled(apConfig, enabled);
					if(Logger.r) Logger.c(TAG, "----setWifiApEnabled.bb=" + bb);
					if (bb && apConfig != null) {
						CREATE_AP_NAME = apConfig.SSID ;
					}
					return bb;
				}

			} else {

				boolean bb = setMyWifiApEnabled(null, enabled);

				if(Logger.r) Logger.c(TAG, "----setWifiApEnabled false.bb=" + bb);
				return bb;
			}

		} catch (Exception e) {

			if(Logger.r) Logger.ce(TAG, "this.getClass().toString(),e=" + e);
			return false;
		}
	}


    public boolean setWifiApEnabledOnAndroidO(boolean enabled,WifiManager.LocalOnlyHotspotCallback callback) {
        if(Build.VERSION.SDK_INT < 26){
            return false;
        }

        try {
            if (enabled) { // disable WiFi in any case

                mWifiManager.startLocalOnlyHotspot(callback,null);
                return true;

            } else {

                cancelLocalOnlyHotspotRequest();

                return true;
            }

        } catch (Exception e) {

            if(Logger.r) Logger.ce(TAG, "this.getClass().toString(),e=" + e);
            return false;
        }
    }

	//这里创建开启ap所使用的wificonfiguration,里面包括热点名称的创建
	private WifiConfiguration createMyWifiApConfiguration(String pwd,String ssid){
		
		WifiConfiguration apConfig = new WifiConfiguration();
		
		apConfig.SSID = ssid;   //9
		if(Logger.r) Logger.c(TAG, "apRomdamid1=" + ssid);

        if(TextUtils.isEmpty(pwd)){
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            apConfig.preSharedKey = null;
        }else{
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            apConfig.preSharedKey = pwd;
        }

		if (isHtc()) { 
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
				if(Logger.r) Logger.ce(TAG, "NoSuchFieldException " + e);
			}
		    
		}
		
		return apConfig;
	}

	private boolean setMyWifiApEnabled(WifiConfiguration configuration, boolean enabled) {
        boolean result = false;
        try {
			Method method = methodMap.get(METHOD_SET_WIFI_AP_ENABLED);
			result = (Boolean)method.invoke(mWifiManager, configuration, enabled);

        } catch (Throwable e) {
            e.printStackTrace();
            if(Logger.r) Logger.ce(TAG,"set wifi ap enable exception:"+e);
        }
        return result;
    }
	


	/**
	 * Gets the Wi-Fi enabled state.
	 * 
	 * @return {@link WIFI_AP_STATE}
	 * @see #isWifiApEnabled()
	 */
	public WIFI_AP_STATE getWifiApState() {
		try {
			Method method = mWifiManager.getClass().getMethod("getWifiApState");

			int tmp = ((Integer) method.invoke(mWifiManager));

			// Fix for Android 4
			if (tmp >= 10) {
				tmp = tmp - 10;
			}

			return WIFI_AP_STATE.class.getEnumConstants()[tmp];
		} catch (Exception e) {
			if(Logger.r) Logger.ce(TAG, "" + e);
			return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
		}
	}
 
	/**
	 * Return whether Wi-Fi AP is enabled or disabled.
	 * 
	 * @return {@code true} if Wi-Fi AP is enabled
	 * @see #getWifiApState()
	 * 
	 * @hide Dont open yet
	 */
	public boolean isWifiApEnabled() {
		return getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
	}

	/**
	 * Gets the Wi-Fi AP Configuration.
	 * 
	 * @return AP details in {@link WifiConfiguration}
	 */
	public static WifiConfiguration getWifiApConfiguration(WifiManager mWifiManager) {
		WifiConfiguration configuration = null;
		
        try {
            Method method = methodMap.get(METHOD_GET_WIFI_AP_CONFIG);
            configuration = (WifiConfiguration) method.invoke(mWifiManager);
            if(isHtc()){
                configuration = getHtcWifiApConfiguration(configuration);
            }
        } catch (Exception e) {
			e.printStackTrace();
            if(Logger.r) Logger.ce(TAG, "getWifiApConfiguration exception:"+e);
        }
		return configuration;
	}
	
	private static WifiConfiguration getHtcWifiApConfiguration(WifiConfiguration standard){
        WifiConfiguration htcWifiConfig = standard;
        try {
            Object mWifiApProfileValue = WifiApFieldUtils.getFieldValue(standard, "mWifiApProfile");

            if (mWifiApProfileValue != null) {
                htcWifiConfig.SSID = (String) WifiApFieldUtils.getFieldValue(mWifiApProfileValue, "SSID");
            }
        } catch (Exception e) {
            if(Logger.r) Logger.ce(TAG, "getHtcWifiApConfiguration exception:"+e);
        }
        return htcWifiConfig;
    }

	/**
	 * Sets the Wi-Fi AP Configuration.
	 * 
	 * @return {@code true} if the operation succeeded, {@code false} otherwise
	 */
	/*public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
		try {
			Method method = mWifiManager.getClass().getMethod(
					"setWifiApConfiguration", WifiConfiguration.class);
			return (Boolean) method.invoke(mWifiManager, wifiConfig);
		} catch (Exception e) {
			Logger.e(TAG, "--e="+e);  
			return false;
		}
	}*/

	public static boolean setWifiApConfiguration(WifiManager mWifiManager,WifiConfiguration netConfig) {
		if(mWifiManager == null){
			return false;
		}
		boolean result = false;
		try {

			Method method = methodMap.get(getSetWifiApConfigName());
			if(Logger.r) Logger.c(TAG, "setWifiApConfiguration -> " + method.getName());
			if (isHtc()) {
				int rValue = (Integer) method.invoke(mWifiManager, netConfig);
				if(Logger.r) Logger.c(TAG, "rValue -> " + rValue);
				result = rValue > 0;
			} else {
				result = (Boolean) method.invoke(mWifiManager, netConfig);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(Logger.r) Logger.ce(TAG, "setWifiApConfiguration exception happened: "+ e);
		}
		return result;
	}


	private boolean setWifiApConfiguration(WifiConfiguration netConfig) {
        return setWifiApConfiguration(mWifiManager,netConfig);
    }


	private void cancelLocalOnlyHotspotRequest(){
		try {

			Method method = methodMap.get("cancelLocalOnlyHotspotRequest");
			method.invoke(mWifiManager);
		} catch (Exception e) {
			e.printStackTrace();
			if(Logger.r) Logger.ce(TAG, "cancelLocalOnlyHotspotRequest exception happened: "+ e);
		}
	}
}