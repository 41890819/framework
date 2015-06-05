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
    }

    MusicThread::~MusicThread() {
	ALOGD("~MusicThread----");
    }
    void MusicThread::isShutdown(bool shutdown)
    {
	mShutdown = shutdown;
    }
    bool MusicThread::threadLoop() {
	MediaPlayer* mp = new MediaPlayer();
	if(mShutdown){
	    if(access(POWEROFF_FILE, R_OK) != 0){
		ALOGE("---read file=%s error",POWEROFF_FILE);
		return false;
	    }
	    if (mp->setDataSource(POWEROFF_FILE, NULL) != NO_ERROR)
		return false;
	}else{
	    if(access(POWERON_FILE, R_OK) != 0){
		ALOGE("---read file=%s error",POWERON_FILE);
		return false;
	    }
	    if (mp->setDataSource(POWERON_FILE, NULL) != NO_ERROR)
		return false;
	}

	ALOGD("---bootMusic");
	mp->setAudioStreamType(AUDIO_STREAM_ENFORCED_AUDIBLE);
	mp->prepare();
	audio_devices_t device = AudioSystem::getDevicesForStream(AUDIO_STREAM_ENFORCED_AUDIBLE); 
	AudioSystem::setStreamVolumeIndex(AUDIO_STREAM_ENFORCED_AUDIBLE,3,device);
	mp->seekTo(0);
	mp->start();
	return false;
    }
// ---------------------------------------------------------------------------

}
; // namespace android
