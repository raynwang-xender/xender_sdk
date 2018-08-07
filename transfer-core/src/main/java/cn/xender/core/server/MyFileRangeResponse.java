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
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import cn.xender.core.log.Logger;
import cn.xender.core.server.utils.ActionProtocol;

/**
 * HTTP response. Return one of these from serve().
 */
public class MyFileRangeResponse extends NanoHTTPD.Response {


    /**
     * custom,current send file,need delete it after sent if needed;
     */
    private File file;

    private long startFrom;


    private long endAt;

    private String eTag;

    private Context androidContext;

    private String remote_ip;

    /**
     * Basic constructor.
     */
    public MyFileRangeResponse(Context context,IStatus status, String mimeType, File file, long startFrom, long endAt,String serverSideVersion,String remote_ip) throws IOException {
        super(status, mimeType, new BufferedInputStream(new FileInputStream(file)));
        this.file = file;
        this.startFrom = startFrom;
        this.endAt = endAt;
        this.eTag = serverSideVersion;
        this.remote_ip = remote_ip;

        this.androidContext = context;

    }


    private boolean needDel(File file) {
        String name = file.getName();
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        if (name.endsWith(".contact")) {
            return true;
        }
        if (name.endsWith(".csv")) {
            return true;
        }
        if (name.endsWith(").txt")) {
            return true;
        }
        return false;

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
//            int pending = getData() != null ? getData().available() : 0;
                long bytesToSend = 0;
                if (endAt >= 0) {
                    bytesToSend = endAt - startFrom;
                } else {
                    bytesToSend = file.length() - startFrom;
                }
                sendContentLengthHeaderIfNotAlreadyPresent(pw, header, bytesToSend);
                sendContentRangeHeader(pw, startFrom, endAt, file.length());
                sendETagHeader(pw);


//            sendContentLengthHeaderIfNotAlreadyPresent(pw, header, length);


                pw.print("\r\n");
                pw.flush();
                sendAsFixedLength(outputStream, bytesToSend);
            }
            outputStream.flush();
            NanoHTTPD.safeClose(getData());

        } catch (Exception ioe) {
            if(Logger.r) Logger.e("send_file","send range file error",ioe);

            ActionProtocol.sendTransferFailureAction(androidContext);
        }finally {
        }

    }


    private void sendContentRangeHeader(PrintWriter pw, long startFrom, long endAt, long fileLength) {
        pw.print("Accept-Ranges: bytes\r\n");
        if (endAt >= 0) {
            pw.print("Content-Range: bytes " + startFrom + "-" + endAt + "/" + fileLength + "\r\n");
        } else {
            pw.print("Content-Range: bytes " + startFrom + "-" + "/" + fileLength + "\r\n");
        }
    }

    private void sendETagHeader(PrintWriter pw){
        pw.print("eTag: "+eTag + "\r\n");
    }

    protected void sendAsFixedLength(OutputStream outputStream, long bytesToSend)
            throws IOException {

        RandomAccessFile raf = new RandomAccessFile(file, "r");
        data = Channels.newInputStream(raf.getChannel().position(startFrom));

        if (getRequestMethod() != NanoHTTPD.Method.HEAD && data != null) {
            int BUFFER_SIZE = 16 * 1024;
            byte[] buff = new byte[BUFFER_SIZE];
            int count = 0;
            long byteleft = bytesToSend;


            while (byteleft != 0 && ((count = data.read(buff, 0, (int) Math.min(BUFFER_SIZE, byteleft))) != -1)) {
                byteleft = byteleft - count;
                outputStream.write(buff, 0, count);

            }


            ActionProtocol.sendTransferSuccessAction(androidContext,remote_ip);
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
            outputStream.write(String.format(Locale.US, "%x\r\n", read).getBytes());
            outputStream.write(buff, 0, read);
            outputStream.write(CRLF);
            if(Logger.r) Logger.d("http", "-----file read one time end------");


        }
        outputStream.write(String.format("0\r\n\r\n").getBytes());

        ActionProtocol.sendTransferSuccessAction(androidContext,remote_ip);
    }

}