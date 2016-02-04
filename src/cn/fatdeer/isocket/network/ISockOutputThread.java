package cn.fatdeer.isocket.network;

import cn.fatdeer.isocket.entity.MsgEntity;

public interface ISockOutputThread {
	public void setStart(boolean isStart) ;
	public void addMsgToSendList(MsgEntity msg) ;
}
