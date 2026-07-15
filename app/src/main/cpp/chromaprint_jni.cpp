#include <jni.h>
#include <cstdint>
#include <atomic>
#include <cstring>
#include <memory>
#include <mutex>
#include <unordered_map>

#include "chromaprint/src/chromaprint.h"

namespace {

struct ChromaprintSession {
    ChromaprintContext* context = chromaprint_new(CHROMAPRINT_ALGORITHM_DEFAULT);
    std::mutex mutex;

    ~ChromaprintSession() {
        if (context != nullptr) {
            chromaprint_free(context);
        }
    }
};

std::mutex sessionsMutex;
std::unordered_map<jlong, std::shared_ptr<ChromaprintSession>> sessions;
std::atomic<jlong> nextHandle{1};

std::shared_ptr<ChromaprintSession> sessionFromHandle(jlong handle) {
    if (handle <= 0) {
        return nullptr;
    }
    std::lock_guard<std::mutex> lock(sessionsMutex);
    const auto iterator = sessions.find(handle);
    return iterator == sessions.end() ? nullptr : iterator->second;
}

constexpr jint kMaxSampleRate = 768000;
constexpr jint kMaxChannels = 32;
constexpr jint kMaxSamplesPerFeed = 16 * 1024 * 1024;
constexpr size_t kMaxFingerprintBytes = 1024 * 1024;

}  // namespace

extern "C" JNIEXPORT jlong JNICALL
Java_elovaire_music_droidbeauty_app_data_tags_matching_NativeChromaprintBridge_nativeCreate(
    JNIEnv*,
    jobject) {
    auto session = std::make_shared<ChromaprintSession>();
    if (session->context == nullptr) {
        return 0;
    }
    const jlong handle = nextHandle.fetch_add(1, std::memory_order_relaxed);
    if (handle <= 0) {
        return 0;
    }
    std::lock_guard<std::mutex> lock(sessionsMutex);
    sessions.emplace(handle, std::move(session));
    return handle;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_elovaire_music_droidbeauty_app_data_tags_matching_NativeChromaprintBridge_nativeStart(
    JNIEnv*,
    jobject,
    jlong handle,
    jint sampleRate,
    jint channels) {
    auto session = sessionFromHandle(handle);
    if (session == nullptr || session->context == nullptr ||
        sampleRate <= 0 || sampleRate > kMaxSampleRate || channels <= 0 || channels > kMaxChannels) {
        return JNI_FALSE;
    }
    std::lock_guard<std::mutex> lock(session->mutex);
    return chromaprint_start(session->context, sampleRate, channels) == 1 ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_elovaire_music_droidbeauty_app_data_tags_matching_NativeChromaprintBridge_nativeFeed(
    JNIEnv* env,
    jobject,
    jlong handle,
    jshortArray samples,
    jint length) {
    auto session = sessionFromHandle(handle);
    if (session == nullptr || session->context == nullptr || samples == nullptr ||
        length <= 0 || length > kMaxSamplesPerFeed) {
        return JNI_FALSE;
    }
    const jsize arrayLength = env->GetArrayLength(samples);
    if (length > arrayLength) {
        return JNI_FALSE;
    }
    jshort* values = env->GetShortArrayElements(samples, nullptr);
    if (values == nullptr) {
        return JNI_FALSE;
    }
    std::lock_guard<std::mutex> lock(session->mutex);
    const int result = chromaprint_feed(
        session->context,
        reinterpret_cast<const int16_t*>(values),
        length);
    env->ReleaseShortArrayElements(samples, values, JNI_ABORT);
    return result == 1 ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_elovaire_music_droidbeauty_app_data_tags_matching_NativeChromaprintBridge_nativeFinish(
    JNIEnv* env,
    jobject,
    jlong handle) {
    auto session = sessionFromHandle(handle);
    if (session == nullptr || session->context == nullptr) {
        return nullptr;
    }
    std::lock_guard<std::mutex> lock(session->mutex);
    if (chromaprint_finish(session->context) != 1) {
        return nullptr;
    }
    char* fingerprint = nullptr;
    if (chromaprint_get_fingerprint(session->context, &fingerprint) != 1 || fingerprint == nullptr) {
        return nullptr;
    }
    if (std::strlen(fingerprint) > kMaxFingerprintBytes) {
        chromaprint_dealloc(fingerprint);
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
    if (handle <= 0) {
        return;
    }
    std::lock_guard<std::mutex> lock(sessionsMutex);
    sessions.erase(handle);
}
