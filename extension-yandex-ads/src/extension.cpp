#define EXTENSION_NAME YandexSdkAds
#define LIB_NAME "YandexAds"
#define MODULE_NAME "yandexads"

#define DLIB_LOG_DOMAIN LIB_NAME
#include <dmsdk/sdk.h>

#if defined(DM_PLATFORM_ANDROID)

#include "extension_private.h"
#include "extension_callback_private.h"
#include "utils/LuaUtils.h"

namespace dmYandexAds
{

    static int Lua_SetCallback(lua_State *L)
    {
        DM_LUA_STACK_CHECK(L, 0);
        SetLuaCallback(L, 1);
        return 0;
    }

    static int Lua_Initialize(lua_State *L)
    {
        DM_LUA_STACK_CHECK(L, 0);
        Initialize();
        return 0;
    }

    // ------------------------------------------------------------------------------------------

    static int Lua_LoadInterstitial(lua_State *L) {
		DM_LUA_STACK_CHECK(L, 0);
		if (lua_type(L, 1) != LUA_TSTRING) {
			return DM_LUA_ERROR("Expected string, got %s. Wrong type for Interstitial UnitId variable '%s'.", luaL_typename(L, 1), lua_tostring(L, 1));
		}
		const char *unitId_lua = luaL_checkstring(L, 1);
		LoadInterstitial(unitId_lua);
		return 0;
	}

	static int Lua_IsInterstitialLoaded(lua_State *L) {
		DM_LUA_STACK_CHECK(L, 1);
		bool is_loaded = IsInterstitialLoaded();
		lua_pushboolean(L, is_loaded);
		return 1;
	}

	static int Lua_ShowInterstitial(lua_State *L) {
		DM_LUA_STACK_CHECK(L, 0);
		ShowInterstitial();
		return 0;
	}

    // ------------------------------------------------------------------------------------------

    static int Lua_LoadRewarded(lua_State *L) {
		DM_LUA_STACK_CHECK(L, 0);
		if (lua_type(L, 1) != LUA_TSTRING) {
			return DM_LUA_ERROR("Expected string, got %s. Wrong type for Rewarded UnitId variable '%s'.", luaL_typename(L, 1), lua_tostring(L, 1));
		}
		const char *unitId_lua = luaL_checkstring(L, 1);
		LoadRewarded(unitId_lua);
		return 0;
	}

	static int Lua_IsRewardedLoaded(lua_State *L) {
		DM_LUA_STACK_CHECK(L, 1);
		bool is_loaded = IsRewardedLoaded();
		lua_pushboolean(L, is_loaded);
		return 1;
	}

	static int Lua_ShowRewarded(lua_State *L) {
		DM_LUA_STACK_CHECK(L, 0);
		ShowRewarded();
		return 0;
	}

    // ------------------------------------------------------------------------------------------

    static int Lua_LoadBanner(lua_State *L) {
		DM_LUA_STACK_CHECK(L, 0);
		if (lua_type(L, 1) != LUA_TSTRING) {
			return DM_LUA_ERROR("Expected string, got %s. Wrong type for Banner UnitId variable '%s'.", luaL_typename(L, 1), lua_tostring(L, 1));
		}
		const char *unitId_lua = luaL_checkstring(L, 1);
		int banner_width_lua = 0;
		int banner_height_lua = 0;
		if (lua_type(L, 2) != LUA_TNONE) {
			banner_width_lua = luaL_checkinteger(L, 2);
		}
		if (lua_type(L, 3) != LUA_TNONE) {
			banner_height_lua = luaL_checkinteger(L, 3);
		}
		LoadBanner(unitId_lua, banner_width_lua, banner_height_lua);
		return 0;
	}

	static int Lua_IsBannerLoaded(lua_State *L) {
		DM_LUA_STACK_CHECK(L, 1);
		bool is_loaded = IsBannerLoaded();
		lua_pushboolean(L, is_loaded);
		return 1;
	}

	static int Lua_ShowBanner(lua_State *L) {
		DM_LUA_STACK_CHECK(L, 0);
		BannerPosition bannerPos_lua = POS_BOTTOM_CENTER;
		if (lua_type(L, 1) != LUA_TNONE) {
			bannerPos_lua = (BannerPosition)luaL_checknumber(L, 1);
		}
		ShowBanner(bannerPos_lua);
		return 0;
	}

	static int Lua_HideBanner(lua_State *L) {
		DM_LUA_STACK_CHECK(L, 0);
		HideBanner();
		return 0;
	}

	static int Lua_DestroyBanner(lua_State *L) {
		DM_LUA_STACK_CHECK(L, 0);
		DestroyBanner();
		return 0;
	}

    // ------------------------------------------------------------------------------------------

    static const luaL_reg Module_methods[] =
        {
            {"initialize", Lua_Initialize},
            {"set_callback", Lua_SetCallback},

            {"load_interstitial", Lua_LoadInterstitial},
            {"is_interstitial_loaded", Lua_IsInterstitialLoaded},
            {"show_interstitial", Lua_ShowInterstitial},

            {"load_rewarded", Lua_LoadRewarded},
            {"is_rewarded_loaded", Lua_IsRewardedLoaded},
            {"show_rewarded", Lua_ShowRewarded},

            {"load_banner", Lua_LoadBanner},
            {"is_banner_loaded", Lua_IsBannerLoaded},
            {"destroy_banner", Lua_DestroyBanner},
            {"show_banner", Lua_ShowBanner},
            {"hide_banner", Lua_HideBanner},

            {0, 0}};

    static void LuaInit(lua_State *L)
    {
        DM_LUA_STACK_CHECK(L, 0);
        luaL_register(L, MODULE_NAME, Module_methods);

#define SETCONSTANT(name)                \
    lua_pushnumber(L, (lua_Number)name); \
    lua_setfield(L, -2, #name);

        SETCONSTANT(MSG_ADS_INITED)
        SETCONSTANT(MSG_INTERSTITIAL)
        SETCONSTANT(MSG_REWARDED)
        SETCONSTANT(MSG_BANNER)

        SETCONSTANT(EVENT_LOADED)
        SETCONSTANT(EVENT_ERROR_LOAD)
        SETCONSTANT(EVENT_SHOWN)
        SETCONSTANT(EVENT_DISMISSED)
        SETCONSTANT(EVENT_CLICKED)
        SETCONSTANT(EVENT_IMPRESSION)
        SETCONSTANT(EVENT_NOT_LOADED)
        SETCONSTANT(EVENT_REWARDED)
        SETCONSTANT(EVENT_DESTROYED)

        SETCONSTANT(POS_NONE)
	    SETCONSTANT(POS_TOP_LEFT)
	    SETCONSTANT(POS_TOP_CENTER)
	    SETCONSTANT(POS_TOP_RIGHT)
	    SETCONSTANT(POS_BOTTOM_LEFT)
	    SETCONSTANT(POS_BOTTOM_CENTER)
	    SETCONSTANT(POS_BOTTOM_RIGHT)
	    SETCONSTANT(POS_CENTER)

        SETCONSTANT(BANNER_320_50)

#undef SETCONSTANT

        lua_pop(L, 1);
    }

    static dmExtension::Result AppInitializeApp(dmExtension::AppParams *params)
    {
        return dmExtension::RESULT_OK;
    }

    static dmExtension::Result InitializeApp(dmExtension::Params *params)
    {
        LuaInit(params->m_L);
        Initialize_Ext();
        InitializeCallback();
        return dmExtension::RESULT_OK;
    }

    static dmExtension::Result AppFinalizeApp(dmExtension::AppParams *params)
    {
        return dmExtension::RESULT_OK;
    }

    static dmExtension::Result FinalizeApp(dmExtension::Params *params)
    {
        FinalizeCallback();
        return dmExtension::RESULT_OK;
    }

    static dmExtension::Result UpdateApp(dmExtension::Params *params)
    {
        UpdateCallback();
        return dmExtension::RESULT_OK;
    }

    static void OnEventApp(dmExtension::Params *params, const dmExtension::Event *event)
    {
        switch (event->m_Event)
        {
        case dmExtension::EVENT_ID_ACTIVATEAPP:
            ActivateApp();
            break;
        }
    }
}

DM_DECLARE_EXTENSION(EXTENSION_NAME, LIB_NAME, dmYandexAds::AppInitializeApp, dmYandexAds::AppFinalizeApp, dmYandexAds::InitializeApp, dmYandexAds::UpdateApp, dmYandexAds::OnEventApp, dmYandexAds::FinalizeApp)

#else

static dmExtension::Result InitializeApp(dmExtension::Params *params)
{
    dmLogInfo("Registered extension YandexAds (null)");
    return dmExtension::RESULT_OK;
}

static dmExtension::Result FinalizeApp(dmExtension::Params *params)
{
    return dmExtension::RESULT_OK;
}

DM_DECLARE_EXTENSION(EXTENSION_NAME, LIB_NAME, 0, 0, InitializeApp, 0, 0, FinalizeApp)

#endif // Android
