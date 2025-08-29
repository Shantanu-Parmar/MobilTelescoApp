#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <ncnn/net.h>
#include <ncnn/cpu.h>
#include <string>
#include <vector>
#include <fstream>

#define LOG_TAG "NanoDet"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static ncnn::Net net;
static bool is_initialized = false;
static std::vector<std::string> labels;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_mobiltelesco_MainActivity_00024Companion_initNanodet(JNIEnv *env, jobject /* this */, jstring paramPath, jstring binPath) {
    const char *param = env->GetStringUTFChars(paramPath, nullptr);
    const char *bin = env->GetStringUTFChars(binPath, nullptr);

    LOGD("Initializing NanoDet with param: %s, bin: %s", param, bin);

    // Load labels from labels.txt
    std::string labels_path = std::string(param).substr(0, std::string(param).find_last_of("/")) + "/labels.txt";
    std::ifstream file(labels_path);
    if (!file.is_open()) {
        LOGE("Failed to open labels.txt at: %s", labels_path.c_str());
        env->ReleaseStringUTFChars(paramPath, param);
        env->ReleaseStringUTFChars(binPath, bin);
        return JNI_FALSE;
    }
    std::string line;
    labels.clear();
    while (std::getline(file, line)) {
        if (!line.empty()) {
            labels.push_back(line);
        }
    }
    file.close();
    LOGD("Loaded %zu labels", labels.size());

    // Initialize NCNN
    net.opt.use_vulkan_compute = true;
    //net.opt.use_openmp = false; // Explicitly disable OpenMP
    ncnn::create_gpu_instance();
    int param_load_result = net.load_param(param);
    int model_load_result = net.load_model(bin);

    env->ReleaseStringUTFChars(paramPath, param);
    env->ReleaseStringUTFChars(binPath, bin);

    if (param_load_result != 0 || model_load_result != 0) {
        LOGE("Failed to load model: param=%d, model=%d", param_load_result, model_load_result);
        return JNI_FALSE;
    }

    is_initialized = true;
    LOGD("NanoDet initialized successfully");
    return JNI_TRUE;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_mobiltelesco_MainActivity_00024Companion_detectObjects(JNIEnv *env, jobject /* this */, jobject bitmap, jstring selectedLabel) {
    if (!is_initialized) {
        LOGE("NanoDet not initialized");
        return env->NewObjectArray(0, env->FindClass("java/lang/String"), nullptr);
    }

    const char *label = env->GetStringUTFChars(selectedLabel, nullptr);
    std::string selected_label_str(label);
    env->ReleaseStringUTFChars(selectedLabel, label);

    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        LOGE("Failed to get bitmap info");
        return env->NewObjectArray(0, env->FindClass("java/lang/String"), nullptr);
    }

    void *pixels;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        LOGE("Failed to lock bitmap pixels");
        return env->NewObjectArray(0, env->FindClass("java/lang/String"), nullptr);
    }

    // Convert bitmap to NCNN Mat
    ncnn::Mat in = ncnn::Mat::from_pixels_resize(
            (unsigned char*)pixels,
            ncnn::Mat::PIXEL_RGBA2BGR,
            info.width,
            info.height,
            320,
            320
    );

    AndroidBitmap_unlockPixels(env, bitmap);

    // Inference
    ncnn::Extractor ex = net.create_extractor();
    ex.set_num_threads(1); // Single-threaded to avoid OpenMP
    ex.input("input.1", in);

    ncnn::Mat out;
    ex.extract("output", out);

    // Process detections
    std::vector<std::string> results;
    for (int i = 0; i < out.h; ++i) {
        float score = out.row(i)[0];
        if (score > 0.5f) { // Threshold
            int class_id = static_cast<int>(out.row(i)[1]);
            if (class_id >= 0 && class_id < labels.size()) {
                std::string detected_label = labels[class_id];
                if (selected_label_str == "all" || selected_label_str == detected_label) {
                    float x1 = out.row(i)[2] * info.width;
                    float y1 = out.row(i)[3] * info.height;
                    float x2 = out.row(i)[4] * info.width;
                    float y2 = out.row(i)[5] * info.height;
                    char result[256];
                    snprintf(result, sizeof(result), "%s,%.2f,%.0f,%.0f,%.0f,%.0f",
                             detected_label.c_str(), score, x1, y1, x2, y2);
                    results.push_back(result);
                    LOGD("Detection: %s", result);
                }
            }
        }
    }

    // Convert results to Java string array
    jobjectArray resultArray = env->NewObjectArray(results.size(), env->FindClass("java/lang/String"), nullptr);
    for (size_t i = 0; i < results.size(); ++i) {
        env->SetObjectArrayElement(resultArray, i, env->NewStringUTF(results[i].c_str()));
    }

    return resultArray;
}