package cn.xender.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import cn.xender.core.ap.utils.WifiAPUtil;
import cn.xender.core.server.NetWorker;
import cn.xender.core.server.service.HttpServerService;
import cn.xender.core.server.utils.Port;

public class HttpServerStart {

    private Context context;

    public HttpServerStart(Context context) {
        this.context = context;
    }

    private Handler _handler = new Handler();

    private HttpServerService httpServerService;

    private boolean binded = false;

    private ServiceConnection httpServerConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Called when the connection is made.
            httpServerService = ((HttpServerService.LocalBinder) service).getService(_handler);
        }

        public void onServiceDisconnected(ComponentName className) {
            // Received when the service unexpectedly disconnects.
            if (httpServerService != null) {
                httpServerService.stopSelf();
            }
            httpServerService = null;
        }
    };

    public void unbindHttpService() {
        try {
            context.unbindService(httpServerConnection);

            binded = false;
        }catch (Exception e){}
    }

    public void bindHttpService() {

        if(!binded){

            context.bindService(new Intent(context, HttpServerService.class), httpServerConnection, Context.BIND_AUTO_CREATE|Context.BIND_WAIVE_PRIORITY);

            binded = true;
        }
    }


    public static String formaQrCodeStringtUrl(String ssid,String password,String ip){
        if(!TextUtils.isEmpty(ssid)) {
            try {
                if(Build.VERSION.SDK_INT >=28){
                    return String.format(Locale.US,"http://www.xender.com/s?%s|%s|%s|%d|%s|%s","1",URLEncoder.encode(ssid,"utf-8"), URLEncoder.encode(password,"utf-8"), Port.getWebPort(), WifiAPUtil.getSegmentByIp(ip),WifiAPUtil.ip2Long(ip));
                }else{
                    return String.format(Locale.US,"http://www.xender.com/s?%s|%s|%s|%d|%s","1",URLEncoder.encode(ssid,"utf-8"), URLEncoder.encode(password,"utf-8"), Port.getWebPort(), WifiAPUtil.getSegmentByIp(ip));
                }
            } catch (UnsupportedEncodingException e) {
            }
        }
        return "";
    }


    public static boolean friendHasInstalled(String ip,int port ,String package_name){

        String url = friendHasInstalledUrl(ip,port,package_name);

        String result = NetWorker.post(url);

        return TextUtils.equals("1",result);
    }

    private static String friendHasInstalledUrl(String ip,int port,String pkg_name){
        return String.format(Locale.US,"http://%s:%d//waiter/installed?pkg=%s",ip,port,pkg_name);
    }
}
