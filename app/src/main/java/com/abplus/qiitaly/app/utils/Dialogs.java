package com.abplus.qiitaly.app.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import com.abplus.qiitaly.app.R;

/**
 * 各種ダイアログ表示用ユーティリティ
 *
 * Created by kazhida on 2014/08/05.
 */
public class Dialogs {

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

    public static ProgressDialog startLoading(Context context) {
        return startLoading(context, null);
    }
}
