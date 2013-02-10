package com.fedorvlasov.lazylist;

import android.content.Context;

public class ImageLoaderConfiguration {
	
	Context conext;
	int stub_id = -1;
	int threadNumber = -1 ;
	int memoryCashSize = -1;
	
	public ImageLoaderConfiguration(Context conext) {
		this.conext = conext;
	}

	public void setStub_id(int stub_id) {
		this.stub_id = stub_id;
	}

	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}

	public void setMemoryCashSize(int memoryCashSize) {
		this.memoryCashSize = memoryCashSize;
	}
}
