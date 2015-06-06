package com.android.internal.voicerecognizer;

import java.util.ArrayList;

import com.android.internal.voicerecognizer.IVoiceRecognizerListener;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class RecognizeClient implements Parcelable {

        public static final String CMD_EXIT = "退出";
	public static final int REC_TYPE_COMMAND = 0;
	public static final int REC_TYPE_DICTATION = 1;
	public static final int REC_TYPE_DIAL = 2;

	private long mId;
	private String mPackageName = null;
	private String mAppName = null;
        private ArrayList<String> mCommands = new ArrayList<String>();
        private ArrayList<String> mDisplayCommands = new ArrayList<String>();
	private String mDisplayStr = null;
	private IVoiceRecognizerListener mListener = null;
	private int mIcon = -1;
	private int mUserId = -1;
	private int mType = REC_TYPE_COMMAND;
	private boolean mShowWidget = true;
	private boolean mUseTimeout = true;

	public RecognizeClient(long id, int userId, int type,
			IVoiceRecognizerListener listener) {
		mId = id;
		mUserId = userId;
		mType = type;
		mListener = listener;
		if (type == REC_TYPE_COMMAND)
			mCommands.add(CMD_EXIT);
	}

	public RecognizeClient(long id, int userId, int type,
			IVoiceRecognizerListener listener, String packageName,
			String appName, ArrayList<String> cmds, int icon, boolean showWidget, boolean useTimeout) {
	        this(id, userId, type, listener);
		mPackageName = packageName;
		mAppName = appName;
		mIcon = icon;
		mShowWidget = showWidget;
		mUseTimeout = useTimeout;
		if (cmds != null)
		    mCommands.addAll(cmds);
	}

	public RecognizeClient(Parcel in) {
		readFromParcel(in);
	}

	public long getId() {
		return mId;
	}

	public void setPackageName(String name) {
		mPackageName = name;
	}

	public final String getPackageName() {
		return mPackageName;
	}

	public void setAppName(String name) {
		mAppName = name;
	}

	public final String getAppName() {
		return mAppName;
	}

	public void setCommands(ArrayList<String> cmds) {
	        mCommands = cmds;
	}

	public void addCommands(ArrayList<String> cmds) {
		mCommands.addAll(cmds);
	}

	public void addCommand(String cmd) {
		mCommands.add(cmd);
	}

	public final ArrayList<String> getCommands() {
		return mCommands;
	}

	public String findCommand(String sentence) {
		for (String cmd : mCommands) {
			Log.d("sn", "cmd:" + cmd + " sentence:" + sentence);
			if (cmd.equals(sentence)) {
				return cmd;
			}
		}

		return null;
	}

	public void setListener(IVoiceRecognizerListener listener) {
		mListener = listener;
	}

	public final IVoiceRecognizerListener getListener() {
		return mListener;
	}

	public void setIcon(int icon) {
		mIcon = icon;
	}

	public final int getIcon() {
		return mIcon;
	}

	public final int getUserId() {
		return mUserId;
	}

	public void setType(int type) {
		mType = type;
	}

	public final int getType() {
		return mType;
	}

	public void setShowWidget(boolean showWidget) {
		mShowWidget = showWidget;
	}
	
	public boolean getShowWidget() {
		return mShowWidget;
	}
	
	public void setUseTimeout(boolean useTimeout) {
		mUseTimeout = useTimeout;
	}
	
	public boolean getUseTimeout() {
		return mUseTimeout;
	}
	
	public void setDisplayStr(String str) {
		mDisplayStr = str;
	}

	public String getDisplayStr() {
		return mDisplayStr;
	}

	public void setDisplayCommands(ArrayList<String> cmds) {
	        mDisplayCommands = cmds;
	}

	public void addDisplayCommands(ArrayList<String> cmds) {
		mDisplayCommands.addAll(cmds);
	}

	public void addDisplayCommand(String cmd) {
		mDisplayCommands.add(cmd);
	}

	public final ArrayList<String> getDisplayCommands() {
		if (mDisplayCommands == null || mDisplayCommands.size() <= 0)
			return mCommands;
		else
			return mDisplayCommands;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	private void readFromParcel(Parcel source) {
		mId = source.readLong();
		mUserId = source.readInt();
		mType = source.readInt();
		mListener = IVoiceRecognizerListener.Stub.asInterface(source.readStrongBinder());
		mIcon = source.readInt();
		mShowWidget = (source.readInt() == 1);
		mUseTimeout = (source.readInt() == 1);
		if (source.readInt() != 0)
			mPackageName = source.readString();
		if (source.readInt() != 0)
			mAppName = source.readString();
		if (source.readInt() != 0) {
			mCommands = new ArrayList<String>();
			source.readStringList(mCommands);
		}
		if (source.readInt() != 0) {
			mDisplayCommands = new ArrayList<String>();
			source.readStringList(mDisplayCommands);
		}
		if (source.readInt() != 0)
			mDisplayStr = source.readString();
	}

	@Override
	public String toString() {
		return "[id:"+mId+" packageName:"+mPackageName
			+" appName:"+mAppName+" commands:"+mCommands
			+" displayCmds:"+mDisplayCommands+" displayStr:"+mDisplayStr+"]";
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		dest.writeInt(mUserId);
		dest.writeInt(mType);
		dest.writeStrongBinder(mListener.asBinder());
		dest.writeInt(mIcon);
		dest.writeInt(mShowWidget ? 1 : 0);
		dest.writeInt(mUseTimeout ? 1 : 0);
		if (mPackageName != null) {
			dest.writeInt(1);
			dest.writeString(mPackageName);
		} else
			dest.writeInt(0);
		if (mAppName != null) {
			dest.writeInt(1);
			dest.writeString(mAppName);
		} else
			dest.writeInt(0);
		if (mCommands != null) {
			dest.writeInt(1);
			dest.writeStringList(mCommands);
		} else
			dest.writeInt(0);
		if (mDisplayCommands != null) {
			dest.writeInt(1);
			dest.writeStringList(mDisplayCommands);
		} else
			dest.writeInt(0);
		if (mDisplayStr != null) {
			dest.writeInt(1);
			dest.writeString(mDisplayStr);
		} else
			dest.writeInt(0);
	}

	public static final Parcelable.Creator<RecognizeClient> CREATOR = new Parcelable.Creator<RecognizeClient>() {
		@Override
		public RecognizeClient createFromParcel(Parcel source) {
			return new RecognizeClient(source);
		}

		@Override
		public RecognizeClient[] newArray(int size) {
			return new RecognizeClient[size];
		}
	};

}