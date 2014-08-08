package com.abplus.qiitaly.app.api.models;

import com.google.gson.annotations.Expose;
import lombok.Getter;

/**
 * コメント
 *
 * Created by kazhida on 2014/07/30.
 */
public class Comment {
    @Getter @Expose @SuppressWarnings("unused")
    private Integer id;
    @Getter @Expose @SuppressWarnings("unused")
    private String uuid;
    @Getter @Expose @SuppressWarnings("unused")
    private User user;
    @Getter @Expose @SuppressWarnings("unused")
    private String body;
}
