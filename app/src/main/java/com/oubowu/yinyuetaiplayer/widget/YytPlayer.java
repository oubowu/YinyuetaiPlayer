package com.oubowu.yinyuetaiplayer.widget;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.oubowu.yinyuetaiplayer.BuildConfig;
import com.oubowu.yinyuetaiplayer.R;
import com.oubowu.yinyuetaiplayer.adapter.VideoListAdapter;
import com.oubowu.yinyuetaiplayer.bean.VideoSummary;
import com.oubowu.yinyuetaiplayer.callback.OnItemClickCallback;
import com.oubowu.yinyuetaiplayer.ijkplayer.widget.controller.IjkController;
import com.oubowu.yinyuetaiplayer.ijkplayer.widget.media.IjkVideoView;
import com.oubowu.yinyuetaiplayer.itemdecoration.VideoListItemDecoration;
import com.oubowu.yinyuetaiplayer.transform.GlideCircleTransform;

import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by Oubowu on 2016/12/27 17:32.<p>
 * 仿音悦台播放页面的具体实现，组合控件的形式
 */
public class YytPlayer extends YytLayout {

    private IjkController mIjkController;

    private IjkVideoView mIjkVideoView;

    private ImageView mIvAvatar;
    private TextView mTvName;
    private TextView mTvTime;
    private TextView mTvTitle;
    private TextView mTvDesc;
    private RecyclerView mYytRecyclerView;

    private VideoListAdapter mVideoListAdapter;

    public YytPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        // 继承YytLayout并且通过merge标签减少层级来实现组合控件
        LayoutInflater.from(context).inflate(R.layout.yyt_player, this, true);

        setOnLayoutStateListener(new OnLayoutStateListener() {

            @Override
            public void onClose() {
                setVisibility(View.INVISIBLE);
                mIjkVideoView.release(true);
            }
        });

        mIjkVideoView = (IjkVideoView) findViewById(R.id.ijk_player_view);
        final int scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mIjkVideoView.setOnTouchListener(new OnTouchListener() {

            float mDownX = 0;
            float mDownY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = x;
                        mDownY = y;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (Math.abs(mDownX - x) <= scaledTouchSlop && Math.abs(mDownY - y) <= scaledTouchSlop) {
                            // 点击事件偶尔失效，只好这里自己解决了
                            if (isHorizontalEnable()) {
                                expand();
                            } else {
                                mIjkVideoView.toggleMediaControlsVisibility();
                            }
                        }
                        break;
                }
                return true;
            }
        });

        mIvAvatar = (ImageView) findViewById(R.id.iv_avatar);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mTvTime = (TextView) findViewById(R.id.tv_time);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvDesc = (TextView) findViewById(R.id.tv_desc);

        mVideoListAdapter = new VideoListAdapter();
        mVideoListAdapter.setOnItemClickCallback(new OnItemClickCallback() {
            @Override
            public void onClick(View view, int position) {
                int pos = (Integer) view.getTag();
                VideoSummary summary = mVideoListAdapter.getData().get(pos);
                playVideo(mVideoListAdapter.getData(), summary);
            }
        });

        mYytRecyclerView = (RecyclerView) findViewById(R.id.yyt_recycler_view);
        mYytRecyclerView.setNestedScrollingEnabled(false);
        mYytRecyclerView.setLayoutManager(new GridLayoutManager(context, 2, LinearLayoutManager.VERTICAL, false));
        mYytRecyclerView.addItemDecoration(new VideoListItemDecoration(context));
        mYytRecyclerView.setAdapter(mVideoListAdapter);

    }

    // 播放视频
    private void playVideo(String path, String name) {

        try {
            if (mIjkController == null) {

                IjkMediaPlayer.loadLibrariesOnce(null);
                IjkMediaPlayer.native_profileBegin("libijkplayer.so");

                mIjkController = new IjkController(mIjkVideoView, name);

                mIjkController.setOnViewStateListener(new IjkController.OnViewStateListener() {
                    @Override
                    public void onBackPress() {
                        stop();
                    }
                });

                mIjkVideoView.setMediaController(mIjkController);

                mIjkVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(IMediaPlayer mp) {
                        mIjkVideoView.start();
                    }
                });

                mIjkVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(IMediaPlayer mp, int what, int extra) {
                        Toast.makeText(getContext(), "视频播放出错了╮(╯Д╰)╭", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

            } else {
                // 重新设置视频名字
                mIjkController.setVideoName(name);
            }

            // 设置这个TextureView播放器缩放就正常了
            mIjkVideoView.setRender(IjkVideoView.RENDER_TEXTURE_VIEW);
            // 因为每次setRender都会移除view再添加，为了缩放效果这里控制器是添加到IjkVideoView中的，所以这里也要重新添加才能在IjkVideoView的最上面
            mIjkController.updateControlView();

            // 显示加载条
            mIjkController.showProgress();

            // 播放视频
            mIjkVideoView.setVideoURI(Uri.parse(path));

        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "你的CPU是" + Build.CPU_ABI + ",当前播放器使用的编译版本" + BuildConfig.FLAVOR + "不匹配！", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * 显示布局，并且播放视频
     *
     * @param data    视频列表，用于播放页面下面的列表布局
     * @param summary 播放的视频信息
     */
    public void playVideo(List<VideoSummary> data, VideoSummary summary) {

        // 拿到数据，设置到播放的布局的相关信息
        Glide.with(getContext()).load(summary.mTopicImg).transform(new GlideCircleTransform(getContext())).into(mIvAvatar);
        mTvName.setText(summary.mTopicName);
        mTvTime.setText(summary.mPtime);
        mTvTitle.setText(Html.fromHtml(summary.mTitle));
        if (summary.mDescription.isEmpty()) {
            mTvDesc.setText(summary.mTopicDesc);
        } else {
            mTvDesc.setText(Html.fromHtml(summary.mDescription));
        }

        // 设置YytLayout可见，并且展开
        setVisibility(View.VISIBLE);
        expand();

        mVideoListAdapter.setData(data);
        mVideoListAdapter.setItemWidth(mYytRecyclerView.getWidth() / 2);
        mVideoListAdapter.notifyDataSetChanged();

        // 播放视频
        playVideo(summary.mMp4HdUrl == null ? summary.mMp4Url : summary.mMp4HdUrl, summary.mTitle);
    }

    // 开始播放
    public void start() {
        if (mIjkVideoView != null && !mIjkVideoView.isPlaying()) {
            mIjkVideoView.start();
        }
    }

    // 暂停播放
    public void pause() {
        if (mIjkVideoView != null && mIjkVideoView.isPlaying()) {
            mIjkVideoView.pause();
        }
    }

    // 停止播放
    public void stop() {
        setVisibility(View.INVISIBLE);
        if (mIjkVideoView != null) {
            mIjkVideoView.release(true);
        }
    }

    public boolean isShowing() {
        return getVisibility() == VISIBLE;
    }
}
