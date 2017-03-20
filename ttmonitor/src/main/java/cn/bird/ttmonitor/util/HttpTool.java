package cn.bird.ttmonitor.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;


/**
 * Http工具
 */
public final class HttpTool {
	private static Logger logger=LoggerFactory.getLogger(HttpTool.class);
	
	public static final String OK = String.valueOf( HttpURLConnection.HTTP_OK );
	private static final String METHOD_POST = "POST";
	private static final String METHOD_GET = "GET";
	private static MyX509TrustManager x509TrusManager = new MyX509TrustManager();
	private static MyHostnameVerifier hostnameVerifier = new MyHostnameVerifier();
	private static SSLContext sslContext = null;
	static{
		try{
			sslContext = SSLContext.getInstance( "TLS" );
			X509TrustManager[] xtmArray = new X509TrustManager[]{ x509TrusManager };
			sslContext.init( null,xtmArray,new java.security.SecureRandom() );
			if( sslContext!=null ){
				HttpsURLConnection.setDefaultSSLSocketFactory( sslContext.getSocketFactory() );
			}
			HttpsURLConnection.setDefaultHostnameVerifier( hostnameVerifier );
		}catch( Exception e ){
			logger.error("exception", e);
		}
	}
	
	
	public static void logReqAndResp(String url,String params,String[] response){
		try{
			logger.debug("|RequestURL="+url+"|Params="+params+"|Response="+JSON.toJSONString(response));
		}catch (Exception e) {
			logger.error("e",e);
		}
	}

	/**
	 * 发送GET请求
	 * @param url
	 *        请求地址，参数包含在地址后面，如http://www.abc.com?param1=1&param2=2
	 * @param encode
	 *        字符集
	 * @param connectTimeout
	 *        连接超时时间
	 * @param responseTimeout
	 *        响应超时时间
	 * @return 返回长度为2的字符数组：[HTTP状态码，返回的数据]。 举例:["200","welcome!"]、["404",null]、["500",null]
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static String[] get( String url,String encode,int connectTimeout,int responseTimeout ) throws MalformedURLException, IOException {
		HttpURLConnection con = null;
		InputStream in = null;
		String[] response = new String[2];
		try{
			con = (HttpURLConnection)( new URL( url ).openConnection() );
			con.setDoInput( true );
			con.setRequestMethod( METHOD_GET );
			con.setConnectTimeout( connectTimeout );// 连接超时时间
			con.setReadTimeout( responseTimeout );// 响应超时时间
			if( HttpURLConnection.HTTP_OK==con.getResponseCode() ){
				response[0] = OK;
				in = con.getInputStream();
				BufferedReader reader = new BufferedReader( new InputStreamReader( in,encode ) );
				StringBuilder buffer = new StringBuilder();
				String line = null;
				while( ( line = reader.readLine() )!=null ){
					buffer.append( line );
				}
				response[1] = buffer.toString();
			}else{
				response[0] = String.valueOf( con.getResponseCode() );
			}
		}finally{
			if( in!=null ){
				try{
					in.close();
				}catch( Exception e ){
				}
			}
			if( con!=null ){
				con.disconnect();
			}
		}
		logReqAndResp(url,"",response);
		return response;
	}

	/**
	 * 发送POST请求
	 * @param url
	 *        请求地址
	 * @param requestData
	 *        请求数据
	 * @param charset
	 *        字符集
	 * @param connectTimeout
	 *        连接超时时间
	 * @param responseTimeout
	 *        响应超时时间
	 * @return 返回长度为2的字符数组：[HTTP状态码，返回的数据]。 举例:["200","welcome!"]、["404",null]、["500",null]
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static String[] post( String url,String requestData,String charset,int connectTimeout,int responseTimeout ) throws MalformedURLException, IOException {
		HttpURLConnection con = null;
		OutputStream out = null;
		InputStream in = null;
		String[] response = new String[2];
		try{
			con = (HttpURLConnection)( new URL( url ).openConnection() );
			con.setDoInput( true );
			con.setDoOutput( true );
			con.setRequestMethod( METHOD_POST );
			con.setConnectTimeout( connectTimeout );// 连接超时时间
			con.setReadTimeout( responseTimeout );// 响应超时时间
			out = con.getOutputStream();
			out.write( requestData.getBytes( charset ) );
			out.flush();
			if( HttpURLConnection.HTTP_OK==con.getResponseCode() ){
				response[0] = OK;
				in = con.getInputStream();
				BufferedReader reader = new BufferedReader( new InputStreamReader( in,charset ) );
				StringBuilder buffer = new StringBuilder( "" );
				String line = null;
				while( ( line = reader.readLine() )!=null ){
					buffer.append( line );
				}
				response[1] = buffer.toString();
			}else{
				response[0] = String.valueOf( con.getResponseCode() );
			}
		}finally{
			if( out!=null ){
				try{
					out.close();
				}catch( IOException e ){
				}
			}
			if( in!=null ){
				try{
					in.close();
				}catch( IOException e ){
				}
			}
			if( con!=null ){
				con.disconnect();
			}
		}
		logReqAndResp(url,requestData,response);
		return response;
	}
	
	public static String[] post( String url,String requestData,String charset,int connectTimeout,int responseTimeout,Map<String,String> header ) throws MalformedURLException, IOException {
		HttpURLConnection con = null;
		OutputStream out = null;
		InputStream in = null;
		String[] response = new String[2];
		try{
			con = (HttpURLConnection)( new URL( url ).openConnection() );
			con.setDoInput( true );
			con.setDoOutput( true );
			con.setRequestMethod( METHOD_POST );
			if( header!=null ){
				for( Map.Entry<String,String> entry : header.entrySet() ){
					con.setRequestProperty( entry.getKey(),entry.getValue() );
				}
			}
			con.setConnectTimeout( connectTimeout );// 连接超时时间
			con.setReadTimeout( responseTimeout );// 响应超时时间
			out = con.getOutputStream();
			out.write( requestData.getBytes( charset ) );
			out.flush();
			if( HttpURLConnection.HTTP_OK==con.getResponseCode() ){
				response[0] = OK;
				in = con.getInputStream();
				BufferedReader reader = new BufferedReader( new InputStreamReader( in,charset ) );
				StringBuilder buffer = new StringBuilder( "" );
				String line = null;
				while( ( line = reader.readLine() )!=null ){
					buffer.append( line );
				}
				response[1] = buffer.toString();
			}else{
				response[0] = String.valueOf( con.getResponseCode() );
			}
		}finally{
			if( out!=null ){
				try{
					out.close();
				}catch( IOException e ){
				}
			}
			if( in!=null ){
				try{
					in.close();
				}catch( IOException e ){
				}
			}
			if( con!=null ){
				con.disconnect();
			}
		}
		logReqAndResp(url,requestData,response);
		return response;
	}
	
	/**
	 * post请求上传文件
	 */
	public static String[] postFile(String url,HashMap<String, Object> params,HashMap<String, File> fileParams) throws IOException{
		String charset="utf-8";
		HttpURLConnection con=null;
		DataOutputStream out=null;
		InputStream in = null;
		String[] response = new String[2];
		String boundary="---"+System.currentTimeMillis()+"---";//分割符
		String LINE_FEED="\r\n";//换行符
		try {
			System.out.println("URL:"+url);
			con = (HttpURLConnection) new URL(url).openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod(METHOD_POST);
//			con.setConnectTimeout(connectTimeout);
//			con.setReadTimeout(responseTimeout);
			con.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
			
			out=new DataOutputStream(con.getOutputStream());
			
			//提交表单
			if(params!=null && params.size()>0){
				Iterator<Entry<String, Object>> iterator=params.entrySet().iterator();
				while(iterator.hasNext()){
					Entry<String, Object> entry=iterator.next();
					StringBuilder builder=new StringBuilder();
					builder.append("--" + boundary).append(LINE_FEED);
					builder.append("Content-Disposition: form-data; name=").append(entry.getKey()).append(LINE_FEED);
					builder.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
					builder.append(LINE_FEED);
					builder.append(entry.getValue()).append(LINE_FEED);
					out.write(builder.toString().getBytes());
					out.flush();
				}
			}
			
			//上传文件
			if(fileParams!=null && fileParams.size()>0){
				Iterator<Entry<String, File>> fileIterator= fileParams.entrySet().iterator();
				while(fileIterator.hasNext()){
					Entry<String, File> entry=fileIterator.next();
					String fileName=entry.getValue().getName();
					//字段类型，描述
					StringBuilder builder=new StringBuilder();
					builder.append("--"+boundary).append(LINE_FEED);
					builder.append("Content-Disposition:form-data;name=").append(entry.getKey()).
					append(";filename='").append(fileName).append(LINE_FEED);
					builder.append("Content-Type:").append(URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
					builder.append(LINE_FEED);
					out.write(builder.toString().getBytes());
					//字段内容
					FileInputStream fileInputStream=new FileInputStream(entry.getValue());
					byte[] b=new byte[4096];
					int site=-1;
					while((site=fileInputStream.read(b))!=-1){
						out.write(b,0,site);
					}
					out.flush();
					fileInputStream.close();
				}
			}
			//收尾
			out.write((LINE_FEED+"--"+boundary+"--"+LINE_FEED).toString().getBytes());
			out.flush();
			
			if(HttpURLConnection.HTTP_OK == con.getResponseCode()){
				response[0] = OK;
				in = con.getInputStream();
				BufferedReader reader = new BufferedReader( new InputStreamReader( in,"utf-8" ) );
				StringBuilder buffer = new StringBuilder( "" );
				String line = null;
				while( ( line = reader.readLine() )!=null ){
					buffer.append( line );
				}
				response[1] = buffer.toString();
			}else{
				response[0] = String.valueOf( con.getResponseCode() );
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if( out!=null ){
				out.close();
			}
			if( in!=null ){
				try{
					in.close();
				}catch( IOException e ){
				}
			}
			if( con!=null ){
				con.disconnect();
			}
		}
		logReqAndResp(url,JSON.toJSONString(params),response);
		return response;
		
	}

	/**
	 * 发送HTTPS POST请求
	 * @param url
	 *        请求地址
	 * @param requestData
	 *        请求数据
	 * @param charset
	 *        字符集
	 * @param connectTimeout
	 *        连接超时时间
	 * @param responseTimeout
	 *        响应超时时间
	 * @return 返回长度为2的字符数组：[HTTP状态码，返回的数据]。 举例:["200","welcome!"]、["404",null]、["500",null]
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static String[] httpsPost( String url,String requestData,String charset,int connectTimeout,int responseTimeout ) throws MalformedURLException, IOException {
		HttpsURLConnection conn = null;
		InputStream in = null;
		OutputStream out = null;
		String[] response = new String[2];
		try{
			conn = (HttpsURLConnection)new URL( url ).openConnection();
			conn.setHostnameVerifier( hostnameVerifier );
			conn.setSSLSocketFactory( sslContext.getSocketFactory() );
			conn.setRequestMethod( METHOD_POST );
			conn.setDoInput( true );
			conn.setDoOutput( true );
			conn.setConnectTimeout( connectTimeout );
			conn.setReadTimeout( responseTimeout );
			out = conn.getOutputStream();
			out.write( requestData.getBytes( charset ) );
			out.flush();
			if( conn.getResponseCode()==HttpURLConnection.HTTP_OK ){
				response[0] = OK;
				in = conn.getInputStream();
				BufferedReader reader = new BufferedReader( new InputStreamReader( in,charset ) );
				StringBuilder buffer = new StringBuilder();
				String line = null;
				while( ( line = reader.readLine() )!=null ){
					buffer.append( line );
				}
				response[1] = buffer.toString();
			}else{
				response[0] = String.valueOf( conn.getResponseCode() );
			}
		}finally{
			if( out!=null ){
				try{
					out.close();
				}catch( IOException e ){
				}
			}
			if( in!=null ){
				try{
					in.close();
				}catch( IOException e ){
				}
			}
			if( conn!=null ){
				conn.disconnect();
			}
		}
		logReqAndResp(url,requestData,response);
		return response;
	}

	/**
	 * 发送HTTPS Get请求
	 * @param url
	 *        请求地址
	 * @param charset
	 *        字符集
	 * @param connectTimeout
	 *        连接超时时间
	 * @param responseTimeout
	 *        响应超时时间
	 * @return 返回长度为2的字符数组：[HTTP状态码，返回的数据]。 举例:["200","welcome!"]、["404",null]、["500",null]
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static String[] httpsGet( String url,String charset,int connectTimeout,int responseTimeout ) throws MalformedURLException, IOException {
		HttpsURLConnection conn = null;
		InputStream in = null;
		String[] response = new String[2];
		try{
			conn = (HttpsURLConnection)new URL( url ).openConnection();
			conn.setHostnameVerifier( hostnameVerifier );
			conn.setSSLSocketFactory( sslContext.getSocketFactory() );
			conn.setRequestMethod( METHOD_GET );
			conn.setDoInput( true );
			conn.setConnectTimeout( connectTimeout );
			conn.setReadTimeout( responseTimeout );
			if( conn.getResponseCode()==HttpURLConnection.HTTP_OK ){
				response[0] = OK;
				in = conn.getInputStream();
				BufferedReader reader = new BufferedReader( new InputStreamReader( in,charset ) );
				StringBuilder buffer = new StringBuilder();
				String line = null;
				while( ( line = reader.readLine() )!=null ){
					buffer.append( line );
				}
				response[1] = buffer.toString();
			}else{
				response[0] = String.valueOf( conn.getResponseCode() );
			}
		}finally{
			if( in!=null ){
				try{
					in.close();
				}catch( IOException e ){
				}
			}
			if( conn!=null ){
				conn.disconnect();
			}
		}
		logReqAndResp(url,"",response);
		return response;
	}
	private static class MyX509TrustManager implements X509TrustManager {
		public MyX509TrustManager() {
		}

		public void checkClientTrusted( X509Certificate[] chain,String authType ) {
		}

		public void checkServerTrusted( X509Certificate[] chain,String authType ) {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}
	private static class MyHostnameVerifier implements HostnameVerifier {
		public MyHostnameVerifier() {
		}

		public boolean verify( String hostname,SSLSession session ) {
			return true;
		}
	}
}
