package com.lxh.newcalendar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * Created by liuxiaohui on 4/6/16.
 *
 * StageCollapsingToolbarLayout is a wrapper for {@link StageCollapsingView} which implements a collapsing app bar.
 * It is designed to have a direct child of {@link StageCollapsingView}, which has several height stage,
 * and when the scroll view scroll or fling,
 * the {@link StageCollapsingView} can achieve a height stage at most 1 step from current stage.
 *
 * It is suggest that no child of this layout is below the {@link StageCollapsingView},
 * which makes the {@link StageCollapsingView} to stretch the layout
 *
 */
public class StageCollapsingToolbarLayout extends FrameLayout {

    private boolean mRefreshStageCollapsingView = true;
    private StageCollapsingView mStageCollapsingView;

    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;

    public StageCollapsingToolbarLayout(Context context) {
        this(context, null);
    }

    public StageCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StageCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
        ensureStageCollapsingView();
        int height = getHeightWithMargins(mStageCollapsingView);
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
        if (mStageCollapsingView != null) {
            setMinimumHeight(getMinHeightWithMargins(mStageCollapsingView));
        }
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
        if (view instanceof StageCollapsingView) {
            viewHeight = ((StageCollapsingView) view).getCurrentStageHeight();
        } else {
            viewHeight = view.getHeight();
        }
        return viewHeight + getMargins(view);
    }

    private static int getMinHeightWithMargins(@NonNull final View view) {
        int viewHeight;
        if (view instanceof StageCollapsingView) {
            viewHeight = ((StageCollapsingView) view).getLowerStageHeight();
        } else {
            viewHeight = view.getMinimumHeight();
        }
        return viewHeight + getMargins(view);
    }

    private void ensureStageCollapsingView() {
        if (!mRefreshStageCollapsingView) {
            return;
        }

        if (mStageCollapsingView == null) {
            // Find a StageCollapsingView from direct child
            for (int i = 0, count = getChildCount(); i < count; i++) {
                final View child = getChildAt(i);
                if (child instanceof StageCollapsingView) {
                    mStageCollapsingView = (StageCollapsingView) child;
                    break;
                }
            }
        }
        mRefreshStageCollapsingView = false;
    }

    private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
        @Override
        public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {
            final int scrollRange = layout.getTotalScrollRange();

            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);

                if (child == mStageCollapsingView) {
                    // StageCollapsingView, adjust height
                    int stageHeight = mStageCollapsingView.getCurrentStageHeight();
                    int targetHeight = stageHeight + verticalOffset;
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    lp.height = targetHeight;
                    mStageCollapsingView.setLayoutParams(lp);
                }
                child.offsetTopAndBottom(-verticalOffset);
            }
        }
    }
}
