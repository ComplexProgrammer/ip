package ip.complexprogrammer.uz.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Blocking network diagnostics. Call from a background thread; each line is streamed to the listener. */
public final class NetTools {
    public interface Output {
        void line(String text);

        boolean isCancelled();
    }

    public static final int[] COMMON_PORTS = {
            21, 22, 23, 25, 53, 80, 110, 143, 443, 465, 587,
            993, 995, 1194, 1723, 3306, 3389, 5432, 5900, 8080, 8443
    };
    private static final String[] PORT_NAMES = {
            "FTP", "SSH", "Telnet", "SMTP", "DNS", "HTTP", "POP3", "IMAP", "HTTPS", "SMTPS", "Submission",
            "IMAPS", "POP3S", "OpenVPN", "PPTP", "MySQL", "RDP", "PostgreSQL", "VNC", "HTTP-alt", "HTTPS-alt"
    };

    private NetTools() {
    }

    public static void ping(String host, Output out) throws IOException {
        Process process = new ProcessBuilder("ping", "-c", "4", "-W", "3", host)
                .redirectErrorStream(true)
                .start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (out.isCancelled()) break;
                out.line(line);
            }
        } finally {
            process.destroy();
        }
    }

    public static void dns(String host, Output out) throws IOException {
        long start = System.nanoTime();
        InetAddress[] addresses = InetAddress.getAllByName(host);
        long ms = (System.nanoTime() - start) / 1_000_000;
        for (InetAddress a : addresses) {
            out.line((a.getAddress().length == 4 ? "A     " : "AAAA  ") + a.getHostAddress());
        }
        if (addresses.length == 1 && IpUtils.classify(host) != IpUtils.InputType.DOMAIN) {
            String reverse = addresses[0].getCanonicalHostName();
            if (!reverse.equals(addresses[0].getHostAddress())) out.line("PTR   " + reverse);
        }
        out.line("");
        out.line(String.format(Locale.ROOT, "%d record(s), %d ms", addresses.length, ms));
    }

    /** Asks IANA which registry is responsible, then queries that registry. */
    public static void whois(String query, Output out) throws IOException {
        String iana = whoisQuery("whois.iana.org", query);
        String referral = null;
        Matcher m = Pattern.compile("(?im)^\\s*(?:refer|whois):\\s*(\\S+)").matcher(iana);
        if (m.find()) referral = m.group(1);
        if (out.isCancelled()) return;
        String result = referral != null ? whoisQuery(referral, query) : iana;
        if (referral != null) {
            out.line("% " + referral);
            out.line("");
        }
        for (String line : result.split("\r?\n")) {
            if (out.isCancelled()) return;
            out.line(line);
        }
    }

    private static String whoisQuery(String server, String query) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(server, 43), 8000);
            socket.setSoTimeout(10000);
            OutputStream os = socket.getOutputStream();
            os.write((query + "\r\n").getBytes(StandardCharsets.UTF_8));
            os.flush();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        }
    }

    /** Checks the common ports in parallel; prints only open ones plus a summary. */
    public static void ports(String host, Output out, String openLabel, String closedSummary) throws Exception {
        InetAddress address = InetAddress.getByName(host);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int port : COMMON_PORTS) {
                results.add(pool.submit(() -> {
                    try (Socket s = new Socket()) {
                        s.connect(new InetSocketAddress(address, port), 1500);
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                }));
            }
            int open = 0;
            for (int i = 0; i < results.size(); i++) {
                if (out.isCancelled()) return;
                if (results.get(i).get()) {
                    open++;
                    out.line(String.format(Locale.ROOT, "%-6d %-11s %s", COMMON_PORTS[i], PORT_NAMES[i], openLabel));
                }
            }
            out.line("");
            out.line(String.format(Locale.ROOT, closedSummary, open, COMMON_PORTS.length));
        } finally {
            pool.shutdownNow();
        }
    }
}
