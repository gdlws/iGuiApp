package cn.fatdeer.isocket.pub;

import cn.fatdeer.isocket.CLog;
import cn.fatdeer.isocket.entity.Message4JSON;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author GD.lws
 * @version 20150424
 * @Discribe For handle JSon
 * 
 *           at 20150601
 *            1. Message4JSON will have F parameter; 
 *           at 20150605
 *            1. toJSON disableHtmlEscaping()
 *           at 20150608
 *            1. toJSON must have 4 Parameters; 
 *
 */
public final class DoJson {
	static DoJson s_instance;
	private static String tag = DoJson.class.getSimpleName();

	public static synchronized DoJson instance() {
		if (s_instance == null) {
			s_instance = new DoJson();
		}
		return s_instance;
	}
	
	public Message4JSON fromJson(String inf) {
		Message4JSON message = null;

		try {
			Gson gson = new GsonBuilder().disableHtmlEscaping().create();
			message = gson.fromJson(inf, Message4JSON.class);
		} catch (Exception e) {
			CLog.i(tag, "String 2 Message4JSON Fail: inf="+inf+"length="+inf.length());
			message = null;
			e.printStackTrace();
		}
		return message;
	}
//at 20150608
//	public String toJSon(String order, String aim, String inf) {
//		Message4JSON oMessage= new Message4JSON(order, "no te3", aim, inf);
//		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
//		return gson.toJson(oMessage);
//	}

	public String toJSon(String order, String fUser, String tUser, String inf) {
		Message4JSON oMessage= new Message4JSON(order, fUser, tUser, inf);
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(oMessage);
	}
}
