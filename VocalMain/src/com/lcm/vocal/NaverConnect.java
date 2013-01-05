package com.lcm.vocal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class NaverConnect {
	private DefaultHttpClient httpClient;
	private HttpContext httpContext;
	private HttpResponse response;
	private HttpEntity entity;
	private boolean loggedIn = false;
//	private String custid;
//	private String passwd;
	
	public NaverConnect() {//String c, String p) {
//		custid = c;
//		passwd = p;
		httpContext = new BasicHttpContext();
		httpClient = new DefaultHttpClient();
	}
	
	public void login() throws Exception {
		HttpPost httpPost = new HttpPost(
//				"https://member.moneta.co.kr/Auth/SmLoginAuth.jsp");
				"http://search.naver.com/search.naver");

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("where", "nexearch"));
		nvps.add(new BasicNameValuePair("query", "lyrics"));
//		nvps.add(new BasicNameValuePair("target", ));
//		nvps.add(new BasicNameValuePair("custId", custid));
//		nvps.add(new BasicNameValuePair("passwd", passwd));
//		nvps.add(new BasicNameValuePair("returnURL",
//				"http://mmini.moneta.co.kr/cashbook/write/pay/list.do"));
//		nvps.add(new BasicNameValuePair("event_cd", ""));
//		nvps.add(new BasicNameValuePair("majority", "N"));
//		nvps.add(new BasicNameValuePair("style", "mMiga"));
//		nvps.add(new BasicNameValuePair("ipOnOff", "ipOff"));

		httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

		response = httpClient.execute(httpPost, httpContext);
		Log.e("NaverConnect","http request sent");
		entity = response.getEntity();
		if (entity != null) {
//			InputStream instream = entity.getContent();
//			int l;
//			byte[] tmp = new byte[2048];
//			while((l=instream.read(tmp))!= -1) {
			Log.e("NaverConnect",EntityUtils.toString(entity));
//			}
//            entity.consumeContent();
        }
		
//		if(httpClient.getCookieStore().getCookies().size() < 6) loggedIn = true;
//		else loggedIn = false;
	}
	
	public void connectToMonetaWritePage() throws Exception {
		HttpPost httpPost = new HttpPost(
				"http://mmini.moneta.co.kr/cashbook/write/pay/write-action.do");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		
		nvps.add(new BasicNameValuePair("regDate", "20110101"));  //일자
		nvps.add(new BasicNameValuePair("cashClsfy", "2")); // ??
		nvps.add(new BasicNameValuePair("goodsCd", ""));  //종류
		nvps.add(new BasicNameValuePair("remark", "this is for a test")); //내역
		nvps.add(new BasicNameValuePair("amount", "19800")); //금액
		nvps.add(new BasicNameValuePair("itemCode", "10013")); //분류
		nvps.add(new BasicNameValuePair("note", "hello")); //비고

		httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		response = httpClient.execute(httpPost, httpContext);
		entity = response.getEntity();
		if (entity != null) {
            entity.consumeContent();
        }
	}

	public void logout() throws Exception {
		HttpGet httpget = new HttpGet(
				"http://search.naver.com/search.naver?where=nexearch&query=lyrics");

		response = httpClient.execute(httpget, httpContext);
		entity = response.getEntity();
		if (entity != null) {
			Log.e("NaverConnect",EntityUtils.toString(entity));
            entity.consumeContent();
        }
		// When HttpClient instance is no longer needed,
		// shut down the connection manager to ensure
		// immediate deallocation of all system resources
		httpClient.getConnectionManager().shutdown();
	}
	
	
}
