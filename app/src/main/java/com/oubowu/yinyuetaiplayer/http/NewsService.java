package com.oubowu.yinyuetaiplayer.http;


import com.oubowu.yinyuetaiplayer.bean.VideoSummary;

import java.util.List;
import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * ClassName: NewsService<p>
 * Author: oubowu<p>
 * Fuction: 请求数据服务<p>
 * CreateDate:2016/2/13 20:34<p>
 * UpdateUser:<p>
 * UpdateDate:<p>
 */
public interface NewsService {

    /**
     * 网易视频列表 例子：http://c.m.163.com/nc/video/list/V9LG4B3A0/n/0-10.html
     *
     * @param id        视频类别id
     * @param startPage 开始的页码
     * @return 被观察者
     */
    @GET("nc/video/list/{id}/n/{startPage}-10.html")
    Observable<Map<String, List<VideoSummary>>> getVideoList(@Path("id") String id, @Path("startPage") int startPage);

}
