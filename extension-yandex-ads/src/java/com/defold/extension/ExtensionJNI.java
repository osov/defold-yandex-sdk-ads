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

public class ExtensionJNI {

  private static final String TAG = "ExtensionYandexAds";

  public static native void AddToQueue(int msg, String json);

  private static final int MSG_ADS_INITED = 1;
  private static final int MSG_INTER_LOADED = 2;
  private Activity activity;

  public ExtensionJNI(Activity mainActivity) {
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
            sendSimpleMessage(MSG_ADS_INITED, "init", "ok");
          }
        });
      }
    });
  }

  public void loadInterstitial(final String unitId) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
            Log.d(TAG, "loadInterstitial");
            sendSimpleMessage(MSG_INTER_LOADED, "init", unitId);
      }
    });
  }

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

  private void sendSimpleMessage(int msg, String key, String value) {
    String message = null;
    try {
      JSONObject obj = new JSONObject();
      obj.put(key, value);
      message = obj.toString();
    } catch (JSONException e) {
      message = getJsonConversionErrorMessage(e.getLocalizedMessage());
    }
    AddToQueue(msg, message);
  }

}
