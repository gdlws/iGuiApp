package cn.fatdeer.isocket.apmode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;
import cn.fatdeer.isocket.CLog;
import cn.fatdeer.isocket.pub.Const;

/**
 * @author GD.lws
 * @version 20151217
 * @Discribe Dispatch the message between mobile and Server (UDP)
 * 
 *           at 20151217
 */
public class UDPHelper {
	private static String tag = UDPHelper.class.getSimpleName();

	private DatagramSocket udpSocket;
	private InetAddress mIP = null;
	private int mPort = -1;
	private String mFrom = null;

	public UDPHelper(String ip, String port, String from) {
		try {
			this.mIP = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.mPort = Integer.parseInt(port);
		this.mFrom=from;
		CLog.i(tag, "ip=" + ip + ";port=" + port);

	}

	public boolean openSocket() {

		try {
			udpSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(tag, "openSocket():Connection Exception: " + e.getMessage());
			this.closeSocket();
			return false;
		}

		return true;
	}

	public void closeSocket() {
		if (udpSocket != null) {
			this.udpSocket.close();
			this.udpSocket = null;
		}
	}

	public void sendMessage(String inf) {
		byte[] sendByte;
		DatagramPacket updPacket;
		try {
			CLog.e(tag, "sendMessage to " + this.mIP + ":" + this.mPort + ":"
					+ inf + "\r\n");
			sendByte = Const.str2EncryptedByte(inf, "NAN", this.mFrom);
			updPacket = new DatagramPacket(sendByte, sendByte.length, this.mIP,
					this.mPort);
			if (udpSocket != null)
				udpSocket.send(updPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String rcvMessage() {

		CLog.e(tag, "waiting Server's response \r\n");
		String rtnStr = null;
		DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
		try {
			udpSocket.setSoTimeout(15000); //at 20160126
			udpSocket.receive(dp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int byteLen = dp.getLength();
		byte[] rcvByte = new byte[byteLen];
		rcvByte = dp.getData();
		if (rcvByte[0] == 0x00) {
			CLog.e(tag, "rcv zero \r\n");
			return null;
		}
//		if (rcvByte[0] != '<' || rcvByte[byteLen - 1] != '>') {
//			CLog.e(tag, "not begin with < and end with > \r\n");
//			return null;
//		}

		rtnStr = Const.analyzeEncrptedPacket(rcvByte, byteLen);

		return rtnStr;

	}

}
