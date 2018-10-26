package com.tsoft.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

import com.tsoft.util.SmsMessageManager;

public class SmsModel implements Parcelable {
	public String getContactThumbnailUri() {
		return contactThumbnailUri;
	}

	public void setContactThumbnailUri(String contactThumbnailUri) {
		this.contactThumbnailUri = contactThumbnailUri;
	}

	private long threadId;

	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	private long id;
	private String phone;
	private String name;
	private String message;
	private long date;
	private String phoneAttach;
	private int type;
	private String sendStatus;
	private int status;
	private int read;
	private int seen;

	public int getRead() {
		return read;
	}

	public void setRead(int read) {
		this.read = read;
	}

	public int getSeen() {
		return seen;
	}

	public void setSeen(int seen) {
		this.seen = seen;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getSendStatus() {
		return sendStatus;
	}

	public void setSendStatus(String sendStatus) {
		this.sendStatus = sendStatus;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getPhoneAttach() {
		return phoneAttach;
	}

	public void setPhoneAttach(String phoneAttach) {
		this.phoneAttach = phoneAttach;
	}

	public boolean isSender() {
		return this.type == SmsMessageManager.MESSAGE_TYPE_INBOX;
	}

	private String contactThumbnailUri;
	private boolean selected;
	private String attach;

	public String getAttach() {
		return attach;
	}

	public void setAttach(String attach) {
		this.attach = attach;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public SmsModel() {
	}

	public SmsModel(long id, String phone, String name, String message,
			long date, String contactThumbnailUri) {
		this.id = id;
		this.phone = phone;
		this.name = name;
		this.message = message;
		this.date = date;
		this.contactThumbnailUri = contactThumbnailUri;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(id);
		out.writeString(phone);
		out.writeString(name);
		out.writeString(message);
		out.writeLong(date);
		out.writeString(contactThumbnailUri);
		out.writeInt(type);
		out.writeString(sendStatus);
		out.writeInt(status);
		out.writeInt(read);
		out.writeInt(seen);
	}

	public static final Parcelable.Creator<SmsModel> CREATOR = new Parcelable.Creator<SmsModel>() {
		public SmsModel createFromParcel(Parcel in) {
			return new SmsModel(in);
		}

		public SmsModel[] newArray(int size) {
			return new SmsModel[size];
		}
	};

	private SmsModel(Parcel in) {
		id = in.readLong();
		phone = in.readString();
		name = in.readString();
		message = in.readString();
		date = in.readLong();
		contactThumbnailUri = in.readString();
		type = in.readInt();
		sendStatus = in.readString();
		status = in.readInt();
		read = in.readInt();
		seen = in.readInt();
	}
}
