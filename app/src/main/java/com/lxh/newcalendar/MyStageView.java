package com.lxh.newcalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Created by liuxiaohui on 4/6/16.
 */
public class MyStageView extends StageCollapsingView {

    private static final int STAGE_WEEK = BASE_STAGE_INDEX;
    private static final int STAGE_MONTH = BASE_STAGE_INDEX + 1;
    private static final int STAGE_FULL_SCREEN = BASE_STAGE_INDEX + 2;

    public MyStageView(Context context) {
        this(context, null);
    }

    public MyStageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyStageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setStageIndex(STAGE_MONTH);
    }

    @Override
    public void setStageIndex(int stageIndex) {
        super.setStageIndex(stageIndex);
        setBackgroundColor(getColorForStage(getStageIndex()));
        requestLayout();
    }

    @Override
    public int getStageCount() {
        return 3;
    }

    @Override
    public int getHeightForStage(int stageIndex) {
        switch (stageIndex) {
            case STAGE_WEEK:
                return 150;
            case STAGE_MONTH:
                return 800;
            default:
                return 1200;
        }
    }

    public static int getColorForStage(int stageIndex) {
        switch (stageIndex) {
            case STAGE_WEEK:
                return Color.BLUE;
            case STAGE_MONTH:
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
