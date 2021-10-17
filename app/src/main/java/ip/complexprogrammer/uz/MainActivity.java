package ip.complexprogrammer.uz;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ip.complexprogrammer.uz.models._Response;
import ip.complexprogrammer.uz.services.ApiClient;
import ip.complexprogrammer.uz.services.ChangeLanguage;
import ip.complexprogrammer.uz.services.NetworkUtil;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {
    EditText _ip;
    Button ip_lookup;
    private _Response ResponseList=new _Response();
    private static final String IP_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String IPV4_PATTERN =
            "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
    private static final Pattern pattern = Pattern.compile(IPV4_PATTERN);

    public static boolean isValid(final String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ChangeLanguage.setLocal(this,ChangeLanguage.getLangCode(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _ip=findViewById(R.id._ip);
        ip_lookup=findViewById(R.id.ip_lookup);

        boolean status = NetworkUtil.getConnectivityStatusString(this);
        if(!status){
            startActivity(new Intent(this, NoInternetActivity.class));
        }
        else {
            LoadData();
            ip_lookup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.e("_ip",_ip.getText().toString());
                    if(isValid(_ip.getText().toString())){
                        Log.e("_ip","Yes");
                    }
                    else {
                        Log.e("_ip","No");
                    }
                    Toast.makeText(MainActivity.this,_ip.getText(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    public void LoadData(){
        Call<_Response> _response = ApiClient.getInterface().GetMyIpData();

        _response.enqueue(new Callback<_Response>() {
            @Override
            public void onResponse(Call<_Response> call, retrofit2.Response<_Response> response) {
                if(response.isSuccessful()){
                    ResponseList =response.body();
                    ImageView country_flag = findViewById(R.id.country_flag);
                    TextView ip=findViewById(R.id.ip);
                    TextView city=findViewById(R.id.city);
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
                    city.setText(ResponseList.getCity());
                    region.setText(ResponseList.getRegion());
                    region_code.setText(ResponseList.getRegion_code());
                    country.setText(ResponseList.getCountry());
                    country_code.setText(ResponseList.getCountry_code());
                    country_code_iso3.setText(ResponseList.getCountry_code_iso3());
                    country_name.setText(ResponseList.getCountry_name());
                    country_capital.setText(ResponseList.getCountry_capital());
                    country_tld.setText(ResponseList.getCountry_tld());
                    country_area.setText(ResponseList.getCountry_area());
                    country_population.setText(ResponseList.getCountry_population());
                    continent_code.setText(ResponseList.getContinent_code());
                    in_eu.setText(ResponseList.getIn_eu());
                    postal.setText(ResponseList.getPostal());
                    latitude.setText(ResponseList.getLatitude());
                    longitude.setText(ResponseList.getLongitude());
                    latlong.setText(ResponseList.getLatlong());
                    timezone.setText(ResponseList.getTimezone());
                    utc_offset.setText(ResponseList.getUtc_offset());
                    country_calling_code.setText(ResponseList.getCountry_calling_code());
                    currency.setText(ResponseList.getCurrency());
                    currency_name.setText(ResponseList.getCurrency_name());
                    languages.setText(ResponseList.getLanguages());
                    asn.setText(ResponseList.getAsn());
                    org.setText(ResponseList.getOrg());
                    String country_flag_url = "https://ipapi.co/static/images/flags/"+ResponseList.getCountry_code().toLowerCase(Locale.ROOT)+".png";
                    Glide.with(MainActivity.this).load(country_flag_url).into(country_flag);
                    //Utils.fetchSvg(MainActivity.this, country_flag_url, country_flag);

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
    }
}