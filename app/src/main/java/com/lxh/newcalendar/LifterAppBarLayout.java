package com.lxh.newcalendar;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuxiaohui on 4/7/16.
 */
//@CoordinatorLayout.DefaultBehavior(LifterAppBarLayout.Behavior.class)
public class LifterAppBarLayout extends AppBarLayout {
    private final List<OnFloorChangedListener> mFloorChangeListeners;

    public LifterAppBarLayout(Context context) {
        this(context, null);
    }

    public LifterAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFloorChangeListeners = new ArrayList();
    }


    /**
     * Interface definition for a callback to be invoked when an {@link AppBarLayout}'s vertical
     * offset changes.
     */
    public interface OnFloorChangedListener {
        void onFloorChanged(LifterAppBarLayout lifterAppBarLayout, int floorOffset);
    }


    /**
     * Add a listener that will be called when the offset of this {@link AppBarLayout} changes.
     *
     * @param listener The listener that will be called when the offset changes.]
     *
     * @see #removeOnFloorChangedListener(OnFloorChangedListener)
     */
    public void addOnFloorChangedListener(OnFloorChangedListener listener) {
        if (listener != null && !mFloorChangeListeners.contains(listener)) {
            mFloorChangeListeners.add(listener);
        }
    }

    /**
     * Remove the previously added {@link OnFloorChangedListener}.
     *
     * @param listener the listener to remove.
     */
    public void removeOnFloorChangedListener(OnFloorChangedListener listener) {
        if (listener != null) {
            mFloorChangeListeners.remove(listener);
        }
    }

    public static class Behavior extends AppBarLayout.Behavior {
        private static final int PENDING_ACTION_NONE = 0;
        private static final int PENDING_ACTION_UP = 1;
        private static final int PENDING_ACTION_DOWN = 2;
        private int mPendingAction = PENDING_ACTION_NONE;

        private boolean mWasNestedFlung;

        @Override
        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout abl, View target) {
            super.onStopNestedScroll(coordinatorLayout, abl, target);

            if (!mWasNestedFlung) {
                // If we haven't been flung then let's see if the current view has been set to snap
                changeFloorIfNeeded(coordinatorLayout, abl);
            }

            // Reset the flags
            mWasNestedFlung = false;
        }

        @Override
        public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {
            mWasNestedFlung = super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
            changeFloorIfNeeded(coordinatorLayout, child);
            return mWasNestedFlung;
        }

        private void changeFloorIfNeeded(CoordinatorLayout coordinatorLayout, AppBarLayout abl) {
            final int offset = abl.getTop();
            final LifterToolbarLayout offsetChild;
            LifterToolbarLayout lifterToolbarLayout = null;
            for (int i = 0; i < abl.getChildCount(); i++) {
                if (abl.getChildAt(i) instanceof LifterToolbarLayout) {
                    lifterToolbarLayout = (LifterToolbarLayout)abl.getChildAt(i);
                }
            }
            offsetChild = lifterToolbarLayout;
            if (offsetChild != null) {
                final LayoutParams lp = (LayoutParams) offsetChild.getLayoutParams();
                // We're set the snap, so animate the offset to the nearest edge
                int childTop = -offsetChild.getTop();
                int childBottom = -offsetChild.getBottom();

                // If the view is set only exit until it is collapsed, we'll abide by that
                if ((lp.getScrollFlags() & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
                        == LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) {
                    childBottom += ViewCompat.getMinimumHeight(offsetChild);
                }
//
//                final int newOffset = offset < (childBottom + childTop) / 2
//                        ? childBottom : childTop;
                int destinFloor = offset < (childBottom + childTop) / 2 ?
                        Math.max(0, lifterToolbarLayout.getCurrentFloorIndex() - 1) :
                        lifterToolbarLayout.getCurrentFloorIndex();
                lifterToolbarLayout.goToFloor(destinFloor);
                abl.requestLayout();
//                    animateOffsetTo(coordinatorLayout, abl,
//                            MathUtils.constrain(newOffset, -abl.getTotalScrollRange(), 0));
            }
        }
    }
}
