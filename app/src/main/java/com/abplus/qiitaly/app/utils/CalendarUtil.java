package com.abplus.qiitaly.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 日付関連のユーティリティ
 *
 * Created by kazhida on 2014/08/19.
 */
public class CalendarUtil {

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    public static long parse(String time) {
        try {
            return timeFormat.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String format(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return timeFormat.format(calendar.getTime());
    }
}
