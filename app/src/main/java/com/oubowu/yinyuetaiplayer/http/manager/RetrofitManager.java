package com.oubowu.yinyuetaiplayer.http.manager;


import android.util.Log;

import com.oubowu.yinyuetaiplayer.bean.VideoSummary;
import com.oubowu.yinyuetaiplayer.http.NewsService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * ClassName: RetrofitManager<p>
 * Author: oubowu<p>
 * Fuction: Retrofit请求管理类<p>
 * CreateDate:2016/2/13 20:34<p>
 * UpdateUser:<p>
 * UpdateDate:<p>
 */
public class RetrofitManager {


    // 打印返回的json数据拦截器
    private Interceptor mLoggingInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {

            Request request = chain.request();

            Request.Builder requestBuilder = request.newBuilder();
            requestBuilder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
            request = requestBuilder.build();

            final Response response = chain.proceed(request);

            final ResponseBody responseBody = response.body();
            final long contentLength = responseBody.contentLength();

            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();

            Charset charset = Charset.forName("UTF-8");
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(charset);
                } catch (UnsupportedCharsetException e) {
                    return response;
                }
            }

            Log.e("RetrofitManager","68行-intercept(): "+request.url());

            return response;
        }
    };

    private NewsService mNewsService;

    private RetrofitManager() {

        OkHttpClient sOkHttpClient = new OkHttpClient.Builder().addInterceptor(mLoggingInterceptor).retryOnConnectionFailure(true).connectTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://c.m.163.com/").client(sOkHttpClient).addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build();

        mNewsService = retrofit.create(NewsService.class);
    }

    private static class Holder {
        private static RetrofitManager sRetrofitManager = new RetrofitManager();
    }

    public static RetrofitManager getInstance() {
        return Holder.sRetrofitManager;
    }

    /**
     * 网易视频列表 例子：http://c.m.163.com/nc/video/list/V9LG4B3A0/n/0-10.html
     *
     * @param id        视频类别id
     * @param startPage 开始的页码
     * @return 被观察者
     */
    public Observable<Map<String, List<VideoSummary>>> getVideoListObservable(String id, int startPage) {
        return mNewsService.getVideoList(id, startPage);
    }

}
