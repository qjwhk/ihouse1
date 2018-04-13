package com.lierda.kesi.ihouse.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by qianjiawei on 2018/3/1.
 */

public class CameraHelper {

//    public static  Uri takePhoto(){
//        /**
//         * 最后一个参数是文件夹的名称，可以随便起
//         */
//        File file=new File(Environment.getExternalStorageDirectory(),"拍照");
//        if(!file.exists()){
//            file.mkdirs();
//        }
//        /**
//         * 这里将时间作为不同照片的名称
//         */
//        File output=new File(file,System.currentTimeMillis()+".jpg");
//
//        /**
//         * 如果该文件夹已经存在，则删除它，否则创建一个
//         */
//        try {
//            if (output.exists()) {
//                output.delete();
//            }
//            output.createNewFile();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        /**
//         * 隐式打开拍照的Activity，并且传入CROP_PHOTO常量作为拍照结束后回调的标志
//         * 将文件转化为uri
//         */
//        Uri imageUri = Uri.fromFile(output);
//        return imageUri;
////        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
////        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
////        getActivity().startActivityForResult(intent, CROP_PHOTO);
//
//    }

    public static  File takePhoto(){
        /**
         * 最后一个参数是文件夹的名称，可以随便起
         */
        File file=new File(Environment.getExternalStorageDirectory(),"拍照");
        if(!file.exists()){
            file.mkdirs();
        }
        /**
         * 这里将时间作为不同照片的名称
         */
        File output=new File(file,System.currentTimeMillis()+".jpg");

        /**
         * 如果该文件夹已经存在，则删除它，否则创建一个
         */
        try {
            if (output.exists()) {
                output.delete();
            }
            output.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         * 隐式打开拍照的Activity，并且传入CROP_PHOTO常量作为拍照结束后回调的标志
         * 将文件转化为uri
         */
        Uri imageUri = Uri.fromFile(output);
        return output;
//        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        getActivity().startActivityForResult(intent, CROP_PHOTO);

    }


    public static Bitmap makeRoundCorner(Bitmap bitmap)
    {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int left = 0, top = 0, right = width, bottom = height;
        float roundPx = height/2;
        if (width > height) {
            left = (width - height)/2;
            top = 0;
            right = left + height;
            bottom = height;
        } else if (height > width) {
            left = 0;
            top = (height - width)/2;
            right = width;
            bottom = top + width;
            roundPx = width/2;
        }
//        ZLog.i(TAG, "ps:"+ left +", "+ top +", "+ right +", "+ bottom);
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xff424242;
        Paint paint = new Paint();
        Rect rect = new Rect(left, top, right, bottom);
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
}
