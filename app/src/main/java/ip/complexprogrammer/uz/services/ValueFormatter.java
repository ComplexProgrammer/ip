package ip.complexprogrammer.uz.services;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/** Turns raw API values into text people understand. Pure Java so it can be unit tested. */
public final class ValueFormatter {
    private ValueFormatter() {
    }

    /** "uz,ru,tg" or "en-US,es-US" -> "Uzbek, Russian, Tajik" in the display locale. */
    public static String languages(String codes, Locale display) {
        if (codes == null || codes.trim().isEmpty()) return null;
        List<String> names = new ArrayList<>();
        for (String raw : codes.split(",")) {
            String code = raw.trim();
            if (code.isEmpty()) continue;
            String lang = code.split("[-_]")[0];
            String name = new Locale(lang).getDisplayLanguage(display);
            if (name.isEmpty() || name.equalsIgnoreCase(lang)) name = code;
            name = capitalize(name, display);
            if (!names.contains(name)) names.add(name);
        }
        return names.isEmpty() ? null : join(names);
    }

    /** "UZS" -> "UZS · Uzbekistani Som" (localized when the platform knows the currency). */
    public static String currency(String code, String fallbackName, Locale display) {
        if (code == null) return null;
        String name = null;
        try {
            name = Currency.getInstance(code).getDisplayName(display);
        } catch (IllegalArgumentException ignored) {
        }
        if (name == null || name.equalsIgnoreCase(code)) name = fallbackName;
        name = name == null ? null : capitalize(name, display);
        return name == null ? code : code + " · " + name;
    }

    /** "+0500" -> "UTC+05:00", "-0330" -> "UTC-03:30". */
    public static String utcOffset(String offset) {
        if (offset == null) return null;
        String o = offset.trim().replace(":", "");
        if (o.length() == 5 && (o.charAt(0) == '+' || o.charAt(0) == '-')) {
            return "UTC" + o.substring(0, 3) + ":" + o.substring(3);
        }
        return "UTC" + offset;
    }

    /** Current local time in the given IANA time zone, e.g. "14:32". Null when the zone is unknown. */
    public static String currentTime(String timezoneId, Date now, Locale display) {
        if (timezoneId == null) return null;
        TimeZone tz = TimeZone.getTimeZone(timezoneId);
        if (tz.getID().equals("GMT") && !timezoneId.equals("GMT")) return null;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm, EEE", display);
        format.setTimeZone(tz);
        return format.format(now);
    }

    public static String number(Number value, Locale display) {
        if (value == null) return null;
        return NumberFormat.getIntegerInstance(display).format(value);
    }

    public static String area(Double km2, Locale display) {
        if (km2 == null) return null;
        return NumberFormat.getIntegerInstance(display).format(Math.round(km2)) + " km²";
    }

    public static String coordinates(Double lat, Double lon) {
        if (lat == null || lon == null) return null;
        return String.format(Locale.ROOT, "%.4f, %.4f", lat, lon);
    }

    private static String capitalize(String s, Locale locale) {
        if (s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(locale) + s.substring(1);
    }

    private static String join(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(item);
        }
        return sb.toString();
    }
}
