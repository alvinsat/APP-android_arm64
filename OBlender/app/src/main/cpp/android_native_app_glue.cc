/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "android_native_app_glue.h"

#include <jni.h>

#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <locale>
#include <codecvt>
#include <android/log.h>

#include "creator/creator.h"

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "threaded_app", __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "threaded_app", __VA_ARGS__))

/* For debug builds, always enable the debug traces in this library */
#ifndef NDEBUG
#  define LOGV(...)  ((void)__android_log_print(ANDROID_LOG_VERBOSE, "threaded_app", __VA_ARGS__))
#else
#  define LOGV(...)  ((void)0)
#endif

#include <string>


std::string jstring2string(JNIEnv *env, jstring jStr) {
    if (!jStr)
        return "";

    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes,
                                                                       env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte *pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    std::string ret = std::string((char *) pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

static void free_saved_state(struct android_app *android_app) {
    pthread_mutex_lock(&android_app->mutex);
    if (android_app->savedState != nullptr) {
        free(android_app->savedState);
        android_app->savedState = nullptr;
        android_app->savedStateSize = 0;
    }
    pthread_mutex_unlock(&android_app->mutex);
}

int8_t android_app_read_cmd(struct android_app *android_app) {
    int8_t cmd;
    if (read(android_app->msgread, &cmd, sizeof(cmd)) != sizeof(cmd)) {
        LOGI("OBL No data on command pipe!");
        return -1;
    }
    if (cmd == APP_CMD_SAVE_STATE) free_saved_state(android_app);
    return cmd;
}

static void print_cur_config(struct android_app *android_app) {
    char lang[2], country[2];
    AConfiguration_getLanguage(android_app->config, lang);
    AConfiguration_getCountry(android_app->config, country);

    LOGI("OBL Config: mcc=%d mnc=%d lang=%c%c cnt=%c%c orien=%d touch=%d dens=%d "
         "keys=%d nav=%d keysHid=%d navHid=%d sdk=%d size=%d long=%d "
         "modetype=%d modenight=%d",
         AConfiguration_getMcc(android_app->config),
         AConfiguration_getMnc(android_app->config),
         lang[0], lang[1], country[0], country[1],
         AConfiguration_getOrientation(android_app->config),
         AConfiguration_getTouchscreen(android_app->config),
         AConfiguration_getDensity(android_app->config),
         AConfiguration_getKeyboard(android_app->config),
         AConfiguration_getNavigation(android_app->config),
         AConfiguration_getKeysHidden(android_app->config),
         AConfiguration_getNavHidden(android_app->config),
         AConfiguration_getSdkVersion(android_app->config),
         AConfiguration_getScreenSize(android_app->config),
         AConfiguration_getScreenLong(android_app->config),
         AConfiguration_getUiModeType(android_app->config),
         AConfiguration_getUiModeNight(android_app->config));
    AConfiguration_setNavHidden(android_app->config, 1);
}

void android_app_pre_exec_cmd(struct android_app *android_app, int8_t cmd) {
    switch (cmd) {
        case APP_CMD_INPUT_CHANGED:
            LOGI("OBL APP_CMD_INPUT_CHANGED");
            pthread_mutex_lock(&android_app->mutex);
            if (android_app->inputQueue != nullptr) {
                AInputQueue_detachLooper(android_app->inputQueue);
            }
            android_app->inputQueue = android_app->pendingInputQueue;
            if (android_app->inputQueue != nullptr) {
                LOGI("OBL Attaching input queue to looper");
                AInputQueue_attachLooper(android_app->inputQueue,
                                         android_app->looper, LOOPER_ID_INPUT, nullptr,
                                         &android_app->inputPollSource);
            }
            pthread_cond_broadcast(&android_app->cond);
            pthread_mutex_unlock(&android_app->mutex);
            break;

        case APP_CMD_INIT_WINDOW:
            LOGI("OBL APP_CMD_INIT_WINDOW");
            pthread_mutex_lock(&android_app->mutex);
            android_app->window = android_app->pendingWindow;
            pthread_cond_broadcast(&android_app->cond);
            pthread_mutex_unlock(&android_app->mutex);
            break;

        case APP_CMD_TERM_WINDOW:
            LOGI("OBL APP_CMD_TERM_WINDOW");
            pthread_cond_broadcast(&android_app->cond);
            break;

        case APP_CMD_RESUME:
        case APP_CMD_START:
        case APP_CMD_PAUSE:
        case APP_CMD_STOP:
            LOGI("OBL activityState=%d", cmd);
            pthread_mutex_lock(&android_app->mutex);
            android_app->activityState = cmd;
            pthread_cond_broadcast(&android_app->cond);
            pthread_mutex_unlock(&android_app->mutex);
            break;

        case APP_CMD_CONFIG_CHANGED:
            LOGI("OBL APP_CMD_CONFIG_CHANGED");
            AConfiguration_fromAssetManager(android_app->config,
                                            android_app->activity->assetManager);
            print_cur_config(android_app);
            break;

        case APP_CMD_DESTROY:
            LOGI("OBL APP_CMD_DESTROY");
            android_app->destroyRequested = 1;
            break;
    }
}

void android_app_post_exec_cmd(struct android_app *android_app, int8_t cmd) {
    switch (cmd) {
        case APP_CMD_TERM_WINDOW:
            LOGI("OBL APP_CMD_TERM_WINDOW");
            pthread_mutex_lock(&android_app->mutex);
            android_app->window = nullptr;
            pthread_cond_broadcast(&android_app->cond);
            pthread_mutex_unlock(&android_app->mutex);
            break;

        case APP_CMD_SAVE_STATE:
            LOGI("OBL APP_CMD_SAVE_STATE");
            pthread_mutex_lock(&android_app->mutex);
            android_app->stateSaved = 1;
            pthread_cond_broadcast(&android_app->cond);
            pthread_mutex_unlock(&android_app->mutex);
            break;

        case APP_CMD_RESUME:
            free_saved_state(android_app);
            break;
    }
}

void app_dummy() {
}

static void android_app_destroy(struct android_app *android_app) {
    LOGI("OBL android_app_destroy!");
    free_saved_state(android_app);
    pthread_mutex_lock(&android_app->mutex);
    if (android_app->inputQueue != nullptr) {
        AInputQueue_detachLooper(android_app->inputQueue);
    }
    AConfiguration_delete(android_app->config);
    android_app->destroyed = 1;
    pthread_cond_broadcast(&android_app->cond);
    pthread_mutex_unlock(&android_app->mutex);
    // Can't touch android_app object after this.
}

static void process_input(struct android_app *app, struct android_poll_source *source) {
    AInputEvent *event = nullptr;
    while (AInputQueue_getEvent(app->inputQueue, &event) >= 0) {
        int inputEventType = AInputEvent_getType(event);
        int32_t action=AKeyEvent_getAction(event);
        int32_t flags=AKeyEvent_getFlags(event);
        int32_t keyCode=AKeyEvent_getKeyCode(event);
        int32_t scanCode=AKeyEvent_getScanCode(event);
        int32_t metaState=AKeyEvent_getMetaState(event);// AMETA_CTRL_ON
        int32_t repeatCount=AKeyEvent_getRepeatCount(event);
        int32_t downTime=AKeyEvent_getDownTime(event);
        LOGI("OBL process_input  ------ 1 ------ %d %d %d %d %d %d %d %d",
                inputEventType,action,flags,keyCode,scanCode,metaState,repeatCount,downTime);

        // Hardware volume keys are not handled by the engine
        //  修改 将 声音按键 消息传递到底层 ，由底层处理，但底层返回 0 这样才能在 声音按键点击时 打开声音调节响应页面
//        if (keyCode == AKEYCODE_VOLUME_UP || keyCode == AKEYCODE_VOLUME_DOWN || keyCode=AKEYCODE_POWER)
//            return;
        /*
         *
         *   AINPUT_EVENT_TYPE_KEY = 1,
              AINPUT_EVENT_TYPE_MOTION = 2, https://developer.android.com/reference/android/view/MotionEvent
              AINPUT_EVENT_TYPE_FOCUS = 3,
              AINPUT_EVENT_TYPE_CAPTURE = 4,
              AINPUT_EVENT_TYPE_DRAG = 5,
              AINPUT_EVENT_TYPE_TOUCH_MODE = 6
         *
         *
         */
//        LOGI("OBL New input event: type=%d", AInputEvent_getType(event));
        if (AInputQueue_preDispatchEvent(app->inputQueue, event)) {
            continue;
        }
        LOGI("OBL process_input  ------ 2 ------ ");
        int32_t handled = 0;
        if (app->onInputEvent != nullptr) handled = app->onInputEvent(app, event);
        AInputQueue_finishEvent(app->inputQueue, event, handled);
    }
}

static void process_cmd(struct android_app *app, struct android_poll_source *source) {
    int8_t cmd = android_app_read_cmd(app);
    android_app_pre_exec_cmd(app, cmd);
    if (app->onAppCmd != nullptr) app->onAppCmd(app, cmd);
    android_app_post_exec_cmd(app, cmd);
}

void showWindow(struct android_app *app,int32_t left,
                int32_t top,
                uint32_t width,
                uint32_t height,
                int shape_type,
                std::string strInfo);
std::wstring getClipboard(bool selection);
void putClipboard(wchar_t *data,bool selection);
bool GetAsyncKeyState(int type);
void SetValue(int type,int value);
void setCursorPosition(int32_t x, int32_t y);
struct android_app *android_app;
static void *android_app_entry(void *param) {
    android_app = (struct android_app *) param;

    android_app->showWindow = showWindow;
    android_app->putClipboard = putClipboard;
    android_app->getClipboard = getClipboard;
    android_app->GetAsyncKeyState=GetAsyncKeyState;
    android_app->setValue=SetValue;
    android_app->setCursorPosition=setCursorPosition;

    android_app->config = AConfiguration_new();
    AConfiguration_fromAssetManager(android_app->config, android_app->activity->assetManager);

    print_cur_config(android_app);

    android_app->cmdPollSource.id = LOOPER_ID_MAIN;
    android_app->cmdPollSource.app = android_app;
    android_app->cmdPollSource.process = process_cmd;
    android_app->inputPollSource.id = LOOPER_ID_INPUT;
    android_app->inputPollSource.app = android_app;
    android_app->inputPollSource.process = process_input;

    ALooper *looper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);
    ALooper_addFd(looper, android_app->msgread, LOOPER_ID_MAIN, ALOOPER_EVENT_INPUT, nullptr,
                  &android_app->cmdPollSource);
    android_app->looper = looper;

    pthread_mutex_lock(&android_app->mutex);
    android_app->running = 1;
    pthread_cond_broadcast(&android_app->cond);
    pthread_mutex_unlock(&android_app->mutex);

    android_main(android_app);

    android_app_destroy(android_app);
    return nullptr;
}

// --------------------------------------------------------------------
// Native activity interaction (called from main thread)
// --------------------------------------------------------------------

static struct android_app *android_app_create(ANativeActivity *activity,
                                              void *savedState, size_t savedStateSize) {
    struct android_app *android_app = static_cast<struct android_app *>(calloc(1,
                                                                               sizeof(struct android_app)));
    android_app->activity = activity;

    pthread_mutex_init(&android_app->mutex, nullptr);
    pthread_cond_init(&android_app->cond, nullptr);

    if (savedState != nullptr) {
        android_app->savedState = malloc(savedStateSize);
        android_app->savedStateSize = savedStateSize;
        memcpy(android_app->savedState, savedState, savedStateSize);
    }

    int msgpipe[2];
    if (pipe(msgpipe)) {
        LOGI("OBL could not create pipe: %s", strerror(errno));
        return nullptr;
    }
    android_app->msgread = msgpipe[0];
    android_app->msgwrite = msgpipe[1];

    pthread_attr_t attr;
    pthread_attr_init(&attr);
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
    //  从主线程开启一个新的渲染 交互线程
    pthread_create(&android_app->thread, &attr, android_app_entry, android_app);

    // Wait for thread to start.
    pthread_mutex_lock(&android_app->mutex);
    while (!android_app->running) {
        pthread_cond_wait(&android_app->cond, &android_app->mutex);
    }
    pthread_mutex_unlock(&android_app->mutex);

    return android_app;
}

static void android_app_write_cmd(struct android_app *android_app, int8_t cmd) {
    if (write(android_app->msgwrite, &cmd, sizeof(cmd)) != sizeof(cmd)) {
        LOGI("OBL Failure writing android_app cmd: %s", strerror(errno));
    }
}

static void android_app_set_input(struct android_app *android_app, AInputQueue *inputQueue) {
    pthread_mutex_lock(&android_app->mutex);
    android_app->pendingInputQueue = inputQueue;
    android_app_write_cmd(android_app, APP_CMD_INPUT_CHANGED);
    while (android_app->inputQueue != android_app->pendingInputQueue) {
        pthread_cond_wait(&android_app->cond, &android_app->mutex);
    }
    pthread_mutex_unlock(&android_app->mutex);
}

static void android_app_set_window(struct android_app *android_app, ANativeWindow *window) {
    pthread_mutex_lock(&android_app->mutex);
    if (android_app->pendingWindow != nullptr) {
        android_app_write_cmd(android_app, APP_CMD_TERM_WINDOW);
    }
    android_app->pendingWindow = window;
    if (window != nullptr) {
        android_app_write_cmd(android_app, APP_CMD_INIT_WINDOW);
    }
    while (android_app->window != android_app->pendingWindow) {
        pthread_cond_wait(&android_app->cond, &android_app->mutex);
    }
    pthread_mutex_unlock(&android_app->mutex);
}

static void android_app_set_activity_state(struct android_app *android_app, int8_t cmd) {
    pthread_mutex_lock(&android_app->mutex);
    android_app_write_cmd(android_app, cmd);
    while (android_app->activityState != cmd) {
        pthread_cond_wait(&android_app->cond, &android_app->mutex);
    }
    pthread_mutex_unlock(&android_app->mutex);
}

static void android_app_free(struct android_app *android_app) {
    pthread_mutex_lock(&android_app->mutex);
    android_app_write_cmd(android_app, APP_CMD_DESTROY);
    while (!android_app->destroyed) {
        pthread_cond_wait(&android_app->cond, &android_app->mutex);
    }
    pthread_mutex_unlock(&android_app->mutex);

    close(android_app->msgread);
    close(android_app->msgwrite);
    pthread_cond_destroy(&android_app->cond);
    pthread_mutex_destroy(&android_app->mutex);
    free(android_app);
}

static struct android_app *ToApp(ANativeActivity *activity) {
    return (struct android_app *) activity->instance;
}

static void onDestroy(ANativeActivity *activity) {
    LOGI("OBL Destroy: %p", activity);
    android_app_free(ToApp(activity));
}

static void onStart(ANativeActivity *activity) {
    LOGI("OBL Start: %p", activity);
    android_app_set_activity_state(ToApp(activity), APP_CMD_START);
}

static void onResume(ANativeActivity *activity) {
    LOGI("OBL Resume: %p", activity);
    android_app_set_activity_state(ToApp(activity), APP_CMD_RESUME);
}

static void *onSaveInstanceState(ANativeActivity *activity, size_t *outLen) {
    LOGI("OBL SaveInstanceState: %p", activity);

    struct android_app *android_app = ToApp(activity);
    void *savedState = nullptr;
    pthread_mutex_lock(&android_app->mutex);
    android_app->stateSaved = 0;
    android_app_write_cmd(android_app, APP_CMD_SAVE_STATE);
    while (!android_app->stateSaved) {
        pthread_cond_wait(&android_app->cond, &android_app->mutex);
    }

    if (android_app->savedState != nullptr) {
        savedState = android_app->savedState;
        *outLen = android_app->savedStateSize;
        android_app->savedState = nullptr;
        android_app->savedStateSize = 0;
    }

    pthread_mutex_unlock(&android_app->mutex);

    return savedState;
}

static void onPause(ANativeActivity *activity) {
    LOGI("OBL Pause: %p", activity);
    android_app_set_activity_state(ToApp(activity), APP_CMD_PAUSE);
}

static void onStop(ANativeActivity *activity) {
    LOGI("OBL Stop: %p", activity);
    android_app_set_activity_state(ToApp(activity), APP_CMD_STOP);
}

static void onConfigurationChanged(ANativeActivity *activity) {
    LOGI("OBL ConfigurationChanged: %p", activity);
    android_app_write_cmd(ToApp(activity), APP_CMD_CONFIG_CHANGED);
}

static void onContentRectChanged(ANativeActivity *activity, const ARect *r) {
    LOGI("OBL ContentRectChanged: l=%d,t=%d,r=%d,b=%d", r->left, r->top, r->right, r->bottom);
    struct android_app *android_app = ToApp(activity);
    pthread_mutex_lock(&android_app->mutex);
    android_app->contentRect = *r;
    pthread_mutex_unlock(&android_app->mutex);
    android_app_write_cmd(ToApp(activity), APP_CMD_CONTENT_RECT_CHANGED);
}

static void onLowMemory(ANativeActivity *activity) {
    LOGI("OBL LowMemory: %p", activity);
    android_app_write_cmd(ToApp(activity), APP_CMD_LOW_MEMORY);
}

static void onWindowFocusChanged(ANativeActivity *activity, int focused) {
    LOGI("OBL WindowFocusChanged: %p -- %d", activity, focused);
    android_app_write_cmd(ToApp(activity), focused ? APP_CMD_GAINED_FOCUS : APP_CMD_LOST_FOCUS);
}

static void onNativeWindowCreated(ANativeActivity *activity, ANativeWindow *window) {
    LOGI("OBL NativeWindowCreated: %p -- %p", activity, window);
    android_app_set_window(ToApp(activity), window);
}

static void onNativeWindowDestroyed(ANativeActivity *activity, ANativeWindow *window) {
    LOGI("OBL NativeWindowDestroyed: %p -- %p", activity, window);
    android_app_set_window(ToApp(activity), nullptr);
}

static void onNativeWindowRedrawNeeded(ANativeActivity *activity, ANativeWindow *window) {
    LOGI("OBL NativeWindowRedrawNeeded: %p -- %p", activity, window);
    android_app_write_cmd(ToApp(activity), APP_CMD_WINDOW_REDRAW_NEEDED);
}

static void onNativeWindowResized(ANativeActivity *activity, ANativeWindow *window) {
    LOGI("OBL NativeWindowResized: %p -- %p", activity, window);
    android_app_write_cmd(ToApp(activity), APP_CMD_WINDOW_RESIZED);
}

static void onInputQueueCreated(ANativeActivity *activity, AInputQueue *queue) {
    LOGI("OBL InputQueueCreated: %p -- %p", activity, queue);
    android_app_set_input(ToApp(activity), queue);
}

static void onInputQueueDestroyed(ANativeActivity *activity, AInputQueue *queue) {
    LOGI("OBL InputQueueDestroyed: %p -- %p", activity, queue);
    android_app_set_input(ToApp(activity), nullptr);
}

#include <android/native_window_jni.h>

jobject javaCallBackObj;
jmethodID javaCallBackmethod;
jmethodID javaCallBackGetState;
jmethodID javaCallBackSetValue;
jmethodID javaMethodGetClipboard;
jmethodID javaMethodPutClipboard;
jmethodID javaMethodSetCursorPosition;
JavaVM *g_vm;
ANativeWindow* aNativeWindow;

JNIEnv *AttachCurrentThreadIfNeeded() {
    JNIEnv *env;
    jint result = g_vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
    if (result == JNI_EDETACHED) {
        g_vm->AttachCurrentThread(&env, nullptr);
    }
    return env;
}

bool GetAsyncKeyState(int type){
    JNIEnv *jenv = AttachCurrentThreadIfNeeded();
    int ret=jenv->CallIntMethod(javaCallBackObj, javaCallBackGetState,type);
    g_vm->DetachCurrentThread();
    return ret!=0;
}

void SetValue(int type,int value){
    JNIEnv *jenv = AttachCurrentThreadIfNeeded();

//    app->windowWindow=ANativeWindow_fromSurface(jenv,NULL);
    jenv->CallVoidMethod(javaCallBackObj, javaCallBackSetValue,type,value);

    g_vm->DetachCurrentThread();
}

void setCursorPosition(int32_t x, int32_t y){
    JNIEnv *jenv = AttachCurrentThreadIfNeeded();

//    app->windowWindow=ANativeWindow_fromSurface(jenv,NULL);
    jenv->CallVoidMethod(javaCallBackObj, javaMethodSetCursorPosition,(long)x,(long)y);

    g_vm->DetachCurrentThread();
}


std::wstring getClipboard(bool selection){
    JNIEnv *jenv = AttachCurrentThreadIfNeeded();

    jstring javaResult=(jstring )jenv->CallObjectMethod(javaCallBackObj,javaMethodGetClipboard,selection);

    std::string strValue=jstring2string(jenv,javaResult);

    g_vm->DetachCurrentThread();

    std::wstring_convert<std::codecvt_utf8<wchar_t>> converter;  // 定义UTF-8转换器
    return converter.from_bytes(strValue);
}

void putClipboard(wchar_t *data,bool selection){
    JNIEnv *jenv = AttachCurrentThreadIfNeeded();

    std::wstring_convert<std::codecvt_utf8<wchar_t>> converter;  // UTF-8 转换器
    std::string strValue= converter.to_bytes(data);
    jstring jstringValue=jenv->NewStringUTF(strValue.c_str());

    jenv->CallVoidMethod(javaCallBackObj,javaMethodPutClipboard,jstringValue,selection);

    jenv->DeleteLocalRef(jstringValue);

    g_vm->DetachCurrentThread();
}

void showWindow(struct android_app *app,int32_t left,
                int32_t top,
                uint32_t width,
                uint32_t height,
                int shape_type,
                std::string strInfo) {
    JNIEnv *jenv = AttachCurrentThreadIfNeeded();

    jstring aJStr = jenv->NewStringUTF(strInfo.c_str());

//    app->windowWindow=ANativeWindow_fromSurface(jenv,NULL);
    jenv->CallVoidMethod(javaCallBackObj, javaCallBackmethod,(int)left,(int)top,(int)width,(int)height,shape_type,
                         aJStr);
    jenv->DeleteLocalRef(aJStr);

    g_vm->DetachCurrentThread();
}

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_vm = vm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNICALL Java_com_epai_oblender_OBLNativeActivity_initial(
        JNIEnv *env,
        jobject obj,
        jstring stringparameter,
        jstring stringPython) {
    javaCallBackObj = env->NewGlobalRef(obj);
    jclass aJClass = env->FindClass("com/epai/oblender/OBLNativeActivity");
    javaCallBackmethod = env->GetMethodID(aJClass, "showWindow", "(IIIIILjava/lang/String;)V");
    javaCallBackGetState=env->GetMethodID(aJClass,"GetAsyncKeyState", "(I)I");
    javaCallBackSetValue=env->GetMethodID(aJClass,"SetValue", "(II)V");
    javaMethodGetClipboard=env->GetMethodID(aJClass,"getClipboard", "(Z)Ljava/lang/String;");
    javaMethodPutClipboard=env->GetMethodID(aJClass,"putClipboard", "(Ljava/lang/String;Z)V");
    javaMethodSetCursorPosition=env->GetMethodID(aJClass,"SetCursorPosition", "(JJ)V");
    std::string strHomePathTemp=jstring2string(env, stringparameter);
    std::string strConfigPathTemp=jstring2string(env,stringPython);

    strcat(strHomePath,strHomePathTemp.c_str());
    strcat(strConfigPath,strConfigPathTemp.c_str());
}
extern "C" JNIEXPORT void JNICALL Java_com_epai_oblender_OBLNativeActivity_updateSurface(
        JNIEnv *env,
        jobject obj,
        jobject surface) {
    aNativeWindow=ANativeWindow_fromSurface(env, surface);
    android_app->windowWindow=aNativeWindow;
}
extern "C" JNIEXPORT void JNICALL Java_com_epai_oblender_OBLNativeActivity_updateSurfaceDestroyed(
        JNIEnv *env,
        jobject obj,
        jobject surface) {
    android_app->destroyedWindowWindow=true;
}

#include <vector>
#include <string>
#include <sstream>
std::vector<std::string> split(const std::string &text, char separator) {
    std::vector<std::string> tokens;
    std::string token;
    std::istringstream tokenStream(text);
    while (std::getline(tokenStream, token, separator)) {
        tokens.push_back(token);
    }
    return tokens;
}

extern "C" JNIEXPORT void JNICALL Java_com_epai_oblender_OBLNativeActivity_oblSetValue(
        JNIEnv *env,
        jobject obj,
        jstring stringparameter) {
    std::string strsetting = jstring2string(env, stringparameter);
    std::vector<std::string> values=split(strsetting,',');
    std::vector<int> valuesInt;
    valuesInt.reserve(values.size());
valuesInt.reserve(values.size());
    for(const auto& value:values){
        valuesInt.push_back(atoi(value.c_str()));
    }
    if(!valuesInt.empty()){
        oblSetValue(&(valuesInt[0]),valuesInt.size());
    }
}

extern "C" JNIEXPORT void JNICALL Java_com_epai_oblender_OBLNativeActivity_oblSetValueOn(
        JNIEnv *env,
        jobject obj,
        jstring stringparameter) {
    std::string strsetting = jstring2string(env, stringparameter);
    std::vector<std::string> values=split(strsetting,',');
    std::vector<int> valuesInt;
    valuesInt.reserve(values.size());
    valuesInt.reserve(values.size());
    for(const auto& value:values){
        valuesInt.push_back(atoi(value.c_str()));
    }
    if(!valuesInt.empty()){
        oblSetValueOn(&(valuesInt[0]),valuesInt.size());
    }
}

extern "C" JNIEXPORT void JNICALL Java_com_epai_oblender_OBLNativeActivity_oblSetValueOff(
        JNIEnv *env,
        jobject obj,
        jstring stringparameter) {
    std::string strsetting = jstring2string(env, stringparameter);
    std::vector<std::string> values=split(strsetting,',');
    std::vector<int> valuesInt;
    valuesInt.reserve(values.size());
    valuesInt.reserve(values.size());
    for(const auto& value:values){
        valuesInt.push_back(atoi(value.c_str()));
    }
    if(!valuesInt.empty()){
        oblSetValueOff(&(valuesInt[0]),valuesInt.size());
    }
}

extern "C" JNIEXPORT jstring JNICALL Java_com_epai_oblender_OBLNativeActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {

    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


//  原生Activity入口函数
JNIEXPORT
void ANativeActivity_onCreate(ANativeActivity *activity, void *savedState, size_t savedStateSize) {
    LOGI("OBL Creating: %p", activity);

    activity->callbacks->onConfigurationChanged = onConfigurationChanged;
    activity->callbacks->onContentRectChanged = onContentRectChanged;
    activity->callbacks->onDestroy = onDestroy;
    activity->callbacks->onInputQueueCreated = onInputQueueCreated;
    activity->callbacks->onInputQueueDestroyed = onInputQueueDestroyed;
    activity->callbacks->onLowMemory = onLowMemory;
    activity->callbacks->onNativeWindowCreated = onNativeWindowCreated;
    activity->callbacks->onNativeWindowDestroyed = onNativeWindowDestroyed;
    activity->callbacks->onNativeWindowRedrawNeeded = onNativeWindowRedrawNeeded;
    activity->callbacks->onNativeWindowResized = onNativeWindowResized;
    activity->callbacks->onPause = onPause;
    activity->callbacks->onResume = onResume;
    activity->callbacks->onSaveInstanceState = onSaveInstanceState;
    activity->callbacks->onStart = onStart;
    activity->callbacks->onStop = onStop;
    activity->callbacks->onWindowFocusChanged = onWindowFocusChanged;

    activity->instance = android_app_create(activity, savedState, savedStateSize);
}
