package com.abplus.qiitaly.app.models;

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
    @Getter @Expose
    private Integer id;
    @Getter @Expose
    private String uuid;
    @Getter @Expose
    private User user;
    @Getter @Expose
    private String title;
    @Getter @Expose
    private String body;
    @Getter @Expose @SerializedName("created_at")
    private String createdAt;
    @Getter @Expose @SerializedName("updated_at")
    private String updatedAt;
    @Getter @Expose @SerializedName("created_at_in_words")
    private String createdAtInWords;
    @Getter @Expose @SerializedName("updated_at_in_words")
    private String updatedAtInWords;
    @Getter @Expose
    private List<Tag> tags;
    @Getter @Expose @SerializedName("stock_count")
    private Integer stockCount;
    @Getter @Expose @SerializedName("stock_users")
    private List<User> stockUsers;
    @Getter @Expose @SerializedName("comment_count")
    private Integer commentCount;
    @Getter @Expose
    private String url;
    @Getter @Expose @SerializedName("gist_url")
    private String gistUrl;
    @Getter @Expose
    private Boolean tweet;
    @Getter @Expose @SerializedName("private")
    private Boolean privateItem;
    @Getter @Expose
    private Boolean stocked;
    @Getter @Expose
    private List<Comment> comments;
}
