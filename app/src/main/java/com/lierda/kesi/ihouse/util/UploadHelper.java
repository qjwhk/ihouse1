package com.lierda.kesi.ihouse.util;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lierda.kesi.ihouse.activity.MainActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by qianjiawei on 2018/3/8.
 */

public class UploadHelper {
    public static final int Success=112;


    public static void imageUpload(Bitmap bit) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.PNG, 100, bos);
        bos.close();
        byte[] buffer =bos.toByteArray();
        String photo = Base64.encodeToString(buffer, 0, buffer.length, Base64.DEFAULT);
    }

    public static void upImage(String imageUrl, String serverUrl, final Handler handler, final Handler handler1) {
        OkHttpClient mOkHttpClent = new OkHttpClient();
        File file = new File(imageUrl.replace("file:",""));
        boolean a=file.exists();
        String name=file.getName();
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", name,
                        RequestBody.create(MediaType.parse("image/*"), file))
                .addFormDataPart("pn", "upload")
                .addFormDataPart("dir", "ksheader");

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();
        Call call = mOkHttpClent.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                Log.e(TAG, "onFailure: "+e );
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
//                    }
//                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String imgUrl = response.body().string();
                Message m=new Message();
                m.what=Success;
                m.obj=((JSONObject) JSON.parse(imgUrl)).getString("data");
                handler.sendMessage(m);
                Message message = handler1.obtainMessage(MainActivity.UPLOAD_PROGRESS_HIDE);
                handler1.sendMessage(message);
//                Log.e(TAG, "成功"+response);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this, "成功", Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        });

    }

    public static void upImage1(String imageUrl, final String serverUrl, final Handler handler) {
        final File file = new File(imageUrl.replace("file:",""));
        String name=file.getName();
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    StringBuffer s = new StringBuffer();
                        BasicHttpParams httpParams = new BasicHttpParams();
                        HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
                        HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);
                        HttpClient client = new DefaultHttpClient(httpParams);
                        HttpPost post = new HttpPost(serverUrl);
                        MultipartEntity entity = new MultipartEntity();
                        entity.addPart("pn", new StringBody("upload", Charset.forName("UTF-8")));
                        entity.addPart("dir", new StringBody("ksheader ", Charset.forName("UTF-8")));
                        entity.addPart("file", new FileBody(file, "image/*"));
                        post.setEntity(entity);
                        HttpResponse resp = client.execute(post);
                        String jsonStr = EntityUtils.toString(resp.getEntity());

                        System.out.println("Response:" + jsonStr);
                        JSONObject object = (JSONObject) JSON.parse(jsonStr);
                        s.append(object.getString("data"));

                    Message msg = Message.obtain();
                    msg.what=Success;
                    msg.obj=s.toString();
                    handler.sendMessage(msg);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

}
