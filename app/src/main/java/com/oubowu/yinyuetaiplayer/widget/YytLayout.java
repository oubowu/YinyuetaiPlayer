package com.oubowu.yinyuetaiplayer.widget;

import android.content.Context;
import android.support.v4.view.ViewGroupCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by Oubowu on 2016/12/26 13:58.
 */
public class YytLayout extends LinearLayout {

    private static final int MIN_FLING_VELOCITY = 400;
    private ViewDragHelper mDragHelper;

    // 拖动的宽度
    private int mDragWidth;
    // 拖动的高度
    private int mDragHeight;

    // 响应滑动做缩放的View
    private View mFlexView;
    // 与mFlexView联动的View
    private View mFollowView;

    public void setDragEnable(boolean dragEnable) {
        mIsDragEnable = dragEnable;
    }

    private boolean mIsDragEnable;

    public boolean isHorizontalEnable() {
        return mHorizontalEnable;
    }

    // 水平滑动与否的标志位
    private boolean mHorizontalEnable;

    // 是否正在关闭页面的标志位
    private boolean mIsClosing;

    private OnLayoutStateListener mOnLayoutStateListener;

    private int mFlexWidth;
    private int mFlexHeight;

    private float mScaleRatio = 1;

    private boolean mInFlexViewTouchRange;

    private int mScaleOffset;

    public YytLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        // 垂直布局
        setOrientation(VERTICAL);

        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;

        ViewGroupCompat.setMotionEventSplittingEnabled(this, false);
        FlexCallback flexCallback = new FlexCallback();
        mDragHelper = ViewDragHelper.create(this, 1.0f, flexCallback);
        // 最小拖动速度
        mDragHelper.setMinVelocity(minVel);

        post(new Runnable() {
            @Override
            public void run() {

                // 需要添加的两个子View，其中mFlexView作为滑动的响应View，mLinkView作为跟随View
                mFlexView = getChildAt(0);
                mFollowView = getChildAt(1);

                mDragHeight = getMeasuredHeight() - mFlexView.getMeasuredHeight();

                mFlexWidth = mFlexView.getMeasuredWidth();
                mFlexHeight = mFlexView.getMeasuredHeight();

            }
        });

    }

    private class FlexCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // Log.e("FlexCallback", "74行-tryCaptureView(): " + " " + child);
            return mFlexView == child;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return Math.max(Math.min(mDragWidth, left), -mDragWidth);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mDragWidth * 2;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return Math.max(Math.min(mDragHeight, top), 0);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mDragHeight;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {

            if (mHorizontalEnable) {
                // 如果水平滑动有效，首先根据滑动的速度决定关闭页面，方向根据速度正负决定
                if (xvel > 1500) {
                    mDragHelper.settleCapturedViewAt(mDragWidth, mDragHeight);
                    invalidate();
                    mIsClosing = true;
                    return;
                } else if (xvel < -1500) {
                    mDragHelper.settleCapturedViewAt(-mDragWidth, mDragHeight);
                    invalidate();
                    mIsClosing = true;
                    return;
                }

                // 速度没到关闭页面的要求，根据透明度来决定关闭页面，方向根据releasedChild.getLeft()正负决定
                float alpha = releasedChild.getAlpha();
                if (releasedChild.getLeft() < 0) {
                    if (alpha <= 0.4f) {
                        mDragHelper.settleCapturedViewAt(-mDragWidth, mDragHeight);
                        invalidate();
                        mIsClosing = true;
                        return;
                    }
                } else if (alpha <= 0.4f) {
                    mDragHelper.settleCapturedViewAt(mDragWidth, mDragHeight);
                    invalidate();
                    mIsClosing = true;
                    return;
                }

            }

            // 根据垂直方向的速度正负决定布局的展示方式
            if (yvel > 1500) {
                mDragHelper.settleCapturedViewAt(0, mDragHeight);
                invalidate();
                return;
            } else if (yvel < -1500) {
                mDragHelper.settleCapturedViewAt(0, 0);
                invalidate();
                return;
            }

            // 根据releasedChild.getTop()决定布局的展示方式
            if (releasedChild.getTop() <= mDragHeight / 2) {
                mDragHelper.settleCapturedViewAt(0, 0);
            } else {
                mDragHelper.settleCapturedViewAt(0, mDragHeight);
            }

            invalidate();
        }

        @Override
        public void onViewPositionChanged(final View changedView, int left, int top, int dx, int dy) {

            float fraction = top * 1.0f / mDragHeight;

            // mFlexView缩放的比率
            mScaleRatio = 1 - 0.5f * fraction;
            mScaleOffset = changedView.getWidth() / 20;
            // 设置缩放基点
            changedView.setPivotX(changedView.getWidth() - mScaleOffset);
            changedView.setPivotY(changedView.getHeight() - mScaleOffset);
            changedView.setScaleX(mScaleRatio);
            changedView.setScaleY(mScaleRatio);

            // mLinkView透明度的比率
            float alphaRatio = 1 - fraction;
            mFollowView.setAlpha(alphaRatio);
            // 根据垂直方向的dy设置top，产生跟随的效果
            mFollowView.setTop(mFollowView.getTop() + dy);

            // 通过alphaRatio作为水平滑动的基准
            mHorizontalEnable = alphaRatio <= 0.05;

            if (mHorizontalEnable) {
                // 如果水平滑动允许的话，由于设置缩放不会影响mFlexView的宽高，所以水平滑动距离为mFlexView宽度一半
                mDragWidth = (int) (changedView.getMeasuredWidth() * 0.5f);

                // 设置mFlexView的透明度，这里向左右水平滑动透明度都随之变化
                changedView.setAlpha(1 - Math.abs(left) * 1.0f / mDragWidth);

            } else {
                // 不是水平滑动的处理
                changedView.setAlpha(1);
                mDragWidth = 0;
            }

            if (mOnLayoutStateListener != null) {
                mOnLayoutStateListener.onScroll();
            }

        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        // Log.e("YytLayout", mFlexView.getLeft() + ";" + mFlexView.getTop() + " --- " + ev.getX() + ":" + ev.getY());

        // 由于缩放不会影响mFlexView真实宽高，这里手动计算实际的范围
        float left = mFlexView.getLeft() + mFlexWidth * (1 - mScaleRatio) - mScaleOffset * (1 - mScaleRatio);
        float top = mFlexView.getTop() + mFlexHeight * (1 - mScaleRatio) - mScaleOffset * (1 - mScaleRatio);

        // 这里所做的是判断手指是否落在mFlexView真实范围内
        mInFlexViewTouchRange = ev.getX() >= left && ev.getY() >= top;

        if (mInFlexViewTouchRange) {

            return mIsDragEnable && mDragHelper.shouldInterceptTouchEvent(ev);

        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mInFlexViewTouchRange) {
            // 这里还要做判断是因为，即使我不阻断事件，但是此Layout的子View不消费的话，事件还是给回此Layout
            mDragHelper.processTouchEvent(event);
            return true;
        } else {
            // 不在mFlexView触摸范围内，并且子View没有消费，返回false，把事件传递下去
            return false;
        }
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            invalidate();
        } else if (mIsClosing && mOnLayoutStateListener != null) {
            // 正在关闭的情况下，并且滑动结束后，告知将要关闭页面
            mOnLayoutStateListener.onClose();
            mIsClosing = false;
        }
    }

    /**
     * 监听布局是否在垂直滑动或者水平滑动关闭
     */
    public interface OnLayoutStateListener {

        void onClose();

        void onScroll();
    }

    public void setOnLayoutStateListener(OnLayoutStateListener onLayoutStateListener) {
        mOnLayoutStateListener = onLayoutStateListener;
    }

    /**
     * 展开布局
     */
    public void expand() {
        mDragHelper.smoothSlideViewTo(mFlexView, 0, 0);
        invalidate();
    }

}
