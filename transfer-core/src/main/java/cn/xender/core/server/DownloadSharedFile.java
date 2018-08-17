package cn.xender.core.server;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cn.xender.core.server.utils.ActionProtocol;
import cn.xender.core.server.utils.Encoder;
import cn.xender.core.log.Logger;
import cn.xender.core.server.NanoHTTPD.IHTTPSession;
import cn.xender.core.server.NanoHTTPD.Response;
import cn.xender.core.server.NanoHTTPD.Response.Status;
import cn.xender.core.server.utils.TaskCountCalcultor;

import static cn.xender.core.server.NanoHTTPD.Response.Status.NOT_FOUND;

public class DownloadSharedFile extends Base {

	public static final String URL_PATTERN = "/waiter/downloadSharedFile";

	private static Random random = new Random();

	public DownloadSharedFile(Context context) {
		super(context);
	}
	
	@Override
	public Response doResponse(Map<String, String> headers, IHTTPSession session, String uri) throws IOException {

		if (Logger.r) {
			Logger.d(TAG, "------DownloadSharedFile---------" + System.currentTimeMillis());
			Logger.d(TAG, "uri:" + uri);
		}
		Map<String, String> params = parseParams(session);

		String filePathName = params.get("fileurl");
		String taskid = params.get("taskid");

		if (Logger.r) {
			Logger.d(TAG, "filePathName=" + filePathName);
			Logger.d(TAG, "taskid:" + taskid);
		}

		String remote_ip = headers.get("http-client-ip");
		if (Logger.r) {
			Logger.d(TAG, "download file remote ip =" + remote_ip) ;
		}

		if(! new File(filePathName).exists()){
			TaskCountCalcultor.transferredOneFile(androidContext,remote_ip,filePathName,false);
			return new Response(NOT_FOUND,"text/plain","no file found");
		}
		return createRangeOrFullResponse(headers,filePathName,taskid,remote_ip);

	}

	private NanoHTTPD.Response createRangeOrFullResponse(Map<String, String> headers, String filePath, String taskid,String remote_ip) throws IOException {
        //1. 是否续传
        //1.1 带ranger header
        //1.2 版本匹配
        File file = new File(filePath);

        String range = headers.get("range");
		if (Logger.r) {
			Logger.d(TAG, "range:" + range) ;
		}

        long [] startAndEnd = new long[]{0, -1};

        parseRange(range, startAndEnd);

        long startFrom = startAndEnd[0];
        long endAt = startAndEnd[1];

        if(range != null && isInvalidRange(file, startFrom, endAt)){
			TaskCountCalcultor.transferredOneFile(androidContext,remote_ip,filePath,false);
            return new Response(NanoHTTPD.Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
        }

        String serverSideVersion = Integer.toHexString((file.getAbsolutePath()
                + file.lastModified() + "" + file.length()).hashCode());

        String clientSideVersion = headers.get("if-none-match");
		if (Logger.r) {
			Logger.d(TAG, "if-none-match:" + clientSideVersion) ;
		}
        if(range != null && isVersionMatchs(clientSideVersion, serverSideVersion)){
            return createRangeResponse(file, startFrom, endAt, serverSideVersion,remote_ip);
        }else{
            return createFullResponse(file,serverSideVersion,remote_ip);
        }
    }

	private Map<String,String> parseParams(NanoHTTPD.IHTTPSession session){
        Map<String, String> params = session.getParms();

        String filePathName = params.get("fileurl");

        if(filePathName == null){
            try {
                if (session.getMethod() == NanoHTTPD.Method.POST) {

                    HashMap<String, String> postData = new HashMap<String, String>();
                    session.parseBody(postData);
                }
            } catch (Exception e) {
            }
            params = session.getParms();
        }
        return params;
    }

    private void parseRange(String range, long[] startAndEnd) {
        if (range != null) {
            if (range.startsWith("bytes=")) {
                String srange = range.substring("bytes=".length());
                int minus = srange.indexOf('-');
                try {
                    if (minus > 0) {
						startAndEnd[0] = Long.parseLong(srange.substring(0, minus));
						startAndEnd[1] = Long.parseLong(srange.substring(minus + 1));
                    }
                } catch (Exception ignored) {
					if (Logger.r) {
						Logger.d(TAG,ignored.getMessage(), ignored);
					}
                }
			}
        }
    }

	private NanoHTTPD.Response createFullResponse(File file, String serverSideVersion,String remote_ip) throws IOException {
		NanoHTTPD.Response response;

		response = new MyFileResponse(androidContext,Status.OK, CONTENT_TYPE_STREAM_APP, file,remote_ip);

		response.addHeader("Content-Disposition", "attachment;filename=\"" + Encoder.encodeUri(file.getName()) + "\"");
		response.addHeader("Content-Length", String.valueOf(file.length()));
        response.addHeader("eTag",serverSideVersion);
		return response;
	}

	private NanoHTTPD.Response createRangeResponse(File file, long startFrom, long endAt, String serverSideVersion,String remote_ip) throws IOException {
		return new MyFileRangeResponse(androidContext,Response.Status.PARTIAL_CONTENT,CONTENT_TYPE_STREAM_APP,file,startFrom,endAt,serverSideVersion,remote_ip);
	}

	private boolean isVersionMatchs(String clientSideVersion, String serverSideVersion) {
		return TextUtils.equals(clientSideVersion,serverSideVersion);
	}


	private boolean isInvalidRange(File file, long startFrom, long endAt) {
		return startFrom < 0 || startFrom >= file.length() || (startFrom >= endAt && endAt > 0 );
	}




}
