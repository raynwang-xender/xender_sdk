package cn.xender.core.server;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import cn.xender.core.log.Logger;


public class NetWorker {
    public static final String TAG = "NetWorker";
    public static final String EX = "ex";


    public static String getServerDatabyPost(String serverPath, String strData) {

        if (TextUtils.isEmpty(serverPath) || TextUtils.isEmpty(strData))
            return "-1";
        if(Logger.r) Logger.e("Exception", "serverPath=" + serverPath + "--strData=" + strData);
        byte[] data = strData.getBytes();
        URL url;
        try {
            url = new URL(serverPath);
        } catch (MalformedURLException e) {
            if(Logger.r) Logger.e("Exception", "MalformedURLException=" + e);
            return "-1";
        }

        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            if(Logger.r) Logger.e("Exception", "IOException=" + e);
            return "-1";
        }

        if (conn == null) return "-1";

        conn.setConnectTimeout(6 * 1000);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        try {
            conn.setRequestMethod("POST");
        } catch (ProtocolException e) {
            if(Logger.r) Logger.e("Exception", "ProtocolException=" + e);
            return "-1";
        }
        conn.setRequestProperty("Connection", "close");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(data.length));
        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        try {
            DataOutputStream outStream = new DataOutputStream(conn
                    .getOutputStream());
            outStream.write(data);// send Data in body
            // Log.e(TAG, "data="+ new String(data));
            outStream.flush();
            outStream.close();
            // response OK
            if (conn.getResponseCode() == 200) {
                if(Logger.r) Logger.d("Step", "conn.getResponseCode() == 200");
                return new String(readStream(conn.getInputStream()), "UTF-8");
            } else if (conn.getResponseCode() == 400) {
                if(Logger.r) Logger.e("Exception", "ResponseCode=" + conn.getResponseCode() + "," + serverPath);
                return "-1"; // Unauthorized
            } else if (conn.getResponseCode() == 401) {
                if(Logger.r) Logger.e("Exception", "ResponseCode=" + conn.getResponseCode() + "," + serverPath);
                return "-1"; // Unauthorized
            } else if (conn.getResponseCode() == 500) {
                if(Logger.r) Logger.e("Exception", "ResponseCode=" + conn.getResponseCode() + "," + serverPath);
                return "-1"; // Unauthorized
            } else {
                if(Logger.r) Logger.e(EX, "ResponseCode=" + conn.getResponseCode() + "," + serverPath);
                return "-1";  // TimeOut or ServerError
            }

        } catch (Exception e) {
            if(Logger.r) Logger.e(EX, "DataOutputStream.IOException=" + e);
            return "-1";
        }
    }

    public static String post(String serverPath) {
        return post(serverPath, true);
    }


    public static String post(String serverPath, boolean keepAlive) {

        if (TextUtils.isEmpty(serverPath))
            return "-1";

        URL url;
        try {
            url = new URL(serverPath);
        } catch (MalformedURLException e) {
            if(Logger.r) Logger.ce("Exception", "MalformedURLException=" + e);
            return "-1";

        }
        HttpURLConnection conn;

        try {
            if(Logger.r) Logger.d(TAG, "openConnection");
            conn = (HttpURLConnection) url.openConnection();
            if (!keepAlive) {
                conn.setRequestProperty("Connection", "Close");
            }
        } catch (Exception e) {
            if(Logger.r) Logger.ce("Exception", "IOException=" + e);
            return "-1";
        }

        if (conn == null) return "-1";

        conn.setConnectTimeout(6 * 1000);
        try {
            // response OK
            if(Logger.r) Logger.d(TAG, "getResponseCode");

            conn.connect();

            if (conn.getResponseCode() == 200) {
                if(Logger.r) Logger.ce("Step", "conn.getResponseCode() == 200");
                return new String(readStream(conn.getInputStream()), "UTF-8");

            } else if (conn.getResponseCode() == 400) {
                if(Logger.r) Logger.ce("Exception", "ResponseCode=" + conn.getResponseCode() + "," + serverPath);
                return "-1"; // Unauthorized
            } else if (conn.getResponseCode() == 401) {
                if(Logger.r) Logger.ce("Exception", "ResponseCode=" + conn.getResponseCode() + "," + serverPath);
                return "-1"; // Unauthorized
            } else if (conn.getResponseCode() == 500) {
                if(Logger.r) Logger.ce("Exception", "ResponseCode=" + conn.getResponseCode() + "," + serverPath);
                return "-1"; // Unauthorized
            } else {
                if(Logger.r) Logger.ce(EX, "ResponseCode=" + conn.getResponseCode() + "," + serverPath);
                return "-1"; // TimeOut or ServerError 
            }

        } catch (Exception e) {
            if(Logger.r) Logger.ce(EX, "DataOutputStream.IOException=" + e);
            return "-1";
        } finally {
            conn.disconnect();
        }

    }

    // 
    public static byte[] readStream(InputStream inStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;
    }




}
