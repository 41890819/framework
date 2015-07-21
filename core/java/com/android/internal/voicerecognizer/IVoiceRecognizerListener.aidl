package com.android.internal.voicerecognizer;

interface IVoiceRecognizerListener {

    void onWakeup();
	  
    void onReadyForSpeaking();
    	
    boolean onResult(String result, float score);

    boolean onEnd(String error);

    void onUpdateVolume(int volume);
    
    boolean onCanceled();
    
    boolean onRecordingStop();

    void onPreempted();

    void onTTSPlayBegin(String tts);

    void onTTSPlayEnd(String tts);

    void onTTSPlayCanceled(String tts);
}