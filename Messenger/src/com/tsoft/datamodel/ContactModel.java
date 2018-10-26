package com.tsoft.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactModel implements Parcelable {

	private long id;
	private String displayName;
	private String thumbnailUri;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getThumbnailUri() {
		return thumbnailUri;
	}

	public void setThumbnailUri(String thumbnailUri) {
		this.thumbnailUri = thumbnailUri;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(id);
		out.writeString(displayName);
		out.writeString(thumbnailUri);
	}

	public static final Parcelable.Creator<ContactModel> CREATOR = new Parcelable.Creator<ContactModel>() {
		public ContactModel createFromParcel(Parcel in) {
			return new ContactModel(in);
		}

		public ContactModel[] newArray(int size) {
			return new ContactModel[size];
		}
	};

	private ContactModel(Parcel in) {
		id = in.readLong();
		displayName = in.readString();
		thumbnailUri = in.readString();
	}

	public ContactModel() {
	}
}
