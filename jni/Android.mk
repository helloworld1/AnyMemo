LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := native_db_helper
LOCAL_SRC_FILES := native_db_helper.c sqlite3.c
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
