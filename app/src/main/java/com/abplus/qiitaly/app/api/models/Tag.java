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
    @Getter @Expose @SuppressWarnings("unused")
    private String name;
    @Getter @Expose @SerializedName("url_name") @SuppressWarnings("unused")
    private String urlName;
    @Getter @Expose @SerializedName("icon_url") @SuppressWarnings("unused")
    private String iconUrl;
    @Getter @Expose @SerializedName("item_count") @SuppressWarnings("unused")
    private Integer itemCount;
    @Getter @Expose @SerializedName("follower_count") @SuppressWarnings("unused")
    private Integer followerCount;
    @Getter @Expose @SuppressWarnings("unused")
    private Boolean following;
    @Getter @Expose @SuppressWarnings("unused")
    private List<String> versions;
}
