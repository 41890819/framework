#ifndef ANDROID_MUSICTHREAD_H
#define ANDROID_MUSICTHREAD_H

#include <stdint.h>
#include <sys/types.h>
#include <utils/threads.h>
#include <media/AudioSystem.h>
#include <media/mediaplayer.h>
class SkBitmap;

namespace android {
    class MusicThread : public Thread {
    public:
        MusicThread();
	void isShutdown(bool shutdown);
    protected:
        virtual ~MusicThread();

    private:
	bool mShutdown;
        bool threadLoop();
    };
}; // namespace android

#endif // ANDROID_MUSICTHREAD_H
