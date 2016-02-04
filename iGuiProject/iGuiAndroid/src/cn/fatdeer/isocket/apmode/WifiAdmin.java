package cn.fatdeer.isocket.apmode;

import java.util.List;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import cn.fatdeer.isocket.pub.Const;


/**
 * @author GD.lws
 * @version 20150925
 * @Discribe This Class is for Connect esp8266's AP Mode 
 * 
 *           at 20150501
 *            1. search prefix = esp8266 HotSpot; 
 *           at 20150925
 *            1. getIPAddress() return true IP String (before int); 
 *            2. lookUpScan(): call refreshWifiInfo() at first part; 
 *            3. remove com.example.wificonnection.RECEIVER;
 *            4. inform WifiActivity PROGRESS DONE; 
 *           at 20160126
 *            1. lookUpScan(): return one row; 
 * 
 */

public class WifiAdmin extends ContextWrapper {
	private static final String TAG = WifiAdmin.class.getSimpleName();
	
	private WifiManager mWifiManager;
	private WifiInfo mWifiInfo;
	// Scan List of NetWork Connection 
	private List<ScanResult> mWifiList;
	private List<WifiConfiguration> mWifiConfiguration;
	// LastConnection's Information
	private WifiConfiguration mLastWifiConfig; 
	private String mLastSSID = ""; 
	private String mSSID = "";

	// 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	}

//	private Intent intent = new Intent("com.example.wificonnection.RECEIVER");

	private Context mContext = null;

	public WifiAdmin(Context context) {
		super(context);
		mContext = context;
		refreshWifiInfo();

		mSSID = mWifiInfo.getSSID();
		if (mSSID != null) {
			mLastWifiConfig = IsExsits(mSSID);
			mLastSSID = mSSID;
		}
		if (mLastWifiConfig == null)
			Log.v(TAG, "mLastWifiConfig = NULL");
		else
			Log.v(TAG, "mLastWifiConfig.ssid = " + mLastWifiConfig.SSID);
//		intent.putExtra("msg", "wifi:" + mSSID);
//		sendBroadcast(intent);
	}

	private void refreshWifiInfo() {
		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		mWifiInfo = mWifiManager.getConnectionInfo();
		startScan();
	}

	public void startScan() {
		mWifiManager.startScan();
		mWifiList = mWifiManager.getScanResults();
		mWifiConfiguration = mWifiManager.getConfiguredNetworks();
	}
	
	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	public void closeWifi() {
		Thread thread = new Thread(new closeWifiRunnable());
		thread.start();
	}
	
	class closeWifiRunnable implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mWifiManager.isWifiEnabled()) {
				mWifiManager.setWifiEnabled(false);
				while (checkState() != 1) {
				}
//at 20150925				
//				intent.putExtra("progress", 100);
//				sendBroadcast(intent);
				Const.broadCastToActivity("PROGRESS", null,"SUCC", Const.whichActivity.WIFI);
//end 20150925
			}
		}

	}

	@Override
	protected void finalize() {
		try {
			super.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * remark: SSID is surrounded by ""
	 * @param SSID
	 * @return
	 */
	private WifiConfiguration IsExsits(String SSID) {
		refreshWifiInfo();
		if (mWifiConfiguration == null) {
			Log.d(TAG, "mWifiConfiguration is NULL");
			return null; // at 20150225
		}
		Log.d(TAG, "mWifiConfiguration.size() == " + mWifiConfiguration.size());
		Log.d(TAG, "SSID=" + "\"" + SSID + "\"");
		for (WifiConfiguration existingConfig : mWifiConfiguration) {
			Log.d(TAG, "existingConfig.SSID=" + existingConfig.SSID);
			if (existingConfig.SSID.equals(SSID)) {
				Log.d(TAG, "FOUND: existingConfig:" + SSID);
				return existingConfig;
			}
		}
		return null;
	}

//	public void disconnectWifi(int netId) {
//		mWifiManager.disableNetwork(netId);
//		mWifiManager.disconnect();
//	}

	// 检查当前WIFI状态
	public int checkState() {
		return mWifiManager.getWifiState();
	}

	// // Lock 
	// public void acquireWifiLock() {
	// mWifiLock.acquire();
	// }

	// // UnLock
	// public void releaseWifiLock() {
	// // 判断时候锁定
	// if (mWifiLock.isHeld()) {
	// mWifiLock.acquire();
	// }
	// }
	//
	// // Create Lock
	// public void creatWifiLock() {
	// mWifiLock = mWifiManager.createWifiLock("Test");
	// }

	// 
	// public List<WifiConfiguration> getConfiguration() {
	// return mWifiConfiguration;
	// }

	// 
	// public void connectConfiguration(int index) {
	// // 
	// if (index > mWifiConfiguration.size()) {
	// return;
	// }
	// // 
	// mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
	// true);
	// }
	// 
	// public List<ScanResult> getWifiList() {
	// return mWifiList;
	// }

	public StringBuilder lookUpScan() {
		refreshWifiInfo();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("mWifiConfiguration.size="
				+ mWifiConfiguration.size());
		stringBuilder.append("\n");
		for (int i = 0; i < mWifiList.size(); i++) {
			stringBuilder.append((mWifiList.get(i)).SSID);
			stringBuilder.append(": ");
			stringBuilder.append((mWifiList.get(i)).level);
			stringBuilder.append("@");
		}

		return stringBuilder;
	}

	public String lookUpScan(String prefixStr) {
		refreshWifiInfo();
		Log.d(TAG, "Enter lookUpScan()");
		String wifiStr=null;

		for (int i = 0; i < mWifiList.size(); i++) {
			if(mWifiList.get(i).SSID.indexOf(prefixStr)>=0) {
				wifiStr=mWifiList.get(i).SSID;
				Log.d(TAG, "i:" + i);
				Log.d(TAG, "SSID:" + mWifiList.get(i).SSID);
				Log.d(TAG, "level:" + mWifiList.get(i).level);
				break; //at 20160126
			}
		}
		return wifiStr;
	}

	public String getSSID() {
		refreshWifiInfo();
//at 20160126		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
		return (mWifiInfo == null) ? "NA" : mWifiInfo.getSSID();
	}
	public String getIPAddress() {
//at 20150925		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
		if(mWifiInfo == null) return null;
		String ipString = 
				((mWifiInfo.getIpAddress() & 0xff) + "." + (mWifiInfo.getIpAddress() >> 8 & 0xff) + "."   
		        + (mWifiInfo.getIpAddress() >> 16 & 0xff) + "." + (mWifiInfo.getIpAddress() >> 24 & 0xff));  
		return ipString;
	}

	private WifiConfiguration createWifiInfo(String SSID, String Password,
			WifiCipherType Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		// nopass
		if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		// wep
		if (Type == WifiCipherType.WIFICIPHER_WEP) {
			if (!TextUtils.isEmpty(Password)) {
				if (isHexWepKey(Password)) {
					config.wepKeys[0] = Password;
				} else {
					config.wepKeys[0] = "\"" + Password + "\"";
				}
			}
			config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
			config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
			config.allowedKeyManagement.set(KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		// wpa
		if (Type == WifiCipherType.WIFICIPHER_WPA) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			// 此处需要修改否则不能自动重联
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}

	// 提供一个外部接口，传入要连接的无线网
	public void connect(String ssid, String password, WifiCipherType type) {
		Thread thread = new Thread(new ConnectRunnable(ssid, password, type));
		thread.start();
	}

	class ConnectRunnable implements Runnable {
		private String ssid;
		private String password;
		private WifiCipherType type;

		public ConnectRunnable(String ssid, String password, WifiCipherType type) {
			this.ssid = ssid;
			this.password = password;
			this.type = type;
		}

		@Override
		public void run() {
			Log.v(TAG, "ready to open wifi!");
			// 打开wifi
			openWifi();
			Log.v(TAG, "wifi opened!");
			// 开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
			// 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
			while (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
				try {
					// 为了避免程序一直while循环，让它睡个100毫秒检测……
					Thread.sleep(100);
				} catch (InterruptedException ie) {
				}
				Log.v(TAG, "wifi in waiting enable!");
			}
			Log.v(TAG, "wifi is ready!");

			WifiConfiguration wifiConfig = createWifiInfo(ssid, password, type);
			//
			if (wifiConfig == null) {
				Log.d(TAG, "wifiConfig is null!");
				return;
			}

			WifiConfiguration tempConfig = IsExsits("\""+ssid+"\"");
			if (tempConfig != null) {
				mWifiManager.removeNetwork(tempConfig.networkId);
			}
			int netID = mWifiManager.addNetwork(wifiConfig);
			boolean enabled = mWifiManager.enableNetwork(netID, true);
			Log.d(TAG, "enableNetwork status enable=" + enabled);
			boolean connected = mWifiManager.reconnect();
			Log.d(TAG, "enableNetwork connected=" + connected);
			while (!isWifiConnected("\""+ssid+"\"")) {
				Log.d(TAG, "WIFI is in init");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}

			}
			Log.v(TAG, "wifi is OK!");
//at 20150925			intent.putExtra("progress", 100);
			mWifiInfo = mWifiManager.getConnectionInfo();
			refreshWifiInfo();
//at 20150925
//			intent.putExtra("msg", "wifi:" + mWifiInfo.getSSID());
//			sendBroadcast(intent);
			Const.broadCastToActivity("PROGRESS", null,"SUCC", Const.whichActivity.WIFI);
//end 20150925
		}
	}

	public void reconnect() {
		Thread thread = new Thread(new ReConnectRunnable());
		thread.start();
	}

	class ReConnectRunnable implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int netID = mWifiManager.addNetwork(mLastWifiConfig);
			boolean enabled = mWifiManager.enableNetwork(netID, true);
			boolean connected = mWifiManager.reconnect();
			Log.d(TAG, "enableNetwork status enable=" + enabled);
			Log.d(TAG, "enableNetwork connected=" + connected);
			while (!isWifiConnected(mLastSSID)) {
				Log.d(TAG, "WIFI is in init");
				try {
					// 为了避免程序一直while循环，让它睡个100毫秒检测……
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}

			}

			Log.v(TAG, "wifi is OK!");
//at 20150925			intent.putExtra("progress", 100);
			mWifiInfo = mWifiManager.getConnectionInfo();
			refreshWifiInfo();
//at 20150925
//			intent.putExtra("msg", "wifi:" + mWifiInfo.getSSID());
//			sendBroadcast(intent);
			Const.broadCastToActivity("PROGRESS", null,"SUCC", Const.whichActivity.WIFI);
//end 20150925
		}

	}

	/**
	 * 20150228 by GD.lws 判断WIFI网络是否可用
	 * remark ssid is surrounded by ""
	 * @return
	 */
	public boolean isWifiConnected(String ssid) {
		if (mContext != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				// at 20150228
				// return mWiFiNetworkInfo.isAvailable();
				if (mWiFiNetworkInfo.isAvailable()) {
					refreshWifiInfo();
					Log.v(TAG, "ssid=" + ssid);
					Log.v(TAG, "mWifiInfo.getSSID()=" + mWifiInfo.getSSID());
					if (ssid.equals(mWifiInfo.getSSID())) {
						// at 20150228
						mLastSSID = mSSID;
						mLastWifiConfig = IsExsits(mSSID);
						mSSID = ssid;
						Log.v(TAG, "mLastSSID=" + mLastSSID);
						Log.v(TAG, "mSSID=" + mSSID);
						if (mLastWifiConfig == null)
							Log.v(TAG, "mLastWifiConfig = NULL");
						else
							Log.v(TAG, "mLastWifiConfig.ssid = " + mLastWifiConfig.SSID);
						// end 20150228
						
						return true;
					}
				}
				// end 20150228
			}
		}
		return false;
	}

	private static boolean isHexWepKey(String wepKey) {
		final int len = wepKey.length();

		// WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
		if (len != 10 && len != 26 && len != 58) {
			return false;
		}

		return isHex(wepKey);
	}

	private static boolean isHex(String key) {
		for (int i = key.length() - 1; i >= 0; i--) {
			final char c = key.charAt(i);
			if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
					&& c <= 'f')) {
				return false;
			}
		}

		return true;
	}
	 
}
