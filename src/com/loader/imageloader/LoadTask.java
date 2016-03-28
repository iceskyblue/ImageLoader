package com.loader.imageloader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;




import android.os.AsyncTask;

public class LoadTask extends AsyncTask<String, Void, Void>
{

	public LoadTask(CompleteCallback callback)
	{
		this.mCallback = callback;
		this.mRequest = new HttpGet();
	}
	

	public void setTarget(Object target)
	{
		this.mTarget = target;
	}
	
	@Override
	protected void onPreExecute()
	{
		// TODO Auto-generated method stub
		super.onPreExecute();
		init();
	}
	
	private void init()
	{
		initHttpsClient();
		//initHttpClient();
		
	
	}
	
	private void initHttpsClient()
	{
		 KeyStore trustStore;
		 SSLSocketFactory sf = null;
		try
		{
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

	        sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

         HttpParams params = new BasicHttpParams();

         HttpConnectionParams.setConnectionTimeout(params, HTTP_CONNECT_TIMEOUT);
         HttpConnectionParams.setSoTimeout(params, HTTP_SOCKET_TIMEOUT);

         HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
         HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

         SchemeRegistry registry = new SchemeRegistry();
         registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
         registry.register(new Scheme("https", sf, 443));

         ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
         mClient = new DefaultHttpClient(ccm, params);
	}

//	private void initHttpClient()
//	{
//		BasicHttpParams httpParameters = new BasicHttpParams();
//        
//	    HttpConnectionParams.setConnectionTimeout(httpParameters, HTTP_CONNECT_TIMEOUT);
//	    HttpConnectionParams.setSoTimeout(httpParameters, HTTP_SOCKET_TIMEOUT);
//	    mClient = new DefaultHttpClient(httpParameters);
//	}
	
	@Override
	protected Void doInBackground(String... params)
	{
		// TODO Auto-generated method stub
		InputStream inputStream = null;
		mIsError = true;
		if(null == params || params.length < 1)
		{
			return null;
		}
		
		String url = params[0];
		mUrl = url;
		try
		{
			URI uri = URI.create(url);
			mRequest.setURI(uri);
			mRequest.setHeader("User-Agent", System.getProperties().getProperty("http.agent"));
			
			HttpResponse response = mClient.execute(mRequest);
			

			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
				mIsError = false;	
			}
			
			inputStream = response.getEntity().getContent();
			Header header = response.getFirstHeader("Content-Encoding");
	        if (header != null && header.getValue().toLowerCase().indexOf("gzip") > -1) 
	        {
	        	inputStream = new GZIPInputStream(inputStream);
	        }
	        
			receiveData(new BufferedInputStream(inputStream));
			inputStream.close();
			
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally
		{			
			mRequest.abort();
		}
		
		return null;
	}
	
	
	
	@Override
	protected void onPostExecute(Void result)
	{
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
		if(null != this.mCallback)
		{
			if(!this.mIsError)
			{				
				this.mCallback.complete(mResult, mTarget, mUrl);
			}
			else
			{
				this.mCallback.error(mResult, mTarget, mUrl);
				//mResult.toString("UTF-8");
			
			}
		}
	}

	private void receiveData(BufferedInputStream inputStream) throws Exception
	{
		mResult = new ByteArrayOutputStream();
		int len = 0;
		byte [] buffer = new byte[1024];
		
		while((len = inputStream.read(buffer)) > 0)
		{
			mResult.write(buffer, 0, len);
		}
		mResult.close();
	}
	
	public HttpPost setPostRequestParams(HttpEntity mpEntity)
	{
		HttpPost post = new HttpPost();
		
	
		
		post.setEntity(mpEntity);
		this.mRequest = post;
		
		return post;
	}
	
	

	
	@Override
	protected void onCancelled()
	{
		// TODO Auto-generated method stub
		super.onCancelled();
	}

	
	public ByteArrayOutputStream  synExcute(String url)
	{
		this.mRequest = new HttpGet();
		init();
		doInBackground(url);
		
		return this.mResult;
	}
	
	private HttpRequestBase mRequest;
	private DefaultHttpClient mClient;
	private CompleteCallback mCallback;
	private ByteArrayOutputStream mResult;
	private boolean mIsError;
	private Object mTarget;
	private String mUrl;
	private static final int HTTP_CONNECT_TIMEOUT = 5 * 1000;
	private static final int HTTP_SOCKET_TIMEOUT = 20 * 1000;
	
	public interface CompleteCallback
	{
		public void complete(ByteArrayOutputStream out, Object target, String url);
		public void error(ByteArrayOutputStream err, Object target, String url);
	}
	
	
	public static class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,
                KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
}
