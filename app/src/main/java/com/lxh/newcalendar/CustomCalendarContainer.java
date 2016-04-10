package com.lxh.newcalendar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.*;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LiuXiaoHui on 2016/4/9.
 */
@CoordinatorLayout.DefaultBehavior(CustomCalendarContainer.Behavior.class)
public class CustomCalendarContainer extends LinearLayout {

    private static final int PENDING_ACTION_NONE = 0x0;
    private static final int PENDING_ACTION_WEEK = 0x1;
    private static final int PENDING_ACTION_MONTH = 0x2;
    private static final int PENDING_ACTION_FULL_SCREEN = 0x3;
    private static final int PENDING_ACTION_ANIMATE_ENABLED = 0x5;

    /**
     * Interface definition for a callback to be invoked when an {@link CustomCalendarContainer}'s vertical
     * offset changes.
     */
    public interface OnOffsetChangedListener {
        /**
         * Called when the {@link CustomCalendarContainer}'s layout offset has been changed. This allows
         * child views to implement custom behavior based on the offset (for instance pinning a
         * view at a certain y value).
         *
         * @param calendarContainer the {@link CustomCalendarContainer} which offset has changed
         * @param verticalOffset the vertical offset for the parent {@link CustomCalendarContainer}, in px
         */
        void onOffsetChanged(CustomCalendarContainer calendarContainer, int verticalOffset);
    }

    private static final int INVALID_SCROLL_RANGE = -1;

    private int mTotalScrollRange = INVALID_SCROLL_RANGE;
    private int mDownPreScrollRange = INVALID_SCROLL_RANGE;
    private int mDownScrollRange = INVALID_SCROLL_RANGE;
    private int mUpPreScrollRange = INVALID_SCROLL_RANGE;
    private int mUpScrollRange = INVALID_SCROLL_RANGE;

//    boolean mHaveChildWithInterpolator;

//    private float mTargetElevation;

    private int mPendingAction = PENDING_ACTION_NONE;

    private int mCurrentMode = PENDING_ACTION_WEEK;

    private WindowInsetsCompat mLastInsets;

    private final List<CustomCalendarContainer.OnOffsetChangedListener> mListeners;

    public CustomCalendarContainer(Context context) {
        this(context, null);
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AppBarLayout,
//                0, R.style.Widget_Design_AppBarLayout);
//        mTargetElevation = a.getDimensionPixelSize(R.styleable.AppBarLayout_elevation, 0);
//        setBackgroundDrawable(a.getDrawable(R.styleable.AppBarLayout_android_background));
//        if (a.hasValue(R.styleable.AppBarLayout_expanded)) {
//            setExpanded(a.getBoolean(R.styleable.AppBarLayout_expanded, false));
//        }
//        a.recycle();
    }

    public CustomCalendarContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        mListeners = new ArrayList();

        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        return onWindowInsetChanged(insets);
                    }
                });
    }

    @Override
    public void setElevation(float elevation) {
        ViewCompat.setElevation(this, elevation);
    }

    /**
     * Add a listener that will be called when the offset of this {@link CustomCalendarContainer} changes.
     *
     * @param listener The listener that will be called when the offset changes.]
     *
     * @see #removeOnOffsetChangedListener(CustomCalendarContainer.OnOffsetChangedListener)
     */
    public void addOnOffsetChangedListener(CustomCalendarContainer.OnOffsetChangedListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     * Remove the previously added {@link CustomCalendarContainer.OnOffsetChangedListener}.
     *
     * @param listener the listener to remove.
     */
    public void removeOnOffsetChangedListener(CustomCalendarContainer.OnOffsetChangedListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        invalidateScrollRanges();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        invalidateScrollRanges();

//        mHaveChildWithInterpolator = false;
//        for (int i = 0, z = getChildCount(); i < z; i++) {
//            final View child = getChildAt(i);
//            final CustomCalendarContainer.LayoutParams childLp = (CustomCalendarContainer.LayoutParams) child.getLayoutParams();
//            final Interpolator interpolator = childLp.getScrollInterpolator();
//
//            if (interpolator != null) {
//                mHaveChildWithInterpolator = true;
//                break;
//            }
//        }
    }

    private void invalidateScrollRanges() {
        // Invalidate the scroll ranges
        mTotalScrollRange = INVALID_SCROLL_RANGE;
        mDownPreScrollRange = INVALID_SCROLL_RANGE;
        mDownScrollRange = INVALID_SCROLL_RANGE;
        mUpPreScrollRange = INVALID_SCROLL_RANGE;
        mUpScrollRange = INVALID_SCROLL_RANGE;
    }

    @Override
    public void setOrientation(int orientation) {
        if (orientation != VERTICAL) {
            throw new IllegalArgumentException("CustomCalendarContainer is always vertical and does"
                    + " not support horizontal orientation");
        }
        super.setOrientation(orientation);
    }

    /**
     * Sets this {@link CustomCalendarContainer} 's mode
     *
     * <p>As with {@link CustomCalendarContainer}'s scrolling, this method relies on this layout being a
     * direct child of a {@link CoordinatorLayout}.</p>
     *
     * @param mode mode of the Calendar
     *
     */
    public void setMode(int mode) {
        setMode(mode, ViewCompat.isLaidOut(this));
    }

    /**
     * Sets this {@link CustomCalendarContainer} 's mode
     *
     * <p>As with {@link CustomCalendarContainer}'s scrolling, this method relies on this layout being a
     * direct child of a {@link CoordinatorLayout}.</p>
     *
     * @param mode mode of the Calendar
     * @param animate Whether to animate to the new state
     *
     */
    public void setMode(int mode, boolean animate) {
        // TODO: not quiet right about the |
        mPendingAction = mode | (animate ? PENDING_ACTION_ANIMATE_ENABLED : 0);
        requestLayout();
    }
//
//    @Override
//    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
//        return p instanceof CustomCalendarContainer.LayoutParams;
//    }
//
//    @Override
//    protected CustomCalendarContainer.LayoutParams generateDefaultLayoutParams() {
//        return new CustomCalendarContainer.LayoutParams(CustomCalendarContainer.LayoutParams.MATCH_PARENT, CustomCalendarContainer.LayoutParams.WRAP_CONTENT);
//    }
//
//    @Override
//    public CustomCalendarContainer.LayoutParams generateLayoutParams(AttributeSet attrs) {
//        return new CustomCalendarContainer.LayoutParams(getContext(), attrs);
//    }
//
//    @Override
//    protected CustomCalendarContainer.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
//        if (p instanceof LinearLayout.LayoutParams) {
//            return new LayoutParams((LinearLayout.LayoutParams) p);
//        } else if (p instanceof MarginLayoutParams) {
//            return new LayoutParams((MarginLayoutParams) p);
//        }
//        return new CustomCalendarContainer.LayoutParams(p);
//    }

    /**
     * Returns the scroll range of all children.
     *
     * @return the scroll range in px
     */
    public final int getTotalScrollRange() {
        if (mTotalScrollRange != INVALID_SCROLL_RANGE) {
            return mTotalScrollRange;
        }

        int range = 0;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final CustomCalendarContainer.LayoutParams lp = (CustomCalendarContainer.LayoutParams) child.getLayoutParams();
            final int childHeight = child.getMeasuredHeight();

            if (child instanceof LifterView) {
                // We're set to scroll so add the child's height
                range += childHeight + lp.topMargin + lp.bottomMargin;
                range -= ViewCompat.getMinimumHeight(child);
            } else {
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                break;
            }
        }
        return mTotalScrollRange = Math.max(0, range - getTopInset());
    }

    private boolean hasScrollableChildren() {
        return getTotalScrollRange() != 0;
    }

    /**
     * Return the scroll range when scrolling up from a nested pre-scroll.
     */
    private int getUpNestedPreScrollRange() {
        return getTotalScrollRange();
    }

    /**
     * Return the scroll range when scrolling down from a nested pre-scroll.
     */
    private int getDownNestedPreScrollRange() {
        if (mDownPreScrollRange != INVALID_SCROLL_RANGE) {
            // If we already have a valid value, return it
            return mDownPreScrollRange;
        }

        int range = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            final CustomCalendarContainer.LayoutParams lp = (CustomCalendarContainer.LayoutParams) child.getLayoutParams();
            final int childHeight = child.getMeasuredHeight();
        }
        return mDownPreScrollRange = Math.max(0, range - getTopInset());
    }

    /**
     * Return the scroll range when scrolling down from a nested scroll.
     */
    private int getDownNestedScrollRange() {
        if (mDownScrollRange != INVALID_SCROLL_RANGE) {
            // If we already have a valid value, return it
            return mDownScrollRange;
        }

        int range = 0;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final CustomCalendarContainer.LayoutParams lp = (CustomCalendarContainer.LayoutParams) child.getLayoutParams();
            int childHeight = child.getMeasuredHeight();
            childHeight += lp.topMargin + lp.bottomMargin;

//            final int flags = lp.mScrollFlags;

            if (child instanceof LifterView) {
                // We're set to scroll so add the child's height
                range += childHeight;
                range -= ViewCompat.getMinimumHeight(child) + getTopInset();
                break;
            } else {
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                break;
            }
        }
        return mDownScrollRange = Math.max(0, range);
    }

    final int getMinimumHeightForVisibleOverlappingContent() {
        final int topInset = getTopInset();
        final int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight != 0) {
            // If this layout has a min height, use it (doubled)
            return (minHeight * 2) + topInset;
        }

        // Otherwise, we'll use twice the min height of our last child
        final int childCount = getChildCount();
        return childCount >= 1
                ? (ViewCompat.getMinimumHeight(getChildAt(childCount - 1)) * 2) + topInset
                : 0;
    }

    int getPendingAction() {
        return mPendingAction;
    }

    private void resetPendingAction() {
        mPendingAction = PENDING_ACTION_NONE;
    }

    private int getTopInset() {
        return mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
    }

    private WindowInsetsCompat onWindowInsetChanged(final WindowInsetsCompat insets) {
        WindowInsetsCompat newInsets = null;

        if (ViewCompat.getFitsSystemWindows(this)) {
            // If we're set to fit system windows, keep the insets
            newInsets = insets;
        }

        // If our insets have changed, keep them and invalidate the scroll ranges...
        if (newInsets != mLastInsets) {
            mLastInsets = newInsets;
            invalidateScrollRanges();
        }

        return insets;
    }

//    public static class LayoutParams extends LinearLayout.LayoutParams {
//
//        /** @hide */
//        @IntDef(flag=true, value={
//                SCROLL_FLAG_SCROLL,
//                SCROLL_FLAG_EXIT_UNTIL_COLLAPSED,
//                SCROLL_FLAG_ENTER_ALWAYS,
//                SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED,
//                SCROLL_FLAG_SNAP
//        })
//        @Retention(RetentionPolicy.SOURCE)
//        public @interface ScrollFlags {}
//
//        /**
//         * The view will be scroll in direct relation to scroll events. This flag needs to be
//         * set for any of the other flags to take effect. If any sibling views
//         * before this one do not have this flag, then this value has no effect.
//         */
//        public static final int SCROLL_FLAG_SCROLL = 0x1;
//
//        /**
//         * When exiting (scrolling off screen) the view will be scrolled until it is
//         * 'collapsed'. The collapsed height is defined by the view's minimum height.
//         *
//         * @see ViewCompat#getMinimumHeight(View)
//         * @see View#setMinimumHeight(int)
//         */
//        public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 0x2;
//
//        /**
//         * When entering (scrolling on screen) the view will scroll on any downwards
//         * scroll event, regardless of whether the scrolling view is also scrolling. This
//         * is commonly referred to as the 'quick return' pattern.
//         */
//        public static final int SCROLL_FLAG_ENTER_ALWAYS = 0x4;
//
//        /**
//         * An additional flag for 'enterAlways' which modifies the returning view to
//         * only initially scroll back to it's collapsed height. Once the scrolling view has
//         * reached the end of it's scroll range, the remainder of this view will be scrolled
//         * into view. The collapsed height is defined by the view's minimum height.
//         *
//         * @see ViewCompat#getMinimumHeight(View)
//         * @see View#setMinimumHeight(int)
//         */
//        public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 0x8;
//
//        /**
//         * Upon a scroll ending, if the view is only partially visible then it will be snapped
//         * and scrolled to it's closest edge. For example, if the view only has it's bottom 25%
//         * displayed, it will be scrolled off screen completely. Conversely, if it's bottom 75%
//         * is visible then it will be scrolled fully into view.
//         */
//        public static final int SCROLL_FLAG_SNAP = 0x10;
//
//        /**
//         * Internal flags which allows quick checking features
//         */
//        static final int FLAG_QUICK_RETURN = SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS;
//        static final int FLAG_SNAP = SCROLL_FLAG_SCROLL | SCROLL_FLAG_SNAP;
//
//        int mScrollFlags = SCROLL_FLAG_SCROLL;
//        Interpolator mScrollInterpolator;
//
//        public LayoutParams(Context c, AttributeSet attrs) {
//            super(c, attrs);
//            TypedArray a = c.obtainStyledAttributes(attrs, android.support.design.R.styleable.CustomCalendarContainer_LayoutParams);
//            mScrollFlags = a.getInt(android.support.design.R.styleable.CustomCalendarContainer_LayoutParams_layout_scrollFlags, 0);
//            a.recycle();
//        }
//
//        public LayoutParams(int width, int height) {
//            super(width, height);
//        }
//
//        public LayoutParams(int width, int height, float weight) {
//            super(width, height, weight);
//        }
//
//        public LayoutParams(ViewGroup.LayoutParams p) {
//            super(p);
//        }
//
//        public LayoutParams(MarginLayoutParams source) {
//            super(source);
//        }
//
//        public LayoutParams(LinearLayout.LayoutParams source) {
//            super(source);
//        }
//
//        public LayoutParams(CustomCalendarContainer.LayoutParams source) {
//            super(source);
//            mScrollFlags = source.mScrollFlags;
//            mScrollInterpolator = source.mScrollInterpolator;
//        }
//
//        /**
//         * Set the scrolling flags.
//         *
//         * @param flags bitwise int of {@link #SCROLL_FLAG_SCROLL},
//         *             {@link #SCROLL_FLAG_EXIT_UNTIL_COLLAPSED}, {@link #SCROLL_FLAG_ENTER_ALWAYS},
//         *             {@link #SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED} and {@link #SCROLL_FLAG_SNAP }.
//         *
//         * @see #getScrollFlags()
//         *
//         * @attr ref android.support.design.R.styleable#CustomCalendarContainer_LayoutParams_layout_scrollFlags
//         */
//        public void setScrollFlags(@CustomCalendarContainer.LayoutParams.ScrollFlags int flags) {
//            mScrollFlags = flags;
//        }
//
//        /**
//         * Returns the scrolling flags.
//         *
//         * @see #setScrollFlags(int)
//         *
//         * @attr ref android.support.design.R.styleable#CustomCalendarContainer_LayoutParams_layout_scrollFlags
//         */
//        @CustomCalendarContainer.LayoutParams.ScrollFlags
//        public int getScrollFlags() {
//            return mScrollFlags;
//        }
//    }

    /**
     * The default {@link Behavior} for {@link CustomCalendarContainer}. Implements the necessary nested
     * scroll handling with offsetting.
     */
    public static class Behavior extends ViewOffsetBehavior<CustomCalendarContainer> {

        private static final int INVALID_POINTER = -1;

        private Runnable mFlingRunnable;
        private ScrollerCompat mScroller;

        private boolean mIsBeingDragged;
        private int mActivePointerId = INVALID_POINTER;
        private int mLastMotionY;
        private int mTouchSlop = -1;
        private VelocityTracker mVelocityTracker;


        private static final int ANIMATE_OFFSET_DIPS_PER_SECOND = 300;
        private static final int INVALID_POSITION = -1;
        /**
         * Callback to allow control over any {@link CustomCalendarContainer} dragging.
         */
        public static abstract class DragCallback {
            /**
             * Allows control over whether the given {@link CustomCalendarContainer} can be dragged or not.
             *
             * <p>Dragging is defined as a direct touch on the CustomCalendarContainer with movement. This
             * call does not affect any nested scrolling.</p>
             *
             * @return true if we are in a position to scroll the CustomCalendarContainer via a drag, false
             *         if not.
             */
            public abstract boolean canDrag(@NonNull CustomCalendarContainer CustomCalendarContainer);
        }

        private int mOffsetDelta;

        private boolean mSkipNestedPreScroll;
        private boolean mWasNestedFlung;

        private ValueAnimator mAnimator;

        private int mOffsetToChildIndexOnLayout = INVALID_POSITION;
        private boolean mOffsetToChildIndexOnLayoutIsMinHeight;
        private float mOffsetToChildIndexOnLayoutPerc;

        private WeakReference<View> mLastNestedScrollingChildRef;
        private DragCallback mOnDragCallback;

        public Behavior() {}

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, CustomCalendarContainer child, int layoutDirection) {
            boolean handled = super.onLayoutChild(parent, child, layoutDirection);

            final int pendingAction = child.getPendingAction();
            if (pendingAction != PENDING_ACTION_NONE) {
                final boolean animate = (pendingAction & PENDING_ACTION_ANIMATE_ENABLED) != 0;
                if ((pendingAction & PENDING_ACTION_WEEK) != 0) {
                    final int offset = -child.getUpNestedPreScrollRange();
                    if (animate) {
                        animateOffsetTo(parent, child, offset);
                    } else {
                        setHeaderTopBottomOffset(parent, child, offset);
                    }
                } else if ((pendingAction & PENDING_ACTION_MONTH) != 0) {
                    if (animate) {
                        animateOffsetTo(parent, child, 0);
                    } else {
                        setHeaderTopBottomOffset(parent, child, 0);
                    }
                }
            } else if (mOffsetToChildIndexOnLayout >= 0) {
                View childView = child.getChildAt(mOffsetToChildIndexOnLayout);
                int offset = -childView.getBottom();
                if (mOffsetToChildIndexOnLayoutIsMinHeight) {
                    offset += ViewCompat.getMinimumHeight(childView);
                } else {
                    offset += Math.round(childView.getHeight() * mOffsetToChildIndexOnLayoutPerc);
                }
                setTopAndBottomOffset(offset);
            }

            // Finally reset any pending states
            child.resetPendingAction();
            mOffsetToChildIndexOnLayout = INVALID_POSITION;

            // We may have changed size, so let's constrain the top and bottom offset correctly,
            // just in case we're out of the bounds
            setTopAndBottomOffset(
                    constrain(getTopAndBottomOffset(), -child.getTotalScrollRange(), 0));

            // Make sure we update the elevation
            dispatchOffsetUpdates(child);

            return handled;
        }

        @Override
        public boolean onInterceptTouchEvent(CoordinatorLayout parent, CustomCalendarContainer child, MotionEvent ev) {
            if (mTouchSlop < 0) {
                mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
            }

            final int action = ev.getAction();

            // Shortcut since we're being dragged
            if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
                return true;
            }

            switch (MotionEventCompat.getActionMasked(ev)) {
                case MotionEvent.ACTION_DOWN: {
                    mIsBeingDragged = false;
                    final int x = (int) ev.getX();
                    final int y = (int) ev.getY();
                    if (canDragView(child) && parent.isPointInChildBounds(child, x, y)) {
                        mLastMotionY = y;
                        mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                        ensureVelocityTracker();
                    }
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    final int activePointerId = mActivePointerId;
                    if (activePointerId == INVALID_POINTER) {
                        // If we don't have a valid id, the touch down wasn't on content.
                        break;
                    }
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                    if (pointerIndex == -1) {
                        break;
                    }

                    final int y = (int) MotionEventCompat.getY(ev, pointerIndex);
                    final int yDiff = Math.abs(y - mLastMotionY);
                    if (yDiff > mTouchSlop) {
                        mIsBeingDragged = true;
                        mLastMotionY = y;
                    }
                    break;
                }

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP: {
                    mIsBeingDragged = false;
                    mActivePointerId = INVALID_POINTER;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    break;
                }
            }

            if (mVelocityTracker != null) {
                mVelocityTracker.addMovement(ev);
            }

            return mIsBeingDragged;
        }

        @Override
        public boolean onTouchEvent(CoordinatorLayout parent, CustomCalendarContainer child, MotionEvent ev) {
            if (mTouchSlop < 0) {
                mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
            }

            switch (MotionEventCompat.getActionMasked(ev)) {
                case MotionEvent.ACTION_DOWN: {
                    final int x = (int) ev.getX();
                    final int y = (int) ev.getY();

                    if (parent.isPointInChildBounds(child, x, y) && canDragView(child)) {
                        mLastMotionY = y;
                        mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                        ensureVelocityTracker();
                    } else {
                        return false;
                    }
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    final int activePointerIndex = MotionEventCompat.findPointerIndex(ev,
                            mActivePointerId);
                    if (activePointerIndex == -1) {
                        return false;
                    }

                    final int y = (int) MotionEventCompat.getY(ev, activePointerIndex);
                    int dy = mLastMotionY - y;

                    if (!mIsBeingDragged && Math.abs(dy) > mTouchSlop) {
                        mIsBeingDragged = true;
                        if (dy > 0) {
                            dy -= mTouchSlop;
                        } else {
                            dy += mTouchSlop;
                        }
                    }

                    if (mIsBeingDragged) {
                        mLastMotionY = y;
                        // We're being dragged so scroll the ABL
                        scroll(parent, child, dy, getMaxDragOffset(child), 0);
                    }
                    break;
                }

                case MotionEvent.ACTION_UP:
                    if (mVelocityTracker != null) {
                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000);
                        float yvel = VelocityTrackerCompat.getYVelocity(mVelocityTracker,
                                mActivePointerId);
                        fling(parent, child, -getScrollRangeForDragFling(child), 0, yvel);
                    }
                    // $FALLTHROUGH
                case MotionEvent.ACTION_CANCEL: {
                    mIsBeingDragged = false;
                    mActivePointerId = INVALID_POINTER;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    break;
                }
            }

            if (mVelocityTracker != null) {
                mVelocityTracker.addMovement(ev);
            }

            return true;
        }
        @Override
        public boolean onStartNestedScroll(CoordinatorLayout parent, CustomCalendarContainer child,
                                           View directTargetChild, View target, int nestedScrollAxes) {
            // Return true if we're nested scrolling vertically, and we have scrollable children
            // and the scrolling view is big enough to scroll
            final boolean started = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
                    && child.hasScrollableChildren()
                    && parent.getHeight() - directTargetChild.getHeight() <= child.getHeight();

            if (started && mAnimator != null) {
                // Cancel any offset animation
                mAnimator.cancel();
            }

            // A new nested scroll has started so clear out the previous ref
            mLastNestedScrollingChildRef = null;

            return started;
        }

        @Override
        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, CustomCalendarContainer child,
                                      View target, int dx, int dy, int[] consumed) {
            if (dy != 0 && !mSkipNestedPreScroll) {
                int min, max;
                if (dy < 0) {
                    // We're scrolling down
                    min = -child.getTotalScrollRange();
                    max = min + child.getDownNestedPreScrollRange();
                } else {
                    // We're scrolling up
                    min = -child.getUpNestedPreScrollRange();
                    max = 0;
                }
                consumed[1] = scroll(coordinatorLayout, child, dy, min, max);
            }
        }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, CustomCalendarContainer child,
                                   View target, int dxConsumed, int dyConsumed,
                                   int dxUnconsumed, int dyUnconsumed) {
            if (dyUnconsumed < 0) {
                // If the scrolling view is scrolling down but not consuming, it's probably be at
                // the top of it's content
                scroll(coordinatorLayout, child, dyUnconsumed,
                        -child.getDownNestedScrollRange(), 0);
                // Set the expanding flag so that onNestedPreScroll doesn't handle any events
                mSkipNestedPreScroll = true;
            } else {
                // As we're no longer handling nested scrolls, reset the skip flag
                mSkipNestedPreScroll = false;
            }
        }

        @Override
        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, CustomCalendarContainer abl,
                                       View target) {
            if (!mWasNestedFlung) {
                // If we haven't been flung then let's see if the current view has been set to snap
                snapToChildIfNeeded(coordinatorLayout, abl);
            }

            // Reset the flags
            mSkipNestedPreScroll = false;
            mWasNestedFlung = false;
            // Keep a reference to the previous nested scrolling child
            mLastNestedScrollingChildRef = new WeakReference(target);
        }

        @Override
        public boolean onNestedFling(final CoordinatorLayout coordinatorLayout,
                                     final CustomCalendarContainer child, View target, float velocityX, float velocityY,
                                     boolean consumed) {
            boolean flung = false;

            if (!consumed) {
                // It has been consumed so let's fling ourselves
                flung = fling(coordinatorLayout, child, -child.getTotalScrollRange(),
                        0, -velocityY);
            } else {
                // If we're scrolling up and the child also consumed the fling. We'll fake scroll
                // upto our 'collapsed' offset
                if (velocityY < 0) {
                    // We're scrolling down
                    final int targetScroll = -child.getTotalScrollRange()
                            + child.getDownNestedPreScrollRange();
                    if (getTopBottomOffsetForScrollingSibling() < targetScroll) {
                        // If we're currently not expanded more than the target scroll, we'll
                        // animate a fling
                        animateOffsetTo(coordinatorLayout, child, targetScroll);
                        flung = true;
                    }
                } else {
                    // We're scrolling up
                    final int targetScroll = -child.getUpNestedPreScrollRange();
                    if (getTopBottomOffsetForScrollingSibling() > targetScroll) {
                        // If we're currently not expanded less than the target scroll, we'll
                        // animate a fling
                        animateOffsetTo(coordinatorLayout, child, targetScroll);
                        flung = true;
                    }
                }
            }

            mWasNestedFlung = flung;
            return flung;
        }

        protected void layoutChild(CoordinatorLayout parent, CustomCalendarContainer child, int layoutDirection) {
            // Let the parent lay it out by default
            parent.onLayoutChild(child, layoutDirection);
        }

        int setHeaderTopBottomOffset(CoordinatorLayout parent, CustomCalendarContainer header, int newOffset,
                                     int minOffset, int maxOffset) {
            final int curOffset = getTopBottomOffsetForScrollingSibling();
            int consumed = 0;

            if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
                // If we have some scrolling range, and we're currently within the min and max
                // offsets, calculate a new offset
                newOffset = constrain(newOffset, minOffset, maxOffset);
                CustomCalendarContainer CustomCalendarContainer = (CustomCalendarContainer) header;
                if (curOffset != newOffset) {
                    final int interpolatedOffset = newOffset;

                    boolean offsetChanged = setTopAndBottomOffset(interpolatedOffset);

                    // Update how much dy we have consumed
                    consumed = curOffset - newOffset;
                    // Update the stored sibling offset
                    mOffsetDelta = newOffset - interpolatedOffset;

                    // Dispatch the updates to any listeners
                    dispatchOffsetUpdates(CustomCalendarContainer);
                }
            } else {
                // Reset the offset delta
                mOffsetDelta = 0;
            }

            return consumed;
        }

        int getTopBottomOffsetForScrollingSibling() {
            return getTopAndBottomOffset() + mOffsetDelta;
        }

        final int scroll(CoordinatorLayout coordinatorLayout, CustomCalendarContainer header,
                         int dy, int minOffset, int maxOffset) {
            return setHeaderTopBottomOffset(coordinatorLayout, header,
                    getTopBottomOffsetForScrollingSibling() - dy, minOffset, maxOffset);
        }

        final boolean fling(CoordinatorLayout coordinatorLayout, CustomCalendarContainer layout, int minOffset,
                            int maxOffset, float velocityY) {
            if (mFlingRunnable != null) {
                layout.removeCallbacks(mFlingRunnable);
                mFlingRunnable = null;
            }

            if (mScroller == null) {
                mScroller = ScrollerCompat.create(layout.getContext());
            }

            mScroller.fling(
                    0, getTopAndBottomOffset(), // curr
                    0, Math.round(velocityY), // velocity.
                    0, 0, // x
                    minOffset, maxOffset); // y

            if (mScroller.computeScrollOffset()) {
                mFlingRunnable = new FlingRunnable(coordinatorLayout, layout);
                ViewCompat.postOnAnimation(layout, mFlingRunnable);
                return true;
            } else {
                onFlingFinished(coordinatorLayout, layout);
                return false;
            }
        }



        /**
         * Called when a fling has finished, or the fling was initiated but there wasn't enough
         * velocity to start it.
         */
        void onFlingFinished(CoordinatorLayout parent, CustomCalendarContainer layout) {
            // At the end of a manual fling, check to see if we need to snap to the edge-child
            snapToChildIfNeeded(parent, layout);
        }

        /**
         * Return true if the view can be dragged.
         */
        boolean canDragView(CustomCalendarContainer view) {
            if (mOnDragCallback != null) {
                // If there is a drag callback set, it's in control
                return mOnDragCallback.canDrag(view);
            }

            // Else we'll use the default behaviour of seeing if it can scroll down
            if (mLastNestedScrollingChildRef != null) {
                // If we have a reference to a scrolling view, check it
                final View scrollingView = mLastNestedScrollingChildRef.get();
                return scrollingView != null && scrollingView.isShown()
                        && !ViewCompat.canScrollVertically(scrollingView, -1);
            } else {
                // Otherwise we assume that the scrolling view hasn't been scrolled and can drag.
                return true;
            }
        }

        int getMaxDragOffset(CustomCalendarContainer view) {
            return -view.getDownNestedScrollRange();
        }

        int getScrollRangeForDragFling(CustomCalendarContainer view) {
            return view.getTotalScrollRange();
        }


        private void ensureVelocityTracker() {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
        }

        private class FlingRunnable implements Runnable {
            private final CoordinatorLayout mParent;
            private final CustomCalendarContainer mLayout;

            FlingRunnable(CoordinatorLayout parent, CustomCalendarContainer layout) {
                mParent = parent;
                mLayout = layout;
            }

            @Override
            public void run() {
                if (mLayout != null && mScroller != null) {
                    if (mScroller.computeScrollOffset()) {
                        setHeaderTopBottomOffset(mParent, mLayout, mScroller.getCurrY());
                        // Post ourselves so that we run on the next animation
                        ViewCompat.postOnAnimation(mLayout, this);
                    } else {
                        onFlingFinished(mParent, mLayout);
                    }
                }
            }
        }

        int setHeaderTopBottomOffset(CoordinatorLayout parent, CustomCalendarContainer header, int newOffset) {
            return setHeaderTopBottomOffset(parent, header, newOffset,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        /**
         * Set a callback to control any {@link CustomCalendarContainer} dragging.
         *
         * @param callback the callback to use, or {@code null} to use the default behavior.
         */
        public void setDragCallback(@Nullable CustomCalendarContainer.Behavior.DragCallback callback) {
            mOnDragCallback = callback;
        }

        private void animateOffsetTo(final CoordinatorLayout coordinatorLayout,
                                     final CustomCalendarContainer child, final int offset) {
            final int currentOffset = getTopBottomOffsetForScrollingSibling();
            if (currentOffset == offset) {
                if (mAnimator != null && mAnimator.isRunning()) {
                    mAnimator.cancel();
                }
                return;
            }

            if (mAnimator == null) {
                mAnimator = new ValueAnimator();
                mAnimator.setInterpolator(new DecelerateInterpolator());
                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        // TODO: maybe not right
                        setHeaderTopBottomOffset(coordinatorLayout, child,
                                Integer.parseInt(mAnimator.getAnimatedValue().toString()));
                    }
                });
            } else {
                mAnimator.cancel();
            }

            // Set the duration based on the amount of dips we're travelling in
            final float distanceDp = Math.abs(currentOffset - offset) /
                    coordinatorLayout.getResources().getDisplayMetrics().density;
            mAnimator.setDuration(Math.round(distanceDp * 1000 / ANIMATE_OFFSET_DIPS_PER_SECOND));

            mAnimator.setIntValues(currentOffset, offset);
            mAnimator.start();
        }

        private View getChildOnOffset(CustomCalendarContainer abl, final int offset) {
            for (int i = 0, count = abl.getChildCount(); i < count; i++) {
                View child = abl.getChildAt(i);
                if (child.getTop() <= -offset && child.getBottom() >= -offset) {
                    return child;
                }
            }
            return null;
        }

        private void snapToChildIfNeeded(CoordinatorLayout coordinatorLayout, CustomCalendarContainer calendarContainer) {
            final int offset = getTopBottomOffsetForScrollingSibling();
            final View offsetChild = getChildOnOffset(calendarContainer, offset);
            if (offsetChild != null) {
                final CustomCalendarContainer.LayoutParams lp = (CustomCalendarContainer.LayoutParams) offsetChild.getLayoutParams();
                if (offsetChild instanceof LifterView) {
                    // We're set the snap, so animate the offset to the nearest edge
                    int childTop = -offsetChild.getTop();
                    int childBottom = -offsetChild.getBottom();

                    childBottom += ViewCompat.getMinimumHeight(offsetChild);

                    final int newOffset = offset < (childBottom + childTop) / 2
                            ? childBottom : childTop;
                    animateOffsetTo(coordinatorLayout, calendarContainer,
                            constrain(newOffset, -calendarContainer.getTotalScrollRange(), 0));
                }
            }
        }

        private void dispatchOffsetUpdates(CustomCalendarContainer layout) {
            final List<CustomCalendarContainer.OnOffsetChangedListener> listeners = layout.mListeners;

            // Iterate backwards through the list so that most recently added listeners
            // get the first chance to decide
            for (int i = 0, z = listeners.size(); i < z; i++) {
                final CustomCalendarContainer.OnOffsetChangedListener listener = listeners.get(i);
                if (listener != null) {
                    listener.onOffsetChanged(layout, getTopAndBottomOffset());
                }
            }
        }

        @Override
        public Parcelable onSaveInstanceState(CoordinatorLayout parent, CustomCalendarContainer CustomCalendarContainer) {
            final Parcelable superState = super.onSaveInstanceState(parent, CustomCalendarContainer);
            final int offset = getTopAndBottomOffset();

            // Try and find the first visible child...
            for (int i = 0, count = CustomCalendarContainer.getChildCount(); i < count; i++) {
                View child = CustomCalendarContainer.getChildAt(i);
                final int visBottom = child.getBottom() + offset;

                if (child.getTop() + offset <= 0 && visBottom >= 0) {
                    final SavedState ss = new SavedState(superState);
                    ss.firstVisibleChildIndex = i;
                    ss.firstVisibileChildAtMinimumHeight =
                            visBottom == ViewCompat.getMinimumHeight(child);
                    ss.firstVisibileChildPercentageShown = visBottom / (float) child.getHeight();
                    return ss;
                }
            }

            // Else we'll just return the super state
            return superState;
        }

        @Override
        public void onRestoreInstanceState(CoordinatorLayout parent, CustomCalendarContainer CustomCalendarContainer,
                                           Parcelable state) {
            if (state instanceof SavedState) {
                final SavedState ss = (SavedState) state;
                super.onRestoreInstanceState(parent, CustomCalendarContainer, ss.getSuperState());
                mOffsetToChildIndexOnLayout = ss.firstVisibleChildIndex;
                mOffsetToChildIndexOnLayoutPerc = ss.firstVisibileChildPercentageShown;
                mOffsetToChildIndexOnLayoutIsMinHeight = ss.firstVisibileChildAtMinimumHeight;
            } else {
                super.onRestoreInstanceState(parent, CustomCalendarContainer, state);
                mOffsetToChildIndexOnLayout = INVALID_POSITION;
            }
        }

        protected static class SavedState extends View.BaseSavedState {
            int firstVisibleChildIndex;
            float firstVisibileChildPercentageShown;
            boolean firstVisibileChildAtMinimumHeight;

            public SavedState(Parcel source, ClassLoader loader) {
                super(source);
                firstVisibleChildIndex = source.readInt();
                firstVisibileChildPercentageShown = source.readFloat();
                firstVisibileChildAtMinimumHeight = source.readByte() != 0;
            }

            public SavedState(Parcelable superState) {
                super(superState);
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                super.writeToParcel(dest, flags);
                dest.writeInt(firstVisibleChildIndex);
                dest.writeFloat(firstVisibileChildPercentageShown);
                dest.writeByte((byte) (firstVisibileChildAtMinimumHeight ? 1 : 0));
            }

            public static final Parcelable.Creator<SavedState> CREATOR =
                    ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
                        @Override
                        public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                            return new SavedState(source, loader);
                        }

                        @Override
                        public SavedState[] newArray(int size) {
                            return new SavedState[size];
                        }
                    });
        }
    }


    /**
     * Behavior which should be used by {@link View}s which can scroll vertically and support
     * nested scrolling to automatically scroll any {@link CustomCalendarContainer} siblings.
     */
    public static class ScrollingViewBehavior extends ViewOffsetBehavior {

        private final Rect mTempRect1 = new Rect();
        private final Rect mTempRect2 = new Rect();

        private int mVerticalLayoutGap = 0;
        private int mOverlayTop;

        public ScrollingViewBehavior() {}

        public ScrollingViewBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);

            final TypedArray a = context.obtainStyledAttributes(attrs,
                    android.support.design.R.styleable.ScrollingViewBehavior_Params);
            setOverlayTop(a.getDimensionPixelSize(
                    android.support.design.R.styleable.ScrollingViewBehavior_Params_behavior_overlapTop, 0));
            a.recycle();
        }

        @Override
        public boolean onMeasureChild(CoordinatorLayout parent, View child,
                                      int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec,
                                      int heightUsed) {
            final int childLpHeight = child.getLayoutParams().height;
            if (childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT
                    || childLpHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                // If the menu's height is set to match_parent/wrap_content then measure it
                // with the maximum visible height

                final List<View> dependencies = parent.getDependencies(child);
                final View header = findFirstDependency(dependencies);
                if (header != null) {
                    if (ViewCompat.getFitsSystemWindows(header)
                            && !ViewCompat.getFitsSystemWindows(child)) {
                        // If the header is fitting system windows then we need to also,
                        // otherwise we'll get CoL's compatible measuring
                        ViewCompat.setFitsSystemWindows(child, true);

                        if (ViewCompat.getFitsSystemWindows(child)) {
                            // If the set succeeded, trigger a new layout and return true
                            child.requestLayout();
                            return true;
                        }
                    }

                    if (ViewCompat.isLaidOut(header)) {
                        int availableHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec);
                        if (availableHeight == 0) {
                            // If the measure spec doesn't specify a size, use the current height
                            availableHeight = parent.getHeight();
                        }

                        final int height = availableHeight - header.getMeasuredHeight()
                                + getScrollRange(header);
                        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height,
                                childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT
                                        ? View.MeasureSpec.EXACTLY
                                        : View.MeasureSpec.AT_MOST);

                        // Now measure the scrolling view with the correct height
                        parent.onMeasureChild(child, parentWidthMeasureSpec,
                                widthUsed, heightMeasureSpec, heightUsed);

                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        protected void layoutChild(final CoordinatorLayout parent, final View child,
                                   final int layoutDirection) {
            final List<View> dependencies = parent.getDependencies(child);
            final View header = findFirstDependency(dependencies);

            if (header != null) {
                final CoordinatorLayout.LayoutParams lp =
                        (CoordinatorLayout.LayoutParams) child.getLayoutParams();
                final Rect available = mTempRect1;
                available.set(parent.getPaddingLeft() + lp.leftMargin,
                        header.getBottom() + lp.topMargin,
                        parent.getWidth() - parent.getPaddingRight() - lp.rightMargin,
                        parent.getHeight() + header.getBottom()
                                - parent.getPaddingBottom() - lp.bottomMargin);

                final Rect out = mTempRect2;
                GravityCompat.apply(resolveGravity(lp.gravity), child.getMeasuredWidth(),
                        child.getMeasuredHeight(), available, out, layoutDirection);

                final int overlap = getOverlapPixelsForOffset(header);

                child.layout(out.left, out.top - overlap, out.right, out.bottom - overlap);
                mVerticalLayoutGap = out.top - header.getBottom();
            } else {
                // If we don't have a dependency, let super handle it
                super.layoutChild(parent, child, layoutDirection);
                mVerticalLayoutGap = 0;
            }
        }

        final int getOverlapPixelsForOffset(final View header) {
            return mOverlayTop == 0
                    ? 0
                    : constrain(Math.round(getOverlapRatioForOffset(header) * mOverlayTop),
                    0, mOverlayTop);

        }

        private static int resolveGravity(int gravity) {
            return gravity == Gravity.NO_GRAVITY ? GravityCompat.START | Gravity.TOP : gravity;
        }


        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
            // We depend on any CustomCalendarContainers
            return dependency instanceof CustomCalendarContainer;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, View child,
                                              View dependency) {
            offsetChildAsNeeded(parent, child, dependency);
            return false;
        }

        private void offsetChildAsNeeded(CoordinatorLayout parent, View child, View dependency) {
            final CoordinatorLayout.Behavior behavior =
                    ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
            if (behavior instanceof CustomCalendarContainer.Behavior) {
                // Offset the child, pinning it to the bottom the header-dependency, maintaining
                // any vertical gap, and overlap
                final CustomCalendarContainer.Behavior ablBehavior = (CustomCalendarContainer.Behavior) behavior;
                final int offset = ablBehavior.getTopBottomOffsetForScrollingSibling();
                child.offsetTopAndBottom((dependency.getBottom() - child.getTop())
                        + ablBehavior.mOffsetDelta
                        + getVerticalLayoutGap()
                        - getOverlapPixelsForOffset(dependency));
            }
        }

        float getOverlapRatioForOffset(final View header) {
            if (header instanceof CustomCalendarContainer) {
                final CustomCalendarContainer abl = (CustomCalendarContainer) header;
                final int totalScrollRange = abl.getTotalScrollRange();
                final int preScrollDown = abl.getDownNestedPreScrollRange();
                final int offset = getCustomCalendarContainerOffset(abl);

                if (preScrollDown != 0 && (totalScrollRange + offset) <= preScrollDown) {
                    // If we're in a pre-scroll down. Don't use the offset at all.
                    return 0;
                } else {
                    final int availScrollRange = totalScrollRange - preScrollDown;
                    if (availScrollRange != 0) {
                        // Else we'll use a interpolated ratio of the overlap, depending on offset
                        return 1f + (offset / (float) availScrollRange);
                    }
                }
            }
            return 0f;
        }
        /**
         * The gap between the top of the scrolling view and the bottom of the header layout in pixels.
         */
        final int getVerticalLayoutGap() {
            return mVerticalLayoutGap;
        }

        /**
         * Set the distance that this view should overlap any {@link CustomCalendarContainer}.
         *
         * @param overlayTop the distance in px
         */
        public final void setOverlayTop(int overlayTop) {
            mOverlayTop = overlayTop;
        }

        /**
         * Returns the distance that this view should overlap any {@link CustomCalendarContainer}.
         */
        public final int getOverlayTop() {
            return mOverlayTop;
        }

        private static int getCustomCalendarContainerOffset(CustomCalendarContainer abl) {
            final CoordinatorLayout.Behavior behavior =
                    ((CoordinatorLayout.LayoutParams) abl.getLayoutParams()).getBehavior();
            if (behavior instanceof CustomCalendarContainer.Behavior) {
                return ((CustomCalendarContainer.Behavior) behavior).getTopBottomOffsetForScrollingSibling();
            }
            return 0;
        }

        View findFirstDependency(List<View> views) {
            for (int i = 0, z = views.size(); i < z; i++) {
                View view = views.get(i);
                if (view instanceof CustomCalendarContainer) {
                    return view;
                }
            }
            return null;
        }

        int getScrollRange(View v) {
            if (v instanceof CustomCalendarContainer) {
                return ((CustomCalendarContainer) v).getTotalScrollRange();
            } else {
                return v.getMeasuredHeight();
            }
        }
    }

    static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }
}
