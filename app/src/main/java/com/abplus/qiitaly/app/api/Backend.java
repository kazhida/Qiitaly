package com.abplus.qiitaly.app.api;

import android.os.Handler;
import com.abplus.qiitaly.app.models.Item;
import com.abplus.qiitaly.app.models.Tag;
import com.abplus.qiitaly.app.models.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

/**
 * APIでのやりとりをするクラス
 *
 * Created by kazhida on 2014/07/30.
 */
public class Backend {

    public interface CommonCallback {
        void onException(Throwable throwable);
        void onError(String errorReason);
    }

    private interface AuthCallback extends CommonCallback {
        void onSuccess(AuthResponse authResponse);
    }

    public interface RateLimitCallback extends CommonCallback {
        void onSuccess(RateLimitResponse rateLimit);
    }

    public interface UserCallback extends CommonCallback {
        void onSuccess(User user);
    }

    public interface UsersCallback extends CommonCallback {
        void onSuccess(List<User> users);
    }

    public interface ItemCallback extends CommonCallback {
        void onSuccess(Item item);
    }

    public interface ItemsCallback extends CommonCallback {
        void onSuccess(List<Item> items);
    }

    public interface TagsCallback extends CommonCallback {
        void onSuccess(List<Tag> tags);
    }

    private Backend() {}

    private static Backend instance;

    public static Backend sharedInstance() {
        if (instance == null) {
            instance = new Backend();
        }
        return instance;
    }

    private static class AuthResponse {
        @Getter @Expose
        private String token;
        @Getter @Expose @SerializedName("url_name")
        private String urlName;
    }

    private static class ErrorResponse {
        @Getter @Expose
        String error;
    }

    public static class RateLimitResponse {
        @Expose
        private Integer remaining;
        @Expose
        private Integer limit;

        public int getRemaining() {
            return remaining == null ? 0 : remaining;
        }

        public int getLimit() {
            return limit == null ? 0 : limit;
        }
    }

    private AuthResponse auth;
    private RateLimitResponse rateLimit = new RateLimitResponse();

    public String urlName() {
        if (auth == null)  {
            return null;
        } else {
            return auth.urlName;
        }
    }

    public boolean isLoggedIn() {
        return auth != null && auth.token != null && auth.urlName != null;
    }

    public void logout() {
        auth = null;
    }
}
