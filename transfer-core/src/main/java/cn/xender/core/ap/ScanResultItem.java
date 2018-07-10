package cn.xender.core.ap;

public class ScanResultItem {

    private boolean mClickable = true;
    private String SSID;
    private String ssid_nickname;
    private String BSSID;
    private String profix;//新加的前缀，用来做标识。
    private String keyMgmt;

    private String qr_scan_action_type;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;

    public String getQr_scan_action_type() {
        return qr_scan_action_type;
    }

    public void setQr_scan_action_type(String qr_scan_action_type) {
        this.qr_scan_action_type = qr_scan_action_type;
    }

    public String sessionKey;//没用

    public ScanResultItem(){

    }

    public ScanResultItem(String ssid, String nickname, String bssid, String profix) {
        this.SSID = ssid;
        this.BSSID = bssid;
        this.ssid_nickname = nickname;
        this.profix = profix;
    }

    public String getKeyMgmt() {
        return keyMgmt;
    }

    public void setKeyMgmt(String keyMgmt) {
        this.keyMgmt = keyMgmt;
    }

    public boolean isClickable() {
        return mClickable;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String sSID) {
        SSID = sSID;
    }

    public String getSsid_nickname() {
        return ssid_nickname;
    }

    public void setSsid_nickname(String ssid_nickname) {
        this.ssid_nickname = ssid_nickname;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String bSSID) {
        BSSID = bSSID;
    }

    public String getProfix() {
        return profix;
    }

    public void setProfix(String profix) {
        this.profix = profix;
    }

}
