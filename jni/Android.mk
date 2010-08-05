LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := sqlite3
LOCAL_SRC_FILES := shell.c  sqlite3.c

include $(BUILD_SHARED_LIBRARY)
