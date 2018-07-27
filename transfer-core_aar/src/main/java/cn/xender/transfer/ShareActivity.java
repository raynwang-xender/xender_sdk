package cn.xender.transfer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.security.Permission;

import cn.xender.core.ap.CoreApManager;
import cn.xender.core.ap.CoreCreateApCallback;
import cn.xender.core.ap.CreateApEvent;
import cn.xender.core.server.utils.ActionListener;
import cn.xender.core.server.utils.ActionProtocol;
import cn.xender.transfer.permission.PermissionUtil;
import cn.xender.transfer.views.ConnectionView;
import cn.xender.transfer.views.MyAnimImageView;
import cn.xender.transfer.views.NougatOpenApDlg;

import static android.view.View.VISIBLE;

public class ShareActivity extends BaseActivity implements ActionListener {

    private LinearLayout tc_content_container;
    private TextView android_o_tips;

    private ActionProtocol protocol;

    private boolean dialogOut = false;
    private LinearLayout android_o_tips_layout;
    private RelativeLayout create_ap_layout;
    private LinearLayout create_ap_tips_layout;
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
        android_o_tips_layout = findViewById(R.id.android_o_tips_layout);
        create_ap_layout = findViewById(R.id.create_ap_layout);
        android_o_tips = findViewById(R.id.android_o_tips);
        create_ap_tips_layout = findViewById(R.id.create_ap_tips_layout);
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
        System.out.println("---Rayn createAp");
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
                PermissionUtil.showPermissionDlg(this);
            }else {
                PermissionUtil.requestAllNeededPermission(this);
            }
        }
    }

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

        showNoXenderAnimIn(android_o_tips);
        android_o_tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(android_o_tips_layout.getVisibility() == VISIBLE &&android_o_tips_layout.getScaleX() == 1.0f){
                    //进去
                    androidOTipsAnimOut();
                }else{
                    //出来
                    androidOTipsAnimIn();
                }
            }
        });

        tc_content_container.addView(tc_qr_layout);
        ((ImageView)findViewById(R.id.tc_qr_code_iv)).setImageBitmap(QrCodeCreateWorker.getQrBitmap());

    }

    private void showNoXenderAnimIn(View view){
        view.setVisibility(VISIBLE);
        float translationX = view.getWidth();
        Animator animIn = ObjectAnimator.ofFloat(view, "translationX", translationX, 0);
        animIn.setDuration(600);
        animIn.start();
    }

    ScaleAnimation dismissScaleAnimation;
    ScaleAnimation showScaleAnimation;

    private void androidOTipsAnimOut(){
        if(dismissScaleAnimation != null && !dismissScaleAnimation.hasEnded()){
            return;
        }
        if (showScaleAnimation != null) {
            showScaleAnimation.cancel();
        }
        float pivotXValue = (android_o_tips.getLeft() + android_o_tips.getWidth()/2)/(float)PhonePxConversion.getScreenWidth(this);
        //减去other_phone_dialog的top是因为此时动画只对other_phone_dialog为全屏的时候生效
        float pivotYValue = (android_o_tips.getTop()+android_o_tips.getHeight()/2 - android_o_tips_layout.getTop())/(float)create_ap_layout.getHeight();

        dismissScaleAnimation = new ScaleAnimation(1.0f, 0, 1.0f, 0, Animation.RELATIVE_TO_PARENT, pivotXValue, Animation.RELATIVE_TO_PARENT, pivotYValue);
        dismissScaleAnimation.setDuration(200);
        android_o_tips_layout.startAnimation(dismissScaleAnimation);
        dismissScaleAnimation.setInterpolator(new AccelerateInterpolator());
        dismissScaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                android_o_tips_layout.setVisibility(View.INVISIBLE);
                create_ap_tips_layout.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void androidOTipsAnimIn() {
        if(android_o_tips_layout.getScaleX()<1.0f){
            return;
        }
        if (dismissScaleAnimation != null) {
            dismissScaleAnimation.cancel();
        }
        float pivotXValue = (android_o_tips.getLeft() + android_o_tips.getWidth()/2)/(float)PhonePxConversion.getScreenWidth(this);
        //减去other_phone_dialog的top是因为此时动画只对other_phone_dialog为全屏的时候生效
        float pivotYValue = (android_o_tips.getTop()+android_o_tips.getHeight()/2 - android_o_tips_layout.getTop())/(float)create_ap_layout.getHeight();
        android_o_tips_layout.setVisibility(VISIBLE);
        showScaleAnimation = new ScaleAnimation(0, 1.0f, 0, 1.0f, Animation.RELATIVE_TO_PARENT, pivotXValue, Animation.RELATIVE_TO_PARENT, pivotYValue);
        showScaleAnimation.setDuration(200);
        showScaleAnimation.setInterpolator(new AccelerateInterpolator());

        android_o_tips_layout.startAnimation(showScaleAnimation);

        showScaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                create_ap_tips_layout.setBackgroundColor(getResources().getColor(R.color.tc_txt_color));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
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

        android_o_tips.setVisibility(View.INVISIBLE);

        addWaitingLayout();

        TextView tc_waiting_des_tv = findViewById(R.id.tc_waiting_des_tv);
        tc_waiting_des_tv.setText(R.string.tc_transferring);

        ConnectionView tc_waiting_view = (ConnectionView)findViewById(R.id.tc_waiting_view);

        tc_waiting_view.drawCenterImage(R.drawable.tc_ic_transfer);
        tc_waiting_view.startRippleAnimation();
    }




    private void showTransferSuccessLayout(){
        showRocketAnimation();
    }

    private  MyAnimImageView iv_rocket;

    private void showRocketAnimation() {

        View rocket_layout = LayoutInflater.from(this).inflate(R.layout.rocket_layout,null);

        iv_rocket = rocket_layout.findViewById(R.id.iv_rocket);

        MyAnimImageView.loadAnimation(iv_rocket, new MyAnimImageView.OnFrameAnimationListener() {
            @Override
            public void onStart() {
            }
            @Override
            public void onEnd() {
                /**
                 * Rayn
                 * 火箭动画结束后，向上移动
                 */
                showMoveAnimation();
            }
        });

        tc_content_container.removeAllViews();
        tc_content_container.addView(rocket_layout);
    }

    private void showMoveAnimation() {
        TranslateAnimation ta = new TranslateAnimation(0,0,0,-1000);
        ta.setDuration(600);
        ta.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                /**
                 * Rayn
                 * 动画全部结束，才显示完成页面
                 */
                addResultLayout();
                TextView tc_result_des_tv = findViewById(R.id.tc_result_des_tv);
                tc_result_des_tv.setText(R.string.tc_transfer_success);
                ((ImageView)findViewById(R.id.tc_result_iv)).setImageResource(R.drawable.tc_ic_succeed);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv_rocket.startAnimation(ta);


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
            case PermissionUtil.BACK_FROM_SETTING_PERMISSION://从系统设置回来
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
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (null == permissions || permissions.length == 0) return;

        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            //在用户已经拒绝授权的情况下，如果shouldShowRequestPermissionRationale返回false则
            //可以推断出用户选择了“不在提示”选项，在这种情况下需要引导用户至设置页手动授权
            if (requestCode == PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSIONS) {
                //选择了Don't ask again
                if (!shouldShowRequestPermissionRationale("android.permission.READ_EXTERNAL_STORAGE")) {
                    PermissionUtil.showSettingPermissionDlg(this);
                } else {
                    createAp();
                }
            }

            if (requestCode == PermissionUtil.ACCESS_COARSE_LOCATION_PERMISSIONS) {
                if (!shouldShowRequestPermissionRationale("android.permission.ACCESS_COARSE_LOCATION")) {
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
