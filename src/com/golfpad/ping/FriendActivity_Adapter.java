package com.golfpad.ping;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FriendActivity_Adapter extends BaseAdapter {

	private Context mContext;
	
	public FriendActivity_Adapter(Activity ctx) {
		mContext = ctx;
	}

	@Override
	public int getCount() {
		return 10;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.lvitem_friendship, null);
		return convertView;
	}
}
