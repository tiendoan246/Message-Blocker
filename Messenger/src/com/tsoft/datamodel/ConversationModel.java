package com.tsoft.datamodel;

import android.graphics.Bitmap;

public class ConversationModel {

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	private long id;
	private long date;
	private int messageCount;
	private String snippet;
	private Bitmap contactImage;
	private String sender;
	private int unreadCount;

	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public Bitmap getContactImage() {
		return contactImage;
	}

	public void setContactImage(Bitmap contactImage) {
		this.contactImage = contactImage;
	}

}
