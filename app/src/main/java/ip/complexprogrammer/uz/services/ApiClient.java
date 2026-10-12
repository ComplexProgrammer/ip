package ip.complexprogrammer.uz.services;

import java.util.concurrent.TimeUnit;

import ip.complexprogrammer.uz.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static OkHttpClient httpClient;
    private static ApiInterface ipApi;
    private static ApiInterface ipWho;

    public static synchronized OkHttpClient getHttpClient() {
        if (httpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    // Bounds the whole call, including DNS, so a dead network fails fast to the fallback API.
                    .callTimeout(8, TimeUnit.SECONDS)
                    // ipapi.co's Cloudflare front rejects the default "okhttp/x" user agent with 403.
                    .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                            .header("User-Agent", "IPFinder/" + BuildConfig.VERSION_NAME + " (Android)")
                            .build()));
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
                builder.addInterceptor(logging);
            }
            httpClient = builder.build();
        }
        return httpClient;
    }

    private static ApiInterface create(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getHttpClient())
                .build()
                .create(ApiInterface.class);
    }

    public static synchronized ApiInterface getIpApi() {
        if (ipApi == null) ipApi = create("https://ipapi.co/");
        return ipApi;
    }

    public static synchronized ApiInterface getIpWho() {
        if (ipWho == null) ipWho = create("https://ipwho.is/");
        return ipWho;
    }
}
