package cn.xender.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import cn.xender.core.server.DownloadMe;
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


    public static String formaQrCodeStringtUrl(String ssid,String password){
        if(!TextUtils.isEmpty(ssid)) {
            try {
                return String.format(Locale.US,"http://www.xender.com/?%s&%s&%s&%d", DownloadMe.URL_PATTERN,URLEncoder.encode(ssid,"utf-8"), URLEncoder.encode(password,"utf-8"), Port.getWebPort());
            } catch (UnsupportedEncodingException e) {
            }
        }
        return "";
    }
}
