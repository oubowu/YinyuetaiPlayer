package com.oubowu.yinyuetaiplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.oubowu.yinyuetaiplayer.R;
import com.oubowu.yinyuetaiplayer.base.BaseViewHolder;
import com.oubowu.yinyuetaiplayer.bean.VideoSummary;
import com.oubowu.yinyuetaiplayer.callback.OnItemClickCallback;

import java.util.List;

/**
 * Created by Oubowu on 2016/12/27 17:43.
 */
public class VideoListAdapter extends RecyclerView.Adapter<BaseViewHolder> implements View.OnClickListener {

    public void setItemWidth(int itemWidth) {
        mItemWidth = itemWidth;
    }

    private int mItemWidth;

    private List<VideoSummary> mData;

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        mOnItemClickCallback = onItemClickCallback;
    }

    private OnItemClickCallback mOnItemClickCallback;

    public List<VideoSummary> getData() {
        return mData;
    }

    public void setData(List<VideoSummary> data) {
        this.mData = data;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BaseViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_summary, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {

        holder.itemView.setOnClickListener(this);
        holder.itemView.setTag(position);

        VideoSummary summary = mData.get(position);

        // 设置视频列表相关信息
        holder.setText(R.id.tv, Html.fromHtml(summary.mTitle));

        ImageView imageView = holder.getImageView(R.id.iv);
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        lp.width = lp.height = mItemWidth;
        imageView.setLayoutParams(lp);

        Glide.with(holder.itemView.getContext()).load(summary.mCover).into(imageView);

    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public void onClick(View view) {

        int pos = (Integer) view.getTag();

        if (mOnItemClickCallback != null) {
            mOnItemClickCallback.onClick(view, pos);
        }

    }

}
