package ip.complexprogrammer.uz.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Map;

import ip.complexprogrammer.uz.models.IpInfo;

/** Keeps lookup results for an hour so repeated lookups do not spend the free API quota. */
public class IpCache {
    private static final long TTL_MS = 60 * 60 * 1000L;
    private static final int MAX_ENTRIES = 50;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public IpCache(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences("ip_cache", Context.MODE_PRIVATE);
    }

    public IpInfo get(String ip) {
        String json = prefs.getString(ip, null);
        if (json == null) return null;
        try {
            IpInfo info = gson.fromJson(json, IpInfo.class);
            if (info == null || System.currentTimeMillis() - info.fetchedAt > TTL_MS) return null;
            return info;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public void put(IpInfo info) {
        if (info == null || info.ip == null) return;
        SharedPreferences.Editor editor = prefs.edit();
        Map<String, ?> all = prefs.getAll();
        if (all.size() >= MAX_ENTRIES) {
            long now = System.currentTimeMillis();
            for (Map.Entry<String, ?> entry : all.entrySet()) {
                try {
                    IpInfo cached = gson.fromJson(String.valueOf(entry.getValue()), IpInfo.class);
                    if (cached == null || now - cached.fetchedAt > TTL_MS) editor.remove(entry.getKey());
                } catch (RuntimeException e) {
                    editor.remove(entry.getKey());
                }
            }
        }
        editor.putString(info.ip, gson.toJson(info)).apply();
    }
}
