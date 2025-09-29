package com.artillexstudios.axinventoryrestore.utils;

import java.text.SimpleDateFormat;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class DateUtils {
    private static SimpleDateFormat sdf = null;

    public static void reload() {
        sdf = new SimpleDateFormat(CONFIG.getString("date-format", "yyyy/MM/dd HH:mm:ss"));
    }

    public static String formatDate(long date) {
        if (sdf == null) reload();
        return sdf.format(date);
    }
}
