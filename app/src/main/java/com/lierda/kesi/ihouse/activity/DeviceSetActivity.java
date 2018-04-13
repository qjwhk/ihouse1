package com.lierda.kesi.ihouse.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.lierda.kesi.ihouse.R;
import com.lierda.kesi.ihouse.business.Business;
import com.lierda.kesi.ihouse.constants.CommonTitle;
import com.lierda.kesi.ihouse.constants.CommonTitle.OnTitleClickListener;

public class DeviceSetActivity  extends Activity {

	private String tag                 = "DeviceSetActivity";
	private CommonTitle mCommonTitle;
	private ToggleButton mSwitch;                      //动检开关
	private ToggleButton mCloudMealBtn;                //云存储套餐开关
	private String mChannelUUID        = null;
	private int             mCloudMealStates;             //云套餐状态
	private int             mAlarmStates;                 //报警计划状态  
	private boolean         IsClickSwitchBtn   ;   //是否主动修改动检开关（手势触发）
	private boolean         IsClickCloudMealBtn ;   //是否主动修改套餐状态 （手势触发）
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_set);
		
		mChannelUUID = getIntent().getStringExtra("UUID");                  //获取通道的UUID
		
		mSwitch = (ToggleButton) findViewById(R.id.switchPlan);
		mCloudMealBtn =(ToggleButton) findViewById(R.id.cloudMealBtn);
		mSwitch.setClickable(false);
		mCloudMealBtn.setClickable(false);
		initTitle();
		setListener();
		getOriginStatus();
		

		
	}

	@Override
	protected void onDestroy() {
		
		super.onDestroy();
	}
	
	/**
	 * 初始化标题栏
	 */
	public void initTitle()
	{
		//绘制标题
		mCommonTitle = (CommonTitle) findViewById(R.id.title);
		mCommonTitle.initView(R.drawable.title_btn_back, 0, R.string.devices_operation_name);
		
		mCommonTitle.setOnTitleClickListener(new OnTitleClickListener() {
			@Override
			public void onCommonTitleClick(int id) {
				// TODO Auto-generated method stub
				switch (id) {
				case CommonTitle.ID_LEFT:
					
					finish();
					break;
				}
			}
		});
	}
	
	/**
	 * 获取初始状态
	 */
	private void getOriginStatus() {
		// 初始化数据
		Business.getInstance().getDeviceInfo(mChannelUUID, new Handler()	{
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Bundle bundle = (Bundle) msg.obj;
				if (msg.what == 0) {
					
					mAlarmStates = bundle.getInt("alarmStatus");
					mCloudMealStates = bundle.getInt("cloudStatus");
					
					if(mCloudMealStates == 1){
						IsClickSwitchBtn = false;
						mCloudMealBtn.setChecked(true);
					}
					if(mAlarmStates == 1){
						IsClickCloudMealBtn = false;
						mSwitch.setChecked(true);
					}
					mSwitch.setVisibility(View.VISIBLE);
					mCloudMealBtn.setVisibility(View.VISIBLE);
				}
				else {
					Toast.makeText(DeviceSetActivity.this, "获取初始状态信息失败", Toast.LENGTH_SHORT).show();
				}
				
				IsClickCloudMealBtn = true;
				IsClickSwitchBtn = true;
				mSwitch.setClickable(true);
				mCloudMealBtn.setClickable(true);
			}
		});
	}
	/**
	 * 设置监听函数
	 */
	public void setListener()
	{
		mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton view, boolean state) {
				
				//如果是手势触发，则进行网络请求
				if(IsClickSwitchBtn){
					ModifyAlarmPlan(state);
				}
				
		
			}
		});
		
		mCloudMealBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton view, boolean state) {
				//如果是手势触发，则进行网络请求
				if(IsClickCloudMealBtn){
					setStorageStrateyg( state);
				}				
			}
		});
	}
	
	/**
	 * 设置云套餐状态
	 */
	public void setStorageStrateyg( final boolean state){
		
		mCloudMealBtn.setClickable(false);
		String states = null;
		if(state){
			states = "on";
		}
		else{
			states = "off";
		}
		Business.getInstance().setStorageStartegy(states, mChannelUUID, new Handler(){

			@Override
			public void handleMessage(Message msg) {
				if(0==msg.what){
					Toast.makeText(DeviceSetActivity.this, "云套餐更改成功", Toast.LENGTH_SHORT).show();
				}
				else{			
					Toast.makeText(DeviceSetActivity.this, "云套餐更改失败", Toast.LENGTH_SHORT).show();
					IsClickCloudMealBtn = false;
					mCloudMealBtn.setChecked(!state);
				}
				IsClickCloudMealBtn = true;
				mCloudMealBtn.setClickable(true);
			}		
		});
	}

	
	/**
	 * 修改动检计划状态
	 * @param enable 动检计划开启与否
	 */
	public void ModifyAlarmPlan(final boolean enable){
		
		mSwitch.setClickable(false);
		Business.getInstance().modifyAlarmStatus(enable, mChannelUUID, new Handler(){

			@Override
			public void handleMessage(Message msg) {
				if(0 == msg.what)
				{
					Toast.makeText(DeviceSetActivity.this, "动检计划更改成功", Toast.LENGTH_LONG).show();
				}
				else
				{
					Toast.makeText(DeviceSetActivity.this, "动检计划更改失败", Toast.LENGTH_LONG).show();
					IsClickSwitchBtn = false;
					mSwitch.setChecked(!enable);
				}
				IsClickSwitchBtn = true;
				mSwitch.setClickable(true);
			}
			
		});
	}
}
