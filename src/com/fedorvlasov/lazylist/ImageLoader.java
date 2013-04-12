package com.fedorvlasov.lazylist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class ImageLoader {
    
	private static final String ERROR_INIT_CONFIG = "Imageloader not configured. Initialize it with init() method.";
	
	private static ImageLoader instance;
    MemoryCache memoryCache=new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    Handler handler=new Handler();//handler to display images in UI thread
    int stub_id;
    boolean isInit = false;
    
    public void init(ImageLoaderConfiguration config) {
    	fileCache = new FileCache(config.conext);
		int threadNumber = config.threadNumber;
    	if (config.stub_id > 0) {
    		stub_id = config.stub_id;
    	}
        executorService=Executors.newFixedThreadPool(threadNumber);
        if (config.memoryCashSize > 0) {
        	memoryCache.setLimit(config.memoryCashSize);
        }
        isInit = true;
    }
    
    private ImageLoader() {
    }
    
    public static ImageLoader getInstance() {
		if (instance == null) {
			instance = new ImageLoader();
		}
		return instance;
	}
    
    public void displayImage(String url, ImageView imageView, boolean isCompress)
    {
    	if (!isInit) {
    		throw new IllegalArgumentException(ERROR_INIT_CONFIG);
    	}
        imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap!=null)
            imageView.setImageBitmap(bitmap);
        else
        {
        	PhotoToLoad p = new PhotoToLoad(url, imageView, isCompress);
            executorService.submit(new PhotosLoader(p));
            imageView.setImageResource(stub_id);
        }
    }
    
    public void displayImage(String url, ImageView imageView) {
    	displayImage(url, imageView, true);
    }
    
    private Bitmap getBitmap(PhotoToLoad photo) 
    {
        File f=fileCache.getFile(photo.url);
        
        //from SD cache
        Bitmap b = decodeFile(f, photo.isCompress);
        if(b!=null)
            return b;
        
        //from web
        try {
            Bitmap bitmap=null;
            URL imageUrl = new URL(photo.url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f, photo.isCompress);
            return bitmap;
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
               memoryCache.clear();
           return null;
        }
    }
    
    private Bitmap decodeFile(File f, boolean isCommpress){
    	try {
	    	if (isCommpress) {
	    		return decodeFileCompress(f);
	    	} else {
	    		return decodeFileNoCompress(f);
	    	}
    	} catch (FileNotFoundException e) {
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFileCompress(File f) throws FileNotFoundException, IOException {
        //decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        FileInputStream stream1=new FileInputStream(f);
        BitmapFactory.decodeStream(stream1,null,o);
        stream1.close();
        
        //Find the correct scale value. It should be the power of 2.
        final int REQUIRED_SIZE=70;
        int width_tmp=o.outWidth, height_tmp=o.outHeight;
        int scale=1;
        while(true){
            if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                break;
            width_tmp/=2;
            height_tmp/=2;
            scale*=2;
        }
        
        //decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize=scale;
        FileInputStream stream2=new FileInputStream(f);
        Bitmap bitmap=BitmapFactory.decodeStream(stream2, null, o2);
        stream2.close();
        return bitmap;
    }
    
    //decodes image without compress
    private Bitmap decodeFileNoCompress(File f) throws FileNotFoundException, IOException {
		FileInputStream stream;
		stream = new FileInputStream(f);
		Bitmap bitmap=BitmapFactory.decodeStream(stream);
	    stream.close();
	    return bitmap;
    }
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public boolean isCompress;
        
        public PhotoToLoad(String u, ImageView i, boolean isCompress){
            url=u; 
            imageView=i;
            this.isCompress=isCompress;
        }
    }
    
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        @Override
        public void run() {
            try{
                if(imageViewReused(photoToLoad))
                    return;
                Bitmap bmp=getBitmap(photoToLoad);
                memoryCache.put(photoToLoad.url, bmp);
                if(imageViewReused(photoToLoad))
                    return;
                BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
                handler.post(bd);
            }catch(Throwable th){
                th.printStackTrace();
            }
        }
    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            if(bitmap!=null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(stub_id);
        }
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

	public void setStub_id(int stub_id) {
		this.stub_id = stub_id;
	}

}
