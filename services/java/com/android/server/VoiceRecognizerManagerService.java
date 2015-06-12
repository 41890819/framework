package com.android.server;

import com.android.internal.voicerecognizer.IVoiceRecognizerManager;
import com.android.internal.voicerecognizer.IVoiceRecognizer;
import com.android.internal.voicerecognizer.RecognizeClient;

import android.os.RemoteException;
import android.util.Log;

public class VoiceRecognizerManagerService extends IVoiceRecognizerManager.Stub
{
	static final String TAG = "VoiceRecognizerManagerService";

	private IVoiceRecognizer mVoiceRecognizer = null;

	private boolean mRecStoped = true;

	public VoiceRecognizerManagerService() {
		Log.d(TAG, "VoiceRecognizerManagerService construct");
	}

	/**
	 * 注册语音识别服务，由语音识别服务调用，提供注册客户端、开始以及停止识别等接口
	 */
	@Override
	public void registerService(IVoiceRecognizer service) {
		Log.d(TAG, "registerService");
		mVoiceRecognizer = service;
	}

	/**
	 * 注册客户端，由需要进行语音识别的客户端调用
	 */
	@Override
	public void registerClient(RecognizeClient client) {
		if (mVoiceRecognizer != null) {
			try {
				mVoiceRecognizer.register(client);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 注销客户端，由需要进行语音识别的客户端调用
	 */
	@Override
	public void unRegisterClient(RecognizeClient client) {
		if (mVoiceRecognizer != null) {
			try {
				stopRecognize(client);
				mVoiceRecognizer.unRegister(client);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 开始语音识别，为异步开启，执行完此方法后，真正开启动作为异步执行
	 */
	@Override
	public void startRecognize(RecognizeClient client) {
		if (mVoiceRecognizer != null) {
			try {
				mRecStoped = false;
				mVoiceRecognizer.start(client, false);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

        /**
	 * 开始语音识别，为异步开启，执行完此方法后，真正开启动作为异步执行
	 */
	@Override
	public void startRecognizeForce(RecognizeClient client) {
		if (mVoiceRecognizer != null) {
			try {
				mRecStoped = false;
				mVoiceRecognizer.start(client, true);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 停止语音识别，为异步停止，执行完此方法后，真正停止动作为异步执行
	 */
	@Override
	public void stopRecognize(RecognizeClient client) {
		if (mRecStoped)
			return;
		if (mVoiceRecognizer != null) {
			try {
				mRecStoped = true;
				mVoiceRecognizer.stop(client);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 立即停止语音识别，在同一线程中进行
	 */
	@Override
	public void stopRecognizeImmediate(RecognizeClient client) {
		if (mVoiceRecognizer != null) {
			try {
				mRecStoped = true;
				mVoiceRecognizer.stopImmediate(client);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 设置语音识别参数，如开启关闭语音、开启关闭语音播报等
	 */	
	@Override
	public boolean setParameter(String key, String value) {
		if (mVoiceRecognizer != null) {
			try {
				return mVoiceRecognizer.setParameter(key, value);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
		return false;
	}

	/**
	 * 获取语音识别参数
	 */	
	@Override
	public String getParameter(String key) {
		if (mVoiceRecognizer != null) {
			try {
				return mVoiceRecognizer.getParameter(key);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
		return null;
	}

	/**
	 * 开始语音播报
	 */	
	@Override
	public void playTTS(String tts) {
		if (mVoiceRecognizer != null) {
			try {
				mVoiceRecognizer.playTTS(tts);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 停止语音播报
	 */	
	@Override
	public void stopTTS() {
		if (mVoiceRecognizer != null) {
			try {
				mVoiceRecognizer.stopTTS();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}
