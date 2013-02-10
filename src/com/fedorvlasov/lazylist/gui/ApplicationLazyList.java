package com.fedorvlasov.lazylist.gui;

import com.fedorvlasov.lazylist.ImageLoader;
import com.fedorvlasov.lazylist.ImageLoaderConfiguration;
import com.fedorvlasov.lazylist.R;

import android.app.Application;

public class ApplicationLazyList extends Application{
	@Override
	public void onCreate() {
		super.onCreate();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration(this);
		config.setStub_id(R.drawable.stub);
		ImageLoader.getInstance().init(config);
	}
}
