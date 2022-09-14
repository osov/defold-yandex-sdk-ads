#define EXTENSION_NAME YandexSdkAds
#define LIB_NAME "YandexAds"
#define MODULE_NAME "yandexads"

#define DLIB_LOG_DOMAIN LIB_NAME
#include <dmsdk/sdk.h>

#if defined(DM_PLATFORM_ANDROID)

#include "extension_private.h"
#include "extension_callback_private.h"
#include "utils/LuaUtils.h"

namespace dmApp {

static int Lua_Initialize(lua_State* L)
{
   DM_LUA_STACK_CHECK(L, 0);
    Initialize();
    return 0; 
}

static int Lua_SetCallback(lua_State* L)
{
    DM_LUA_STACK_CHECK(L, 0);
    SetLuaCallback(L, 1);
    return 0;
}

static int Lua_LoadInterstitial(lua_State* L)
{
    DM_LUA_STACK_CHECK(L, 0);
    if (lua_type(L, 1) != LUA_TSTRING) {
        char msg[256];
        snprintf(msg, sizeof(msg), "Expected string, got %s. Wrong type for Interstitial UnitId variable '%s'.", luaL_typename(L, 1), lua_tostring(L, 1));
        luaL_error(L, msg);
        return 0;
    }
    const char* unitId_lua = luaL_checkstring(L, 1);
    LoadInterstitial(unitId_lua);
    return 0;
}

static const luaL_reg Module_methods[] =
{
    {"initialize", Lua_Initialize},
    {"set_callback", Lua_SetCallback},
    {"load_interstitial", Lua_LoadInterstitial},
    {0, 0}
};

static void LuaInit(lua_State* L)
{
    DM_LUA_STACK_CHECK(L, 0);
    luaL_register(L, MODULE_NAME, Module_methods);

    #define SETCONSTANT(name) \
    lua_pushnumber(L, (lua_Number) name); \
    lua_setfield(L, -2, #name); \

    SETCONSTANT(MSG_ADS_INITED)
    SETCONSTANT(MSG_INTER_LOADED)
    SETCONSTANT(MSG_NOT_SUPPORTED)
    SETCONSTANT(MSG_JSON_ERROR)

    #undef SETCONSTANT

    lua_pop(L, 1);
}

static dmExtension::Result AppInitializeApp(dmExtension::AppParams* params)
{
    return dmExtension::RESULT_OK;
}

static dmExtension::Result InitializeApp(dmExtension::Params* params)
{
    LuaInit(params->m_L);
    Initialize_Ext();
    InitializeCallback();
    return dmExtension::RESULT_OK;
}

static dmExtension::Result AppFinalizeApp(dmExtension::AppParams* params)
{
    return dmExtension::RESULT_OK;
}

static dmExtension::Result FinalizeApp(dmExtension::Params* params)
{
    FinalizeCallback();
    return dmExtension::RESULT_OK;
}

static dmExtension::Result UpdateApp(dmExtension::Params* params)
{
    UpdateCallback();
    return dmExtension::RESULT_OK;
}

static void OnEventApp(dmExtension::Params* params, const dmExtension::Event* event)
 {
    switch(event->m_Event)
    {
        case dmExtension::EVENT_ID_ACTIVATEAPP:
            ActivateApp();
            break;
    }
 }

} //namespace dmApp

DM_DECLARE_EXTENSION(EXTENSION_NAME, LIB_NAME, dmApp::AppInitializeApp, dmApp::AppFinalizeApp, dmApp::InitializeApp, dmApp::UpdateApp, dmApp::OnEventApp, dmApp::FinalizeApp)

#else

static  dmExtension::Result InitializeApp(dmExtension::Params* params)
{
    dmLogInfo("Registered extension App (null)");
    return dmExtension::RESULT_OK;
}

static dmExtension::Result FinalizeApp(dmExtension::Params* params)
{
    return dmExtension::RESULT_OK;
}

DM_DECLARE_EXTENSION(EXTENSION_NAME, LIB_NAME, 0, 0, InitializeApp, 0, 0, FinalizeApp)

#endif // Android
