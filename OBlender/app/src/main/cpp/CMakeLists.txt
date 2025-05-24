# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("blender")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# Select C++17 as the standard for C++ projects.
set(CMAKE_CXX_STANDARD 17)
# If C++17 is not available, downgrading to an earlier standard is NOT OK.
set(CMAKE_CXX_STANDARD_REQUIRED OFF)
# Do not enable compiler specific language extensions.
set(CMAKE_CXX_EXTENSIONS OFF)

add_definitions(-DWITH_PYTHON)
add_definitions(-DWITH_OPENEXR)
add_definitions(-DWITH_FLUID)
add_definitions(-DWITH_OPENVDB)
add_definitions(-DWITH_OPENVDB_BLOSC)
add_definitions(-DWITH_GREASE_PENCIL_V3)
add_definitions(-DWITH_ALEMBIC)
add_definitions(-DWITH_BULLET)
add_definitions(-DWITH_FREESTYLE)
add_definitions(-DWITH_FFMPEG)
add_definitions(-DWITH_OPENJPEG)
add_definitions(-DWITH_AUDASPACE)
add_definitions(-DWITH_XR_OPENXR)
add_definitions(-DWITH_CYCLES)
#add_definitions(-DWITH_INPUT_NDOF)
add_definitions(-DWITH_INTERNATIONAL)
add_definitions(-DWITH_GMP)
add_definitions(-DWITH_OPENIMAGEDENOISE)
add_definitions(-DWITH_BUILDINFO_HEADER)
#add_definitions(-DWITH_SDL_DYNLOAD)
add_definitions(-DWITH_USD)
add_definitions(-DWITH_LIBMV)
add_definitions(-DWITH_FFMPEG)
add_library( # Sets the name of the library.
        blender

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        creator/buildinfo.c
        creator/creator.cc
        creator/creator_args.cc
        creator/creator_signals.cc
        android_native_app_glue.cc
        native-lib.cpp
        java_godot_lib_jni.cpp
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

set(blenderLibDir    D:/FreeProjects/Blender/Blender/Blender-android_arm64/)
set(blenderLibLibDirRelease D:/FreeProjects/Blender/Blender/Utilities-android_arm64/MakeBlender/app/.cxx/cmake/release/arm64-v8a/lib/)
set(blenderLibLibDirDebug D:/FreeProjects/Blender/Blender/Utilities-android_arm64/MakeBlender/app/.cxx/cmake/debug/arm64-v8a/lib/)

target_include_directories(blender PUBLIC
        D:/FreeProjects/Blender/Blender/lib-android_arm64/boost/boost_1_71_0/
        D:/FreeProjects/Blender/Blender/lib-android_arm64/OpenVDB/include/
        D:/FreeProjects/Blender/Blender/lib-android_arm64/Tbb/include/tbb-master/include/
        D:/FreeProjects/Blender/Blender/Utilities-android_arm64/MakeBlender/app/.cxx/cmake/release/arm64-v8a/source/creator/
        ${blenderLibDir}/intern/
        ${blenderLibDir}/intern/clog/
        ${blenderLibDir}/intern/ghost/
        ${blenderLibDir}/intern/guardedalloc
        ${blenderLibDir}/intern/guardedalloc/intern
        ${blenderLibDir}/intern/cycles/blender
        ${blenderLibDir}/source/blender/bmesh
        ${blenderLibDir}/source/blender/blenlib
        ${blenderLibDir}/source/blender/blenkernel
        ${blenderLibDir}/source/blender/makesdna
        ${blenderLibDir}/source/blender/makesrna
        ${blenderLibDir}/source/blender/bmesh/intern
        ${blenderLibDir}/source/blender/editors/include
        ${blenderLibDir}/source/blender/editors/mesh/
        ${blenderLibDir}/source/blender/blenkernel/
        ${blenderLibDir}/source/blender/blenloader/
        ${blenderLibDir}/source/blender/imbuf/
        ${blenderLibDir}/source/blender/gpu/
        ${blenderLibDir}/source/blender/blentranslation/
        ${blenderLibDir}/source/blender/render/
        ${blenderLibDir}/source/blender/windowmanager/
        ${blenderLibDir}/source/blender/depsgraph/
        ${blenderLibDir}/extern/audaspace/bindings/C/
        ${blenderLibDir}/intern/guardedalloc/
        ${blenderLibDir}/source/blender/python/
        ${blenderLibDir}/source/blender/freestyle/
        ${blenderLibDir}/intern/libmv/
        ${blenderLibDir}/source/blender/io/usd/
        )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

SET(BlenderLibNames
        bf_modifiers
        bf_nodes_composite
        bf_editor_sculpt_paint
        bf_editor_transform
        bf_python
        bf_gpencil_modifiers_legacy
        bf_windowmanager
        bf_editor_space_view3d
        bf_editor_space_node
        bf_sequencer
        bf_editor_interface
        bf_shader_fx
        bf_editor_space_outliner
        bf_editor_screen
        bf_editor_space_spreadsheet
        bf_editor_uvedit
        bf_editor_util
        bf_editor_space_sequencer
        bf_editor_mesh
        bf_editor_object
        bf_editor_gpencil_legacy
        bf_editor_space_topbar
        bf_editor_space_info
        bf_editor_space_nla
        bf_editor_space_statusbar
        bf_editor_space_graph
        bf_editor_space_image
        bf_editor_space_clip
        bf_editor_space_file
        bf_editor_animation
        bf_editor_armature
        bf_editor_asset
        bf_editor_curves
        bf_editor_physics
        bf_editor_space_action
        bf_editor_gizmo_library
        bf_editor_render
        bf_editor_space_buttons
        bf_editor_curve
        bf_editor_sound
        bf_rna
        bf_draw
        bf_editor_space_text
        bf_editor_space_userpref
        bf_editor_space_console
        bf_editor_space_script
        bf_usd
        bf_alembic
        bf_collada
        bf_render
        bf_editor_undo
        bf_editor_scene
        bf_editor_mask
        bf_editor_metaball
        bf_editor_lattice
        bf_editor_io
        bf_editor_geometry
        bf_compositor
        bf_blenkernel
        bf_nodes_geometry
        bf_freestyle
        bf_nodes_shader
        bf_intern_cycles
        bf_depsgraph
        bf_nodes_texture
        bf_nodes
        bf_blenloader
        bf_dna
        bf_gpu
        bf_python_mathutils
        bf_python_bmesh
        bf_python_gpu
        bf_python_ext
        bf_compositor_shaders
        bf_realtime_compositor
        bf_imbuf_openexr
        bf_imbuf_cineon
        makesdna
        bf_functions
        bf_avi
        bf_gpencil
        bf_ply
        bf_wavefront_obj
        bf_stl
        bf_io_common
        bf_nodes_function
        bf_geometry
        bf_imbuf
        bf_imbuf_openimageio
        bf_gpu_shaders
        bf_ikplugin
        bf_simulation
        bf_draw_shaders
        bf_blentranslation
        bf_blenfont
        bf_blenlib
        bf_bmesh
        bf_asset_system
        bf_editor_datafiles
        bf_editor_space_api
        bf_intern_opensubdiv
        bf_ocio_shaders
        extern_mantaflow
        extern_quadriflow
        extern_ceres
        audaspace-py
        audaspace
        extern_glog
        extern_gflags
        cycles_bvh
        cycles_scene
        cycles_integrator
        cycles_session
        cycles_device
        draco
        extern_lzma
        extern_hipew
        extern_cuew
        extern_clew
        extern_minilzo
        extern_bullet
        bf_intern_mantaflow
        bf_intern_quadriflow
        bf_intern_openvdb
        cycles_graph
        bf_intern_rigidbody
        extern_curve_fit_nd
        extern_wcwidth
        extern_rangetree
        bf_intern_locale
        bf_intern_libc_compat
        bf_intern_libmv
        cycles_kernel
        bf_intern_iksolver
        bf_intern_itasc
        bf_intern_eigen
        bf_intern_dualcon
        bf_intern_opencolorio
        bf_intern_audaspace
        bf_intern_sky
        bf_intern_memutil
        cycles_util
        cycles_subd
        bf_intern_clog
        bf_intern_ghost
        bf_intern_guardedalloc
        )

# bf_bmesh依赖bf_blenkernel
# bf_blenkernel依赖bf_bmesh
# 其它不同模块也或多或少相互依赖
set(BlenderLibs
        ${BlenderLibNames}
        )
foreach (blenderLib ${BlenderLibs})
    add_library(${blenderLib} STATIC IMPORTED)
    set_target_properties(${blenderLib} PROPERTIES IMPORTED_LOCATION
            ${blenderLibLibDirRelease}/lib${blenderLib}.a)
endforeach ()

target_link_libraries( # Specifies the target library.
        blender
        -Wl,--start-group
        ${BlenderLibNames}
        -Wl,--end-group
        )
# 静态库循环依赖 -Wl,--start-group -Wl,--end-group

set(ExternLibs
        boostlocale
        boostiostreams
        cpython
        crypto
        ssl
        expat
        freetype
        icuc
        Iex-3_0
        IlmThread-3_0
        Imath-3_0
        OpenColorIO
        OpenEXR-3_0
        OpenEXRUtil-3_0
        OpenImageIO
        OpenImageIO_Util
        osdCPU
        osdGPU
        sqlite
        zip
        zstd
        tbb
        tbbmalloc
        tbbmalloc_proxy
        GeneratedSaxParser
        xml
        OpenCOLLADASaxFrameworkLoader
        OpenCOLLADAFramework
        UTF
        OpenCOLLADABaseUtils
        OpenCOLLADAStreamWriter
        pcre
        ftoa
        MathMLSolver
        buffer
        zlib
        openvdb
        png16
        avcodec
        avdevice
        avfilter
        avformat
        avutil
        swresample
        swscale
        potrace
        embree4
        SDL2
        openal
        gmp
        gmpxx
        dnnl
        OpenImageDenoise
        OpenImageDenoise_device_cpu
        OpenImageDenoise_core
        hdTiny
        usd_ar
        usd_arch
        usd_cameraUtil
        usd_geomUtil
        usd_gf
        usd_hd
        usd_hdar
        usd_hdGp
        usd_hdsi
        usd_hf
        usd_hgi
        usd_hio
        usd_js
        usd_kind
        usd_ndr
        usd_pcp
        usd_plug
        usd_pxOsd
        usd_sdf
        usd_sdr
        usd_tf
        usd_trace
        usd_usd
        usd_usdGeom
        usd_usdHydra
        usd_usdImaging
        usd_usdLux
        usd_usdMedia
        usd_usdPhysics
        usd_usdProc
        usd_usdProcImaging
        usd_usdRender
        usd_usdRi
        usd_usdRiImaging
        usd_usdShade
        usd_usdSkel
        usd_usdSkelImaging
        usd_usdUI
        usd_usdUtils
        usd_usdVol
        usd_usdVolImaging
        usd_vt
        usd_work
        sdrGlslfx
        usdShaders
        Alembic
        fftw3
        openpgl
        boostpython
        )
foreach (externLib ${ExternLibs})
    add_library(${externLib} SHARED IMPORTED)
    set_target_properties(${externLib} PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/lib${externLib}.so)

    target_link_libraries( # Specifies the target library.
            blender
            ${externLib}
            )
endforeach ()


find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        blender

        # Links the target library to the log library
        # included in the NDK.
        GLESv3
        GLESv2
        EGL
        android
        jnigraphics
        D:/FreeProjects/Blender/Blender/lib-android_arm64/JPEG/lib/libjpeg.a
        D:/FreeProjects/Blender/Blender/lib-android_arm64/PugiXML/lib/libpugixml.a
        D:/FreeProjects/Blender/Blender/lib-android_arm64/Epoxy/lib/libepoxy.a
        ${log-lib}
        )