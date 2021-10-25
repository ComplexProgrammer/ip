package ip.complexprogrammer.uz;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ip.complexprogrammer.uz.models._Response;
import ip.complexprogrammer.uz.services.ApiClient;
import ip.complexprogrammer.uz.services.ChangeLanguage;
import ip.complexprogrammer.uz.services.NetworkUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    String ip_text,error_text,reason_text,reserved_text,version_text,city_text,region_text,region_code_text,country_text,
            country_code_text,country_code_iso3_text,country_name_text,country_capital_text,country_tld_text,country_area_text,
            country_population_text,continent_code_text,in_eu_text,postal_text,latlong_text,
            timezone_text,utc_offset_text,country_calling_code_text,currency_text,currency_name_text,languages_text,
            asn_text,org_text;
    String latitude_text,longitude_text;
    TableRow reason_tab,city_tab,map_tab,region_tab,country_tab,in_eu_tab,position_tab,longitude_tab,timezone_tab,country_calling_code_tab,currency_tab,languages_tab,asn_tab,org_tab;
    TextView reason,ip;
    EditText _ip;
    Button ip_lookup,my_ip,map;
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String TAG = "MainActivity";
    private InterstitialAd interstitialAd;
    private RewardedAd mRewardedAd;
    private _Response ResponseList=new _Response();
    private static final String IPV4_PATTERN =
            "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
    private static final Pattern pattern = Pattern.compile(IPV4_PATTERN);

    public static boolean isValid(final String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    public void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                this,
                AD_UNIT_ID,
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        interstitialAd.show(MainActivity.this);
                        //MainActivity.this.interstitialAd = interstitialAd;
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        MainActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad was dismissed.");
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        MainActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad failed to show.");
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d("TAG", "The ad was shown.");
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        interstitialAd = null;

                        String error =
                                String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
//                        Toast.makeText(
//                                MainActivity.this, "onAdFailedToLoad() with error: " + error, Toast.LENGTH_SHORT)
//                                .show();
                    }
                });
    }
    public void loadRewardedad(){

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "Ad was loaded.");
                        if (mRewardedAd != null) {
                            Activity activityContext = MainActivity.this;
                            mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                                @Override
                                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                    // Handle the reward.
                                    Log.d(TAG, "The user earned the reward.");
                                    int rewardAmount = rewardItem.getAmount();
                                    String rewardType = rewardItem.getType();
                                }
                            });
                        } else {
                            Log.d(TAG, "The rewarded ad wasn't ready yet.");
                        }
                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad was shown.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.d(TAG, "Ad failed to show.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad was dismissed.");
                                mRewardedAd = null;
                            }
                        });
                    }
                });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ChangeLanguage.setLocal(this,ChangeLanguage.getLangCode(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));
        //StartAppSDK.init(this, "209602699", false);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//        loadInterstitialAd();
        loadRewardedad();

        _ip=findViewById(R.id._ip);
        ip_lookup=findViewById(R.id.ip_lookup);
        my_ip=findViewById(R.id.my_ip);
        map=findViewById(R.id.map);
        ip=findViewById(R.id.ip);
        reason_tab=findViewById(R.id.reason_tab);
        reason=findViewById(R.id.reason);
        city_tab=findViewById(R.id.city_tab);
        map_tab=findViewById(R.id.map_tab);
        region_tab=findViewById(R.id.region_tab);
        country_tab=findViewById(R.id.country_tab);
        in_eu_tab=findViewById(R.id.in_eu_tab);
        position_tab=findViewById(R.id.position_tab);
        longitude_tab=findViewById(R.id.longitude_tab);
        timezone_tab=findViewById(R.id.timezone_tab);
        country_calling_code_tab=findViewById(R.id.country_calling_code_tab);
        currency_tab=findViewById(R.id.currency_tab);
        languages_tab=findViewById(R.id.languages_tab);
        asn_tab=findViewById(R.id.asn_tab);
        org_tab=findViewById(R.id.org_tab);
        boolean status = NetworkUtil.getConnectivityStatusString(this);
        if(!status){
            startActivity(new Intent(this, NoInternetActivity.class));
        }
        else {
            LoadData("");
            ip_lookup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String ip2=_ip.getText().toString();
                    if(isValid(ip2)){
                        LoadData(ip2);
                    }
                    else {
                        reason.setText(getString(R.string.invalid_ip));
                        ip.setText(ip2);
                        reason_tab.setVisibility(View.VISIBLE);
                        city_tab.setVisibility(View.GONE);
                        region_tab.setVisibility(View.GONE);
                        country_tab.setVisibility(View.GONE);
                        in_eu_tab.setVisibility(View.GONE);
                        position_tab.setVisibility(View.GONE);
                        map_tab.setVisibility(View.GONE);
                        longitude_tab.setVisibility(View.GONE);
                        timezone_tab.setVisibility(View.GONE);
                        country_calling_code_tab.setVisibility(View.GONE);
                        currency_tab.setVisibility(View.GONE);
                        languages_tab.setVisibility(View.GONE);
                        asn_tab.setVisibility(View.GONE);
                        org_tab.setVisibility(View.GONE);
                    }
                    Toast.makeText(MainActivity.this,ip2,Toast.LENGTH_LONG).show();
                }
            });
            my_ip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LoadData("");
                }
            });
            map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString("latitude_text", latitude_text);
                    bundle.putString("longitude_text", longitude_text);
                    startActivity(new Intent(MainActivity.this, MapsActivity.class).putExtras(bundle));
                }
            });
        }
    }
    public void LoadData(String ip_address){
        Call<_Response> _response=null;
        if(ip_address.equals("")){
            _response = ApiClient.getInterface().GetMyIpData();
        }
        else {
            _response = ApiClient.getInterface().GetCustomerIp(ip_address);
        }
        _response.enqueue(new Callback<_Response>() {
            @Override
            public void onResponse(Call<_Response> call, Response<_Response> response) {
                if(response.isSuccessful()){
//                    Log.e("code",response.code()+"");
                    Log.e("response",response+"");


                    ResponseList =response.body();
                    ImageView country_flag = findViewById(R.id.country_flag);
                    TextView city=findViewById(R.id.city) ;
                    TextView region=findViewById(R.id.region);
                    TextView region_code=findViewById(R.id.region_code);
                    TextView country=findViewById(R.id.country);
                    TextView country_code=findViewById(R.id.country_code);
                    TextView country_code_iso3=findViewById(R.id.country_code_iso3);
                    TextView country_name=findViewById(R.id.country_name);
                    TextView country_capital=findViewById(R.id.country_capital);
                    TextView country_tld=findViewById(R.id.country_tld);
                    TextView country_area=findViewById(R.id.country_area);
                    TextView country_population=findViewById(R.id.country_population);
                    TextView continent_code=findViewById(R.id.continent_code);
                    TextView in_eu=findViewById(R.id.in_eu);
                    TextView postal=findViewById(R.id.postal);
                    TextView latitude=findViewById(R.id.latitude);
                    TextView longitude=findViewById(R.id.longitude);
                    TextView latlong=findViewById(R.id.latlong);
                    TextView timezone=findViewById(R.id.timezone);
                    TextView utc_offset=findViewById(R.id.utc_offset);
                    TextView country_calling_code=findViewById(R.id.country_calling_code);
                    TextView currency=findViewById(R.id.currency);
                    TextView currency_name=findViewById(R.id.currency_name);
                    TextView languages=findViewById(R.id.languages);
                    TextView asn=findViewById(R.id.asn);
                    TextView org=findViewById(R.id.org);

                    ip.setText(ResponseList.getIp());
                    _ip.setText(ResponseList.getIp()) ;
                    reason.setText(ResponseList.getReason());
                    Log.e("getReason",ResponseList.getReason()+"");
                    Log.e("reasongetText()",reason.getText()+"");

                    if(ResponseList.getReason() != null){
                        reason_tab.setVisibility(View.VISIBLE);
                        city_tab.setVisibility(View.GONE);
                        region_tab.setVisibility(View.GONE);
                        country_tab.setVisibility(View.GONE);
                        in_eu_tab.setVisibility(View.GONE);
                        position_tab.setVisibility(View.GONE);
                        map_tab.setVisibility(View.GONE);
                        longitude_tab.setVisibility(View.GONE);
                        timezone_tab.setVisibility(View.GONE);
                        country_calling_code_tab.setVisibility(View.GONE);
                        currency_tab.setVisibility(View.GONE);
                        languages_tab.setVisibility(View.GONE);
                        asn_tab.setVisibility(View.GONE);
                        org_tab.setVisibility(View.GONE);
                    }
                    else {
                        reason_tab.setVisibility(View.GONE);


                        city_text=ResponseList.getCity();
                        region_text=ResponseList.getRegion();
                        region_code_text=ResponseList.getRegion_code();
                        country_text=ResponseList.getCountry();
                        country_code_text=ResponseList.getCountry_code();
                        country_code_iso3_text=ResponseList.getCountry_code_iso3();
                        country_name_text=ResponseList.getCountry_name();
                        country_capital_text=ResponseList.getCountry_capital();
                        country_tld_text=ResponseList.getCountry_tld();
                        country_area_text=ResponseList.getCountry_area();
                        country_population_text=ResponseList.getCountry_population();
                        continent_code_text=ResponseList.getContinent_code();
                        in_eu_text=ResponseList.getIn_eu();
                        postal_text=ResponseList.getPostal();
                        latitude_text=ResponseList.getLatitude();
                        longitude_text=ResponseList.getLongitude();
                        latlong_text=ResponseList.getLatlong();
                        timezone_text=ResponseList.getTimezone();
                        utc_offset_text=ResponseList.getUtc_offset();
                        country_calling_code_text=ResponseList.getCountry_calling_code();
                        currency_text=ResponseList.getCurrency();
                        currency_name_text=ResponseList.getCurrency_name();
                        languages_text=ResponseList.getLanguages();
                        asn_text=ResponseList.getAsn();
                        org_text=ResponseList.getOrg();
                        if(city_text==null){
                            city_tab.setVisibility(View.GONE);
                        }
                        else {
                            city.setText(city_text);
                            city_tab.setVisibility(View.VISIBLE);
                        }
                        if(region_text==null){
                            region_tab.setVisibility(View.GONE);
                        }
                        else {
                            region.setText(region_text);
                            region_tab.setVisibility(View.VISIBLE);
                        }
                        if(region_code_text==null){

                        }
                        else {
                            region_code.setText(region_code_text);


                        }
                        if(country_text==null){
                            country_tab.setVisibility(View.GONE);
                        }
                        else {
                            country.setText(country_text);
                            country_tab.setVisibility(View.VISIBLE);
                        }
                        if(country_code_text==null){

                        }
                        else {
                            country_code.setText(country_code_text);
                        }
                        if(country_code_iso3_text==null){

                        }
                        else {
                            country_code_iso3.setText(country_code_iso3_text);
                        }
                        if(country_name_text==null){

                        }
                        else {
                            country_name.setText(country_name_text);
                        }
                        if(country_capital_text==null){

                        }
                        else {
                            country_capital.setText(country_capital_text);
                        }
                        if(country_tld_text==null){

                        }
                        else {
                            country_tld.setText(country_tld_text);
                        }
                        if(country_area_text==null){

                        }
                        else {
                            country_area.setText(country_area_text);
                        }
                        if(country_population_text==null){

                        }
                        else {
                            country_population.setText(country_population_text);
                        }
                        if(continent_code_text==null){

                        }
                        else {
                            continent_code.setText(continent_code_text);
                        }
                        if(in_eu_text==null){
                            in_eu_tab.setVisibility(View.GONE);
                        }
                        else {
                            in_eu_tab.setVisibility(View.VISIBLE);
                            in_eu.setText(in_eu_text);
                        }
                        if(postal_text==null||postal_text.equals("Sign up to access")){

                        }
                        else {
                            postal.setText(postal_text);
                        }
                        if(latitude_text==null||latitude_text.equals("Sign up to access")){
                            position_tab.setVisibility(View.GONE);
                            map_tab.setVisibility(View.GONE);

                        }
                        else {
                            position_tab.setVisibility(View.VISIBLE);
                            map_tab.setVisibility(View.VISIBLE);
                            latitude.setText(latitude_text+" / "+longitude_text);
                        }
                        if(longitude_text==null||longitude_text.equals("Sign up to access")){
                            longitude_tab.setVisibility(View.GONE);
                        }
                        else {
                            longitude_tab.setVisibility(View.GONE);
                            longitude.setText(longitude_text+"");
                        }
                        if(latlong_text==null){

                        }
                        else {
                            latlong.setText(latlong_text);
                        }
                        if(timezone_text==null){
                            timezone_tab.setVisibility(View.GONE);
                        }
                        else {
                            timezone_tab.setVisibility(View.VISIBLE);
                            timezone.setText(timezone_text+" ("+utc_offset_text+")");
                        }
                        if(utc_offset_text==null){

                        }
                        else {
                            utc_offset.setText(utc_offset_text);
                        }
                        if(country_calling_code_text==null){
                            country_calling_code_tab.setVisibility(View.GONE);
                        }
                        else {
                            country_calling_code_tab.setVisibility(View.VISIBLE);
                            country_calling_code.setText(country_calling_code_text);
                        }
                        if(currency_text==null){
                            currency_tab.setVisibility(View.GONE);
                        }
                        else {
                            currency_tab.setVisibility(View.VISIBLE);
                            currency.setText(currency_text);
                        }
                        if(currency_name_text==null){

                        }
                        else {
                            currency_name.setText(currency_name_text);
                        }
                        if(languages_text==null){
                            languages_tab.setVisibility(View.GONE);
                        }
                        else {
                            languages_tab.setVisibility(View.VISIBLE);
                            languages.setText(languages_text);
                        }
                        if(asn_text==null){
                            asn_tab.setVisibility(View.GONE);
                        }
                        else {
                            asn_tab.setVisibility(View.VISIBLE);
                            asn.setText(asn_text);
                        }
                        if(org_text==null){
                            org_tab.setVisibility(View.GONE);
                        }
                        else {
                            org_tab.setVisibility(View.VISIBLE);
                            org.setText(org_text);
                        }
                        String country_flag_url = "https://ipapi.co/static/images/flags/"+ResponseList.getCountry_code().toLowerCase(Locale.ROOT)+".png";
                        Glide.with(MainActivity.this).load(country_flag_url).into(country_flag);
                        //Utils.fetchSvg(MainActivity.this, country_flag_url, country_flag);
                    }
                }
                else if (response.errorBody() != null) {
                    // Get response errorBody
                    String errorBody = response.errorBody().toString();
                    Log.e("errorBody",errorBody);
                    Toast.makeText(getBaseContext(),errorBody,Toast.LENGTH_LONG).show();

                }
                else {
                    String message="Xatolik yuz berdi. keyinroq yana urinib ko'rig";
                    Toast.makeText(getBaseContext(),message,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<_Response> call, Throwable t) {
                startActivity(new Intent(MainActivity.this, ServerErrorActivity.class));

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        String language=ChangeLanguage.getLangCode(this);

        MenuItem uzbek = menu.findItem(R.id.uzbek);
        MenuItem english = menu.findItem(R.id.english);
        MenuItem russian = menu.findItem(R.id.russian);
        switch (language){
            case "uz": uzbek.setVisible(false); break;
            case "en": english.setVisible(false); break;
            case "ru": russian.setVisible(false); break;

        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.telegram:
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/complexprogrammer"));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "No application can handle this request."
                            + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                return true;
            case R.id.about:
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                final View aboutView = factory.inflate(R.layout.about, null);
                if(NetworkUtil.getConnectivityStatusString(getBaseContext())){
//                    AdView mAdView = aboutView.findViewById(R.id.adView);
//                    AdRequest adRequest = new AdRequest.Builder()
//                            .build();
//                    mAdView.loadAd(adRequest);
                }
                TextView versionLabel = aboutView.findViewById(R.id.version_label);
                String versionName = getAppVersionName(getApplicationContext());
                versionLabel.setText(MainActivity.this.getString(R.string.version, versionName));
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setView(aboutView)
                        .setPositiveButton("OK", null);
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            case R.id.uzbek:
                ChangeLanguage.setLocal(this,"uz");
                onBackPressed();
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case R.id.english:
                ChangeLanguage.setLocal(this,"en");
                onBackPressed();
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case R.id.russian:
                ChangeLanguage.setLocal(this,"ru");
                onBackPressed();
                startActivity(new Intent(this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public static String getAppVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onBackPressed() {
        super.finish();
        //StartAppAd.onBackPressed(this);
        super.onBackPressed();
    }
}