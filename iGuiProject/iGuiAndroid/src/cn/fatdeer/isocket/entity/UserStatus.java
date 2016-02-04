package cn.fatdeer.isocket.entity;

import java.util.Date;

/**
 * @author GD.lws
 * @version 20151123
 * @Discribe This Class for user_status, server restore module's status each 5 minutes
 * 
 *           at 20151123
 *            1. json do not display id & index_id; 
 */
public class UserStatus {
	private transient int id;
	private int op_id;
	private transient int index_id;
	private String index_code;
	private String index_value;
	private Date create_time;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getOp_id() {
		return op_id;
	}
	public void setOp_id(int op_id) {
		this.op_id = op_id;
	}
	public int getIndex_id() {
		return index_id;
	}
	public void setIndex_id(int index_id) {
		this.index_id = index_id;
	}
	public String getIndex_code() {
		return index_code;
	}
	public void setIndex_code(String index_code) {
		this.index_code = index_code;
	}
	public String getIndex_value() {
		return index_value;
	}
	public void setIndex_value(String index_value) {
		this.index_value = index_value;
	}
	public Date getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
	@Override
	public String toString() {
		return "UserStatus [id=" + id + ", op_id=" + op_id + ", index_id="
				+ index_id + ", index_code=" + index_code + ", index_value="
				+ index_value + ", create_time=" + create_time + "]";
	}
	
	
	
	
}
