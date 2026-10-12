package ip.complexprogrammer.uz.models;

/** Provider-independent IP information shown in the app, cached and stored in history. */
public class IpInfo {
    public String ip;
    public String version;
    public String city;
    public String region;
    public String regionCode;
    public String postal;
    public Double latitude;
    public Double longitude;
    public String countryCode;
    public String countryCodeIso3;
    public String countryName;
    public String countryCapital;
    public String countryTld;
    public Double countryArea;
    public Long countryPopulation;
    public String continentCode;
    public Boolean inEu;
    public String timezone;
    public String utcOffset;
    public String callingCode;
    public String currency;
    public String currencyName;
    public String languages;
    public String asn;
    public String org;
    /** Host name the user typed when it was resolved to {@link #ip}, otherwise null. */
    public String query;
    /** Host name of the API that answered. */
    public String source;
    public long fetchedAt;

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    public static IpInfo fromIpApi(IpApiResponse r) {
        IpInfo i = new IpInfo();
        i.ip = r.ip;
        i.version = r.version;
        i.city = clean(r.city);
        i.region = clean(r.region);
        i.regionCode = clean(r.region_code);
        i.postal = clean(r.postal);
        i.latitude = parseDouble(r.latitude);
        i.longitude = parseDouble(r.longitude);
        i.countryCode = clean(r.country_code != null ? r.country_code : r.country);
        i.countryCodeIso3 = clean(r.country_code_iso3);
        i.countryName = clean(r.country_name);
        i.countryCapital = clean(r.country_capital);
        i.countryTld = clean(r.country_tld);
        i.countryArea = parseDouble(r.country_area);
        Double population = parseDouble(r.country_population);
        i.countryPopulation = population == null ? null : population.longValue();
        i.continentCode = clean(r.continent_code);
        i.inEu = r.in_eu == null ? null : Boolean.parseBoolean(r.in_eu);
        i.timezone = clean(r.timezone);
        i.utcOffset = clean(r.utc_offset);
        i.callingCode = clean(r.country_calling_code);
        i.currency = clean(r.currency);
        i.currencyName = clean(r.currency_name);
        i.languages = clean(r.languages);
        i.asn = clean(r.asn);
        i.org = clean(r.org);
        i.source = "ipapi.co";
        return i;
    }

    public static IpInfo fromIpWho(IpWhoResponse r) {
        IpInfo i = new IpInfo();
        i.ip = r.ip;
        i.version = r.type;
        i.city = clean(r.city);
        i.region = clean(r.region);
        i.regionCode = clean(r.region_code);
        i.postal = clean(r.postal);
        i.latitude = r.latitude;
        i.longitude = r.longitude;
        i.countryCode = clean(r.country_code);
        i.countryName = clean(r.country);
        i.countryCapital = clean(r.capital);
        i.continentCode = clean(r.continent_code);
        i.inEu = r.is_eu;
        i.callingCode = r.calling_code == null || r.calling_code.isEmpty() ? null : "+" + r.calling_code;
        if (r.timezone != null) {
            i.timezone = clean(r.timezone.id);
            i.utcOffset = r.timezone.utc == null ? null : r.timezone.utc.replace(":", "");
        }
        if (r.connection != null) {
            i.asn = r.connection.asn == null ? null : "AS" + r.connection.asn;
            i.org = clean(r.connection.isp != null ? r.connection.isp : r.connection.org);
        }
        i.source = "ipwho.is";
        return i;
    }

    /** ipapi.co returns placeholder text instead of data for some fields on the free plan. */
    static String clean(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty() || v.equalsIgnoreCase("Sign up to access") || v.equalsIgnoreCase("null")) return null;
        return v;
    }

    static Double parseDouble(String value) {
        String v = clean(value);
        if (v == null) return null;
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
