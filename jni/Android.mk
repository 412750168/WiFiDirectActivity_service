# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := libWifiDirect_Miracast
LOCAL_SRC_FILES := zzl_bestidear_wifidirect_miracast_SinkPlayer.cpp

LOCAL_C_INCLUDES:= \
        $(TOP)/frameworks/av/include/media/stagefright \
        $(TOP)/frameworks/av/media/libstagefright/wifi-display \
	$(TOP)/frameworks/native/include \
	$(TOP)/base/include \
        $(TOP)/frameworks/native/include/media/openmax \
        $(PV_INCLUDES) \
        $(JNI_H_INCLUDE) \
        $(call include-path-for, corecg graphics)

LOCAL_SHARED_LIBRARIES:= \
	libbinder \
        libandroid_runtime \
        libstagefright_wfd                          \
        libstagefright_foundation       \
	libstagefright \
        libutils                        \
        libnativehelper \
        libui \
        libgui \
        libskia \

LOCAL_MODULE_TAGS:= optional
include $(BUILD_SHARED_LIBRARY)
