package com.lierda.kesi.ihouse.util;

/**
 * Created by qianjiawei on 2018/1/16.
 */

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

public final class QRCodeUtil {
    private static final int BLACK = 0xff000000;

    public static Bitmap createQRCode(String str, final int width, final int height){
        MultiFormatWriter writer = new MultiFormatWriter();
        Hashtable<EncodeHintType, String> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, width, height, hints);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = parseBitMatrix(bitMatrix);
        return bitmap;
    }

    public static Bitmap parseBitMatrix(BitMatrix matrix) {
        final int QR_WIDTH = matrix.getWidth();
        final int QR_HEIGHT = matrix.getHeight();
        int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
//this we using qrcode algorithm
        for (int y = 0; y < QR_HEIGHT; y++) {
            for (int x = 0; x < QR_WIDTH; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * QR_WIDTH + x] = 0xff000000;
                } else {
                    pixels[y * QR_WIDTH + x] = 0xffffffff;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
        return bitmap;
    }

    public Result scanningImage(Bitmap bitmap){
        Result result=null;
        MultiFormatReader multiFormatReader=new MultiFormatReader();
        BitmapLuminanceSource source=new BitmapLuminanceSource(bitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        Map<DecodeHintType,Object> hints = new LinkedHashMap<DecodeHintType,Object>();
        // 解码设置编码方式为：utf-8，
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        //优化精度
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        //复杂模式，开启PURE_BARCODE模式
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        try {
            result = multiFormatReader.decode(bitmap1,hints);
            String text=result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }


}
