package ip.complexprogrammer.uz.services;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/** App-wide settings: theme, language, ad-free period and a few counters. */
public final class AppPrefs {
    private static final String NAME = "settings";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String KEY_AD_FREE_UNTIL = "ad_free_until";
    private static final String KEY_SEARCH_COUNT = "search_count";
    private static final String KEY_LAST_OWN_IP = "last_own_ip";
    private static final String KEY_LANG_MIGRATED = "lang_migrated";

    private AppPrefs() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    // Theme

    public static int getNightMode(Context c) {
        return prefs(c).getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void setNightMode(Context c, int mode) {
        prefs(c).edit().putInt(KEY_NIGHT_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    // Language (per-app locales; stored by AppCompat, shown in system settings on Android 13+)

    public static String getLanguage() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        return locales.isEmpty() ? "" : locales.get(0).getLanguage();
    }

    public static void setLanguage(String languageTag) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag));
    }

    /** Moves the language chosen in versions up to 1.2.x to the per-app locale API. Call from onCreate. */
    public static void migrateLegacyLanguage(Context c) {
        if (prefs(c).getBoolean(KEY_LANG_MIGRATED, false)) return;
        prefs(c).edit().putBoolean(KEY_LANG_MIGRATED, true).apply();
        String legacy = c.getSharedPreferences("C0mplexLanguage", Context.MODE_PRIVATE).getString("langCode", null);
        if (legacy != null && AppCompatDelegate.getApplicationLocales().isEmpty()) setLanguage(legacy);
    }

    // Ads

    public static boolean isAdFree(Context c) {
        return System.currentTimeMillis() < prefs(c).getLong(KEY_AD_FREE_UNTIL, 0);
    }

    public static long getAdFreeUntil(Context c) {
        return prefs(c).getLong(KEY_AD_FREE_UNTIL, 0);
    }

    public static void grantAdFree(Context c, long durationMs) {
        long base = Math.max(System.currentTimeMillis(), getAdFreeUntil(c));
        prefs(c).edit().putLong(KEY_AD_FREE_UNTIL, base + durationMs).apply();
    }

    /** Increments and returns the number of manual lookups. */
    public static int incrementSearchCount(Context c) {
        int count = prefs(c).getInt(KEY_SEARCH_COUNT, 0) + 1;
        prefs(c).edit().putInt(KEY_SEARCH_COUNT, count).apply();
        return count;
    }

    // Own IP change detection

    public static String getLastOwnIp(Context c) {
        return prefs(c).getString(KEY_LAST_OWN_IP, null);
    }

    public static void setLastOwnIp(Context c, String ip) {
        prefs(c).edit().putString(KEY_LAST_OWN_IP, ip).apply();
    }
}
