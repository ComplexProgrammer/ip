package ip.complexprogrammer.uz;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdActivity;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import ip.complexprogrammer.uz.services.AdsManager;
import ip.complexprogrammer.uz.services.AppPrefs;

/**
 * Shows an app-open ad when the user comes back to the app after at least
 * {@link #MIN_BACKGROUND_MS}. Never on a cold start, so the first result appears immediately.
 */
public class AppOpenManager implements DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {
    private static final String LOG_TAG = "AppOpenManager";
    private static final String AD_UNIT_ID = "ca-app-pub-7773722896374259/8129297187";
    private static final long MAX_AD_AGE_MS = 4 * 3600_000L;
    private static final long MIN_BACKGROUND_MS = 30_000L;

    private final Application application;
    private AppOpenAd appOpenAd;
    private long loadTime;
    private boolean isLoading;
    private boolean isShowingAd;
    private boolean coldStart = true;
    private long backgroundSince;
    @Nullable
    private Activity currentActivity;

    public AppOpenManager(Application application) {
        this.application = application;
        application.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        boolean longEnough = backgroundSince > 0 && System.currentTimeMillis() - backgroundSince >= MIN_BACKGROUND_MS;
        if (coldStart) {
            coldStart = false;
            fetchAd();
        } else if (longEnough) {
            showAdIfAvailable();
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        backgroundSince = System.currentTimeMillis();
    }

    private void showAdIfAvailable() {
        if (AppPrefs.isAdFree(application) || isShowingAd || AdsManager.isShowingFullScreen()
                || currentActivity == null || currentActivity instanceof AdActivity) {
            return;
        }
        if (!isAdAvailable()) {
            fetchAd();
            return;
        }
        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                appOpenAd = null;
                isShowingAd = false;
                fetchAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                appOpenAd = null;
                isShowingAd = false;
                fetchAd();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                isShowingAd = true;
            }
        });
        appOpenAd.show(currentActivity);
    }

    private void fetchAd() {
        if (isLoading || isAdAvailable() || AppPrefs.isAdFree(application)) return;
        isLoading = true;
        AppOpenAd.load(application, AD_UNIT_ID, new AdRequest.Builder().build(),
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        loadTime = System.currentTimeMillis();
                        isLoading = false;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        isLoading = false;
                        Log.d(LOG_TAG, loadAdError.getMessage());
                    }
                });
    }

    private boolean isAdAvailable() {
        return appOpenAd != null && System.currentTimeMillis() - loadTime < MAX_AD_AGE_MS;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (!isShowingAd) currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (!isShowingAd) currentActivity = activity;
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (currentActivity == activity) currentActivity = null;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }
}
