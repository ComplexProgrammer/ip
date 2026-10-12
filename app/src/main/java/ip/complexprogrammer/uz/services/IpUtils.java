package ip.complexprogrammer.uz.services;

import java.util.regex.Pattern;

/** Classifies what the user typed in the lookup field. Pure Java so it can be unit tested. */
public final class IpUtils {
    public enum InputType { EMPTY, IPV4, IPV6, DOMAIN, INVALID }

    private static final Pattern IPV4 = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$");
    private static final Pattern HEX_GROUP = Pattern.compile("^[0-9a-fA-F]{1,4}$");
    private static final Pattern DOMAIN = Pattern.compile(
            "^(?=.{1,253}$)([a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,63}$");

    private IpUtils() {
    }

    /** Trims whitespace and strips a URL scheme, path or port so pasted links work. */
    public static String normalize(String input) {
        if (input == null) return "";
        String s = input.trim();
        int scheme = s.indexOf("://");
        if (scheme >= 0) s = s.substring(scheme + 3);
        int slash = s.indexOf('/');
        if (slash >= 0) s = s.substring(0, slash);
        if (s.startsWith("[") && s.contains("]")) {
            s = s.substring(1, s.indexOf(']'));
        } else if (s.indexOf(':') == s.lastIndexOf(':') && s.contains(":")) {
            // host:port (a single colon can never be IPv6)
            s = s.substring(0, s.indexOf(':'));
        }
        if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
        return s.toLowerCase(java.util.Locale.ROOT);
    }

    public static InputType classify(String input) {
        String s = normalize(input);
        if (s.isEmpty()) return InputType.EMPTY;
        if (isIpv4(s)) return InputType.IPV4;
        if (isIpv6(s)) return InputType.IPV6;
        if (DOMAIN.matcher(s).matches()) return InputType.DOMAIN;
        return InputType.INVALID;
    }

    public static boolean isIpv4(String s) {
        return s != null && IPV4.matcher(s).matches();
    }

    public static boolean isIpv6(String s) {
        if (s == null || !s.contains(":")) return false;
        String address = s;
        int zone = address.indexOf('%');
        if (zone >= 0) address = address.substring(0, zone);
        int groupsNeeded = 8;
        // An embedded IPv4 tail (e.g. ::ffff:1.2.3.4) counts as two groups.
        int lastColon = address.lastIndexOf(':');
        String tail = address.substring(lastColon + 1);
        if (tail.contains(".")) {
            if (!isIpv4(tail)) return false;
            address = address.substring(0, lastColon + 1) + "0:0";
        }
        int doubleColon = address.indexOf("::");
        if (doubleColon >= 0 && address.indexOf("::", doubleColon + 1) >= 0) return false;
        if (doubleColon < 0) {
            String[] groups = address.split(":", -1);
            if (groups.length != groupsNeeded) return false;
            for (String g : groups) if (!HEX_GROUP.matcher(g).matches()) return false;
            return true;
        }
        String head = address.substring(0, doubleColon);
        String rest = address.substring(doubleColon + 2);
        int count = 0;
        for (String part : new String[]{head, rest}) {
            if (part.isEmpty()) continue;
            for (String g : part.split(":", -1)) {
                if (!HEX_GROUP.matcher(g).matches()) return false;
                count++;
            }
        }
        return count < groupsNeeded;
    }

    /** Private, loopback, link-local and other ranges that geolocation services cannot locate. */
    public static boolean isPrivateIpv4(String ip) {
        if (!isIpv4(ip)) return false;
        String[] p = ip.split("\\.");
        int a = Integer.parseInt(p[0]);
        int b = Integer.parseInt(p[1]);
        return a == 10 || a == 127 || a == 0
                || (a == 172 && b >= 16 && b <= 31)
                || (a == 192 && b == 168)
                || (a == 169 && b == 254)
                || (a == 100 && b >= 64 && b <= 127)
                || a >= 224;
    }

    /** Converts an ISO 3166 alpha-2 code into its emoji flag, e.g. "UZ" -> 🇺🇿. */
    public static String flagEmoji(String countryCode) {
        if (countryCode == null || countryCode.length() != 2) return "";
        String cc = countryCode.toUpperCase(java.util.Locale.ROOT);
        char a = cc.charAt(0);
        char b = cc.charAt(1);
        if (a < 'A' || a > 'Z' || b < 'A' || b > 'Z') return "";
        int first = 0x1F1E6 + (a - 'A');
        int second = 0x1F1E6 + (b - 'A');
        return new String(Character.toChars(first)) + new String(Character.toChars(second));
    }
}
