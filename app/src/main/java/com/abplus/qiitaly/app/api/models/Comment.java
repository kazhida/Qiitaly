package com.abplus.qiitaly.app.api.models;

import com.google.gson.annotations.Expose;
import lombok.Getter;

/**
 * コメント
 *
 * Created by kazhida on 2014/07/30.
 */
public class Comment {
    @Getter @Expose
    private Integer id;
    @Getter @Expose
    private String uuid;
    @Getter @Expose
    private User user;
    @Getter @Expose
    private String body;
}
