#if defined(DM_PLATFORM_ANDROID)

#pragma once

namespace dmYandexAds
{

    enum BannerSize
    {
        BANNER_320_50 = 0
    };

    void Initialize_Ext();
    void ActivateApp();

    void Initialize();

    void LoadInterstitial(const char *unitId);
    bool IsInterstitialLoaded();
    void ShowInterstitial();

    void LoadRewarded(const char *unitId);
    bool IsRewardedLoaded();
    void ShowRewarded();

    void LoadBanner(const char *unitId, BannerSize bannerSize);
    bool IsBannerLoaded();
    void DestroyBanner();
    void ShowBanner();
    void HideBanner();

}

#endif
