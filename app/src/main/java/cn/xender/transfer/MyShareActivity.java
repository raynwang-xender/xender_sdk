package cn.xender.transfer;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import cn.xender.aar.QrCodeCreateWorker;
import cn.xender.aar.ShareActivity;
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

    //已经允许所有权限
    //Grant all permissions
    @Override
    protected void grantAllPermi() {
        super.grantAllPermi();
        Toast.makeText(this,"grantAllPermi",Toast.LENGTH_SHORT).show();
    }
    //成功展示二维码
    //Successfully show QR code
    @Override
    protected void showQRCode() {
        super.showQRCode();
        Toast.makeText(this,"showQRCode",Toast.LENGTH_SHORT).show();
    }
    //连接成功
    //Successfully connect
    @Override
    protected void connect() {
        super.connect();
        Toast.makeText(this,"connect",Toast.LENGTH_SHORT).show();
    }

    /**
     * 每传成功一个文件，走一次
     * One file success, run once
     * @param s     phone B android Id
     * @param s1    channel
     * @param s2    filepath
     */
    @Override
    protected void oneFileSucc(String s, String s1, String s2) {
        super.oneFileSucc(s, s1, s2);
        Toast.makeText(this,"oneSuccess:"+s2,Toast.LENGTH_SHORT).show();
    }

    /**
     * 每传失败一个文件，走一次
     * One file fail, run once
     * @param s     filepath
     */
    @Override
    protected void oneFileFail(String s) {
        super.oneFileFail(s);
        Toast.makeText(this,"oneFail:"+s,Toast.LENGTH_SHORT).show();
    }

    /**
     * 所有文件传输成功，走一次
     * All files success, run once
     * @param s     phone B android Id
     * @param s1    channel
     */
    @Override
    protected void allFileSucc(String s, String s1) {
        super.allFileSucc(s, s1);
        Toast.makeText(this,"allSuccess",Toast.LENGTH_SHORT).show();
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
