package com.android.internal.voicerecognizer;

interface IVoiceRecognizerListener {

    void onWakeup();
	  
    void onReadyForSpeaking();
    	
    boolean onResult(String result, float score);

    boolean onEnd(String error);

    void onUpdateVolume(int volume);
    
    boolean onCanceled();
    
    boolean onRecordingStop();

    void onTTSPlayBegin();

    void onTTSPlayEnd();
}