package cn.xender.transfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import cn.xender.core.server.utils.ActionListenerAdapter;
import cn.xender.core.server.utils.ActionProtocol;
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
                Intent intent = new Intent(MainActivity.this,ShareActivity.class);
                startActivity(intent);
            }
        });


    }
}

