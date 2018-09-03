package cn.xender.transfer;

public class ShareActivityContent {

    private ShareActivityContent(){}

    private static ShareActivityContent instance;

    public static ShareActivityContent getInstance(){
        if (null == instance){
            instance = new ShareActivityContent();
        }
        return instance;
    }

    /**
     * 上方标题
     */
    private String title = "Send File";
    /**
     * 二维码下方文字
     */
    private String invite = "Open Xender scan the QR code";
    /**
     * 电影缩略图地址
     */
    private String pic_url = "";
    /**
     * 电影名
     */
    private String film_name = "";
    /**
     * 电影大小
     */
    private String film_volume = "";
    /**
     * 右下角正在连接文案
     */
    private String connecting = "Connecting..";
    /**
     * 右下角正在传输文案
     */
    private String sending = "Sending File..";
    /**
     * 需要一些权限，连接身边好友
     * 允许
     * 退出
     */
    private String dlg_1_msg = "Need some permissions to connect with friends";
    private String dlg_1_positive = "Allow";
    private String dlg_1_negative = "Exit";
    /**
     * 确认退出吗？
     * 退出
     * 取消
     */
    private String dlg_2_msg = "Confirm Exit?";
    private String dlg_2_positive = "Exit";
    private String dlg_2_negative = "Cancel";
    /**
     * 7.1需要手动开启热点
     * 设置
     * 退出
     */
    private String dlg_3_msg = "System 7.1 needs to turn on hotspot manually";
    private String dlg_3_positive = "Settings";
    private String dlg_3_negative = "Exit";
    /**
     * **这个是拒绝权限，并且勾选了不再出现，弹出的dlg，需进入系统打开权限
     * 需要到系统设置手动开启存储和位置权限
     * 设置
     * 退出
     */
    private String dlg_4_msg = "Enter system setting, open storage and location permission";
    private String dlg_4_positive = "Settings";
    private String dlg_4_negative = "Exit";


    public String getDlg_4_msg() {
        return dlg_4_msg;
    }

    public void setDlg_4_msg(String dlg_4_msg) {
        this.dlg_4_msg = dlg_4_msg;
    }

    public String getDlg_4_positive() {
        return dlg_4_positive;
    }

    public void setDlg_4_positive(String dlg_4_positive) {
        this.dlg_4_positive = dlg_4_positive;
    }

    public String getDlg_4_negative() {
        return dlg_4_negative;
    }

    public void setDlg_4_negative(String dlg_4_negative) {
        this.dlg_4_negative = dlg_4_negative;
    }

    public String getDlg_1_msg() {
        return dlg_1_msg;
    }

    public void setDlg_1_msg(String dlg_1_msg) {
        this.dlg_1_msg = dlg_1_msg;
    }

    public String getDlg_1_positive() {
        return dlg_1_positive;
    }

    public void setDlg_1_positive(String dlg_1_positive) {
        this.dlg_1_positive = dlg_1_positive;
    }

    public String getDlg_1_negative() {
        return dlg_1_negative;
    }

    public void setDlg_1_negative(String dlg_1_negative) {
        this.dlg_1_negative = dlg_1_negative;
    }

    public String getDlg_2_msg() {
        return dlg_2_msg;
    }

    public void setDlg_2_msg(String dlg_2_msg) {
        this.dlg_2_msg = dlg_2_msg;
    }

    public String getDlg_2_positive() {
        return dlg_2_positive;
    }

    public void setDlg_2_positive(String dlg_2_positive) {
        this.dlg_2_positive = dlg_2_positive;
    }

    public String getDlg_2_negative() {
        return dlg_2_negative;
    }

    public void setDlg_2_negative(String dlg_2_negative) {
        this.dlg_2_negative = dlg_2_negative;
    }

    public String getDlg_3_msg() {
        return dlg_3_msg;
    }

    public void setDlg_3_msg(String dlg_3_msg) {
        this.dlg_3_msg = dlg_3_msg;
    }

    public String getDlg_3_positive() {
        return dlg_3_positive;
    }

    public void setDlg_3_positive(String dlg_3_positive) {
        this.dlg_3_positive = dlg_3_positive;
    }

    public String getDlg_3_negative() {
        return dlg_3_negative;
    }

    public void setDlg_3_negative(String dlg_3_negative) {
        this.dlg_3_negative = dlg_3_negative;
    }


    public String getInvite() {
        return invite;
    }

    public void setInvite(String invite) {
        this.invite = invite;
    }

    public String getPic_url() {
        return pic_url;
    }

    public void setPic_url(String pic_url) {
        this.pic_url = pic_url;
    }

    public String getFilm_name() {
        return film_name;
    }

    public void setFilm_name(String film_name) {
        this.film_name = film_name;
    }

    public String getFilm_volume() {
        return film_volume;
    }

    public void setFilm_volume(String film_volume) {
        this.film_volume = film_volume;
    }

    public String getConnecting() {
        return connecting;
    }

    public void setConnecting(String connecting) {
        this.connecting = connecting;
    }

    public String getSending() {
        return sending;
    }

    public void setSending(String sending) {
        this.sending = sending;
    }








    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }


}
