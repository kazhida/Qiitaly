package com.abplus.qiitaly.app.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * ユーザ
 *
 * Created by kazhida on 2014/07/29.
 */
public class User {
    @Getter @Expose @SuppressWarnings("unused")
    private String name;
    @Getter @Expose @SerializedName("url_name") @SuppressWarnings("unused")
    private String urlName;
    @Getter @Expose @SerializedName("profile_image_url") @SuppressWarnings("unused")
    private String profileImageUrl;
    @Getter @Expose @SuppressWarnings("unused")
    private String url;
    @Getter @Expose @SuppressWarnings("unused")
    private String description;
    @Getter @Expose @SerializedName("website_url") @SuppressWarnings("unused")
    private String websiteUrl;
    @Getter @Expose @SuppressWarnings("unused")
    private String organization;
    @Getter @Expose @SuppressWarnings("unused")
    private String location;
    @Getter @Expose @SuppressWarnings("unused")
    private String facebook;
    @Getter @Expose @SuppressWarnings("unused")
    private String linkedin;
    @Getter @Expose @SuppressWarnings("unused")
    private String twitter;
    @Getter @Expose @SuppressWarnings("unused")
    private String github;
    @Getter @Expose @SuppressWarnings("unused")
    private Integer followers;
    @Getter @Expose @SerializedName("following_users") @SuppressWarnings("unused")
    private Integer followingUsers;
    @Getter @Expose @SuppressWarnings("unused")
    private Integer items;
    @Getter @Expose @SuppressWarnings("unused")
    private List<Team> teams;
    @Getter @Expose @SuppressWarnings("unused")
    private Boolean following;

    //  ここから下は、APIのレスポンスにはない
    public static class Followings {
        @Getter
        private List<User> users = new ArrayList<>();
        @Getter
        private List<Tag> tags = new ArrayList<>();
    }
    @Getter
    private Followings followings;

    public void addFollowingUsers(List<User> users) {
        if (followings == null) followings = new Followings();
        followings.users.addAll(users);
    }

    public void addFollowingTags(List<Tag> tags) {
        if (followings == null) followings = new Followings();
        followings.tags.addAll(tags);
    }

    @SuppressWarnings("unused")
    public User() {}
    public User(String name, String urlName, String profileImageUrl) {
        this.name = name;
        this.urlName = urlName;
        this.profileImageUrl = profileImageUrl;
    }
}
