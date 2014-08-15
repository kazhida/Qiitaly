package com.abplus.qiitaly.app.api.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    @Getter @Setter @SuppressWarnings("unused")
    private String nextUrl;
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

        public static final String CREATE_ITEMS = "create table " + ITEMS + " (" +
                UUID                    + " text primary key,   " +
                USER_NAME               + " text,               " +
                USER_URL_NAME           + " text,               " +
                USER_PROFILE_IMAGE_URL  + " text,               " +
                TITLE                   + " text,               " +
                BODY                    + " text,               " +
                CREATED_TIME_IN_MILLIS  + " integer,            " +
                UPDATED_TIME_IN_MILLIS  + " integer,            " +
                CHECKED_TIME_IN_MILLIS  + " integer);           ";

        private static final String[] columns = new String[] {
                UUID,
                USER_NAME,
                USER_URL_NAME,
                USER_PROFILE_IMAGE_URL,
                TITLE,
                BODY,
                CREATED_TIME_IN_MILLIS,
                UPDATED_TIME_IN_MILLIS,
                CHECKED_TIME_IN_MILLIS
        };

        private String  uuid;
        private String  userName;
        private String  userUrlName;
        private String  userProfileImageUrl;
        private String  title;
        private String  body;
        private long    createdAt;
        private long    updatedAt;
        private long    checkedAt = 0;

        private Cache(Item item) {
            this.uuid                = item.uuid;
            this.userName            = item.user.getName();
            this.userUrlName         = item.user.getUrlName();
            this.userProfileImageUrl = item.user.getProfileImageUrl();
            this.title               = item.title;
            this.body                = item.body;
            try {
                this.createdAt = timeFormat.parse(item.createdAt).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            try {
                this.updatedAt = timeFormat.parse(item.updatedAt).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
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
        }

        public static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

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
                cv.put(CREATED_TIME_IN_MILLIS,  cache.createdAt);
                cv.put(UPDATED_TIME_IN_MILLIS,  cache.updatedAt);
                cv.put(CHECKED_TIME_IN_MILLIS,  cache.checkedAt);

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
