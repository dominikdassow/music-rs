package de.dominikdassow.musicrs.util;

import com.google.common.base.CharMatcher;

import java.util.Locale;
import java.util.Objects;

public class Formatter {

    private Formatter() {}

    public static String format(Double value) {
        if (Objects.isNull(value)) return "";

        final String s = String.format(Locale.US, "%.4f", value);

        if (s.equals("0.0000")) return "0";

        String trimmed = CharMatcher.anyOf("0").trimTrailingFrom(s);

        if (trimmed.charAt(trimmed.length() - 1) == '.') trimmed += "0";

        return trimmed;
    }

    public static String format(Float value) {
        return format(value.doubleValue());
    }
}
