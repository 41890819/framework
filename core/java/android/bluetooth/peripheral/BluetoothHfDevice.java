/******************************************************************************
 *
 *  Copyright (C) 2013 Broadcom Corporation
 *
 *  This program is the proprietary software of Broadcom Corporation and/or its
 *  licensors, and may only be used, duplicated, modified or distributed
 *  pursuant to the terms and conditions of a separate, written license
 *  agreement executed between you and Broadcom (an "Authorized License").
 *  Except as set forth in an Authorized License, Broadcom grants no license
 *  (express or implied), right to use, or waiver of any kind with respect to
 *  the Software, and Broadcom expressly reserves all rights in and to the
 *  Software and all intellectual property rights therein.
 *  IF YOU HAVE NO AUTHORIZED LICENSE, THEN YOU HAVE NO RIGHT TO USE THIS
 *  SOFTWARE IN ANY WAY, AND SHOULD IMMEDIATELY NOTIFY BROADCOM AND DISCONTINUE
 *  ALL USE OF THE SOFTWARE.
 *
 *  Except as expressly set forth in the Authorized License,
 *
 *  1.     This program, including its structure, sequence and organization,
 *         constitutes the valuable trade secrets of Broadcom, and you shall
 *         use all reasonable efforts to protect the confidentiality thereof,
 *         and to use this information only in connection with your use of
 *         Broadcom integrated circuit products.
 *
 *  2.     TO THE MAXIMUM EXTENT PERMITTED BY LAW, THE SOFTWARE IS PROVIDED
 *         "AS IS" AND WITH ALL FAULTS AND BROADCOM MAKES NO PROMISES,
 *         REPRESENTATIONS OR WARRANTIES, EITHER EXPRESS, IMPLIED, STATUTORY,
 *         OR OTHERWISE, WITH RESPECT TO THE SOFTWARE.  BROADCOM SPECIFICALLY
 *         DISCLAIMS ANY AND ALL IMPLIED WARRANTIES OF TITLE, MERCHANTABILITY,
 *         NONINFRINGEMENT, FITNESS FOR A PARTICULAR PURPOSE, LACK OF VIRUSES,
 *         ACCURACY OR COMPLETENESS, QUIET ENJOYMENT, QUIET POSSESSION OR
 *         CORRESPONDENCE TO DESCRIPTION. YOU ASSUME THE ENTIRE RISK ARISING
 *         OUT OF USE OR PERFORMANCE OF THE SOFTWARE.
 *
 *  3.     TO THE MAXIMUM EXTENT PERMITTED BY LAW, IN NO EVENT SHALL BROADCOM
 *         OR ITS LICENSORS BE LIABLE FOR
 *         (i)   CONSEQUENTIAL, INCIDENTAL, SPECIAL, INDIRECT, OR EXEMPLARY
 *               DAMAGES WHATSOEVER ARISING OUT OF OR IN ANY WAY RELATING TO
 *               YOUR USE OF OR INABILITY TO USE THE SOFTWARE EVEN IF BROADCOM
 *               HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES; OR
 *         (ii)  ANY AMOUNT IN EXCESS OF THE AMOUNT ACTUALLY PAID FOR THE
 *               SOFTWARE ITSELF OR U.S. $1, WHICHEVER IS GREATER. THESE
 *               LIMITATIONS SHALL APPLY NOTWITHSTANDING ANY FAILURE OF
 *               ESSENTIAL PURPOSE OF ANY LIMITED REMEDY.
 *
 *****************************************************************************/


package android.bluetooth.peripheral;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.peripheral.IBluetoothHfDevice;
import android.bluetooth.peripheral.CallStateInfo;
import android.bluetooth.peripheral.IBluetoothHfDeviceCallback;
import android.bluetooth.peripheral.IBluetoothHfDevice.Stub;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.IBluetoothStateChangeCallback;

import android.os.ServiceManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public final class BluetoothHfDevice implements BluetoothProfile  {
    private static final String TAG = "BluetoothHfDevice";
    private static final boolean DBG = true;

    /** The profile is in disconnected state */
    public static final int STATE_DISCONNECTED = 0;
    /** The profile is in connecting state */
    public static final int STATE_CONNECTING = 1;
    /** The profile is in connecting state */
    public static final int STATE_CONNECTED = 2;
    /** The profile is in disconnecting state */
    public static final int STATE_DISCONNECTING = 3;

    /** A SCO audio channel is not established */
    public static final int STATE_AUDIO_DISCONNECTED = 0;
    /** A SCO audio channel connecting */
    public static final int STATE_AUDIO_CONNECTING    = 1;
    /** A SCO audio channel is established */
    public static final int STATE_AUDIO_CONNECTED = 2;
    /** A SCO audio channel disconnecting */
    public static final int STATE_AUDIO_DISCONNECTING = 3;

    /** Call state when a call setup is completed and becomes an active call */
    public static final int CALL_STATE_ACTIVE = 0;
    /** Call state for a held call */
    public static final int CALL_STATE_HELD = 1;
    /** Call state when a call is dialed */
    public static final int CALL_SETUP_STATE_DIALING = 2;
    /** Call state when a the number is alerted */
    public static final int CALL_SETUP_STATE_ALERTING = 3;
    /** Call state when there is an incoming call */
    public static final int CALL_SETUP_STATE_INCOMING = 4;
    /** Call state when there is an incoming call */
    public static final int CALL_SETUP_STATE_WAITING =5;
    /** Call state when there is no call setup is in progress */
    public static final int CALL_SETUP_STATE_IDLE = 6;

    /** Device status indicator indices to access service information
        from the integer array returned by the function {@link #getDeviceIndicators}. */
    public static final int INDICATOR_TYPE_SERVICE = 0;
    /** Device status indicator indices to access roaming information
        from the integer array returned by the function {@link #getDeviceIndicators} */
    public static final int INDICATOR_TYPE_ROAM = 1;
    /** Device status indicator indices to access signal information
        from the integer array returned by the function {@link #getDeviceIndicators} */
    public static final int INDICATOR_TYPE_SIGNAL = 2;
    /** Device status indicator indices to access battery information
        from the integer array returned by the function {@link #getDeviceIndicators} */
    public static final int INDICATOR_TYPE_BATTERY = 3;
    public static final int INDICATOR_TYPE_MAX= 4;


    /** @hide */
    public static final int INDEX_NUM_OF_ACTV = 0;
    /** @hide */
    public static final int INDEX_CALL_SETUP_STATE  = 1;
    /** @hide */
    public static final int INDEX_CALL_NUM_OF_HELD = 2;
    /** @hide */
    public static final int INDEX_CALL_STATE_INFO_MAX = 3;

    /** Network service available */
    public static final int SERVICE_NOT_AVAILABLE = 0;
    /** Network status unavailable */
    public static final int SERVICE_AVAILABLE = 1;


    /**  Service type home  */
    public static final int SERVICE_TYPE_HOME = 0;
    /**  Service type roaming */
    public static final int SERVICE_TYPE_ROAMING = 1;


    /** Call directions for outgoing call */
    public static final int CALL_DIRECTION_OUTGOING = 0;
    /** Call directions for incoming call */
    public static final int CALL_DIRECTION_INCOMING = 1;

    /** Call type for voice call */
    public static final int CALL_TYPE_VOICE = 0;
    /** Call type for data call */
    public static final int CALL_TYPE_DATA = 1;
    /** Call type for fax call */
    public static final int CALL_TYPE_FAX = 2;

    /** Single party call */
    public static final int CALL_MPTY_TYPE_SINGLE = 0;
    /** Muti  party call */
    public static final int CALL_MPTY_TYPE_MULTI =1;

    /** Call address type unknown */
    public static final int CALL_ADDRTYPE_UNKNOWN = 129;//0x81,
    /** Call address type international */
    public static final int CALL_ADDRTYPE_INTERNATIONAL = 145;//0x91

    /** When the ag disables the wbs */
    public static final int WBS_NONE =0;
    /** When the ag enables the wbs */
    public static final int WBS_YES = 1;

    /** VR State inactive*/
    public static final int VR_STATE_INACTIVE = 0;
    /** VR State active*/
    public static final int VR_STATE_ACTIVE = 1;

    /** Volume type speaker */
    public static final int VOLUME_TYPE_SPK = 0;
    /** Volume type mic */
    public static final int VOLUME_TYPE_MIC = 1;


    /** Hangup held, reject incoming/waiting*/

    public static final int HANGUP_HELD = 0;
    /** Hangup active calls and accept waiting/held call */
    public static final int HANGUP_ACTIVE_ACCEPT_HELD= 1;
    /** Swap active and held calls */
    public static final int SWAP_CALLS = 2;
    /**  Conference calls */
    public static final int CONFERENCE = 3;

    /** @hide phone storage type*/
    public static final String PHONE_MEM_TYPE_SIM = "SM"; //SIM book
    /** @hide phone storage type*/
    public static final String PHONE_MEM_TYPE_FDN = "FD"; // SIM fixes dialing
    /** @hide phone storage type*/
    public static final String PHONE_MEM_TYPE_MSISDN = "ON"; //SIM owner
    /** @hide phone storage type*/
    public static final String PHONE_MEM_TYPE_EN = "EN"; //SIM emergency
    /** Dialed calls*/
    public static final String PHONE_MEM_TYPE_LAST_DIALED  = "DC";
    /** Missed calls*/
    public static final String PHONE_MEM_TYPE_MISSED  = "MC";
    /**  phonebook*/
    public static final String PHONE_MEM_TYPE_PHONEBOOK = "ME";
    /** @hide phone storage type*/
    public static final String PHONE_MEM_TYPE_MT = "MT"; //Combined ME and SIM phonebook
    /** ME received call list*/
    public static final String PHONE_MEM_TYPE_RECEIVED  = "RC"; //
    /** @hide phone storage type*/
    public static final String PHONE_MEM_TYPE_SDN = "SN"; //Service dialing phonebook


    public static final int ERROR_AG_FAILURE = 0;
    public static final int ERROR_NO_CONNECTION_TO_PHONE = 1;
    public static final int ERROR_OPERATION_NOT_ALLOWED = 3;
    public static final int ERROR_OPERATION_NOT_SUPPORTED = 4;
    public static final int ERROR_PIN_REQUIRED = 5;
    public static final int ERROR_SIM_MISSING = 10;
    public static final int ERROR_SIM_PIN_REQUIRED = 11;
    public static final int ERROR_SIM_PUK_REQUIRED = 12;
    public static final int ERROR_SIM_FAILURE = 13;
    public static final int ERROR_SIM_BUSY = 14;
    public static final int ERROR_WRONG_PASSWORD = 16;
    public static final int ERROR_SIM_PIN2_REQUIRED = 17;
    public static final int ERROR_SIM_PUK2_REQUIRED = 18;
    public static final int ERROR_MEMORY_FULL = 20;
    public static final int ERROR_INVALID_INDEX = 21;
    public static final int ERROR_MEMORY_FAILURE = 23;
    public static final int ERROR_TEXT_TOO_LONG = 24;
    public static final int ERROR_TEXT_HAS_INVALID_CHARS = 25;
    public static final int ERROR_DIAL_STRING_TOO_LONG = 26;
    public static final int ERROR_DIAL_STRING_HAS_INVALID_CHARS = 27;
    public static final int ERROR_NO_SERVICE = 30;
    public static final int ERROR_ONLY_911_ALLOWED = 32;

    public static final int PHONEBOOK_READ_COMPLETED = 253;
    public static final int PHONEBOOK_READ_PROGRESS_UPDATE = 254;

    /** Error code for success */
    public static final int NO_ERROR = 255;

    /**
     * Intent used to broadcast the change in connection state of the Hf Device
     * profile.
     *
     * <p>This intent will have 3 extras:
     * <ul>
     *   <li> {@link #EXTRA_STATE} - The current state of the profile. </li>
     *   <li> {@link #EXTRA_PREVIOUS_STATE}- The previous state of the profile. </li>
     *   <li> {@link BluetoothDevice#EXTRA_DEVICE} - The remote device. </li>
     * </ul>
     * <p>{@link #EXTRA_STATE} or {@link #EXTRA_PREVIOUS_STATE} can be any of
     * {@link #STATE_DISCONNECTED}, {@link #STATE_CONNECTING},
     * {@link #STATE_CONNECTED}, {@link #STATE_DISCONNECTING}.
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission to
     * receive.
     */
    public static final String ACTION_CONNECTION_STATE_CHANGED =
        "com.broadcom.bt.hfdevice.profile.action.CONNECTION_STATE_CHANGED";

    /**
     * Intent used to broadcast the change in the Audio Connection state of the
     * Hf Device profile.
     *
     * <p>This intent will have 3 extras:
     * <ul>
     *   <li> {@link #EXTRA_STATE} - The current state of the profile. </li>
     *   <li> {@link #EXTRA_PREVIOUS_STATE}- The previous state of the profile. </li>
     *   <li> {@link BluetoothDevice#EXTRA_DEVICE} - The remote device. </li>
     * </ul>
     * <p>{@link #EXTRA_STATE} or {@link #EXTRA_PREVIOUS_STATE} can be any of
     * {@link #STATE_AUDIO_CONNECTED}, {@link #STATE_AUDIO_DISCONNECTED},
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission
     * to receive.
     */

    public static final String ACTION_AUDIO_STATE_CHANGED =
        "com.broadcom.bt.hfdevice.profile.action.AUDIO_STATE_CHANGED";

    /**
     * Intent used to broadcast the change in the Call state of the
     * Hf Device profile.
     *
     * <p>This intent will have 2 extras:
     * <ul>
     *   <li> {@link #EXTRA_STATE} - The current state of the profile. </li>
     *   <li> {@link BluetoothDevice#EXTRA_DEVICE} - The remote device. </li>
     * </ul>
     * <p>{@link #EXTRA_STATE} can be any of CALL_STATE_
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission
     * to receive.
     */
    public static final String ACTION_CALL_STATE_CHANGED =
               "com.broadcom.bt.hfdevice.profile.action.CALL_STATE_CHANGED";

    /**
     * Intent used to broadcast the Ring event when HSP connection is active
     *
     * <p>This intent will have 1 extra:
     * <ul>
     *   <li> {@link BluetoothDevice#EXTRA_DEVICE} - The remote device. </li>
     * </ul>
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission
     * to receive.
     */
    public static final String ACTION_RING_EVENT =
               "com.broadcom.bt.hfdevice.profile.action.RING_EVENT";

    /** This intent can be sent to enable/disable HF device SDP with EXTRA_REGISTER_HFDEVICE_SDP*/
    public static final String ACTION_REGISTER_HFDEVICE_SDP =
               "com.broadcom.bt.action.REGISTER_HFDEVICE_SDP";

    /** This intent sent after enable/disable HF device SDP happens*/
    public static final String ACTION_UUID_CHANGED =
               "com.broadcom.bt.action.ACTION_UUID_CHANGED";

    /** boolean extra true/false for enable/disable SDP */
    public static final String EXTRA_REGISTER_HFDEVICE_SDP = "EXTRA.REGISTER.SDP";


    /**
     * Intent used to broadcast the WBS state of Hf Device profile.
     * This intent is broadcasted only when both AG and HF supports WBS.
     * This will be broadcasted for whenever codec negotiation happens.
     * The app has to remember the codec negotiated for the existing SLC
     * connection.
     * <p>This intent will have 1 extras:
     * <ul>
     *   <li> {@link #EXTRA_STATE} - The current state of WBS. </li>
     * </ul>
     * <p>{@link #EXTRA_STATE} can be any of WBS_
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission
     * to receive.
     */
    public static final String ACTION_WBS_STATE_CHANGED =
               "com.broadcom.bt.hfdevice.profile.action.WBS_STATE_CHANGED";

    private Context mContext;
    private ServiceListener mServiceListener;
    private BluetoothAdapter mAdapter;
    private IBluetoothHfDevice mService;
    private IHfDeviceEventHandler mCallback;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (DBG) Log.d(TAG, "Proxy object connected");
            mService = IBluetoothHfDevice.Stub.asInterface(service);
            if (mServiceListener != null) {
                mServiceListener.onServiceConnected(BluetoothProfile.HF_DEVICE, BluetoothHfDevice.this);
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            if (DBG) Log.d(TAG, "Proxy object disconnected");
            mService = null;
            if (mServiceListener != null) {
                mServiceListener.onServiceDisconnected(BluetoothProfile.HF_DEVICE);
            }
        }
    };


    /**
     * Bluetooth state change handlers
     */
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback =
        new IBluetoothStateChangeCallback.Stub() {
            public void onBluetoothStateChange(boolean up) {
                if (DBG) Log.d(TAG, "onBluetoothStateChange: up=" + up);
                if (!up) {
                    if (DBG) Log.d(TAG,"Unbinding service...");
                    synchronized (mConnection) {
                        try {
                            mService = null;
                            mContext.unbindService(mConnection);
                        } catch (Exception re) {
                            Log.e(TAG,"",re);
                        }
                    }
                } else {
                    synchronized (mConnection) {
                        try {
                            if (mService == null) {
                                if (DBG) Log.d(TAG,"Binding service...");
                                if (!mContext.bindService(new
                                        Intent(IBluetoothHfDevice.class.getName()),
                                        mConnection, 0)) {
                                    Log.e(TAG, "Could not bind to Bluetooth GATT Service");
                                }
                            }
                        } catch (Exception re) {
                            Log.e(TAG,"",re);
                        }
                    }
                }
            }
        };

    /**
     * Retrieve an instance of the Proxy object.
     *
     * @param ctx Application context
     * @param l The callback object that will contain the proxy instance
     *            returned
     * @return false if unable to initialize proxy retrieval, true if
     *         initialization succeeded
     */
    public static boolean getProxy(Context ctx, ServiceListener l){

        boolean status = false;
        BluetoothHfDevice proxy = null;

        try {
            proxy = new BluetoothHfDevice(ctx, l);
        } catch (Throwable t) {
            Log.e(TAG, "Unable to get BluetoothHfDevice", t);
            return false;
        }
        return true;

    }

    /**
     * Close the profile proxy for cleanup.
     *
     */
    public  void closeProxy(){
        unregisterEventHandler();
        mServiceListener = null;

        IBinder b = ServiceManager.getService(BluetoothAdapter.BLUETOOTH_MANAGER_SERVICE);
        if (b != null) {
            IBluetoothManager mgr = IBluetoothManager.Stub.asInterface(b);
            try {
                mgr.unregisterStateChangeCallback(mBluetoothStateChangeCallback);
            } catch (RemoteException re) {
                Log.e(TAG, "Unable to unregister BluetoothStateChangeCallback", re);
            }
        }

        synchronized (mConnection) {
            if (mService != null) {
                try {
                    mService = null;
                    mContext.unbindService(mConnection);
                } catch (Exception re) {
                    Log.e(TAG,"",re);
                }
            }
        }
    }

    /*
     ** Get the local supported features
     ** @return {@link #LocalHfFeatures}
     */
     public LocalHfFeatures getLocalFeatures(){
        if (DBG) log("getLocalFeatures");
        int localFeatures = 0;
        if (mService != null && isEnabled()) {
            try {
                localFeatures = mService.getLocalFeatures();
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return new LocalHfFeatures(localFeatures);
     }


     /*
     ** Get the remote supported features
     ** @return {@link #PeerHfFeatures}
     */
     public PeerHfFeatures getPeerFeatures(){
         if (DBG) log("getPeerFeatures");
         int peerFeatures = 0;
         if (mService != null && isEnabled()) {
             try {
                 peerFeatures = mService.getPeerFeatures();
             } catch (RemoteException e) {
                 Log.e(TAG, Log.getStackTraceString(new Throwable()));
             }
         }
         if (mService == null) Log.w(TAG, "Proxy not attached to service");
         return new PeerHfFeatures(peerFeatures);
     }

    /**
     * Create a BluetoothHfDevice proxy object.
     */
    BluetoothHfDevice(Context context, ServiceListener l){
        mContext = context;
        mServiceListener = l;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        IBinder b = ServiceManager.getService(BluetoothAdapter.BLUETOOTH_MANAGER_SERVICE);
        if (b != null) {
            IBluetoothManager mgr = IBluetoothManager.Stub.asInterface(b);
            try {
                mgr.registerStateChangeCallback(mBluetoothStateChangeCallback);
            } catch (RemoteException re) {
                Log.e(TAG, "Unable to register BluetoothStateChangeCallback", re);
            }
        } else {
            Log.e(TAG, "Unable to get BluetoothManager interface.");
        }
        Log.d(TAG, "BluetoothHfDevice() call bindService");
        if (!context.bindService(new Intent(IBluetoothHfDevice.class.getName()),
                                 mConnection, 0)) {
            Log.e(TAG, "Could not bind to Bluetooth HfDevice Service");
        }
    }

    /**
     * Register a callback event handler to receive events. ALL events will be
     * sent from the profile service to handler.
     * NOTE: calling registerEventHandler again with with a new handler will
     * cause events from the stack to be delivered to the new handler.
     *
     * @param handler
     *            object that implements the event handler
     */
    public void registerEventHandler(IHfDeviceEventHandler handler){
        if (DBG) log( "registerEventHandler()");
        if (mService == null) return ;

        mCallback = handler;
        try {
            mService.registerEventHandler(mHfDeviceCallback);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
        }

        return ;
    }


    /**
     * Unregister the event handler.
     * This function performs the following:
     * <ol>
     * <li>Stops event delivery to thecurrently registered
     * {IHfDeviceEventHandler}</li>
     * <li>Unregisters the event delivery channel between the profile service
     * and this proxy object. This method
     * unregisters the internal remote callback object it uses</li>
     * </ol>
     */
    public void unregisterEventHandler(){
        if (DBG) Log.d(TAG, "unregisterEventHandler()");
        if (mService == null) return ;

        mCallback = null;
        try {
            mService.unRegisterEventHandler(mHfDeviceCallback);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
        }
        return ;
    }


    /**
     * Get the current connection state of the profile
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @param device Remote bluetooth device.
     * @return State of the profile connection. One of
     *               {@link #STATE_CONNECTED}, {@link #STATE_CONNECTING},
     *               {@link #STATE_DISCONNECTED}, {@link #STATE_DISCONNECTING}
     */
    public int getConnectionState(BluetoothDevice device) {
        if (DBG) log("getConnectionState(" + device + ")");
        if (mService != null && isEnabled() &&
            isValidDevice(device)) {
            try {
                return mService.getConnectionState(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return BluetoothProfile.STATE_DISCONNECTED;
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return BluetoothProfile.STATE_DISCONNECTED;
    }

    /**
     * Get connected devices for this specific profile.
     *
     * <p> Return the set of devices which are in state {@link #STATE_CONNECTED}
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @return List of devices. The list will be empty on error.
     */
    public List<BluetoothDevice> getConnectedDevices() {
        if (DBG) log("getConnectedDevices()");
        if (mService != null && isEnabled()) {
            try {
                return mService.getConnectedDevices();
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return new ArrayList<BluetoothDevice>();
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return new ArrayList<BluetoothDevice>();
    }

    /**
     * Get a list of devices that match any of the given connection
     * states.
     *
     * <p> If none of the devices match any of the given states,
     * an empty list will be returned.
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @param states Array of states. States can be one of
     *              {@link #STATE_CONNECTED}, {@link #STATE_CONNECTING},
     *              {@link #STATE_DISCONNECTED}, {@link #STATE_DISCONNECTING},
     * @return List of devices. The list will be empty on error.
     */
    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        if (DBG) log("getDevicesMatchingStates()");
        if (mService != null && isEnabled()) {
            try {
                return mService.getDevicesMatchingConnectionStates(states);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return new ArrayList<BluetoothDevice>();
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return new ArrayList<BluetoothDevice>();
    }

    /**
     * Get the current SCO/Audio state
     *
     * @return One of the values from AUDIO_STATE_
     */
    public int getAudioState(BluetoothDevice device) {
        if (DBG) log("getAudioState(" + device + ")");
        if (mService != null && isEnabled() &&
            isValidDevice(device)) {
            try {
                return mService.getAudioState(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return BluetoothHfDevice.STATE_AUDIO_DISCONNECTED;
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return BluetoothHfDevice.STATE_AUDIO_DISCONNECTED;
    }


    /**
     * Set priority of the profile
     *
     * <p> The device should already be paired.
     *  Priority can be one of {@link #PRIORITY_ON} or
     * {@link #PRIORITY_OFF},
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH_ADMIN}
     * permission.
     *
     * @param device Paired bluetooth device
     * @param priority
     * @return true if priority is set, false on error
     * @hide
     */
    public boolean setPriority(BluetoothDevice device, int priority) {
        if (DBG) log("setPriority(" + device + ", " + priority + ")");
        if (mService != null && isEnabled() &&
            isValidDevice(device)) {
            if (priority != BluetoothProfile.PRIORITY_OFF &&
                priority != BluetoothProfile.PRIORITY_ON) {
              return false;
            }
            try {
                return mService.setPriority(device, priority);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return false;
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return false;
    }

    /**
     * Get the priority of the profile.
     *
     * <p> The priority can be any of:
     * {@link #PRIORITY_AUTO_CONNECT}, {@link #PRIORITY_OFF},
     * {@link #PRIORITY_ON}, {@link #PRIORITY_UNDEFINED}
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @param device Bluetooth device
     * @return priority of the device
     * @hide
     */
    public int getPriority(BluetoothDevice device) {
        if (DBG) log("getPriority(" + device + ")");
        if (mService != null && isEnabled() &&
            isValidDevice(device)) {
            try {
                return mService.getPriority(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return PRIORITY_OFF;
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return PRIORITY_OFF;
    }


    /**
     * Returns the cached device indicators that are updated on Ag notification.
     *
     * @return
     *         The integer array has  current indicators as received from
     *         CIND
     *         Status info regarding Service,Battery, Signal, Roam
     *         can be extracted from array using
     *         the corresponding INDICATOR_TYPE_ as array index.
     */
    public int[] getDeviceIndicators(BluetoothDevice device){
        if (DBG) log("getDeviceIndicators()");
        if (mService != null && isEnabled()) {
            try {
                return mService.getDeviceIndicators(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return new int[3];
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return new int[3];
    }


    /**
     * Get the current Call state
     *
     * @return {@link CallStateInfo} containing call setup state (CALL_STATE_ ),
     * num of active calls, num of held calls.
     */
    public CallStateInfo getCallStateInfo(BluetoothDevice device){
        if (DBG) log("getDeviceIndicators()");
        if (mService != null && isEnabled()) {
            try {
                int[]arrCallStateInfo = mService.getCallStateInfo(device);
                return new CallStateInfo(arrCallStateInfo[0],
                            arrCallStateInfo[1], arrCallStateInfo[2]);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return new CallStateInfo(0,0,0);
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return new CallStateInfo(0,0,0);
    }

    /**
     * Request to initiate a connection to a AG and updates the status
     * through {@link IHfDeviceEventHandler#onConnectionStateChange}
     * @param device
     *            device to connect to, or null to auto-connect last connected.
     * @return false if there was a problem initiating the connection
     *         procedure, and no further call back to onAudioStateChange
     *         will not happen.
     */
    public boolean connect(BluetoothDevice device) {
        if (DBG) log("connect(" + device + ")");
        if (mService != null && isEnabled() &&
            isValidDevice(device)) {
            try {
                return mService.connect(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return false;
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return false;
    }

    /**
     * Intiate disconnection and updates the status through
     * {@link IHfDeviceEventHandler#onConnectionStateChange}
     * @param device
     *            device to disconnect
     * @return false if this proxy object is
     *         not currently connected to the Hf Device service.
     */
    public boolean disconnect(BluetoothDevice device) {
        if (DBG) log("disconnect(" + device + ")");
        if (mService != null && isEnabled() &&
            isValidDevice(device)) {
            try {
                return mService.disconnect(device);
            } catch (RemoteException e) {
              Log.e(TAG, Log.getStackTraceString(new Throwable()));
              return false;
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return false;
    }

    /**
     * Request to initiate an audio connection to the AG device.
     * Updates the status through {@link IHfDeviceEventHandler#onAudioStateChange}
     *
     *            device to connect audio to, or null to connect audio to connected device
     * @return false if there was a problem initiating the connection
     *         procedure, and no status callback will be called.
     */
    public boolean connectAudio() {
        if (mService != null && isEnabled()) {
            try {
                return mService.connectAudio();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }


    /**
     * Disconnects the audio connection.
     * Updates the status through {@link IHfDeviceEventHandler#onAudioStateChange}
     *
     *
     * @return false if this proxy object is
     *         not currently connected to the Hf Device service or no active audio connection
     */
    public boolean disconnectAudio() {
        if (mService != null && isEnabled()) {
            try {
                return mService.disconnectAudio();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }


    /**
     * Send command to activate Voice Recognition in AG.
     * Updates the status through {@link IHfDeviceEventHandler#onVRStateChange}
     *
     * @param device
     *
     * @return Returns false if there is no AG connected, or  on error.
     */
    public boolean startVoiceRecognition(BluetoothDevice device) {
        if (DBG) log("startVoiceRecognition()");
        if (mService != null && isEnabled() &&
            isValidDevice(device)) {
            try {
                return mService.startVoiceRecognition(device);
            } catch (RemoteException e) {
                Log.e(TAG,  Log.getStackTraceString(new Throwable()));
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return false;
    }


    /**
     * Send command to deactivate Voice Recognition in AG.
     * Updates the status through {@link IHfDeviceEventHandler#onVRStateChange}
     * @param device
     * @return Returns false if there is no AG connected, or on error.
     */
    public boolean stopVoiceRecognition(BluetoothDevice device) {
        if (DBG) log("stopVoiceRecognition()");
        if (mService != null && isEnabled() &&
            isValidDevice(device)) {
            try {
                return mService.stopVoiceRecognition(device);
            } catch (RemoteException e) {
                Log.e(TAG,  Log.getStackTraceString(new Throwable()));
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return false;
    }

    /**
     * Set volume
     *
     * @param volType
     *            Type of volume change speaker/mic
     * @param volume
     *            Current volume (0-15)
     */

    public boolean setVolume(int volType, int volume) {
        if (DBG) log("setVolume()");
        if (mService != null && isEnabled()) {
            try {
                return mService.setVolume(volType, volume);
            } catch (RemoteException e) {
                Log.e(TAG,  Log.getStackTraceString(new Throwable()));
            }
        }
        if (mService == null) Log.w(TAG, "Proxy not attached to service");
        return false;
    }

    /**
     * Place an outgoing call to the specified number string
     * Updates the status through {@link IHfDeviceEventHandler#onCallStateChange}
     * @param number
     *            Number to dial.

     * @return false if object is currently not connected to the HfDevice
     *         service.
     */
    public boolean dial(String number){
        if (DBG) log("dial"+"number"+number);
        if (mService != null && isEnabled()) {
            try {
                return mService.dial(number);
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }


    /**
     * Redial the last dialed number
     * Updates the status through {@link IHfDeviceEventHandler#onCallStateChange}
     *
     * @return false if object is currently not connected to the HfDevice
     *         service.
     */
    public boolean redial(){
        if (DBG) log("redial");
        if (mService != null && isEnabled()) {
            try {
                return mService.redial();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }


    /**
     * Hangup active call and incoming call.
     * Updates the status through {@link IHfDeviceEventHandler#onCallStateChange}
     *
     * @return false if object is currently not connected to the HfDevice
     *         service.
     */
    public boolean hangup(){
        if (DBG) log("hangup");
        if (mService != null && isEnabled()) {
            try {
                return mService.hangup();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }


    /**
     * Call held handling.To reject ,hold , call swapping , conference.
     * Updates the status through {@link IHfDeviceEventHandler#onCallStateChange}
     *
     * @param holdType One of call hold values
     * @return false if object is currently not connected to the HfDevice
     *         service.
     */
    public boolean hold(int holdType){
        if (DBG) log("hold"+"type"+holdType);
        if (mService != null && isEnabled()) {
            try {
                return mService.hold(holdType);
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }


    /**
     * Answer an incoming call.
     * Updates the status through {@link IHfDeviceEventHandler#onCallStateChange}
     *
     * @return false if object is currently not connected to the HfDevice
     *         service.
     */
    public boolean answer() {
        if (DBG) log("answer");
        if (mService != null && isEnabled()) {
            try {
                return mService.answer();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }

    /**
     * Send DTMF code
     * @param dtmfcode DTMF code value
     * @return false if object is currently not connected to the HfDevice
     *         service.
     */
    public boolean sendDTMFcode(char dtmfcode){
        if (DBG) log("sendDTMFcode"+"dtmfcode"+dtmfcode);
        if (mService != null && isEnabled()) {
            try {
                return mService.sendDTMFcode(dtmfcode);
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }


    /**
     * Send pre-formatted AT command strings (example: AT+CPIN)
     * Updates the response through {@link IHfDeviceEventHandler#onVendorAtRsp}
     *
     * @return false if object is currently not connected to the HfDevice
     *         service.
     */
    public boolean sendVendorCmd(String atCmd){
        if (DBG) log("sendVendorCmd"+"atCmd"+atCmd);
        if (mService != null && isEnabled()) {
            try {
                return mService.sendVendorCmd(atCmd);
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }

    /**
     * Query operator selection info.
     * Updates the status through {@link IHfDeviceEventHandler#onOperatorSelectionRsp}
     * @return false if this proxy object is
     *         not currently connected to the Hf Device service.
     */
    public boolean queryOperatorSelectionInfo(){
        if (DBG) log("queryOperatorSelectionInfo");
        if (mService != null && isEnabled()) {
            try {
                return mService.queryOperatorSelectionInfo();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }

    /**
     * Query subscriber info.
     * Updates the status through {@link IHfDeviceEventHandler#onSubscriberInfoRsp}
     * @return false if this proxy object is
     *         not currently connected to the Hf Device service.
     */
    public boolean querySubscriberInfo(){
        if (DBG) log("querySubscriberInfo");
        if (mService != null && isEnabled()) {
            try {
                return mService.querySubscriberInfo();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }


    /**
     * Get CLCC from ag.
     * Updates the status through {@link IHfDeviceEventHandler#onCLCCRsp}
     * App should avoid calling getCLCC() when it is has already done getCLCC()
     * and still not received an corresponding response throught onCLCCRsp()
     * @return false if this proxy object is
     *         not currently connected to the Hf Device service.
     */
    public boolean getCLCC(){
        if (DBG) log("getCurrentCallList");
        if (mService != null && isEnabled()) {
            try {
                return mService.getCLCC();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }


    /*
     * Read phone book entries .
     * Updates the status through onPhoneBookReadRsp().
     * @param phoneMemType
     *            Select the phone storage for reading using the PHONE_MEM_TYPE_.
     * @param maxReadLimit
     *             maxReadLimit gives the user a option to  limit the item count to query from AG side.
     *             For eg: The AG may have 100 contact but if the app sets the MaxLimit = 10
     *             then only 10 items will queried and returned to app.
     *             If app doesn't want to bother about the count and want to download all the contacts in AG side
     *             then set maxReadLimit = -1(default which will download all the available contacts for the memory).
     * @return false if this proxy object is
     *         not currently connected to the Hf Device service.
     * @ hide
     */
    public boolean readPhoneBookList(String phoneMemType, int maxReadLimit) {
        if (DBG) log("readPhoneBookList"+"phoneMemType"+phoneMemType+
            "maxReadLimit"+maxReadLimit);
        if (mService != null && isEnabled()) {
            try {
                return mService.readPhoneBookList(phoneMemType, maxReadLimit);
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }

    /**
     * Send HSP key pressed event to AG
     * @return false if object is currently not connected to the HfDevice
     *         service.
     */
    public boolean sendKeyPressedEvent(){
        if (DBG) log("sendKeyPressedEvent");
        if ((mService != null) && isEnabled()) {
            try {
                return mService.sendKeyPressEvent();
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return false;
    }

    /* callbacks */
    /**
     * The class containing all the HfDevice callback function handlers. These
     * functions will be called by the HfDeviceService module when callback
     * events occur. They in turn relay the callback information back to the
     * main applications callback handler.
     */
    private final IBluetoothHfDeviceCallback mHfDeviceCallback =
        new IBluetoothHfDeviceCallback.Stub() {
        /**
         * Callback for connection state change.
         *
         * @param deviceState
         *            One of the STATE_
         */
        public void onConnectionStateChange(int errCode, BluetoothDevice remoteDevice,
                int newState, int prevState, int peerFeatures, int localFeatures){
            if (mCallback != null)
                mCallback.onConnectionStateChange(errCode, remoteDevice,
                newState, prevState);
         }

        /**
         * Callback for audio state change.
         *
         * @param deviceState
         *            One of the AUDIO_STATE_
         */
        public void onAudioStateChange(int newState, int prevState){
            if (mCallback != null)
                mCallback.onAudioStateChange(newState, prevState);
        }

        /**
         * Callback for device indicators update ( Battery, signal, roam, service)
         *
         * @param indValue, array containing all indicators of INDICATOR_TYPE_ as index.
         *            Contains the individual indicators
         */
        public void onIndicatorsUpdate(int[] indValue){
            if (mCallback != null)
                mCallback.onIndicatorsUpdate(indValue);
        }

        /**
         * Callback for call state change.
         *
         * @param callState
         *            One of the values from CALL_STATE_
         *            number,addrType: This is valid only for valid call states
         *            like CALL_SETUP_STATE_INCOMING.
         */
        public void onCallStateChange(int status, int callState, int numActive, int numHeld,
                                    String number ,int addrType){
            if (mCallback != null)
                mCallback.onCallStateChange(status, callState, numActive,
                numHeld, number, addrType);
        }

        /**
         * Callback for VR state change
         *
         * @param vrState
         *            One of the values from VR_STATE_
         */
        public void onVRStateChange(int status, int vrState){
            if (mCallback != null)
                mCallback.onVRStateChange(status, vrState);
        }

        /**
         * Callback for volume change
         *
         * @param volType
         *            Type of volume change speaker/mic
         * @param volume
         *            Current volume (0-15)
         */
        public void onVolumeChange(int volType, int volume){
            if (mCallback != null)
                mCallback.onVolumeChange(volType, volume);
        }


        /**
         * Callback for readPhoneBookList() response
         *
         * @param PhoneBookInfo
         *         List of phone book entries  with index, number , addrtype,name.
         *         getXXXX() can be used to get info from the class PhoneBookInfo
         */
         public void onPhoneBookReadRsp(int status, List<PhoneBookInfo> phoneNum) {
            if (mCallback != null)
                mCallback.onPhoneBookReadRsp(status, phoneNum);
        }



        /**
         * Callback for querySubscriberInfo() response
         *
         * @param number
         * @param addrType
         */
        public void onSubscriberInfoRsp(int status, String number ,int addrType) {
            if (mCallback != null)
                mCallback.onSubscriberInfoRsp(status, number, addrType);
        }

        /**
         * Callback for queryOperatorSelectionInfo response
         *
         * @param cops
         *            String containing the operator name.
         */
        public void onOperatorSelectionRsp(int status, int mode, String operatorName){
            if (mCallback != null)
                mCallback.onOperatorSelectionRsp(status, mode, operatorName);
        }

        /**
         * Callback for Extended error result code
         *
         * @param errorResultCode
         *            Containing the extended result code
         */
        public void onExtendedErrorResult(int errorResultCode){
            if (mCallback != null)
                mCallback.onExtendedErrorResult(errorResultCode);
        }


        /**
         * Callback for getCurrentCallList response
         *
         * @param HfDeviceCLCCInfo
         *         List of current call info with index, call_direction_ , call_state,
         *         call_mode_,call_mpty, number, call_addrtype
         *         getXXXX() can be used to get info from the class HfDeviceCLCCInfo
         */
      public void onCLCCRsp(int status, List<ClccInfo> clcc) {
            if (mCallback != null)
                mCallback.onCLCCRsp(status, clcc);
         }

        /**
         * Callback for Vendor/app pre-formatted AT strings.
         * Note that if app sends a pre-formatted AT command for which a
         * callback
         * is already defined above, then the response will be sent in the
         * pre-defined callback.
         *
         * @param atRsp
         *            String containing the AT response
         */
        public void onVendorAtRsp(int status, String atRsp){
            if (mCallback != null)
                mCallback.onVendorAtRsp(status, atRsp);
        }

        /**
         * Callback for RING event(send by AG) when a HSP connection exist.
         * This will usually happen for incoming call in AG. Continous RING event
         * may be sent from AG side till the call is answered.In such case
         *.the app should take care such that it ignores the
         * the subsequent RING event if it is not necessary.
         */
        public void onRingEvent(){
            if (mCallback != null)
                mCallback.onRingEvent();
        }

    };


    public class LocalHfFeatures {

        private static final int ECNR =  0x0001;  /* Echo cancellation and/or noise reduction */
        private static final int THREEWAY =  0x0002;  /* Call waiting and three-way calling */
        private static final int CLIP =  0x0004;  /* Caller ID presentation capability  */
        private static final int VREC =  0x0008;  /* Voice recoginition activation capability  */
        private static final int RVOL =  0x0010;  /* Remote volume control capability  */
        private static final int ECS =   0x0020;  /* Enhanced Call Status  */
        private static final int ECC =   0x0040;  /* Enhanced Call Control  */
        private static final int CODEC = 0x0080;  /* Codec negotiation */
        private static final int VOIP =  0x0100;  /* VoIP call */
        private static final int UNAT =  0x1000;  /* Pass unknown AT command responses */

        int mLocalFeatures;
        public boolean isThreeWayCallSupported(){
            return (THREEWAY == (THREEWAY & mLocalFeatures));
        }

        public boolean isECNRSupported(){
            return (ECNR == (ECNR & mLocalFeatures));
        }

        public boolean isCLIPSupported(){
            return (CLIP == (CLIP & mLocalFeatures));
        }

        public boolean isVRSupported(){
            return (VREC == (VREC & mLocalFeatures));
        }

        public boolean isRemoteVolumeControlSupported(){
            return (RVOL == (RVOL & mLocalFeatures));
        }

        public boolean isECStatusSupported(){
            return (ECS == (ECS & mLocalFeatures));
        }

        public boolean isECControlSupported(){
            return (ECC == (ECC & mLocalFeatures));
        }

        public boolean isCodecNegotiationSupported(){
            return (CODEC == (CODEC & mLocalFeatures));
        }

        public boolean isVoipCallSupported(){
            return (VOIP == (VOIP & mLocalFeatures));
        }

        public boolean isUnknownAtCommandSupported(){
            return (UNAT == (UNAT & mLocalFeatures));
        }


        LocalHfFeatures (int localFeatures){
            mLocalFeatures = localFeatures;
        }

        public void printLog() {
            Log.d(TAG+"LocalHfFeatures","isThreeWayCallSupported()="+isThreeWayCallSupported()+"\n"+
                "isECNRSupported()="+isECNRSupported()+"\n"+
                "isCLIPSupported()="+isCLIPSupported()+"\n"+
                "isVRSupported()="+isVRSupported()+"\n"+
                "isRemoteVolumeControlSupported()="+isRemoteVolumeControlSupported()+"\n"+
                "isECStatusSupported()="+isECStatusSupported()+"\n"+
                "isECControlSupported()="+isECControlSupported()+"\n"+
                "isCodecNegotiationSupported()="+isCodecNegotiationSupported()+"\n"+
                "isVoipCallSupported()="+isVoipCallSupported()+
                "isUnknownAtCommandSupported()="+isUnknownAtCommandSupported());
        }


}

    public class PeerHfFeatures {

        private static final int THREEWAY =  0x0001;    /* Three-way calling */
        private static final int ECNR =  0x0002;    /* Echo cancellation and/or noise reduction */
        private static final int VREC =  0x0004;    /* Voice recognition */
        private static final int INBAND= 0x0008;    /* In-band ring tone */
        private static final int VTAG =  0x0010;    /* Attach a phone number to a voice tag */
        private static final int REJECT= 0x0020;    /* Ability to reject incoming call */
        private static final int ECS =   0x0040;    /* Enhanced call status */
        private static final int ECC =   0x0080;    /* Enhanced call control */
        private static final int EERC =  0x0100;    /* Extended error result codes */
        private static final int CODEC = 0x0200;    /* Codec Negotiation */
        private static final int VOIP =  0x0400;    /* VoIP call */

        /* When no fields are set it is assumed as HSP conn */
        private static final int HSP_ROLE =  0x0000;

        int mPeerFeatures;

        public boolean isHSPConnection(){
            return (HSP_ROLE == mPeerFeatures);
        }

        public boolean isThreeWayCallSupported(){
            return (THREEWAY == (THREEWAY & mPeerFeatures));
        }

        public boolean isECNRSupported(){
            return (ECNR == (ECNR & mPeerFeatures));
        }

        public boolean isVRSupported(){
            return (VREC == (VREC & mPeerFeatures));
        }

        public boolean isInBandToneSupported(){
            return (INBAND == (INBAND & mPeerFeatures));
        }

        public boolean isVtagSupported(){
            return (VTAG == (VTAG & mPeerFeatures));
        }

        public boolean isRejectIncomingSupported(){
            return (REJECT == (REJECT & mPeerFeatures));
        }

        public boolean isECStatusSupported(){
            return (ECS == (ECS & mPeerFeatures));
        }

        public boolean isECControlSupported(){
            return (ECC == (ECC & mPeerFeatures));
        }

        public boolean isEERCSupported(){
            return (EERC == (EERC & mPeerFeatures));
        }

        public boolean isCodecNegotiationSupported(){
            return (CODEC == (CODEC & mPeerFeatures));
        }

        public boolean isVoipCallSupported(){
            return (VOIP == (VOIP & mPeerFeatures));
        }

        public PeerHfFeatures(int peerFeatures){
            mPeerFeatures = peerFeatures;
        }

        public void printLog() {
            Log.d(TAG+"PeerHfFeatures","isThreeWayCallSupported()="+isThreeWayCallSupported()+"\n"+
                "isECNRSupported()="+isECNRSupported()+"\n"+
                "isVRSupported()="+isVRSupported()+"\n"+
                "isInBandToneSupported()="+isInBandToneSupported()+"\n"+
                "isVtagSupported()="+isVtagSupported()+"\n"+
                "isRejectIncomingSupported()="+isRejectIncomingSupported()+"\n"+
                "isECStatusSupported()="+isECStatusSupported()+"\n"+
                "isECControlSupported()="+isECControlSupported()+"\n"+
                "isEERCSupported()="+isEERCSupported()+"\n"+
                "isCodecNegotiationSupported()="+isCodecNegotiationSupported()+"\n"+
                "isVoipCallSupported()="+isVoipCallSupported()+"\n"+
                "isHSPConnection()= "+ isHSPConnection());
        }
    }

private boolean isEnabled() {
    if (mAdapter.getState() == BluetoothAdapter.STATE_ON) return true;
    return false;
 }

    private boolean isDisabled() {
        if (mAdapter.getState() == BluetoothAdapter.STATE_OFF) return true;
        return false;
    }

    private boolean isValidDevice(BluetoothDevice device) {
        if (device == null) return false;

        if (BluetoothAdapter.checkBluetoothAddress(device.getAddress())) return true;
        return false;
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }


}
