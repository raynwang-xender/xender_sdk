package cn.xender.core.server;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import cn.xender.core.log.Logger;


public class NetWorker {
    public static final String TAG = "NetWorker";
    public static final String EX = "ex";


    public static String getServerDataByPostImage(Context context, String serverPath, String filePathName) {
        if(Logger.r) Logger.d(TAG, "serverPath=" + serverPath);
        if(Logger.r) Logger.d(TAG, "filePathName=" + filePathName);

        if (TextUtils.isEmpty(serverPath) || TextUtils.isEmpty(filePathName))
            return "-1";

        String fileShortName = filePathName.substring(filePathName.lastIndexOf("/") + 1);

        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";

        if(Logger.r) Logger.d(TAG, "-@fileShortName----fileShortName =" + fileShortName);
        HttpURLConnection httpURLConnection = null;
        try {

            URL url = new URL(serverPath);

            httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            //大文件设置
            httpURLConnection.setChunkedStreamingMode(0);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            if(Logger.r) Logger.d(TAG, "httpURLConnection=" + httpURLConnection);

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"fileupload\"; filename=\"" + URLEncoder.encode(fileShortName, "utf-8") + "\"" + end);
            dos.writeBytes(end);

            //
            FileInputStream fis = new FileInputStream(filePathName);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);

            }
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();

            dos.close();
            fis.close();

            if (httpURLConnection.getResponseCode() == 200) {
                if(Logger.r) Logger.d(TAG, "getResponseCode= 200 ");
                return new String(readStream(httpURLConnection.getInputStream()), "UTF-8");

            } else {
                if(Logger.r) Logger.e(EX, "ResponseCode=" + httpURLConnection.getResponseCode() + "," + serverPath);
                if(Logger.r) Logger.e(EX, "serverPath=" + serverPath);
                if(Logger.r) Logger.e(EX, "filePathName=" + filePathName);

                return "-1";
            }

        } catch (Exception e) {
            if(Logger.r) Logger.e(EX, "5Exception=" + e);
            return "-1";
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

    }

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

    public static String getHasOfferData(String url) {
        String result = "";
        BufferedReader in = null;
        try {
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            Map<String, List<String>> map = connection.getHeaderFields();
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static String postDataToServr(String serverPath, byte[] data) {

        if (TextUtils.isEmpty(serverPath) || TextUtils.isEmpty(new String(data)))
            return "-1";
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
        conn.setRequestProperty("Content-Type", "application/jsonrequest;charset=utf-8");
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

}
