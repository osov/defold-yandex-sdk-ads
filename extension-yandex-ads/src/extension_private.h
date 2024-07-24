#if defined(DM_PLATFORM_ANDROID)

#pragma once

namespace dmYandexAds
{

    enum BannerSize
    {
        BANNER_320_50 = 0
    };

    enum BannerPosition
    {
        POS_NONE =              0,
        POS_TOP_LEFT =          1,
        POS_TOP_CENTER =        2,
        POS_TOP_RIGHT =         3,
        POS_BOTTOM_LEFT =       4,
        POS_BOTTOM_CENTER =     5,
        POS_BOTTOM_RIGHT =      6,
        POS_CENTER =            7
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

    void LoadBanner(const char *unitId, int width, int height);
    bool IsBannerLoaded();
    void DestroyBanner();
    void ShowBanner(BannerPosition bannerPos);
    void HideBanner();

}

#endif
