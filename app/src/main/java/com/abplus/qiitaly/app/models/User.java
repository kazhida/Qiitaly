package com.abplus.qiitaly.app.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

/**
 * ユーザ
 *
 * Created by kazhida on 2014/07/29.
 */
public class User {
    @Getter @Expose
    private String name;
    @Getter @Expose @SerializedName("url_name")
    private String urlName;
    @Getter @Expose @SerializedName("profile_image_url")
    private String profileImageUrl;
    @Getter @Expose
    private String url;
    @Getter @Expose
    private String description;
    @Getter @Expose @SerializedName("website_url")
    private String websiteUrl;
    @Getter @Expose
    private String organization;
    @Getter @Expose
    private String location;
    @Getter @Expose
    private String facebook;
    @Getter @Expose
    private String linkedin;
    @Getter @Expose
    private String twitter;
    @Getter @Expose
    private String github;
    @Getter @Expose
    private Integer followers;
    @Getter @Expose @SerializedName("following_users")
    private Integer followingUsers;
    @Getter @Expose
    private Integer items;
    @Getter @Expose
    private List<Team> teams;
}
