package com.example.doannt118.utils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Dateutils {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static String format(Date date) {
        return sdf.format(date);
    }

    public static Date parse(String dateStr) {
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
}
