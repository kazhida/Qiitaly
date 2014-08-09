package com.abplus.qiitaly.app.api;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import com.abplus.qiitaly.app.api.models.Auth;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 実際にやりとりをするクラス
 *
 * Created by kazhida on 2014/08/09.
 */
class Executor<T> {

    private static final String SCHEMA = "https";
    private static final String HOST   = "qiita.com";
    private static final String PATH   = "/api/v1";

    private Handler handler = new Handler();
    private Map<String, String> params = new HashMap<>();
    private Uri.Builder builder;
    private Backend.EntityEvaluator<T> evaluator;

    public Executor(@NotNull String path, @Nullable Auth auth, @NotNull Backend.EntityEvaluator<T> evaluator) {
        builder = new Uri.Builder();
        builder.scheme(SCHEMA);
        builder.encodedAuthority(HOST);
        if (! path.startsWith("/")) path = "/" + path;
        if (! path.endsWith("/"))   path = path + "/";
        builder.path(PATH + path);
        if (auth != null) {
            builder.appendQueryParameter("token", auth.getToken());
        }
        this.evaluator = evaluator;
    }

    public void addParam(@NotNull String name, @NotNull String value) {
        params.put(name, value);
    }

    private HttpPost buildPost() throws UnsupportedEncodingException {
        HttpPost post = new HttpPost(builder.build().toString());

        List<BasicNameValuePair> postParams = new ArrayList<>();
        for (String key: params.keySet()) {
            postParams.add(new BasicNameValuePair(key, params.get(key)));
        }

        HttpEntity entity = new UrlEncodedFormEntity(postParams, HTTP.UTF_8);
        post.setEntity(entity);

        return post;
    }

    private HttpGet buildGet() {
        for (String key: params.keySet()) {
            builder.appendQueryParameter(key, params.get(key));
        }
        return new HttpGet(builder.build().toString());
    }

    private static class ResponseException extends RuntimeException {
        @Getter @SuppressWarnings("unused")
        private int statusCode;

        ResponseException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }
    }

    private static class ErrorResponse {
        @Getter @Expose
        String error;
    }

    void handleException(final Backend.Callback<T> callback, final Throwable throwable) {
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

    String executeRequest(final HttpUriRequest request) throws IOException, ResponseException {
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

    abstract class AsyncExecute extends AsyncTask<Void, Void, T> {

        Backend.Callback<T> callback;
        Backend.PostProcess<T> postProcess;

        AsyncExecute(@NotNull final Backend.Callback<T> callback, @Nullable final Backend.PostProcess<T> postProcess) {
            super();
            this.callback = callback;
            this.postProcess = postProcess;
        }

        @Override
        protected void onPostExecute(@Nullable T result) {
            if (result != null) {
                if (postProcess != null) {
                    postProcess.onPostProcess(result);
                }
                callback.onSuccess(result);
            }
        }
    }

    void get(@NotNull final Backend.Callback<T> callback, @Nullable Backend.PostProcess<T> postProcess) {

        new AsyncExecute(callback, postProcess) {
            @Override
            protected T doInBackground(@Nullable Void... voids) {
                try {
                    String entity = executeRequest(buildGet());
                    return evaluator.fromEntity(entity);
                } catch (Exception e) {
                    handleException(callback, e);
                    return null;
                }
            }
        }.execute();
    }

    void get(@NotNull final Backend.Callback<T> callback) {
        get(callback, null);
    }

    void post(@NotNull final Backend.Callback<T> callback, @Nullable Backend.PostProcess<T> postProcess) {

        new AsyncExecute(callback, postProcess) {
            @Override
            protected T doInBackground(@Nullable Void... voids) {
                try {
                    String entity = executeRequest(buildPost());
                    return evaluator.fromEntity(entity);
                } catch (Exception e) {
                    handleException(callback, e);
                    return null;
                }
            }
        }.execute();
    }
}
