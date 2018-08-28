package cn.xender.transfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import cn.xender.core.server.utils.NeedSharedFiles;
import cn.xender.transfertest.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        setToolbar(R.id.toolbar,"Xender SDK Demo");

        findViewById(R.id.share_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                NeedSharedFiles.confirmStart();//初始化
                NeedSharedFiles.setShareMyApk(true);//是否传输apk包，默认true
//                NeedSharedFiles.appendNewFile("/storage/emulated/0/aaa.png", NeedSharedFiles$FileItem.CATE_IMAGE);
//                NeedSharedFiles.appendNewFile("/storage/emulated/0/bbb.mp4", NeedSharedFiles$FileItem.CATE_VIDEO);
                NeedSharedFiles.appendNewFile("/storage/emulated/0/aaa.png", NeedSharedFiles.FileItem.CATE_IMAGE);
                NeedSharedFiles.appendNewFile("/storage/emulated/0/bbb.mp4", NeedSharedFiles.FileItem.CATE_VIDEO);

                Intent intent = new Intent(MainActivity.this,ShareActivity.class);
                startActivity(intent);

            }
        });

    }
}

