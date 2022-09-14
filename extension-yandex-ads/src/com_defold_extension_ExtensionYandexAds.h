#if defined(DM_PLATFORM_ANDROID)

#include <jni.h>

#ifndef COM_DEFOLD_EXTENSION_EXTENSIONYA_ADS_H
#define COM_DEFOLD_EXTENSION_EXTENSIONYA_ADS_H
#ifdef __cplusplus
extern "C" {
#endif
	
	JNIEXPORT void JNICALL Java_com_defold_extension_ExtensionYandexAds_AddToQueue
		(JNIEnv *, jclass, jint, jstring);

#ifdef __cplusplus
}
#endif
#endif

#endif
