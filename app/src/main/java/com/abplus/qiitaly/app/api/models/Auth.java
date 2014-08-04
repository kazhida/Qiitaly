package com.abplus.qiitaly.app.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * 認証トークン
 *
 * Created by kazhida on 2014/08/05.
 */
public class Auth {
    @Getter
    @Expose
    private String token;
    @Getter @Expose @SerializedName("url_name")
    private String urlName;

    @SuppressWarnings("unused")
    public Auth() {}

    public Auth(Auth src) {
        this.token = src.token;
        this.urlName = src.urlName;
    }
}
