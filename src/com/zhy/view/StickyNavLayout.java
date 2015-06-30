package com.zhy.view;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.OverScroller;
import android.widget.ScrollView;

import com.golfpad.ping.R;

public class StickyNavLayout extends LinearLayout
{
	private static final String TAG = "StickyNavLayout";
	
	private View mTop;
	private View mTopContent;
	private View mNav;
	private ViewPager mViewPager;
	
	private int mTopViewHeight;
	private ScrollView mInnerScrollView;
	private ListView mInnerListView;
	private boolean isTopHidden = false;
	private boolean isTopShowAll = true;

	private OverScroller mScroller;
	private VelocityTracker mVelocityTracker;
	private int mTouchSlop;
	private int mMaximumVelocity, mMinimumVelocity;
	
	private float mLastY;
	private boolean mDragging;
	
	private TopViewListener topViewListener;
	public void setTopViewListener(TopViewListener listener)
	{
		topViewListener = listener;
	}
	public interface TopViewListener
	{
		void topviewVisibility();
		void topviewGone();
	}
	
	public StickyNavLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setOrientation(LinearLayout.VERTICAL);

		mScroller = new OverScroller(context);
		mVelocityTracker = VelocityTracker.obtain();
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mMaximumVelocity = ViewConfiguration.get(context)
				.getScaledMaximumFlingVelocity();
		mMinimumVelocity = ViewConfiguration.get(context)
				.getScaledMinimumFlingVelocity();

	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mTop = findViewById(R.id.id_stickynavlayout_topview);
		mTopContent = findViewById(R.id.id_stickynavlayout_topview_content);
		mNav = findViewById(R.id.id_stickynavlayout_indicator);
		View view = findViewById(R.id.id_stickynavlayout_viewpager);
		if (!(view instanceof ViewPager))
		{
			throw new RuntimeException(
					"id_stickynavlayout_viewpager show used by ViewPager !");
		}
		mViewPager = (ViewPager) view;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
		params.height = getMeasuredHeight() - mNav.getMeasuredHeight();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		mTopViewHeight = mTop.getMeasuredHeight();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		int action = ev.getAction();
		float y = ev.getY();

		switch (action)
		{
		case MotionEvent.ACTION_DOWN:
			mLastY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			float dy = y - mLastY; //dy>0涓嬫媺
			if (Math.abs(dy) > mTouchSlop)
			{
				mDragging = true;
				if(dy > 0)//涓嬫媺
				{
					getCurrentScrollView();
					if(mInnerScrollView != null)
					{
						if(mInnerScrollView.getScrollY() == 0)
						{
							if(isTopShowAll)
							{
								return false;
							}else {
								return true;
							}
						}else {
							return false;
						}
					}
					if(mInnerListView != null)
					{
						if(mInnerListView.getScrollY() == 0)
						{
							if(isTopShowAll)
							{
								return false;
							}else {
								return true;
							}
						}else {
							return false;
						}
					}
				}else { //涓婃媺
					if(isTopHidden)
					{
						return false;
					}else {
						return true;
					}
				}
			}
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	private void getCurrentScrollView()
	{
		int currentItem = mViewPager.getCurrentItem();
		PagerAdapter a = mViewPager.getAdapter();
		if (a instanceof FragmentPagerAdapter)
		{
			FragmentPagerAdapter fadapter = (FragmentPagerAdapter) a;
			Fragment item = fadapter.getItem(currentItem);
			mInnerScrollView = (ScrollView) (item.getView()
					.findViewById(R.id.id_stickynavlayout_innerscrollview));
			mInnerListView = (ListView) (item.getView()
					.findViewById(R.id.id_stickynavlayout_innerlistview));
		} else if (a instanceof FragmentStatePagerAdapter)
		{
			FragmentStatePagerAdapter fsAdapter = (FragmentStatePagerAdapter) a;
			Fragment item = fsAdapter.getItem(currentItem);
			mInnerScrollView = (ScrollView) (item.getView()
					.findViewById(R.id.id_stickynavlayout_innerscrollview));
			mInnerListView = (ListView) (item.getView()
					.findViewById(R.id.id_stickynavlayout_innerlistview));
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		mVelocityTracker.addMovement(event);
		int action = event.getAction();
		float y = event.getY();

		switch (action)
		{
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished())
				mScroller.abortAnimation();
			mVelocityTracker.clear();
			mVelocityTracker.addMovement(event);
			mLastY = y;
			return true;
		case MotionEvent.ACTION_MOVE:
			float dy = y - mLastY;

			if (!mDragging && Math.abs(dy) > mTouchSlop)
			{
				mDragging = true;
			}
			if (mDragging)
			{
				scrollBy(0, (int) -dy);
				mLastY = y;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			mDragging = false;
			if (!mScroller.isFinished())
			{
				mScroller.abortAnimation();
			}
			break;
		case MotionEvent.ACTION_UP:
			mDragging = false;
			mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
			int velocityY = (int) mVelocityTracker.getYVelocity();
			if (Math.abs(velocityY) > mMinimumVelocity)
			{
				fling(-velocityY);
			}
			mVelocityTracker.clear();
			break;
		}

		return super.onTouchEvent(event);
	}

	public void fling(int velocityY)
	{
		mScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, mTopViewHeight);
		invalidate();
	}

	@Override
	public void scrollTo(int x, int y)
	{
		if (y < 0)
		{
			y = 0;
		}
		if (y > mTopViewHeight)
		{
			y = mTopViewHeight;
		}
		if (y != getScrollY())
		{
			super.scrollTo(x, y);
		}

		isTopHidden = getScrollY() == mTopViewHeight;
		isTopShowAll = getScrollY() == 0;
		if(isTopHidden)
		{
			mTopContent.setVisibility(View.GONE);
			topViewListener.topviewGone();
		}else {
			mTopContent.setVisibility(View.VISIBLE);
			topViewListener.topviewVisibility();
		}

	}

	@Override
	public void computeScroll()
	{
		if (mScroller.computeScrollOffset())
		{
			scrollTo(0, mScroller.getCurrY());
			invalidate();
		}
	}

}
