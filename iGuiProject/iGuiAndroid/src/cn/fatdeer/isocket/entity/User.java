package cn.fatdeer.isocket.entity;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable{
	private static final long serialVersionUID = 7564727866035776361L;
	int id;
	String role;
	String username;
	String nickname;
	String password;
	String mobile;
	String email;
	Date create_time;
	Date alter_time;
	// this function should be enhance in the future ,  regular expression;
	public boolean isChecked() {
		if(role==null||username==null||nickname==null||password==null||mobile==null||email==null) {
			return false;
		} else {
			return true;
		}
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Date getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
	public Date getAlter_time() {
		return alter_time;
	}
	public void setAlter_time(Date alter_time) {
		this.alter_time = alter_time;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", role=" + role + ", username=" + username
				+ ", nickname=" + nickname + ", password=" + password
				+ ", mobile=" + mobile + ", email=" + email + ", create_time="
				+ create_time + ", alter_time=" + alter_time + "]";
	}

	
}
