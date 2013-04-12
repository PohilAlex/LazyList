package com.fedorvlasov.lazylist;

import android.content.Context;

public class ImageLoaderConfiguration {
	
	public static int THREAD_NUMBER_DEFAULT = 4;  
	Context conext;
	int stub_id = -1;
	int threadNumber = THREAD_NUMBER_DEFAULT;
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
