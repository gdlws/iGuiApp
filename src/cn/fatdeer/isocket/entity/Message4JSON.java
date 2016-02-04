package cn.fatdeer.isocket.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * 
 * @author GD.lws
 * @version 20150417
 * @Discribe Hold information of online user
 * 
 *           at 20150410 The type of fromUser changed from OnlineUser to String
 *           at 20150417 Short Parameter
 *           at 20150424
 *            1. new Function toString; 
 *            
 *           at 20150601
 *            1. new Parameter F  for FromUser (UDP);
 *
 */
public class Message4JSON {
	private String O; // Order
	private String F; // FromUser
	private String T; // ToUser
	private String M; // Message
	
	public Message4JSON(String order, String fUser, String tUser, String message) {
		this.O = order;
		this.F = fUser;
		this.T = tUser;
		this.M = message;
	}


	public String getToOrder() {
		return O;
	}

	public String getFromUser() {
		return F;
	}
	public String getToUser() {
		return T;
	}

	public String getuMSG() {
		return M;
	}

	@Override
	public String toString() {
//at 20150605		Gson gson = new Gson();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		
		return gson.toJson(this);
	}
	
	
}
