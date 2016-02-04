package cn.fatdeer.isocket.network;

import cn.fatdeer.isocket.CLog;
import cn.fatdeer.isocket.entity.Login;
import cn.fatdeer.isocket.entity.Message4JSON;
import cn.fatdeer.isocket.pub.Const;
import cn.fatdeer.isocket.pub.DoJson;


/**
 * @author GD.lws
 * @version 20150313
 * @Discribe This Class receive message from server in a loop and inform
 *           activity by msgEntity's Handle;
 * 
 *           at 20150428 
 *            1. manage TCPClient's Status here;
 *           at 20150429
 *            1. new function broadCastToActivity() to inform Activity; 
 *            2. init NetWork retry 3 times(default); 
 *           at 20150518
 *            1. ISO-8859-1 to GBK, for Chinese word; 
 *           at 20150706
 *            1. SUCC:CONN change to CONN:SUCC;
 *           at 20150926
 *            1. income byte[] decrypt by Const.java; 
 * 
 */
public class TCPInputThread extends Thread implements ISockInputThread{
	private boolean isStart = true;

	private static String tag = TCPInputThread.class.getSimpleName();

	private Login mLogin;
	
	public TCPInputThread(Login login) {
		this.mLogin=login;
		this.start();//at 20150710
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
		if(!isStart) TCPClient.instance(this.mLogin).closeTCPSocket(); //at 20151121
	}

	@Override
	public void run() {
		CLog.i(tag, "SocketInputThread begin work");
		while (isStart) {
			// mobile can connect to internet
			if (NetManager.instance().isNetworkConnected()) {
				if (!TCPClient.instance(this.mLogin).isConnect()) {
					for(int i=0;i<Const.RETRY_CONNECT_TIME;i++) {
						TCPClient.instance(this.mLogin).reConnect();
						Const.broadCastToActivity("WARN",null,"TCPInputThread.run():Reconnect Server",Const.whichActivity.MAIN);
						if(TCPClient.instance(this.mLogin).isConnect()) {
//at 20151121							this.isStart=true;
							break;
						}
						if(i==Const.RETRY_CONNECT_TIME-1) {
							this.isStart=false;
							Const.broadCastToActivity("ERROR",null,"TCPInputThread.run():Can not connect Server,try later please.",Const.whichActivity.MAIN);
						}
					}
				} else {
					CLog.i(tag, "NetWork OK!");
//at 20150927					readSocket();
					String rcvStr=TCPClient.instance(this.mLogin).readSocket();
					Message4JSON msg = DoJson.instance().fromJson(rcvStr);
					CLog.i(tag, "msg=" + msg);
					if (msg != null) {
						Const.broadCastToActivity(msg.getToOrder(), null, msg.getuMSG(),Const.whichActivity.MAIN);
					} else {
						Const.broadCastToActivity("WARN",null,"TCPInputThread.run():no json"+rcvStr,Const.whichActivity.MAIN);
					}
					
				}
			}
			else {
//				CLog.i(tag, "reConnect to Server");
//				TCPClient.instance().reConnect();
				Const.broadCastToActivity("ERROR",null,"TCPInputThread.run():broken network",Const.whichActivity.MAIN);
			}
		}
		CLog.i(tag, "SocketInputThread is over!");
	}

//	private void readSocket() {
//		Selector selector = TCPClient.instance(mHostIP,mHostPort).getSelector();
//		if (selector == null) {
//			CLog.i(tag, "selector is null!");
//			return;
//		}
//		int select=0;
//		try {
//			select=selector.select();//no data, block here
//		} catch (IOException e1) {
//			e1.printStackTrace();
//			Const.broadCastToMainActivity("ERROR", null, "readSocket():IOException-selector.select()");
//		} 
//		while(select>0) {
//			for (SelectionKey sk : selector.selectedKeys()) {
//				// If SelectionKey's Channel had readable data
//				if (sk.isReadable()) {
//					// use NIO read data in the Channel
//					SocketChannel sc = (SocketChannel) sk.channel();
//					ByteBuffer buffer = ByteBuffer.allocate(1024);
//					try {
//						sc.read(buffer);
//					} catch (IOException e) {
//						e.printStackTrace();
//						Const.broadCastToMainActivity("ERROR", null,
//								"readSocket():IOException-sc.read(buffer)");
//					}
//					buffer.flip();
//					String receivedString = "";
//					// From buffer to byte[]
//					byte[] content = new byte[buffer.limit()];
//					buffer.get(content);
//					receivedString = Const.analyzeEncrptedPacket(content,
//							content.length);
//					if (receivedString.length() > 0) {
//						CLog.i(tag, "From Server: " + receivedString);
//						if (receivedString.indexOf("}{") < 0) {
//							processOrder(receivedString);
//						} else {
//							processOrders(receivedString);
//						}
//					}
//					buffer.clear();
//					buffer = null;
//
//					try {
//						// Prepare for next request
//						sk.interestOps(SelectionKey.OP_READ);
//						// remove processing SelectionKey
//						selector.selectedKeys().remove(sk);
//					} catch (CancelledKeyException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//	}
//
//	private void processOrders(String messageStr) {
//		int pos = 0;
//		String thisOrderStr;
//
//		while (messageStr.indexOf("}{", pos) >= 0) {
//
//			thisOrderStr = messageStr.substring(pos,
//					messageStr.indexOf("}{", pos) + 1);
//			processOrder(thisOrderStr);
//			pos = messageStr.indexOf("}{", pos);
//			pos++;
//			if (messageStr.indexOf("}{", pos) < 0) {
//				thisOrderStr = messageStr.substring(pos, messageStr.length());
//				processOrder(thisOrderStr);
//			}
//		}
//	}
//
//	private void processOrder(String inf) {
//		Message4JSON msg = DoJson.instance().fromJson(inf);
//		CLog.i(tag, "msg=" + msg);
//		CLog.i(tag, "TCPClient.instance().getTCPStatus()="
//				+ TCPClient.instance(mHostIP, mHostPort).getTCPStatus());
//		if (msg != null) {
//
//			String value = msg.getuMSG();
//			String toActivityStr = value;
//			switch (TCPClient.instance(mHostIP, mHostPort).getTCPStatus()) {
//
//			case Const.TCP_INIT:
//				if (msg.getToOrder().equals("CONN")
//						&& msg.getuMSG().equals("SUCC")) {
//					TCPClient.instance(mHostIP, mHostPort).setTCPStatus(
//							Const.TCP_CONN);
//					toActivityStr = "TCP CONNECT OK, Please Login in 15 seconds";
//				} else if (msg.getToOrder().equals("MAX")) {
//					toActivityStr = "[IN QUEUE] Waiting:" + msg.getuMSG();
//				} else {
//					toActivityStr = "ReConnect.";
//					TCPClient.instance(mHostIP, mHostPort).reConnect();
//				}
//				break;
//
//			case Const.TCP_CONN:
//				if (msg.getToOrder().equals("USERLIST")) {
//					TCPClient.instance(mHostIP, mHostPort).setTCPStatus(
//							Const.TCP_LOGIN);
//				}
//				break;
//
//			case Const.TCP_LOGIN:
//				break;
//			default:
//				toActivityStr = "ReConnect.";
//				TCPClient.instance(mHostIP, mHostPort).reConnect();
//				;
//			}
//			Const.broadCastToMainActivity(msg.getToOrder(), null, toActivityStr);
//		}
//	}

}
