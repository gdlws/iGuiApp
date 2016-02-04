package cn.fatdeer.isocket.entity;

public final class Login {
	private  String mConnType = null;
	private  String mHostIP = null;
	private  String mTCPPort = null;
	private  String mUDPPort = null;
	private  String mName = null;
	private  String mPassword = null;

	public String getConnType() {
		return mConnType;
	}
	public String getHostIP() {
		return mHostIP;
	}
	public String getTCPPort() {
		return mTCPPort;
	}
	public String getUDPPort() {
		return mUDPPort;
	}
	public String getName() {
		return mName;
	}
	public String getPassword() {
		return mPassword;
	}

	public void refresh(String str) {
		this.mConnType=str.split(":")[1];
		this.mHostIP=str.split(":")[2];
		this.mTCPPort=str.split(":")[3];
		this.mUDPPort=str.split(":")[4];
		this.mName=str.split(":")[5];
		this.mPassword=str.split(":")[6];
	}

	public Login(String connType,String ip,String tcpport,String udpport,String name,String password) {
		this.mConnType=connType;
		this.mHostIP=ip;
		this.mTCPPort=tcpport;
		this.mUDPPort=udpport;
		this.mName=name;
		this.mPassword=password;
	}


	@Override
	public String toString() {
		return "[conntype]"+this.mConnType
			+"[ip]"+this.mHostIP+"[tcpport]"+this.mTCPPort+"[udpport]"+this.mUDPPort
			+"[name]"+this.mName+"[password]"+this.mPassword;
	}
	
	
}
