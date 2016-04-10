package com.lxh.newcalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Scroller;

/**
 * Created by liuxiaohui on 4/6/16.
 */
public class CalendarLifterView extends LifterView {

    private Scroller mScroller;

    private static final int FLOOR_WEEK = BASE_FLOOR_INDEX;
    private static final int FLOOR_MONTH = BASE_FLOOR_INDEX + 1;
    private static final int FLOOR_FULL_SCREEN = BASE_FLOOR_INDEX + 2;

    public CalendarLifterView(Context context) {
        this(context, null);
    }

    public CalendarLifterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarLifterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        goToFloor(FLOOR_FULL_SCREEN);
        mScroller = new Scroller(context, new AccelerateDecelerateInterpolator());
    }

    @Override
    public void goToFloor(int floorIndex) {
        super.goToFloor(floorIndex);
        setBackgroundColor(getColorForFloor(getCurrentFloorIndex()));
        requestLayout();
    }

    @Override
    public int getFloorCount() {
        return 3;
    }

    @Override
    public int getHeightForFloor(int floorIndex) {
        if (floorIndex < 0 || floorIndex >= getFloorCount()) {
            throw new IndexOutOfBoundsException("Total floor count " + getFloorCount() + ", floorIndex " + floorIndex);
        }
        switch (floorIndex) {
            case FLOOR_WEEK:
                return 150;
            case FLOOR_MONTH:
                return 800;
            default:
                return 1200;
        }
    }

    public static int getColorForFloor(int floorIndex) {
        switch (floorIndex) {
            case FLOOR_WEEK:
                return Color.BLUE;
            case FLOOR_MONTH:
                return Color.RED;
            default:
                return Color.GREEN;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        int gapHeight = getHeight() / 10;
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int startY = 0;
        int alpha = 1;
        for (; startY < height; startY += gapHeight, alpha *= -1) {
            paint.setAlpha(155 - alpha * 50);
            canvas.drawRect(0, startY, width, startY + gapHeight, paint);
        }
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

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            //这里调用View的scrollTo()完成实际的滚动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //必须调用该方法，否则不一定能看到滚动效果
            postInvalidate();
        }
        super.computeScroll();
    }

}
