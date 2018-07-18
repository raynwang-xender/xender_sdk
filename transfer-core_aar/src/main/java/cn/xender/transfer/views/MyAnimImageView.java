package cn.xender.transfer.views;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;


public class MyAnimImageView extends ImageView {
    public MyAnimImageView(Context context) {
        super(context);
    }

    public MyAnimImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyAnimImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface OnFrameAnimationListener {
        /**
         * 动画开始播放后调用
         */
        void onStart();

        /**
         * 动画结束播放后调用
         */
        void onEnd();
    }

    /**
     * 带动画监听的播放
     */
    public static void loadAnimation(ImageView view, final OnFrameAnimationListener listener) {
        AnimationDrawable anim = (AnimationDrawable) view.getBackground();
        anim.start();
        if (listener != null) {
            // 调用回调函数onStart
            listener.onStart();
        }

        // 计算动态图片所花费的事件
        int durationTime = 0;
        for (int i = 0; i < anim.getNumberOfFrames(); i++) {
            durationTime += anim.getDuration(i);
        }

        // 动画结束后
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (listener != null) {
                    // 调用回调函数onEnd
                    listener.onEnd();
                }
            }
        }, durationTime);
    }
}
