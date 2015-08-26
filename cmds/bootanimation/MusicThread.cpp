/*
 */

#define LOG_TAG "MusicThread"

#include <stdint.h>
#include <sys/types.h>
#include <math.h>
#include <fcntl.h>
#include <utils/misc.h>
#include <signal.h>

#include <utils/Log.h>
#include <utils/threads.h>
#include "MusicThread.h"

#define POWEROFF_FILE "/system/media/poweroff.mp3"
#define POWERON_FILE "/system/media/poweron.mp3"
namespace android {

// ---------------------------------------------------------------------------
    MusicThread::MusicThread()
    {
       mPlayer = new MediaPlayer();
    }

    MusicThread::~MusicThread() {
	ALOGD("~MusicThread----");
    }
    void MusicThread::isShutdown(bool shutdown)
    {
	mShutdown = shutdown;
    }
    bool MusicThread::threadLoop() {
	if(mShutdown){
	    if(access(POWEROFF_FILE, R_OK) != 0){
		ALOGE("---read file=%s error",POWEROFF_FILE);
		return false;
	    }
	    if (mPlayer->setDataSource(POWEROFF_FILE, NULL) != NO_ERROR)
		return false;
	}else{
	    if(access(POWERON_FILE, R_OK) != 0){
		ALOGE("---read file=%s error",POWERON_FILE);
		return false;
	    }
	    if (mPlayer->setDataSource(POWERON_FILE, NULL) != NO_ERROR)
		return false;
	}

	ALOGD("---bootMusic");
	mPlayer->setAudioStreamType(AUDIO_STREAM_ENFORCED_AUDIBLE);
	mPlayer->prepare();
	audio_devices_t device = AudioSystem::getDevicesForStream(AUDIO_STREAM_ENFORCED_AUDIBLE); 

	if(mShutdown)
	    AudioSystem::setStreamVolumeIndex(AUDIO_STREAM_ENFORCED_AUDIBLE,2,device);
	else
	    AudioSystem::setStreamVolumeIndex(AUDIO_STREAM_ENFORCED_AUDIBLE,1,device);

	mPlayer->seekTo(0);
	mPlayer->start();
	return false;
    }
// ---------------------------------------------------------------------------

}
; // namespace android
