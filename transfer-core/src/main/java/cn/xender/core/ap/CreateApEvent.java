package cn.xender.core.ap;

import cn.xender.core.HttpServerStart;

/**
 * Created by liujian on 15/9/17.
 */
public class CreateApEvent {

    private static final int CREATE_ERROR = 1;
    private static final int OFF = 2;
    private static final int SAVED_25_CONFIG = 3;
    private static final int AP_ENABLED_25 = 4;
    private static final int CREATE_OK_BUT_NO_IP_ON25 = 5;
    private static final int CREATE_OK = 6;

    private int type = 0;

    private String apIp;

    private String ssid;

    private int requestCode;

    private String url;


//    public String getPassword() {
//        return password;
//    }

    private String password;


    private CreateApEvent(int type,int requestCode){
        this.type = type;
        this.requestCode = requestCode;
    }


    private CreateApEvent(int type, String ssid, String apIp,int requestCode,String password){
        this.type = type;
        this.ssid = ssid;
        this.apIp = apIp;
        this.requestCode = requestCode;
        this.password = password;

        this.url = HttpServerStart.formaQrCodeStringtUrl(ssid,password,apIp);

    }

    private CreateApEvent(int type, String ssid,int requestCode){
        this.type = type;
        this.ssid = ssid;
        this.requestCode = requestCode;
    }

    static CreateApEvent errorEvent(int requestCode){
        return new CreateApEvent(CREATE_ERROR,requestCode);
    }


    static CreateApEvent okEvent(String ssid, String apIp, int requestCode, String password){
        return new CreateApEvent(CREATE_OK, ssid, apIp,requestCode,password);
    }


    static CreateApEvent offEvent(int requestCode){
        return new CreateApEvent(OFF,requestCode);
    }

    static CreateApEvent save25ConfigEvent(int requestCode){
        return new CreateApEvent(CreateApEvent.SAVED_25_CONFIG,requestCode);
    }

    static CreateApEvent apEnabled25Event(int requestCode){
        return new CreateApEvent(CreateApEvent.AP_ENABLED_25,requestCode);
    }

    static CreateApEvent okButNoIpOn25(int requestCode){
        return new CreateApEvent(CreateApEvent.CREATE_OK_BUT_NO_IP_ON25,requestCode);
    }


    public int getRequestCode() {
        return requestCode;
    }

    public String getUrl() {
        return url;
    }

    public boolean isOk(){
        return type == CREATE_OK;
    }

    public boolean isOff(){
        return type == OFF;
    }


    public boolean isError(){
        return type == CREATE_ERROR;
    }


    public boolean isNeedUserManualOpen(){
        return type == SAVED_25_CONFIG;
    }

    public boolean isManualOpenSuccess(){
        return type == AP_ENABLED_25;
    }

    public boolean isOpendButWeCannotUseAndNeedRetry(){
        return type == CREATE_OK_BUT_NO_IP_ON25;
    }
}
