package cn.xender.core.ap;

import android.content.Context;
import android.os.Build;

import cn.xender.core.HttpServerStart;
import cn.xender.core.ap.utils.FilterManager;

public class CoreApManager implements ICoreApManager {

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

        getCreateApWorker().createAp(ssid, password, timeout, requestCode, callback);

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

        getCreateApWorker().retryCreateAp(ssid,password, timeout,requestCode,callback);

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

    private HttpServerStart getHttpServerStart(){

        if(httpServerStart == null){
            httpServerStart = new HttpServerStart(applicationContext);
        }

        return httpServerStart;
    }

}
