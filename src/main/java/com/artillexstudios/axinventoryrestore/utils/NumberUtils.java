package com.artillexstudios.axinventoryrestore.utils;

import org.jetbrains.annotations.Nullable;

public class NumberUtils {

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isNumeric(@Nullable String strNum) {

        if (strNum == null) return false;

        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isInt(@Nullable String strNum) {

        if (strNum == null) return false;

        try {
            Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }
}
