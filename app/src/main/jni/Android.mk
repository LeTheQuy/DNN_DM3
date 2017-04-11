LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#opencv
OPENCVROOT:= /Users/QUYLE/opencv/platforms/install
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES := com_gotit_quyle_dnn_dm3_NativeClass.cpp
LOCAL_LDLIBS += -llog
LOCAL_MODULE := MyLib

include $(BUILD_SHARED_LIBRARY)