package com.vance.gsonexample;

import com.google.gson.annotations.SerializedName;

/**
 * Auto-generated: 2022-03-30 14:12:32
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class J {

    private String authorImgUrl;
    private String authorName;
    private String title;
    @SerializedName("coverImgUrl")
    private String videoImgUrl;
    private String musicName;
    @SerializedName("videoDownloadUrl")
    private String videoUrl;

    public J() {
    }

    public J(String authorImgUrl, String authorName, String title, String videoImgUrl, String musicName, String videoUrl) {
        this.authorImgUrl = authorImgUrl;
        this.authorName = authorName;
        this.title = title;
        this.videoImgUrl = videoImgUrl;
        this.musicName = musicName;
        this.videoUrl = videoUrl;
    }

    public String getAuthorImgUrl() {
        return authorImgUrl;
    }

    public void setAuthorImgUrl(String authorImgUrl) {
        this.authorImgUrl = authorImgUrl;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoImgUrl() {
        return videoImgUrl;
    }

    public void setVideoImgUrl(String videoImgUrl) {
        this.videoImgUrl = videoImgUrl;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    @Override
    public String toString() {
        return "J{" +
                "authorImgUrl='" + authorImgUrl + '\'' +
                ", authorName='" + authorName + '\'' +
                ", title='" + title + '\'' +
                ", videoImgUrl='" + videoImgUrl + '\'' +
                ", musicName='" + musicName + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                '}';
    }
}
