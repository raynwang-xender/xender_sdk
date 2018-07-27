package cn.xender.transfer;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toolbar;


/**
 * 页面埋点基类
 *
 * @author wang 通过基类的形式，降低N个Activity的埋点成本
 */
public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setToolbar(int viewId, int titleId) {
        Toolbar mToolbarView = (Toolbar) findViewById(viewId);
        setActionBar(mToolbarView);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            setTitle(titleId);
            setTitleColor(Color.WHITE);
            actionBar.setHomeAsUpIndicator(R.drawable.tc_ic_actionbar_back);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    public void setToolbarLow(int titleViewId, int titleId,int titleHomeId) {
        TextView toolbar_title = (TextView) findViewById(titleViewId);
        toolbar_title.setText(titleId);

        findViewById(titleHomeId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTitleHomeClick();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onTitleHomeClick();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void onTitleHomeClick(){
        finish();
    }

}
