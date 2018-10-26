package com.tsoft.datamodel;


public class MessageScheduleModel {

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	}

	public long getSentDate() {
		return sentDate;
	}

	public void setSentDate(long sentDate) {
		this.sentDate = sentDate;
	}

	public long getSentOnDate() {
		return sentOnDate;
	}

	public void setSentOnDate(long sentOnDate) {
		this.sentOnDate = sentOnDate;
	}

	private long id;
	private String sourceAddress;
	private String destinationAddress;
	private String message;
	private long createdDate;
	private long sentDate;
	private long sentOnDate;
}
