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
 * 设置二维码边长
 * 重写addQrCodeLayoutAndSetQrCode()方法，里面给ImageView设置二维码
 */
public class MyShareActivity extends ShareActivity {

    RelativeLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myshare);

        container = findViewById(R.id.container);

        setQRSize(240);//二维码边长 默认200dp

    }

    //生成二维码之前，建议先放一个ProgressBar，生成二维码之后，移除pb，添加iv
    @Override
    protected void addQrCodeLayoutAndSetQrCode() {
        container.removeAllViews();
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(QrCodeCreateWorker.getQrBitmap());
        container.addView(iv);
    }
    @Override
    public void someoneOnline() {
        Toast.makeText(this,"连接成功",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void someoneOffline() {
    }
    /**
     * @param s     android_id
     * @param s1    channel
     * @param s2    filePath
     */
    @Override
    public void transferSuccess(String s, String s1, String s2) {//每传一个文件就走一次
        Toast.makeText(this,s2+"传输完成",Toast.LENGTH_SHORT).show();
    }
    /**
     * @param s     filePath
     */
    @Override
    public void transferFailure(String s) {
    }
    /**
     * @param s     android_id
     * @param s1    filePath
     */
    @Override
    public void transferAll(String s, String s1) {//传完所有，走一次
        Toast.makeText(this,"全部传输完成",Toast.LENGTH_SHORT).show();
    }
}
