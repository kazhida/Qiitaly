package com.abplus.qiitaly.app.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import com.abplus.qiitaly.app.api.models.Auth;
import com.abplus.qiitaly.app.api.models.Item;
import com.abplus.qiitaly.app.api.models.Tag;
import com.abplus.qiitaly.app.api.models.User;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * APIでのやりとりをするクラス
 *
 * Created by kazhida on 2014/07/30.
 */
public class Backend {

    private static final String SCHEMA = "https";
    private static final String HOST   = "qiita.com";
    private static final String PATH   = "/api/v1";
    private static final String KEY_TOKEN    = "KEY_TOKEN";
    private static final String KEY_URL_NAME = "KEY_URL_NAME";

    public interface CommonCallback {
        void onException(Throwable throwable);
        void onError(String errorReason);
    }

    public interface AuthCallback extends CommonCallback {
        void onSuccess(Auth auth);
    }

//    public interface RateLimitCallback extends CommonCallback {
//        void onSuccess(RateLimitResponse rateLimit);
//    }

    public interface UserCallback extends CommonCallback {
        void onSuccess(User user);
    }

    @SuppressWarnings("unused")
    public interface UsersCallback extends CommonCallback {
        void onSuccess(List<User> users);
    }

    @SuppressWarnings("unused")
    public interface ItemCallback extends CommonCallback {
        void onSuccess(Item item);
    }

    @SuppressWarnings("unused")
    public interface ItemsCallback extends CommonCallback {
        void onSuccess(List<Item> items);
    }

    @SuppressWarnings("unused")
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

    private static class ErrorResponse {
        @Getter @Expose
        String error;
    }

//    public static class RateLimitResponse {
//        @Expose
//        private Integer remaining;
//        @Expose
//        private Integer limit;
//
//        public int getRemaining() {
//            return remaining == null ? 0 : remaining;
//        }
//
//        public int getLimit() {
//            return limit == null ? 0 : limit;
//        }
//    }

    private Auth auth;
    @Getter @SuppressWarnings("unused")
    private User current;
//    private RateLimitResponse rateLimit = new RateLimitResponse();

    public String getUrlName() {
        if (auth == null)  {
            return null;
        } else {
            return auth.getUrlName();
        }
    }

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

    @SuppressWarnings("unused")
    public void logout(Context context) {
        auth = null;
        storeAuth(context);
    }

    private void setAuth(Auth auth) {
        this.auth = new Auth(auth);
    }

    private static class ResponseException extends RuntimeException {
        @Getter @SuppressWarnings("unused")
        private int statusCode;

        ResponseException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }
    }

    private class RequestBuilder {

        Handler handler = new Handler();
        List<NameValuePair> params = new ArrayList<>();
        Uri uri;

        RequestBuilder(String path, boolean withAuth, Map<String, String> params) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(SCHEMA);
            builder.encodedAuthority(HOST);
            if (! path.startsWith("/")) path = "/" + path;
            if (! path.endsWith("/"))   path = path + "/";
            builder.path(PATH + path);
            if (auth != null && withAuth) {
                builder.appendQueryParameter("token", auth.getToken());
            }
            if (params != null) {
                for (String key: params.keySet()) {
                    builder.appendQueryParameter(key, params.get(key));
                }
            }
            uri = builder.build();
        }

        void addParam(String name, String value) {
            params.add(new BasicNameValuePair(name, value));
        }

        HttpPost buildPost() throws UnsupportedEncodingException {
            HttpPost post = new HttpPost(uri.toString());
            HttpEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            post.setEntity(entity);
            return post;
        }

        HttpGet buildGet() {
            return new HttpGet(uri.toString());
        }

        void handleException(final CommonCallback callback, final Throwable throwable) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (throwable instanceof ResponseException) {
                        ResponseException responseException = (ResponseException) throwable;
                        switch (responseException.statusCode) {
                            case HttpStatus.SC_BAD_REQUEST:
                            case HttpStatus.SC_FORBIDDEN:
                            case HttpStatus.SC_UNAUTHORIZED:
                                ErrorResponse errorResponse = new Gson().fromJson(responseException.getMessage(), ErrorResponse.class);
                                callback.onError(errorResponse.error);
                                break;
                            default:
                                callback.onError(throwable.getMessage());
                                break;
                        }
                    } else {
                        callback.onException(throwable);
                    }
                }
            });
        }
    }

    private String executeRequest(final HttpUriRequest request) throws IOException, ResponseException {
        if (request == null) {
            throw new ResponseException(0, "Request is null.");
        } else {
            final DefaultHttpClient httpClient = new DefaultHttpClient();

            try {
                HttpResponse httpResponse = httpClient.execute(request);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                String entity = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                switch (statusCode) {
                    case HttpStatus.SC_OK:
                        return entity;
                    case HttpStatus.SC_NO_CONTENT:
                        return "";
                    case HttpStatus.SC_BAD_REQUEST:
                    case HttpStatus.SC_FORBIDDEN:
                    case HttpStatus.SC_UNAUTHORIZED:
                        throw new ResponseException(statusCode, entity);
                    default:
                        throw new ResponseException(statusCode, httpResponse.getStatusLine() + ": \n" +entity);
                }
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    //  token取得
    //  POST /api/v1/auth
    public void auth(final Context context, String userName, String password, final AuthCallback callback) {
        final RequestBuilder builder = new RequestBuilder("auth", true, null);
        builder.addParam("url_name", userName);
        builder.addParam("password", password);

        new AsyncTask<Void, Void, Auth>() {
            @Override
            protected Auth doInBackground(@Nullable Void... voids) {
                try {
                    String entity = executeRequest(builder.buildPost());
                    return new Gson().fromJson(entity, Auth.class);
                } catch (Exception e) {
                    builder.handleException(callback, e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(@Nullable Auth auth) {
                if (auth != null) {
                    setAuth(auth);
                    storeAuth(context);
                    callback.onSuccess(auth);
                }
            }
        }.execute();
    }

    //  リクエストユーザーの情報取得
    //  GET /api/v1/user
    public void user(final UserCallback callback) {
        final RequestBuilder builder = new RequestBuilder("user", true, null);

        new AsyncTask<Void, Void, User>() {
            @Override
            protected User doInBackground(@Nullable Void... voids) {
                try {
                    String entity = executeRequest(builder.buildGet());
                    return new Gson().fromJson(entity, User.class);
                } catch (Exception e) {
                    builder.handleException(callback, e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(@Nullable User user) {
                if (user != null) {
                    current = user;
                    callback.onSuccess(user);
                }
            }
        }.execute();
    }

    //  特定ユーザーのフォローしているユーザー取得
    //  GET /api/v1/users/:url_name/following_users
    public void usersFollowingUsers(String urlName, final UsersCallback callback) {
        final RequestBuilder builder = new RequestBuilder("users/" + urlName + "/following_users", true, null);

        new AsyncTask<Void, Void, List<User>>() {
            @Override
            protected List<User> doInBackground(@Nullable Void... voids) {
                try {
                    String entity = executeRequest(builder.buildGet());
                    return new Gson().fromJson(entity, new TypeToken<List<User>>(){}.getType());
                } catch (Exception e) {
                    builder.handleException(callback, e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(@Nullable List<User> users) {
                if (users != null) {
                    callback.onSuccess(users);
                }
            }
        }.execute();
    }

    //  特定ユーザーのフォローしているタグ取得
    //  GET /api/v1/users/:url_name/following_tags
    public void usersFollowingTags(String urlName, final TagsCallback callback) {
        final RequestBuilder builder = new RequestBuilder("users/" + urlName + "/following_tags", true, null);

        new AsyncTask<Void, Void, List<Tag>>() {
            @Override
            protected List<Tag> doInBackground(@Nullable Void... voids) {
                try {
                    String entity = executeRequest(builder.buildGet());
                    return new Gson().fromJson(entity, new TypeToken<List<Tag>>(){}.getType());
                } catch (Exception e) {
                    builder.handleException(callback, e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(@Nullable List<Tag> tags) {
                if (tags != null) {
                    callback.onSuccess(tags);
                }
            }
        }.execute();
    }

    //  新着投稿の取得
    //  GET /api/v1/items
    public void items(boolean withAuth, final ItemsCallback callback) {
        final RequestBuilder builder = new RequestBuilder("items", withAuth, null);

        new AsyncTask<Void, Void, List<Item>>() {
            @Override
            protected List<Item> doInBackground(@Nullable Void... voids) {
                try {
                    String entity = executeRequest(builder.buildGet());
                    return new Gson().fromJson(entity, new TypeToken<List<Item>>(){}.getType());
                } catch (Exception e) {
                    builder.handleException(callback, e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(@Nullable List<Item> items) {
                if (items != null) {
                    callback.onSuccess(items);
                }
            }
        }.execute();
    }

    //自分のストックした投稿の取得
    //GET /api/v1/stocks
    public void stocks(final ItemsCallback callback) {
        final RequestBuilder builder = new RequestBuilder("stocks", true, null);

        new AsyncTask<Void, Void, List<Item>>() {
            @Override
            protected List<Item> doInBackground(@Nullable Void... voids) {
                try {
                    String entity = executeRequest(builder.buildGet());
                    return new Gson().fromJson(entity, new TypeToken<List<Item>>(){}.getType());
                } catch (Exception e) {
                    builder.handleException(callback, e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(@Nullable List<Item> items) {
                if (items != null) {
                    callback.onSuccess(items);
                }
            }
        }.execute();
    }

    //特定ユーザーの投稿取得
    //GET /api/v1/users/:url_name/userItems
    public void userItems(String urlName, final ItemsCallback callback) {
        final RequestBuilder builder = new RequestBuilder("users/" + urlName + "/items", true, null);

        new AsyncTask<Void, Void, List<Item>>() {
            @Override
            protected List<Item> doInBackground(@Nullable Void... voids) {
                try {
                    String entity = executeRequest(builder.buildGet());
                    return new Gson().fromJson(entity, new TypeToken<List<Item>>(){}.getType());
                } catch (Exception e) {
                    builder.handleException(callback, e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(@Nullable List<Item> items) {
                if (items != null) {
                    callback.onSuccess(items);
                }
            }
        }.execute();
    }

    //特定タグの投稿取得
    //GET /api/v1/tags/:url_name/userItems
    public void tagItems(String tag, final ItemsCallback callback) {
        final RequestBuilder builder = new RequestBuilder("tags/" + tag + "/items", true, null);

        new AsyncTask<Void, Void, List<Item>>() {
            @Override
            protected List<Item> doInBackground(@Nullable Void... voids) {
                try {
                    String entity = executeRequest(builder.buildGet());
                    return new Gson().fromJson(entity, new TypeToken<List<Item>>(){}.getType());
                } catch (Exception e) {
                    builder.handleException(callback, e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(@Nullable List<Item> items) {
                if (items != null) {
                    callback.onSuccess(items);
                }
            }
        }.execute();
    }

    //  検索結果取得
    //  GET /api/v1/search
    public void search(String query, final ItemsCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        final RequestBuilder builder = new RequestBuilder("search", true, params);

        new AsyncTask<Void, Void, List<Item>>() {
            @Override
            protected List<Item> doInBackground(@Nullable Void... voids) {
                try {
                    String entity = executeRequest(builder.buildGet());
                    return new Gson().fromJson(entity, new TypeToken<List<Item>>(){}.getType());
                } catch (Exception e) {
                    builder.handleException(callback, e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(@Nullable List<Item> items) {
                if (items != null) {
                    callback.onSuccess(items);
                }
            }
        }.execute();
    }
}
