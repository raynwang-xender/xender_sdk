package cn.xender.transfer;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import cn.xender.aar.QrCodeCreateWorker;
import cn.xender.aar.ShareActivity;
import cn.xender.core.server.utils.NeedSharedFiles;
import cn.xender.transfertest.R;

/**
 * 写一个Activity继承ShareActivity
 * New an Activity to extends ShareActivity
 *
 * 设置二维码边长
 * Set QR size
 *
 * 重写addQrCodeLayoutAndSetQrCode()方法，里面给ImageView设置二维码
 * Override addQrCodeLayoutAndSetQrCode(), set QR to a ImageView
 */
public class MyShareActivity extends ShareActivity {

    RelativeLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myshare);

        container = findViewById(R.id.container);

        setQRSize(240); //二维码边长 默认200dp
                        //default 200dp

    }


    //开启热点之前，建议先放一个ProgressBar，开启热点之后，移除ProgressBar，添加ImageView
    //Before turning on hopspot, suggest to put a ProgressBar. After that, remove ProgressBar then add ImageView.
    @Override
    protected void addQrCodeLayoutAndSetQrCode() {
        container.removeAllViews();
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(QrCodeCreateWorker.getQrBitmap());
        container.addView(iv);
    }


    /**
     * One file success, run once
     * @param s     phone B android Id
     * @param s1    channel
     * @param s2    filepath
     */
    @Override
    protected void oneFileSucc(String s, String s1, String s2) {
        super.oneFileSucc(s, s1, s2);
    }

    /**
     * One file fail, run once
     * @param s     filepath
     */
    @Override
    protected void oneFileFail(String s) {
        super.oneFileFail(s);
    }

    /**
     * All files success, run once
     * @param s     phone B android Id
     * @param s1    channel
     */
    @Override
    protected void allFileSucc(String s, String s1) {
        super.allFileSucc(s, s1);
    }

    /**
     * Connect successfully
     */
    @Override
    protected void connect() {
        super.connect();
    }

    /**
     * Disconnect
     */
    @Override
    protected void disconnect() {
    }

    @Override
    protected void grantStoragePermi() {
    }

    @Override
    protected void grantLocationPermi() {
    }

    @Override
    protected void denyStoragePermi() {
    }

    @Override
    protected void denyLocationPermi() {
    }

    @Override
    protected void denyStoragePermiAndDontAsk() {
    }

    @Override
    protected void denyLocationPermiAndDontAsk() {
    }

    @Override
    protected void dialog_1_yes() {
    }

    @Override
    protected void dialog_2_yes() {
    }

    @Override
    protected void dialog_3_yes() {
    }

    @Override
    protected void dialog_4_yes() {
    }

    @Override
    protected void dialog_1_no() {
    }

    @Override
    protected void dialog_2_no() {
    }

    @Override
    protected void dialog_3_no() {
    }

    @Override
    protected void dialog_4_no() {
    }
}
