package com.fedorvlasov.lazylist;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import android.graphics.Bitmap;
import android.util.Log;

public class MemoryCache {

    private static final String TAG = "MemoryCache";
    private Map<String, SoftReference<Bitmap>> cache=Collections.synchronizedMap(
            new LinkedHashMap<String, SoftReference<Bitmap>>(10,1.5f,true));//Last argument true for LRU ordering
    private long size=0;//current allocated size
    private long limit=1000000;//max memory in bytes

    public MemoryCache(){
        //use 25% of available heap size
        setLimit(Runtime.getRuntime().maxMemory()/4);
    }
    
    protected boolean containsKey(String id) {
		try {
			if (!cache.containsKey(id)) { 
				return false; 
			}
			// NullPointerException sometimes happen here
			// http://code.google.com/p/osmdroid/issues/detail?id=78
		} catch (NullPointerException ex) {
			ex.printStackTrace();
			return false;
		}
		return cache.get(id).get() != null;
    }
    
    public void setLimit(long new_limit){
        limit=new_limit;
        Log.i(TAG, "MemoryCache will use up to "+limit/1024./1024.+"MB");
    }

    public Bitmap get(String id){
    	if (containsKey(id)) {
    		return cache.get(id).get();
    	}
    	return null;
    }
    
    public void put(String id, Bitmap bitmap){
        try{
        	if (containsKey(id)) {
        		size-=getSizeInBytes(cache.get(id).get());
        	}
            cache.put(id, new SoftReference<Bitmap>(bitmap));
            size+=getSizeInBytes(bitmap);
            checkSize();
        }catch(Throwable th){
            th.printStackTrace();
        }
    }
    
    private void checkSize() {
        Log.i(TAG, "cache size="+size+" length="+cache.size());
        if(size>limit){
            //clearCacheReference();
            Iterator<Entry<String, SoftReference<Bitmap>>> iter=cache.entrySet().iterator();//least recently accessed item will be the first one iterated  
            while(iter.hasNext()){
                Entry<String, SoftReference<Bitmap>> entry=iter.next();
                Bitmap bitmap = entry.getValue().get();
                if (bitmap != null) {
                	size-=getSizeInBytes(bitmap);
                }
                iter.remove();
                if(size<=limit)
                    break;
            }
            Log.i(TAG, "Clean cache. New size "+cache.size());
        }
    }
    
    protected void clearCacheReference() {
    	Iterator<Entry<String, SoftReference<Bitmap>>> iter=cache.entrySet().iterator();//least recently accessed item will be the first one iterated  
        while(iter.hasNext()){
            Entry<String, SoftReference<Bitmap>> entry=iter.next();
            if(entry.getValue().get() == null) {
            	iter.remove();
            }
        }
    }
    
    public void clear() {
        try{
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            cache.clear();
            size=0;
        }catch(NullPointerException ex){
            ex.printStackTrace();
        }
    }

    long getSizeInBytes(Bitmap bitmap) {
        if(bitmap==null)
            return 0;
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}