package cn.xender.core.ap;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by liujian on 15/9/17.
 */
public class ScanApEvent {

    private static final String TAG = "ScanApEvent";

    public static final int REQUEST_CODE_SCAN_COMMON_SSID = 10;
    public static final int RQQUEST_CODE_SANC_ASSIGNED_SSID = 11;

    private boolean hasAndroidOAp = false;

    public boolean isHasAndroidOAp() {
        return hasAndroidOAp;
    }

    public List<ScanResultItem> getAplist() {
        return aplist;
    }

    public boolean isScanStoped() {
        return scanStoped;
    }

    private List<ScanResultItem> aplist = new ArrayList<>();

    boolean scanStoped;

    private int requestCode;

    public ScanApEvent(List<ScanResultItem> r,boolean hasAndroidOAp,int requestCode) {
        aplist = r;
        this.hasAndroidOAp = hasAndroidOAp;
        this.requestCode= requestCode;
    }

    public ScanApEvent(boolean scanStoped,int requestCode){
        this.scanStoped = scanStoped;
        this.requestCode = requestCode;
    }

    public int getRequestCode() {
        return requestCode;
    }
}
