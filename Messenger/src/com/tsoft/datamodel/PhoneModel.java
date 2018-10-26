package com.tsoft.datamodel;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class PhoneModel implements Serializable, Parcelable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7888815746374632540L;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getContactId() {
		return contactId;
	}

	public void setContactId(long contactId) {
		this.contactId = contactId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public void toggleChecked() {
		checked = !checked;
	}

	public PhoneModel() {
	}

	public PhoneModel(long id, long contactId, String displayName,
			String number, boolean checked) {
		this.id = id;
		this.contactId = contactId;
		this.displayName = displayName;
		this.number = number;
		this.checked = checked;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	private long id;
	private long contactId;
	private String displayName;
	private String number;
	private boolean checked = false;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(contactId);
		dest.writeString(displayName);
		dest.writeString(number);
		dest.writeByte((byte) (checked ? 1 : 0));
	}

	public static final Parcelable.Creator<PhoneModel> CREATOR = new Parcelable.Creator<PhoneModel>() {
		public PhoneModel createFromParcel(Parcel in) {
			return new PhoneModel(in);
		}

		public PhoneModel[] newArray(int size) {
			return new PhoneModel[size];
		}
	};

	private PhoneModel(Parcel in) {
		id = in.readLong();
		contactId = in.readLong();
		displayName = in.readString();
		number = in.readString();
		checked = in.readByte() != 0;
	}
}
