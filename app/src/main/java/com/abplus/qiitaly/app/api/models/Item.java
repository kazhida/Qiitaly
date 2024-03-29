package com.abplus.qiitaly.app.api.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.abplus.qiitaly.app.utils.CalendarUtil;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 投稿
 *
 * Created by kazhida on 2014/07/29.
 */
public class Item {
    @Getter @Expose @SuppressWarnings("unused")
    private Long id;
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
    private List<String> stockUsers;
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

    //  ここから下は、APIのレスポンスにはない
    @Getter @SuppressWarnings("unused")
    private boolean saved;

    public static class Cache {
        private static final String ITEMS                   = "ITEMS";
        private static final String UUID                    = "UUID";
        private static final String USER_NAME               = "USER_NAME";
        private static final String USER_URL_NAME           = "USER_URL_NAME";
        private static final String USER_PROFILE_IMAGE_URL  = "USER_PROFILE_IMAGE_URL";
        private static final String TITLE                   = "TITLE";
        private static final String BODY                    = "BODY";
        private static final String CREATED_TIME_IN_MILLIS  = "CREATED_TIME_IN_MILLIS";
        private static final String UPDATED_TIME_IN_MILLIS  = "UPDATED_TIME_IN_MILLIS";
        private static final String CHECKED_TIME_IN_MILLIS  = "CHECKED_TIME_IN_MILLIS";
        private static final String CREATED_AT_IN_WORDS     = "CREATED_AT_IN_WORDS";
        private static final String UPDATED_AT_IN_WORDS     = "UPDATED_AT_IN_WORDS";
        private static final String URL                     = "URL";
        private static final String STOCKED                 = "STOCKED";

        public static final String CREATE_ITEMS =
                "create table " + ITEMS + " (" +
                UUID                    + " text primary key,   " +
                USER_NAME               + " text,               " +
                USER_URL_NAME           + " text,               " +
                USER_PROFILE_IMAGE_URL  + " text,               " +
                TITLE                   + " text,               " +
                BODY                    + " text,               " +
                CREATED_AT_IN_WORDS     + " text,               " +
                UPDATED_AT_IN_WORDS     + " text,               " +
                CREATED_TIME_IN_MILLIS  + " integer,            " +
                UPDATED_TIME_IN_MILLIS  + " integer,            " +
                CHECKED_TIME_IN_MILLIS  + " integer,            " +
                STOCKED                 + " integer,            " +
                URL                     + " text);              ";

        public static final String ADD_URL =
                "alter table " + ITEMS + " add column " +
                URL + " text;";

        public static final String ADD_STOCKED =
                "alter table " + ITEMS + " add column " +
                        STOCKED + " integer;";

        private static final String[] columns = new String[] {
                UUID,
                USER_NAME,
                USER_URL_NAME,
                USER_PROFILE_IMAGE_URL,
                TITLE,
                BODY,
                CREATED_TIME_IN_MILLIS,
                UPDATED_TIME_IN_MILLIS,
                CHECKED_TIME_IN_MILLIS,
                CREATED_AT_IN_WORDS,
                UPDATED_AT_IN_WORDS,
                URL,
                STOCKED
        };

        @Getter
        private String  uuid;
        @Getter
        private String  userName;
        @Getter
        private String  userUrlName;
        @Getter
        private String  userProfileImageUrl;
        @Getter
        private String  title;
        @Getter
        private String  body;
        @Getter
        private long    createdAt;
        @Getter
        private long    updatedAt;
        @Getter
        private long    checkedAt = 0;
        @Getter
        private String  createdAtInWords;
        @Getter
        private String  updatedAtInWords;
        @Getter
        private String  url;
        @Getter
        private boolean stocked;

        private Cache(Item item) {
            this.uuid                = item.uuid;
            this.userName            = item.user.getName();
            this.userUrlName         = item.user.getUrlName();
            this.userProfileImageUrl = item.user.getProfileImageUrl();
            this.title               = item.title;
            this.body                = item.body;
            this.createdAt           = CalendarUtil.parse(item.createdAt);
            this.updatedAt           = CalendarUtil.parse(item.updatedAt);
            this.createdAtInWords    = item.createdAtInWords;
            this.updatedAtInWords    = item.updatedAtInWords;
            this.url                 = item.url;
            this.stocked             = item.stocked != null && item.stocked;
        }

        private Cache(Cursor cursor) {
            uuid                = cursor.getString(0);
            userName            = cursor.getString(1);
            userUrlName         = cursor.getString(2);
            userProfileImageUrl = cursor.getString(3);
            title               = cursor.getString(4);
            body                = cursor.getString(5);
            createdAt           = cursor.getLong(6);
            updatedAt           = cursor.getLong(7);
            checkedAt           = cursor.getLong(8);
            createdAtInWords    = cursor.getString(9);
            updatedAtInWords    = cursor.getString(10);
            url                 = cursor.getString(11);
            stocked             = cursor.getInt(12) != 0;
        }

        public Item toItem() {
            Item item = new Item();

            item.uuid               = this.uuid;
            item.user               = new User(userName, userUrlName, userProfileImageUrl);
            item.title              = this.title;
            item.body               = this.body;
            item.createdAt          = CalendarUtil.format(this.createdAt);
            item.updatedAt          = CalendarUtil.format(this.updatedAt);
            item.createdAtInWords   = this.createdAtInWords;
            item.updatedAtInWords   = this.updatedAtInWords;
            item.url                = this.url;
            item.stocked            = this.stocked;

            return item;
        }

        public void checked(SQLiteDatabase db) {
            ContentValues cv = new ContentValues();
            checkedAt = System.currentTimeMillis();
            cv.put(CHECKED_TIME_IN_MILLIS, checkedAt);
            String selection = UUID + "='" + uuid + "'";
            db.update(ITEMS, cv, selection, null);
        }

        public boolean isUnread() {
            return checkedAt < createdAt;
        }

        public boolean isUpdated() {
            return checkedAt < updatedAt;
        }

        public static Holder holder;

        public static Holder getHolder() {
            if (holder == null) {
                holder = new Holder();
            }
            return holder;
        }

        public static class Holder {
            Map<String, Cache> items = new HashMap<>();

            public void load(SQLiteDatabase db) {
                items.clear();

                Cursor cursor = db.query(ITEMS, columns, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        Cache cache = new Cache(cursor);
                        items.put(cache.uuid, cache);
                    } while (cursor.moveToNext());
                }
            }

            public Cache get(String uuid) {
                return items.get(uuid);
            }

            public void save(SQLiteDatabase db, Item item) {
                ContentValues cv = new ContentValues();
                Cache cache = new Cache(item);

                Cache old = items.get(item.uuid);
                if (old != null) {
                    cache.checkedAt = old.checkedAt;
                }

                cv.put(UUID,                    cache.uuid);
                cv.put(USER_NAME,               cache.userName);
                cv.put(USER_URL_NAME,           cache.userUrlName);
                cv.put(USER_PROFILE_IMAGE_URL,  cache.userProfileImageUrl);
                cv.put(TITLE,                   cache.title);
                cv.put(BODY,                    cache.body);
                cv.put(CREATED_AT_IN_WORDS,     cache.createdAtInWords);
                cv.put(UPDATED_AT_IN_WORDS,     cache.updatedAtInWords);
                cv.put(CREATED_TIME_IN_MILLIS,  cache.createdAt);
                cv.put(UPDATED_TIME_IN_MILLIS,  cache.updatedAt);
                cv.put(CHECKED_TIME_IN_MILLIS,  cache.checkedAt);
                cv.put(URL,                     cache.url);
                cv.put(STOCKED,                 cache.stocked ? 1 : 0);

                String selection = UUID + "='" + cache.uuid + "'";
                if (db.update(ITEMS, cv, selection, null) == 0) {
                    db.insert(ITEMS, null, cv);
                }
                item.saved = true;

                items.put(cache.uuid, cache);
            }
        }
    }
}
