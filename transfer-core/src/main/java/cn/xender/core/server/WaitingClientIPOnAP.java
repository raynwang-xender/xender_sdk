package cn.xender.core.server;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cn.xender.core.HttpServerStart;
import cn.xender.core.log.Logger;
import cn.xender.core.server.utils.ActionProtocol;
import cn.xender.core.server.utils.ShareMessage;


/* ------------------------------------------------------------ */

/**
 * Hello Servlet
 * 
 */
public class WaitingClientIPOnAP extends Base {

	public static final String URL_PATTERN = "/waiter/waitingClientIPOnAP";

	public WaitingClientIPOnAP(Context context) {
		super(context);
	}

	@Override
	public NanoHTTPD.Response doResponse(Map<String, String> headers, NanoHTTPD.IHTTPSession session, String uri) throws IOException {

		if (Logger.r) {
			Logger.c(TAG,
					"-----------WaitingClientIPOnAP------------------"
							+ System.currentTimeMillis());
		}

		Map<String, String> params = session.getParms();

		parseBodyWhenPost(session);


		String	clientip = params.get("clientIP");
		String	status = params.get("status");


		String remote_ip = headers.get("http-client-ip");

		if (Logger.r) {
			Logger.c(TAG, "session content is " + session);
			Logger.c(TAG, "some is change , client_IP=" + clientip + ",status=" + status);
		}


		if ("AP".equalsIgnoreCase(clientip) && "offline".endsWith(status)) {

			ClientManager.getInstance().clear();
			 
			return new NanoHTTPD.Response("1");
			
		}

		ConnectRequestData clientInfo = ConnectRequestData.fromJSON(clientip);

		boolean isOnlineStatus = "online".contains(status);

		if (isOnlineStatus) {

			ClientManager.getInstance().clientJoin(clientInfo);

			//将自己的信息变成jsonArray，将要发给对方。为了适配xender的握手协议
			String iplist = ClientManager.getInstance().getMyClientsInGroupJson(androidContext);

			String result = setClientInfoToClient(iplist,remote_ip);


			if(TextUtils.equals("1",result)){

				ActionProtocol.sendOnlineAction(androidContext);

				//将自己的apk发给对方
				sendMyApkInfoToClient(clientInfo);

			}

		} else if ("offline".endsWith(status)) {

			ClientManager.getInstance().clientExit(clientInfo);

			ActionProtocol.sendOfflineAction(androidContext);

		}



		return new NanoHTTPD.Response("1");
	}

	private void parseBodyWhenPost(NanoHTTPD.IHTTPSession session) {
		try {
			if (session.getMethod() == NanoHTTPD.Method.POST) {
				HashMap<String, String> postData = new HashMap<String, String>();
				session.parseBody(postData);
			}
		} catch (Exception e) {

		}
	}

	/**
	 * 把组内客户端信息发送给全部客户端
	 * @param iplist
	 */
	private String setClientInfoToClient(String iplist,String ip) {
		ConnectRequestData data = ClientManager.getInstance().getClientByIp(ip);

		if (Logger.r) {
			Logger.c(TAG, "send to client content:" + iplist);
		}

		return waitingAllIPOnWifi(ip, data.getPort() ,iplist);
	}


	private static String waitingAllIPOnWifi(String ip, int port, String clients) {
		String url = String.format(Locale.US, "http://%s:%d/waiter/waitingAllIPOnWifi?allclientIP=%s", ip, port , urlencode(clients));
		if (Logger.r) {
			Logger.c(TAG, "waitingAllIPOnWifi,url=" + url);
		}
		return NetWorker.post(url);
	}


	private static String urlencode(String clients) {
		try {

			return URLEncoder.encode(clients, "utf-8");
		} catch (Exception e) {
			throw new UnsupportedOperationException("utf-8 is not supported");
		}
	}


	private void sendMyApkInfoToClient(final ConnectRequestData client){


		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					//休息1秒钟再发送
					Thread.sleep(1000);
				} catch (InterruptedException e) {

				}

				try {

					//先验证一下对方有没有安装我的app
					boolean friendHasInstalledMe = HttpServerStart.friendHasInstalled(client.getIp(),client.getPort(),androidContext.getPackageName());

					String info = ShareMessage.createMyAppInfo(androidContext,friendHasInstalledMe);


					if (Logger.r) {
						Logger.c(TAG, "send my apk info to client ,info=" + info);
					}

					sendFileInfo(client.getIp(),client.getPort(),info);

				}catch (Exception e){
					if (Logger.r) {
						Logger.e(TAG, "send my apk info to client failure" ,e);
					}
				}



			}
		},"sendMyApkInfoToClient-thread").start();


	}


	public static void sendFileInfo(String ip, int port,String json) {
		String url = String.format(Locale.US, "http://%s:%d%s", ip, port, "/waiter/shareSomethingOnMessage");
		if (Logger.r) {
			Logger.i(TAG, "shareSomethingOnMessage,url=" + url);
		}
		NetWorker.getServerDatabyPost(url, json);
	}




}
