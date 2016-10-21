package android.widget;

import java.util.List;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WifiAdmin {
	
    private final static String TAG = "WifiAdmin-Android";
    private final static boolean DEBUG = true;
    private WifiManager mWifiManager;
    private WifiManager.ActionListener mConnectListener;
    private StateListener mStateListener;
    private boolean mNeedWakeLock;
    private WakeLock mWakeLock = null;
    private String mCurrentSSID = "";
    private Context mContext;
    private static WifiAdmin sInstance;

    public enum WifiCipherType {
	WIFICIPHER_NOPASS, WIFICIPHER_WPA, WIFICIPHER_WEP, WIFICIPHER_INVALID, WIFICIPHER_WPA2
	    }

	
    /** Interface for current action state callback */
    public interface StateListener {
            public static final int SUCCESS = 0;
        public static final int F_OTHERS = 1;
        public static final int F_AUTH = 2;
        /** The operation succeeded */
        public void onCurrent(String ssid);
        /**
         * The operation failed
         * @param reason The reason for failure could be one of
         * {@link #ERROR}, {@link #IN_PROGRESS} or {@link #BUSY}
         */
        public void onFailure(String ssid,int reason);
    }

    public static WifiAdmin getInstance(Context c,boolean needWakeLock) {
	if (null == sInstance)
	    sInstance = new WifiAdmin(c,needWakeLock);
	return sInstance;
    }

    public boolean connect(String SSID, String Password, String security,StateListener listener) {
	WifiCipherType type = WifiCipherType.WIFICIPHER_INVALID;
	if (security.equals("NONE")) {
	    type = WifiCipherType.WIFICIPHER_NOPASS;
	}
	else if (security.equals("WPA_EAP")) {
	    type = WifiCipherType.WIFICIPHER_WEP;
	}
	else if (security.equals("WPA_PSK") || security.equals("WPA2_PSK")) {
	    type = WifiCipherType.WIFICIPHER_WPA;
	} 
	return connect(SSID,Password,type ,listener);
    }

    public boolean connect(String SSID, String Password, WifiCipherType Type,StateListener listener) {
	Log.i(TAG, "connect:SSID=" + SSID+" Password="+Password+" Type="+Type+" listener="+listener);

	  /*close wifi ap*/
	int wifiApState = mWifiManager.getWifiApState();
	if (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED || 
	    wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) {
	    mWifiManager.setWifiApEnabled(null, false);
	}

	  /*open wifi*/
	if (!this.openWifi()) return false;
	while (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
	    try {				
		Thread.currentThread();
		Thread.sleep(100);
	    } catch (InterruptedException ie) {
	    }
	}

	  /*connect wifi ap*/
	WifiConfiguration wifiConfig = createWifiInfo(SSID, Password, Type);
		
	if (wifiConfig == null) {
	    Log.e(TAG,"createWifiInfo failed");
	    return false;
	}
	mCurrentSSID = SSID;
	mStateListener = listener;
	removeSameSSIDActivePoint();
	disableCurrentActivePoint();
	mWifiManager.connect(wifiConfig, mConnectListener);
	return true;
    }

    public String getLocalIpAddress() {
	WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
	if (wifiInfo != null && wifiInfo.getIpAddress() != 0) {
	    int ipAddress = wifiInfo.getIpAddress();
	    String ipAddr = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
					  (ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),
					  (ipAddress >> 24 & 0xff)).toString();
	    Log.d(TAG, "Glass's wifi ip = " + ipAddr);
	    return ipAddr;
	}
	return null;
    }

    public boolean isConnected(String ssid) {
	ConnectivityManager connectivityManager = (ConnectivityManager)mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
	NetworkInfo netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	if(netInfo == null || null == netInfo.getExtraInfo()) 
	    return false;
	String extraInfo = netInfo.getExtraInfo();
	  //getExtraInfo()大部分情况内容是"ssid"，例如"test1",而不是text1.
	String currentWifiName = extraInfo.substring(1,extraInfo.length()-1);
	if(netInfo.isConnected() && currentWifiName.equals(ssid)) {
	    if(DEBUG) Log.d(TAG,"isConnected true");
	    return true;
	}
	if(DEBUG) Log.d(TAG,"isConnected false");
	return false;
    }


    public boolean closeWifi() {
	if(DEBUG) Log.d(TAG,"closeWifi in");
	if (mWifiManager.isWifiEnabled()) {
	    return mWifiManager.setWifiEnabled(false);
	}
	return false;
    }

    private boolean openWifi() {
	if(DEBUG) Log.d(TAG,"openWifi in");
	if (!mWifiManager.isWifiEnabled()) {
	    return mWifiManager.setWifiEnabled(true);
	}
	return true;
    }

      /*unused*/
    private WifiConfiguration isExsits(String SSID) {
	List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
	if(existingConfigs == null){
	    return null;
	}
	for (WifiConfiguration existingConfig : existingConfigs) {
	    if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
		Log.e(TAG, "----" + existingConfig.networkId);
		return existingConfig;
	    }
	}
	return null;
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password,
					     WifiCipherType Type) {
	WifiConfiguration config = new WifiConfiguration();
	config.allowedAuthAlgorithms.clear();
	config.allowedGroupCiphers.clear();
	config.allowedKeyManagement.clear();
	config.allowedPairwiseCiphers.clear();
	config.allowedProtocols.clear();
		
	config.priority = 40;
		
	config.SSID = "\"" + SSID + "\"";
	if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
	    config.wepKeys[0] = "\"\"";
	    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	    config.wepTxKeyIndex = 0;
	}
	else if (Type == WifiCipherType.WIFICIPHER_WEP) {
	    config.preSharedKey = Password;
	    config.hiddenSSID = true;
	    config.allowedAuthAlgorithms
		.set(WifiConfiguration.AuthAlgorithm.SHARED);
	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
	    config.allowedGroupCiphers
		.set(WifiConfiguration.GroupCipher.WEP104);
	    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	    config.wepTxKeyIndex = 0;
	}
	else if (Type == WifiCipherType.WIFICIPHER_WPA || 
		 Type == WifiCipherType.WIFICIPHER_WPA2) {
	    config.preSharedKey = "\"" + Password + "\"";
	    config.hiddenSSID = true;
	    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	    config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	      // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	    config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

	}
	else {
	    return null;
	}
	return config;
    }

    private void removeSameSSIDActivePoint(){
    	List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
	if (configs != null) {
	    for (WifiConfiguration config : configs) {
		String ssid = config.SSID.substring(1, config.SSID.length()-1);
		if (mCurrentSSID.equals(ssid)) {
		    Log.d(TAG,"remove same config ssid="+config.SSID+"  status="+config.status);
		    mWifiManager.removeNetwork(config.networkId);
		}
	    }
	}
    }

    private void disableCurrentActivePoint(){
	List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
	if (configs != null) {
	    for (WifiConfiguration config : configs) {
		if (config.status == WifiConfiguration.Status.CURRENT) {
		Log.d(TAG,"disable config ssid="+config.SSID+"  status="+config.status);
		    mWifiManager.disableNetwork(config.networkId);
		}
	    }
	}
    }

    private BroadcastReceiver mWifiStateBroadcastReceiver = new BroadcastReceiver() {
	    @Override
		public void onReceive(Context context, Intent intent) {
		Log.d(TAG,"onReceive: "+intent.getAction());
		if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
		    NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		    if(DEBUG) Log.d(TAG, " netInfo= "+netInfo);
		    if(netInfo.getState() == NetworkInfo.State.CONNECTED){
			notifyState(StateListener.SUCCESS);
			if(mWakeLock == null && mNeedWakeLock == true){
			    PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
			    mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, mContext.getClass().getName());
			    mWakeLock.acquire();
			}

		    }else if(netInfo.getState() == NetworkInfo.State.DISCONNECTED){
		        WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
		        if (2 == wifiInfo.getReason()) {
		                notifyState(StateListener.F_AUTH);
		        } else {
		                notifyState(StateListener.F_OTHERS);
		        }
			if(mWakeLock != null){
			    mWakeLock.release();
			    mWakeLock = null;
			}
		    }
		}
	    }
	};


      /*callback current ssid state to listener*/
    private void notifyState(int reason){
	if(mStateListener == null){
	    Log.w(TAG,"mStateListener is null");
	    return;
	}
	List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
	if (configs == null){
	    Log.w(TAG,"getConfiguredNetworks list is null");
	    return;
	}

	for (WifiConfiguration config : configs) {
	    String ssid = config.SSID.substring(1, config.SSID.length()-1);
	    if (mCurrentSSID.equals(ssid)) {
		if(config.status == WifiConfiguration.Status.CURRENT)
		    mStateListener.onCurrent(mCurrentSSID);
		else if(config.status == WifiConfiguration.Status.DISABLED)
		    mStateListener.onFailure(mCurrentSSID, reason);

		break;
	    }
	}
    }

    private WifiAdmin(Context context,boolean needWakeLock) {
	mContext = context;
	mNeedWakeLock = needWakeLock;
	mWifiManager = (WifiManager)mContext.getSystemService(mContext.WIFI_SERVICE);
	mConnectListener = new WifiManager.ActionListener() {//mConnectListener
		@Override
		    public void onSuccess() {
		    if(DEBUG) Log.d(TAG,"onsuccess"); 
		      /*
		       *note:This may result in the asynchronous delivery of state change
		       */
		}
			
		@Override
		    public void onFailure(int reason) {
		    Log.e(TAG,"connect wifi ap failed!");
		    if(mStateListener != null) mStateListener.onFailure(mCurrentSSID,reason);
		}
	    };
	IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
	mContext.registerReceiver(mWifiStateBroadcastReceiver, filter);
    }

}
