package cn.fatdeer.isocket.pub;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Intent;
import cn.fatdeer.isocket.CLog;
import cn.fatdeer.isocket.MainActivity;
import cn.fatdeer.isocket.MenuActivity;
import cn.fatdeer.isocket.apmode.WifiActivity;
import cn.fatdeer.isocket.chart.HistoryChartActivity;

/**
 * @author GD.lws
 * @version 20150429
 * @Discribe Dispatch the message between mobile and module
 * 
 *           at 20151121
 *            1. All broadcast be merged in broadCastToActivity();
 *           at 20160125
 *            1. prefix_AP change from esp8266 to esp8266_ ;
 *            2. new parameter ISSN_LENGTH for module's ssid length;
 *           at 20160127
 *            1. InitStage: add new status INIT(before shakehand with server);
 *           at 20160202
 *            1. 5 new parameter for AIMSETTING; 
 */
public class Const {
	private static String TAG = Const.class.getSimpleName();
	// 默认timeout 时间 60s
	public final static int SOCKET_TIMOUT = 60 * 1000;

	public final static int SOCKET_READ_TIMOUT = 15 * 1000;

	// 如果没有连接无服务器。读线程的sleep时间
	public final static int SOCKET_SLEEP_SECOND = 3;

	// 心跳包发送间隔时间
	public final static int SOCKET_HEART_SECOND = 30; // at 20150706 10; // at
														// 20150206 3 ;

	// IF no reply from module, after [RETRY_SEND_SECOND] , send package again
	// ,default is 5
	public final static int RETRY_SEND_SECOND = 5;

	// IF no reply from module, after [RETRY_SEND_TIMES] , stop send again
	// ,default is 5
	public final static int RETRY_SEND_TIMES = 3;

	// In SocketInputThread.java , How many times for
	// TCPClient.instance().reConnect()
	public final static int RETRY_CONNECT_TIME = 3;

	public final static String BC = "BC";
	public final static int TCP_INIT = 0; // TCP is inited
	public final static int TCP_CONN = 1; // TCP connection is OK
	public final static int TCP_LOGIN = 2;// Login is OK
	public final static boolean DEBUG_FLAG = true;// at 20150711
	// module's AP prefix, default=esp8266
	public final static String prefix_AP = "esp8266_";

	public final static int ISSN_LENGTH = 10; // e.g. IS21516001
	// TEA encrypt switch
	public final static boolean isEncrypt = true; // at 20150827

	public final static String SSL_PORT = "8020"; // at 20150921
	public final static String ESP8266_IP = "192.168.4.1";
	public final static String ESP8266_PORT = "8080";
	public final static byte[] KEY = new byte[] {// 加密解密所用的KEY
	0x63, 0x6A, 0x77, 0x72, // cjwr
			0x63, 0x72, 0x64, 0x6A, // crdj
			0x67, 0x6F, 0x75, 0x70, // goup
			0x78, 0x75, 0x65, 0x73 // xues
	};
	public final static String RSA_PRIVATE_KEY = "MIICdAIBADANBgkqhkiG9w0BAQEFAASCAl4wggJaAgEAAoGBALCzvhLq+4r2hiBzRQcpUKxEMJ886QFeghUz0MI0afYIkEhgrWbOuV2GgZdaWpuPuZLO8CIbOabvskBsaXwFst4REl+qpCeTOxwKxZJ5IHV65MdalS1k/SqmQF8yv+rMeI4eLcbxJXn3qEmQEp1JU03IAGguNHUU4Q5enkMqZsqxAgMBAAECf0UF8TlykM+3fK0wWcZyXRDtkhChumLOiAHAO8ugrcNtlO6w1QtTtRTTdcupf7tEaTjvnN21xM9w0jBYKNxJXZS6OWgsMsZvQZSGGmt/05HGueVsIwQhjqa0FUA1d0iWp96oPRfVClhrdB+BR0r+Hp8YoPFzVLuSJb6K8qol8cECQQDpJzAiF6o/S/m5ISGRs6b5X4N2w1GacfmgmnsPoNUGQskCJhLAIwWQn7bmuZffoyCpAWKb/Ziwh725ILnk7ZmdAkEAwgRxE8yfjCyJSDaBSBMH183IoSzlWG+WwTVeRSBib1NRG8CpxSXXEIIQNR6ojwg0P6LCzvPU9pNjXAnOJTHDJQJAag/mqIuladfxCROWRsa3/ZdCoaMmmyCgEzxTIP/kCE4XTL/vJbKI1IqjVIA3I4f80oULy7RoYl3No8rMZeTkTQJBAKVttBz1tHSI5OU11DXoRB+1zwPejA9D1n8XSjIW0tepatcHB7qZ8S6aN8eTRJXwoNxBo2libotwUKABnEwKiiECQEaVsS5CzJmJ3x9JumEnUP3FWB1SqcVIOgb+Niyl1MBcRo09m45PPThqR05GtCKIIGvo/T4tnxyfhTNilinlYUQ=";

	public static enum whichActivity {
        HISTORY, WIFI, MENU, MAIN
    }

	public static enum InitStage {
		INIT, LOOKUP,UPLOAD,JOIN,SETUP,OPENSTA,CHANGEWIFI
    }
//at 20160202
	public final static int AIMSETTING_MINT = 15; // min temperature user can set
	public final static int AIMSETTING_MAXT = 35; // max temperature user can set
	public final static int AIMSETTING_MINH = 50; // min humidity user can set
	public final static int AIMSETTING_MAXH = 90; // max temperature user can set

	public final static int NUMINTIMEZONE=6; // how many times in each Time zone(4)
//end 20160202	
	@SuppressLint("DefaultLocale")
	public static String Bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
			ret += " ";
		}
		return ret;
	}

	// end 20150828
	// at 20150922
//	public static void broadCastToMainActivity(String orderStr,
//			String requestStr, String responseStr) {
//		Intent intent = new Intent(Const.BC);
//		intent.putExtra("order", orderStr);
//		intent.putExtra("request", requestStr);
//		intent.putExtra("response", responseStr);
//		MainActivity.s_context.sendBroadcast(intent);
//	}
//at 20151120
//	public static void broadCastToWifiActivity(
//			String orderStr, String responseStr) {
//		Intent intent = new Intent(Const.BC);
//		intent.putExtra("order", orderStr);
//		intent.putExtra("response", responseStr);
//		WifiActivity.s_context.sendBroadcast(intent); // at 20150922
//	}
	public static void broadCastToActivity(String orderStr,String requestStr,
			String responseStr, whichActivity which) {
		Intent intent = new Intent(Const.BC);
		intent.putExtra("order", orderStr);
		intent.putExtra("request", requestStr);
		intent.putExtra("response", responseStr);
		if(which==whichActivity.WIFI)
			WifiActivity.s_context.sendBroadcast(intent);
		else if(which==whichActivity.HISTORY)
			HistoryChartActivity.s_context.sendBroadcast(intent);
		else if(which==whichActivity.MENU)
			MenuActivity.s_context.sendBroadcast(intent);
		else if(which==whichActivity.MAIN)
			MainActivity.s_context.sendBroadcast(intent);
	}
//end 20151120
	public static String analyzeEncrptedPacket(byte[] comeBytes, int comeLength) {
		// <NAN>...</NAN> or <TEA>...</TEA> or <RSA>...</RSA>
		byte[] headBytes = new byte[3];
		byte[] tailBytes = new byte[3];
		byte[] infoBytes = new byte[comeLength - 11];
		String encryptStr = null, infoStr = null;
		Date beginTime = new Date();
		if (comeBytes[0] != 0x3C || comeBytes[comeLength - 1] != 0x3E) {
			return infoStr;
		}
		for (int i = 0; i < 3; i++) {
			headBytes[i] = comeBytes[i + 1];
			tailBytes[2 - i] = comeBytes[comeLength - 2 - i];
		}
		if (headBytes[0] != tailBytes[0] || headBytes[1] != tailBytes[1]
				|| headBytes[2] != tailBytes[2]) {
			return infoStr;
		}
		for (int i = 0; i < comeLength - 11; i++) {
			infoBytes[i] = comeBytes[i + 5];
		}
		encryptStr = new String(headBytes);
		if (encryptStr.equals("NAN")) {
			try {
				infoStr = new String(infoBytes,"gbk");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				infoStr = null;
				broadCastToActivity("ERROR","analyzeEncrptedPacket(NAN):toString ERROR",null,Const.whichActivity.MAIN);
			}
		} 
		//at 20151123
		else if (encryptStr.equals("GZP")) {
			try {
				infoStr = new String(GZIP.unCompress(infoBytes),"gbk");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				infoStr = null;
				broadCastToActivity("ERROR","analyzeEncrptedPacket(GZP):toString ERROR",null,Const.whichActivity.MAIN);
			}
		}
		//end 20151123
		else if (encryptStr.equals("TEA")) {
			byte[] decryptBytes = new Encrypt().decrypt(infoBytes,
					comeLength - 11, Const.KEY);
			try {
				infoStr = new String(decryptBytes,"gbk");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				infoStr = null;
				broadCastToActivity("ERROR","analyzeEncrptedPacket(TEA):toString ERROR",null,Const.whichActivity.MAIN);
			}
		} else if (encryptStr.equals("RSA")) {
			try {
				infoStr = new String(RSAEncrypt.decryptByPrivateKey(infoBytes,
						Const.RSA_PRIVATE_KEY),"gbk");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				infoStr = null;
				broadCastToActivity("ERROR","analyzeEncrptedPacket(RSA):toString ERROR",null,Const.whichActivity.MAIN);
			}
		}
		Date endTime = new Date();
		CLog.i(TAG, "encrpyt times" + (endTime.getTime() - beginTime.getTime())
				+ "\r\n");

		return infoStr.trim();
	}

	public static byte[] str2EncryptedByte(String str, String encryType,
			String from) {
		byte[] sendByte;
		byte[] goByte;
		Date beginTime = new Date();
		// at 20150923 byte[] headBytes = new byte[5];
		int headLen = 6 + from.length();
		byte[] headBytes = new byte[headLen]; // <NAN:12345678901234567890>
		byte[] tailBytes = new byte[6];
		headBytes[0] = (byte) 0x3C; // <
		// at 20150923 headBytes[4] = (byte) 0x3E; // >
		headBytes[4] = (byte) 0x3A; // 0x3A=:
		for (int i = 0; i < from.length(); i++) {
			headBytes[i + 5] = (byte) from.charAt(i);
		}
		headBytes[headLen - 1] = (byte) 0x3E; // <NAN:note3>
		// end 20150923
		tailBytes[0] = (byte) 0x3C; // <
		tailBytes[1] = (byte) 0x2F; // /
		tailBytes[5] = (byte) 0x3E; // >

		for (int i = 0; i < 3; i++) {
			headBytes[i + 1] = (byte) encryType.charAt(i);
			tailBytes[i + 2] = (byte) encryType.charAt(i);
		}
		if (encryType.equals("TEA")) {
			sendByte = new Encrypt().encrypt(str.getBytes(),
					str.getBytes().length, Const.KEY);
		} else if (encryType.equals("RSA")) {
			try {
				sendByte = RSAEncrypt.encryptByPrivateKey(str.getBytes(),
						Const.RSA_PRIVATE_KEY);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sendByte = null;
			}
		} else {
			sendByte = str.getBytes();
		}

		// at 20150923
		// goByte = new byte[sendByte.length + 11];
		// for (int i = 0; i < 5; i++) {
		goByte = new byte[sendByte.length + headLen + 6];
		for (int i = 0; i < headLen; i++) {
			// end 20150923
			goByte[i] = headBytes[i];

		}
		for (int i = 0; i < sendByte.length; i++) {
			// at 20150923 goByte[i + 5] = sendByte[i];
			goByte[i + headLen] = sendByte[i];
		}
		for (int i = 0; i < 6; i++) {
			goByte[goByte.length - 1 - i] = tailBytes[5 - i];
		}
		Date endTime = new Date();
		CLog.i(TAG, "decrpyt times" + (endTime.getTime() - beginTime.getTime())
				+ "\r\n");

		return goByte;
	}
	// end 20150922
}
