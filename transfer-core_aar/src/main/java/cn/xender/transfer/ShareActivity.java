package cn.xender.transfer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import cn.xender.core.ap.CoreApManager;
import cn.xender.core.ap.CoreCreateApCallback;
import cn.xender.core.ap.CreateApEvent;
import cn.xender.core.server.utils.ActionListener;
import cn.xender.core.server.utils.ActionProtocol;
import cn.xender.transfer.permission.PermissionUtil;
import cn.xender.transfer.views.NougatOpenApDlg;


public class ShareActivity extends BaseActivity implements ActionListener {

    private LinearLayout tc_content_container;
    private RelativeLayout rl_status;
    private TextView tv_status, tv_film_name, tv_film_volume;
    private ImageView iv_status, iv_film_pic;

    private ActionProtocol protocol;

    private boolean dialogOut = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tc_share_activity);

        getContentFromInstance();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){//21 5.0
            setToolbar(R.id.toolbar,title);
        }else{
            setToolbarLow(R.id.toolbar_title,title,R.id.home_back);
        }

        tc_content_container = findViewById(R.id.tc_content_container);
        iv_film_pic = findViewById(R.id.iv_film_pic);
        if (!TextUtils.isEmpty(pic_url)) {
            Glide.with(this).load(pic_url).into(iv_film_pic);
        }
        tv_film_name = findViewById(R.id.tv_film_name);
        tv_film_name.setText(film_name);
        tv_film_volume = findViewById(R.id.tv_film_volume);
        tv_film_volume.setText(film_volume);
        rl_status = findViewById(R.id.rl_status);
        tv_status = findViewById(R.id.tv_status);
        tv_status.setText(connecting);
        iv_status = new ImageView(this);

        CoreApManager.getInstance().initApplicationContext(getApplicationContext());

        createAp();

        protocol = new ActionProtocol();
        protocol.setActionListener(this);
        protocol.register(this);
    }

    private String title;
    private String invite;
    private String pic_url;
    private String film_name;
    private String film_volume;
    private String connecting;
    private String sending;
    private ShareActivityContent content;

    private void getContentFromInstance() {
        content = ShareActivityContent.getInstance();
        title = content.getTitle();
        invite = content.getInvite();
        pic_url = content.getPic_url();
        film_name = content.getFilm_name();
        film_volume = content.getFilm_volume();
        connecting = content.getConnecting();
        sending = content.getSending();
    }

    /**
     * Rayn
     * 检查权限+开启热点
     * 检查失败弹出dialog
     */
    private void createAp(){
        if(PermissionUtil.checkAllNeededPermission(this)){
            CoreApManager.getInstance().createAp("", "", 30000, 12, new CoreCreateApCallback() {
                @Override
                public void callback(final CreateApEvent result) {
                    /**
                     * Rayn
                     * callback出来的是子线程，而handleCreateResult里面（7.1会让手动开启热点）
                     * 会创建dialog，所以要用handler post到主线程去跑
                     */
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            handleCreateResult(result,true);
                        }
                    });
                }
            });
        }else{
            /**
             * Rayn
             * dialogOut开关，只弹一次
             */
            if (!dialogOut) {
                dialogOut = true;//弹出之后，就变成true，下次不弹
                PermissionUtil.showPermissionDlg(this);
            }else {
                PermissionUtil.requestAllNeededPermission(this);
            }
        }
    }

    private void handleCreateResult(CreateApEvent result,boolean retryIfNeed) {
        //CREATE_OK = 6
        if(result.isOk() && !TextUtils.isEmpty(result.getUrl())){

            int qrSize = PhonePxConversion.dip2px(ShareActivity.this,230);

            new QrCodeCreateWorker().startWork(ShareActivity.this,_handler,result.getUrl(),qrSize,qrSize, Color.WHITE,true);

        }else if(result.isNeedUserManualOpen()){//SAVED_25_CONFIG = 3

            showManualOpenDialog();//显示7.1的dlg

        }else if(result.isManualOpenSuccess()){//AP_ENABLED_25 = 4

            dismissManualOpenDialog();//关掉7.1的dlg
            //回到ShareActivity
            NougatOpenApDlg.goBack(ShareActivity.this,ShareActivity.class.getName());
        }else if(result.isOpendButWeCannotUseAndNeedRetry()){//CREATE_OK_BUT_NO_IP_ON25 = 5

            if(retryIfNeed){

                CoreApManager.getInstance().retryCreateAp("", "", 30000, 12, new CoreCreateApCallback() {
                    @Override
                    public void callback(final CreateApEvent result) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                handleCreateResult(result,false);
                            }
                        });
                    }
                });
            }else{
                //失败
                finish();
            }
        }else if(result.isError()){
                finish();
        }else if(result.isOff()){
                finish();
        }
    }

    //退出的时候把单例置空
    @Override
    public void finish() {
        content = null;
        super.finish();
    }


    private NougatOpenApDlg dlg;

    private void showManualOpenDialog(){
        if(dlg == null){
            dlg = new NougatOpenApDlg(this);
        }

        dlg.show();
    }

    private void dismissManualOpenDialog(){
        if(dlg != null){
            dlg.dismiss();
        }

    }

    Handler _handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == QrCodeCreateWorker.CREATE_SUCCESS_WHAT){

                addQrCodeLayoutAndSetQrCode();

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        protocol.unregister(this);
        QrCodeCreateWorker.clear();

        CoreApManager.getInstance().shutdownAp();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            showQuitDlg();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    void onTitleHomeClick() {

        showQuitDlg();
    }


    private void showQuitDlg(){

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)//点击外部区域，不关
                .setMessage(ShareActivityContent.getInstance().getDlg_2_msg())
                .setPositiveButton(ShareActivityContent.getInstance().getDlg_2_positive(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(ShareActivityContent.getInstance().getDlg_2_negative(), null)
                .create();

        dialog.show();
    }



    private void addQrCodeLayoutAndSetQrCode(){
        View tc_qr_layout = LayoutInflater.from(this).inflate(R.layout.tc_qr_layout,null);
        tc_content_container.removeAllViews();

        TextView tv_invite = tc_qr_layout.findViewById(R.id.tv_invite);
        tv_invite.setText(invite);

        tc_content_container.addView(tc_qr_layout);
        ((ImageView)findViewById(R.id.tc_qr_code_iv)).setImageBitmap(QrCodeCreateWorker.getQrBitmap());

    }


    private void showTransferingLayout(){
        tv_status.setText(sending);
    }

    private void showTransferSuccessLayout(){
        iv_status.setImageDrawable(getResources().getDrawable(R.drawable.complete));
        rl_status.removeAllViews();
        rl_status.addView(iv_status);
    }

    private void showTransferFailureLayout(){
        iv_status.setImageDrawable(getResources().getDrawable(R.drawable.fail));
        rl_status.removeAllViews();
        rl_status.addView(iv_status);
    }





    @Override
    public void someoneOnline() {
        showTransferingLayout();
    }

    @Override
    public void someoneOffline() {
        addQrCodeLayoutAndSetQrCode();
    }

    @Override
    public void transferSuccess(String android_id, String channel,String filePath) {
        showTransferSuccessLayout();
    }

    @Override
    public void transferFailure(String filePath) {
        showTransferFailureLayout();
    }

    @Override
    public void transferAll(String android_id, String channel) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSIONS:
                System.out.println("---Rayn READ_EXTERNAL_STORAGE_PERMISSIONS");
                createAp();
                break;
            case PermissionUtil.CREATE_AP_WRITE_SETTING_PERMISSIONS://从allow modify settings回来
                System.out.println("---Rayn CREATE_AP_WRITE_SETTING_PERMISSIONS");
                dialogOut = false;//如果没有允许，让dialog再弹出
                createAp();
                break;
            case PermissionUtil.ACCESS_COARSE_LOCATION_PERMISSIONS:
                System.out.println("---Rayn ACCESS_GPS_LOCATION_PERMISSIONS");
                if (resultCode == RESULT_OK) {
                    createAp();
                }
                break;
            case PermissionUtil.ACCESS_GPS_LOCATION_PERMISSIONS:
                System.out.println("---Rayn ACCESS_GPS_LOCATION_PERMISSIONS");
                createAp();
                break;
            case PermissionUtil.BACK_FROM_SETTING_PERMISSION://从系统设置回来
                System.out.println("---Rayn BACK_FROM_SETTING_PERMISSION");
                createAp();
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Rayn
     * requestCode
     * 每个数字代表一种权限 1是storage 7是location
     * grantResult
     * PackageManager.PERMISSION_GRANTED 是0
     * PackageManager.PERMISSION_DENIED 是-1
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (null == permissions || permissions.length == 0) return;

        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            //在用户已经拒绝授权的情况下，如果shouldShowRequestPermissionRationale返回false则
            //可以推断出用户选择了“不在提示”选项，在这种情况下需要引导用户至设置页手动授权
            if (requestCode == PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSIONS) {
                //选择了Don't ask again
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !shouldShowRequestPermissionRationale("android.permission.READ_EXTERNAL_STORAGE")) {
                    PermissionUtil.showSettingPermissionDlg(this);
                } else {
                    createAp();
                }
            }

            if (requestCode == PermissionUtil.ACCESS_COARSE_LOCATION_PERMISSIONS) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !shouldShowRequestPermissionRationale("android.permission.ACCESS_COARSE_LOCATION")) {
                    PermissionUtil.showSettingPermissionDlg(this);
                } else {
                    createAp();
                }
            }
        } else {//PERMISSION_GRANTED
            switch (requestCode) {
                case PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSIONS:
                case PermissionUtil.ACCESS_COARSE_LOCATION_PERMISSIONS:
                    createAp();
                    break;
                default:
                    break;
            }
        }
    }
}
