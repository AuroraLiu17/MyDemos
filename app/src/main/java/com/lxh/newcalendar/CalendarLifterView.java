package com.lxh.newcalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Created by liuxiaohui on 4/6/16.
 */
public class CalendarLifterView extends LifterView {

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
}
