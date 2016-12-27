package com.oubowu.yinyuetaiplayer;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.oubowu.yinyuetaiplayer.adapter.VideoListAdapter;
import com.oubowu.yinyuetaiplayer.bean.VideoSummary;
import com.oubowu.yinyuetaiplayer.callback.OnItemClickCallback;
import com.oubowu.yinyuetaiplayer.callback.RequestCallback;
import com.oubowu.yinyuetaiplayer.itemdecoration.VideoListItemDecoration;
import com.oubowu.yinyuetaiplayer.model.VideoModel;
import com.oubowu.yinyuetaiplayer.widget.YytPlayer;

import java.util.List;

import rx.Subscription;


public class MainActivity extends AppCompatActivity {

    private Subscription mSubscription;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;

    private VideoListAdapter mVideoListAdapter;

    private YytPlayer mYytPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        requestVideoList();

    }

    // 初始化主列表相关View
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mYytPlayer = (YytPlayer) findViewById(R.id.yyt_player);

        mVideoListAdapter = new VideoListAdapter();
        mVideoListAdapter.setOnItemClickCallback(new OnItemClickCallback() {
            @Override
            public void onClick(View view, int position) {
                int pos = (Integer) view.getTag();
                VideoSummary summary = mVideoListAdapter.getData().get(pos);

                mYytPlayer.playVideo(mVideoListAdapter.getData(),summary);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new VideoListItemDecoration(this));
        mRecyclerView.setAdapter(mVideoListAdapter);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestVideoList();
            }
        });
    }

    // 请求视频列表数据
    private void requestVideoList() {
        mSubscription = VideoModel.getVideoList("V9LG4B3A0", 0, new RequestCallback<List<VideoSummary>>() {
            @Override
            public void beforeRequest() {
                mRefreshLayout.setRefreshing(true);
            }

            @Override
            public void requestError(String msg) {
                mRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void requestComplete() {
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void requestSuccess(List<VideoSummary> data) {
                mVideoListAdapter.setItemWidth(mRecyclerView.getWidth() / 2);
                mVideoListAdapter.setData(data);
                mVideoListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mYytPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mYytPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onBackPressed() {
        if (mYytPlayer.getVisibility() == View.VISIBLE) {
            // 按返回键的时候，YytLayout可见的话，设置不可见，并且关闭播放器
            mYytPlayer.stop();
        } else {
            super.onBackPressed();
        }

    }

}
