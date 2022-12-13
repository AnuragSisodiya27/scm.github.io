package com.smart.helper;


public class Message {
	
	private String content;
	private String status;
	public Message(String content, String status) {
		super();
		this.content = content;
		this.status = status;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
}
