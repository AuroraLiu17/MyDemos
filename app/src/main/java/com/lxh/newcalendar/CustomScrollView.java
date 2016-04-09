package com.lxh.newcalendar;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Scroller;

/**
 * Created by liuxiaohui on 4/8/16.
 */
public class CustomScrollView extends ImageView implements GestureDetector.OnGestureListener {

    private Scroller mScroller;
    private GestureDetector mGestureDetector;

    private static final int[] mColors = new int[] {
        Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.BLACK};
    private int mCurrentColor = 0;

    public CustomScrollView(Context context) {
        this(context, null);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context, new AccelerateDecelerateInterpolator());
        mGestureDetector = new GestureDetector(context, this);
        updateBG();
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction() & MotionEvent.ACTION_MASK;
                if (action == MotionEvent.ACTION_UP ||
                        action == MotionEvent.ACTION_CANCEL) {
                    updateElevation(5);
                }
                return mGestureDetector.onTouchEvent(event);
            }
        });
    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        return mGestureDetector.onTouchEvent(event);
//    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        mCurrentColor = (mCurrentColor + 1) % mColors.length;
        updateBG();
        scrollTo((mCurrentColor + 1) % mColors.length  * 100, (mCurrentColor + 1) % mColors.length * 50);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        updateElevation(10);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        smoothScrollBy(-(int)distanceX, -(int)distanceY);

        Log.e("GD", "onScroll:" + -(int)distanceX +" ," + -(int)distanceY);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.e("GD", "onFling:" + velocityX +" ," + velocityY);
        mScroller.fling(mScroller.getFinalX(), mScroller.getFinalY(), (int)velocityX, (int)velocityY,
                0, 300, 0, 600);
        postInvalidate();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        updateElevation(20);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    private void updateElevation(int elevation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(elevation);
        }
    }

    private void updateBG() {
        setBackgroundColor(mColors[mCurrentColor]);
    }

    //调用此方法滚动到目标位置
    public void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    //调用此方法设置滚动的相对偏移
    public void smoothScrollBy(int dx, int dy) {
        //设置mScroller的滚动偏移量
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }

//    @Override
//    public void computeScroll() {
//        if (mScroller.computeScrollOffset()) {
//            //这里调用View的scrollTo()完成实际的滚动
//            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
//            Log.e("GD", "scrollTo:" + mScroller.getCurrX() +" ," + mScroller.getCurrY()
//                    +" ," + getLeft() +" ," + getTop());
////            Log.e("GD", "offsetTop:" + (mScroller.getCurrY() - getTop()));
////            offsetTopAndBottom(mScroller.getCurrY() - getTop());
////            Log.e("GD", "offsetLeft:" + (mScroller.getCurrX() - getLeft()));
////            offsetLeftAndRight(mScroller.getCurrX() - getLeft());
//            //必须调用该方法，否则不一定能看到滚动效果
//            postInvalidate();
//        }
//        super.computeScroll();
//    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }
}
