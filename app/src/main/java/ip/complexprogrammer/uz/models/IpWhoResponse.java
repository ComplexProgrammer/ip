package ip.complexprogrammer.uz.models;

/** Raw response of https://ipwho.is/{ip}, used as a fallback when ipapi.co fails or is rate limited. */
public class IpWhoResponse {
    public String ip;
    public boolean success;
    public String message;
    public String type;
    public String continent_code;
    public String country;
    public String country_code;
    public String region;
    public String region_code;
    public String city;
    public Double latitude;
    public Double longitude;
    public Boolean is_eu;
    public String postal;
    public String calling_code;
    public String capital;
    public Connection connection;
    public Timezone timezone;

    public static class Connection {
        public String asn;
        public String org;
        public String isp;
    }

    public static class Timezone {
        public String id;
        public String utc;
    }
}
