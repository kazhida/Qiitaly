package com.abplus.qiitaly.app.api;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import com.abplus.qiitaly.app.api.models.Auth;
import com.abplus.qiitaly.app.api.models.Item;
import com.abplus.qiitaly.app.api.models.Tag;
import com.abplus.qiitaly.app.api.models.User;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * APIでのやりとりをするクラス
 *
 * Created by kazhida on 2014/07/30.
 */
public class Backend {

    private static final String SCHEMA = "https";
    private static final String HOST = "qiita.com";
    private static final String PATH = "/api/v1";

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
    @Getter
    private User current;
//    private RateLimitResponse rateLimit = new RateLimitResponse();

    public String getUrlName() {
        if (auth == null)  {
            return null;
        } else {
            return auth.getUrlName();
        }
    }

    public boolean isLoggedIn() {
        return auth != null && auth.getToken() != null && auth.getUrlName() != null;
    }

    public void logout() {
        auth = null;
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
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        Uri uri;

        RequestBuilder(String path) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(SCHEMA);
            builder.encodedAuthority(HOST);
            if (! path.startsWith("/")) path = "/" + path;
            if (! path.endsWith("/"))   path = path + "/";
            builder.path(PATH + path);
            if (auth != null) {
                builder.appendQueryParameter("token", auth.getToken());
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
                            case HttpStatus.SC_NOT_FOUND:
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
                return httpClient.execute(request, new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(final HttpResponse httpResponse) throws ResponseException, IOException {
                        int statusCode = httpResponse.getStatusLine().getStatusCode();
                        String entity = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                        switch (statusCode) {
                            case HttpStatus.SC_OK:
                                return entity;
                            case HttpStatus.SC_NO_CONTENT:
                                return "";
                            case HttpStatus.SC_BAD_REQUEST:
                            case HttpStatus.SC_FORBIDDEN:
                            case HttpStatus.SC_NOT_FOUND:
                                throw new ResponseException(statusCode, entity);
                            default:
                                throw new ResponseException(statusCode, httpResponse.getStatusLine() + ": \n" +entity);
                        }
                    }
                });
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    //token取得
    //POST /api/v1/auth
    public void auth(String userName, String password, final AuthCallback callback) {
        final RequestBuilder builder = new RequestBuilder("auth");
        builder.addParam("url_name", userName);
        builder.addParam("password", password);

        new AsyncTask<Void, Void, Auth>() {
            @Override
            protected Auth doInBackground(Void... voids) {
                try {
                    String entity = executeRequest(builder.buildPost());
                    return new Gson().fromJson(entity, Auth.class);
                } catch (Exception e) {
                    builder.handleException(callback, e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(Auth auth) {
                if (auth != null) {
                    setAuth(auth);
                    callback.onSuccess(auth);
                }
            }
        }.execute();
    }

    //リクエストユーザーの情報取得
    //GET /api/v1/user
    public void user(final UserCallback callback) {
        final RequestBuilder builder = new RequestBuilder("user");

        new AsyncTask<Void, Void, User>() {
            @Override
            protected User doInBackground(Void... voids) {
                try {
                    String entity = executeRequest(builder.buildGet());
                    return new Gson().fromJson(entity, User.class);
                } catch (Exception e) {
                    builder.handleException(callback, e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(User user) {
                if (auth != null) {
                    current = user;
                    callback.onSuccess(user);
                }
            }
        }.execute();
    }
}
