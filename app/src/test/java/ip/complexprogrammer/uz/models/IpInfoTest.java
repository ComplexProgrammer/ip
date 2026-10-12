package ip.complexprogrammer.uz.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

public class IpInfoTest {
    private final Gson gson = new Gson();

    @Test
    public void mapsIpApiResponse() {
        String json = "{\"ip\":\"195.158.6.194\",\"version\":\"IPv4\",\"city\":\"Tashkent\",\"region\":\"Tashkent\","
                + "\"country_code\":\"UZ\",\"country_name\":\"Uzbekistan\",\"latitude\":41.3171,\"longitude\":69.2494,"
                + "\"in_eu\":false,\"postal\":\"Sign up to access\",\"country_population\":33580650.0,"
                + "\"timezone\":\"Asia/Tashkent\",\"utc_offset\":\"+0500\",\"asn\":\"AS8193\",\"org\":\"Uzbektelekom\"}";
        IpInfo info = IpInfo.fromIpApi(gson.fromJson(json, IpApiResponse.class));
        assertEquals("195.158.6.194", info.ip);
        assertEquals("UZ", info.countryCode);
        assertEquals(41.3171, info.latitude, 1e-9);
        assertEquals(Boolean.FALSE, info.inEu);
        assertNull("placeholder text must be dropped", info.postal);
        assertEquals(Long.valueOf(33580650L), info.countryPopulation);
        assertTrue(info.hasLocation());
        assertEquals("ipapi.co", info.source);
    }

    @Test
    public void mapsIpWhoResponse() {
        String json = "{\"ip\":\"8.8.8.8\",\"success\":true,\"type\":\"IPv4\",\"country\":\"United States\","
                + "\"country_code\":\"US\",\"city\":\"Mountain View\",\"latitude\":37.38,\"longitude\":-122.08,"
                + "\"is_eu\":false,\"calling_code\":\"1\",\"connection\":{\"asn\":15169,\"org\":\"Google LLC\","
                + "\"isp\":\"Google LLC\"},\"timezone\":{\"id\":\"America/Los_Angeles\",\"utc\":\"-07:00\"}}";
        IpInfo info = IpInfo.fromIpWho(gson.fromJson(json, IpWhoResponse.class));
        assertEquals("AS15169", info.asn);
        assertEquals("+1", info.callingCode);
        assertEquals("-0700", info.utcOffset);
        assertEquals("Google LLC", info.org);
        assertEquals("ipwho.is", info.source);
    }

    @Test
    public void missingCoordinatesMeansNoLocation() {
        IpInfo info = IpInfo.fromIpApi(gson.fromJson("{\"ip\":\"1.1.1.1\",\"latitude\":\"Sign up to access\"}",
                IpApiResponse.class));
        assertFalse(info.hasLocation());
    }
}
