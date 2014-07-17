/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;
import android.util.Log;

import com.android.internal.msgcenter.IMessageCenter;
import com.android.internal.msgcenter.IMessageCenterService;
import com.android.internal.msgcenter.MessageCenterIcon;
import com.android.internal.msgcenter.MessageCenterIconList;
import android.service.notification.MessageCenterNotification;
import com.android.server.wm.WindowManagerService;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A note on locking:  We rely on the fact that calls onto mBar are oneway or
 * if they are local, that they just enqueue messages to not deadlock.
 */
public class MessageCenterManagerService extends IMessageCenterService.Stub
{
    static final String TAG = "MessageCenterManagerService";
    static final boolean SPEW = false;

    final Context mContext;
    final WindowManagerService mWindowManager;
    Handler mHandler = new Handler();
    StatusBarManagerService.NotificationCallbacks mNotificationCallbacks;
    volatile IMessageCenter mMsgCenter;
    MessageCenterIconList mIcons = new MessageCenterIconList();
    HashMap<IBinder,MessageCenterNotification> mNotifications
            = new HashMap<IBinder,MessageCenterNotification>();

    Object mLock = new Object();
    // encompasses lights-out mode and other flags defined on View
    int mSystemUiVisibility = 0;
    boolean mMenuVisible = false;
    int mImeWindowVis = 0;
    int mImeBackDisposition;
    IBinder mImeToken = null;
    int mCurrentUserId;

    // public interface NotificationCallbacks {
    //     void onSetDisabled(int status);
    //     void onClearAll();
    //     void onNotificationClick(String pkg, String tag, int id);
    //     void onNotificationClear(String pkg, String tag, int id);
    //     void onPanelRevealed();
    //     void onNotificationError(String pkg, String tag, int id,
    //             int uid, int initialPid, String message);
    // }

    /**
     * Construct the service, add the status bar view to the window manager
     */
    public MessageCenterManagerService(Context context, WindowManagerService windowManager) {
        mContext = context;
        mWindowManager = windowManager;
	Log.e(TAG,"MessageCenterManagerService ................");
        // final Resources res = context.getResources();
        // mIcons.defineSlots(res.getStringArray(com.android.internal.R.array.config_statusBarIcons));
    }

    public void setNotificationCallbacks(StatusBarManagerService.NotificationCallbacks listener) {
        mNotificationCallbacks = listener;
    }

    // ================================================================================
    // From IMessageCenterService
    // ================================================================================
    public void collapsePanels() {
        if (mMsgCenter != null) {
            try {
                mMsgCenter.animateCollapsePanels();
            } catch (RemoteException ex) {
            }
        }
    }

    // ================================================================================
    // Callbacks from the msg center service.
    // ================================================================================
    public void registerMessageCenter(IMessageCenter callbacks, List<IBinder> notificationKeys, 
	    List<MessageCenterNotification> notifications,
            List<IBinder> binders) {
        Slog.i(TAG, "registerMessageCenter callbacks=" + callbacks);
        mMsgCenter = callbacks;
        synchronized (mNotifications) {
            for (Map.Entry<IBinder,MessageCenterNotification> e: mNotifications.entrySet()) {
                notificationKeys.add(e.getKey());
                notifications.add(e.getValue());
            }
        }
    }

    public void onNotificationClick(String pkg, String tag, int id) {
        mNotificationCallbacks.onNotificationClick(pkg, tag, id);
    }

    public void onNotificationError(String pkg, String tag, int id,
            int uid, int initialPid, String message) {
        // WARNING: this will call back into us to do the remove.  Don't hold any locks.
        mNotificationCallbacks.onNotificationError(pkg, tag, id, uid, initialPid, message);
    }

    public void onNotificationClear(String pkg, String tag, int id) {
        mNotificationCallbacks.onNotificationClear(pkg, tag, id);
    }

    public void onClearAllNotifications() {
        mNotificationCallbacks.onClearAll();
    }

    // ================================================================================
    // Callbacks for NotificationManagerService.
    // ================================================================================
    public IBinder addNotification(MessageCenterNotification notification) {
        synchronized (mNotifications) {
            IBinder key = new Binder();
            mNotifications.put(key, notification);
            if (mMsgCenter != null) {
                try {
                    mMsgCenter.addNotification(key, notification);
                } catch (RemoteException ex) {
                }
            }
            return key;
        }
    }

    public void updateNotification(IBinder key, MessageCenterNotification notification) {
        synchronized (mNotifications) {
            if (!mNotifications.containsKey(key)) {
                throw new IllegalArgumentException("updateNotification key not found: " + key);
            }
            mNotifications.put(key, notification);
            if (mMsgCenter != null) {
                try {
                    mMsgCenter.updateNotification(key, notification);
                } catch (RemoteException ex) {
                }
            }
        }
    }

    public void removeNotification(IBinder key) {
        synchronized (mNotifications) {
            final MessageCenterNotification n = mNotifications.remove(key);
            if (n == null) {
                Slog.e(TAG, "removeNotification key not found: " + key);
                return;
            }
            if (mMsgCenter != null) {
                try {
                    mMsgCenter.removeNotification(key);
                } catch (RemoteException ex) {
                }
            }
        }
    }


    // ================================================================================
    // Can be called from any thread
    // ================================================================================
    public void openNotificationDrawer() {
	if (mMsgCenter != null) {
	    Slog.e(TAG, "openNotificationDrawer ....");
	    try {
		mMsgCenter.openNotificationDrawer();
	    } catch (RemoteException ex) {
	    }
	}
    }

    // ================================================================================
    // Always called from UI thread
    // ================================================================================

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump StatusBar from from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return;
        }

        synchronized (mIcons) {
            mIcons.dump(pw);
        }

        synchronized (mNotifications) {
            int i=0;
            pw.println("Notification list:");
            for (Map.Entry<IBinder,MessageCenterNotification> e: mNotifications.entrySet()) {
                pw.printf("  %2d: %s\n", i, e.getValue().toString());
                i++;
            }
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)
                    || Intent.ACTION_SCREEN_OFF.equals(action)) {
                collapsePanels();
            }
            /*
            else if (Telephony.Intents.SPN_STRINGS_UPDATED_ACTION.equals(action)) {
                updateNetworkName(intent.getBooleanExtra(Telephony.Intents.EXTRA_SHOW_SPN, false),
                        intent.getStringExtra(Telephony.Intents.EXTRA_SPN),
                        intent.getBooleanExtra(Telephony.Intents.EXTRA_SHOW_PLMN, false),
                        intent.getStringExtra(Telephony.Intents.EXTRA_PLMN));
            }
            else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                updateResources();
            }
            */
        }
    };

}
