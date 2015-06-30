package com.golfpad.ping;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

//下拉刷新listview控件
public class PullDownListView extends FrameLayout implements
		GestureDetector.OnGestureListener, Animation.AnimationListener {
	
	public static int MAX_LENGHT = 0;  //下拉刷新头部控件最大高度
	public static final int STATE_REFRESH = 1;
	public static final int SCROLL_TO_CLOSE = 2;
	public static final int SCROLL_TO_REFRESH = 3;
	public static final double SCALE = 0.3d;  //滑动相对控件移动缩放系数
	private static final int CLOSEDELAY = 300;
	private static final int REFRESHDELAY = 300;
	
	private Animation mAnimationDown;
	private Animation mAnimationUp;
	
	private TextView mTitle;  //下拉刷新标题
	private ImageView mArrow;  //下拉刷新箭头
	private ProgressBar mProgressBar;  //下拉刷新进度条
	private TextView more;  //上拉刷新加载更多标题
	private ProgressBar mProgressBar2;  //上拉刷新加载更多进度条
	
	public ListView mListView;
	View view_listview_headView;  //listview头部布局
	LinearLayout layout_listview_foot;  //listview底部布局
	LinearLayout layout_header;  //listview头部根布局
	LinearLayout layout_footer;  //listview底部根布局
	private LinearLayout mFirstChild;  //下拉刷新头部控件(在listview的上方)
	
	private int mState;
	
	private GestureDetector mDetector;
	private FlingRunnable mFlinger;
	private int mPading;  //下拉刷新控件顶部相对父控件的偏移量(<0说明下拉刷新头部还没完全展现)
	private int mDestPading;  //下拉刷新控件顶部相对父控件的目标偏移量(=0说明正在更新状态)
	private int mLastTop;  //下拉刷新控件最新顶部高度
	
	private OnRefreshListioner mRefreshListioner;
	private boolean isAutoLoadMore = false;  //是否自动加载更多数据
	private boolean hasMore = true;  //外部设置是否还有更多数据要加载
	private boolean isEnd = true;  //内部判断是否还有更多数据要加载
	private boolean listviewDoScroll = false;
	private boolean mLongPressing;// 如果设置为true说明刚好到了执行长按的时间
	private boolean mPendingRemoved = false;
	
	//文字提示
	private String pulldowntorefresh;
	private String releasetorefresh;
	private String loading;
	
	private MotionEvent downEvent;
	private CheckForLongPress mPendingCheckForLongPress = new CheckForLongPress();
	private CheckForLongPress2 mPendingCheckForLongPress2 = new CheckForLongPress2();
	private float lastY;

	/**
	 * 长按检查方法执行1线程
	 * @author Administrator
	 *
	 */
	private class CheckForLongPress implements Runnable {
		public void run() {
			if (mListView.getOnItemLongClickListener() == null) {
			} else {
				postDelayed(mPendingCheckForLongPress2, 100);
			}
		}
	}

	/**
	 * 长按检查方法执行2线程 ----> 延后 100 
	 * @author Administrator
	 *
	 */
	private class CheckForLongPress2 implements Runnable {
		public void run() {
			mLongPressing = true;
			MotionEvent e = MotionEvent.obtain(downEvent.getDownTime(),downEvent.getEventTime()
							+ ViewConfiguration.getLongPressTimeout(),
					MotionEvent.ACTION_CANCEL, downEvent.getX(),
					downEvent.getY(), downEvent.getMetaState());
			PullDownListView.super.dispatchTouchEvent(e);
		}
	}

	class FlingRunnable implements Runnable {

		private int mLastFlingY;
		private Scroller mScroller;

		public FlingRunnable() {
			mScroller = new Scroller(getContext());
		}
		
		private void startCommon() {
			removeCallbacks(this);
		}

		public void run() {
			boolean noFinish = mScroller.computeScrollOffset();
			int curY = mScroller.getCurrY();
			int deltaY = curY - mLastFlingY;
			if (noFinish) {
				move(deltaY, true);
				mLastFlingY = curY;
				post(this);
			} else {
				removeCallbacks(this);
				if (mState == SCROLL_TO_CLOSE) {
					mState = -1;
				}
			}
		}

		public void startUsingDistance(int distance, int duration) {
			if (distance == 0)
			{
				distance--;
			}
			startCommon();
			mLastFlingY = 0;
			mScroller.startScroll(0, 0, 0, distance, duration);
			post(this);
		}
	}

	/**
	 * 下拉刷新以及加载更多回调监听接口
	 * @author Administrator
	 *
	 */
	public interface OnRefreshListioner {
		public abstract void onRefresh();
		public abstract void onLoadMore();
	}
	/**
	 * 直接new时调用的构造方法 
	 * @param context
	 */
	public PullDownListView(Context context) {
		super(context);
		mDetector = new GestureDetector(context, this);
		mFlinger = new FlingRunnable();
		init();
		addRefreshBar();
	}
	
	/**
	 * 在xml中使用时调用的构造方法
	 * @param context
	 */
	public PullDownListView(Context context, AttributeSet att) {
		super(context, att);
		mDetector = new GestureDetector(this);
		mFlinger = new FlingRunnable();
		init();
		addRefreshBar();
	}

	/**
	 * 添加刷新头部的控件
	 */
	private void addRefreshBar() {

		//向上滑动的动画
		mAnimationUp = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_up);
		mAnimationUp.setAnimationListener(this);
		
		//向下滑动的动画
		mAnimationDown = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_down);
		mAnimationDown.setAnimationListener(this);
		
		//下拉刷新头部的布局
		mFirstChild = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.common_refresh_bar,null);
		//添加下拉刷新头部控件到父控件中
		addView(mFirstChild);
		
		//初始化下拉刷新左边容器控件
		FrameLayout mUpdateContent = (FrameLayout) findViewById(R.id.layout_refreshbar_content);
		FrameLayout.LayoutParams layoutparams = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		//下拉刷新箭头
		mArrow = new ImageView(getContext());
		mArrow.setScaleType(ImageView.ScaleType.FIT_CENTER);
		mArrow.setLayoutParams(layoutparams);
		mArrow.setImageResource(R.drawable.arrow_down);
		mUpdateContent.addView(mArrow);
		//下拉刷新进度条
		mProgressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmallInverse);
		mProgressBar.setIndeterminate(false);
		int i = getResources().getDimensionPixelSize(R.dimen.updatebar_padding);
		mProgressBar.setPadding(i, i, i, i);
		layoutparams.gravity = Gravity.CENTER;
		mProgressBar.setLayoutParams(layoutparams);
		mUpdateContent.addView(mProgressBar);
		//下拉刷新标题
		mTitle = (TextView) findViewById(R.id.tv_refreshbar_title);
	}

	protected void onFinishInflate() {
		super.onFinishInflate();
		mListView = (ListView) getChildAt(1);
		
		layout_header = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.common_listviewaddview_empty, null);
		mListView.addHeaderView(layout_header);
		
		layout_footer = (LinearLayout) LayoutInflater.from(getContext())
				.inflate(R.layout.common_listviewaddview_empty, null);
		mListView.addFooterView(layout_footer);
		
		layout_listview_foot = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.common_listview_more, null);
		layout_listview_foot.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (!isAutoLoadMore)
					onLoadMore();
			}
		});
		more = (TextView) layout_listview_foot.findViewById(R.id.ref);
		mProgressBar2 = (ProgressBar) layout_listview_foot.findViewById(R.id.refbar);
		mProgressBar2.setVisibility(View.GONE);
		
		
		
		mListView.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (isEnd && scrollState == SCROLL_STATE_IDLE && hasMore
						&& isAutoLoadMore) {
					onLoadMore();
				}
			}

			public void onScroll(AbsListView view, int f, int v, int t) {
				if (isAutoLoadMore) {
					if (f + v >= t - 1)
						isEnd = true;
					else
						isEnd = false;
				}
			}
		});
	}

	/**
	 * 设置没有数据时默认图片
	 * 
	 * @param empty
	 */
	public void setHeaderView(View empty) {
		view_listview_headView = empty;
		addHeaderView();
	}

	/**
	 * 添加listview顶部view
	 */
	private void addHeaderView() {
		layout_header.removeAllViews();
		if (view_listview_headView != null)
			layout_header.addView(view_listview_headView);
	}
	/**
	 * 移除listview顶部view
	 */
	public void removeHeaderView() {
		if (view_listview_headView != null)
			layout_header.removeView(view_listview_headView);
	}

	/**
	 * 初始化设置及变量
	 */
	private void init() {
		MAX_LENGHT = getResources().getDimensionPixelSize(R.dimen.updatebar_height);// 62.0dip
		
		setDrawingCacheEnabled(false);
		setBackgroundDrawable(null);
		setClipChildren(false);
		
		mDetector.setIsLongpressEnabled(false);
		mPading = -MAX_LENGHT;
		mLastTop = -MAX_LENGHT;
		
		pulldowntorefresh = getContext().getText(R.string.drop_dowm).toString();
		releasetorefresh = getContext().getText(R.string.release_update).toString();
		loading = getContext().getText(R.string.loading).toString();
	}

	/** deltaY > 0 向上 */
	private boolean move(float deltaY, boolean auto) {
		if (deltaY > 0 && mFirstChild.getTop() == -MAX_LENGHT) {
			mPading = -MAX_LENGHT;
			return false;
		}

		if (auto) {
			if (mFirstChild.getTop() - deltaY < mDestPading) {
				deltaY = mFirstChild.getTop() - mDestPading;
			}
			mFirstChild.offsetTopAndBottom((int) -deltaY);
			mListView.offsetTopAndBottom((int) -deltaY);
			mPading = mFirstChild.getTop();
			if (mDestPading == 0 && mFirstChild.getTop() == 0
					&& mState == SCROLL_TO_REFRESH) {
				//onRefresh 刷新方法执行
				onRefresh();
			} else if (mDestPading == -MAX_LENGHT) {
			}
			invalidate();
			updateView();
			return true;
		} else {
			if (mState != STATE_REFRESH
					|| (mState == STATE_REFRESH && deltaY > 0)) {
				//向上滑
				mFirstChild.offsetTopAndBottom((int) -deltaY);
				mListView.offsetTopAndBottom((int) -deltaY);
				mPading = mFirstChild.getTop();
			} else if (mState == STATE_REFRESH && deltaY < 0
					&& mFirstChild.getTop() <= 0) {
				//向下滑
				if (mFirstChild.getTop() - deltaY > 0) {
					deltaY = mFirstChild.getTop();
				}
				mFirstChild.offsetTopAndBottom((int) -deltaY);
				mListView.offsetTopAndBottom((int) -deltaY);
				mPading = mFirstChild.getTop();
			}
		}
		
		if (deltaY > 0 && mFirstChild.getTop() <= -MAX_LENGHT) {
			mPading = -MAX_LENGHT;
			deltaY = -MAX_LENGHT - mFirstChild.getTop();
			mFirstChild.offsetTopAndBottom((int) deltaY);
			mListView.offsetTopAndBottom((int) deltaY);
			mPading = mFirstChild.getTop();
			updateView();
			invalidate();
			return false;
		}
		updateView();
		invalidate();
		return true;
	}

	private void updateView() {
		if (mState != STATE_REFRESH) {
			if (mFirstChild.getTop() < 0) {
				mTitle.setText(pulldowntorefresh);
				mProgressBar.setVisibility(View.INVISIBLE);
				mArrow.setVisibility(View.VISIBLE);
				if (mLastTop >= 0 && mState != SCROLL_TO_CLOSE) {
					mArrow.startAnimation(mAnimationUp);//向上移动动画
				}
			} else if (mFirstChild.getTop() > 0) {
				mTitle.setText(releasetorefresh);
				mProgressBar.setVisibility(View.INVISIBLE);
				mArrow.setVisibility(View.VISIBLE);
				if (mLastTop <= 0) {
					mArrow.startAnimation(mAnimationDown);//向下移动动画
				}
			}
		}
		mLastTop = mFirstChild.getTop();
	}

	//release 方法执行 	
	private boolean release() {
		if (listviewDoScroll) {
			listviewDoScroll = false;
			return true;
		}
		if (mFirstChild.getTop() > 0) {
			//下拉刷新整体控件滑出父控件，准备回去
			scrollToUpdate(false);
		} else {
			scrollToClose();
		}
		invalidate();
		return false;
	}

	private void scrollToClose() {
		mDestPading = -MAX_LENGHT;
		mFlinger.startUsingDistance(MAX_LENGHT, CLOSEDELAY);
	}

	//scrollToUpdate 方法执行
	public void scrollToUpdate(boolean load) {
		mState = SCROLL_TO_REFRESH;

		mDestPading = 0;
		if (load) {
			mFlinger.startUsingDistance(50, REFRESHDELAY);
			load = false;
		} else
		{
			mFlinger.startUsingDistance(mFirstChild.getTop(), REFRESHDELAY);
		}
	}

	private void onRefresh() {
		mState = STATE_REFRESH;
		mTitle.setText(loading);
		mProgressBar.setVisibility(View.VISIBLE);
		mArrow.setVisibility(View.INVISIBLE);
		if (mRefreshListioner != null) {
			mRefreshListioner.onRefresh();
		}
	}

	public void onRefreshComplete() {
		onRefreshComplete(null);
	}

	public void onRefreshComplete(String date) {
		mState = SCROLL_TO_CLOSE;
		mArrow.setImageResource(R.drawable.arrow_down);
		mProgressBar2.setVisibility(View.INVISIBLE);
		scrollToClose();
	}

	public void setMore(boolean hasMore) {
		if (hasMore) {
			mListView.setFooterDividersEnabled(true);
			layout_footer.removeAllViews();
			layout_footer.addView(layout_listview_foot);
		} else {
			mListView.setFooterDividersEnabled(false);
			layout_footer.removeAllViews();
		}
	}

	public void onLoadMore() {
		//onLoadMore 方法执行 
		layout_listview_foot.setEnabled(false);
		mState = STATE_REFRESH;
		mProgressBar2.setVisibility(View.VISIBLE);
		more.setText(R.string.loading);
		if (mRefreshListioner != null) {
			mRefreshListioner.onLoadMore();
		}
	}

	public void onLoadMoreComplete(String date) {
		mState = -1;
		mProgressBar2.setVisibility(View.INVISIBLE);
		more.setText(R.string.seen_more);
		layout_listview_foot.setEnabled(true);
	}

	public void onLoadMoreComplete() {
		onLoadMoreComplete(null);
	}

	public boolean dispatchTouchEvent(MotionEvent e) {
		int action;
		float y = e.getY();
		action = e.getAction();
		if (mLongPressing && action != MotionEvent.ACTION_DOWN) {
			return false;
		}
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			mLongPressing = true;
		}
		boolean handled = true;
		handled = mDetector.onTouchEvent(e);
		switch (action) {
		case MotionEvent.ACTION_UP:
			boolean f1 = mListView.getTop() <= e.getY()
					&& e.getY() <= mListView.getBottom();
			if (!handled && mFirstChild.getTop() == -MAX_LENGHT && f1
					|| mState == STATE_REFRESH) {
				super.dispatchTouchEvent(e);
			} else {
				//执行释放方法 
				handled = release();
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			handled = release();
			super.dispatchTouchEvent(e);
			break;
		case MotionEvent.ACTION_DOWN:
			downEvent = e;
			mLongPressing = false;
			//长按的时间间隔
			postDelayed(mPendingCheckForLongPress,
					ViewConfiguration.getLongPressTimeout() + 100);
			mPendingRemoved = false;
			super.dispatchTouchEvent(e);
			break;
		case MotionEvent.ACTION_MOVE:
			float deltaY = lastY - y;
			lastY = y;
			if (!mPendingRemoved) {
				removeCallbacks(mPendingCheckForLongPress);
				mPendingRemoved = true;
			}

			if (!handled && mFirstChild.getTop() == -MAX_LENGHT) {
				try {
					return super.dispatchTouchEvent(e);
				} catch (Exception e2) {
					e2.printStackTrace();
					return true;
				}
			} else if (handled && mListView.getTop() > 0 && deltaY < 0) {// deltaY小于0，向下
				e.setAction(MotionEvent.ACTION_CANCEL);
				super.dispatchTouchEvent(e);
			}
			break;

		default:
			break;
		}

		return true;
	}

	public void onAnimationEnd(Animation animation) {
		int top = mFirstChild.getTop();
		if (top < 0)
		{
			mArrow.setImageResource(R.drawable.arrow_down);
		}else if (top > 0)
		{
			mArrow.setImageResource(R.drawable.arrow_up);
		}else {
			if (top < mLastTop) {
				mArrow.setImageResource(R.drawable.arrow_down);
			} else {
				mArrow.setImageResource(R.drawable.arrow_up);
			}
		}
	}

	public void onAnimationRepeat(Animation animation) {
	}

	public void onAnimationStart(Animation animation) {
	}

	public boolean onDown(MotionEvent e) {
		return false;
	}

	public boolean onFling(MotionEvent motionevent, MotionEvent e, float f,
			float f1) {
		return false;
	}

	protected void onLayout(boolean flag, int i, int j, int k, int l) {
		int top = mPading;
		int w = getMeasuredWidth();
		mFirstChild.layout(0, top, w, top + MAX_LENGHT);

		int h = getMeasuredHeight() + mPading + MAX_LENGHT;
		mListView.layout(0, top + MAX_LENGHT, w, h);
	}

	public void onLongPress(MotionEvent e) {
	}

	/** deltaY > 0 向上 */
	public boolean onScroll(MotionEvent curdown, MotionEvent cur, float deltaX,
			float deltaY) {
		deltaY = (float) ((double) deltaY * SCALE);
		boolean handled = false;
		boolean flag = false;  //标记是否正常状态(没拉动状态)
		if (mListView.getCount() == 0) {
			flag = true;
		} else {
			View c = mListView.getChildAt(0);
			if (mListView.getFirstVisiblePosition() == 0 && c != null
					&& c.getTop() == 0) {
				flag = true;
			}
		}
		if (deltaY < 0F && flag || getChildAt(0).getTop() > -MAX_LENGHT) { 
			// deltaY<0向下
			handled = move(deltaY, false);
		} else
		{
			handled = false;
		}
			
		return handled;
	}

	public void onShowPress(MotionEvent motionevent) {
	}

	public boolean onSingleTapUp(MotionEvent motionevent) {
		return false;
	}

	public void setRefreshListioner(OnRefreshListioner RefreshListioner) {
		mRefreshListioner = RefreshListioner;
	}

	public void setAutoLoadMore(boolean isAutoLoadMore) {
		this.isAutoLoadMore = isAutoLoadMore;
		if (!isAutoLoadMore) {
			layout_listview_foot.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					onLoadMore();
				}
			});
			mListView.setOnScrollListener(null);
		} else {
			mListView.setOnScrollListener(new OnScrollListener() {

				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
					if (isEnd && scrollState == SCROLL_STATE_IDLE && hasMore) {
						onLoadMore();
					}
				}

				public void onScroll(AbsListView view, int f, int v, int t) {
					if (f + v >= t - 1)
						isEnd = true;
					else
						isEnd = false;
				}
			});
			layout_listview_foot.setOnClickListener(null);
		}
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

}
