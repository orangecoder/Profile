package com.golfpad.ping;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

public class AvatarImageView extends CircleImageView{
	public static final int MALE = 1;
	public static final int FEMALE = 2;
	
	public AvatarImageView(Context context) {
		super(context);
		setImageResource(R.drawable.ic_useravatar_default);
	}
	
	public AvatarImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setImageResource(R.drawable.ic_useravatar_default);
	}
	
	public AvatarImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setImageResource(R.drawable.ic_useravatar_default);
	}

	public void setGender(int gender)
	{
		switch (gender) {
		case MALE:
			setBorderColor(Color.parseColor("#4ecca6"));
			setBorderWidth(3);
			break;
		case FEMALE:
			setBorderColor(Color.parseColor("#ff93a1"));
			setBorderWidth(3);
			break;
		default:
			setBorderColor(Color.parseColor("#cccccc"));
			setBorderWidth(3);
			break;
		}
	}

}
