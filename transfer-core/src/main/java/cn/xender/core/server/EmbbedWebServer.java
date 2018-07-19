package cn.xender.core.server;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.xender.core.ap.utils.WifiAPUtil;
import cn.xender.core.log.Logger;


@SuppressLint("DefaultLocale")
public class EmbbedWebServer extends NanoHTTPD {
    private static final String TAG = EmbbedWebServer.class.getSimpleName();

    /**
     * Common mime type for dynamic content: binary
     */
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";

    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {

        {
            put("css", "text/css");
            put("htm", "text/html");
            put("html", "text/html");
            put("xml", "text/xml");
            put("java", "text/x-java-source, text/java");
            put("md", "text/plain");
            put("txt", "text/plain");
            put("asc", "text/plain");
            put("gif", "image/gif");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
            put("png", "image/png");
            put("mp3", "audio/mpeg");
            put("m3u", "audio/mpeg-url");
            put("mp4", "video/mp4");
            put("ogv", "video/ogg");
            put("flv", "video/x-flv");
            put("mov", "video/quicktime");
            put("swf", "application/x-shockwave-flash");
            put("js", "application/javascript");
            put("pdf", "application/pdf");
            put("doc", "application/msword");
            put("ogg", "application/x-ogg");
            put("zip", "application/octet-stream");
            put("exe", "application/octet-stream");
            put("class", "application/octet-stream");
        }
    };

    private Context mContext;

    private final boolean quiet;

    private String directUri = UUID.randomUUID().toString();

    private String token = null;

    private int port;

    public String createNewDirectUrl() {


        if(Logger.r) Logger.d(TAG, "------------------createNewDirectUrl---------------------------------");
        this.directUri = "/";
        token = null;
        return uriToUrl(directUri);
    }

    private String uriToUrl(String uri) {
        return "http://" + WifiAPUtil.getIpOnWifiAndAP(mContext) + (port == 80 ? "" : ":" + port) + uri;
    }

    public EmbbedWebServer(Context context, String host, int port, String tempFilePath) {
        super(null, port, tempFilePath);
        this.mContext = context;
        this.port = port;
        this.quiet = true;
        this.init();
    }

    public EmbbedWebServer(String host, int port, List<File> wwwroots, boolean quiet) {
        super(host, port, "");
        this.quiet = quiet;
        this.port = port;
        this.init();
    }

    public static File getTempFile(Context context, String prefix) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(prefix, "", context.getCacheDir());
        } catch (IOException e) {
            try {
                tmpFile = getExternalCacheDir(context);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return tmpFile;
    }

    private static File getExternalCacheDir(Context context) {
        File file = context.getExternalCacheDir();

        if (file != null) return file;

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * Used to initialize and customize the server.
     */
    public void init() {
        setAsyncRunner(new AsyncRunner() {

            ExecutorService executor = Executors.newFixedThreadPool(10);

            @Override
            public void exec(Runnable code) {
                executor.execute(code);
            }
        });
    }


    /**
     * Starts as a standalone file server and waits for Enter.
     */
    public static void main(String[] args) {
        // Defaults
        int port = 8080;

        String host = "127.0.0.1";

        List<File> rootDirs = new ArrayList<File>();

        boolean quiet = true;

        EmbbedWebServer server = new EmbbedWebServer(host, port, rootDirs,
                quiet);
        String url = server.createNewDirectUrl();
        System.out.println("direct url:" + url);
        ServerRunner.executeInstance(server);
    }

    public Response serve(IHTTPSession session) {
        Map<String, String> header = session.getHeaders();
        Map<String, String> parms = session.getParms();

        String uri = session.getUri();
//		if(Logger.r) Logger.e("serve", "session header = " + header.get("accept"));
        if (!quiet) {
            if(Logger.r) Logger.d(TAG, session.getMethod() + " '" + uri + "' ");

            Iterator<String> e = header.keySet().iterator();
            while (e.hasNext()) {
                String value = e.next();
                if(Logger.r) Logger.d(TAG, "  HDR: '" + value + "' = '"
                        + header.get(value) + "'");
            }
            e = parms.keySet().iterator();
            while (e.hasNext()) {
                String value = e.next();
                if(Logger.r) Logger.d(TAG, "  PRM: '" + value + "' = '"
                        + parms.get(value) + "'");
            }
        }
        try {
            return respondAllRequest(Collections.unmodifiableMap(header), session, uri);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("error");
        }
    }

    /**
     * 所有的请求将在这里进行统一的管理
     *
     * @throws IOException
     */
    private Response respondAllRequest(Map<String, String> headers, IHTTPSession session, String uri) throws IOException {

        if (uri == null) return new Response(Response.Status.BAD_REQUEST, "text/plain", "bad request");

        if(Logger.r) Logger.e(TAG, "respondAllRequest uri=" + uri);

        return respondPhoneConnectRequest(headers, session, uri);
    }


    /**
     * 手机连接时在这里进行管理
     *
     * @throws IOException
     */
    private Response respondPhoneConnectRequest(Map<String, String> headers, IHTTPSession session, String uri) throws IOException {

        // AJAX 跨域时不能读写cookie，这里不再校验cookie.
        // String clientCookie = session.getCookies().read("id");
        if (!TextUtils.isEmpty(uri)) {
            // Remove URL arguments

            uri = uri.trim().replace(File.separatorChar, '/');
            if (uri.indexOf('?') >= 0) {
                uri = uri.substring(0, uri.indexOf('?'));
            }

            if(uri.equals(WaitingClientIPOnAP.URL_PATTERN)){
                return new WaitingClientIPOnAP(mContext).doResponse(headers,session,uri);
            }


            if(uri.equals(DownloadSharedFile.URL_PATTERN)){
                return new DownloadSharedFile(mContext).doResponse(headers,session,uri);
            }


            return new Response(Response.Status.BAD_REQUEST, "text/plain", "bad request");
        } else {
            return getForbiddenResponse("Access is Forbidden");
        }

    }

    protected Response getForbiddenResponse(String s) {
        return createResponse(Response.Status.FORBIDDEN,
                NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: " + s);
    }

    // Get MIME type from file name extension, if possible
    @SuppressLint("DefaultLocale")
    private String getMimeTypeForFile(String uri) {
        int dot = uri.lastIndexOf('.');
        String mime = null;
        if (dot >= 0) {
            mime = MIME_TYPES.get(uri.substring(dot + 1).toLowerCase());
        }
        return mime == null ? MIME_DEFAULT_BINARY : mime;
    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType,
                                    String message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }
}
