package de.uka.iti.pseudo.util;

import nonnull.NonNull;

public class StringUtil {

    public static String ellipsisEnd(@NonNull String str, int length) {
        if (str == null) {
            return null;
        }

        int strlen = str.length();
        if (strlen <= length) {
            return str;
        }

        StringBuilder sb = new StringBuilder();
        // -5 because of " ... "
        sb.append(str.substring(0, length - 5))
                .append(" ... ");

        return sb.toString();
    }

    public static String ellipsisMiddle(@NonNull String str, int length) {

        if (str == null) {
            return null;
        }

        int strlen = str.length();
        if (strlen <= length) {
            return str;
        }

        StringBuilder sb = new StringBuilder();
        // -5 because of " ... "
        int section1 = (length - 5) / 2;
        int section2 = (length - 5) - section1;

        sb.append(str.substring(0, section1))
                .append(" ... ")
                .append(str.substring(strlen - section2, strlen));

        return sb.toString();
    }
}
