#if defined(DM_PLATFORM_ANDROID)
#pragma once

#include "extension_private.h"
#include <dmsdk/sdk.h>

namespace dmYandexAds
{

    enum MessageId
    {
        MSG_ADS_INITED = 0,
        MSG_INTERSTITIAL = 1,
        MSG_REWARDED = 2,
        MSG_BANNER = 3
    };

    enum MessageEvent
    {
        EVENT_LOADED = 0,
        EVENT_ERROR_LOAD = 1,
        EVENT_SHOWN = 2,
        EVENT_DISMISSED = 3,
        EVENT_CLICKED = 4,
        EVENT_IMPRESSION = 5,
        EVENT_NOT_LOADED = 6,
        EVENT_REWARDED = 7,
        EVENT_DESTROYED = 8,
    };

    struct CallbackData
    {
        MessageId msg;
        char *json;
    };

    void SetLuaCallback(lua_State *L, int pos);
    void UpdateCallback();
    void InitializeCallback();
    void FinalizeCallback();

    void AddToQueueCallback(MessageId type, const char *json);

}

#endif
