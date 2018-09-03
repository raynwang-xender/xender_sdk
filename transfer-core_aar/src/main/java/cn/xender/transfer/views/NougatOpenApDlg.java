package cn.xender.transfer.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.KeyEvent;

import cn.xender.transfer.R;
import cn.xender.transfer.ShareActivityContent;


public class NougatOpenApDlg {

    private AlertDialog dialog;
    private Activity mActivity;

    public NougatOpenApDlg(Activity mActivity){
        this.mActivity = mActivity;
        init();
    }

    private void init() {
        ShareActivityContent content = ShareActivityContent.getInstance();
        if(dialog==null){
            dialog = new AlertDialog.Builder(mActivity)
                    .setCancelable(false)//点击外部区域，不关
                    .setMessage(content.getDlg_3_msg())
                    .setPositiveButton(content.getDlg_3_positive(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setComponent(new ComponentName("com.android.settings","com.android.settings.TetherSettings"));
                                mActivity.startActivity(intent);
                            }catch (Exception e){
                                try {
                                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mActivity.startActivity(intent);
                                }catch (Exception e1){
                                }
                            }
                        }
                    })
                    .setNegativeButton(content.getDlg_3_negative(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.finish();
                        }
                    })
                    .create();

        }


        dialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if(event.getAction() ==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK ){
                    dismiss();

                    mActivity.finish();
                }
                return false;
            }
        });
    }

    public void dismiss(){
        if(!mActivity.isFinishing() && dialog!=null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    public void show(){
        if(!mActivity.isFinishing() && dialog!=null && !dialog.isShowing()){
            dialog.show();
        }
    }

    public static void goBack(Context context, String classNameNeedGo){
        try {
            Intent intent1 = new Intent(Intent.ACTION_MAIN);
            intent1.addCategory(Intent.CATEGORY_LAUNCHER);
//            intent1.addCategory(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent1.setComponent(new ComponentName(context.getPackageName(),classNameNeedGo));
            context.startActivity(intent1);
        }catch (Exception e){

        }
    }
}
