package com.lxh.newcalendar;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * Created by liuxiaohui on 4/6/16.
 *
 * StageCollapsingToolbarLayout is a wrapper for {@link LifterView} which implements a collapsing app bar.
 * It is designed to have a direct child of {@link LifterView}, which has several height stage,
 * and when the scroll view scroll or fling,
 * the {@link LifterView} can achieve a height stage at most 1 step from current stage.
 *
 * It is suggest that no child of this layout is below the {@link LifterView},
 * which makes the {@link LifterView} to stretch the layout
 *
 */
public class LifterToolbarLayout extends FrameLayout implements Lifter {

    private boolean mRefreshLifterView = true;
    private LifterView mLifterView;

    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;

    public LifterToolbarLayout(Context context) {
        this(context, null);
    }

    public LifterToolbarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LifterToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Add an OnOffsetChangedListener if possible
        final ViewParent parent = getParent();
        if (parent instanceof AppBarLayout) {
            if (mOnOffsetChangedListener == null) {
                mOnOffsetChangedListener = new OffsetUpdateListener();
            }
            ((AppBarLayout) parent).addOnOffsetChangedListener(mOnOffsetChangedListener);
        }

        // We're attached, so lets request an inset dispatch
        ViewCompat.requestApplyInsets(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        // Remove our OnOffsetChangedListener if possible and it exists
        final ViewParent parent = getParent();
        if (mOnOffsetChangedListener != null && parent instanceof AppBarLayout) {
            ((AppBarLayout) parent).removeOnOffsetChangedListener(mOnOffsetChangedListener);
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureLifterView();

        int height = getHeightWithMargins(mLifterView);
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);

        switch (specMode) {
            case MeasureSpec.EXACTLY:
                height = specSize;
                break;
        }
        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, specMode));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Finally, set our minimum height to enable proper AppBarLayout collapsing
        if (mLifterView != null) {
            setMinimumHeight(getMinHeightWithMargins(mLifterView));
        }
    }

    @Override
    public int getFloorCount() {
        if (mLifterView != null)
            return mLifterView.getFloorCount();
        return 1;
    }

    @Override
    public int getCurrentFloorIndex() {
        if (mLifterView != null)
            return mLifterView.getCurrentFloorIndex();
        return 0;
    }

    @Override
    public int getHeightForFloor(int floorIndex) {
        if (mLifterView != null) {
            int viewHeight = mLifterView.getHeightForFloor(floorIndex);
            return viewHeight + getMargins(mLifterView);
        }
        return getMeasuredHeight();
    }

    @Override
    public void goToFloor(int floorIndex) {
        if (mLifterView != null)
            mLifterView.goToFloor(floorIndex);
    }

    private static int getMargins(@NonNull final View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams mlp = (MarginLayoutParams) lp;
            return mlp.topMargin + mlp.bottomMargin;
        }
        return 0;
    }

    private static int getHeightWithMargins(@NonNull final View view) {
        int viewHeight;
        if (view instanceof LifterView) {
            viewHeight = ((LifterView) view).getCurrentFloorHeight();
        } else {
            viewHeight = view.getHeight();
        }
        return viewHeight + getMargins(view);
    }

    private static int getMinHeightWithMargins(@NonNull final View view) {
        int viewHeight;
        if (view instanceof LifterView) {
            viewHeight = ((LifterView) view).getLowerFloorHeight();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewHeight = view.getMinimumHeight();
            } else {
                viewHeight = view.getHeight();
            }
        }
        return viewHeight + getMargins(view);
    }

    private void ensureLifterView() {
        if (!mRefreshLifterView) {
            return;
        }

        if (mLifterView == null) {
            // Find a LifterView from direct child
            for (int i = 0, count = getChildCount(); i < count; i++) {
                final View child = getChildAt(i);
                if (child instanceof LifterView) {
                    mLifterView = (LifterView) child;
                    break;
                }
            }
        }
        mRefreshLifterView = false;
    }

    private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
        @Override
        public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {
            final int scrollRange = layout.getTotalScrollRange();

            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);

                if (child == mLifterView) {
                    // LifterView, adjust height
                    int stageHeight = mLifterView.getCurrentFloorHeight();
                    int targetHeight = stageHeight + verticalOffset;
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    lp.height = targetHeight;
                    mLifterView.setLayoutParams(lp);
                }
                child.offsetTopAndBottom(-verticalOffset);
            }
        }
    }
}
