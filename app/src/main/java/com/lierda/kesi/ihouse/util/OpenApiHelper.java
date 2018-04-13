package com.lierda.kesi.ihouse.util;

import android.os.Handler;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class OpenApiHelper {

	public final static String tag = "OpenApiHelper";


	public static void getAccessToken(final String host,
                                      final String phoneNumber, final String appid,
                                      final String appsecret, final Handler handler) {
		// 拼装url
		String url = "";
		if (host.endsWith(":443")) {
			url = "https://" + host + "/openapi/accessToken";
		} else {
			url = "http://" + host + "/openapi/accessToken";
		}
		Log.d(tag,url);
		getToken(url, phoneNumber, appid, appsecret, handler);
	}

	public static void getUserToken(final String host,
                                    final String phoneNumber, final String appid,
                                    final String appsecret, final Handler handler) {
		// 拼装url
		String url = "";
		if (host.endsWith(":443")) {
			url = "https://" + host + "/openapi/userToken";
		} else {
			url = "http://" + host + "/openapi/userToken";
		}
		Log.d(tag,url);
		getToken(url, phoneNumber, appid, appsecret, handler);
	}

	public static void userBindSms(final String host, final String phoneNumber,
                                   final String appid, final String appsecret, final Handler handler) {
		String url = "";
		if (host.endsWith(":443")) {
			url = "https://" + host + "/openapi/userBindSms";
		} else {
			url = "http://" + host + "/openapi/userBindSms";
		}
		Log.d(tag,url);
		// 生成HttpRequest对象
		HttpRequestBase httpRequest;
		HttpPost httpPost = new HttpPost(url);
		try {
			JSONObject body = new JSONObject();
			String data = "{phone: \"" + phoneNumber + "\"}";
			body.put("params", new JSONObject(data));
			body.put("id", "1");// id号 随机值
			
			body.put(
					"system",
					new JSONObject(SignHelper.getSystem(data, appid, appsecret,
							"1.1")));
			httpPost.setEntity(new StringEntity(body.toString(), "utf-8"));
			Log.d(tag, body.toString());
			httpPost.setHeader("Content-Type", "application/json");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpRequest = httpPost;

		// 设置超时时间
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);
		httpRequest.setParams(params);
		SingleClientConnManager manager = null;
		int code = -1;
		String result = "";
		try {
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", SSLSocketFactory
					.getSocketFactory(), 443));
			manager = new SingleClientConnManager(httpRequest.getParams(),
					registry);
			HttpClient httpclient = new DefaultHttpClient(manager,
					httpRequest.getParams());

			HttpResponse httpResponse = httpclient.execute(httpRequest);
			StatusLine status = httpResponse.getStatusLine();
			code = status.getStatusCode();
			if (code == HttpStatus.SC_OK) {
				// 状态为正常时，进行body内容获取
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					result = EntityUtils.toString(entity, "UTF-8");
					Log.d(tag, result);
					JSONObject res = new JSONObject((String) result);

					// 简单做法 直接传出
					if (res.getJSONObject("result").getString("code")
							.equals("0")) {
						code = 0;
						result = res.getJSONObject("result").getString("msg");
					}

					else {
						code = -1;
						result = res.getJSONObject("result").getString("msg");
					}
				}
			} else {
				result = status.getReasonPhrase();
			}
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			manager.shutdown();
		}
		handler.obtainMessage(code, result).sendToTarget();
	}

	public static void userBind(final String host, final String phoneNumber,
                                final String appId, String appSecret, final String smsCode,
                                final Handler handler) {
		String url = "";
		if (host.endsWith(":443")) {
			url = "https://" + host + "/openapi/userBind";
		} else {
			url = "http://" + host + "/openapi/userBind";
		}
		Log.d(tag,url);
		// 生成HttpRequest对象
		HttpRequestBase httpRequest;
		HttpPost httpPost = new HttpPost(url);
		try {
			JSONObject body = new JSONObject();
			String data = "{phone: \"" + phoneNumber + "\",smsCode:\"" + smsCode
					+ "\"}";
			body.put("params", new JSONObject(data));
			body.put("id", "1");// id号 随机值
			body.put(
					"system",
					new JSONObject(SignHelper.getSystem(data, appId, appSecret,
							"1.1")));
			httpPost.setEntity(new StringEntity(body.toString(), "utf-8"));
			Log.d(tag, body.toString());
			httpPost.setHeader("Content-Type", "application/json");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpRequest = httpPost;

		// 设置超时时间
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);
		httpRequest.setParams(params);
		SingleClientConnManager manager = null;
		int code = -1;
		String result = "";
		try {
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", SSLSocketFactory
					.getSocketFactory(), 443));
			manager = new SingleClientConnManager(httpRequest.getParams(),
					registry);
			HttpClient httpclient = new DefaultHttpClient(manager,
					httpRequest.getParams());

			HttpResponse httpResponse = httpclient.execute(httpRequest);
			StatusLine status = httpResponse.getStatusLine();
			code = status.getStatusCode();
			if (code == HttpStatus.SC_OK) {
				// 状态为正常时，进行body内容获取
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					result = EntityUtils.toString(entity, "UTF-8");
					Log.d(tag, result);
					JSONObject res = new JSONObject((String) result);
					// 简单做法 直接传出
					if (res.getJSONObject("result").getString("code")
							.equals("0")) {
						code = 0;
						result = res.getJSONObject("result").getString("msg");
					}

					else {
						code = -1;
						result = res.getJSONObject("result").getString("msg");
					}
				}
			} else {
				result = status.getReasonPhrase();
			}

		} catch (Exception e) {
			e.printStackTrace();
			result = e.getMessage();
		} finally {
			manager.shutdown();
		}
		handler.obtainMessage(code, result).sendToTarget();
	}

	private static void getToken(final String host, final String phoneNumber,
                                 final String appid, final String appsecret, final Handler handler) {

		// 生成HttpRequest对象
		HttpRequestBase httpRequest;
		HttpPost httpPost = new HttpPost(host);
		try {
			JSONObject body = new JSONObject();
			String data = "{phone:\"" + phoneNumber + "\"}";
			body.put("params", new JSONObject(data));
			body.put("id", "1");// id号 随机值
			body.put(
					"system",
					new JSONObject(SignHelper.getSystem(data, appid, appsecret,
							"1.1")));
			httpPost.setEntity(new StringEntity(body.toString(), "utf-8"));
			Log.d(tag, body.toString());
			httpPost.setHeader("Content-Type", "application/json");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpRequest = httpPost;

		// 设置超时时间
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);
		httpRequest.setParams(params);
		SingleClientConnManager manager = null;
		int code = -1;
		String result = "";
		try {
			// 初始化SSL环境
			// final SSLContext context = SSLContext.getInstance("TLS");
			// context.init(null, new TrustManager[] {}, null);
			// registry.register(new Scheme("http",
			// PlainSocketFactory.getSocketFactory(), 81));
			// registry.register(new Scheme("https", new SSLSocketFactory(null){
			// public java.net.Socket createSocket() throws java.io.IOException
			// {
			// return context.getSocketFactory().createSocket();
			// };
			// public java.net.Socket createSocket(Socket socket, String host,
			// int port, boolean autoClose) throws IOException
			// ,UnknownHostException {
			// return context.getSocketFactory().createSocket(socket, host,
			// port, autoClose);
			// };
			// }, 443));
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", SSLSocketFactory
					.getSocketFactory(), 443));
			manager = new SingleClientConnManager(httpRequest.getParams(),
					registry);
			// 发送请求、接收响应
			HttpClient httpclient = new DefaultHttpClient(manager,
					httpRequest.getParams());

			HttpResponse httpResponse = httpclient.execute(httpRequest);
			StatusLine status = httpResponse.getStatusLine();
			code = status.getStatusCode();
			if (code == HttpStatus.SC_OK) {
				// 状态为正常时，进行body内容获取
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					result = EntityUtils.toString(entity, "UTF-8");
					Log.d(tag, result);
					JSONObject res = new JSONObject((String) result);
					// 简单做法 直接传出
					if (res.getJSONObject("result").getString("code")
							.equals("0")) {
						code = 0;
						if (res.getJSONObject("result").getJSONObject("data")
								.has("accessToken")) {
							result = res.getJSONObject("result")
									.getJSONObject("data")
									.getString("accessToken");
						} else {
							result = res.getJSONObject("result")
									.getJSONObject("data")
									.getString("userToken");
						}
					} else {
						code = -1;
						result = res.getJSONObject("result").getString("msg");
						// 界面展示和业务需要 特殊处理
						if (res.getJSONObject("result").getString("code")
								.equals("TK1004"))
							code = 1;
						if (res.getJSONObject("result").getString("code")
								.equals("TK1006"))
							code = 1;
						
					}
				}
			} else {
				result = status.getReasonPhrase();
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = e.getMessage();
		} finally {
			manager.shutdown();
		}
		handler.obtainMessage(code, result).sendToTarget();
	}
}
