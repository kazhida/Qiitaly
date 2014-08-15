package com.abplus.qiitaly.app.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.abplus.qiitaly.app.api.models.Auth;
import com.abplus.qiitaly.app.api.models.Item;
import com.abplus.qiitaly.app.api.models.Tag;
import com.abplus.qiitaly.app.api.models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * APIでのやりとりをするクラス
 *
 * Created by kazhida on 2014/07/30.
 */
public class Backend {

    private static final String KEY_TOKEN    = "KEY_TOKEN";
    private static final String KEY_URL_NAME = "KEY_URL_NAME";

    public interface Callback<T> {
        void onSuccess(T result, @Nullable String nextUrl);
        void onException(Throwable throwable);
        void onError(String errorReason);
    }

    private Backend() {}

    private static Backend instance;

    public static Backend sharedInstance() {
        if (instance == null) {
            instance = new Backend();
        }
        return instance;
    }

    private Auth auth;
    @Getter @SuppressWarnings("unused")
    private User current;

//    public String getUrlName() {
//        if (auth == null)  {
//            return null;
//        } else {
//            return auth.getUrlName();
//        }
//    }

    public void restoreAuth(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String token    = preferences.getString(KEY_TOKEN, null);
        String urlName  = preferences.getString(KEY_URL_NAME, null);
        if (token != null && urlName != null) {
            auth = new Auth(token, urlName);
        }
    }

    @SuppressLint("CommitPrefEdits")
    private void storeAuth(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (auth != null) {
            editor.putString(KEY_TOKEN,     auth.getToken());
            editor.putString(KEY_URL_NAME,  auth.getUrlName());
        } else {
            editor.remove(KEY_TOKEN);
            editor.remove(KEY_URL_NAME);
        }
        editor.commit();
    }

    public boolean isLoggedIn() {
        return auth != null && auth.getToken() != null && auth.getUrlName() != null;
    }

    public void logout(Context context) {
        auth = null;
        storeAuth(context);
    }

    private void setAuth(Auth auth) {
        this.auth = new Auth(auth);
    }

    interface EntityEvaluator<T> {
        T fromEntity(@Nullable String entity);
    }

    interface PostProcess<T> {
        void onPostProcess(T result);
    }

    //  token取得
    //  POST /api/v1/auth
    public void auth(@NotNull final Context context, @NotNull String userName, @NotNull String password, @NotNull final Callback<Auth> callback) {
        Executor<Auth> executor = new Executor<>("auth", null, new EntityEvaluator<Auth>() {
            @Override
            public Auth fromEntity(@Nullable String entity) {
                return new Gson().fromJson(entity, Auth.class);
            }
        });
        executor.addParam("url_name", userName);
        executor.addParam("password", password);
        executor.post(callback, new PostProcess<Auth>() {
            @Override
            public void onPostProcess(Auth result) {
                setAuth(result);
                storeAuth(context);
            }
        });
    }

    //  リクエストユーザーの情報取得
    //  GET /api/v1/user
    public void user(@NotNull final Callback<User> callback) {
        Executor<User> executor = new Executor<>("user", auth, new EntityEvaluator<User>() {
            @Override
            public User fromEntity(@Nullable String entity) {
                return new Gson().fromJson(entity, User.class);
            }
        });
        executor.get(callback, new PostProcess<User>() {
            @Override
            public void onPostProcess(User result) {
                current = result;
            }
        });
    }

    //  特定ユーザーのフォローしているユーザー取得
    //  GET /api/v1/users/:url_name/following_users
    public void usersFollowingUsers(@NotNull String urlName, @NotNull final Callback<List<User>> callback) {
        Executor<List<User>> executor = new Executor<>("users/" + urlName + "/following_users", auth, new EntityEvaluator<List<User>>() {
            @Override
            public List<User> fromEntity(@Nullable String entity) {
                return new Gson().fromJson(entity, new TypeToken<List<User>>(){}.getType());
            }
        });
        executor.get(callback);
    }

    //  特定ユーザーのフォローしているタグ取得
    //  GET /api/v1/users/:url_name/following_tags
    public void usersFollowingTags(String urlName, final Callback<List<Tag>> callback) {
        Executor<List<Tag>> executor = new Executor<>("users/" + urlName + "/following_tags", auth, new EntityEvaluator<List<Tag>>() {
            @Override
            public List<Tag> fromEntity(@Nullable String entity) {
                return new Gson().fromJson(entity, new TypeToken<List<Tag>>(){}.getType());
            }
        });
        executor.get(callback, null);
    }

    //  新着投稿の取得
    //  GET /api/v1/items
    public void items(boolean withAuth, @NotNull final Callback<List<Item>> callback) {
        Executor<List<Item>> executor = new Executor<>("items", withAuth ? auth : null, new EntityEvaluator<List<Item>>() {
            @Override
            public List<Item> fromEntity(@Nullable String entity) {
                return new Gson().fromJson(entity, new TypeToken<List<Item>>(){}.getType());
            }
        });
        executor.get(callback);
    }

    //  特定の投稿取得
    //  GET /api/v1/items/:uuid
    public void item(@NotNull String uuid, @NotNull final Callback<Item> callback) {
        Executor<Item> executor = new Executor<>("items/" + uuid, auth, new EntityEvaluator<Item>() {
            @Override
            public Item fromEntity(@Nullable String entity) {
                return new Gson().fromJson(entity, Item.class);
            }
        });
        executor.get(callback);
    }

    //  自分のストックした投稿の取得
    //  GET /api/v1/stocks
    public void stocks(@NotNull final Callback<List<Item>> callback) {
        Executor<List<Item>> executor = new Executor<>("stocks", auth, new EntityEvaluator<List<Item>>() {
            @Override
            public List<Item> fromEntity(@Nullable String entity) {
                return new Gson().fromJson(entity, new TypeToken<List<Item>>(){}.getType());
            }
        });
        executor.get(callback);
    }

    //  特定ユーザーの投稿取得
    //  GET /api/v1/users/:url_name/userItems
    public void userItems(@NotNull String urlName, @NotNull final Callback<List<Item>> callback) {
        Executor<List<Item>> executor = new Executor<>("users/" + urlName + "/items", auth, new EntityEvaluator<List<Item>>() {
            @Override
            public List<Item> fromEntity(@Nullable String entity) {
                return new Gson().fromJson(entity, new TypeToken<List<Item>>(){}.getType());
            }
        });
        executor.get(callback);
    }

    //  特定タグの投稿取得
    //  GET /api/v1/tags/:url_name/userItems
    public void tagItems(@NotNull String tag, @NotNull final Callback<List<Item>> callback) {
        Executor<List<Item>> executor = new Executor<>("tags/" + tag + "/items", auth, new EntityEvaluator<List<Item>>() {
            @Override
            public List<Item> fromEntity(@Nullable String entity) {
                return new Gson().fromJson(entity, new TypeToken<List<Item>>(){}.getType());
            }
        });
        executor.get(callback);
    }

    //  検索結果取得
    //  GET /api/v1/search
    public void search(@NotNull String query, @NotNull final Callback<List<Item>> callback) {
        Executor<List<Item>> executor = new Executor<>("search", auth, new EntityEvaluator<List<Item>>() {
            @Override
            public List<Item> fromEntity(@Nullable String entity) {
                return new Gson().fromJson(entity, new TypeToken<List<Item>>(){}.getType());
            }
        });
        executor.addParam("q", query);
        executor.get(callback);
    }

    //  続きの読み込み
    public void moreItems(@NotNull String url, @NotNull final Callback<List<Item>> callback) {
        Executor<List<Item>> executor = new Executor<>(url, new EntityEvaluator<List<Item>>() {
            @Override
            public List<Item> fromEntity(@Nullable String entity) {
                return new Gson().fromJson(entity, new TypeToken<List<Item>>(){}.getType());
            }
        });
        executor.get(callback);
    }

    //  投稿のストック
    //  PUT /api/v1/items/:uuid/stock
    public void stock(@NotNull String uuid, @NotNull final Callback<String> callback) {
        Executor<String> executor = new Executor<>("items/" + uuid + "/stock", auth, new EntityEvaluator<String>() {
            @Override
            public String fromEntity(@Nullable String entity) {
                return "";
            }
        });
        executor.put(callback, null);
    }
}
