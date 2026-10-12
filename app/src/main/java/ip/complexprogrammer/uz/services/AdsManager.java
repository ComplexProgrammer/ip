package ip.complexprogrammer.uz.services;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

/**
 * Keeps full-screen ads out of the user's way: an interstitial at most every
 * {@link #SEARCHES_PER_INTERSTITIAL} manual lookups and only after the result is on screen,
 * and none at all while a rewarded ad-free period is active.
 */
public final class AdsManager {
    private static final String TAG = "AdsManager";
    public static final String INTERSTITIAL_ID = "ca-app-pub-7773722896374259/2800286177";
    public static final String REWARDED_ID = "ca-app-pub-7773722896374259/5043306138";
    public static final String NATIVE_ID = "ca-app-pub-7773722896374259/5378732148";
    public static final int SEARCHES_PER_INTERSTITIAL = 4;
    public static final long AD_FREE_DURATION_MS = 24 * 60 * 60 * 1000L;

    private static InterstitialAd interstitial;
    private static boolean loadingInterstitial;
    private static boolean showingFullScreen;

    public interface RewardCallback {
        void onRewarded();

        void onUnavailable();
    }

    private AdsManager() {
    }

    public static boolean isShowingFullScreen() {
        return showingFullScreen;
    }

    public static void preloadInterstitial(Context context) {
        if (AppPrefs.isAdFree(context) || interstitial != null || loadingInterstitial) return;
        loadingInterstitial = true;
        InterstitialAd.load(context.getApplicationContext(), INTERSTITIAL_ID, new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitial = ad;
                        loadingInterstitial = false;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        loadingInterstitial = false;
                        Log.d(TAG, "Interstitial failed: " + error.getMessage());
                    }
                });
    }

    /** Call after a manual lookup result is displayed. */
    public static void onSearchCompleted(Activity activity) {
        if (AppPrefs.isAdFree(activity)) return;
        int count = AppPrefs.incrementSearchCount(activity);
        if (count % SEARCHES_PER_INTERSTITIAL != 0 || interstitial == null) {
            preloadInterstitial(activity);
            return;
        }
        InterstitialAd ad = interstitial;
        interstitial = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                showingFullScreen = true;
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                showingFullScreen = false;
                preloadInterstitial(activity);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                showingFullScreen = false;
            }
        });
        ad.show(activity);
    }

    /** Loads and immediately shows a rewarded ad; grants 24 ad-free hours when the reward is earned. */
    public static void showRewardedForAdFree(Activity activity, RewardCallback callback) {
        RewardedAd.load(activity, REWARDED_ID, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                final boolean[] earned = {false};
                ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        showingFullScreen = true;
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        showingFullScreen = false;
                        if (earned[0]) callback.onRewarded();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                        showingFullScreen = false;
                        callback.onUnavailable();
                    }
                });
                ad.show(activity, rewardItem -> {
                    earned[0] = true;
                    AppPrefs.grantAdFree(activity, AD_FREE_DURATION_MS);
                    interstitial = null;
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                Log.d(TAG, "Rewarded failed: " + error.getMessage());
                callback.onUnavailable();
            }
        });
    }
}
