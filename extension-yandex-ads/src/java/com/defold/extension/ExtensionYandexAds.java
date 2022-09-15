package com.defold.extension;

import androidx.annotation.NonNull;
import android.util.Log;
import android.app.Activity;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import com.yandex.mobile.ads.banner.AdSize;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.InitializationListener;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;

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

  private static final int BANNER_320_50 = 0;

  private Activity activity;
  private InterstitialAd mInterstitialAd;
  private RewardedAd mRewardedAd;
  private BannerAdView mBannerAdView;
  private RelativeLayout mBannerLayout;

  public ExtensionYandexAds(Activity mainActivity) {
    activity = mainActivity;
  }

  public void initialize() {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        MobileAds.initialize(activity, new InitializationListener() {
          @Override
          public void onInitializationCompleted() {
            Log.d(TAG, "onInitializationCompleted");
            sendSimpleMessage(MSG_ADS_INITED, EVENT_LOADED);
          }
        });
      }
    });
  }

  // ------------------------------------------------------------------------------------------

  public void loadInterstitial(final String unitId) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "loadInterstitial");
        mInterstitialAd = new InterstitialAd(activity);
        mInterstitialAd.setAdUnitId(unitId);
        AdRequest adRequest = new AdRequest.Builder().build();

        mInterstitialAd.setInterstitialAdEventListener(new InterstitialAdEventListener() {
          @Override
          public void onAdLoaded() {
            Log.d(TAG, "interstitial:onAdLoaded");
            sendSimpleMessage(MSG_INTERSTITIAL, EVENT_LOADED);
          }

          @Override
          public void onAdFailedToLoad(AdRequestError adRequestError) {
            Log.e(TAG, "interstitial:onAdFailedToLoad" + adRequestError.toString());
            sendSimpleMessage(MSG_INTERSTITIAL, EVENT_ERROR_LOAD, "error", adRequestError.toString());
          }

          @Override
          public void onAdShown() {
            Log.d(TAG, "interstitial:onAdShown");
            sendSimpleMessage(MSG_INTERSTITIAL, EVENT_SHOWN);
          }

          @Override
          public void onAdDismissed() {
            Log.d(TAG, "interstitial:onAdDismissed");
            sendSimpleMessage(MSG_INTERSTITIAL, EVENT_DISMISSED);
          }

          @Override
          public void onAdClicked() {
            Log.d(TAG, "interstitial:onAdClicked");
            sendSimpleMessage(MSG_INTERSTITIAL, EVENT_CLICKED);
          }

          @Override
          public void onImpression(@Nullable ImpressionData impressionData) {
            Log.d(TAG, "interstitial:onImpression");
            sendSimpleMessage(MSG_INTERSTITIAL, EVENT_IMPRESSION);
          }

          @Override
          public void onLeftApplication() {
          }

          @Override
          public void onReturnedToApplication() {
          }

        });

        mInterstitialAd.loadAd(adRequest);
      }
    });
  }

  public boolean isInterstitialLoaded() {
    return mInterstitialAd != null && mInterstitialAd.isLoaded();
  }

  public void showInterstitial() {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (isInterstitialLoaded()) {
          Log.d(TAG, "showInterstitial");
          mInterstitialAd.show();
        } else {
          Log.e(TAG, "The interstitial ad wasn't ready yet.");
          sendSimpleMessage(MSG_INTERSTITIAL, EVENT_NOT_LOADED, "error",
              "Can't show Interstitial AD that wasn't loaded.");
        }
      }
    });
  }

  // ------------------------------------------------------------------------------------------

  public void loadRewarded(final String unitId) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "loadRewarded");
        mRewardedAd = new RewardedAd(activity);
        mRewardedAd.setAdUnitId(unitId);
        AdRequest adRequest = new AdRequest.Builder().build();

        mRewardedAd.setRewardedAdEventListener(new RewardedAdEventListener() {
          @Override
          public void onRewarded(final Reward reward) {
            Log.d(TAG, "rewarded:onRewarded");
            sendSimpleMessage(MSG_REWARDED, EVENT_REWARDED);
          }

          @Override
          public void onAdClicked() {
            Log.d(TAG, "rewarded:onAdClicked");
            sendSimpleMessage(MSG_REWARDED, EVENT_CLICKED);
          }

          @Override
          public void onAdLoaded() {
            rewarded_video_ad_is_loaded = true;
            Log.d(TAG, "rewarded:onAdLoaded");
            sendSimpleMessage(MSG_REWARDED, EVENT_LOADED);
          }

          @Override
          public void onAdFailedToLoad(final AdRequestError adRequestError) {
            Log.e(TAG, "rewarded:onAdFailedToLoad" + adRequestError.toString());
            sendSimpleMessage(MSG_REWARDED, EVENT_ERROR_LOAD, "error", adRequestError.toString());
          }

          @Override
          public void onAdShown() {
            Log.d(TAG, "rewarded:onAdShown");
            sendSimpleMessage(MSG_REWARDED, EVENT_SHOWN);
          }

          @Override
          public void onAdDismissed() {
            Log.d(TAG, "rewarded:onAdDismissed");
            sendSimpleMessage(MSG_REWARDED, EVENT_DISMISSED);
          }

          @Override
          public void onImpression(@Nullable ImpressionData impressionData) {
            Log.d(TAG, "rewarded:onImpression");
            sendSimpleMessage(MSG_REWARDED, EVENT_IMPRESSION);
          }

          @Override
          public void onLeftApplication() {
          }

          @Override
          public void onReturnedToApplication() {
          }
        });
        // Загрузка объявления.
        mRewardedAd.loadAd(adRequest);
      }
    });
  }

  public boolean isRewardedLoaded() {
    return mRewardedAd != null && mRewardedAd.isLoaded();
  }

  public void showRewarded() {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (isRewardedLoaded()) {
          Log.d(TAG, "showRewarded");
          mRewardedAd.show();
        } else {
          Log.e(TAG, "The rewarded ad wasn't ready yet.");
          sendSimpleMessage(MSG_REWARDED, EVENT_NOT_LOADED, "error",
              "Can't show rewarded AD that wasn't loaded.");
        }
      }
    });
  }

  // ------------------------------------------------------------------------------------------

  public void loadBanner(final String unitId, int bannerSize) {
    int w = 320;
    int h = 50;
    // todo add other...
    if (bannerSize == BANNER_320_50) {
    }
    destroyBanner();
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "loadBanner");
        mBannerAdView = new BannerAdView(activity);
        mBannerAdView.setAdUnitId(unitId);
        mBannerAdView.setAdSize(AdSize.flexibleSize(w, h));
        final BannerAdView view = mBannerAdView;
        AdRequest adRequest = new AdRequest.Builder().build();
        mBannerAdView.setBannerAdEventListener(new BannerAdEventListener() {
          @Override
          public void onAdLoaded() {
            Log.d(TAG, "banner:onAdLoaded");
            if (view != mBannerAdView) {
              Log.w(TAG, "Prevent reporting onAdLoaded for obsolete BannerAd (loadBanner was called multiple times)");
              view.destroy();
              return;
            }
            showBanner();
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
            sendSimpleMessage(MSG_BANNER, EVENT_IMPRESSION);
          }

          @Override
          public void onLeftApplication() {
          }

          @Override
          public void onReturnedToApplication() {
          }
      
        });

        // Загрузка объявления.
        mBannerAdView.loadAd(adRequest);
      }
    });
  }

  public boolean isBannerLoaded() {
    // todo проверка на реальность загружен ли он по факту
    return mBannerAdView != null;
  }

  public void destroyBanner() {
    if (!isBannerLoaded()) {
      return;
    }
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "destroyBanner");
        mBannerAdView.destroy();
        mBannerAdView = null;
        removeBannerLayout();
      }
    });
  }

  public void showBanner(final int pos) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "showBanner");
        recreateBannerLayout();
        mBannerLayout.setVisibility(View.VISIBLE);
        mBannerAdView.setBackgroundColor(Color.TRANSPARENT);
      }
    });
  }

  public void hideBanner() {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "hideBanner");
        mBannerLayout.setVisibility(View.INVISIBLE);
      }
    });
  }

  private void removeBannerLayout() {
    if (mBannerLayout != null) {
      mBannerLayout.removeAllViews();
      activity.getWindowManager().removeView(mBannerLayout);
      mBannerLayout = null;
    }
  }

  private WindowManager.LayoutParams getWindowLayoutParams() {
    WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
    windowParams.x = WindowManager.LayoutParams.WRAP_CONTENT;
    windowParams.y = WindowManager.LayoutParams.WRAP_CONTENT;
    windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
    windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    windowParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
    return windowParams;
  }

  private void recreateBannerLayout() {
    removeBannerLayout();
    mBannerLayout = new RelativeLayout(activity);
    mBannerLayout.setVisibility(View.GONE);
    mBannerLayout.setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT);
    adParams.setMargins(0, 0, 0, 0);
    mBannerLayout.addView(mBannerAdView, adParams);
    activity.getWindowManager().addView(mBannerLayout, getWindowLayoutParams());
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
