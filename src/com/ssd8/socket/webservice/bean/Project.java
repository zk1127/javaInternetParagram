package com.ssd8.socket.webservice.server;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 *
 */
public class Project implements Serializable{

	private static final long serialVersionUID = 1L;
	public String username;
    public String tag;
    public Date start;
    public Date end;
    public UUID projectID;
	public Project(String username, String tag, Date start, Date end) {
		super();
		this.username = username;
		this.tag = tag;
		this.start = start;
		this.end = end;
	}
	public UUID getProjectID() {
		return projectID;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
}
