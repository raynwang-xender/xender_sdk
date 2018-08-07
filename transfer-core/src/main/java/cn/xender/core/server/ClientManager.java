package cn.xender.core.server;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;

import java.util.Collection;
import java.util.LinkedHashMap;

import cn.xender.core.log.Logger;

/**
 * Created by liujian on 15/9/28.
 */
public class ClientManager {

    public static final String TRANSFER_SDK_CHANNEL = "toutiao_hello_20180807";

    private static ClientManager instance = new ClientManager();

    LinkedHashMap<String, ConnectRequestData> others = new LinkedHashMap<>();


    public static ClientManager getInstance(){
        return instance;
    }

    private ClientManager(){

    }


    public synchronized void clientJoin(ConnectRequestData clientInfo) {
        if(clientInfo == null || clientInfo.getImei() == null) return;
        others.put(clientInfo.getImei(), clientInfo);
    }

    public synchronized void clientExit(ConnectRequestData clientInfo) {
        if(clientInfo == null || clientInfo.getImei() == null) return;
        others.remove(clientInfo.getImei());
    }


    public synchronized void clear(){
        if(others.size() > 0){
            others.clear();
            if(Logger.r) Logger.d("ClientManager", "clear others");
        }
    }


    public synchronized String getMyClientsInGroupJson(Context context){
        ConnectRequestData apRequest = ConnectRequestData.getMyConnectRequestData(context);

        JSONArray array = new JSONArray();
        array.put(apRequest.toJsonObj());

        return array.toString();
    }


    public synchronized ConnectRequestData getClientByIp(String ip){
        Collection<ConnectRequestData> clients = others.values();
        for(ConnectRequestData item:clients){
            if(TextUtils.equals(ip,item.getIp())){
                return item;
            }
        }
        return null;
    }



}
