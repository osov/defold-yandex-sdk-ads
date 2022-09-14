#if defined(DM_PLATFORM_ANDROID) 

#pragma once

namespace dmYandexAds {

void Initialize_Ext();

void Initialize();
void LoadInterstitial(const char* unitId);

void ActivateApp();


}

#endif
