/*  
 * 项目名: YYS 
 * 文件名: MediaPlayActivity.java  
 * 版权声明:
 *      本系统的所有内容，包括源码、页面设计，文字、图像以及其他任何信息，
 *      如未经特殊说明，其版权均属大华技术股份有限公司所有。
 *      Copyright (c) 2015 大华技术股份有限公司
 *      版权所有
 */
package com.lierda.kesi.ihouse.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.lierda.kesi.ihouse.R;
import com.lierda.kesi.ihouse.constants.CommonTitle;
import com.lierda.kesi.ihouse.fragment.MediaPlayBackFragment;
import com.lierda.kesi.ihouse.fragment.MediaPlayFragment;
import com.lierda.kesi.ihouse.fragment.MediaPlayOnlineFragment;
import com.lierda.kesi.ihouse.constants.CommonTitle.OnTitleClickListener;

/**
 * 描述：视频播放Activity 作者： lc
 */
public class MediaPlayActivity extends FragmentActivity implements MediaPlayFragment.BackHandlerInterface {
	
	private final static String TAG = "MediaPlayActivity";
//	private CommonTitle mCommonTitle;
	private MediaPlayFragment mMediaPlayFragment;
	
	public static final int IS_VIDEO_ONLINE = 1;
	public static final int IS_VIDEO_REMOTE_RECORD = 2;	
	public static final int IS_VIDEO_REMOTE_CLOUD_RECORD = 3;
	//public static final int IS_VIDEO_ALARM_RECORD = 4;
	//public static final int IS_VIDEO_LOCAL_FILE = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_media_video);
		//绘制标题
//		mCommonTitle = findViewById(R.id.title);
//		mCommonTitle.initView(R.drawable.title_btn_back, 0, getIntent().getIntExtra("MEDIA_TITLE", 0));
		
//		mCommonTitle.setOnTitleClickListener(new OnTitleClickListener() {
//			@Override
//			public void onCommonTitleClick(int id) {
//				// TODO Auto-generated method stub
//				switch (id) {
//				case CommonTitle.ID_LEFT:
//					Log.v(TAG,TAG);
//					startActivity();
//					break;
//				default:
//					break;
//				}
//			}
//		});
		
		//嵌入使用的帧
		MediaPlayFragment mediaPlayFragment; //引用的布局帧
		Bundle b = new Bundle();
		String resId; //资源id号
		switch(getIntent().getIntExtra("TYPE", 0)){
		case IS_VIDEO_ONLINE:
			mediaPlayFragment = new MediaPlayOnlineFragment();
//			resId = getIntent().getStringExtra("UUID");
//			b.putString("RESID", resId);
//			mediaPlayFragment.setArguments(b);
//			Log.v(TAG,resId);
			Intent intent=getIntent();
			String token=intent.getStringExtra("token");
			String deviceId=intent.getStringExtra("deviceId");
			String encryptKey=intent.getStringExtra("encryptKey");
			b.putString("token",token);
			b.putString("deviceId",deviceId);
			b.putString("encryptKey",encryptKey);
			mediaPlayFragment.setArguments(b);
			changeFragment(mediaPlayFragment, false);
			break;
		case IS_VIDEO_REMOTE_RECORD:
			mediaPlayFragment = new MediaPlayBackFragment();
			resId = getIntent().getStringExtra("ID");
			b.putString("RESID", resId);
			mediaPlayFragment.setArguments(b);
			changeFragment(mediaPlayFragment, false);
			break;
		case IS_VIDEO_REMOTE_CLOUD_RECORD:
			mediaPlayFragment = new MediaPlayBackFragment();
			resId = getIntent().getStringExtra("ID");
			b.putString("RESID", resId);
			mediaPlayFragment.setArguments(b);
			changeFragment(mediaPlayFragment, false);
			break;
		default:
			break;
		}

	}


	/**
	 *      boolean) 描述：切换fragment
	 */
	public void changeFragment(Fragment targetFragment, boolean isAddToStack) {
		if (isAddToStack) {
			getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, targetFragment)
					.addToBackStack(null).commitAllowingStateLoss();
		} else {
			getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, targetFragment).commitAllowingStateLoss();
		}

	}
	
	@Override
	public void onBackPressed() {  	
        if(mMediaPlayFragment == null || !mMediaPlayFragment.onBackPressed()) {  
        	Log.d(TAG, "onBackPressed");
            super.onBackPressed();  
        }  
    }

	@Override
	public void setSelectedFragment(MediaPlayFragment backHandledFragment) {
		// TODO Auto-generated method stub
		this.mMediaPlayFragment = backHandledFragment;
	}
	
	//横竖屏切换需要,隐藏标题栏
	public void toggleTitle(boolean isShow) {
//		mCommonTitle.setVisibility(isShow ? View.VISIBLE : View.GONE);
	}
	/**
	 * 跳转到主页
	 */
	public void startActivity(){
		Intent mIntent = new Intent(this, MainActivity.class);
		Bundle b = getIntent().getExtras();
		if (b != null) {
			mIntent.putExtras(getIntent().getExtras());
		}
		startActivity(mIntent);
		finish();
	}

}