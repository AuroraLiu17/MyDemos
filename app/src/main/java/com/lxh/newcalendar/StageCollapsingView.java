package com.lxh.newcalendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by liuxiaohui on 4/6/16.
 */
public abstract class StageCollapsingView extends View {

    protected static final int BASE_STAGE_INDEX = 0;
    protected static final int DEFAULT_COUNT = 1;

    protected int mStageIndex = BASE_STAGE_INDEX;

    /**
     * It is prefered tha the stage count is a constant value that >= 1
     * @return stageCount of this view
     */
    public int getStageCount() {
        return DEFAULT_COUNT;
    }

    public int getStageIndex() {
        return mStageIndex;
    }

    public void setStageIndex(int stageIndex) {
        if (mStageIndex != stageIndex) {
            mStageIndex = stageIndex;
        }
    }

    public abstract int getHeightForStage(int stageIndex);

    public int getCurrentStageHeight() {
        return getHeightForStage(getStageIndex());
    }

    public int getLowerStageHeight() {
        int lowerStageIndex = BASE_STAGE_INDEX;
        if (mStageIndex > BASE_STAGE_INDEX) {
            lowerStageIndex = mStageIndex - 1;
        }
        return getHeightForStage(lowerStageIndex);
    }

    public int getHigherStageHeight() {
        int higherStageIndex = getStageCount() - 1;
        if (mStageIndex < getStageCount() - 2) {
            higherStageIndex = mStageIndex + 1;
        }
        return getHeightForStage(higherStageIndex);
    }

    public StageCollapsingView(Context context) {
        this(context, null);
    }

    public StageCollapsingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StageCollapsingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getCurrentStageHeight();
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                height = specSize;
                break;
        }
        setMeasuredDimension(widthMeasureSpec, height);
    }
}
