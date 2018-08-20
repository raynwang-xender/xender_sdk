package cn.xender.transfer.permission;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.xender.transfer.R;


public class PermissionUtil {
    public static final int READ_EXTERNAL_STORAGE_PERMISSIONS = 1;
    public static final int CREATE_AP_WRITE_SETTING_PERMISSIONS = 2;
    public static final int ACCESS_COARSE_LOCATION_PERMISSIONS = 7;
    public static final int ACCESS_GPS_LOCATION_PERMISSIONS = 8;
    public static final int BACK_FROM_SETTING_PERMISSION = 9;


    public static boolean checkAllNeededPermission(Activity activity){
        if(!hasPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            return false;
        }
        if(!checkCreateApPermission(activity)){
            return false;
        }
        return true;
    }

    public static boolean checkCreateApPermission(Activity activity){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (!PermissionUtil.hasPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                return false;
            }else if (!PermissionUtil.getLocationEnabled(activity)) {
                return false;
            }else{
                return true;
            }
        }else{
            if (!PermissionUtil.writeSettingPermission(activity)) {
                return false;
            }else{
                return true;
            }
        }
    }

    public static boolean requestAllNeededPermission(Activity activity){
//        aaa(activity);
        if(!readExternalStorage(activity)){
            return false;
        }
        if(!requestCreateApPermission(activity)){
            return false;
        }
        return true;
    }

//    private void aaa(Activity activity){
//        System.out.println("---Rayn checkAllPermission");
//        if (!activity.shouldShowRequestPermissionRationale("android.permission.READ_EXTERNAL_STORAGE")
//                || !activity.shouldShowRequestPermissionRationale("android.permission.ACCESS_COARSE_LOCATION")) {
//            System.out.println("---Rayn lalala");
//            PermissionUtil.showSettingPermissionDlg(this);
//        }
//    }


    private static boolean requestCreateApPermission(Activity activity){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (!PermissionUtil.hasPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION_PERMISSIONS);

                return false;
            }else if (!PermissionUtil.getLocationEnabled(activity)) {

                Intent myAppSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                myAppSettings.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivityForResult(myAppSettings, ACCESS_GPS_LOCATION_PERMISSIONS);

                return false;
            }else{
                return true;
            }

        }else{
            if (!PermissionUtil.writeSettingPermission(activity)) {

                Intent writeSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
                if (writeSettings.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivityForResult(writeSettings, CREATE_AP_WRITE_SETTING_PERMISSIONS);
                }

                return false;
            }else{
                return true;
            }

        }

    }


    private static boolean readExternalStorage(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        List<String> permissionList = new ArrayList<>();
        if (!hasPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionList.size() < 1) {
            return true;
        }
        try {
            activity.requestPermissions(permissionList.toArray(new String[permissionList.size()]), READ_EXTERNAL_STORAGE_PERMISSIONS);
        }catch (Exception e){}
        return false;
    }


    private static boolean hasPermission(Context activity, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity.checkSelfPermission(permission) != PackageManager.PERMISSION_DENIED;
        }
        return true;
    }


    private static boolean writeSettingPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(activity)) {
                return false;
            }
        }
        return true;
    }


    private static boolean getLocationEnabled(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {
        }
        return gps_enabled || network_enabled;
    }

    /**
     * Rayn
     * 是否允许权限dialog
     */
    public static void showPermissionDlg(final Activity mContext){

        new AlertDialog.Builder(mContext)
                .setMessage("需要一些权限，连接身边好友")
                .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestAllNeededPermission(mContext);
                    }
                })
                .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContext.finish();
                    }
                }).show();
    }

    public static void showSettingPermissionDlg(final Activity mContext){

        new AlertDialog.Builder(mContext)
                .setMessage("需要到设置页面手动开启存储和位置权限")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
                        intent.setData(uri);
                        mContext.startActivityForResult(intent, PermissionUtil.BACK_FROM_SETTING_PERMISSION);
                    }
                })
                .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContext.finish();
                    }
                }).show();

    }
}
