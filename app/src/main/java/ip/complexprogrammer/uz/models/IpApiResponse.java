package ip.complexprogrammer.uz.models;

/** Raw response of https://ipapi.co/{ip}/json/. Gson coerces numbers and booleans into these strings. */
public class IpApiResponse {
    public String ip;
    public String error;
    public String reason;
    public String reserved;
    public String version;
    public String city;
    public String region;
    public String region_code;
    public String country;
    public String country_code;
    public String country_code_iso3;
    public String country_name;
    public String country_capital;
    public String country_tld;
    public String country_area;
    public String country_population;
    public String continent_code;
    public String in_eu;
    public String postal;
    public String latitude;
    public String longitude;
    public String timezone;
    public String utc_offset;
    public String country_calling_code;
    public String currency;
    public String currency_name;
    public String languages;
    public String asn;
    public String org;
}
