package cn.xender.core.ap;

/**
 * Created by liujian on 15/9/17.
 */
public class JoinApEvent {

    public JoinApEvent(boolean success,boolean wrongPassword,int type){
        this.success = success;
        this.wrongPassword = wrongPassword;
        this.type = type;
    }

    public JoinApEvent(boolean success,boolean wrongPassword,int type,boolean isNearbyConnection){
        this.success = success;
        this.wrongPassword = wrongPassword;
        this.type = type;
        this.isNearbyConnection = isNearbyConnection;
    }

    public static final int JOIN = 1;
    public static final int EXIT = 2;
    public static final int JOIN_LIMIT = 3;//一键换机的连接限制

    private int type;

    private boolean isNearbyConnection = false;

    public boolean isSuccess() {
        return success;
    }

    public boolean isWrongPassword() {
        return wrongPassword;
    }

    private boolean success;

    private boolean wrongPassword;

    public int getType() {
        return type;
    }

    public boolean isNearbyConnection() {
        return isNearbyConnection;
    }
}
