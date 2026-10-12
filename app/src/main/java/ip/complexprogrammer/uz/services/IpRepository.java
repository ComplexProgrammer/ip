package ip.complexprogrammer.uz.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ip.complexprogrammer.uz.models.IpApiResponse;
import ip.complexprogrammer.uz.models.IpInfo;
import ip.complexprogrammer.uz.models.IpWhoResponse;
import retrofit2.Response;

/**
 * Looks up an IP address, a domain name, or (for an empty query) the device's own public IP.
 * Tries the cache, then ipapi.co, then ipwho.is.
 */
public class IpRepository {
    public enum ErrorType { NO_INTERNET, RESERVED, UNKNOWN_HOST, RATE_LIMITED, SERVER }

    public interface Callback {
        void onSuccess(IpInfo info);

        void onError(ErrorType type, String detail);
    }

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Context context;
    private final IpCache cache;

    public IpRepository(Context context) {
        this.context = context.getApplicationContext();
        this.cache = new IpCache(context);
    }

    public Future<?> lookup(String rawQuery, Callback callback) {
        return EXECUTOR.submit(() -> {
            try {
                IpInfo info = lookupBlocking(rawQuery);
                if (!Thread.currentThread().isInterrupted()) main.post(() -> callback.onSuccess(info));
            } catch (LookupException e) {
                if (!Thread.currentThread().isInterrupted()) main.post(() -> callback.onError(e.type, e.getMessage()));
            }
        });
    }

    private IpInfo lookupBlocking(String rawQuery) throws LookupException {
        if (!NetworkUtil.isOnline(context)) throw new LookupException(ErrorType.NO_INTERNET, null);
        String query = IpUtils.normalize(rawQuery);
        IpUtils.InputType type = IpUtils.classify(query);
        String ip;
        String host = null;
        switch (type) {
            case EMPTY:
                ip = null;
                break;
            case DOMAIN:
                host = query;
                ip = resolve(query);
                break;
            default:
                ip = query;
        }
        if (ip != null && IpUtils.isPrivateIpv4(ip)) throw new LookupException(ErrorType.RESERVED, ip);

        if (ip != null) {
            IpInfo cached = cache.get(ip);
            if (cached != null) {
                cached.query = host;
                return cached;
            }
        }

        LookupException firstError;
        try {
            IpInfo info = fromIpApi(ip);
            return finish(info, host);
        } catch (LookupException e) {
            if (e.type == ErrorType.RESERVED || Thread.currentThread().isInterrupted()) throw e;
            firstError = e;
        }
        try {
            IpInfo info = fromIpWho(ip);
            return finish(info, host);
        } catch (LookupException e) {
            if (e.type == ErrorType.RESERVED) throw e;
            throw firstError.type == ErrorType.RATE_LIMITED ? firstError : e;
        }
    }

    private IpInfo finish(IpInfo info, String host) {
        info.query = host;
        info.fetchedAt = System.currentTimeMillis();
        cache.put(info);
        return info;
    }

    private static String resolve(String host) throws LookupException {
        try {
            InetAddress[] all = InetAddress.getAllByName(host);
            for (InetAddress a : all) if (a instanceof Inet4Address) return a.getHostAddress();
            return all[0].getHostAddress();
        } catch (UnknownHostException e) {
            throw new LookupException(ErrorType.UNKNOWN_HOST, host);
        }
    }

    private static IpInfo fromIpApi(String ip) throws LookupException {
        ApiInterface api = ApiClient.getIpApi();
        try {
            Response<IpApiResponse> response = (ip == null ? api.getMyIpData() : api.getIpData(ip)).execute();
            IpApiResponse body = response.body();
            if (response.code() == 429) throw new LookupException(ErrorType.RATE_LIMITED, "ipapi.co");
            if (!response.isSuccessful() || body == null) {
                throw new LookupException(ErrorType.SERVER, "ipapi.co HTTP " + response.code());
            }
            if ("true".equalsIgnoreCase(body.error) || body.reason != null) {
                if ("true".equalsIgnoreCase(body.reserved)) throw new LookupException(ErrorType.RESERVED, body.ip);
                if (body.reason != null && body.reason.toLowerCase().contains("rate")) {
                    throw new LookupException(ErrorType.RATE_LIMITED, body.reason);
                }
                throw new LookupException(ErrorType.SERVER, body.reason);
            }
            return IpInfo.fromIpApi(body);
        } catch (IOException | RuntimeException e) {
            throw new LookupException(ErrorType.SERVER, e.getMessage());
        }
    }

    private static IpInfo fromIpWho(String ip) throws LookupException {
        ApiInterface api = ApiClient.getIpWho();
        try {
            Response<IpWhoResponse> response = (ip == null ? api.whoMyIp() : api.whoIp(ip)).execute();
            IpWhoResponse body = response.body();
            if (response.code() == 429) throw new LookupException(ErrorType.RATE_LIMITED, "ipwho.is");
            if (!response.isSuccessful() || body == null) {
                throw new LookupException(ErrorType.SERVER, "ipwho.is HTTP " + response.code());
            }
            if (!body.success) {
                String message = body.message == null ? "" : body.message.toLowerCase();
                if (message.contains("reserved") || message.contains("private")) {
                    throw new LookupException(ErrorType.RESERVED, ip);
                }
                if (message.contains("limit")) throw new LookupException(ErrorType.RATE_LIMITED, body.message);
                throw new LookupException(ErrorType.SERVER, body.message);
            }
            return IpInfo.fromIpWho(body);
        } catch (IOException | RuntimeException e) {
            throw new LookupException(ErrorType.SERVER, e.getMessage());
        }
    }

    private static class LookupException extends Exception {
        final ErrorType type;

        LookupException(ErrorType type, String detail) {
            super(detail);
            this.type = type;
        }
    }
}
