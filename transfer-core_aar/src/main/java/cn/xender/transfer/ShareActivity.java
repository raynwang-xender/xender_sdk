package cn.xender.transfer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.xender.core.ap.CoreApManager;
import cn.xender.core.ap.CoreCreateApCallback;
import cn.xender.core.ap.CreateApEvent;
import cn.xender.core.log.Logger;
import cn.xender.core.server.utils.ActionListener;
import cn.xender.core.server.utils.ActionProtocol;
import cn.xender.transfer.permission.PermissionUtil;
import cn.xender.transfer.views.ConnectionView;
import cn.xender.transfer.views.NougatOpenApDlg;

public class ShareActivity extends BaseActivity implements ActionListener {

    private LinearLayout tc_content_container;

    private ActionProtocol protocol;

    private boolean dialogOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tc_share_activity);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setToolbar(R.id.toolbar,R.string.app_name);
        }else{
            setToolbarLow(R.id.toolbar_title,R.string.app_name,R.id.home_back);
        }


        tc_content_container = (LinearLayout) findViewById(R.id.tc_content_container);

        showCreatingLayout();

        CoreApManager.getInstance().initApplicationContext(getApplicationContext());

        createAp();

        protocol = new ActionProtocol();
        protocol.setActionListener(this);
        protocol.register(this);

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
                public void callback(CreateApEvent result) {
                    handleCreateResult(result,true);
                }
            });
        }else{
            /**
             * Rayn
             * dialogOut开关，只弹一次
             */
            if (!dialogOut) {
                dialogOut = true;
                PermissionUtil.showPermissionDlg(this, PermissionUtil.ALL_NEED_PERMISSIONS, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, R.string.tc_need_permission_contect_frends);
            }else {
                PermissionUtil.requestAllNeededPermission(this);
            }
        }
    }


    /**
     * Rayn
     * 请求权限+开启热点
     */
//    private void createAp(){
//        if(PermissionUtil.requestAllNeededPermission(this)){
//            CoreApManager.getInstance().createAp("", "", 30000, 12, new CoreCreateApCallback() {
//                @Override
//                public void callback(final CreateApEvent result) {
//                    _handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            handleCreateResult(result,true);
//                        }
//                    });
//                }
//            });
//        }
//    }

    private void handleCreateResult(CreateApEvent result,boolean retryIfNeed) {

        if(result.isOk() && !TextUtils.isEmpty(result.getUrl())){

            int qrSize = PhonePxConversion.dip2px(ShareActivity.this,200);

            new QrCodeCreateWorker().startWork(ShareActivity.this,_handler,result.getUrl(),qrSize,qrSize, Color.WHITE,true);

        }else if(result.isNeedUserManualOpen()){

            showManualOpenDialog();

        }else if(result.isManualOpenSuccess()){

            dismissManualOpenDialog();

            NougatOpenApDlg.goBack(ShareActivity.this,ShareActivity.class.getName());
        }else if(result.isOpendButWeCannotUseAndNeedRetry()){

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
                .setTitle(R.string.tc_nougat_open_ap_warn_title)
                .setMessage(R.string.tc_quit_dlg_content)
                .setPositiveButton(R.string.tc_quit_dlg_quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.tc_quit_dlg_cancel, null)
                .create();


        dialog.show();


    }

    private void addQrCodeLayoutAndSetQrCode(){

        View tc_qr_layout = LayoutInflater.from(this).inflate(R.layout.tc_qr_layout,null);

        tc_content_container.removeAllViews();

        tc_content_container.addView(tc_qr_layout);


        ((ImageView)findViewById(R.id.tc_qr_code_iv)).setImageBitmap(QrCodeCreateWorker.getQrBitmap());

    }


    private void showCreatingLayout(){

        addWaitingLayout();

        TextView tc_waiting_des_tv = findViewById(R.id.tc_waiting_des_tv);
        tc_waiting_des_tv.setText(R.string.tc_creating_ap);

        ConnectionView tc_waiting_view = (ConnectionView)findViewById(R.id.tc_waiting_view);

        tc_waiting_view.drawCenterImage(R.drawable.tc_ic_wifi);
        tc_waiting_view.startRippleAnimation();
    }


    private void showTransferingLayout(){

        addWaitingLayout();

        TextView tc_waiting_des_tv = findViewById(R.id.tc_waiting_des_tv);
        tc_waiting_des_tv.setText(R.string.tc_transferring);

        ConnectionView tc_waiting_view = (ConnectionView)findViewById(R.id.tc_waiting_view);

        tc_waiting_view.drawCenterImage(R.drawable.tc_ic_transfer);
        tc_waiting_view.startRippleAnimation();
    }




    private void showTransferSuccessLayout(){

        addResultLayout();

        TextView tc_result_des_tv = findViewById(R.id.tc_result_des_tv);
        tc_result_des_tv.setText(R.string.tc_transfer_success);


        ((ImageView)findViewById(R.id.tc_result_iv)).setImageResource(R.drawable.tc_ic_succeed);
    }

    private void showTransferFailureLayout(){

        addResultLayout();

        TextView tc_result_des_tv = findViewById(R.id.tc_result_des_tv);
        tc_result_des_tv.setText(R.string.tc_transfer_failure);


        ((ImageView)findViewById(R.id.tc_result_iv)).setImageResource(R.drawable.tc_ic_defeated);
    }


    private void addWaitingLayout(){

        View tc_waiting_layout = LayoutInflater.from(this).inflate(R.layout.tc_waiting_layout,null);

        tc_content_container.removeAllViews();

        tc_content_container.addView(tc_waiting_layout);

    }


    private void addResultLayout(){

        View tc_result_layout = LayoutInflater.from(this).inflate(R.layout.tc_result_layout,null);

        tc_content_container.removeAllViews();

        tc_content_container.addView(tc_result_layout);

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
    public void transferSuccess() {
        showTransferSuccessLayout();
    }

    @Override
    public void transferFailure() {
        showTransferFailureLayout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSIONS:
                createAp();
                break;
            case PermissionUtil.CREATE_AP_WRITE_SETTING_PERMISSIONS:
                createAp();
                break;
            case PermissionUtil.ACCESS_COARSE_LOCATION_PERMISSIONS:
                if (resultCode == RESULT_OK) {
                    createAp();
                }
                break;
            case PermissionUtil.ACCESS_GPS_LOCATION_PERMISSIONS:
                createAp();
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (null == permissions || permissions.length == 0) return;
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
