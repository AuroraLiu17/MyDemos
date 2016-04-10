package com.lxh.newcalendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by liuxiaohui on 4/6/16.
 */
public abstract class LifterView extends View implements Lifter {

    protected static final int BASE_FLOOR_INDEX = 0;
    protected static final int DEFAULT_COUNT = 1;

    protected int mFloorIndex = BASE_FLOOR_INDEX;

    @Override
    public int getFloorCount() {
        return DEFAULT_COUNT;
    }

    @Override
    public int getCurrentFloorIndex() {
        return mFloorIndex;
    }

    @Override
    public int getLowerFloor() {
        int lowerFloorIndex = BASE_FLOOR_INDEX;
        if (mFloorIndex > BASE_FLOOR_INDEX) {
            lowerFloorIndex = mFloorIndex - 1;
        }
        return lowerFloorIndex;
    }

    @Override
    public int getHigherFloor() {
        int higherFloorIndex = getFloorCount() - 1;
        if (mFloorIndex < getFloorCount() - 2) {
            higherFloorIndex = mFloorIndex + 1;
        }
        return higherFloorIndex;
    }

    @Override
    public void goToFloor(int floorIndex) {
        if (floorIndex < 0 || floorIndex >= getFloorCount()) {
            throw new IndexOutOfBoundsException("Total floor count " + getFloorCount() + ", go to floor " + floorIndex);
        }
        if (mFloorIndex != floorIndex) {
            mFloorIndex = floorIndex;
        }
    }

    public int getCurrentFloorHeight() {
        return getHeightForFloor(getCurrentFloorIndex());
    }

    public int getLowerFloorHeight() {
        return getHeightForFloor(getLowerFloor());
    }

    public int getHigherFloorHeight() {
        return getHeightForFloor(getHigherFloor());
    }

    public LifterView(Context context) {
        this(context, null);
    }

    public LifterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LifterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getCurrentFloorHeight();
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
