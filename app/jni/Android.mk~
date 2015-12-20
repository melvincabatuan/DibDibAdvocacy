LOCAL_PATH := $(call my-dir)

# Build library 1
include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED

include /home/cobalt/Android/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := DetectionBasedTracker.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH) 
LOCAL_LDLIBS     += -llog -ldl

LOCAL_MODULE     := detection_based_tracker

include $(BUILD_SHARED_LIBRARY)


# Build library 2
include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED

include /home/cobalt/Android/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := opencv_native_module.cpp CMT.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH) 
LOCAL_LDLIBS     += -llog -ldl

LOCAL_MODULE     := opencv_native_module

include $(BUILD_SHARED_LIBRARY)
