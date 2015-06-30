package com.zhy.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.golfpad.ping.R;

public class SimpleViewPagerIndicator extends LinearLayout
{

	private static final int COLOR_TEXT_NORMAL = 0xFF000000;
	private static final int COLOR_INDICATOR_COLOR = 0xFF4ECCA6;

	private String[] mTitles;
	private int mTabCount;
	private int mIndicatorColor = COLOR_INDICATOR_COLOR;
	private float mTranslationX;
	private Paint mPaint = new Paint();
	private int mTabWidth;
	
	private TabOnClickListener listener;
	public void setTabOnClickListener(TabOnClickListener listener)
	{
		this.listener = listener;
	}
	public interface TabOnClickListener{
		void onclick(int position);
	}

	public SimpleViewPagerIndicator(Context context)
	{
		this(context, null);
	}

	public SimpleViewPagerIndicator(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mPaint.setColor(mIndicatorColor);
		mPaint.setStrokeWidth(9.0F);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		mTabWidth = w / mTabCount;
	}

	public void setTitles(String[] titles)
	{
		mTitles = titles;
		mTabCount = titles.length;
		generateTitleView();
	}

	public void setIndicatorColor(int indicatorColor)
	{
		this.mIndicatorColor = indicatorColor;
	}

	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		super.dispatchDraw(canvas);
		canvas.save();
		canvas.translate(mTranslationX, getHeight() - 2);
		canvas.drawLine(0, 0, mTabWidth, 0, mPaint);
		canvas.restore();
	}

	public void scroll(int position, float offset)
	{
		/**
		 * <pre>
		 *  0-1:position=0 ;1-0:postion=0;
		 * </pre>
		 */
		mTranslationX = getWidth() / mTabCount * (position + offset);
		invalidate();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		return super.dispatchTouchEvent(ev);
	}

	private void generateTitleView()
	{
		if (getChildCount() > 0)
			this.removeAllViews();
		int count = mTitles.length;

		setWeightSum(count);
		for (int i = 0; i < count; i++)
		{
			RelativeLayout layout_tab = new RelativeLayout(getContext());
			LinearLayout.LayoutParams lp_tab = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
			lp_tab.weight = 1;
			layout_tab.setGravity(Gravity.CENTER);
			layout_tab.setBackgroundResource(R.drawable.bg_clicklayout);
			layout_tab.setLayoutParams(lp_tab);
			TextView tv = new TextView(getContext());
			RelativeLayout.LayoutParams lp_tv = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp_tv.addRule(RelativeLayout.CENTER_IN_PARENT);
			tv.setTextColor(Color.parseColor("#808080"));
			tv.setText(mTitles[i]);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
			tv.setLayoutParams(lp_tv);
			
			layout_tab.addView(tv);
			final int position = i;
			layout_tab.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					scroll(position, 0);
					if(listener != null)
					{
						listener.onclick(position);
					}
				}
			});
			addView(layout_tab);
		}
	}

}
