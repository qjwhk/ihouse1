package com.lierda.kesi.ihouse.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lechange.opensdk.api.bean.CheckDeviceBindOrNot;
import com.lechange.opensdk.api.bean.CheckDeviceBindOrNot.Response;
import com.lechange.opensdk.api.bean.DeviceOnline;
import com.lechange.opensdk.api.client.b;
import com.lechange.opensdk.configwifi.LCOpenSDK_ConfigWifi;
import com.lierda.kesi.ihouse.R;
import com.lierda.kesi.ihouse.business.Business;
import com.lierda.kesi.ihouse.constants.CommonTitle;
import com.lierda.kesi.ihouse.constants.ProgressDialog;
import com.lierda.kesi.ihouse.constants.CommonTitle.OnTitleClickListener;
import com.lierda.kesi.ihouse.business.Business.RetObject;

import java.util.List;

public class DeviceAddActivity extends Activity implements OnClickListener,ActivityCompat.OnRequestPermissionsResultCallback{
	public final static String tag = "AddDeviceActivity";
	
	private final int startPolling = 0x10;
	private final int successOnline = 0x11;
	private final int asynWaitOnlineTimeOut = 0x12;
	private final int successAddDevice = 0x13;
	//private final int successOffline = 0x14;
	//private final int addDeviceTimeOut = 0x15;
	//private final int failedAddDevice = 0x16;
	//private final int successOnlineEx = 0x17;
	
	private CommonTitle mCommonTitle;
	private ProgressDialog mProgressDialog;  //播放加载使用
	private WifiInfo mWifiInfo;
	private TextView mSsidText;
	private EditText mSnText;
	private EditText mPwdText;
	private ImageView mWirelessButton;
	private ImageView mWiredButton;

	private Handler mHandler;
	
	private boolean isOffline = true;
	private enum ConfigStatus{query, wifipair, wired};  //配对方式
	private ConfigStatus mConfigStatus = ConfigStatus.query; //默认为轮询状态
	
	//无线配置参数
	private static final int PROGRESS_TIMEOUT_TIME = 60 * 1000;
	private int time = 25;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_add);
		
		//绘制标题
		mCommonTitle = (CommonTitle) findViewById(R.id.title);
		mCommonTitle.initView(R.drawable.title_btn_back, 0, R.string.devices_add_name);
		
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
		
		
		//绘制画面
		mSnText = (EditText)findViewById(R.id.deviceSN);
		mPwdText = (EditText)findViewById(R.id.wifiPasswd);
		mSsidText = (TextView)findViewById(R.id.wifiName);
		
		mWirelessButton = (ImageView) findViewById(R.id.wirelessAdd);
		mWirelessButton.setOnClickListener(this);
		mWiredButton = (ImageView) findViewById(R.id.wiredAdd);
		mWiredButton.setOnClickListener(this);
		//load组件
		mProgressDialog = (ProgressDialog) this.findViewById(R.id.query_load);

		WifiManager mWifiManager =  (WifiManager)getApplicationContext().getSystemService(Activity.WIFI_SERVICE);
		mWifiInfo = mWifiManager.getConnectionInfo();
		if (mWifiInfo != null) {
			mSsidText.setText("SSID:" + mWifiInfo.getSSID().replaceAll("\"", ""));
		}
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
					Log.d(tag, "msg.what"+msg.what);
					switch (msg.what) {
					//无线配对消息回调
					case LCOpenSDK_ConfigWifi.ConfigWifi_Event_Success:
						if(isOffline){
							Log.d(tag, "无线配对...");
							mConfigStatus = ConfigStatus.wifipair;
							toast("smartConfig success");
							stopConfig();
							mHandler.removeCallbacks(progressPoll);
							checkOnline();
						}
						break;
					case startPolling:							
						checkOnline();
						break;
					//校验消息回调
					case asynWaitOnlineTimeOut:
						Log.d(tag, "checkIsOnlineTimeOut");
						break;
					case successOnline:
						Log.d(tag, "successOnline");
						bindDevice();
						break;
					case successAddDevice:
						//Attributes device = (Attributes) msg.obj;
						//success(device.getUuid());
						Log.d(tag, "SuccessAddDevice");
						toast("SuccessAddDevice");
						//只有这么一种情况
						setResult(RESULT_OK);
						finish();
						break;
					default:
						break;
				}
			}
		};
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		time = 0;
		stopConfig();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.wirelessAdd:
			if (TextUtils.isEmpty(mPwdText.getText().toString()) || TextUtils.isEmpty(mSnText.getText().toString())) {
				toast("序列号/密码不能为空！");
				
			}else{
				checkOnBindandremind();
			}
			break;
		case R.id.wiredAdd:
			if (TextUtils.isEmpty(mSnText.getText().toString())) {
				toast("序列号不能为空！");
			}else{
				mConfigStatus = ConfigStatus.wired;
				checkOnBindandline();
			}
			break;
		default:
			break;
		}

	}
	
	/**
	 * 校验在线
	 */
	private void checkOnline(){
		//隔两秒轮询.....
		Business.getInstance().checkOnline(mSnText.getText().toString(), new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(!isOffline) return;
				
				RetObject retObject = (RetObject) msg.obj;
				switch(msg.what){
				case 0:
					if(((DeviceOnline.Response)retObject.resp).data.onLine.equals("1")){
						switch(mConfigStatus){
						case wired:
							Log.d(tag, "有线配对....");
							break;
						case query:
							Log.d(tag, "轮询....");
							stopConfig();
						case wifipair:
							mProgressDialog.setStop();
						}
						toast("Online");
						isOffline = false;
						mHandler.obtainMessage(successOnline).sendToTarget();
					}else{
						if(mConfigStatus == ConfigStatus.wired){
							Log.d(tag, "offline..... wired");
							toast("offline");
						}else if(time > 0){
							Log.d(tag, "offline..... try again checkOnline");
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							time--;
							mHandler.obtainMessage(startPolling).sendToTarget();
						}else{
							Log.d(tag, "offline..... try again max");
							mProgressDialog.setStop();
							time = 25;
							toast("offline");
						}
					}
					break;
				case -1000:
					if(time > 0){
						Log.d(tag, "code:-1000..... try again checkOnline");
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						time--;
						mHandler.obtainMessage(startPolling).sendToTarget();
					}
					break;
				default:
					switch(mConfigStatus){
					case wired:
						Log.d(tag, "有线配对失败....");
						break;
					case query:
						Log.d(tag, "轮询失败....");
						stopConfig();
					case wifipair:
						mProgressDialog.setStop();
					}
					toast(retObject.mMsg);					
					break;
				}
			}
		});
	}
	
	/**
	 * 绑定
	 */
	private void bindDevice(){
		Business.getInstance().bindDevice(mSnText.getText().toString(), new Handler(){
			@Override
			public void handleMessage(Message msg) {
				RetObject retObject = (RetObject) msg.obj;
				if(msg.what == 0){				
					mHandler.obtainMessage(successAddDevice).sendToTarget();			
				}else{
					toast(retObject.mMsg);
				}
			}
		});
	}

	
	/**
	 * 有线配对校验
	 */
	private void checkOnBindandline(){
		Business.getInstance().checkBindOrNot(mSnText.getText().toString(), new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				RetObject retObject = (RetObject) msg.obj;
				if(msg.what == 0){
					CheckDeviceBindOrNot.Response resp = (Response) retObject.resp;
					if(!resp.data.isBind)
						checkOnline();
					else if(resp.data.isBind && resp.data.isMine)
						toast("已经被自己绑定");
					else
						toast("已经被他人绑定");
				}else{
					toast(retObject.mMsg);
				}
			}
		});
	}

	/**
	 * 无线配对校验
	 */
	public void checkOnBindandremind(){
		Business.getInstance().checkBindOrNot(mSnText.getText().toString(), new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				RetObject retObject = (RetObject) msg.obj;
				if(msg.what == 0){
					CheckDeviceBindOrNot.Response resp = (Response) retObject.resp;
					if(!resp.data.isBind){
						showWifiConfig();
						}
					else if(resp.data.isBind && resp.data.isMine)
						toast("已经被自己绑定");
					else
						toast("已经被他人绑定");
				}else{
					toast(retObject.mMsg);
				}
			}
		});	
	}
	/**
	 * 启动无线配对
	 */
	private void startConfig() {
		//开启播放加载控件	
		mProgressDialog.setStart(getString(R.string.wifi_config_loading));
		
		String ssid = mWifiInfo.getSSID().replaceAll("\"", "");
		String ssid_pwd = mPwdText.getText().toString();
		String code = mSnText.getText().toString().toUpperCase();
		
		String mCapabilities = getWifiCapabilities(ssid);
		//无线超时任务
		mHandler.postDelayed(progressRun, PROGRESS_TIMEOUT_TIME);
		//10s开启轮询
		mHandler.postDelayed(progressPoll, 10 * 1000);
		//调用接口，开始通过smartConfig匹配
		//System.out.println("mLinkIPCProxy.start");
		LCOpenSDK_ConfigWifi.configWifiStart(code, ssid, ssid_pwd, mCapabilities, mHandler);
	}
	
	/**
	 * 关闭无线配对
	 */
	private void stopConfig() {
		mHandler.removeCallbacks(progressRun);
		LCOpenSDK_ConfigWifi.configWifiStop();//调用smartConfig停止接口
	}
	
	/**
	 * 无线配对超时任务
	 */
	private Runnable progressRun = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			toast("超时配置失败");
			stopConfig();
		}
	};

	/**
	 * 轮询定时启动任务
	 */
	private Runnable progressPoll = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mHandler.obtainMessage(startPolling).sendToTarget();
		}
	};
	
	/**
	 * 获取wifi加密信息
	 */
	private String getWifiCapabilities(String ssid) {
		String mCapabilities = null;
		ScanResult mScanResult = null;
			WifiManager mWifiManager = (WifiManager)getApplicationContext().getSystemService(Activity.WIFI_SERVICE);

		if (mWifiManager != null) {
			WifiInfo mWifi = mWifiManager.getConnectionInfo();
			if (mWifi != null) {
				// 判断SSID是否存在
				if (mWifi.getSSID() != null && mWifi.getSSID().replaceAll("\"", "").equals(ssid)) {
					List<ScanResult> mList = mWifiManager.getScanResults();
					if (mList != null) {
						for (ScanResult s : mList) {
							if (s.SSID.replaceAll("\"", "").equals(ssid)) {
								mScanResult = s;
								break;
							}
						}
					}
				}
			}
		}
		mCapabilities = mScanResult != null ? mScanResult.capabilities : null;
		return mCapabilities;
	}
	
	private void toast(String content) {
		Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
			@NonNull int[] grantResults) {
		// TODO Auto-generated method stub
		 if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
             // Camera permission has been granted, preview can be displayed
			 showPairDescription();
            
         } else {
        	 toast("位置服务已被禁止，请在《权限管理》打开");
         }
		
	}
	
	/**
	 * 开启无线配网流程（权限检查，配对说明）
	 */
	public void showWifiConfig() {
		boolean isMinSDKM = Build.VERSION.SDK_INT < 23;
		boolean isGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
		if(isMinSDKM  || isGranted) {
			
			showPairDescription();
			//开启无线配对
			return;
		}
		
		requestLocationPermission();
	}
	
	/**
	 * 显示配对说明
	 */
	private void showPairDescription() {
		DialogInterface.OnClickListener dialogOnclicListener=new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){  
				case Dialog.BUTTON_POSITIVE:
					startConfig();
					break;  
				case Dialog.BUTTON_NEGATIVE:
					break;  
				case Dialog.BUTTON_NEUTRAL:
					break;  
				}  
			}
		};  
		//dialog参数设置  
		AlertDialog.Builder builder=new AlertDialog.Builder(DeviceAddActivity.this);  //先得到构造器
		builder.setTitle("提示"); //设置标题  
		builder.setMessage("根据说明书操作设备的配对按钮,再点击确认进入配对"); //设置内容  
		builder.setPositiveButton("确认",dialogOnclicListener);
		builder.setNegativeButton("取消",dialogOnclicListener);
		builder.create().show();
	}
	
	/**
	 * 请求相关权限
	 */
	private void requestLocationPermission() {
		if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			
			Log.d("Uriah","Uriah + shouldShowRequestPermission true");
			 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
	                    1);
		}else {
			 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
	                    1);
		}
	}
}
