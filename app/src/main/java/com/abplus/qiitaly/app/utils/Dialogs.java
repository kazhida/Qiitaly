package com.abplus.qiitaly.app.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import com.abplus.qiitaly.app.R;
import org.jetbrains.annotations.NotNull;

/**
 * 各種ダイアログ表示用ユーティリティ
 *
 * Created by kazhida on 2014/08/05.
 */
public class Dialogs {

    @SuppressWarnings("unused")
    public static void errorMessage(Context context, int title, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.close, null);
        builder.create().show();
    }

    public static void errorMessage(Context context, int title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.close, null);
        builder.create().show();
    }

    public static ProgressDialog startLoading(Context context, String caption) {
        ProgressDialog dialog = new ProgressDialog(context);
        if (caption != null) {
            dialog.setTitle(caption);
        } else {
            dialog.setTitle(R.string.loading);
        }
        dialog.show();
        return dialog;
    }

    public static AlertDialog confirm(Context context, String title, String message, final Runnable callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(@NotNull DialogInterface dialog, int which) {
                callback.run();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        return builder.show();
    }

    public static AlertDialog confirm(Context context, int titleId, int messageId, final Runnable callback) {
        return confirm(context, context.getString(titleId), context.getString(messageId), callback);
    }

    public static ProgressDialog startLoading(Context context) {
        return startLoading(context, null);
    }
}
