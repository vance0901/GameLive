# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION
        3.4.1)


MESSAGE($CMAKE_LIBRARY_OUTPUT_DIRECTORY)
message($LIBRARY_OUTPUT_PATH)
add_subdirectory(librtmp)
#okhttp.jar  libnative-lib.so
add_library(
        native-lib

        SHARED

        native-lib.cpp)
set(CMAKE_C_FLAGS  "${CMAKE_C_FLAGS} -L${CMAKE_SOURCE_DIR}")
set(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS}  -L${CMAKE_SOURCE_DIR}")

target_link_libraries(
        native-lib
        rtmp
        log)