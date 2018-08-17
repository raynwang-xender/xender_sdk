package cn.xender.core.server.service;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import cn.xender.core.log.Logger;
import cn.xender.core.server.EmbbedWebServer;
import cn.xender.core.server.utils.Port;

public class HttpServerService extends Service {

    private static final String TAG = "HTTP_SERVER_SERVICE";

    private EmbbedWebServer server;

    //从界面端传进来的Handler,用于控制一些信息的更新
    private final IBinder binder = new LocalBinder();


    /**
     * IJettyService always runs in-process with the IJetty activity.
     */
    public class LocalBinder extends Binder {
        public HttpServerService getService(Handler _handler) {
            // Return this instance of LocalService so clients can call public methods
            return HttpServerService.this;
        }
    }


    /**
     * Android Service Start
     *
     * @see Service#onStart(Intent, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initState();

        return START_STICKY;
    }

    private void initState() {


        try {
            startServer();
        } catch (Exception e) {
            if (Logger.r) Logger.e(TAG, "Error starting server" + e);
        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        initState();

        return binder;
    }

    /**
     * Android Service create
     *
     * @see Service#onCreate()
     */
    @Override
    public void onCreate() {
    }


    /**
     * Android Service destroy
     *
     * @see Service#onDestroy()
     */
    @Override
    public void onDestroy() {

        try {

            stopServer();

        } catch (Exception e) {
            if (Logger.r) Logger.e(TAG, "Error stopping server" + e);
        } finally {
            try {
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancelAll();
            } catch (Exception e) {
            }
        }

    }


    @Override
    public void onLowMemory() {
        if (Logger.r) Logger.i(TAG, "Low on memory");
        super.onLowMemory();
    }

    @SuppressLint("NewApi")
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (Logger.r) Logger.d(TAG, "onTrimMemory--------" + level);
    }

    protected EmbbedWebServer getServer() {
        if(server == null) {
            server = new EmbbedWebServer(HttpServerService.this, null, 0, EmbbedWebServer.getTempFile(this, "NanoHTTPD-").getParent());//开启的时候，不绑定ip地址
        }
        return server;
    }

    protected void startServer() throws Exception {


        if (!getServer().isAlive()) {

            getServer().start();

            getServer().createNewDirectUrl();

//            Toast.makeText(HttpServerService.this,"server started",Toast.LENGTH_LONG).show();
        }

    }

    protected void stopServer() {
        try {
            if (Logger.r) Logger.i(TAG, "web server stopping");
            if (server != null)
                server.stop();
            if (Logger.r) Logger.i(TAG, "web server stopped");
            server = null;
//            Toast.makeText(HttpServerService.this,"server stopped",Toast.LENGTH_LONG).show();
        } finally {
            if (Logger.r) Logger.i(TAG, "Finally stopped ");
        }
    }


}
