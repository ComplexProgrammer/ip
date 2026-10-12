package ip.complexprogrammer.uz.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.os.Build;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkUtil {
    public enum Transport { WIFI, CELLULAR, ETHERNET, VPN, OTHER, NONE }

    /** Any validated connection counts, including Ethernet and VPN. */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = cm(context);
        if (cm == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        @SuppressWarnings("deprecation") NetworkInfo info = cm.getActiveNetworkInfo();
        //noinspection deprecation
        return info != null && info.isConnected();
    }

    public static boolean isVpnActive(Context context) {
        ConnectivityManager cm = cm(context);
        if (cm == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
        }
        for (Network network : cm.getAllNetworks()) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return true;
        }
        return false;
    }

    /** Physical transport of the active connection; VPN only when nothing else is reported. */
    public static Transport getTransport(Context context) {
        ConnectivityManager cm = cm(context);
        if (cm == null) return Transport.NONE;
        NetworkCapabilities caps;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        } else {
            @SuppressWarnings("deprecation") NetworkInfo info = cm.getActiveNetworkInfo();
            //noinspection deprecation
            if (info == null || !info.isConnected()) return Transport.NONE;
            //noinspection deprecation
            switch (info.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    return Transport.WIFI;
                case ConnectivityManager.TYPE_MOBILE:
                    return Transport.CELLULAR;
                case ConnectivityManager.TYPE_ETHERNET:
                    return Transport.ETHERNET;
                default:
                    return Transport.OTHER;
            }
        }
        if (caps == null) return Transport.NONE;
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return Transport.WIFI;
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return Transport.CELLULAR;
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return Transport.ETHERNET;
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return Transport.VPN;
        return Transport.OTHER;
    }

    /** Snapshot of the device's local network configuration. */
    public static class LocalInfo {
        public final List<String> addresses = new ArrayList<>();
        public final List<String> gateways = new ArrayList<>();
        public final List<String> dnsServers = new ArrayList<>();
        public String interfaceName;
        public String privateDnsServer;
    }

    public static LocalInfo getLocalInfo(Context context) {
        LocalInfo result = new LocalInfo();
        ConnectivityManager cm = cm(context);
        if (cm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LinkProperties lp = cm.getLinkProperties(cm.getActiveNetwork());
            if (lp != null) {
                result.interfaceName = lp.getInterfaceName();
                for (LinkAddress la : lp.getLinkAddresses()) {
                    result.addresses.add(la.getAddress().getHostAddress() + "/" + la.getPrefixLength());
                }
                for (RouteInfo route : lp.getRoutes()) {
                    InetAddress gw = route.getGateway();
                    if (gw != null && !gw.isAnyLocalAddress()) {
                        String g = gw.getHostAddress();
                        if (!result.gateways.contains(g)) result.gateways.add(g);
                    }
                }
                for (InetAddress dns : lp.getDnsServers()) result.dnsServers.add(dns.getHostAddress());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && lp.isPrivateDnsActive()) {
                    result.privateDnsServer = lp.getPrivateDnsServerName();
                    if (result.privateDnsServer == null) result.privateDnsServer = "auto";
                }
            }
        }
        if (result.addresses.isEmpty()) {
            try {
                for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                    if (!ni.isUp() || ni.isLoopback()) continue;
                    for (InetAddress a : Collections.list(ni.getInetAddresses())) {
                        if (a instanceof Inet4Address) {
                            result.addresses.add(a.getHostAddress());
                            if (result.interfaceName == null) result.interfaceName = ni.getName();
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    private static ConnectivityManager cm(Context context) {
        return (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}
