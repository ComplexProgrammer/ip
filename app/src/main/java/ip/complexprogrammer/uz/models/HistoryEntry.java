package ip.complexprogrammer.uz.models;

public class HistoryEntry {
    public String ip;
    public String query;
    public String countryCode;
    public String countryName;
    public String city;
    public String org;
    public long time;
    public boolean favorite;

    public static HistoryEntry from(IpInfo info) {
        HistoryEntry e = new HistoryEntry();
        e.ip = info.ip;
        e.query = info.query;
        e.countryCode = info.countryCode;
        e.countryName = info.countryName;
        e.city = info.city;
        e.org = info.org;
        e.time = System.currentTimeMillis();
        return e;
    }
}
