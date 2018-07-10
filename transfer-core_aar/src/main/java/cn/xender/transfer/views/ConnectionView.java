package cn.xender.transfer.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;


import java.util.ArrayList;

import cn.xender.transfer.R;


public class ConnectionView extends RelativeLayout {

    private Paint paint;
    private boolean animationRunning = false;
    private AnimatorSet animatorSet;
    private ArrayList<RippleView> rippleViewList = new ArrayList<>();
//    public static final int AVATARID = 1;

    // 圆心x坐标
    protected int mXCenter;
    // 圆心y坐标
    protected int mYCenter;

    private float centerRadius;
    private CircleView circleView;
    private boolean startTime = false;
    private int arcColor = Color.WHITE;
    private int centerColor = Color.WHITE;

    public boolean isStartTime() {
        return startTime;
    }

    public void setStartTime(boolean startTime) {
        this.startTime = startTime;
    }

    public ConnectionView(Context context) {
        super(context);
    }

    public ConnectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ConnectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (isInEditMode())
            return;

        if (null == attrs) {
            throw new IllegalArgumentException("Attributes should be provided to this view,");
        }
        int rippleDurationTime = 3000;
        int rippleAmount = 3;
        centerColor = getResources().getColor(R.color.tc_btn_color);
        arcColor = getResources().getColor(R.color.tc_btn_color);

        centerRadius = getResources().getDimension(R.dimen.tc_create_center_size);
        float rippleScale = getResources().getDimension(R.dimen.tc_create_ripple_size)/2/centerRadius;
        int rippleDelay = rippleDurationTime / rippleAmount;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(arcColor);

        LayoutParams rippleParams = new LayoutParams(-1,-1);
        rippleParams.addRule(CENTER_HORIZONTAL,TRUE);

        animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        ArrayList<Animator> animatorList = new ArrayList<>();


        for (int i = 0; i < rippleAmount; i++) {
            RippleView rippleView = new RippleView(getContext());
            addView(rippleView, rippleParams);
            rippleViewList.add(rippleView);
            final ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "scaleX", 1.0f, rippleScale);
            scaleXAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scaleXAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleXAnimator.setStartDelay(i * rippleDelay);
            scaleXAnimator.setDuration(rippleDurationTime);
            animatorList.add(scaleXAnimator);
            final ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "scaleY", 1.0f, rippleScale);
            scaleYAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scaleYAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleYAnimator.setStartDelay(i * rippleDelay);
            scaleYAnimator.setDuration(rippleDurationTime);
            animatorList.add(scaleYAnimator);
            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(rippleView, "alpha", 1.0f, 0f);
            alphaAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ObjectAnimator.RESTART);
            alphaAnimator.setStartDelay(i * rippleDelay);
            alphaAnimator.setDuration(rippleDurationTime);
            animatorList.add(alphaAnimator);
        }

        circleView = new CircleView(getContext());
//        circleView.drawAvatar();
        addView(circleView, rippleParams);

        animatorSet.playTogether(animatorList);
    }

    public void setArcColor(int arcColor){
        this.arcColor = arcColor;
        paint.setColor(arcColor);
        for(RippleView rippleView : rippleViewList){
            rippleView.postInvalidate();
        }
    }

    public void setCenterColor(int centerColor){
        this.centerColor = centerColor;
        if(circleView != null){
            circleView.setCenterColor(centerColor);
            circleView.postInvalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        setupBounds(w, h);
        postInvalidate();
    }

    private void setupBounds(int w, int h) {
        mXCenter = w / 2;
        mYCenter = h / 2;
    }

    public void drawCenterImage(int resId){
//        if(resId == AVATARID){
//            drawCenterAvatar();
//        }else {
//            circleView.drawCenterImage(resId);
//        }
        if (resId == 0)return;
        circleView.drawCenterImage(resId);
    }



    public void recycleBmp() {
        circleView.recycleBitmap();
    }

    private class RippleView extends View {

        public RippleView(Context context) {
            super(context);
            this.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawCircle(mXCenter, mYCenter, centerRadius-5, paint);
        }
    }

    public void startRippleAnimation() {
        if (!isRippleAnimationRunning()) {
            for (RippleView rippleView : rippleViewList) {
                rippleView.setVisibility(VISIBLE);
            }
            animatorSet.start();
            animationRunning = true;
        }
    }

    public void stopRippleAnimation() {
        if (isRippleAnimationRunning()) {
            animatorSet.end();
            animationRunning = false;
        }
    }

    public boolean isRippleAnimationRunning() {
        return animationRunning;
    }

    private class CircleView extends View{

        private Paint backgroundPaint;
        private Bitmap bmp;
        public CircleView(Context context) {
            super(context);
            init();
        }

        private void init() {
            backgroundPaint = new Paint();
            backgroundPaint.setAntiAlias(true);
            backgroundPaint.setColor(centerColor);
            backgroundPaint.setStyle(Paint.Style.FILL);
        }

        public void setCenterColor(int color){
            backgroundPaint.setColor(color);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawBackGround(canvas);
            drawCenterBitmap(canvas);
        }


        private void drawBackGround(Canvas canvas) {
            canvas.drawCircle(mXCenter, mYCenter, centerRadius, backgroundPaint);
        }

        private void drawCenterBitmap(Canvas canvas) {
            if (bmp != null && !bmp.isRecycled()) {
                Rect src = new Rect(0,0,bmp.getWidth(),bmp.getHeight());

                int horizontalRadius = bmp.getWidth()/2;
                int verticalRadius = bmp.getHeight()/2;
                Rect dst = new Rect(mXCenter - horizontalRadius,mYCenter - verticalRadius,mXCenter + horizontalRadius,mYCenter + verticalRadius);

                canvas.drawBitmap(bmp,src,dst,null);
            }
        }

        public void recycleBitmap() {
            if (bmp != null) {
                bmp.recycle();
            }
        }

        public void drawCenterImage(int resId){
            recycleBitmap();
            bmp = BitmapFactory.decodeResource(getResources(), resId);
            postInvalidate();
        }



    }
}
