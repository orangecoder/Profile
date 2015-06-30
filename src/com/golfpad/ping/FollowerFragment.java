package com.golfpad.ping;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

public class FollowerFragment extends Fragment implements 
	PullDownListView.OnRefreshListioner{

	private static final String TAG = "FollowerFragment";
	
	private PullDownListView mPullDownView; // 下拉刷新整体控件
	private ListView mListView; // 列表控件
	private FriendActivity_Adapter mAdapter;
	private int currentPage = 0;
	private View v_emptyTip;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_profileactivity_followertab, container, false);
		
		//初始化自定义下拉刷新控件
		mPullDownView = (PullDownListView) view.findViewById(R.id.layout_profileActivityFollowerTab_list);
		mPullDownView.setRefreshListioner(this);
		mPullDownView.setAutoLoadMore(true); // listview到底部自动加载
		//初始化数据列表控件
		mListView = mPullDownView.mListView;
		if(mAdapter == null)
		{
			mAdapter = new FriendActivity_Adapter(getActivity());
		}
		mListView.setAdapter(mAdapter);
		View v_bottom = new View(getActivity());
		v_bottom.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 400));
		mListView.addFooterView(v_bottom);
		
		v_emptyTip = view.findViewById(R.id.tv_profileActivityFollowerTab_emptytip);
		v_emptyTip.setVisibility(View.GONE);
		
		onRefresh();
		return view;
	}

	@Override
	public void onRefresh() {
		mPullDownView.onRefreshComplete();
	}

	@Override
	public void onLoadMore() {
		mPullDownView.onLoadMoreComplete();		
	}

}
