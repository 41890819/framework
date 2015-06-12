package com.android.internal.voicerecognizer;

import com.android.internal.voicerecognizer.RecognizeClient;

interface IVoiceRecognizer {
    boolean register(in RecognizeClient client);
    boolean unRegister(in RecognizeClient client);
    
    boolean start(in RecognizeClient client, boolean force);
    boolean stop(in RecognizeClient client);
    boolean stopImmediate(in RecognizeClient client);

    boolean setParameter(String key, String value);
    String getParameter(String key);

    void playTTS(String tts);
    void stopTTS();
}