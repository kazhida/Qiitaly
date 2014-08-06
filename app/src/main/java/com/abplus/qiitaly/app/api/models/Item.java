package com.abplus.qiitaly.app.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

/**
 * 投稿
 *
 * Created by kazhida on 2014/07/29.
 */
public class Item {
    @Getter @Expose @SuppressWarnings("unused")
    private Integer id;
    @Getter @Expose @SuppressWarnings("unused")
    private String uuid;
    @Getter @Expose @SuppressWarnings("unused")
    private User user;
    @Getter @Expose @SuppressWarnings("unused")
    private String title;
    @Getter @Expose @SuppressWarnings("unused")
    private String body;
    @Getter @Expose @SerializedName("created_at") @SuppressWarnings("unused")
    private String createdAt;
    @Getter @Expose @SerializedName("updated_at") @SuppressWarnings("unused")
    private String updatedAt;
    @Getter @Expose @SerializedName("created_at_in_words") @SuppressWarnings("unused")
    private String createdAtInWords;
    @Getter @Expose @SerializedName("updated_at_in_words") @SuppressWarnings("unused")
    private String updatedAtInWords;
    @Getter @Expose @SuppressWarnings("unused")
    private List<Tag> tags;
    @Getter @Expose @SerializedName("stock_count") @SuppressWarnings("unused")
    private Integer stockCount;
    @Getter @Expose @SerializedName("stock_users") @SuppressWarnings("unused")
    private List<User> stockUsers;
    @Getter @Expose @SerializedName("comment_count") @SuppressWarnings("unused")
    private Integer commentCount;
    @Getter @Expose @SuppressWarnings("unused")
    private String url;
    @Getter @Expose @SerializedName("gist_url") @SuppressWarnings("unused")
    private String gistUrl;
    @Getter @Expose @SuppressWarnings("unused")
    private Boolean tweet;
    @Getter @Expose @SerializedName("private") @SuppressWarnings("unused")
    private Boolean privateItem;
    @Getter @Expose @SuppressWarnings("unused")
    private Boolean stocked;
    @Getter @Expose @SuppressWarnings("unused")
    private List<Comment> comments;
}
