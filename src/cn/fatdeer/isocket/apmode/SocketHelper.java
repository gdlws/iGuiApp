package cn.fatdeer.isocket.apmode;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import android.util.Log;
import cn.fatdeer.isocket.CLog;
import cn.fatdeer.isocket.pub.Const;

/**
 * @author GD.lws
 * @version 20150923
 * @Discribe Dispatch the message between mobile and module (TCP)
 * 
 *           at 20150922
 *            1. HOST&PORT move to Const.java;
 *           at 20150923
 *            1. getMessage() set timeout = 6 seconds; 
 *            2. sendMessage() do not send new line code; 
 */
public class SocketHelper {
	private static String tag = SocketHelper.class.getSimpleName();

	Socket socket = null;
	DataInputStream in =null;
	PrintWriter out = null;
	boolean isConnect = false; 
	String mIP=null;
	String mPort=null;
	
	public SocketHelper(String ip,String port) {
		this.mIP=ip;
		this.mPort=port;
		CLog.i(tag, "ip="+ip+";port="+port);

	}

	public boolean openSocket() {

		try {
			socket = new Socket(this.mIP, Integer.parseInt(this.mPort));
			in=new DataInputStream(socket.getInputStream());
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream())), true);
		} catch (IOException ex) {
			Log.e(tag, "openSocket():Connection Exception: " + ex.getMessage());
			this.closeSocket();
			return false;
		}
		isConnect = true;
		return true;
	}

	public void closeSocket() {
		try {
			isConnect=false;
			if(in!=null) in.close();
			if(out!=null) out.close();
			in=null;
			out=null;
			if(socket!=null) socket.close();
			socket = null;
		} catch (IOException ex) {
			Log.e(tag, "closeSocket():Connection Exception: " + ex.getMessage());
		}
	}

	public void sendMessage(String msgToServer) {
		if (socket.isConnected()) {
			if (!socket.isOutputShutdown()) {
				out.print(msgToServer);
				out.flush();
			}
		}
	}

	public String rcvMessage() {
		try {
			socket.setSoTimeout(5*1000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "set timeout fail";
		}
		byte[] incomeByte=new byte[255];
		byte[] infoByte;
		try {
			int rcvNum=in.read(incomeByte,0,255);
			infoByte=new byte[rcvNum];
			for(int i=0;i<rcvNum;i++) infoByte[i]=incomeByte[i];
			Log.i(tag, "rcvNum= " + rcvNum+";infoByte="+Const.Bytes2HexString(infoByte));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "receive message fail";
		}
		return new String(infoByte);
	}

	
}
