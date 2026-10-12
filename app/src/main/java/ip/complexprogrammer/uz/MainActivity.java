package ip.complexprogrammer.uz;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Date;

import ip.complexprogrammer.uz.databinding.ActivityMainBinding;
import ip.complexprogrammer.uz.services.AdsManager;
import ip.complexprogrammer.uz.services.AppPrefs;
import ip.complexprogrammer.uz.ui.DeviceFragment;
import ip.complexprogrammer.uz.ui.HistoryFragment;
import ip.complexprogrammer.uz.ui.LookupFragment;
import ip.complexprogrammer.uz.ui.ToolsFragment;
import ip.complexprogrammer.uz.ui.UiUtils;

public class MainActivity extends AppCompatActivity {
    private static final String BANNER_ID = "ca-app-pub-7773722896374259/8849931038";
    private static final String TAG_LOOKUP = "lookup";
    private static final String TAG_HISTORY = "history";
    private static final String TAG_TOOLS = "tools";
    private static final String TAG_DEVICE = "device";

    private ActivityMainBinding binding;
    private AdView bannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        AppPrefs.migrateLegacyLanguage(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        applyEdgeToEdge();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, LookupFragment.newInstance(null), TAG_LOOKUP)
                    .commitNow();
        }
        binding.bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });
        binding.bottomNav.setOnItemReselectedListener(item -> { });
        loadBanner();
    }

    /**
     * Android 15+ always draws apps edge-to-edge, so do it on every version and pad the content
     * away from the system bars. BottomNavigationView pads itself for the navigation bar.
     */
    private void applyEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(0, ime.bottom - bars.bottom));
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        if (bannerView != null) bannerView.destroy();
        super.onDestroy();
    }

    /** Switches to the lookup tab and searches for {@code query} (empty = own IP). */
    public void openLookup(String query) {
        binding.bottomNav.setSelectedItemId(R.id.nav_lookup);
        LookupFragment lookup = (LookupFragment) getSupportFragmentManager().findFragmentByTag(TAG_LOOKUP);
        if (lookup != null) lookup.lookup(query);
    }

    private void showTab(int itemId) {
        FragmentManager fm = getSupportFragmentManager();
        String tag;
        if (itemId == R.id.nav_history) tag = TAG_HISTORY;
        else if (itemId == R.id.nav_tools) tag = TAG_TOOLS;
        else if (itemId == R.id.nav_device) tag = TAG_DEVICE;
        else tag = TAG_LOOKUP;

        FragmentTransaction tx = fm.beginTransaction().setReorderingAllowed(true);
        for (Fragment f : fm.getFragments()) {
            if (!tag.equals(f.getTag()) && !f.isHidden()) tx.hide(f);
        }
        Fragment target = fm.findFragmentByTag(tag);
        if (target == null) {
            tx.add(R.id.fragment_container, createFragment(tag), tag);
        } else {
            tx.show(target);
        }
        tx.commitNow();
    }

    private static Fragment createFragment(String tag) {
        switch (tag) {
            case TAG_HISTORY:
                return new HistoryFragment();
            case TAG_TOOLS:
                return new ToolsFragment();
            case TAG_DEVICE:
                return new DeviceFragment();
            default:
                return LookupFragment.newInstance(null);
        }
    }

    // Ads

    private void loadBanner() {
        if (AppPrefs.isAdFree(this)) {
            binding.bannerContainer.setVisibility(View.GONE);
            return;
        }
        bannerView = new AdView(this);
        bannerView.setAdUnitId(BANNER_ID);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int widthDp = (int) (metrics.widthPixels / metrics.density);
        bannerView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, widthDp));
        binding.bannerContainer.removeAllViews();
        binding.bannerContainer.addView(bannerView);
        binding.bannerContainer.setVisibility(View.VISIBLE);
        // Refresh rate is controlled from the AdMob console, not from code.
        bannerView.loadAd(new AdRequest.Builder().build());
    }

    private void onAdFreeGranted() {
        binding.bannerContainer.setVisibility(View.GONE);
        if (bannerView != null) {
            bannerView.destroy();
            bannerView = null;
        }
        LookupFragment lookup = (LookupFragment) getSupportFragmentManager().findFragmentByTag(TAG_LOOKUP);
        if (lookup != null) lookup.onAdFreeChanged();
        Toast.makeText(this, R.string.remove_ads_thanks, Toast.LENGTH_LONG).show();
    }

    private void showRemoveAdsDialog() {
        String message = AppPrefs.isAdFree(this)
                ? getString(R.string.remove_ads_active, formatDateTime(AppPrefs.getAdFreeUntil(this)))
                : getString(R.string.remove_ads_msg);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.remove_ads_title)
                .setMessage(message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.remove_ads_watch, (d, w) ->
                        AdsManager.showRewardedForAdFree(this, new AdsManager.RewardCallback() {
                            @Override
                            public void onRewarded() {
                                onAdFreeGranted();
                            }

                            @Override
                            public void onUnavailable() {
                                Toast.makeText(MainActivity.this, R.string.remove_ads_unavailable, Toast.LENGTH_LONG).show();
                            }
                        }))
                .show();
    }

    private String formatDateTime(long millis) {
        Date date = new Date(millis);
        return DateFormat.getMediumDateFormat(this).format(date) + " " + DateFormat.getTimeFormat(this).format(date);
    }

    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.telegram) {
            UiUtils.openUrl(this, "https://t.me/complexprogrammer");
        } else if (id == R.id.remove_ads) {
            showRemoveAdsDialog();
        } else if (id == R.id.theme) {
            showThemeDialog();
        } else if (id == R.id.language) {
            showLanguageDialog();
        } else if (id == R.id.privacy) {
            showPrivacyDialog();
        } else if (id == R.id.about) {
            showAboutDialog();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showThemeDialog() {
        int[] modes = {AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_YES};
        String[] labels = {getString(R.string.theme_system), getString(R.string.theme_light), getString(R.string.theme_dark)};
        int current = 0;
        for (int i = 0; i < modes.length; i++) if (modes[i] == AppPrefs.getNightMode(this)) current = i;
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.menu_theme)
                .setSingleChoiceItems(labels, current, (d, which) -> {
                    d.dismiss();
                    AppPrefs.setNightMode(this, modes[which]);
                })
                .show();
    }

    private void showLanguageDialog() {
        String[] tags = {"uz", "en", "ru"};
        String[] labels = {getString(R.string.uzbek), getString(R.string.english), getString(R.string.russian)};
        String language = AppPrefs.getLanguage();
        if (language.isEmpty()) language = getResources().getConfiguration().locale.getLanguage();
        int current = -1;
        for (int i = 0; i < tags.length; i++) if (tags[i].equals(language)) current = i;
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.language)
                .setSingleChoiceItems(labels, current, (d, which) -> {
                    d.dismiss();
                    AppPrefs.setLanguage(tags[which]);
                })
                .show();
    }

    private void showPrivacyDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.privacy_policy)
                .setMessage(R.string.privacy_text)
                .setNeutralButton(R.string.privacy_open_full, (d, w) -> UiUtils.openUrl(this, getString(R.string.privacy_url)))
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showAboutDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.about, null);
        ((TextView) view.findViewById(R.id.version_label)).setText(getString(R.string.version, getAppVersionName(this)));
        new MaterialAlertDialogBuilder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setView(view)
                .setNeutralButton(R.string.privacy_policy, (d, w) -> showPrivacyDialog())
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    public static String getAppVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}
