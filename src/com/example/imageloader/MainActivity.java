package com.example.imageloader;

import java.util.ArrayList;
import java.util.Random;

import com.loader.imageloader.AsyImageView;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	ArrayList<String> urls = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		
		this.setContentView(R.layout.activity_main);
		
		Random rand = new Random();
		for(int i = 0; i<100; i++){
			urls.add(String.format("http://10.2.30.7:8080/%d.png", rand.nextInt(10)));
		}
		
		ListView list = (ListView)this.findViewById(R.id.listView1);
		list.setAdapter(new ListAdapter());
	}

	private class ListAdapter extends BaseAdapter{
		private LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return urls.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = null;
			if(null == convertView){
				v = inflater.inflate(R.layout.list_item, null);
			}else{
				v = convertView;
			}
			
			AsyImageView img = (AsyImageView)v.findViewById(R.id.imageView1);
			img.setDefaultDrawable(getResources().getDrawable(R.drawable.ic_launcher));
			img.load(urls.get(position));
			TextView txt = (TextView)v.findViewById(R.id.textView1);
			txt.setText(urls.get(position));
			return v;
		}
		
	}
}
