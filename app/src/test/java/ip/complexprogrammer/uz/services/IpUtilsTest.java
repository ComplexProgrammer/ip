package ip.complexprogrammer.uz.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ip.complexprogrammer.uz.services.IpUtils.InputType;

public class IpUtilsTest {
    @Test
    public void classifiesIpv4() {
        assertEquals(InputType.IPV4, IpUtils.classify("8.8.8.8"));
        assertEquals(InputType.IPV4, IpUtils.classify(" 195.158.6.194 "));
        assertEquals(InputType.IPV4, IpUtils.classify("0.0.0.0"));
        assertEquals(InputType.IPV4, IpUtils.classify("255.255.255.255"));
    }

    @Test
    public void rejectsBadIpv4() {
        assertEquals(InputType.INVALID, IpUtils.classify("256.1.1.1"));
        assertEquals(InputType.INVALID, IpUtils.classify("1.2.3"));
        assertEquals(InputType.INVALID, IpUtils.classify("01.2.3.4"));
        assertEquals(InputType.INVALID, IpUtils.classify("1.2.3.4.5"));
    }

    @Test
    public void classifiesIpv6() {
        assertEquals(InputType.IPV6, IpUtils.classify("2001:4860:4860::8888"));
        assertEquals(InputType.IPV6, IpUtils.classify("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertEquals(InputType.IPV6, IpUtils.classify("::1"));
        assertEquals(InputType.IPV6, IpUtils.classify("::"));
        assertEquals(InputType.IPV6, IpUtils.classify("::ffff:192.168.1.1"));
        assertEquals(InputType.IPV6, IpUtils.classify("[2001:db8::1]:443"));
        assertEquals(InputType.IPV6, IpUtils.classify("fe80::1%wlan0"));
    }

    @Test
    public void rejectsBadIpv6() {
        assertFalse(IpUtils.isIpv6("2001:db8::1::2"));
        assertFalse(IpUtils.isIpv6("12345::1"));
        assertFalse(IpUtils.isIpv6("1:2:3:4:5:6:7:8:9"));
        assertFalse(IpUtils.isIpv6("1:2:3:4:5:6:7"));
        assertFalse(IpUtils.isIpv6("gggg::1"));
    }

    @Test
    public void classifiesDomains() {
        assertEquals(InputType.DOMAIN, IpUtils.classify("google.com"));
        assertEquals(InputType.DOMAIN, IpUtils.classify("https://www.Kun.uz/news/123"));
        assertEquals(InputType.DOMAIN, IpUtils.classify("example.co.uk:8080"));
        assertEquals(InputType.INVALID, IpUtils.classify("localhost"));
        assertEquals(InputType.INVALID, IpUtils.classify("bad_domain.com"));
        assertEquals(InputType.EMPTY, IpUtils.classify("   "));
    }

    @Test
    public void normalizesPastedInput() {
        assertEquals("kun.uz", IpUtils.normalize("https://Kun.uz/path?x=1"));
        assertEquals("1.2.3.4", IpUtils.normalize("1.2.3.4:80"));
        assertEquals("2001:db8::1", IpUtils.normalize("[2001:db8::1]:443"));
        assertEquals("example.com", IpUtils.normalize("example.com."));
    }

    @Test
    public void detectsPrivateRanges() {
        assertTrue(IpUtils.isPrivateIpv4("10.0.0.1"));
        assertTrue(IpUtils.isPrivateIpv4("192.168.1.1"));
        assertTrue(IpUtils.isPrivateIpv4("172.16.0.1"));
        assertTrue(IpUtils.isPrivateIpv4("127.0.0.1"));
        assertTrue(IpUtils.isPrivateIpv4("100.64.0.1"));
        assertFalse(IpUtils.isPrivateIpv4("172.32.0.1"));
        assertFalse(IpUtils.isPrivateIpv4("8.8.8.8"));
    }

    @Test
    public void flagEmoji() {
        assertEquals("🇺🇿", IpUtils.flagEmoji("UZ"));
        assertEquals("🇺🇿", IpUtils.flagEmoji("uz"));
        assertEquals("", IpUtils.flagEmoji(null));
        assertEquals("", IpUtils.flagEmoji("U1"));
    }
}
