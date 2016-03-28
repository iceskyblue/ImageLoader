package com.loader.imageloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.support.v4.util.LruCache;

public class ImageCache
{
	private MemeryCache mCache;
	
	private ImageCache()
	{
		 int maxMemory = (int) Runtime.getRuntime().maxMemory();    
	     int mCacheSize = maxMemory / 8; 
	     mCache = new MemeryCache(mCacheSize);
	}
	
	public static void setPath(String path){
		if(null == mCachePath){			
			mCachePath = path;
		}
	}
	
	public static ImageCache getInstance()
	{
		File imageDir = new File(mCachePath);
		if((!imageDir.exists()) || (!imageDir.isDirectory()))
		{
			imageDir.mkdir();
		}
		
		return mThis;
	}
	
	public void saveImage(String token, ByteArrayOutputStream out)
	{
		clearCache();
		
		String name = getMD5String(token);
		try
		{
			FileOutputStream fos = new FileOutputStream(mCachePath + name);
			fos.write(out.toByteArray());
			fos.close();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Bitmap getImage(byte[] data, int w, int h){
		if(0>=w || 0>=h){
			return null;
		}
		
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, opts);
		int wScale = opts.outWidth/w;
		int hScale = opts.outHeight/h;
		if((wScale <= 1 || hScale <= 1) && data.length < w*h*4*2){
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		}
		
		int sScale = wScale;
		int lScale = hScale;
		if(wScale > hScale){
			sScale = hScale;
			lScale = wScale;
		}
		
		opts.inJustDecodeBounds = false;
		if(sScale == 0){			
			opts.inSampleSize = lScale;
		}else if(sScale >= 2 && lScale/sScale <= 3){
			opts.inSampleSize = sScale;
		}else{
			opts.inSampleSize = lScale;
		}
		
		return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
	}
	
	public Bitmap getImage(String token)
	{
		String name = getMD5String(token);
		Bitmap cacheImg = mCache.get(name);
		if(null != cacheImg){
			return cacheImg;
		}
		
		Bitmap bmp = BitmapFactory.decodeFile(mCachePath + name);
		
		if(null == bmp)
		{
			delete(name);
		}
		
		if(null != bmp){
			mCache.put(name, bmp);
		}
		
		return bmp;
	}
	
	public Bitmap getImage(String token, int w, int h){
		if(0>=w || 0>=h){
			return getImage(token);
		}
		
		String name = getMD5String(token);
		Bitmap cacheImg = mCache.get(name);
		if(null != cacheImg){
			return cacheImg;
		}
		
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCachePath + name, opts);
		
		int wScale = opts.outWidth/w;
		int hScale = opts.outHeight/h;
		
		
		opts.inJustDecodeBounds = false;
		int sScale = wScale;
		int lScale = hScale;
		if(wScale > hScale){
			sScale = hScale;
			lScale = wScale;
		}
		
		if(sScale == 0){			
			opts.inSampleSize = lScale;
		}else if(sScale >= 2 && lScale/sScale <= 3){
			opts.inSampleSize = sScale;
		}else{
			opts.inSampleSize = lScale;
		}
		
		Bitmap bmp = BitmapFactory.decodeFile(mCachePath + name, opts);
		if(null != bmp){
			mCache.put(name, bmp);
		}
		return bmp;
	}
	
	public boolean isExists(String token)
	{
		String name = getMD5String(token);
		File file = new File(mCachePath + name);
		
		return file.exists(); 
	}
	
	
	private void clearCache()
	{
		if(null == mCachePath)
		{
			return;
		}
		
		File cacheDir = new File(mCachePath);
		if(!cacheDir.isDirectory())
		{
			return;
		}
		
		File[] fileList = cacheDir.listFiles();
		if(70 > fileList.length)
		{
			return;
		}
		
		Arrays.sort(fileList, 0, fileList.length - 1, new Comparator<File>(){

			@Override
			public int compare(File lhs, File rhs)
			{
				// TODO Auto-generated method stub
				return (int)(lhs.lastModified() - rhs.lastModified());
			}
			
		});
		
		for(int i = 0; i < 25; i++)
		{
			fileList[i].delete();
		}
	}
	
	private void delete(String name)
	{
		File file = new File(mCachePath + name);
		file.delete();
	}
	
	public void clear()
	{
		File file = new File(mCachePath);
		if(file.exists())
		{
			file.delete();
		}
	}
	
	private String getMD5String(String source)
	{
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',   
				    'a', 'b', 'c', 'd', 'e', 'f' };   
		try 
		{   
			byte[] strTemp = source.getBytes();   
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");   
			mdTemp.update(strTemp);   
			byte[] md = mdTemp.digest();   
			int length = md.length;   
			char str[] = new char[length * 2];   
			int k = 0;   
			for (int i = 0; i < length; i++) 
			{   
				byte byte0 = md[i];   
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];   
				str[k++] = hexDigits[byte0 & 0xf];   
			}   
			
			return new String(str);   
		} catch (Exception e) 
		{   
			return null;   
		}   
	}
	
	private static ImageCache mThis = new ImageCache();
	private static String mCachePath;
	
	private class MemeryCache extends LruCache<String, Bitmap>{

		public MemeryCache(int maxSize) {
			super(maxSize);
			// TODO Auto-generated constructor stub
		}
		
		@Override  
        protected int sizeOf(String key, Bitmap value) {  
            return value.getRowBytes() * value.getHeight();  
        }  
	}
}
