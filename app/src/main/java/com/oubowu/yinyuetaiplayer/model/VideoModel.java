package com.oubowu.yinyuetaiplayer.model;

import com.oubowu.yinyuetaiplayer.base.BaseSubscriber;
import com.oubowu.yinyuetaiplayer.bean.VideoSummary;
import com.oubowu.yinyuetaiplayer.callback.RequestCallback;
import com.oubowu.yinyuetaiplayer.http.manager.RetrofitManager;

import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by Oubowu on 2016/12/27 12:34.
 */
public class VideoModel {

    public static Subscription getVideoList(String id, int startPage, RequestCallback<List<VideoSummary>> callback) {
        return RetrofitManager.getInstance().getVideoListObservable("V9LG4B3A0", 0)//
                .flatMap(new Func1<Map<String, List<VideoSummary>>, Observable<VideoSummary>>() {
                    @Override
                    public Observable<VideoSummary> call(Map<String, List<VideoSummary>> map) {
                        // 通过id取到list
                        return Observable.from(map.get("V9LG4B3A0"));
                    }
                })//
                .toSortedList(new Func2<VideoSummary, VideoSummary, Integer>() {
                    @Override
                    public Integer call(VideoSummary videoSummary, VideoSummary videoSummary2) {
                        // 时间排序
                        return videoSummary2.mPtime.compareTo(videoSummary.mPtime);
                    }
                })//
                .subscribeOn(Schedulers.io())//
                .observeOn(AndroidSchedulers.mainThread())//
                .subscribe(new BaseSubscriber<>(callback));
    }

}
