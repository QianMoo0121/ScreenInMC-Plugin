/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class cn_mingbai_ScreenInMC_Natives_GPUDither */

#ifndef _Included_cn_mingbai_ScreenInMC_Natives_GPUDither
#define _Included_cn_mingbai_ScreenInMC_Natives_GPUDither
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     cn_mingbai_ScreenInMC_Natives_GPUDither
 * Method:    getPlatforms
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_getPlatforms
  (JNIEnv *, jclass);

/*
 * Class:     cn_mingbai_ScreenInMC_Natives_GPUDither
 * Method:    init
 * Signature: (I[III)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_init
  (JNIEnv *, jclass, jint, jintArray, jint, jint);

/*
 * Class:     cn_mingbai_ScreenInMC_Natives_GPUDither
 * Method:    unInit
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_unInit
  (JNIEnv *, jclass);

/*
 * Class:     cn_mingbai_ScreenInMC_Natives_GPUDither
 * Method:    dither
 * Signature: ([IIII)[B
 */
JNIEXPORT jbyteArray JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_dither
  (JNIEnv *, jclass, jintArray, jint, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
