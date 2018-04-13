package com.lierda.kesi.ihouse.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/12/20.
 */
public class Okhttp {
    public static void main(String[] args) {
        new Okhttp().postHttp();
    }

    private void postHttp(){
        OkHttpClient mOkHttpClient=new OkHttpClient();
        RequestBody postBody= new FormBody.Builder()
                .add("size","10")
                .build();
        Request request=new Request.Builder()
                .url("")
                .post(postBody)
                .build();
        Call call=mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str=response.body().string();
                System.out.print(str);
            }
        });
    }

}
