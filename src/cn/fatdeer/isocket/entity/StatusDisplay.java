package cn.fatdeer.isocket.entity;

public class StatusDisplay implements Comparable<StatusDisplay>{
	private String name;
	private String value;
	private Integer index;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	@Override
	public int compareTo(StatusDisplay arg0) {
		return this.getIndex().compareTo(arg0.getIndex());
	}
	
}
