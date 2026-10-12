package ip.complexprogrammer.uz.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ip.complexprogrammer.uz.models.HistoryEntry;
import ip.complexprogrammer.uz.models.IpInfo;

/** Recent lookups (newest first). Favorites are never trimmed. */
public class HistoryStore {
    private static final int MAX_RECENT = 30;
    private static final String KEY = "entries";
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public HistoryStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences("history", Context.MODE_PRIVATE);
    }

    public synchronized List<HistoryEntry> getAll() {
        String json = prefs.getString(KEY, null);
        if (json == null) return new ArrayList<>();
        try {
            List<HistoryEntry> list = gson.fromJson(json, new TypeToken<List<HistoryEntry>>() {
            }.getType());
            return list == null ? new ArrayList<>() : list;
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }

    /** Favorites first, then the rest, each group newest first. */
    public List<HistoryEntry> getSorted() {
        List<HistoryEntry> list = getAll();
        Collections.sort(list, (a, b) -> {
            if (a.favorite != b.favorite) return a.favorite ? -1 : 1;
            return Long.compare(b.time, a.time);
        });
        return list;
    }

    public synchronized void add(IpInfo info) {
        if (info == null || info.ip == null) return;
        List<HistoryEntry> list = getAll();
        boolean favorite = false;
        for (Iterator<HistoryEntry> it = list.iterator(); it.hasNext(); ) {
            HistoryEntry e = it.next();
            if (info.ip.equals(e.ip)) {
                favorite = e.favorite;
                it.remove();
            }
        }
        HistoryEntry entry = HistoryEntry.from(info);
        entry.favorite = favorite;
        list.add(0, entry);
        int recent = 0;
        for (Iterator<HistoryEntry> it = list.iterator(); it.hasNext(); ) {
            HistoryEntry e = it.next();
            if (e.favorite) continue;
            if (++recent > MAX_RECENT) it.remove();
        }
        save(list);
    }

    public synchronized boolean contains(String ip) {
        for (HistoryEntry e : getAll()) if (e.ip.equals(ip)) return true;
        return false;
    }

    public synchronized boolean isFavorite(String ip) {
        for (HistoryEntry e : getAll()) if (e.ip.equals(ip)) return e.favorite;
        return false;
    }

    /** Toggles the favorite flag and returns the new value. */
    public synchronized boolean toggleFavorite(String ip) {
        List<HistoryEntry> list = getAll();
        boolean value = false;
        for (HistoryEntry e : list) {
            if (e.ip.equals(ip)) {
                e.favorite = !e.favorite;
                value = e.favorite;
            }
        }
        save(list);
        return value;
    }

    public synchronized void remove(String ip) {
        List<HistoryEntry> list = getAll();
        for (Iterator<HistoryEntry> it = list.iterator(); it.hasNext(); ) {
            if (it.next().ip.equals(ip)) it.remove();
        }
        save(list);
    }

    /** Removes everything except favorites. */
    public synchronized void clearRecent() {
        List<HistoryEntry> list = getAll();
        for (Iterator<HistoryEntry> it = list.iterator(); it.hasNext(); ) {
            if (!it.next().favorite) it.remove();
        }
        save(list);
    }

    public synchronized void replaceAll(List<HistoryEntry> list) {
        save(list);
    }

    private void save(List<HistoryEntry> list) {
        prefs.edit().putString(KEY, gson.toJson(list)).apply();
    }
}
