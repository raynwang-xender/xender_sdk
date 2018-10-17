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


//    @Override
//    public void someoneOnline() {
//        Toast.makeText(this,"connect success",Toast.LENGTH_SHORT).show();
//    }
//    @Override
//    public void someoneOffline() {
//    }
//    /**
//     * @param s     android_id
//     * @param s1    channel
//     * @param s2    filePath
//     */
//    @Override
//    public void transferSuccess(String s, String s1, String s2) {   //每传一个文件就走一次
//                                                                    //Run once per file.
//        Toast.makeText(this,s2+"transfer success",Toast.LENGTH_SHORT).show();
//    }
//    /**
//     * @param s     filePath
//     */
//    @Override
//    public void transferFailure(String s) {
//    }
//    /**
//     * @param s     android_id
//     * @param s1    filePath
//     */
//    @Override
//    public void transferAll(String s, String s1) {  //传完所有，走一次
//                                                    //Run once after all files.
//        Toast.makeText(this,"transfer all",Toast.LENGTH_SHORT).show();
//    }


    @Override
    protected void connect() {
        System.out.println("---Rayn 连上了");
    }


    @Override
    protected void allFileSucc(String s, String s1) {

    }

    @Override
    protected void oneFileFail(String s) {

    }

    @Override
    protected void oneFileSucc(String s, String s1, String s2) {

    }

    @Override
    protected void disconnect() {

    }
}
