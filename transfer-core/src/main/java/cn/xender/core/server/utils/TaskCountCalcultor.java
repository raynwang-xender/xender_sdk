package cn.xender.core.server.utils;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

public class TaskCountCalcultor {

    private static Set<String> pathSets;


    public static void transferredOneFile(Context context, String remote_ip, String filepath,boolean success){

        if(pathSets != null){

            pathSets.remove(filepath);

            if(success){
                ActionProtocol.sendTransferOneFileSuccessAction(context,remote_ip,filepath);
            }else{
                ActionProtocol.sendTransferOneFileFailureAction(context,filepath);
            }

            if(pathSets.size() == 0){
                ActionProtocol.sendTransferredAllAction(context,remote_ip);
            }
        }

    }

    public static void appendOneTask(String filepath){
        if(pathSets == null){
            pathSets = new HashSet<>();
        }
        pathSets.add(filepath);
    }

    public static void init(){
        pathSets = null;
    }

}
