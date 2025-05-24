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
 *
 */

//BEGIN_INCLUDE(all)
#include <initializer_list>
#include <memory>
#include <cstdlib>
#include <cstring>
#include <jni.h>
#include <cerrno>
#include <cassert>
#include <string>
#include <EGL/egl.h>
#include <GLES3/gl3.h>

#include <android/sensor.h>
#include <android/log.h>
#include "android_native_app_glue.h"

#include <BLI_blenlib.h>
#include "creator/creator.h"

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "LearnOpenGLES", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "LearnOpenGLES", __VA_ARGS__))

struct appUserData{
    void*pContext=nullptr;
};

bool isInitial= false;

static int engine_init_display_reinit(struct android_app*app){
    mainBlenderInitial_reinit(((appUserData*)(app->userData))->pContext);
    return 0;
}
/**
 * Initialize an EGL context for the current display.
 */
static int engine_init_display(struct android_app *app) {
    // 重定向 stderr 到文件 无法实现重定向
    char logpath[256]={0};
    strcat(logpath,strHomePath);
    strcat(logpath,"error.log");
    if (freopen(logpath, "w", stderr) == NULL) {
        LOGI("重定向err到文件失败");
        fprintf(stderr, "Error opening file\n");
    }else{
        LOGI("重定向err到文件成功");
    }
    auto*userData=new appUserData;
    app->userData=userData;\
    initialLib((void*)app);
    //  设置系统参数 注册表

    BLI_setenv("XDG_CACHE_HOME", strHomePath);
    BLI_setenv("HOME",           strHomePath);
    LOGI("路径 %s %s",strConfigPath,strHomePath);


    char BLENDER_SYSTEM_DATAFILES_Path[256]={0};
    strcat(BLENDER_SYSTEM_DATAFILES_Path,strConfigPath);
    strcat(BLENDER_SYSTEM_DATAFILES_Path,"4.0/config/datafiles");
    char BLENDER_SYSTEM_SCRIPTS_PATH[256]={0};
    strcat(BLENDER_SYSTEM_SCRIPTS_PATH,strConfigPath);
    strcat(BLENDER_SYSTEM_SCRIPTS_PATH,"scripts");
    char PYTHON_PATH[256]={0};
    strcat(PYTHON_PATH,strConfigPath);
    strcat(PYTHON_PATH,"python");
    char BLENDER_EXTERN_DRACO_LIBRARY_PATH[256]={0};
    strcat(BLENDER_EXTERN_DRACO_LIBRARY_PATH,strConfigPath);
    strcat(BLENDER_EXTERN_DRACO_LIBRARY_PATH,"python/lib/python3.11/site-packages/libextern_draco.so");
//    BLI_setenv("BLENDER_USER_DATAFILES",(strConfigPath+std::string("4.0/config/datafiles")).c_str());
    BLI_setenv("BLENDER_SYSTEM_DATAFILES",BLENDER_SYSTEM_DATAFILES_Path);
    BLI_setenv("BLENDER_SYSTEM_SCRIPTS",BLENDER_SYSTEM_SCRIPTS_PATH);
    BLI_setenv("PYTHONPATH",PYTHON_PATH);
    BLI_setenv("PYTHONHOME",PYTHON_PATH);
    BLI_setenv("BLENDER_EXTERN_DRACO_LIBRARY_PATH",BLENDER_EXTERN_DRACO_LIBRARY_PATH);
    char blenderpath[256]={0};
    strcat(blenderpath,strHomePath);
    strcat(blenderpath,"blender");
    const char *argv1 = blenderpath;
    const char *argv[2] = {argv1,"-d"};
    userData->pContext=mainBlenderInitial(2, (const char **) (argv));
    isInitial=true;
    return 0;
}

/**
 * Just the current frame in the display.
 */
static void engine_draw_frame(struct android_app *app) {
    mainBlenderLoop(((appUserData*)(app->userData))->pContext);
}

/**
 * Process the next main command.
 */
static void engine_handle_cmd(struct android_app *app, int32_t cmd) {
    switch (cmd) {
        case APP_CMD_SAVE_STATE:
            // The system has asked us to save our current state.  Do so.
            break;
        case APP_CMD_INIT_WINDOW:
            // The window is being shown, get it ready.
            if (!isInitial){
                engine_init_display(app);
            }else{
                engine_init_display_reinit(app);
            }
            break;
        case APP_CMD_TERM_WINDOW:
            // The window is being hidden or closed, clean it up.
            break;
        case APP_CMD_GAINED_FOCUS:
            // When our app gains focus, we start monitoring the accelerometer.
            break;
        case APP_CMD_LOST_FOCUS:
            // When our app loses focus, we stop monitoring the accelerometer.
            // This is to avoid consuming battery while not being used.
            // Also stop animating.
            break;
    }
}


//  独立线程
//  事件循环 输入处理 渲染
/**
 * This is the main entry point of a native application that is using
 * android_native_app_glue.  It runs in its own thread, with its own
 * event loop for receiving input events and doing other things.
 */
void android_main(struct android_app *state) {
    state->onAppCmd = engine_handle_cmd;

    // Prepare to monitor accelerometer

    if (state->savedState != nullptr) {
        // We are starting with a previous saved state; restore from it.

    }
    isInitial= false;
    // loop waiting for stuff to do.

    while (true) {
        // Read all pending events.
        if(isInitial){
            engine_draw_frame(state);
        }else{
            int ident;
            int events;
            struct android_poll_source *source;

            // we loop until all events are read, then continue
            // to draw the next frame of animation.
            while ((ident = ALooper_pollAll(0, nullptr, &events,
                                            (void **) &source)) >= 0) {

                // Process this event.
                if (source != nullptr) {
                    source->process(state, source);
                }
                // If a sensor has data, process it now.
                if (ident == LOOPER_ID_USER) {

                }
                // Check if we are exiting.
                if (state->destroyRequested != 0) {
                    return;
                }
            }
        }
    }
}
//END_INCLUDE(all)