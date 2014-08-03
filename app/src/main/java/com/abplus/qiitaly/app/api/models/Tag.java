package com.abplus.qiitaly.app.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

/**
 * タグ
 *
 * Created by kazhida on 2014/07/29.
 */
public class Tag {
    @Getter @Expose
    private String name;
    @Getter @Expose @SerializedName("url_name")
    private String urlName;
    @Getter @Expose @SerializedName("icon_url")
    private String iconUrl;
    @Getter @Expose @SerializedName("item_count")
    private Integer itemCount;
    @Getter @Expose @SerializedName("follower_count")
    private Integer followerCount;
    @Getter @Expose
    private Boolean following;
    @Getter @Expose
    private List<String> versions;
}
