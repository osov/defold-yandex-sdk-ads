#if defined(DM_PLATFORM_ANDROID)

#include <dmsdk/dlib/android.h>
#include "extension_private.h"
#include "com_defold_extension_ExtensionYandexAds.h"
#include "extension_callback_private.h"

JNIEXPORT void JNICALL Java_com_defold_extension_ExtensionYandexAds_AddToQueue(JNIEnv *env, jclass cls, jint jmsg, jstring jjson)
{
    const char *json = env->GetStringUTFChars(jjson, 0);
    dmYandexAds::AddToQueueCallback((dmYandexAds::MessageId)jmsg, json);
    env->ReleaseStringUTFChars(jjson, json);
}

namespace dmYandexAds
{

    struct App
    {
        jobject m_AppJNI;

        jmethodID m_Initialize;

        jmethodID m_LoadInterstitial;
        jmethodID m_IsInterstitialLoaded;
        jmethodID m_ShowInterstitial;

        jmethodID m_LoadRewarded;
        jmethodID m_IsRewardedLoaded;
        jmethodID m_ShowRewarded;

        jmethodID m_LoadBanner;
        jmethodID m_IsBannerLoaded;
        jmethodID m_DestroyBanner;
        jmethodID m_ShowBanner;
        jmethodID m_HideBanner;
        jmethodID m_UpdateBannerLayout;
    };

    static App g_app;

    static void CallVoidMethod(jobject instance, jmethodID method)
    {
        dmAndroid::ThreadAttacher threadAttacher;
        JNIEnv *env = threadAttacher.GetEnv();

        env->CallVoidMethod(instance, method);
    }

    static bool CallBoolMethod(jobject instance, jmethodID method)
    {
        dmAndroid::ThreadAttacher threadAttacher;
        JNIEnv *env = threadAttacher.GetEnv();

        jboolean return_value = (jboolean)env->CallBooleanMethod(instance, method);
        return JNI_TRUE == return_value;
    }

    static void CallVoidMethodChar(jobject instance, jmethodID method, const char *cstr)
    {
        dmAndroid::ThreadAttacher threadAttacher;
        JNIEnv *env = threadAttacher.GetEnv();

        jstring jstr = env->NewStringUTF(cstr);
        env->CallVoidMethod(instance, method, jstr);
        env->DeleteLocalRef(jstr);
    }

    static void CallVoidMethodCharInt(jobject instance, jmethodID method, const char *cstr, int cint)
    {
        dmAndroid::ThreadAttacher threadAttacher;
        JNIEnv *env = threadAttacher.GetEnv();

        jstring jstr = env->NewStringUTF(cstr);
        env->CallVoidMethod(instance, method, jstr, cint);
        env->DeleteLocalRef(jstr);
    }

       static void CallVoidMethodCharIntInt(jobject instance, jmethodID method, const char *cstr, int cint, int cint2)
    {
        dmAndroid::ThreadAttacher threadAttacher;
        JNIEnv *env = threadAttacher.GetEnv();

        jstring jstr = env->NewStringUTF(cstr);
        env->CallVoidMethod(instance, method, jstr, cint, cint2);
        env->DeleteLocalRef(jstr);
    }

    static void CallVoidMethodInt(jobject instance, jmethodID method, int cint)
    {
        dmAndroid::ThreadAttacher threadAttacher;
        JNIEnv *env = threadAttacher.GetEnv();

        env->CallVoidMethod(instance, method, cint);
    }

    static void CallVoidMethodBool(jobject instance, jmethodID method, bool cbool)
    {
        dmAndroid::ThreadAttacher threadAttacher;
        JNIEnv *env = threadAttacher.GetEnv();

        env->CallVoidMethod(instance, method, cbool);
    }

    static void InitJNIMethods(JNIEnv *env, jclass cls)
    {
        g_app.m_Initialize = env->GetMethodID(cls, "initialize", "()V");

        g_app.m_LoadInterstitial = env->GetMethodID(cls, "loadInterstitial", "(Ljava/lang/String;)V");
        g_app.m_IsInterstitialLoaded = env->GetMethodID(cls, "isInterstitialLoaded", "()Z");
        g_app.m_ShowInterstitial = env->GetMethodID(cls, "showInterstitial", "()V");

        g_app.m_LoadRewarded = env->GetMethodID(cls, "loadRewarded", "(Ljava/lang/String;)V");
        g_app.m_IsRewardedLoaded = env->GetMethodID(cls, "isRewardedLoaded", "()Z");
        g_app.m_ShowRewarded = env->GetMethodID(cls, "showRewarded", "()V");

        g_app.m_LoadBanner = env->GetMethodID(cls, "loadBanner", "(Ljava/lang/String;II)V");
        g_app.m_DestroyBanner = env->GetMethodID(cls, "destroyBanner", "()V");
        g_app.m_ShowBanner = env->GetMethodID(cls, "showBanner", "(I)V");
        g_app.m_HideBanner = env->GetMethodID(cls, "hideBanner", "()V");
        g_app.m_IsBannerLoaded = env->GetMethodID(cls, "isBannerLoaded", "()Z");
        g_app.m_UpdateBannerLayout= env->GetMethodID(cls, "updateBannerLayout", "()V");

    }

    void Initialize_Ext()
    {
        dmAndroid::ThreadAttacher threadAttacher;
        JNIEnv *env = threadAttacher.GetEnv();
        jclass cls = dmAndroid::LoadClass(env, "com.defold.extension.ExtensionYandexAds");

        InitJNIMethods(env, cls);

        jmethodID jni_constructor = env->GetMethodID(cls, "<init>", "(Landroid/app/Activity;)V");

        g_app.m_AppJNI = env->NewGlobalRef(env->NewObject(cls, jni_constructor, threadAttacher.GetActivity()->clazz));
    }

    void ActivateApp()
    {
        CallVoidMethod(g_app.m_AppJNI, g_app.m_UpdateBannerLayout);
    }

    void Initialize()
    {
        CallVoidMethod(g_app.m_AppJNI, g_app.m_Initialize);
    }

    // ------------------------------------------------------------------------------------------

    void LoadInterstitial(const char *unitId)
    {
        CallVoidMethodChar(g_app.m_AppJNI, g_app.m_LoadInterstitial, unitId);
    }

    bool IsInterstitialLoaded()
    {
        return CallBoolMethod(g_app.m_AppJNI, g_app.m_IsInterstitialLoaded);
    }

    void ShowInterstitial()
    {
        CallVoidMethod(g_app.m_AppJNI, g_app.m_ShowInterstitial);
    }

    // ------------------------------------------------------------------------------------------

    void LoadRewarded(const char *unitId)
    {
        CallVoidMethodChar(g_app.m_AppJNI, g_app.m_LoadRewarded, unitId);
    }

    bool IsRewardedLoaded()
    {
        return CallBoolMethod(g_app.m_AppJNI, g_app.m_IsRewardedLoaded);
    }

    void ShowRewarded()
    {
        CallVoidMethod(g_app.m_AppJNI, g_app.m_ShowRewarded);
    }

    // ------------------------------------------------------------------------------------------

    void LoadBanner(const char *unitId, int width, int height)
    {
        CallVoidMethodCharIntInt(g_app.m_AppJNI, g_app.m_LoadBanner, unitId, width, height);
    }

    void DestroyBanner()
    {
        CallVoidMethod(g_app.m_AppJNI, g_app.m_DestroyBanner);
    }

    void ShowBanner(BannerPosition bannerPos)
    {
        CallVoidMethodInt(g_app.m_AppJNI, g_app.m_ShowBanner, (int)bannerPos);
    }

    void HideBanner()
    {
        CallVoidMethod(g_app.m_AppJNI, g_app.m_HideBanner);
    }

    bool IsBannerLoaded()
    {
        return CallBoolMethod(g_app.m_AppJNI, g_app.m_IsBannerLoaded);
    }
    // ------------------------------------------------------------------------------------------

}

#endif
