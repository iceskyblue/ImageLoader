package com.loader.imageloader;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AsyImageView extends ImageView {
	
	private Vector<String> mUrls = new Vector<String>();
	private LoadTask mLoader = null;
	private Context mCtx;
	private Drawable defaultDrawable;
	private String curUrl = null;
	
	public AsyImageView(Context ctx){
		super(ctx);
		mCtx = ctx;
		ImageCache.setPath(ctx.getCacheDir()+"/images/");
	}
	
	public void setDefaultDrawable(Drawable drawable){
		defaultDrawable = drawable;
	}
	
	public AsyImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mCtx = context;
		ImageCache.setPath(context.getCacheDir()+"/images/");
	}

	public void recycleImg(){
		Drawable drawable = this.getDrawable();
		if(drawable instanceof BitmapDrawable){
			((BitmapDrawable) drawable).getBitmap().recycle();
		}
	}
	
	public synchronized void load(String url){
		curUrl = url;
		Bitmap bmp = ImageCache.getInstance().getImage(url, this.getWidth(), this.getHeight());
		if(null != bmp){
			this.setImageDrawable(new BitmapDrawable(mCtx.getResources(), bmp));
			return;
		}
		this.setImageDrawable(defaultDrawable);
		if(mUrls.size() >= 1){
			mUrls.clear();
			mUrls.add(url);
			return;
		}
		
		mUrls.add(url);
		loadNext();
	}
	
	
	private void loadNext(){
		if(mLoader != null || mUrls.size() == 0){
			return;
		}
		
		mLoader = new LoadTask(new LoadTask.CompleteCallback() {
			
			@Override
			public void error(ByteArrayOutputStream err, Object target, String url) {
				// TODO Auto-generated method stub
				
				mLoader = null;
				loadNext();
			}
			
			@Override
			public void complete(ByteArrayOutputStream out, Object target, String url) {
				// TODO Auto-generated method stub
				ImageCache.getInstance().saveImage(url, out);
				if(url.equals(curUrl)){
					Bitmap bmp = ImageCache.getInstance().getImage(out.toByteArray(), AsyImageView.this.getWidth(), 
							AsyImageView.this.getHeight());
					if(null != bmp){						
						AsyImageView.this.setImageDrawable(new BitmapDrawable(mCtx.getResources(), bmp));
					}
				}
				
				mLoader = null;
				loadNext();
			}
		});
		
		mLoader.execute(mUrls.remove(0));
	}
		
}
