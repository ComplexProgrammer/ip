package ip.complexprogrammer.uz;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.color.MaterialColors;

import ip.complexprogrammer.uz.databinding.ActivityMapsBinding;
import ip.complexprogrammer.uz.services.ValueFormatter;

/**
 * Shows the approximate area of an IP address. IP geolocation is city-level at best,
 * so we draw a radius instead of pretending to know the street.
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String EXTRA_LAT = "lat";
    public static final String EXTRA_LON = "lon";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_IP = "ip";
    private static final double ACCURACY_RADIUS_M = 10_000;
    private static final float ZOOM = 10f;

    private ActivityMapsBinding binding;
    private GoogleMap map;
    private double latitude;
    private double longitude;
    private String title;
    private Insets systemBars = Insets.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyEdgeToEdge();

        if (!getIntent().hasExtra(EXTRA_LAT) || !getIntent().hasExtra(EXTRA_LON)) {
            finish();
            return;
        }
        latitude = getIntent().getDoubleExtra(EXTRA_LAT, 0);
        longitude = getIntent().getDoubleExtra(EXTRA_LON, 0);
        title = getIntent().getStringExtra(EXTRA_TITLE);
        String ip = getIntent().getStringExtra(EXTRA_IP);

        binding.place.setText(title == null || title.isEmpty() ? ip : title);
        binding.coords.setText(ip + "  ·  " + ValueFormatter.coordinates(latitude, longitude));
        binding.back.setOnClickListener(v -> finish());
        binding.mapTypes.setOnCheckedStateChangeListener((group, checkedIds) -> applyMapType());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        updateMapPadding();
        applyMapType();

        LatLng position = new LatLng(latitude, longitude);
        int primary = MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary, Color.BLUE);
        map.addCircle(new CircleOptions()
                .center(position)
                .radius(ACCURACY_RADIUS_M)
                .strokeWidth(3f)
                .strokeColor(primary)
                .fillColor(ColorUtils.setAlphaComponent(primary, 50)));
        map.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .snippet(getString(R.string.map_approx_title)));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM));
    }

    /** The map fills the screen behind the system bars; the overlays and map controls stay clear of them. */
    private void applyEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        int topBarPadding = binding.topBar.getPaddingTop();
        ViewGroup.MarginLayoutParams cardParams = (ViewGroup.MarginLayoutParams) binding.infoCard.getLayoutParams();
        int cardMarginBottom = cardParams.bottomMargin;
        int cardMarginStart = cardParams.getMarginStart();
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            binding.topBar.setPadding(topBarPadding + systemBars.left, topBarPadding + systemBars.top,
                    topBarPadding + systemBars.right, topBarPadding);
            cardParams.bottomMargin = cardMarginBottom + systemBars.bottom;
            cardParams.setMarginStart(cardMarginStart + systemBars.left);
            binding.infoCard.setLayoutParams(cardParams);
            updateMapPadding();
            return insets;
        });
    }

    private void updateMapPadding() {
        if (map == null) return;
        float density = getResources().getDisplayMetrics().density;
        map.setPadding(systemBars.left, (int) (72 * density) + systemBars.top,
                systemBars.right, (int) (140 * density) + systemBars.bottom);
    }

    private void applyMapType() {
        if (map == null) return;
        int checked = binding.mapTypes.getCheckedChipId();
        if (checked == R.id.type_hybrid) map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        else if (checked == R.id.type_satellite) map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        else if (checked == R.id.type_terrain) map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        else map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        boolean night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        if (night && checked == R.id.type_normal) {
            map.setMapStyle(com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(this, R.raw.map_night));
        } else {
            map.setMapStyle(null);
        }
    }
}
