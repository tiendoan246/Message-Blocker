package com.tsoft.adapter;

import java.util.List;

import android.content.Context;

import com.tsoft.control.FilteredArrayAdapter;
import com.tsoft.datamodel.PhoneModel;

public class CustomFilterArrayAdapter extends FilteredArrayAdapter<PhoneModel> {

	public CustomFilterArrayAdapter(Context context, int resource,
			int textViewResourceId, List<PhoneModel> objects) {
		super(context, resource, textViewResourceId, objects);
	}

	@Override
	protected boolean keepObject(PhoneModel arg0, String arg1) {
		arg1 = arg1.toLowerCase();
		return arg0.getDisplayName().toLowerCase().contains(arg1)
				|| arg0.getNumber().toLowerCase().contains(arg1);
	}

}
