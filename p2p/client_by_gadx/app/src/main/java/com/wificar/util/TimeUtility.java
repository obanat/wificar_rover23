package com.wificar.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/* loaded from: classes.dex */
public class TimeUtility {
    public static Date convertTimeStrToDate(String str) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parsedDate = dateFormat.parse(str);
        return parsedDate;
    }

    public static String getCurrentTimeStr() throws ParseException {
        Date d = new Date(System.currentTimeMillis());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return dateFormat.format(d);
    }

    public static String convertDateToTimeStr(long timestamp, String format) {
        Date d = new Date(timestamp);
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(d);
    }

    public static String convertDateToTimeStr(Date date, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static String convertDateToTimeStr(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date());
    }
}
