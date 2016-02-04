package cn.fatdeer.isocket.network;

import cn.fatdeer.isocket.CLog;
import cn.fatdeer.isocket.entity.Login;
import cn.fatdeer.isocket.pub.Const;
import cn.fatdeer.isocket.pub.DoJson;


/**
 * @author GD.lws
 * @version 20150926
 * @Discribe Each 10 second this Class send HeartBeat message to Server
 * 
 *           at 20150313 use SocketThreadManager.sendMsg() to send HeartBeat
 *           at 20150427 1. after Login Status, send heartbeat package;
 *           at 20150428 
 *            1. Remove function SocketHeartThread() ; 
 *           at 20150605
 *            1. new Function closeConnect(); 
 *           at 20150926
 *            1. heartbeat packet encrypt by Const.java; 
 */

public class TCPHeartThread extends Thread implements ISockHeartThread{
	static final String tag = TCPHeartThread.class.getSimpleName();

	private boolean isStart = true;
	
	private Login mLogin;

	public TCPHeartThread(Login login) {
		this.mLogin=login;
		TCPClient.instance(mLogin);
		this.start();
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}


	public void run() {
		CLog.i(tag, "SocketHeartThread begin work");
		while (isStart) {
			if(TCPClient.instance(this.mLogin).isConnect()) {
				byte[] sendByte = Const.str2EncryptedByte(
						DoJson.instance().toJSon("SEND", this.mLogin.getName(), "ALL", "GET+S"), "NAN", mLogin.getName());
				TCPClient.instance(this.mLogin).sendMsg(sendByte);
			}
			try {
				// each 30 seconds send heartbeat packet
				Thread.sleep(Const.SOCKET_HEART_SECOND * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Const.broadCastToActivity("ERROR", null, "TCPClient.closeTCPSocket():InterruptedException-Thread.sleep()",Const.whichActivity.MAIN);
			}
		}

		CLog.i(tag, "SocketHeartThread is over!");
	}
	
	public void closeConnect() {
//		TCPClient.instance(mHostIP,mHostPort).setTCPStatus(Const.TCP_INIT);
		TCPClient.instance(mLogin).closeTCPSocket();
	}

}
