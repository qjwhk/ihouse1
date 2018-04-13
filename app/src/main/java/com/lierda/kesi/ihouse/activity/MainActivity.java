package com.lierda.kesi.ihouse.activity;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.czp.library.ArcProgress;
import com.czp.library.OnTextCenter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.CaptureActivity;
import com.lechange.opensdk.api.client.b;
import com.lechange.opensdk.configwifi.LCOpenSDK_ConfigWifi;
import com.lierda.kesi.ihouse.R;
import com.lierda.kesi.ihouse.application.CustomApplication;
import com.lierda.kesi.ihouse.business.Business;
import com.lierda.kesi.ihouse.fragment.MusicFragment;
import com.lierda.kesi.ihouse.fragment.MainFragment;
import com.lierda.kesi.ihouse.service.NotificationService;
import com.lierda.kesi.ihouse.util.ProgressResponseBody;
import com.lierda.kesi.ihouse.util.QRCodeUtil;
import com.lierda.kesi.ihouse.util.RabbitMqHelper;
import com.tencent.android.tpush.XGCustomPushNotificationBuilder;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGNotifaction;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.XGPushNotifactionCallback;
import com.tencent.android.tpush.common.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.alibaba.fastjson.util.IOUtils.close;

public class MainActivity extends AppCompatActivity{
    private Fragment mainFragm;
    private Fragment musicFragment;
    private boolean mainAdded=false;
    private boolean appAdded=false;
    private com.lierda.kesi.ihouse.activity.MainActivity.MsgReceiver updateListViewReceiver;
    private int allRecorders = 0;// 全部记录数
    Message m = null;
    private String token, deviceId, encryptKey;
    private boolean isOffline = true;
    private ArcProgress mProgressBar;
    private ProgressBar uploadProgress;
    private boolean f=false;
    public static final int VISIBLE=110;
    public static final int INVISIBLE=111;
    public static final int CAMERA=1;
    public static final int CODE=1;
    public static final int UPLOAD_PROGRESS_SHOW=0x110;
    public static final int UPLOAD_PROGRESS_HIDE=0x111;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //无线配对消息回调
                case LCOpenSDK_ConfigWifi.ConfigWifi_Event_Success:
                    if (isOffline) {
//                        stopConfig();
                        Log.e("Main","ConfigWifi_Event_Success");
                    }
                    break;
                case VISIBLE:
                    mProgressBar.setVisibility(View.VISIBLE);
                    Log.d("cylog","开始下载");
                    break;
                case INVISIBLE:
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Log.d("cylog","下载完成");
                    break;
                case UPLOAD_PROGRESS_SHOW:
                    uploadProgress.setVisibility(View.VISIBLE);
                    Log.d("main","开始上传");
                    break;
                case UPLOAD_PROGRESS_HIDE:
                    uploadProgress.setVisibility(View.INVISIBLE);
                    Log.d("main","上传完成");
                    break;
            }
        }
    };

    private final int startPolling = 0x10;
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

    static {
        try {
            System.loadLibrary("LechangeSDK");
        } catch (Throwable var1) {
            var1.printStackTrace();
        }

        b.a();
        b.a("1.5", "V2.0.0", "1.5");
        Log.i("LCOpenSDKInfo", "=============================================");
        Log.i("LCOpenSDKInfo", "SDKVersion : V2.0.0 || SVNVersion : Version");
        Log.i("LCOpenSDKInfo", "=============================================");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        versionSetting();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBar();
        initView();
        initFragment();
//        initXg("android");
    }

    private void versionSetting(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
    }

    /**
     * 状态栏
     */
    private void setStatusBar(){
        Window window = this.getWindow();
        //取消状态栏透明
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
//        window.setStatusBarColor(getResources().getColor(R.color.statusBarColor));
        //设置系统状态栏处于可见状态
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    /**
     * 信鸽服务
     * @param account
     */
    public void initXg(String account){
        updateListViewReceiver = new com.lierda.kesi.ihouse.activity.MainActivity.MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.qq.xgdemo.activity.UPDATE_LISTVIEW");
        registerReceiver(updateListViewReceiver, intentFilter);
        // 注册接口
//        XGPushManager.registerPush(getApplicationContext());//,"17357155010"
//        XGPushManager.registerPush(getApplicationContext(),"17357155010");//,"17357155010"
        XGPushManager.registerPush(this, account,
                new XGIOperateCallback() {
                    @Override
                    public void onSuccess(Object data, int flag) {
                        Log.d("TPush", "注册成功，设备token为：" + data);
                    }

                    @Override
                    public void onFail(Object data, int errCode, String msg) {
                        Log.d("TPush", "注册失败，错误码：" + errCode + ",错误信息：" + msg);
                    }
                });

        String a=XGPushConfig.getToken(MainActivity.this);
        // 设置通知自定义View
        initCustomPushNotificationBuilder(getApplicationContext());
    }

    private void initView(){
        uploadProgress=findViewById(R.id.progress_upload);
        mProgressBar=findViewById(R.id.mProgressBar);
        mProgressBar.setOnCenterDraw(new OnTextCenter());
        mProgressBar.setProgress(0);
    }

    /**
     * app更新
     * @param handler
     * @param url
     */
    public void updateUi(Handler handler,String url){
        f=false;
        try {
            downloadProgress(handler,mHandler,url);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "下载文件失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void downloadProgress(final Handler handler,Handler handler1,String url) throws IOException {
        Message m=new Message();
        m.what=VISIBLE;
        mHandler.sendMessage(m);
        //构建一个请求
        Request request = new Request.Builder()
                .url(url)//"http://n2n.lierdalux.cn:8585/ui/www.zip"
                .build();
        //构建我们的进度监听器
        final ProgressResponseBody.ProgressListener listener = new ProgressResponseBody.ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                //计算百分比并更新ProgressBar
                final int percent = (int) (100 * bytesRead / contentLength);
                if(percent==100){
                    Message m1=new Message();
                    Message m2=new Message();
                    m1.what=INVISIBLE;
                    m2.what=INVISIBLE;
                    handler.sendMessage(m1);
                    mHandler.sendMessage(m2);
                }else {
                    mProgressBar.setProgress(percent);
                    Log.d("cylog","下载进度："+(100*bytesRead)/contentLength+"%");
                }

            }
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response response = chain.proceed(chain.request());
                        return response.newBuilder()
                                .body(new ProgressResponseBody(response.body(),listener))
                                .build();
                    }
                })
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response!=null){
                    File www=new File("/data/data/com.lierda.kesi.ihouse/www");
                    if(www.exists()){
                    deleteDirWihtFile(www);
                 }
                    //从响应体读取字节流
                    InputStream is = response.body().byteStream();
                    new File("/data/data/com.lierda.kesi.ihouse");
                    FileOutputStream fos = new FileOutputStream(new File("/data/data/com.lierda.kesi.ihouse/1.zip"));
                    int len = 0;
                    byte[] buffer = new byte[1024];
                    while (-1 != (len = is.read(buffer))) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    is.close();
                    unZip(new File("/data/data/com.lierda.kesi.ihouse/1.zip"),new File("/data/data/com.lierda.kesi.ihouse"));
                    f=true;
                }
            }
        });
    }


    public static void unZip(File srcFile,File desFile){
        ZipFile zipFile=null;
        try {
            zipFile=new ZipFile(srcFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()){
            ZipEntry zipEntry = entries.nextElement();
            if(zipEntry.isDirectory()){
                String name = zipEntry.getName();
                File f=new File(desFile+"/"+name);
                f.mkdirs();
            }else {
                File f=new File(desFile+"/"+zipEntry.getName());
                f.getParentFile().mkdirs();
                try {
                    f.createNewFile();
                    InputStream is=zipFile.getInputStream(zipEntry);
                    FileOutputStream fos=new FileOutputStream(f);
                    int length=0;
                    byte[] b=new byte[1024];
                    while ((length=is.read(b,0,1024))!=-1){
                        fos.write(b,0,length);
                    }
                    is.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        srcFile.delete();
    }








    private void stopConfig() {
        LCOpenSDK_ConfigWifi.configWifiStop();//调用smartConfig停止接口
    }

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

    /**
     * 二维码生成，识别,无效代码
     */
    private void initialLayout() {
        ImageView imageQRCode = (ImageView) findViewById(R.id.imageQRCode);
        String contentQRCode = "nishishui";
            // 根据字符串生成二维码图片并显示在界面上，第二个参数为图片的大小（310*310）
            Bitmap bitmapQRCode = QRCodeUtil.createQRCode(contentQRCode,
                    310,310);
            new QRCodeUtil().scanningImage(bitmapQRCode);//识别
            imageQRCode.setImageBitmap(bitmapQRCode);
    }

    /**
     * fragment初始化
     */
    public void initFragment(){
        mainFragm=new MainFragment();
        musicFragment =new MusicFragment();
        switchPage("main");
    }

    public void initVideoPara(String token,String deviceId,String encryptKey){
        this.token=token;
        this.deviceId=deviceId;
        this.encryptKey=encryptKey;
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    /**
     *获取h5
     * @return
     */
    public  String read(){
        StringBuilder sb = new StringBuilder("");
        File www=new File("/data/data/com.lierda.kesi.ihouse/www");
        InputStream f=null;
        try {
            if(www.exists()){
                 f=new FileInputStream(new File("/data/data/com.lierda.kesi.ihouse/www","/index.html"));
            }else {
                 f = getAssets().open("www/index.html");
            }
//            InputStream f = getAssets().open("www/index.html");
            byte[] buf = new byte[1024];
            int hasRead = 0;
            while((hasRead = f.read(buf))>0)
            {
                sb.append(new String(buf, 0 , hasRead));
            }
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return  sb.toString();
    }


    /**
     * fragment转换
     * @param tag
     */
    public void switchPage(String tag){
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (tag){
            case "main":
                transaction.hide(musicFragment);
                if(!mainAdded){
                    transaction.add(R.id.showFragm,mainFragm);
                    mainAdded=true;
                }
                transaction.show(mainFragm);
                break;
            case "music":
                transaction.hide(mainFragm);
                if(!appAdded){
                    transaction.add(R.id.showFragm, musicFragment);
                    appAdded =true;
                }
                transaction.show(musicFragment);
                break;
            case "video":
                Business.getInstance().init(CustomApplication.LECHANGE_ID, CustomApplication.LECHANGE_PASSWORD, "openapi.lechange.cn:443");
                startActivity( token, deviceId, encryptKey);
                break;
            default:
                transaction.hide(musicFragment);
                if(!mainAdded){
                    transaction.add(R.id.showFragm,mainFragm);
                    mainAdded=true;
                }
                transaction.show(mainFragm);
                break;
        }
        transaction.commit();
    }
    /**
     * 跳转到视频播放Activity
     */
    public void startActivity(String token,String deviceId,String encryptKey){
        Intent mIntent = new Intent(this, MediaPlayActivity.class);
        mIntent.putExtra("TYPE", MediaPlayActivity.IS_VIDEO_ONLINE);
        mIntent.putExtra("token", token);
        mIntent.putExtra("deviceId", deviceId);
        mIntent.putExtra("encryptKey", encryptKey);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            mIntent.putExtras(getIntent().getExtras());
        }
        startActivity(mIntent);
//        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getSupportFragmentManager().findFragmentById(R.id.showFragm).onActivityResult(requestCode,resultCode,data);
    }

    public String getToken(){
        return XGPushConfig.getToken(MainActivity.this);

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
//        unregisterReceiver(updateListViewReceiver);
        super.onDestroy();
    }

    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationService instance = NotificationService.getInstance(MainActivity.this);
            Log.e("MAIN","NotificationService");
            if(instance.getMsg()!=null&&!instance.getMsg().equals("")){
                Log.e("instance",instance.getMsg());
//                ((MainFragment)getSupportFragmentManager().findFragmentById(R.id.showFragm)).screenShot(instance.getMsg());
            }
        }
    }

    @SuppressWarnings("unused")
    private void initCustomPushNotificationBuilder(Context context) {
        XGCustomPushNotificationBuilder build = new XGCustomPushNotificationBuilder();
        build.setSound(
                RingtoneManager.getActualDefaultRingtoneUri(
                        getApplicationContext(), RingtoneManager.TYPE_ALARM)) // 设置声音
                .setDefaults(Notification.DEFAULT_VIBRATE) // 振动
                .setFlags(Notification.FLAG_AUTO_CANCEL); // 是否可清除
        // 设置自定义通知layout,通知背景等可以在layout里设置
        build.setLayoutId(R.layout.notification);
//		// 设置自定义通知内容id
        build.setLayoutTextId(R.id.content);
//		// 设置自定义通知标题id
        build.setLayoutTitleId(R.id.title);
//		// 设置自定义通知图片id
        build.setLayoutIconId(R.id.icon);
//		// 设置自定义通知图片资源
        build.setLayoutIconDrawableId(R.mipmap.lierdalux);
////		// 设置状态栏的通知小图标
//        build.setIcon(R.drawable.right);
        build.setSmallIcon(R.mipmap.lierdalux);
        // 设置时间id
        build.setLayoutTimeId(R.id.time);
		XGPushManager.setPushNotificationBuilder(this, 1, build);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getSupportFragmentManager().findFragmentById(R.id.showFragm).onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

     public Handler getHandler(){
        return mHandler;
    }

     public boolean onKeyDown(int keyCode, KeyEvent event) {
     //这是一个监听用的按键的方法，keyCode 监听用户的动作，如果是按了返回键，同时Webview要返回的话，WebView执行回退操作，因为mWebView.canGoBack()返回的是一个Boolean类型，所以我们把它返回为true
         if(keyCode==KeyEvent.KEYCODE_BACK&&getSupportFragmentManager().findFragmentById(R.id.showFragm) instanceof MainFragment){//&& mWebView.canGoBack()
            return ((MainFragment)mainFragm).back();
         }
            return super.onKeyDown(keyCode, event);
         }
}
