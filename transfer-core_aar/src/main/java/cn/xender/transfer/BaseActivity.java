package cn.xender.transfer;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


/**
 * 页面埋点基类
 *
 * @author wang 通过基类的形式，降低N个Activity的埋点成本
 */
public abstract class BaseActivity extends AppCompatActivity {
    private Toolbar mToolbarView;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(R.style.tc_AppTheme);

    }


    public void setToolbar(int viewId, int titleId) {
        mToolbarView = (Toolbar) findViewById(viewId);
        setSupportActionBar(mToolbarView);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            setTitle(titleId);
            actionBar.setHomeAsUpIndicator(R.drawable.tc_ic_actionbar_back);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
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
