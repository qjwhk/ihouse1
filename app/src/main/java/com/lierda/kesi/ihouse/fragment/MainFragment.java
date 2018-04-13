package com.lierda.kesi.ihouse.fragment;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.google.zxing.client.android.CaptureActivity;
import com.lechange.opensdk.configwifi.LCOpenSDK_ConfigWifi;
import com.lierda.kesi.ihouse.R;
import com.lierda.kesi.ihouse.activity.MainActivity;
import com.lierda.kesi.ihouse.po.NetInfo;
import com.lierda.kesi.ihouse.po.Alarm;
import com.lierda.kesi.ihouse.po.Attributes;
import com.lierda.kesi.ihouse.util.CameraHelper;
import com.lierda.kesi.ihouse.util.FileUtil;
import com.lierda.kesi.ihouse.util.RabbitMqHelper;
import com.lierda.kesi.ihouse.util.UploadHelper;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2017/12/14.
 */

public class MainFragment extends Fragment{
//    public static String baseurl = "file:///android_asset/www/";
    public static String baseurl = "file:///data/data/com.lierda.kesi.ihouse/www/";
    private  final  static String  serverUrl="http://39.104.69.85:8585/action";
    private MainActivity uiActivity;
    private Handler uiHandler;
    private  String imgUrl;
    private File output;
//    static {
//        File www=new File("/data/data/com.lierda.kesi.ihouse/www");
//        if(www.exists()){
//            baseurl = "file:///data/data/com.lierda.kesi.ihouse/www/";
//        }else {
//            baseurl = "file:///android_asset/www/";
//        }
//    }
    public static String html;
    private static WebView webView;
    private String TAG = "MainFragment";
    private boolean isOffline = true;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //无线配对消息回调
                case LCOpenSDK_ConfigWifi.ConfigWifi_Event_Success:
                    if (isOffline) {
                        stopConfig();
                        Log.e("MESSAGEMainFragment","ConfigWifi_Event_Success");
                    }
                    break;
                case MainActivity.INVISIBLE:
                    String a="true";
                    webView.loadUrl("javascript:comfirmApp(\""+a+"\")");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity mainActivity= (MainActivity) getActivity();
                            Intent intent = mainActivity.getIntent();
                            mainActivity.finish();
                            startActivity(intent);
                        }
                    }, 1000);
                    break;
                case UploadHelper.Success:
                    imgUrl=msg.obj.toString();
                    webView.loadUrl("javascript:getImageData(\""+imgUrl+"\")");
                    Log.d(imgUrl,"上传成功");
                    break;
            }
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void stopConfig() {
        LCOpenSDK_ConfigWifi.configWifiStop();//调用smartConfig停止接口
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_main, container, false);
        requestPermissions();
        initWebView(view);
        initParams();
        return view;
    }


    private void initParams(){
         uiActivity= (MainActivity) getActivity();
         uiHandler=uiActivity.getHandler();
    }

    private void requestPermissions(){
        ActivityCompat.requestPermissions((MainActivity)getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                READ_STORE);
    }

    private final int CROP_PHOTO=1;
    private final int REQUEST_CODE_PICK_IMAGE=2;
    private final int CUT_PIC=3;
    private final int READ_STORE=0X1254;
    private final int WRITE_STORE=0X1255;
    private  final int TAKE_PHOTO=0x113;
    private Uri imageUri;
    private void takePhoto(){
        imageUri=Uri.fromFile(CameraHelper.takePhoto());
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        getActivity().startActivityForResult(intent, CROP_PHOTO);
    }
    private void choosePhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        getActivity().startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void initWebView(View view){
        webView =  view.findViewById(R.id.webView);
        html=((MainActivity) getActivity()).read();
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportMultipleWindows(true);
        settings.setGeolocationEnabled(true);
        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;
        switch (screenDensity) {
            case DisplayMetrics.DENSITY_LOW:
                zoomDensity = WebSettings.ZoomDensity.CLOSE;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                zoomDensity = WebSettings.ZoomDensity.MEDIUM;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                zoomDensity = WebSettings.ZoomDensity.FAR;
                break;
        }
        settings.setDefaultZoom(zoomDensity);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);



        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setInitialScale(25);
        webView.addJavascriptInterface(new JsInteration(), "jsbridge");
        webviewload();
    }

    private static void webviewload(){
        File www=new File("/data/data/com.lierda.kesi.ihouse/www");
        if(FileUtil.isFileExist(www)){
            baseurl = "file:///data/data/com.lierda.kesi.ihouse/www/";
        }else {
            baseurl = "file:///android_asset/www/";
        }
        webView.loadDataWithBaseURL(baseurl, html, "text/html", "UTF-8", null);
    }

    public class JsInteration {

        /**
         * @param data 要变换的页面名
         **/
        @JavascriptInterface
        public void app(String data) {
            if (data != null){
                Log.d(TAG,data);
                MainActivity activity = (MainActivity) getActivity();
                activity.switchPage("app");
            }else {
                //else执行
            }
        }
        /**
         * @param data Json格式字符串
         **/
        @JavascriptInterface
        public void picture(String data) {
            if (data != null){
                Log.d(TAG,data);
            }else {
                //else执行
            }
        }

        /**
         * 视频播放
         * @param token
         * @param userid
         * @param deviceId
         * @param encryptKey
         */
        @JavascriptInterface
        public void playVideo(String token,String userid,String deviceId,String encryptKey) {
//            if (data != null){
//                Log.d(TAG,data);
//            }else {
//                //else执行
//            }
            MainActivity activity= (MainActivity) getActivity();
            activity.initVideoPara(token,deviceId,encryptKey);
            activity.switchPage("video");
        }

        /**
         * 账号绑定
         * @param userid
         * @param source
         */
        @JavascriptInterface
        public void pushUserSever(String userid,String source) {
            if (userid != null&&source!=null){
                MainActivity activity= (MainActivity) getActivity();
                activity.initXg(userid);
                source="jky";//--------------------------------------------------------------delete
                Log.d(TAG,userid);
                Log.d(TAG,source);
                Alarm alarm=new Alarm(source,"reg",getMacAddress(),new Attributes(userid,((MainActivity)getActivity()).getToken(),"QQ"));
                try {
                    RabbitMqHelper.sendMsg(JSON.toJSONString(alarm),getMacAddress()+"."+source);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                //else执行
            }
        }

        /**
         * 二维码扫描
         */
        @JavascriptInterface
        public void scan() {
            ActivityCompat.requestPermissions((MainActivity)getActivity(), new String[]{Manifest.permission.CAMERA},
                    MainActivity.CAMERA);
//            Intent openCameraIntent = new Intent((MainActivity)getActivity(),CaptureActivity.class);
//            startActivityForResult(openCameraIntent, 0);
        }

        /**
         * wifi绑定
         * @param userid
         * @param sn
         * @param wifi
         * @param wifipsd
         * @param brand
         */
        @JavascriptInterface
        public void bindDeviceToWifi(String userid ,String sn,String wifi,String wifipsd,String brand){
            bindDeviceToWifi1(userid,sn,wifi,wifipsd,brand);
        }

        /**
         * app更新
         * @param url
         * @param version
         */
        @JavascriptInterface
        public void updateUi(String url,String version){
            MainActivity mainActivity= (MainActivity) getActivity();
            mainActivity.updateUi(mHandler,url);
            Log.d("更新","更新：中");
        }

        /**
         * 拍照
         */
        @JavascriptInterface
        public void takePhoto(){
            ActivityCompat.requestPermissions((MainActivity)getActivity(), new String[]{Manifest.permission.CAMERA},
                    TAKE_PHOTO);
        }

        /**
         * 照片选择
         */
        @JavascriptInterface
        public void choosePhoto(){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");//相片类型
            getActivity().startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        }

        /**
         *网络连接判断
         */
        @JavascriptInterface
        public void  queryWifiState(){
            NetInfo netInfo = getNetworkInfo();
//            webView.loadUrl("javascript:getWifiState(\""+netInfo.getStatus()+"\",\""+netInfo.getType()+"\")");
        }
    }


    public String getMacAddress(){
        String macAddr="";
        final WifiManager wm = (WifiManager) getActivity().getApplicationContext() .getSystemService(Service.WIFI_SERVICE);
        WifiInfo info=wm.getConnectionInfo();
        macAddr=info.getMacAddress().toUpperCase().replace(":","");
        return  macAddr;
    }

    private NetInfo getNetworkInfo(){
        final  NetInfo netInfo=new NetInfo();
        Context context=getActivity();
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null&&mNetworkInfo.isAvailable()) {
                netInfo.setStatus("1");
//                Toast.makeText(getActivity(), "網絡已連接", Toast.LENGTH_SHORT).show();
                NetworkInfo info=mConnectivityManager.getActiveNetworkInfo();
                if (info!=null&&info.isAvailable()&&info.getType()==ConnectivityManager.TYPE_WIFI){
                    netInfo.setType("1");
//                    Toast.makeText(getActivity(), "無線網連接"+info.getType(), Toast.LENGTH_SHORT).show();
                }else {
                    netInfo.setType("0");
//                    Toast.makeText(getActivity(), "非無線網連接"+info.getType(), Toast.LENGTH_SHORT).show();
                }
            }else {
                netInfo.setStatus("0");
//                Toast.makeText(getActivity(), "網絡未連接", Toast.LENGTH_SHORT).show();
            }
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:getWifiState(\""+netInfo.getStatus()+"\",\""+netInfo.getType()+"\")");
            }
        });
//        webView.loadUrl("javascript:getWifiState(\""+netInfo.getStatus()+"\",\""+netInfo.getType()+"\")");
        Log.d("mainFragment",JSON.toJSONString(netInfo));
        return netInfo;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CROP_PHOTO){
            if(resultCode==RESULT_OK){
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(imageUri, "image/*");
                intent.putExtra("scale", true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                getActivity().startActivityForResult(intent, CUT_PIC);// 启动裁剪程序
            }
        }else if(requestCode==REQUEST_CODE_PICK_IMAGE){
            Message message = uiHandler.obtainMessage(MainActivity.UPLOAD_PROGRESS_SHOW);
            uiHandler.sendMessage(message);

            Uri uri = data.getData();
            uri=geturi(data);
            Bitmap bit = null;
            try {
                bit = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri));
                String[] proj = { MediaStore.Images.Media.DATA };
                Cursor actualimagecursor = getActivity().getContentResolver().query(uri,proj,null,null,null);
                int actual_image_column_index =actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                actualimagecursor.moveToFirst();
                String img_path = actualimagecursor.getString(actual_image_column_index);
                File file = new File(img_path);
                Uri fileUri = Uri.fromFile(file);
                UploadHelper.upImage( Uri.decode(fileUri.toString()),serverUrl,mHandler,uiHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(requestCode==MainActivity.CODE){
            Log.e("MainFragment",data.getStringExtra("code"));
            webView.loadUrl("javascript:doScan(\""+data.getStringExtra("code")+"\")");
        }else if(requestCode==CUT_PIC){
            Message message = uiHandler.obtainMessage(MainActivity.UPLOAD_PROGRESS_SHOW);
            uiHandler.sendMessage(message);
            UploadHelper.upImage( Uri.decode(imageUri.toString()),serverUrl,mHandler,uiHandler);
        }
    }

    public void bindDeviceToWifi1(String userid , String deviceId, String wifi, String wifipsd, String brand){
//        LCOpenSDK_ConfigWifi.configWifiStart("2J05604PAD01821", "fast", "1234567890", "", mHandler);
        LCOpenSDK_ConfigWifi.configWifiStart(deviceId, wifi, wifipsd, "", mHandler);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==MainActivity.CAMERA){
            Intent openCameraIntent = new Intent(getActivity(),CaptureActivity.class);
            startActivityForResult(openCameraIntent, 0);
        }else if(requestCode==READ_STORE){
            ActivityCompat.requestPermissions((MainActivity)getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_STORE);
        }else if(requestCode==WRITE_STORE){
//            Intent intent = new Intent(Intent.ACTION_PICK);
//            intent.setType("image/*");//相片类型
//            getActivity().startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        }else if(requestCode==TAKE_PHOTO){
            output = CameraHelper.takePhoto();
            imageUri=Uri.fromFile(output);
//            imageUri=CameraHelper.takePhoto();
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getActivity(),getActivity().getApplicationContext().getPackageName() + ".provider", output));
//            } else {
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//            }


            getActivity().startActivityForResult(intent, CROP_PHOTO);
        }
    }

    private Uri geturi(android.content.Intent intent) {
        Uri uri = intent.getData();
        String type = intent.getType();
        if (uri.getScheme().equals("file") && (type.contains("image/"))) {
            String path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = getActivity().getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
                        .append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[] { MediaStore.Images.ImageColumns._ID },
                        buff.toString(), null, null);
                int index = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    // set _id value
                    index = cur.getInt(index);
                }
                if (index == 0) {
                    // do nothing
                } else {
                    Uri uri_temp = Uri
                            .parse("content://media/external/images/media/"
                                    + index);
                    if (uri_temp != null) {
                        uri = uri_temp;
                    }
                }
            }
        }
        return uri;
    }

    public boolean back(){
        if(webView.canGoBack()){
            webView.loadUrl("javascript:bk()");
        }
        return true;
    }

}
