#include <jni.h>
#include <cstdint>
#include <memory>

#include "chromaprint/src/chromaprint.h"

namespace {

struct ChromaprintSession {
    ChromaprintContext* context = chromaprint_new(CHROMAPRINT_ALGORITHM_DEFAULT);

    ~ChromaprintSession() {
        if (context != nullptr) {
            chromaprint_free(context);
        }
    }
};

ChromaprintSession* sessionFromHandle(jlong handle) {
    return reinterpret_cast<ChromaprintSession*>(handle);
}

}  // namespace

extern "C" JNIEXPORT jlong JNICALL
Java_elovaire_music_droidbeauty_app_data_tags_matching_NativeChromaprintBridge_nativeCreate(
    JNIEnv*,
    jobject) {
    auto session = std::make_unique<ChromaprintSession>();
    if (session->context == nullptr) {
        return 0;
    }
    return reinterpret_cast<jlong>(session.release());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_elovaire_music_droidbeauty_app_data_tags_matching_NativeChromaprintBridge_nativeStart(
    JNIEnv*,
    jobject,
    jlong handle,
    jint sampleRate,
    jint channels) {
    auto* session = sessionFromHandle(handle);
    if (session == nullptr || session->context == nullptr || sampleRate <= 0 || channels <= 0) {
        return JNI_FALSE;
    }
    return chromaprint_start(session->context, sampleRate, channels) == 1 ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_elovaire_music_droidbeauty_app_data_tags_matching_NativeChromaprintBridge_nativeFeed(
    JNIEnv* env,
    jobject,
    jlong handle,
    jshortArray samples,
    jint length) {
    auto* session = sessionFromHandle(handle);
    if (session == nullptr || session->context == nullptr || samples == nullptr || length <= 0) {
        return JNI_FALSE;
    }
    const jsize arrayLength = env->GetArrayLength(samples);
    const jint safeLength = length < arrayLength ? length : arrayLength;
    jshort* values = env->GetShortArrayElements(samples, nullptr);
    if (values == nullptr) {
        return JNI_FALSE;
    }
    const int result = chromaprint_feed(
        session->context,
        reinterpret_cast<const int16_t*>(values),
        safeLength);
    env->ReleaseShortArrayElements(samples, values, JNI_ABORT);
    return result == 1 ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_elovaire_music_droidbeauty_app_data_tags_matching_NativeChromaprintBridge_nativeFinish(
    JNIEnv* env,
    jobject,
    jlong handle) {
    auto* session = sessionFromHandle(handle);
    if (session == nullptr || session->context == nullptr || chromaprint_finish(session->context) != 1) {
        return nullptr;
    }
    char* fingerprint = nullptr;
    if (chromaprint_get_fingerprint(session->context, &fingerprint) != 1 || fingerprint == nullptr) {
        return nullptr;
    }
    jstring result = env->NewStringUTF(fingerprint);
    chromaprint_dealloc(fingerprint);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_elovaire_music_droidbeauty_app_data_tags_matching_NativeChromaprintBridge_nativeDestroy(
    JNIEnv*,
    jobject,
    jlong handle) {
    delete sessionFromHandle(handle);
}
