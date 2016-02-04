package cn.fatdeer.isocket.entity;

import java.util.Date;

public class SysParameter {
	int id;
	String index_code;
	String index_name;
	int chart_type;
	int ser_no;
	Date create_time;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIndex_code() {
		return index_code;
	}
	public void setIndex_code(String index_code) {
		this.index_code = index_code;
	}
	public String getIndex_name() {
		return index_name;
	}
	public void setIndex_name(String index_name) {
		this.index_name = index_name;
	}
	
	public int getChart_type() {
		return chart_type;
	}
	public void setChart_type(int chart_type) {
		this.chart_type = chart_type;
	}
	public int getSer_no() {
		return ser_no;
	}
	public void setSer_no(int ser_no) {
		this.ser_no = ser_no;
	}
	public Date getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
	@Override
	public String toString() {
		return "SysParameter [id=" + id + ", index_code=" + index_code
				+ ", index_name=" + index_name + ", create_time=" + create_time
				+ "]";
	}

}
