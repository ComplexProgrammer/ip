package ip.complexprogrammer.uz;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.ads.MobileAds;

import ip.complexprogrammer.uz.services.AppPrefs;

public class MyApplication extends Application {
    private static AppOpenManager appOpenManager;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppPrefs.getNightMode(this));
        MobileAds.initialize(this, status -> { });
        appOpenManager = new AppOpenManager(this);
    }
}
