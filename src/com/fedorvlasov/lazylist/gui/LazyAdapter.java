package com.fedorvlasov.lazylist.gui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fedorvlasov.lazylist.ImageLoader;
import com.fedorvlasov.lazylist.ImageLoader.ImageLoadListener;
import com.fedorvlasov.lazylist.R;

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private String[] data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;
    public boolean[] isDataClear;
    
    public LazyAdapter(Activity a, String[] d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader= ImageLoader.getInstance();//new ImageLoader(activity.getApplicationContext(), R.drawable.stub);
        isDataClear = new boolean[data.length];
    }

    public void clearData() {
    	for (int i = 0; i < isDataClear.length; i++) {
    		isDataClear[i] = false;
    	}
    }
    
    public int getCount() {
        return data.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.item, null);

        TextView text=(TextView)vi.findViewById(R.id.text);
        ImageView image=(ImageView)vi.findViewById(R.id.image);
        final ProgressBar progress = (ProgressBar) vi.findViewById(R.id.progress);
        text.setText("item "+position);
        if (!isDataClear[position]) {
        	image.setVisibility(View.INVISIBLE);
			progress.setVisibility(View.VISIBLE);
        }
        imageLoader.displayImage(data[position], image, true, new ImageLoadListener() {
			@Override
			public void onComplete(ImageView image, Bitmap bitmap) {
				image.setVisibility(View.VISIBLE);
				progress.setVisibility(View.INVISIBLE);
			}
		});
        return vi;
    }
}