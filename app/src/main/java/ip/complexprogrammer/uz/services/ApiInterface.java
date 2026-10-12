package ip.complexprogrammer.uz.services;

import ip.complexprogrammer.uz.models.IpApiResponse;
import ip.complexprogrammer.uz.models.IpWhoResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiInterface {
    // ipapi.co
    @GET("json/")
    Call<IpApiResponse> getMyIpData();

    @GET("{ip}/json/")
    Call<IpApiResponse> getIpData(@Path(value = "ip", encoded = true) String ip);

    // ipwho.is
    @GET("./")
    Call<IpWhoResponse> whoMyIp();

    @GET("{ip}")
    Call<IpWhoResponse> whoIp(@Path(value = "ip", encoded = true) String ip);
}
