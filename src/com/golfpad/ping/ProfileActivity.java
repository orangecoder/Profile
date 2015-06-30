package com.golfpad.ping;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.manuelpeinado.imagelayout.ImageLayout;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.view.SimpleViewPagerIndicator;
import com.zhy.view.SimpleViewPagerIndicator.TabOnClickListener;
import com.zhy.view.StickyNavLayout;
import com.zhy.view.StickyNavLayout.TopViewListener;

public class ProfileActivity extends FragmentActivity implements
		OnClickListener {

	private static final String TAG = "ProfileActivity";
	public static final String KEY_UID = "uid";
	private ImageLoader imageLoader;
	private Handler mHandler = new Handler();

	private String[] mTitles = new String[] { "视频", "关于", "粉丝" };
	private SimpleViewPagerIndicator mIndicator;
	private ViewPager mViewPager;
	private FragmentPagerAdapter mAdapter;
	private Fragment[] mFragments = new Fragment[mTitles.length];

	private TextView tv_title;
	private AvatarImageView iv_avatar;

	private static final SpringConfig ORIGAMI_SPRING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(40, 7);
	private Spring rootSpring;
	private Spring avatarDetailSpring;
	private View v_root;  //这个页面根控�?
	private ImageLayout iv_avatarDetail;  //头像放大显示控件
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		imageLoader = ImageLoader.getInstance();

		initViews();
		initDatas();
		initEvents();
	}
	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initViews() {
		tv_title = (TextView) findViewById(R.id.tv_profileActivity_title);
		tv_title.setText("ping");
		iv_avatar = (AvatarImageView) findViewById(R.id.iv_profileActivity_avatar);
		iv_avatar.setImageResource(R.drawable.test_user_avatar);
		iv_avatar.setGender(2);

		((StickyNavLayout) findViewById(R.id.snl_profileActivity))
				.setTopViewListener(new TopViewListener() {

					@Override
					public void topviewVisibility() {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								tv_title.setVisibility(View.GONE);
							}
						});
					}

					@Override
					public void topviewGone() {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								tv_title.setVisibility(View.VISIBLE);
							}
						});
					}
				});
		mIndicator = (SimpleViewPagerIndicator) findViewById(R.id.id_stickynavlayout_indicator);
		mViewPager = (ViewPager) findViewById(R.id.id_stickynavlayout_viewpager);
		
		v_root = findViewById(R.id.layout_profileActivity_root);
		iv_avatarDetail = (ImageLayout) findViewById(R.id.iv_profileActivity_avatardetail);
		BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.test_user_avatar);
		iv_avatarDetail.setImage(bitmapDrawable.getBitmap());
		
	}
	private void initDatas() {
		mIndicator.setTitles(mTitles);

		mFragments[0] = new FollowerFragment();
		mFragments[1] = new AboutFragment();
		mFragments[2] = new FollowerFragment();

		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return mTitles.length;
			}

			@Override
			public Fragment getItem(int position) {
				return mFragments[position];
			}

		};
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(0);

		rootSpring = SpringSystem
		        .create()
		        .createSpring()
		        .setSpringConfig(ORIGAMI_SPRING_CONFIG)
		        .addListener(new SpringListener() {
					
					@Override
					public void onSpringUpdate(Spring arg0) {
						double value = arg0.getCurrentValue();

					    float rootAlpha = (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, 1, 0);
					    v_root.setAlpha(rootAlpha);
					    float rootScale = (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, 1, 0.85);
					    rootScale = Math.max(rootScale, 0); 
					    v_root.setScaleX(rootScale);
					    v_root.setScaleY(rootScale);
					}
					
					@Override
					public void onSpringEndStateChange(Spring arg0) {
						double value = arg0.getCurrentValue();
						if(value > 0)
						{
							v_root.setVisibility(View.VISIBLE);
							iv_avatarDetail.setVisibility(View.GONE);
						}else {
							v_root.setVisibility(View.GONE);
							iv_avatarDetail.setVisibility(View.VISIBLE);
						}
					}
					
					@Override
					public void onSpringAtRest(Spring arg0) {
						
					}
					
					@Override
					public void onSpringActivate(Spring arg0) {
						
					}
				});
		avatarDetailSpring = SpringSystem
		        .create()
		        .createSpring()
		        .setSpringConfig(ORIGAMI_SPRING_CONFIG)
		        .addListener(new SpringListener() {
					
					@Override
					public void onSpringUpdate(Spring spring) {
						double value = spring.getCurrentValue();

					    float avatarDetailAlpha = (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, 1, 0);
					    iv_avatarDetail.setAlpha(avatarDetailAlpha);
					    float avatarDetailScale = (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, 1, 0);
					    avatarDetailScale = Math.max(avatarDetailScale, 0); 
					    iv_avatarDetail.setScaleX(avatarDetailScale);
					    iv_avatarDetail.setScaleY(avatarDetailScale);
					}
					
					@Override
					public void onSpringEndStateChange(Spring spring) {
					
					}
					
					@Override
					public void onSpringAtRest(Spring spring) {
						
					}
					
					@Override
					public void onSpringActivate(Spring spring) {
						
					}
				});
		rootSpring.setEndValue(0);
		avatarDetailSpring.setEndValue(1);
	}
	private void initEvents() {
		findViewById(R.id.layout_profileActivity_back).setOnClickListener(this);
		iv_avatar.setOnClickListener(this);

		mIndicator.setTabOnClickListener(new TabOnClickListener() {

			@Override
			public void onclick(int position) {
				mViewPager.setCurrentItem(position);
			}
		});

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {

			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				mIndicator.scroll(position, positionOffset);
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		findViewById(R.id.iv_profileActivity_avatardetail).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_profileActivity_avatardetail:
			rootSpring.setEndValue(0);
			avatarDetailSpring.setEndValue(1);
			break;
		case R.id.layout_profileActivity_back:
			finish();
			break;
		case R.id.iv_profileActivity_avatar:
			rootSpring.setEndValue(1);
			avatarDetailSpring.setEndValue(0);
			break;
		}
	}

	private void showMessage(final String message) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
			}
		});
	}
}
