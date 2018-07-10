package cn.xender.transfer;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.util.LruCache;

import cn.xender.core.log.Logger;


/**
 * This class holds our bitmap caches (memory and disk).
 */
public class BitmapCache {
    private static final String TAG = "BitmapCache";

    // Default memory cache size in kilobytes
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB

    // Constants to easily toggle various caches
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;

    private LruCache<String, Bitmap> mMemoryCache;

    /**
     * Creating a new ImageCache object using the specified parameters.
     *
     * @param cacheParams The cache parameters to use to initialize the cache
     */
    public BitmapCache(BitmapCacheParams cacheParams) {
        init(cacheParams);
    }

    /**
     * Initialize the cache, providing all parameters.
     *
     * @param cacheParams The cache parameters to initialize the cache
     */
    private void init(BitmapCacheParams cacheParams) {

        // Set up memory cache
        if (cacheParams.memoryCacheEnabled) {
            if(Logger.r) Logger.d(TAG, "Memory cache created (size = " + cacheParams.memCacheSize + ")");
            mMemoryCache = new LruCache<String, Bitmap>(cacheParams.memCacheSize) {
                /**
                 * Measure item size in kilobytes rather than units which is more practical
                 * for a bitmap cache
                 */
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    final int bitmapSize = getBitmapSize(bitmap) / 1024;
                    return bitmapSize == 0 ? 1 : bitmapSize;
                }
            };
        }
        
    }

    /**
     * Adds a bitmap to both memory and disk cache.
     * @param data Unique identifier for the bitmap to store
     * @param bitmap The bitmap to store
     */
    public void addBitmapToCache(String data, Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }

        // Add to memory cache
        if (mMemoryCache != null && mMemoryCache.get(data) == null) {
            mMemoryCache.put(data, bitmap);
        }
    }

    /**
     * Get from memory cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromMemCache(String data) {
        if (mMemoryCache != null) {
            final Bitmap memBitmap = mMemoryCache.get(data);
            if (memBitmap != null) {
//            	if(Logger.r) Logger.d(TAG, "Memory cache hit");
                return memBitmap;
            }
        }
        return null;
    }

    

    /**
     * Clears both the memory and disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI thread.
     */
    public void clearCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
            if(Logger.r) Logger.d(TAG, "Memory cache cleared");
        }
    }
    
    /**
     * A holder class that contains cache parameters.
     */
    public static class BitmapCacheParams {
        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;

        public BitmapCacheParams() {
        	
        }

        /**
         * Sets the memory cache size based on a percentage of the max available VM memory.
         * Eg. setting percent to 0.2 would set the memory cache to one fifth of the available
         * memory. Throws {@link IllegalArgumentException} if percent is < 0.05 or > .8.
         * memCacheSize is stored in kilobytes instead of bytes as this will eventually be passed
         * to construct a LruCache which takes an int in its constructor.
         *
         * This value should be chosen carefully based on a number of factors
         * Refer to the corresponding Android Training class for more discussion:
         * http://developer.android.com/training/displaying-bitmaps/
         *
         * @param percent Percent of available app memory to use to size memory cache
         */
        public void setMemCacheSizePercent(float percent) {
            if (percent < 0.01f || percent > 0.8f) {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
                        + "between 0.01 and 0.8 (inclusive)");
            }
            memCacheSize = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
        }

        public void setMemCacheSize(int memCacheSizeKB) {
            memCacheSize = memCacheSizeKB;
        }
    }
    

    /**
     * Get the size in bytes of a bitmap.
     * @param bitmap
     * @return size in bytes
     */
    @TargetApi(12)
    public static int getBitmapSize(Bitmap bitmap) {
        return bitmap.getByteCount();
    }

}
