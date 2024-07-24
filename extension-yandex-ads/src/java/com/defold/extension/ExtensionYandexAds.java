package com.defold.extension;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.app.Activity;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;

import android.util.DisplayMetrics;

import org.json.JSONObject;
import org.json.JSONException;

import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;

import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader;

import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoader;

public class ExtensionYandexAds {

    private static final String TAG = "ExtensionYandexAds";

    public static native void AddToQueue(int msg, String json);

    private static final int MSG_ADS_INITED = 0;
    private static final int MSG_INTERSTITIAL = 1;
    private static final int MSG_REWARDED = 2;
    private static final int MSG_BANNER = 3;

    private static final int EVENT_LOADED = 0;
    private static final int EVENT_ERROR_LOAD = 1;
    private static final int EVENT_SHOWN = 2;
    private static final int EVENT_DISMISSED = 3;
    private static final int EVENT_CLICKED = 4;
    private static final int EVENT_IMPRESSION = 5;
    private static final int EVENT_NOT_LOADED = 6;
    private static final int EVENT_REWARDED = 7;
    private static final int EVENT_DESTROYED = 8;

    private static final int POS_NONE =                 0;
    private static final int POS_TOP_LEFT =             1;
    private static final int POS_TOP_CENTER =           2;
    private static final int POS_TOP_RIGHT =            3;
    private static final int POS_BOTTOM_LEFT =          4;
    private static final int POS_BOTTOM_CENTER =        5;
    private static final int POS_BOTTOM_RIGHT =         6;
    private static final int POS_CENTER =               7;
    private static final int BANNER_320_50 = 0;

    private final Activity activity;

    public ExtensionYandexAds(Activity mainActivity) {
        activity = mainActivity;
    }

    public void initialize() {
        activity.runOnUiThread(() -> MobileAds.initialize(activity, () -> {
            Log.d(TAG, "onInitializationCompleted");
            initInterstitial();
            initRewarded();
            initBanner();
            sendSimpleMessage(MSG_ADS_INITED, EVENT_LOADED);
        }));
    }


      public void enableLogging() {
        Log.d(TAG, "enableLogging");
        MobileAds.enableLogging(true);
    }

    // ------------------------------------------------------------------------------------------
    private InterstitialAdLoader mInterstitialAdLoader;
    private InterstitialAdEventListener mInterstitialAdEventListener;
    private InterstitialAd mInterstitialAd;

    private void initInterstitial(){
        mInterstitialAdLoader = new InterstitialAdLoader(activity);
        mInterstitialAdLoader.setAdLoadListener(new InterstitialAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                Log.d(TAG, "interstitial:onAdLoaded");
                mInterstitialAd = interstitialAd;
                sendSimpleMessage(MSG_INTERSTITIAL, EVENT_LOADED);
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                Log.e(TAG, "interstitial:onAdFailedToLoad" + adRequestError);
                sendSimpleMessage(MSG_INTERSTITIAL, EVENT_ERROR_LOAD, "error", adRequestError.toString());
            }
        });

        mInterstitialAdEventListener = new InterstitialAdEventListener() {
            @Override
            public void onAdShown() {
                Log.d(TAG, "interstitial:onAdShown");
                sendSimpleMessage(MSG_INTERSTITIAL, EVENT_SHOWN);
            }

            @Override
            public void onAdFailedToShow(@NonNull AdError adError) {
                Log.d(TAG, "interstitial:onAdFailedToShow: "+adError);
                sendSimpleMessage(MSG_INTERSTITIAL, EVENT_DISMISSED); // todo is failed
            }

            @Override
            public void onAdDismissed() {
                Log.d(TAG, "interstitial:onAdDismissed");
                destroyInterstitial();
                sendSimpleMessage(MSG_INTERSTITIAL, EVENT_DISMISSED);
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "interstitial:onAdClicked");
                sendSimpleMessage(MSG_INTERSTITIAL, EVENT_CLICKED);
            }

            @Override
            public void onAdImpression(@Nullable ImpressionData impressionData) {
                Log.d(TAG, "interstitial:onImpression");
                if (impressionData != null)
                    sendSimpleMessage(MSG_INTERSTITIAL, EVENT_IMPRESSION, "data", impressionData.getRawData());
            }
        };
    }

    public void loadInterstitial(final String unitId) {
        activity.runOnUiThread(() -> {
            Log.d(TAG, "loadInterstitial: "+unitId);
            if (mInterstitialAdLoader != null) {
                destroyInterstitial();
                mInterstitialAdLoader.loadAd(new AdRequestConfiguration.Builder(unitId).build());
            }
        });
    }

    public boolean isInterstitialLoaded() {
        return mInterstitialAd != null;
    }

    public void showInterstitial() {
        activity.runOnUiThread(() -> {
            if (isInterstitialLoaded()) {
                Log.d(TAG, "showInterstitial");
                mInterstitialAd.setAdEventListener(mInterstitialAdEventListener);
                mInterstitialAd.show(activity);
            } else {
                Log.e(TAG, "The interstitial ad wasn't ready yet.");
                sendSimpleMessage(MSG_INTERSTITIAL, EVENT_NOT_LOADED, "error","Can't show Interstitial AD that wasn't loaded.");
            }
        });
    }

    private void destroyInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd.setAdEventListener(null);
            mInterstitialAd = null;
        }
    }

    // ------------------------------------------------------------------------------------------
    private RewardedAdLoader mRewardedAdLoader;
    private  RewardedAdEventListener mRewardedAdEventListener;
    private RewardedAd mRewardedAd;

    private void initRewarded(){
        mRewardedAdLoader = new RewardedAdLoader(activity);
        mRewardedAdLoader.setAdLoadListener(new RewardedAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                Log.d(TAG, "rewarded:onAdLoaded");
                mRewardedAd = rewardedAd;
                sendSimpleMessage(MSG_REWARDED, EVENT_LOADED);
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                Log.e(TAG, "rewarded:onAdFailedToLoad" + adRequestError);
                sendSimpleMessage(MSG_REWARDED, EVENT_ERROR_LOAD, "error", adRequestError.toString());
            }
        });

        mRewardedAdEventListener = new RewardedAdEventListener() {
            @Override
            public void onAdShown() {
                Log.d(TAG, "rewarded:onAdShown");
                sendSimpleMessage(MSG_REWARDED, EVENT_SHOWN);
            }

            @Override
            public void onAdFailedToShow(@NonNull AdError adError) {
                Log.d(TAG, "rewarded:onAdFailedToShow: "+adError);
                sendSimpleMessage(MSG_REWARDED, EVENT_DISMISSED); // todo is failed
            }

            @Override
            public void onAdDismissed() {
                Log.d(TAG, "rewarded:onAdDismissed");
                destroyRewardedAd();
                sendSimpleMessage(MSG_REWARDED, EVENT_DISMISSED);
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "rewarded:onAdClicked");
                sendSimpleMessage(MSG_REWARDED, EVENT_CLICKED);
            }

            @Override
            public void onAdImpression(@Nullable ImpressionData impressionData) {
                Log.d(TAG, "rewarded:onImpression");
                if (impressionData != null)
                    sendSimpleMessage(MSG_REWARDED, EVENT_IMPRESSION, "data", impressionData.getRawData());
            }

            @Override
            public void onRewarded(@NonNull Reward reward) {
                Log.d(TAG, "rewarded:onRewarded");
                sendSimpleMessage(MSG_REWARDED, EVENT_REWARDED);
            }
        };
    }

    public void loadRewarded(final String unitId) {
        activity.runOnUiThread(() -> {
            Log.d(TAG, "loadRewarded: "+unitId);
            if (mRewardedAdLoader != null) {
                destroyRewardedAd();
                mRewardedAdLoader.loadAd(new AdRequestConfiguration.Builder(unitId).build());
            }
        });
    }

    public boolean isRewardedLoaded() {
        return mRewardedAd != null;
    }

    public void showRewarded() {
        activity.runOnUiThread(() -> {
            if (isRewardedLoaded()) {
                Log.d(TAG, "showRewarded");
                mRewardedAd.setAdEventListener(mRewardedAdEventListener);
                mRewardedAd.show(activity);
            } else {
                Log.e(TAG, "The rewarded ad wasn't ready yet.");
                sendSimpleMessage(MSG_REWARDED, EVENT_NOT_LOADED, "error","Can't show rewarded AD that wasn't loaded.");
            }
        });
    }

    private void destroyRewardedAd() {
        if (mRewardedAd != null) {
            mRewardedAd.setAdEventListener(null);
            mRewardedAd = null;
        }
    }

    // ------------------------------------------------------------------------------------------
    private BannerAdEventListener mBannerAdEventListener;
    private LinearLayout layout;
    private BannerAdView mBannerAdView;
    private WindowManager windowManager;
    private boolean isBannerShown = false;
    private int m_bannerPosition = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

    private void initBanner(){
       mBannerAdEventListener = new BannerAdEventListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "banner:onAdLoaded");
                sendSimpleMessage(MSG_BANNER, EVENT_LOADED);
            }

            @Override
            public void onAdFailedToLoad(AdRequestError adRequestError) {
                Log.e(TAG, "banner:onAdFailedToLoad" + adRequestError.toString());
                sendSimpleMessage(MSG_BANNER, EVENT_ERROR_LOAD, "error", adRequestError.toString());
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "banner:onAdClicked");
                sendSimpleMessage(MSG_BANNER, EVENT_CLICKED);
            }

            @Override
            public void onImpression(@Nullable ImpressionData impressionData) {
                Log.d(TAG, "banner:onImpression");
                if (impressionData != null)
                    sendSimpleMessage(MSG_BANNER, EVENT_IMPRESSION, "data", impressionData.getRawData());
            }

            @Override
            public void onLeftApplication() {}

            @Override
            public void onReturnedToApplication() {}
        };
    }

    public void loadBanner(final String unitId, int width, int height) {
        activity.runOnUiThread(() -> {
            Log.d(TAG, "loadBanner: "+unitId+' '+width+'/'+height);
            if (isBannerLoaded())
                _destroyBanner();

            final BannerAdView view = new BannerAdView(activity);
            view.setAdUnitId(unitId);
            BannerAdSize adSize = BannerAdSize.inlineSize(activity, 320, 50);
            if (width > 0 && height > 0)
                adSize = BannerAdSize.inlineSize(activity, width, height);
            else if (width > 0)
                adSize = BannerAdSize.stickySize(activity, width);
            view.setAdSize(adSize);
            view.setVisibility(View.INVISIBLE);
            mBannerAdView = view;
            createLayout();

            AdRequest adRequest = new AdRequest.Builder().build();
            view.setBannerAdEventListener(mBannerAdEventListener);
            // Загрузка объявления.
            view.loadAd(adRequest);
        });
    }

    public boolean isBannerLoaded() {
        return mBannerAdView != null;
    }

    private void _destroyBanner() {
        Log.d(TAG, "destroyBanner");
        if (!isBannerLoaded())
            return;

        if (isBannerShown && windowManager != null && layout != null) {
            try {
                windowManager.removeView(layout);
            } catch (Exception e) {
                Log.e(TAG, "_destroyBanner: " + e);
            }
        }
        mBannerAdView.destroy();
        mBannerAdView = null;
        layout = null;
        isBannerShown = false;
        sendSimpleMessage(MSG_BANNER, EVENT_DESTROYED);
    }

    public void destroyBanner() {
        activity.runOnUiThread(() -> {
            _destroyBanner();
        });
    }

    private int getGravity(int bannerPosConst) {
        int bannerPos = Gravity.NO_GRAVITY;
        switch (bannerPosConst) {
            case POS_TOP_LEFT:
                bannerPos = Gravity.TOP | Gravity.LEFT;
                break;
            case POS_TOP_CENTER:
                bannerPos = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                break;
            case POS_TOP_RIGHT:
                bannerPos = Gravity.TOP | Gravity.RIGHT;
                break;
            case POS_BOTTOM_LEFT:
                bannerPos = Gravity.BOTTOM | Gravity.LEFT;
                break;
            case POS_BOTTOM_CENTER:
                bannerPos = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                break;
            case POS_BOTTOM_RIGHT:
                bannerPos = Gravity.BOTTOM | Gravity.RIGHT;
                break;
            case POS_CENTER:
                bannerPos = Gravity.CENTER;
                break;
        }
        return bannerPos;
    }

    public void showBanner(final int pos) {
        activity.runOnUiThread(() -> {
            Log.d(TAG, "showBanner: "+pos);
            if (!isBannerLoaded()) {
                return;
            }
            if (layout == null)
                return;
            layout.setSystemUiVisibility(activity.getWindow().getDecorView().getSystemUiVisibility());
            int gravity = getGravity(pos);
            if ( m_bannerPosition != gravity && isBannerShown) {
                try {
                    m_bannerPosition = gravity;
                    windowManager.updateViewLayout(layout, getParameters());
                } catch (Exception e) {
                    Log.e(TAG, "showBanner: " + e);
                }
                return;
            }
            if (!layout.isShown()) {
                m_bannerPosition = gravity;
                windowManager.addView(layout, getParameters());
                mBannerAdView.setVisibility(View.VISIBLE);
                isBannerShown = true;
            }
        });
    }

    public void hideBanner() {
        activity.runOnUiThread(() -> {
            Log.d(TAG, "hideBanner");
            if (!isBannerLoaded() || !isBannerShown) {
                return;
            }
            isBannerShown = false;
            if (windowManager != null && layout != null) {
                try {
                    windowManager.removeView(layout);
                } catch (Exception e) {
                    Log.e(TAG, "hideBanner: " + e);
                }
            }
            mBannerAdView.setVisibility(View.INVISIBLE);
        });
    }

    public void updateBannerLayout() {
        activity.runOnUiThread(() -> {
            Log.d(TAG, "updateBannerLayout");
            if (!isBannerLoaded()) {
                return;
            }
            if (windowManager != null && layout != null) {
                layout.setSystemUiVisibility(activity.getWindow().getDecorView().getSystemUiVisibility());
                if (!isBannerShown) {
                    return;
                }

                try {
                    windowManager.removeView(layout);
                } catch (Exception e) {
                    Log.e(TAG, "updateBannerLayout(remove): " + e);
                }

                if (isBannerShown) {
                    try {
                        windowManager.updateViewLayout(layout, getParameters());
                    }
                    catch (Exception e) {
                        Log.e(TAG, "updateBannerLayout(update): " + e);
                    }
                    if (!layout.isShown()) {
                        windowManager.addView(layout, getParameters());
                    }
                }
            }
        });
    }

    private void createLayout() {
        if (layout == null){
            windowManager = activity.getWindowManager();
            layout = new LinearLayout(activity);
            layout.setOrientation(LinearLayout.VERTICAL);
        }
        MarginLayoutParams params = new MarginLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, 0, 0);
        layout.setSystemUiVisibility(activity.getWindow().getDecorView().getSystemUiVisibility());
        layout.addView(mBannerAdView, params);
    }

    private WindowManager.LayoutParams getParameters() {
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.x = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.y = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.width = dpToPx(320);//WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.height = dpToPx(50); //WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowParams.gravity = m_bannerPosition;
        return windowParams;
    }

    public int dpToPx(int dp) {
        return (int) (dp * ((float) activity.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));

    }

// ------------------------------------------------------------------------------------------

    private String getJsonConversionErrorMessage(String messageText) {
        String message = null;
        try {
            JSONObject obj = new JSONObject();
            obj.put("error", messageText);
            message = obj.toString();
        } catch (JSONException e) {
            message = "{ \"error\": \"Error while converting simple message to JSON.\" }";
        }
        return message;
    }

    private void sendSimpleMessage(int msg, int eventId) {
        String message = null;
        try {
            JSONObject obj = new JSONObject();
            obj.put("event", eventId);
            message = obj.toString();
        } catch (JSONException e) {
            message = getJsonConversionErrorMessage(e.getLocalizedMessage());
        }
        AddToQueue(msg, message);
    }

    private void sendSimpleMessage(int msg, int eventId, String key_2, String value_2) {
        String message = null;
        try {
            JSONObject obj = new JSONObject();
            obj.put("event", eventId);
            obj.put(key_2, value_2);
            message = obj.toString();
        } catch (JSONException e) {
            message = getJsonConversionErrorMessage(e.getLocalizedMessage());
        }
        AddToQueue(msg, message);
    }

}