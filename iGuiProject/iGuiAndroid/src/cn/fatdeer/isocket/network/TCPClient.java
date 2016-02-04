package cn.fatdeer.isocket.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import cn.fatdeer.isocket.CLog;
import cn.fatdeer.isocket.entity.Login;
import cn.fatdeer.isocket.pub.Const;
import cn.fatdeer.isocket.pub.DoJson;


/**
 * @author GD.lws
 * @version 20150313
 * @Discribe This Class manage the connection of socket
 * 
 *           at 20150313 heartbeat message change to 0xff
 *           at 20150427 
 *             1. New Parameter TCPStatus is added ; 
 *             2. Information of Server move to Const.java;
 *           at 20150428 
 *             1. Login operate move to SocketHeartThread.java;
 *           at 20150927
 *            1. readSocket() move here from TCPInputThread; 
 */
public class TCPClient {
	private static String tag = TCPClient.class.getSimpleName();

	private Selector selector;
	private SocketChannel socketChannel;
	private static TCPClient s_Tcp = null;
	private boolean isInitialized = false;
//	private String mIP;
//	private int mPort;
	private Login mLogin;
//	private int TCPStatus;

//	public int getTCPStatus() {
//		return TCPStatus;
//	}

//	public void setTCPStatus(int tcpStatus) {
//		TCPStatus = tcpStatus;
//	}

	public static synchronized TCPClient instance(Login login) {
		if (s_Tcp == null) {
			CLog.i(tag, "TCP Connect:"+login.getHostIP()+":"+login.getTCPPort());
			s_Tcp = new TCPClient(login);
		}
		return s_Tcp;
	}

	/**
	 * @param HostIp
	 * @param HostListenningPort
	 * @throws IOException
	 */
	public TCPClient(Login login) {
		this.mLogin=login;
		initialize();

	}

	/**
	 * 初始化
	 * 
	 * @throws IOException
	 */
	public void initialize() {
		CLog.i(tag, "TCPClient.initialize() begin");
//		this.TCPStatus = Const.TCP_INIT;
		boolean done = false;
		try {
			// open channel and no block mode 
			socketChannel = SocketChannel.open(new InetSocketAddress(this.mLogin.getHostIP(),
					Integer.parseInt(this.mLogin.getTCPPort())));
			CLog.i(tag, "hostIp=" + this.mLogin.getHostIP() + ";hostListenningPort="
					+ this.mLogin.getTCPPort());
			if (socketChannel != null) {
				socketChannel.socket().setTcpNoDelay(false);
				socketChannel.socket().setKeepAlive(true);
				// Set Socket Timeout=15 seconds
				socketChannel.socket().setSoTimeout(Const.SOCKET_READ_TIMOUT);
				socketChannel.configureBlocking(false);
				// Open and register selector to channel
				selector = Selector.open();
				if (selector != null) {
					socketChannel.register(selector, SelectionKey.OP_READ);
					done = true;
				}

				if (!done && selector != null) {
					selector.close();
				}
				if (!done && socketChannel != null) {
					socketChannel.close();
				}
			} else {
				CLog.i(tag, "socketChannel open fail.");
			}
		} catch (IOException e) {
			e.printStackTrace();
			Const.broadCastToActivity("WARN", null, "TCPClient.initialize():IOException",Const.whichActivity.MAIN);
		}
		String rtnStr=this.readSocket();
		if(rtnStr!=null&&rtnStr.indexOf("\"O\":\"CONN")>=0&&rtnStr.indexOf("\"M\":\"SUCC")>=0) {
			rtnStr=sendLogin(DoJson.instance().toJSon("LOGIN",
					this.mLogin.getName() , "SERVER",
					"@OB@" + this.mLogin.getName() + "@"+this.mLogin.getPassword() )); 
			if(rtnStr!=null&&rtnStr.indexOf("\"O\":\"LOGIN")>=0&&rtnStr.indexOf("\"M\":\"SUCC")>=0) {
				this.isInitialized = true;
			} else {
				Const.broadCastToActivity("ERROR", null, "TCPClient.initialize():login error from server",Const.whichActivity.MAIN);
			}
		} else {
			Const.broadCastToActivity("ERROR", null, "TCPClient.initialize():no response from server",Const.whichActivity.MAIN);
		}
	}

//
//	String LoginStr = DoJson.instance().toJSon("LOGIN",
//			login.getName(), "SERVER",
//			"@OB@" + login.getName() + "@" + login.getPassword());
//	try {
//		msgEntity = new MsgEntity(LoginStr.getBytes("ISO-8859-1"));
//	} catch (UnsupportedEncodingException e) {
//		e.printStackTrace();
//		this.exitToSystem();
//	}

	private String sendLogin(String inf) {
		int MAXTRIES = 5;
		int tries = 0; // Packets may be lost, so we have to keep trying
		String rtnStr=null;
		do {
//			try {
			byte[] goByte=Const.str2EncryptedByte(inf, "NAN", this.mLogin.getName()); 
			this.sendMsg(goByte);
			rtnStr=this.readSocket();
			CLog.e(tag, "Login feedback: " + rtnStr + "\r\n");
			if(rtnStr!=null&&rtnStr.indexOf("\"M\":\"SUCC")>=0) {
				break;
			} else tries++;
//			} catch (IOException e) { // 当receive不到信息或者receive时间超过3秒时，就向服务器重发请求
//				tries ++;
//				CLog.e(tag, "Timed out, " + (MAXTRIES - tries)
//						+ " more tries..." + "\r\n");
//				Const.broadCastToWifiActivity("INF","sendShakehand():IOException-"+(MAXTRIES - tries)+ " more tries...");
//			}
		} while (tries < MAXTRIES);
		return rtnStr;
	}
	/**
	 * @param bytes
	 */
	public void sendMsg(byte[] bytes) {
		ByteBuffer writeBuffer = ByteBuffer.wrap(bytes);

		if (socketChannel == null) {
//at 20150927			CLog.i(tag, "[error]:socketChannel == null (sendMsg byte[])");
			Const.broadCastToActivity("WARN", null, "TCPClient.sendMsg():socketChannel=null",Const.whichActivity.MAIN);
		} else {
			try {
				socketChannel.write(writeBuffer);
			} catch (IOException e) {
				e.printStackTrace();
				Const.broadCastToActivity("WARN", null, "TCPClient.sendMsg():IOException",Const.whichActivity.MAIN);
			}
		}
	}


	/**
	 * Socket连接是否是正常的
	 * 
	 * @return
	 */
	public boolean isConnect() {
		boolean isConnect = false;
		if (this.isInitialized&&this.socketChannel!=null) {
			isConnect = this.socketChannel.isConnected();
		}
		return isConnect;
	}

	/**
	 * close socket and reconnect
	 * 
	 * @return
	 */
	public boolean reConnect() {
		closeTCPSocket();
		initialize();
//		isInitialized = true;

		return isInitialized;
	}

//	/**
//	 * @return
//	 */
//	public boolean canConnectToServer() {
//		boolean result = false;
//		try {
//			if (socketChannel != null) {
//				socketChannel.socket().sendUrgentData(0xff);
//				result = true;
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return result;
//	}

	/**
	 * 关闭socket
	 */
	public void closeTCPSocket() {

		CLog.e(tag, "closeTCPSocket() " + "\r\n");
		try {
			if (socketChannel != null) {
				socketChannel.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
			Const.broadCastToActivity("ERROR", null, "TCPClient.closeTCPSocket():IOException-socketChannel.close()",Const.whichActivity.MAIN);
		}
		try {
			if (selector != null) {
				selector.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			Const.broadCastToActivity("ERROR", null, "TCPClient.closeTCPSocket():IOException-selector.close()",Const.whichActivity.MAIN);
		}
	}

	public synchronized Selector getSelector() {
		return this.selector;
	}
	/**
	 * 每次读完数据后，需要重新注册selector，读取数据
	 */
//	private synchronized void repareRead() {
//		if (socketChannel != null) {
//			try {
//				selector = Selector.open();
//				socketChannel.register(selector, SelectionKey.OP_READ);
//			} catch (ClosedChannelException e) {
//				e.printStackTrace();
//
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	public String readSocket() {
		Selector selector = this.getSelector();
		String receivedString = null;
		if (selector == null) {
			Const.broadCastToActivity("ERROR", null, "readSocket():selector == null",Const.whichActivity.MAIN);
			return null;
		}
		int select=0;
		try {
			CLog.i(tag, "waiting socket's data");
			select=selector.select();//no data, block here
		} catch (IOException e1) {
			e1.printStackTrace();
			Const.broadCastToActivity("ERROR", null, "readSocket():IOException-selector.select()",Const.whichActivity.MAIN);
		} 
//at 20150928		while(select>0) {
		if(select>0) {
			for (SelectionKey sk : selector.selectedKeys()) {
				// If SelectionKey's Channel had readable data
				if (sk.isReadable()) {
					// use NIO read data in the Channel
					SocketChannel sc = (SocketChannel) sk.channel();
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					try {
						sc.read(buffer);
					} catch (IOException e) {
						e.printStackTrace();
						Const.broadCastToActivity("ERROR", null,
								"readSocket():IOException-sc.read(buffer)",Const.whichActivity.MAIN);
					}
					buffer.flip();
					// From buffer to byte[]
					byte[] content = new byte[buffer.limit()];
					buffer.get(content);
					if(content.length>0) { //at 20151120
						receivedString = Const.analyzeEncrptedPacket(content,
								content.length);
						CLog.i(tag, "rcvStr"+receivedString);
						buffer.clear();
						buffer = null;
					}
					try {
						// Prepare for next request
						sk.interestOps(SelectionKey.OP_READ);
						// remove processing SelectionKey
						selector.selectedKeys().remove(sk);
					} catch (CancelledKeyException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return receivedString;
	}
}
