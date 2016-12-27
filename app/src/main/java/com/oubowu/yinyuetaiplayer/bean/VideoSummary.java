package com.oubowu.yinyuetaiplayer.bean;

import com.google.gson.annotations.SerializedName;

/**
 * ClassName: VideoSummary<p>
 * Author:oubowu<p>
 * Fuction: 网易视频列表<p>
 * CreateDate:2016/2/14 0:39<p>
 * UpdateUser:<p>
 * UpdateDate:<p>
 */
public class VideoSummary {


    /**
     * topicImg : http://vimg1.ws.126.net/image/snapshot/2016/11/7/S/VC4VT5P7S.jpg
     * videosource : 新媒体
     * mp4Hd_url : http://flv2.bn.netease.com/videolib3/1612/26/rhBLA2001/HD/rhBLA2001-mobile.mp4
     * topicDesc : 健康生活！快乐生活！一个可以让您笑逐颜开的常笑sir！用我的专业和知识、经验和见闻为您的生活增添一点色彩！
     * topicSid : VC4VT5P7I
     * cover : http://vimg3.ws.126.net/image/snapshot/2016/12/L/U/VC8A9ULLU.jpg
     * title : 排出皮肤毒素有妙招!常喝此汤排毒美容又养颜!
     * playCount : 0
     * replyBoard : video_bbs
     * videoTopic : {"alias":"一个传播正能量的常笑sir","tname":"常笑sir","ename":"T1479135162284","tid":"T1479135162284"}
     * sectiontitle :
     * replyid : C8A981RA008535RB
     * description : 生活作息乱，日常饮食没有规律，这样的恶性循环导致了身体毒素的堆积，而毒素不止会影响身体的健康，同样会影响皮肤的排毒，进一步危害人们的肌肤。痘痘、粉刺、敏感和黯哑等肌肤问题就是由于排毒不畅所产生的！
     * mp4_url : http://flv2.bn.netease.com/videolib3/1612/26/rhBLA2001/SD/rhBLA2001-mobile.mp4
     * length : 154
     * playersize : 0
     * m3u8Hd_url : http://flv2.bn.netease.com/videolib3/1612/26/rhBLA2001/HD/movie_index.m3u8
     * vid : VC8A981RA
     * m3u8_url : http://flv2.bn.netease.com/videolib3/1612/26/rhBLA2001/SD/movie_index.m3u8
     * ptime : 2016-12-26 19:47:03
     * topicName : 常笑sir
     */

    @SerializedName("topicImg")
    public String mTopicImg;
    @SerializedName("videosource")
    public String mVideosource;
    @SerializedName("mp4Hd_url")
    public String mMp4HdUrl;
    @SerializedName("topicDesc")
    public String mTopicDesc;
    @SerializedName("topicSid")
    public String mTopicSid;
    @SerializedName("cover")
    public String mCover;
    @SerializedName("title")
    public String mTitle;
    @SerializedName("playCount")
    public int mPlayCount;
    @SerializedName("replyBoard")
    public String mReplyBoard;
    @SerializedName("videoTopic")
    public VideoTopic mVideoTopic;
    @SerializedName("sectiontitle")
    public String mSectiontitle;
    @SerializedName("replyid")
    public String mReplyid;
    @SerializedName("description")
    public String mDescription;
    @SerializedName("mp4_url")
    public String mMp4Url;
    @SerializedName("length")
    public int mLength;
    @SerializedName("playersize")
    public int mPlayersize;
    @SerializedName("m3u8Hd_url")
    public String mM3u8HdUrl;
    @SerializedName("vid")
    public String mVid;
    @SerializedName("m3u8_url")
    public String mM3u8Url;
    @SerializedName("ptime")
    public String mPtime;
    @SerializedName("topicName")
    public String mTopicName;

    public static class VideoTopic {
        /**
         * alias : 一个传播正能量的常笑sir
         * tname : 常笑sir
         * ename : T1479135162284
         * tid : T1479135162284
         */

        @SerializedName("alias")
        public String mAlias;
        @SerializedName("tname")
        public String mTname;
        @SerializedName("ename")
        public String mEname;
        @SerializedName("tid")
        public String mTid;
    }
}
