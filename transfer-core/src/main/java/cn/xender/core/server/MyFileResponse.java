package cn.xender.core.server;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import cn.xender.core.log.Logger;
import cn.xender.core.server.utils.ActionProtocol;
import cn.xender.core.server.utils.TaskCountCalcultor;

/**
 * HTTP response. Return one of these from serve().
 */
public class MyFileResponse extends NanoHTTPD.Response {



    private Context androidContext;

    private String remote_ip;

    private String filepath;

    /**
     * Basic constructor.
     */
    public MyFileResponse(Context context, IStatus status, String mimeType, File file,String remote_ip) throws IOException{
        super(status,mimeType,new BufferedInputStream(new FileInputStream(file)));
        this.androidContext = context;
        this.remote_ip = remote_ip;
        this.filepath = file.getAbsolutePath();
    }


    /**
     * Sends given response to the socket.
     */
    protected void send(OutputStream outputStream) {
        String mime = getMimeType();
        SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            if (getStatus() == null) {
                throw new Error("sendResponse(): Status can't be null.");
            }
            PrintWriter pw = new PrintWriter(outputStream);
            pw.print("HTTP/1.1 " + getStatus().getDescription() + " \r\n");

            if (mime != null) {
                pw.print("Content-Type: " + mime + "\r\n");
            }

            if (header == null || header.get("Date") == null) {
                pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
            }

            if (header != null) {
                for (String key : header.keySet()) {
                    String value = header.get(key);
                    pw.print(key + ": " + value + "\r\n");
                }
            }

            sendConnectionHeaderIfNotAlreadyPresent(pw, header);

            if (getRequestMethod() != NanoHTTPD.Method.HEAD && isChunkedTransfer()) {
                sendAsChunked(outputStream, pw);
            } else {
                int pending = getData() != null ? getData().available() : 0;
                sendContentLengthHeaderIfNotAlreadyPresent(pw, header, pending);
                pw.print("\r\n");
                pw.flush();
                sendAsFixedLength(outputStream, pending);
            }
            outputStream.flush();
            NanoHTTPD.safeClose(getData());

        } catch (Exception ioe) {
            if(Logger.r) Logger.e("send_file","send file error",ioe);
            TaskCountCalcultor.transferredOneFile(androidContext,remote_ip,filepath,false);
        }finally {
        }
    }

    protected void sendAsFixedLength(OutputStream outputStream, int pending)
            throws IOException {

        if (getRequestMethod() != NanoHTTPD.Method.HEAD && data != null) {
            int BUFFER_SIZE = 16 * 1024;
            byte[] buff = new byte[BUFFER_SIZE];
            int count = 0;
            while( (count = data.read(buff)) != -1){
                outputStream.write(buff, 0, count);
            }

            TaskCountCalcultor.transferredOneFile(androidContext,remote_ip,filepath,true);
        }
    }


    protected void sendAsChunked(OutputStream outputStream, PrintWriter pw) throws IOException {
        pw.print("Transfer-Encoding: chunked\r\n");
        pw.print("\r\n");
        pw.flush();
        int BUFFER_SIZE = 16 * 1024;
        byte[] CRLF = "\r\n".getBytes();
        byte[] buff = new byte[BUFFER_SIZE];
        int read = 0;



        long total = data.available();
        if(Logger.r) Logger.d("http", "-----total file size------" + total);
        while ((read = data.read(buff)) != -1) {
            if(Logger.r) Logger.d("http", "-----file read one time start------");
            outputStream.write(String.format(Locale.US,"%x\r\n", read).getBytes());
            outputStream.write(buff, 0, read);
            outputStream.write(CRLF);
            if(Logger.r) Logger.d("http", "-----file read one time end------");


        }
        outputStream.write(String.format("0\r\n\r\n").getBytes());

        TaskCountCalcultor.transferredOneFile(androidContext,remote_ip,filepath,true);
    }

}