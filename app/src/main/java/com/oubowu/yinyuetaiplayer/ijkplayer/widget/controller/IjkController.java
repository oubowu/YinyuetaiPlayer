package com.oubowu.yinyuetaiplayer.ijkplayer.widget.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.oubowu.yinyuetaiplayer.R;
import com.oubowu.yinyuetaiplayer.ijkplayer.widget.media.IMediaController;
import com.oubowu.yinyuetaiplayer.ijkplayer.widget.media.IjkVideoView;

import java.lang.ref.WeakReference;
import java.util.Locale;


/**
 * Created by Oubowu on 2016/9/8 0008 12:20.<p>
 * 播放器的控制器
 */
public class IjkController implements IMediaController, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final int PLAY_PROGRESS = 0;
    private static final int SHOW = 1;
    private static final int HIDE_DELAY = 2;

    private static final int HIDE_DELAY_DURATION = 3000;

    private final IjkControllerHandler mHandler;
    private final AnimatorListener mAnimatorListener;

    private IjkVideoView mIjkVideoView;
    private String mVideoName = "";

    // 控制进度的相关View
    private View mControlView;
    private FrameLayout mTopLayout;
    private ImageView mIvBack;
    private TextView mTvTitle;
    private LinearLayout mBottomLayout;
    private ImageView mIvPlay;
    private SeekBar mSeekBar;
    private TextView mTvTime;
    private ProgressBar mProgressBar;

    private boolean mAnimationStart;

    private boolean mIsUserTouch;

    private OnViewStateListener mOnViewStateListener;

    public IjkController(IjkVideoView ijkVideoView, String videoName) {
        mHandler = new IjkControllerHandler(ijkVideoView.getHandler().getLooper(), this);
        mIjkVideoView = ijkVideoView;
        mVideoName = videoName;

        mAnimatorListener = new AnimatorListener();

    }

    /**
     * 添加控制布局到播放器布局中
     */
    public void updateControlView() {
        if (mControlView != null) {
            // setRenderView的时候重新添加一个控制的View
            mIjkVideoView.removeView(mControlView);
            mControlView.setVisibility(View.INVISIBLE);
            mIjkVideoView.addView(mControlView);
        } else {
            initControlView();
        }
    }

    private void initControlView() {
        // 添加一个控制的View
        mControlView = LayoutInflater.from(mIjkVideoView.getContext()).inflate(R.layout.player_control_layout, mIjkVideoView, false);
        mControlView.setVisibility(View.INVISIBLE);
        mIjkVideoView.addView(mControlView);

        mTopLayout = (FrameLayout) mControlView.findViewById(R.id.fl_title);

        mIvBack = (ImageView) mControlView.findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);

        mTvTitle = (TextView) mControlView.findViewById(R.id.tv_title);
        mTvTitle.setWidth(mIjkVideoView.getMeasuredWidth() / 2);
        mTvTitle.setText(mVideoName);

        mBottomLayout = (LinearLayout) mControlView.findViewById(R.id.fl_progress);

        mIvPlay = (ImageView) mControlView.findViewById(R.id.iv_play);
        mIvPlay.setOnClickListener(this);

        mSeekBar = (SeekBar) mControlView.findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mTvTime = (TextView) mControlView.findViewById(R.id.tv_time);

        mProgressBar = (ProgressBar) mControlView.findViewById(R.id.progress_bar);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!mIsUserTouch) {
            return;
        }
        mHandler.removeMessages(PLAY_PROGRESS);
        mHandler.removeMessages(HIDE_DELAY);

        mIjkVideoView.seekTo(progress);

        mTvTime.setText(buildTimeMilli(mIjkVideoView.getCurrentPosition()) + "/" +
                buildTimeMilli(mIjkVideoView.getDuration()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsUserTouch = true;
        mIjkVideoView.pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mIsUserTouch = false;

        if (mIvPlay.isSelected()) {
            mHandler.sendEmptyMessageDelayed(PLAY_PROGRESS, 0);
            mHandler.sendEmptyMessageDelayed(HIDE_DELAY, HIDE_DELAY_DURATION);
            mIjkVideoView.start();
        }

    }

    @Override
    public void onClick(View v) {

        if (mAnimationStart) {
            return;
        }

        switch (v.getId()) {
            case R.id.iv_back:
                if (mOnViewStateListener != null) {
                    mOnViewStateListener.onBackPress();
                }
                break;
            case R.id.iv_play:
                v.setSelected(!v.isSelected());
                if (!v.isSelected()) {
                    mIjkVideoView.pause();
                } else {
                    mIjkVideoView.start();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void hide() {
        if (isShowing()) {
            if (mAnimationStart) {
                return;
            }

            mHandler.removeMessages(PLAY_PROGRESS);
            mHandler.removeMessages(HIDE_DELAY);

            if (mTopLayoutHeight == 0) {
                mTopLayoutHeight = mTopLayout.getHeight();
                mBottomLayoutHeight = mBottomLayout.getHeight();
            }
            mTopLayout.animate().translationYBy(-mTopLayoutHeight).setDuration(300).setListener(mAnimatorListener.setState(false));
            mBottomLayout.animate().translationYBy(mBottomLayoutHeight).setDuration(300);
        }
    }

    @Override
    public boolean isShowing() {
        return mControlView != null && mControlView.getVisibility() == View.VISIBLE;
    }

    @Override
    public void setAnchorView(View view) {
        // TODO: 2016/12/27 不是弹窗形式，没必要设置AnchorView
    }

    /**
     * IjkVideoView在setMediaController会调用attachMediaController,然后传递isInPlaybackState()来告知我们现在播放器是不是播放的状态,
     * 此时我们的布局就需要做处理
     *
     * @param enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        mIvPlay.setSelected(enabled);
    }

    /**
     * IjkVideoView在setMediaController会调用attachMediaController,然后mMediaController.setMediaPlayer(this)把自己传递进来，因为它本身实现了MediaPlayerControl接口，
     * 所以就可以通过mPlayer继续播放器的各种操作了
     *
     * @param player
     */
    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {

    }

    @Override
    public void show(int timeout) {
        mHandler.sendEmptyMessageDelayed(PLAY_PROGRESS, 0);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(SHOW), timeout);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE_DELAY), HIDE_DELAY_DURATION);
    }

    @Override
    public void show() {
        show(0);
    }

    /**
     * IjkVideoView没有调用过这个东西，不知道有啥用处
     *
     * @param view
     */
    @Override
    public void showOnce(View view) {
        // TODO: 2016/12/27 啥都不干
    }

    public void setVideoName(String name) {
        mVideoName = name;
        if (mTvTitle != null) {
            mTvTitle.setText(mVideoName);
        }
    }

    public void showProgress() {
        if (mControlView != null) {
            mControlView.setVisibility(View.VISIBLE);
            mTopLayout.setVisibility(View.INVISIBLE);
            mBottomLayout.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private int mTopLayoutHeight;
    private int mBottomLayoutHeight;

    private static class IjkControllerHandler extends Handler {
        private WeakReference<IjkController> mReference;
        private boolean mFirstShow = true;

        public IjkControllerHandler(Looper looper, IjkController controller) {
            super(looper);
            this.mReference = new WeakReference<>(controller);
        }

        @Override
        public void handleMessage(Message msg) {
            final IjkController controller = mReference.get();
            if (controller != null) {
                switch (msg.what) {
                    case PLAY_PROGRESS:
                        // 更新事件进度
                        controller.mTvTime.setText(controller.buildTimeMilli(controller.mIjkVideoView.getCurrentPosition()) + "/" + controller
                                .buildTimeMilli(controller.mIjkVideoView.getDuration()));

                        controller.mSeekBar.setMax(controller.mIjkVideoView.getDuration());
                        controller.mSeekBar.setProgress(controller.mIjkVideoView.getCurrentPosition());
                        controller.mSeekBar.setSecondaryProgress(controller.mIjkVideoView.getBufferPercentage() * controller.mIjkVideoView.getDuration());

                        controller.mIvPlay.setSelected(controller.mIjkVideoView.isPlaying());

                        sendMessageDelayed(obtainMessage(PLAY_PROGRESS), 1000);

                        break;
                    case SHOW:
                        // 显示布局
                        if (controller.mControlView != null) {

                            controller.mControlView.setVisibility(View.VISIBLE);
                            controller.mTopLayout.setVisibility(View.VISIBLE);
                            controller.mBottomLayout.setVisibility(View.VISIBLE);
                            controller.mProgressBar.setVisibility(View.INVISIBLE);

                            if (mFirstShow) {
                                mFirstShow = false;
                            } else {
                                controller.mTopLayout.animate().translationYBy(-controller.mTopLayout.getTranslationY()).setDuration(300)
                                        .setListener(controller.mAnimatorListener.setState(true));
                                controller.mBottomLayout.animate().translationYBy(-controller.mBottomLayout.getTranslationY()).setDuration(300);
                            }
                        }
                        break;
                    case HIDE_DELAY:
                        // 延时隐藏布局
                        controller.hide();
                        break;
                    default:
                        break;
                }
            }
        }

    }

    /**
     * 用于指向顶部和底部两个布局显示或者隐藏的值动画监听
     */
    private class AnimatorListener extends AnimatorListenerAdapter {

        /**
         * 标示是要做隐藏还是显示的动画
         */
        private boolean mShow;

        public AnimatorListener setState(boolean show) {
            mShow = show;
            return this;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            mAnimationStart = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {

            mAnimationStart = false;

            if (!mShow) {
                mControlView.setVisibility(View.INVISIBLE);
            }
        }

    }

    // 格式化视频时间
    private String buildTimeMilli(long duration) {
        long total_seconds = duration / 1000;
        long hours = total_seconds / 3600;
        long minutes = (total_seconds % 3600) / 60;
        long seconds = total_seconds % 60;
        if (duration <= 0) {
            return "00:00";
        }
        if (hours >= 100) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }

    /**
     * 控件布局的监听器
     */
    public interface OnViewStateListener {
        /**
         * 点击播放器返回按键
         */
        void onBackPress();
    }

    public void setOnViewStateListener(OnViewStateListener onViewStateListener) {
        mOnViewStateListener = onViewStateListener;
    }


}
