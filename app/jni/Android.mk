LOCAL_PATH := $(call my-dir)

<<<<<<< HEAD
# Build library 1
=======
>>>>>>> 35416f1914e6ec10b7c14a90236881adfd7e4a99
include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED

include /home/cobalt/Android/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := DetectionBasedTracker.cpp
<<<<<<< HEAD
LOCAL_C_INCLUDES += $(LOCAL_PATH) 
=======
LOCAL_C_INCLUDES += $(LOCAL_PATH)
>>>>>>> 35416f1914e6ec10b7c14a90236881adfd7e4a99
LOCAL_LDLIBS     += -llog -ldl

LOCAL_MODULE     := detection_based_tracker

include $(BUILD_SHARED_LIBRARY)
<<<<<<< HEAD


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
=======
>>>>>>> 35416f1914e6ec10b7c14a90236881adfd7e4a99
