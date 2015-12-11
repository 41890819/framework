LOCAL_PATH:= $(call my-dir)

# the library
# ============================================================
include $(CLEAR_VARS)

FILE := $(shell test -f $(LOCAL_PATH)/../../../device/ingenic/$(TARGET_PRODUCT)/config/PhoneWindowManager.java && echo yes)
ifeq ($(FILE),yes)
#$(warning "link PhoneWindowManager.java file $(TARGET_PRODUCT)")
$(shell ln -f $(LOCAL_PATH)/../../../device/ingenic/$(TARGET_PRODUCT)/config/PhoneWindowManager.java $(LOCAL_PATH)/src/com/android/internal/policy/impl/PhoneWindowManager.java)
endif


LOCAL_SRC_FILES := $(call all-java-files-under, src)
            
LOCAL_MODULE := android.policy

include $(BUILD_JAVA_LIBRARY)

# additionally, build unit tests in a separate .apk
include $(call all-makefiles-under,$(LOCAL_PATH))
