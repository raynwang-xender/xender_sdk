package cn.xender.core.server;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import cn.xender.core.server.utils.Encoder;
import cn.xender.core.log.Logger;


public class DownloadMe extends Base {

	public static final String URL_PATTERN = "1";

	public DownloadMe(Context context) {
		super(context);
	}

	@Override
	public NanoHTTPD.Response doResponse(Map<String, String> headers,
										 NanoHTTPD.IHTTPSession session, String uri) throws IOException {

		if (Logger.r) {
			Logger.d(TAG, "------DownloadSharedFile---------" + System.currentTimeMillis());
		}

		String filePathName =  androidContext.getApplicationInfo().sourceDir;
		File downfile = new File(filePathName);
		File file = null;
		
		
		if (!downfile.exists()|| downfile.length() <= 0) {

			if (Logger.r) {
				Logger.d(TAG, "NO_FILEORDIR_EXISTS or file length is 0");
			}

			return new NanoHTTPD.Response("-1");
		}
		
		if (downfile.isFile()) {
			file = downfile;
		}


		if (Logger.r) {
			Logger.e(TAG, "filePathName=" + filePathName);
		}

		if(file == null){
			return new NanoHTTPD.Response("-1");
		}

        InputStream is = new BufferedInputStream(new FileInputStream(file));
		NanoHTTPD.Response response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, CONTENT_TYPE_STREAM_APP, is);
		
		response.addHeader("Content-Disposition", "attachment;filename=\"" + Encoder.encodeUri(androidContext.getApplicationInfo().loadLabel(androidContext.getPackageManager())+".apk") + "\"");
		response.addHeader("Content-Length", String.valueOf(file.length()));

		return response;
	}

}
