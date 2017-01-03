package com.oubowu.yinyuetaiplayer.widget;

import android.content.Context;
import android.support.v4.view.ViewGroupCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Oubowu on 201612/26 13:58.<p>
 * 实现了布局交互的容器
 */
public class YytLayout extends ViewGroup {

    private static final int MIN_FLING_VELOCITY = 400;
    private ViewDragHelper mDragHelper;

    // 拖动的宽度
    private int mDragWidth;
    // 拖动的高度
    private int mDragHeight;

    // 响应拖动做缩放的View
    private View mFlexView;
    // 与mFlexView联动做透明度渐变的View
    private View mFollowView;

    // 响应拖动做缩放的View保存的位置
    private ChildLayoutPosition mFlexLayoutPosition;
    // 与mFlexView联动的View保存的位置
    private ChildLayoutPosition mFollowLayoutPosition;

    // 水平拖动与否的标志位
    private boolean mHorizontalDragEnable;

    public boolean isHorizontalDragEnable() {
        return mHorizontalDragEnable;
    }

    // 垂直拖动与否的标志位
    private boolean mVerticalDragEnable = true;

    // 是否正在关闭页面的标志位
    private boolean mIsClosing;

    // 监听布局是否水平拖动关闭了
    private OnLayoutStateListener mOnLayoutStateListener;

    // 做拖放缩放的子View的宽度
    private int mFlexWidth;
    // 做拖放缩放的子View的高度
    private int mFlexHeight;

    // mFlexView缩放的比率
    private float mFlexScaleRatio = 1;

    // mFlexView缩放的基准点的偏移值
    private int mFlexScaleOffset;

    // 触摸事件是否发生在mFlexView的区域
    private boolean mInFlexViewTouchRange;

    public YytLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

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

                // 需要添加的两个子View，其中mFlexView作为拖动的响应View，mLinkView作为跟随View
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
            // mFlexView来响应触摸事件
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
            if (!mVerticalDragEnable) {
                // 不允许垂直拖动的时候是mFlexView在底部水平拖动一定距离时设置的，返回mDragHeight就不能再垂直做拖动了
                return mDragHeight;
            }
            return Math.max(Math.min(mDragHeight, top), 0);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mDragHeight;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {

            if (mHorizontalDragEnable) {
                // 如果水平拖动有效，首先根据拖动的速度决定关闭页面，方向根据速度正负决定
                if (xvel > 1500) {
                    mDragHelper.settleCapturedViewAt(mDragWidth, mDragHeight);
                    mIsClosing = true;
                } else if (xvel < -1500) {
                    mDragHelper.settleCapturedViewAt(-mDragWidth, mDragHeight);
                    mIsClosing = true;
                } else {
                    // 速度没到关闭页面的要求，根据透明度来决定关闭页面，方向根据releasedChild.getLeft()正负决定
                    float alpha = releasedChild.getAlpha();
                    if (releasedChild.getLeft() < 0 && alpha <= 0.4f) {
                        mDragHelper.settleCapturedViewAt(-mDragWidth, mDragHeight);
                        mIsClosing = true;
                    } else if (releasedChild.getLeft() > 0 && alpha <= 0.4f) {
                        mDragHelper.settleCapturedViewAt(mDragWidth, mDragHeight);
                        mIsClosing = true;
                    } else {
                        mDragHelper.settleCapturedViewAt(0, mDragHeight);
                    }
                }
            } else {
                // 根据垂直方向的速度正负决定布局的展示方式
                if (yvel > 1500) {
                    mDragHelper.settleCapturedViewAt(0, mDragHeight);
                } else if (yvel < -1500) {
                    mDragHelper.settleCapturedViewAt(0, 0);
                } else {
                    // 根据releasedChild.getTop()决定布局的展示方式
                    if (releasedChild.getTop() <= mDragHeight / 2) {
                        mDragHelper.settleCapturedViewAt(0, 0);
                    } else {
                        mDragHelper.settleCapturedViewAt(0, mDragHeight);
                    }
                }
            }
            invalidate();
        }

        @Override
        public void onViewPositionChanged(final View changedView, int left, int top, int dx, int dy) {

            float fraction = top * 1.0f / mDragHeight;

            // mFlexView缩放的比率
            mFlexScaleRatio = 1 - 0.5f * fraction;
            mFlexScaleOffset = changedView.getWidth() / 20;
            // 设置缩放基点
            changedView.setPivotX(changedView.getWidth() - mFlexScaleOffset);
            changedView.setPivotY(changedView.getHeight() - mFlexScaleOffset);
            // 设置比例
            changedView.setScaleX(mFlexScaleRatio);
            changedView.setScaleY(mFlexScaleRatio);

            // mFollowView透明度的比率
            float alphaRatio = 1 - fraction;
            // 设置透明度
            mFollowView.setAlpha(alphaRatio);
            // 根据垂直方向的dy设置top，产生跟随mFlexView的效果
            mFollowView.setTop(mFollowView.getTop() + dy);

            // 到底部的时候，changedView的top刚好等于mDragHeight，以此作为水平拖动的基准
            mHorizontalDragEnable = top == mDragHeight;

            if (mHorizontalDragEnable) {
                // 如果水平拖动允许的话，由于设置缩放不会影响mFlexView的宽高（比如getWidth），所以水平拖动距离为mFlexView宽度一半
                mDragWidth = (int) (changedView.getMeasuredWidth() * 0.5f);

                // 设置mFlexView的透明度，这里向左右水平拖动透明度都随之变化
                changedView.setAlpha(1 - Math.abs(left) * 1.0f / mDragWidth);

                // 水平拖动一定距离的话，垂直拖动将被禁止
                mVerticalDragEnable = left < 0 && left >= -mDragWidth * 0.05;

            } else {
                // 不是水平拖动的处理
                changedView.setAlpha(1);
                mDragWidth = 0;

                mVerticalDragEnable = true;

            }

            if (mFlexLayoutPosition == null) {
                // 创建子元素位置缓存
                mFlexLayoutPosition = new ChildLayoutPosition();
                mFollowLayoutPosition = new ChildLayoutPosition();
            }

            // 记录子元素的位置
            mFlexLayoutPosition.setPosition(mFlexView.getLeft(), mFlexView.getRight(), mFlexView.getTop(), mFlexView.getBottom());
            mFollowLayoutPosition.setPosition(mFollowView.getLeft(), mFollowView.getRight(), mFollowView.getTop(), mFollowView.getBottom());

            //            Log.e("FlexCallback", "225行-onViewPositionChanged(): 【" + mFlexView.getLeft() + ":" + mFlexView.getRight() + ":" + mFlexView.getTop() + ":" + mFlexView
            //                    .getBottom() + "】 【" + mFollowView.getLeft() + ":" + mFollowView.getRight() + ":" + mFollowView.getTop() + ":" + mFollowView.getBottom() + "】");

        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desireHeight = 0;
        int desireWidth = 0;

        int tmpHeight = 0;

        int count = getChildCount();

        if (count != 2) {
            throw new IllegalArgumentException("只允许容器添加两个子View！");
        }

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            // 测量子元素并考虑外边距
            // 参数heightUse：父容器竖直已经被占用的空间，比如被父容器的其他子 view 所占用的空间；这里我们需要的是子View垂直排列，所以需要设置这个值
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, tmpHeight);
            // 获取子元素的布局参数
            final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            // 计算子元素宽度，取子控件最大宽度
            desireWidth = Math.max(desireWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
            // 计算子元素高度
            tmpHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            desireHeight += tmpHeight;
        }
        // 考虑父容器内边距
        desireWidth += getPaddingLeft() + getPaddingRight();
        desireHeight += getPaddingTop() + getPaddingBottom();
        // 尝试比较建议最小值和期望值的大小并取大值
        desireWidth = Math.max(desireWidth, getSuggestedMinimumWidth());
        desireHeight = Math.max(desireHeight, getSuggestedMinimumHeight());
        // 设置最终测量值
        setMeasuredDimension(resolveSize(desireWidth, widthMeasureSpec), resolveSize(desireHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (mFlexLayoutPosition != null) {
            // 因为在用到ViewDragHelper处理布局交互的时候，若是有子View的UI更新导致重新Layout的话，需要我们自己处理ViewDragHelper拖动时子View的位置，否则会导致位置错误
            // Log.e("YytLayout1", "292行-onLayout(): " + "自己处理布局位置");
            mFlexView.layout(mFlexLayoutPosition.getLeft(), mFlexLayoutPosition.getTop(), mFlexLayoutPosition.getRight(), mFlexLayoutPosition.getBottom());
            mFollowView.layout(mFollowLayoutPosition.getLeft(), mFollowLayoutPosition.getTop(), mFollowLayoutPosition.getRight(), mFollowLayoutPosition.getBottom());
            return;
        }

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        int multiHeight = 0;

        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            // 遍历子元素并对其进行定位布局
            final View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            int left = paddingLeft + lp.leftMargin;
            int right = child.getMeasuredWidth() + left;

            int top = (i == 0 ? paddingTop : 0) + lp.topMargin + multiHeight;
            int bottom = child.getMeasuredHeight() + top;

            child.layout(left, top, right, bottom);

            multiHeight += (child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        // Log.e("YytLayout", mFlexView.getLeft() + ";" + mFlexView.getTop() + " --- " + ev.getX() + ":" + ev.getY());

        // 由于缩放不会影响mFlexView真实宽高，这里手动计算视觉上的范围
        float left = mFlexView.getLeft() + mFlexWidth * (1 - mFlexScaleRatio) - mFlexScaleOffset * (1 - mFlexScaleRatio);
        float top = mFlexView.getTop() + mFlexHeight * (1 - mFlexScaleRatio) - mFlexScaleOffset * (1 - mFlexScaleRatio);

        // 这里所做的是判断手指是否落在mFlexView视觉上的范围内
        mInFlexViewTouchRange = ev.getX() >= left && ev.getY() >= top;

        if (mInFlexViewTouchRange) {

            return mDragHelper.shouldInterceptTouchEvent(ev);

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
            // 不在mFlexView触摸范围内，并且子View没有消费，返回false，把事件传递回去
            return false;
        }
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            invalidate();
        } else if (mIsClosing && mOnLayoutStateListener != null) {
            // 正在关闭的情况下，并且拖动结束后，告知将要关闭页面
            mOnLayoutStateListener.onClose();
            mIsClosing = false;
        }
    }

    /**
     * 监听布局是否水平拖动关闭了
     */
    public interface OnLayoutStateListener {

        void onClose();

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

    // 生成默认的布局参数
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return super.generateDefaultLayoutParams();
    }

    // 生成布局参数,将布局参数包装成我们的
    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    // 生成布局参数,从属性配置中生成我们的布局参数
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    // 查当前布局参数是否是我们定义的类型这在code声明布局参数时常常用到
    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    /**
     * 子View的位置缓存
     */
    private class ChildLayoutPosition {
        private int left;
        private int right;
        private int top;
        private int bottom;

        private void setPosition(int left, int right, int top, int bottom) {
            setLeft(left).setRight(right).setTop(top).setBottom(bottom);
        }

        private int getLeft() {
            return left;
        }

        private ChildLayoutPosition setLeft(int left) {
            this.left = left;
            return this;
        }

        private int getRight() {
            return right;
        }

        private ChildLayoutPosition setRight(int right) {
            this.right = right;
            return this;
        }

        private int getTop() {
            return top;
        }

        private ChildLayoutPosition setTop(int top) {
            this.top = top;
            return this;
        }

        private int getBottom() {
            return bottom;
        }

        private ChildLayoutPosition setBottom(int bottom) {
            this.bottom = bottom;
            return this;
        }
    }

}
