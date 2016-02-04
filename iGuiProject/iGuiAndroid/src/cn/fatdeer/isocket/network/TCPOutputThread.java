package cn.fatdeer.isocket.network;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.fatdeer.isocket.CLog;
import cn.fatdeer.isocket.entity.Login;
import cn.fatdeer.isocket.entity.MsgEntity;
import cn.fatdeer.isocket.pub.Const;


/**
 * @author GD.lws
 * @version 20150313
 * @Discribe Client write message thread: This Class only Do one thing, send
 *           message to socket server
 * 
 *           at 20150313 After send message, no sleep 1 second
 *           at 20150429 
 *            1. broadcast activity request message; 
 *            2. try to add send retry;
 *           at 20150430
 *            1. now we can Retry send message ; 
 *           at 20150710
 *            1. make sure message send success, do not send again;
 */

public class TCPOutputThread extends Thread implements ISockOutputThread {
	private boolean isStart = true;
	private static String tag = TCPOutputThread.class.getSimpleName();
	private List<MsgEntity> sendMsgList;
	DateFormat df = new SimpleDateFormat("hh:mm:ss.SSS", Locale.CHINA);

	private Login mLogin;

	public TCPOutputThread(Login login) {
		this.mLogin=login;
		sendMsgList = new CopyOnWriteArrayList<MsgEntity>();
		this.start();//at 20150710
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
		synchronized (this) {
			notify();
		}
	}

	public void addMsgToSendList(MsgEntity msg) {
		synchronized (this) {
			Const.broadCastToActivity("INF", "SEND:" + msg.toString(), null,Const.whichActivity.MAIN);
			CLog.i(tag, "sendMsgList.add:" + msg.toString());
			this.sendMsgList.add(msg);
			notify();
		}
	}

	@Override
	public void run() {
		CLog.i(tag, "SocketOutputThread begin work");
		while (isStart) {
			CLog.i(tag, "sendMsgList.size()=" + sendMsgList.size());
			// 锁发送list
			synchronized (sendMsgList) {
				// 发送消息
				for (MsgEntity msg : sendMsgList) {
					String message = new String(msg.getBytes());
					Date currTime = new Date();
					CLog.i(tag, "####");
					CLog.i(tag, "currTime=" + df.format(currTime));
					CLog.i(tag, "msg=" + message);
					CLog.i(tag, "msg.getSendTime()=" + df.format(msg.getSendTime()));
					if (msg.getTryTimes() > 0 && msg.isComplete()) {
						sendMsgList.remove(msg);
						continue;
					}
					if (msg.getSendTime().getTime() <= currTime.getTime()) {
						msg.trySend();
						TCPClient.instance(mLogin).sendMsg(
								Const.str2EncryptedByte(msg.toString(), "NAN",
										mLogin.getName()));
						msg.setSendDelay(Const.RETRY_SEND_SECOND);
						// Try send 3 times
						if (msg.getTryTimes() >= Const.RETRY_SEND_TIMES) {
							Const.broadCastToActivity("WARN", null,
									"Send order Fail:" + message,Const.whichActivity.MAIN);
							msg.setComplete();
						}
						if (msg.isComplete())
							sendMsgList.remove(msg);
					}
				}
			}

			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					Const.broadCastToActivity("ERROR", null,
							"TCPOutputThread.wait():InterruptedException",Const.whichActivity.MAIN);
				}// After sending message Thread in waiting
			}
		}

		CLog.i(tag, "SocketOutputThread is over!");
	}

}
