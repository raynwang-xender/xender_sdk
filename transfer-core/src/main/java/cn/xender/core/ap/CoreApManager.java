package cn.xender.core.ap;

import android.content.Context;
import android.os.Build;

import cn.xender.core.HttpServerStart;
import cn.xender.core.ap.utils.BluetoothUtil;
import cn.xender.core.ap.utils.FilterManager;

public class CoreApManager implements ICoreApManager {

    private ScanApWorker scanApWorker;
    private JoinApWorker joinApWorker;
    private ICreateApWorker createApWorker;
    private Context applicationContext;
    private HttpServerStart httpServerStart;

    private static final int DEFAULT_CREATE_TIMEOUT = 30000;


    private static CoreApManager instance = new CoreApManager();

    public static CoreApManager getInstance() {
        return instance;
    }

    public void initApplicationContext(Context context){
        this.applicationContext = context;

        getCreateApWorker();
    }

    public void setSSIDFilterForRestore(SSIDFilter SSIDFilter){
        FilterManager.setSSIDFilter(SSIDFilter);
    }

    @Override
    public void createAp(int requestCode, CoreCreateApCallback callback) {
        createAp("","",DEFAULT_CREATE_TIMEOUT,requestCode,callback);
    }

    @Override
    public void createAp(String ssid, String password, long timeout, int requestCode, final CoreCreateApCallback callback) {

        checkContext();

        cancelDiscoveryBluetooth();

        getCreateApWorker().createAp(ssid, password, timeout, requestCode, new CoreCreateApCallback() {
            @Override
            public void callback(CreateApEvent result) {

                if(callback != null){
                    callback.callback(result);
                }


            }
        });

        getHttpServerStart().bindHttpService();

    }


    @Override
    public int getCreateRequestCode() {

        checkContext();

        return getCreateApWorker().getCurrentRequestCode();
    }

    @Override
    public void retryCreateAp(String ssid, String password, long timeout, int requestCode, final CoreCreateApCallback callback) {

        checkContext();

        getCreateApWorker().retryCreateAp(ssid,password, timeout,requestCode,new CoreCreateApCallback() {
            @Override
            public void callback(CreateApEvent result) {

                if(callback != null){
                    callback.callback(result);
                }

            }
        });

    }

    @Override
    public void shutdownAp() {
        checkContext();

        getCreateApWorker().closeAp();

        getHttpServerStart().unbindHttpService();
    }

    @Override
    public boolean isApEnabled() {
        checkContext();

        return getCreateApWorker().isApEnabled();
    }

    @Override
    public void createFailed() {
        checkContext();

        getCreateApWorker().createFailed();

        getHttpServerStart().unbindHttpService();
    }

    @Override
    public String getApName() {
        checkContext();

        return getCreateApWorker().getApName();
    }

    @Override
    public String getApPassword() {

        checkContext();

        return getCreateApWorker().getApPassword();
    }

    @Override
    public void startScanAp(SSIDFilter f, SSIDDecoder decoder, long timeout, CoreScanApCallback callback,int requestCode) {

        checkContext();

        getScanApWorker().startScan(f,decoder,timeout,callback,requestCode);
    }

    @Override
    public void stopScanAp() {

        checkContext();

        getScanApWorker().stopScan();
    }

    @Override
    public void joinAp(ScanResultItem item, String password, String static_ip, long timeout, CoreJoinApCallback callback) {

        checkContext();

        if(item != null){
            getJoinApWorker().startJoin(item.getSSID(),item.getBSSID(),password,static_ip,timeout,callback);
        }
    }


    private void cancelDiscoveryBluetooth(){

        BluetoothUtil.cancelDiscovery(applicationContext);
    }


    private void checkContext(){
        if(applicationContext == null){
            throw new RuntimeException("please init application context first");
        }
    }

    private ICreateApWorker getCreateApWorker(){
        if(createApWorker == null){

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                createApWorker = new AndroidOCreateApWorker(applicationContext);
            }else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1){
                createApWorker = new AndroidN_MR1CreateApWorker(applicationContext);
            }else {
                createApWorker = new AndroidCommonCreateApWorker(applicationContext);
            }

        }

        return createApWorker;
    }

    private ScanApWorker getScanApWorker() {

        if(scanApWorker==null){
            scanApWorker = new ScanApWorker(applicationContext);
        }

        return scanApWorker;
    }


    private JoinApWorker getJoinApWorker() {

        if(joinApWorker == null){
            joinApWorker = new JoinApWorker(applicationContext);
        }

        return joinApWorker;
    }

    private HttpServerStart getHttpServerStart(){

        if(httpServerStart == null){
            httpServerStart = new HttpServerStart(applicationContext);
        }

        return httpServerStart;
    }

}
