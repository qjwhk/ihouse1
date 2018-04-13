package com.lierda.kesi.ihouse.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2017/12/19.
 */

public class Md5 {

    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5= MessageDigest.getInstance("MD5");
        String SMG="{ \"title \":\"postman  cs\",  \"content \":\"postman cs  content\",  \"accept_time\":  [ { “start”:{“hour”:”13”,“min”:”00”}, ”end”: {“hour”:”23”,“min”:”00”} }, { “start”:{“hour”:”00”,”min”:”00”}, ”end”: {“hour”:”09”,“min”:”00”} } ], \"n_id\":0,  \"builder_id\":0,  \"ring\":1，  \"ring_raw\":\"ring\",  \"vibrate\":1,  \"lights\":1 \"clearable\":1,  \"icon_type\":0  \"icon_res\":\"xg\", \"style_id\":1  \"small_icon\":\"xg\" \"action\":{  \"action_type \": 1,  \"activity \": \"com.lierda.kesi.ihouse.MainActivity\" \"aty_attr \":  { \"if\":0,  \"pf\":0,  } \"browser\": {\"url\": \"xxxx \",\"confirm\": 1},  “intent”: “xxx” } }";
        String msg="{\"content\":\"this is content\",\"title\":\"this is title\", \"vibrate\":1,\"builder_id\":0}";
        String str=byteToString(md5.digest(("POSTopenapi.xg.qq.com/v2/push/single_deviceaccess_id=2100273262" +
                "device_token=dccacff4f14916a44b508e16c17d94a71a0b95d1" +
                "message="+msg+""+
                "message_type=1"+
                "timestamp=1513665179780f142d8154fcaa9cfe8b750c1cc270").getBytes()));
//        System.out.println(str.toLowerCase());
//        System.out.print(str.length());
//        String xg="GETopenapi.xg.qq.com/v2/push/single_deviceaccess_id=2100250470device_token=76501cd0277cdcef4d8499784a819d4772e0fddemessage={\"title\":\"测试消息\",\"content\":\"来自restapi的单推接口测试消息\"}message_type=1timestamp=1502356505f1fa8b11f540794bf13e10d499ac5c36";
//        String s=URLEncoder.encode("access_id=2100250470device_token=76501cd0277cdcef4d8499784a819d4772e0fddemessage={\"title\":\"测试消息\",\"content\":\"来自restapi的单推接口测试消息\"}message_type=1timestamp=1502356505f");
//        xg="GETopenapi.xg.qq.com/v2/push/single_device"+s+"1fa8b11f540794bf13e10d499ac5c36";
//        System.out.println(URLEncoder.encode("access_id=2100250470device_token=76501cd0277cdcef4d8499784a819d4772e0fddemessage={\"title\":\"测试消息\",\"content\":\"来自restapi的单推接口测试消息\"}message_type=1timestamp=1502356505f"));
     /*   String s1="GETopenapi.xg.qq.com/v2/push/single_device";
        String s2="access_id=2100250470";
        String s3="device_token=d213a035da3c68cb388737dad749f4c188431a66";
        String s4="message={\"title\":\"测试消息\",\"content\":\"来自restapi的单推接口测试消息\"}";
        String s5="message_type=1";
        String s6="timestamp=1502356505f1fa8b11f540794bf13e10d499ac5c36";*/
//        String xg=s1+s2+s3+s4+s5+s6;
        String a1="POSTopenapi.xg.qq.com/v2/push/single_device";
        String a2="access_id=2100273262";
        String a3="device_token=d213a035da3c68cb388737dad749f4c188431a66";
        String a4="message={\"title\":\"now  is  10：24\",\"content\":\"jiohscontent\",\"builder_id\":1,\"ring\":1,\"vibrate\":1,\"lights\":1,\"small_icon\":\"icon\"}";//,"action":{"action_type":1,"activity":"com.lierda.kesi.ihouse.activity.MainActivity"}
        String a5="message_type=1";
        String a6="timestamp=1521181026";
        String a7="780f142d8154fcaa9cfe8b750c1cc270";
        String t0=a1+a2+a3+a4+a5+a6+a7;

        String b0="POSTopenapi.xg.qq.com/v2/push/create_multipush";
        String b1="POSTopenapi.xg.qq.com/v2/push/device_list_multiple";
        String b2="access_id=2100273262";
        String b3="device_list=[\"dccacff4f14916a44b508e16c17d94a71a0b95d1\",\"b963ab03567b69aeffc8116eb4eb210236bd4bbf\"]";
        String b4="message={\"title\":\"全体命令1234\",\"content\":\"停止进攻\",\"lights\":1,\"ring\":1,\"vibrate\":1,\"action\":{\"action_type\":1,\"activity\":\"com.lierda.kesi.ihouse.MainActivity\"}}}";
        String b5="message_type=1";
        String b6="push_id=1514255155";
        String b7="timestamp=1514168438";
        String b8="780f142d8154fcaa9cfe8b750c1cc270";
        String t1=b0+b2+b4+b5+b7+b8;
        String t2=b1+b2+b3+b4+b5+b6+b7+b8;


        String qm1="POSTopenapi.xg.qq.com/v2/tags/batch_set";
        String qm2="access_id=2100273262";
        String qm3="tag_token_list=[[\"student\",\"6a7319c275198e0fb325560a5e0b97ce98f83745\"]]";
        String qm4="timestamp=1515484211";
        String qm5="780f142d8154fcaa9cfe8b750c1cc270";
        String t3=qm1+qm2+qm3+qm4+qm5;

        String bq1="POSTopenapi.xg.qq.com/v2/push/tags_device";
        String bq2="access_id=2100273262";
        String bq3="message={\"title\":\"四UI程序包\",\"content\":\"j什么没了\",\"builder_id\":1,\"ring\":1,\"vibrate\":1,\"lights\":1,\"small_icon\":\"icon\",\"action\":{\"action_type\":1,\"activity\":\"com.lierda.kesi.ihouse.activity.MainActivity\"}}";
        String bq4="message_type=1";
        String bq5="tags_list=[\"me\",\"xiaomi\"]";
        String bq6="tags_op=AND";
        String bq7="timestamp=1515485767";
        String bq8="780f142d8154fcaa9cfe8b750c1cc270";
        String t4=bq1+bq2+bq3+bq4+bq5+bq6+bq7+bq8;


        String a11="POSTopenapi.xg.qq.com/v2/tags/query_token_tags";
        String a12="access_id=2100273262";
        String a13="device_token=6a7319c275198e0fb325560a5e0b97ce98f83745";
        String a16="timestamp=1515485767";
        String a17="780f142d8154fcaa9cfe8b750c1cc270";
        String t5=a11+a12+a13+a16+a17;

        String a21="POSTopenapi.xg.qq.com/v2/tags/batch_del";
        String a22="access_id=2100273262";
        String a23="tag_token_list=[[\"student\",\"6a7319c275198e0fb325560a5e0b97ce98f83745\"]]";
        String a26="timestamp=1515485190";
        String a27="780f142d8154fcaa9cfe8b750c1cc270";
        String t6=a21+a22+a23+a26+a27;


        System.out.println(t0);
        String t="GETopenapi.xg.qq.com/v2/push/single_deviceaccess_id=2100250470device_token=76501cd0277cdcef4d8499784a819d4772e0fddemessage={\"title\":\"测试消息\",\"content\":\"来自restapi的单推接口测试消息\"}message_type=1timestamp=1513734547f1fa8b11f540794bf13e10d499ac5c36";
        System.out.print(byteToString(md5.digest(t0.getBytes())).toLowerCase());
    }
    private final static String[] strDigits = { "0", "1", "2", "3", "4", "5",
                         "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

       // 转换字节数组为16进制字串
                 private static String byteToString(byte[] bByte) {
                 StringBuffer sBuffer = new StringBuffer();
                 for (int i = 0; i < bByte.length; i++) {
                        sBuffer.append(byteToArrayString(bByte[i]));
                     }
                 return sBuffer.toString().toLowerCase();
             }

                  private static String byteToArrayString(byte bByte) {
                 int iRet = bByte;
                 if (iRet < 0) {
                         iRet += 256;
                     }
                 int iD1 = iRet /16;
                 int iD2 = iRet %16;
                 return strDigits[iD1] + strDigits[iD2];
             }









}
