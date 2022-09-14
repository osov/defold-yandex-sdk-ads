#if defined(DM_PLATFORM_ANDROID) 

#pragma once

namespace dmApp {

void Initialize_Ext();

void Initialize();
void LoadInterstitial(const char* unitId);

void ActivateApp();


} //namespace dmApp

#endif
