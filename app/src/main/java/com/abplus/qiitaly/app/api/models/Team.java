package com.abplus.qiitaly.app.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * チーム
 * とりあえず用意しているけど、今のところたいした用途はない
 *
 * Created by kazhida on 2014/07/29.
 */
public class Team {
    @Getter @Expose
    private String name;
    @Getter @Expose @SerializedName("url_name")
    private String urlName;
}
