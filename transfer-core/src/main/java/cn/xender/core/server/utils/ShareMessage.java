package cn.xender.core.server.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.xender.core.ap.utils.WifiAPUtil;
import cn.xender.core.server.ConnectRequestData;

/**
 * 文件信息数据结构
 * Created by liujian on 15/10/12.
 */
@SuppressWarnings("unused")
public class ShareMessage {

    private String category = "";

    private String file_path = "";

    private String res_name = "";

    private long file_size = 0;

    private String ip_addr;

    private String spirit_name;

    private String imei;

    private String package_name;

    private int version;

    private long create_time = 0;

    private String brand;

    private String model;

    private String taskid;


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFile_path() {
        return file_path;
    }

    public String getRes_name() {
        return res_name;
    }

    public long getFile_size() {
        return file_size;
    }

    public String getIp_addr() {
        return ip_addr;
    }

    public void setIp_addr(String ip_addr) {
        this.ip_addr = ip_addr;
    }

    public String getSpirit_name() {
        return spirit_name;
    }

    public void setSpirit_name(String spirit_name) {
        this.spirit_name = spirit_name;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public long getCreate_time() {
        return create_time;
    }

    public String getTaskid() {
        return taskid;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public void setRes_name(String res_name) {
        this.res_name = res_name;
    }

    public void setFile_size(long file_size) {
        this.file_size = file_size;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }


    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }


    public JSONObject toJsonObject(){

        JSONObject object = new JSONObject();

        try {

            object.put("taskid",taskid);
            object.put("res_name",res_name);
            object.put("category",category);
            object.put("imei",imei);
            object.put("brand",brand);
            object.put("model",model);
            object.put("file_path",file_path);
            object.put("file_size",file_size);
            object.put("create_time",create_time);
            object.put("ip_addr",ip_addr);
            object.put("spirit_name",spirit_name);
            object.put("package_name",package_name);
            object.put("version",version);



        }catch (Exception e){

        }

        return object;

    }


    public static String createMyAppInfo(Context context){

        ShareMessage message = ShareMessage.create(context);


        JSONArray array = new JSONArray();

        if(message != null){

            array.put(message.toJsonObject());

        }

        List<ShareMessage> files = createNeedShareFilesInfo(context);

        if(files != null && !files.isEmpty()){
            for(ShareMessage item:files){

                array.put(item.toJsonObject());
            }
        }

        return array.toString();

    }

    private static ShareMessage create(Context context){

        try {
            PackageManager packageManager = context.getPackageManager();

            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(),0);

            ApplicationInfo applicationInfo = packageInfo.applicationInfo;

            ShareMessage msg = new ShareMessage();
            msg.taskid = UUID.randomUUID().toString().replace("-", "");
            msg.res_name =applicationInfo.loadLabel(packageManager).toString() + ".apk";
            msg.category = "app";
            msg.imei = ConnectRequestData.getAndroidId(context);
            msg.brand = Build.BRAND;
            msg.model = Build.MODEL;
            msg.file_path = applicationInfo.sourceDir;
            File file = new File(msg.file_path);
            msg.file_size = file.length();
            msg.create_time = file.lastModified();
            msg.ip_addr = WifiAPUtil.getIpOnWifiAndAP(context);
            msg.spirit_name = Build.MODEL; //在这里是固定的
            msg.package_name = packageInfo.packageName;
            msg.version = packageInfo.versionCode;

            return msg;
        }catch (Exception e){

        }

        return null;
    }

    private static List<ShareMessage> createNeedShareFilesInfo(Context context){

        if(NeedSharedFiles.getNeedShared() == null || NeedSharedFiles.getNeedShared().length == 0){
            return null;
        }

        List<ShareMessage> list = new ArrayList<>();

        try {
            String[] files = NeedSharedFiles.getNeedShared();

            String cate = NeedSharedFiles.getCate();

            for(int i = 0; i < files.length;i++){

                String filepath = files[i];

                //
                if(TextUtils.isEmpty(filepath) ){
                    continue;
                }

                File needShared = new File(filepath);

                if(!needShared.exists()){
                    continue;
                }

                ShareMessage msg = new ShareMessage();
                msg.taskid = UUID.randomUUID().toString().replace("-", "");
                msg.res_name = needShared.getName();
                msg.category = cate;
                msg.imei = ConnectRequestData.getAndroidId(context);
                msg.brand = Build.BRAND;
                msg.model = Build.MODEL;
                msg.file_path = needShared.getAbsolutePath();
                msg.file_size = needShared.length();
                msg.create_time = needShared.lastModified();
                msg.ip_addr = WifiAPUtil.getIpOnWifiAndAP(context);
                msg.spirit_name = Build.MODEL; //在这里是固定的

                list.add(msg);
            }
        }catch (Exception e){

        }

        return list;
    }

}
