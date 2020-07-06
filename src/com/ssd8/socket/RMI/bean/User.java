package ssd8.rmi.bean;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable{
	private String name;
	private String password;
	private Date birthday;
	
	public User(String name, String password, Date birthday) {
		super();
		this.name = name;
		this.password = password;
		this.birthday = birthday;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	
	public String toString(){
		return "[name: "+name+",password:"+password+",birthday:"+birthday+"]" ;
		
	}
}
