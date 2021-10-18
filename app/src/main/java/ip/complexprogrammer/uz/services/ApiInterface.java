package ip.complexprogrammer.uz.services;

import java.util.List;

import ip.complexprogrammer.uz.models._Response;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiInterface {
    @GET("ip")
    Call<String> GetIp();
    @GET("json/")
    Call<_Response> GetMyIpData();
    @GET("{ip}/json/")
    Call<_Response> GetCustomerIp(@Path(value = "ip", encoded = true) String ip);
}
