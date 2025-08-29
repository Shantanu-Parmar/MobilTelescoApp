
        message(WARNING "Using `OSDependentTargets.cmake` is deprecated: use `find_package(glslang)` to find glslang CMake targets.")

        if (NOT TARGET glslang::OSDependent)
            include("/mnt/c/Users/HP/AndroidStudioProjects/MobilTelesco2/app/src/main/cpp/ncnn-20240820-android-vulkan/x86_64/lib/cmake/glslang/glslang-targets.cmake")
        endif()

        add_library(OSDependent ALIAS glslang::OSDependent)
    