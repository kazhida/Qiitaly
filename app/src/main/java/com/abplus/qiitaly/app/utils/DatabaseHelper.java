package com.abplus.qiitaly.app.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.abplus.qiitaly.app.api.models.Item;
import org.jetbrains.annotations.NotNull;

/**
 * DB関連のヘルパークラス
 *
 * Created by kazhida on 2014/08/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "dd.db";
    private static final int DB_VERSION = 3;

    private static DatabaseHelper shared;

    public static DatabaseHelper initInstance(Context context) {
        shared = new DatabaseHelper(context);
        shared.getWritableDatabase().close();
        return shared;
    }

    public static DatabaseHelper sharedInstance() {
        return shared;
    }

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(@NotNull SQLiteDatabase db) {
        db.execSQL(Item.Cache.CREATE_ITEMS);
    }

    @Override
    public void onUpgrade(@NotNull SQLiteDatabase db, int oldVersion, int newVersion) {}

    public interface Writer {
        void onWrite(@NotNull SQLiteDatabase db);
    }

    public interface Reader {
        void onRead(@NotNull SQLiteDatabase db);
    }

    public synchronized void executeWrite(@NotNull Writer writer) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            writer.onWrite(db);
        } finally {
            db.close();
        }
    }

    public synchronized void executeRead(@NotNull Reader reader) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            reader.onRead(db);
        } finally {
            db.close();
        }
    }
}
