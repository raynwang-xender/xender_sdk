package cn.xender.transfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import cn.xender.core.server.utils.NeedSharedFiles;
import cn.xender.core.server.utils.NeedSharedFiles$FileItem;
import cn.xender.transfertest.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.share_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NeedSharedFiles.confirmStart();//初始化
                NeedSharedFiles.setShareMyApk(true);//是否传输apk包，默认true
                NeedSharedFiles.appendNewFile("/storage/emulated/0/aaa.png", NeedSharedFiles$FileItem.CATE_IMAGE);
                NeedSharedFiles.appendNewFile("/storage/emulated/0/bbb.mp4", NeedSharedFiles$FileItem.CATE_VIDEO);
//                NeedSharedFiles.appendNewFile("/storage/emulated/0/aaa.png", NeedSharedFiles.FileItem.CATE_IMAGE);
//                NeedSharedFiles.appendNewFile("/storage/emulated/0/bbb.mp4", NeedSharedFiles.FileItem.CATE_VIDEO);

                ShareActivityContent content = ShareActivityContent.getInstance();
                content.setTitle("titletitle");
                content.setInvite("inviteinvite");
                content.setPic_url("/storage/emulated/0/aaa.png");
                content.setFilm_name("film_namefilm_name");
                content.setFilm_volume("100mb");
                content.setConnecting("connectingconnecting");
                content.setSending("sendingsending");
                content.setDlg_1_msg("11111111");
                content.setDlg_1_positive("111yes");
                content.setDlg_1_negative("111no");
                content.setDlg_2_msg("22222222");
                content.setDlg_2_positive("222yes");
                content.setDlg_2_negative("222no");
                content.setDlg_3_msg("3333333");
                content.setDlg_3_positive("333yes");
                content.setDlg_3_negative("333no");
                content.setDlg_4_msg("4444444");
                content.setDlg_4_positive("444yes");
                content.setDlg_4_negative("444no");

                Intent intent = new Intent(MainActivity.this,ShareActivity.class);
                startActivity(intent);

            }
        });

    }
}

