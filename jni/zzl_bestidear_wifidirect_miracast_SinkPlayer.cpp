/*
 * com_example_android_wifidirect_SinkPlayer.cpp
 *
 *  Created on: 2014-6-10
 *      Author: think
 */

#include"jni.h"
#include<stdio.h>
#include"zzl_bestidear_wifidirect_miracast_SinkPlayer.h"
#include "sink/WifiDisplaySink.h"
#include "ANetworkSession.h"
#include <gui/ISurfaceTexture.h>
#include <gui/Surface.h>
#include "android_runtime/AndroidRuntime.h"
#include "android_runtime/android_view_Surface.h"
#include <binder/ProcessState.h>
#include <media/stagefright/DataSource.h>

extern "C" {

using namespace android;

sp < ALooper > looper;
sp < ANetworkSession > session;
sp < WifiDisplaySink > sink;
bool mstart = false;

JNIEXPORT void JNICALL Java_zzl_bestidear_wifidirect_miracast_SinkPlayer_setRtspSink(
		JNIEnv *env, jobject thiz, jstring host, jint port) {


	if (host == NULL)
		return;

	const char *tmp = env->GetStringUTFChars(host, NULL);
	if (tmp == NULL)
		return;

	AString hostStr(tmp);
	env->ReleaseStringUTFChars(host, tmp);
	tmp = NULL;

	mstart = true;

    	looper = new ALooper;
	session = new ANetworkSession;
	sink = new WifiDisplaySink(session);
	session->start();
	looper->setName("media.player");
	looper->registerHandler(sink);
	sink->start(hostStr.c_str(), port);
	looper->start(true);

}

JNIEXPORT void JNICALL Java_zzl_bestidear_wifidirect_miracast_SinkPlayer_stopRtspSink
  (JNIEnv *, jobject){

	if(mstart == true)
	{
		sink->stop();
		session->stop();
		looper->unregisterHandler(sink->id());
		mstart = false;
	}

}


}
