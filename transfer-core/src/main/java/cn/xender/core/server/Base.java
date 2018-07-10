package cn.xender.core.server;

import android.content.Context;

import java.io.IOException;
import java.util.Map;


public abstract class Base {
	public Context androidContext = null;
	public static final String EX = "ex";
	
	public static final String TAG = "base";

	public static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";
	public static final String CONTENT_TYPE_TEXT_JSON = "text/html;charset=utf-8"; 
	public static final String CONTENT_TYPE_STREAM = "application/octet-stream";
	public static final String CONTENT_TYPE_STREAM_APP = "application/vnd.android.package-archive;charset=utf-8";

	/*
	 * -------------------------给子类提供 andorid的context和
	 * ContentResolver-----------------------------------
	 */ 
	public Base(Context context){
		androidContext = context;
	}
	
	//用来做返回操作的
	@SuppressWarnings("unused")
	public abstract NanoHTTPD.Response doResponse(Map<String, String> headers, NanoHTTPD.IHTTPSession session, String uri) throws IOException;



}
