package ip.complexprogrammer.uz.services;

import java.util.List;

import ip.complexprogrammer.uz.models._Response;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("ip")
    Call<String> GetIp();
    @GET("json/")
    Call<_Response> GetMyIpData();
    @GET("/json/")
    Call<_Response> GetCustomerIp();
}
