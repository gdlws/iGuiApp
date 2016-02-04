package cn.fatdeer.isocket.entity;

import java.util.Date;

/**
 * 存储发送socket的类，包含要发送的BufTest，以及对应的返回结果的Handler
 * @author Administrator
 *
 */
/**
 * @author GD.lws
 * @version 20150313
 * @Discribe This Class store message to socket Server, 
 *           bytes for send messages
 *           mHandler for callback to activity
 *           mSendFlag for control whether this message is success
 * 
 *           at 20150313 new parameter mSendFlag
 *           
 *           at 20150429 new parameter mSendTime for the time of this message's send; 
 *           
 *           at 20150430
 *            1. new parameter mTryTimes for How many times had sended; 
 *           at 20150609
 *            1. remove handle; 
 */
public class MsgEntity {
	private byte[] bytes;
	private boolean mComplete;
	private Date mSendTime;
	private int mTryTimes;

	public boolean isComplete() {
		return mComplete;
	}

	public void setComplete() {
		this.mComplete = true;
	}

	public int getTryTimes() {
		return mTryTimes;
	}

	public void trySend() {
		this.mTryTimes++;
	}

	public Date getSendTime() {
		return mSendTime;
	}

	public void setSendDelay(int seconds) {
		this.mSendTime = new Date(this.mSendTime.getTime() + seconds * 1000);
	}

	public MsgEntity(byte[] bytes) {
		this.bytes = bytes;
		this.mComplete = false;
		mSendTime = new Date();
		mTryTimes = 0;
	}

	public byte[] getBytes() {
		return this.bytes;
	}

	@Override
	public String toString() {
		return new String(bytes);
	}

}
