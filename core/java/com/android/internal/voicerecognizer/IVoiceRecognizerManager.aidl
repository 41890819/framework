package com.android.internal.voicerecognizer;

import com.android.internal.voicerecognizer.RecognizeClient;
import com.android.internal.voicerecognizer.IVoiceRecognizer;

/** @hide */
interface IVoiceRecognizerManager
{
	void registerService(in IVoiceRecognizer service);

	void registerClient(in RecognizeClient client);
	void unRegisterClient(in RecognizeClient client);
	void startRecognize(in RecognizeClient client);
	void startRecognizeForce(in RecognizeClient client);
	void stopRecognize(in RecognizeClient client);
	void stopRecognizeImmediate(in RecognizeClient client);

	boolean setParameter(String key, String value);
    	String getParameter(String key);
}
