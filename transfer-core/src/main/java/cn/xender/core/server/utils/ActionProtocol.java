package cn.xender.core.server.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.widget.Toast;


public class ActionProtocol {

    private static final String FRIENDS_ONLINE_INFO_ABOUT_ACTION = "FRIENDS_ONLINE_INFO_ABOUT_ACTION";
    private static final String FRIENDS_OFFLINE_INFO_ABOUT_ACTION = "FRIENDS_OFFLINE_INFO_ABOUT_ACTION";
    private static final String TRANSFER_SUCCESS_ACTION = "TRANSFER_SUCCESS_ACTION";
    private static final String TRANSFER_FAILURE_ACTION = "TRANSFER_FAILURE_ACTION";

    public static void sendOnlineAction(Context context){

        Intent intent = new Intent(FRIENDS_ONLINE_INFO_ABOUT_ACTION);
        context.sendBroadcast(intent);

    }


    public static void sendOfflineAction(Context context){

        Intent intent = new Intent(FRIENDS_OFFLINE_INFO_ABOUT_ACTION);
        context.sendBroadcast(intent);

    }

    public static void sendTransferSuccessAction(Context context){

        Intent intent = new Intent(TRANSFER_SUCCESS_ACTION);
        context.sendBroadcast(intent);

    }


    public static void sendTransferFailureAction(Context context){
        Intent intent = new Intent(TRANSFER_FAILURE_ACTION);
        context.sendBroadcast(intent);
    }


    private ActionListener listener;

    public void setActionListener(ActionListener listener){
        this.listener = listener;
    }


    public void register(Activity activity){

        IntentFilter filter = new IntentFilter();
        filter.addAction(FRIENDS_ONLINE_INFO_ABOUT_ACTION);
        filter.addAction(FRIENDS_OFFLINE_INFO_ABOUT_ACTION);
        filter.addAction(TRANSFER_SUCCESS_ACTION);
        filter.addAction(TRANSFER_FAILURE_ACTION);

        initReceiver();

        try {

            if(receiver != null ){

                activity.registerReceiver(receiver,filter);

            }

        }catch (Exception e){

        }

    }


    public void unregister(Activity activity){
        try {
            if(receiver != null){

                activity.unregisterReceiver(receiver);

                receiver = null;

            }

        }catch (Exception e){

        }

    }


    private BroadcastReceiver receiver;

    private void initReceiver(){
        if(receiver == null){
            receiver = new BroadcastReceiver(){

                @Override
                public void onReceive(Context context, Intent intent) {


                    if(TextUtils.equals(intent.getAction(),FRIENDS_ONLINE_INFO_ABOUT_ACTION)){
                        if(listener != null){
                            listener.someoneOnline();
                        }
                    }else if(TextUtils.equals(intent.getAction(),FRIENDS_OFFLINE_INFO_ABOUT_ACTION)){
                        if(listener != null){
                            listener.someoneOffline();
                        }
                    }else if(TextUtils.equals(intent.getAction(),TRANSFER_SUCCESS_ACTION)){
                        if(listener != null){
                            listener.transferSuccess();
                        }
                    }else if(TextUtils.equals(intent.getAction(),TRANSFER_FAILURE_ACTION)){
                        if(listener != null){
                            listener.transferFailure();
                        }
                    }


                }
            };

        }

    }



}
