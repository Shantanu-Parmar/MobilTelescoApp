#cmake_minimum_required(VERSION 3.22.1)
#
#project("nanodet")
#
#set(ncnn_DIR "${CMAKE_SOURCE_DIR}/ncnn-20240820-android-vulkan/${ANDROID_ABI}/lib/cmake/ncnn")
#find_package(ncnn REQUIRED)
#
#add_library(nanodet SHARED nanodet.cpp)
#
#target_include_directories(nanodet PRIVATE
#        ${CMAKE_SOURCE_DIR}/ncnn-20240820-android-vulkan/${ANDROID_ABI}/include
#)
#
#target_compile_options(nanodet PRIVATE -DNCNN_OPENMP=OFF)
#
#target_link_libraries(nanodet
#        ncnn
#        jnigraphics
#        log
#        android
#)