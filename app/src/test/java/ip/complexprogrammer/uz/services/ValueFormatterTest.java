package ip.complexprogrammer.uz.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Date;
import java.util.Locale;

public class ValueFormatterTest {
    @Test
    public void languages() {
        assertEquals("Uzbek, Russian, Tajik", ValueFormatter.languages("uz,ru,tg", Locale.ENGLISH));
        assertEquals("English, Spanish", ValueFormatter.languages("en-US,es-US,en", Locale.ENGLISH));
        assertNull(ValueFormatter.languages(null, Locale.ENGLISH));
        assertNull(ValueFormatter.languages(" ", Locale.ENGLISH));
    }

    @Test
    public void currency() {
        assertTrue(ValueFormatter.currency("USD", null, Locale.ENGLISH).startsWith("USD · US Dollar"));
        assertEquals("XYZ · Fallback", ValueFormatter.currency("XYZ", "Fallback", Locale.ENGLISH));
        assertEquals("XYZ", ValueFormatter.currency("XYZ", null, Locale.ENGLISH));
        assertNull(ValueFormatter.currency(null, "x", Locale.ENGLISH));
    }

    @Test
    public void utcOffset() {
        assertEquals("UTC+05:00", ValueFormatter.utcOffset("+0500"));
        assertEquals("UTC-03:30", ValueFormatter.utcOffset("-0330"));
        assertEquals("UTC+05:00", ValueFormatter.utcOffset("+05:00"));
        assertNull(ValueFormatter.utcOffset(null));
    }

    @Test
    public void currentTime() {
        // 2024-01-01T00:00:00Z is 05:00 in Tashkent.
        Date date = new Date(1704067200000L);
        assertTrue(ValueFormatter.currentTime("Asia/Tashkent", date, Locale.ENGLISH).startsWith("05:00"));
        assertNull(ValueFormatter.currentTime("Not/AZone", date, Locale.ENGLISH));
        assertNull(ValueFormatter.currentTime(null, date, Locale.ENGLISH));
    }

    @Test
    public void numbers() {
        assertEquals("36,000,000", ValueFormatter.number(36000000L, Locale.ENGLISH));
        assertEquals("447,400 km²", ValueFormatter.area(447400.0, Locale.ENGLISH));
        assertEquals("41.3171, 69.2494", ValueFormatter.coordinates(41.3171, 69.2494));
        assertNull(ValueFormatter.coordinates(null, 1.0));
    }
}
