package cn.fatdeer.isocket.entity;

public class ChartDataSet {
String code;
String name;
int type;
int no;


public ChartDataSet(String code, String name, int type, int no) {
	this.code = code;
	this.name = name;
	this.type = type;
	this.no = no;
}

public ChartDataSet(String code) {
	this.code = code;
}
public String getCode() {
	return code;
}
public String getName() {
	return name;
}
public int getType() {
	return type;
}
public int getNo() {
	return no;
}

public void setName(String name) {
	this.name = name;
}

public void setType(int type) {
	this.type = type;
}

public void setNo(int no) {
	this.no = no;
}


}
