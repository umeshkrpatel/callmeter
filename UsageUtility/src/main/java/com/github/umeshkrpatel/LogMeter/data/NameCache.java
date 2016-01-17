
package com.github.umeshkrpatel.LogMeter.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.github.umeshkrpatel.LogMeter.LogMeter;

import java.io.InputStream;

/**
 * {@link LruCache} Holding the Contact Name and Photo_ID for Number
 *
 * @author Umesh Kumar Patel
 */
public final class NameCache extends LruCache<String, NameCache.NameCacheItem> {

    /**
     * Maximum cache size.
     */
    private static final String TAG = "NameCache";
    private static final int MAX_CACHE_SIZE = 100;

    /**
     * Single instance.
     */
    private static NameCache instance;

    /**
     * hide public constructor.
     */
    private NameCache() {
        super(MAX_CACHE_SIZE);
    }

    public static class NameCacheItem {
        public final String mName; public final Drawable mDrawable;
        public NameCacheItem(String name, Drawable drawable) {
            mName = name; mDrawable = drawable;
        }
    }

    /**
     * Get the cache.
     *
     * @return the single {@link NameCache} instance
     */
    public static NameCache getInstance() {
        if (instance == null) {
            instance = new NameCache();
        }
        return instance;
    }

    /**
     * Return get(key) formatted with format.
     *
     * @param key    key
     * @return Cached Name {@link String}
     */
    public String getName(final Context context, final String key, final int type) {
        NameCacheItem nameCacheItem = get(key);
        if (nameCacheItem == null) {
            nameCacheItem = NameFinder.findName(context, key, type);
        }
        return nameCacheItem.mName;
    }

    /**
     * Return get(key) formatted with format.
     *
     * @param key    key
     * @return Cached Photo_ID {@link String}
     */
    public Drawable getDrawableIcon(final Context context, final String key, final int type) {
        NameCacheItem nameCacheItem = get(key);
        if (nameCacheItem == null) {
            nameCacheItem = NameFinder.findName(context, key, type);
        }
        return nameCacheItem.mDrawable;
    }

    public NameCacheItem getNameCacheItem(final Context context, final String key, final int type) {
        NameCacheItem nameCacheItem = get(key);
        if (nameCacheItem == null) {
            nameCacheItem = NameFinder.findName(context, key, type);
        }
        return nameCacheItem;
    }

    /**
     * Load name from number in background.
     *
     * @author flx
     */
    private static class NameFinder extends AsyncTask<Void, Void, String> {
        private final Context mContext;
        private final String mNumber;
        private final int mType;
        private Drawable mDrawable;
        private static String[] PROJECTION_CONTACT = new String[] {
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI,
            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,
        };
        /**
         * Default constructor.
         * @param context {@link Context}
         * @param number  phone number
         */
        public NameFinder(final Context context, final String number, final int type) {
            mContext = context; mNumber = number; mType = type;
        }

        /**
         * Get name for number synchronously.
         *
         * @param context {@link Context}
         * @param number  phone number
         * @return name or formatted {@link String}
         */
        public static NameCacheItem findName(final Context context, final String number,
                                             final int type) {
            NameFinder loader = new NameFinder(context, number, type);
            String name = loader.doInBackground((Void) null);
            Drawable drawable = loader.getDrawableIcon();
            loader.onPostExecute(name);
            return new NameCacheItem(name, drawable);
        }

        @Override
        protected String doInBackground(final Void... params) {
            String ret = mNumber;
            if (mType == DataProvider.TYPE_DATA_MOBILE) {
                try {
                    PackageManager pm = mContext.getPackageManager();
                    ApplicationInfo applicationInfo = pm.getApplicationInfo(mNumber, 0);
                    ret = pm.getApplicationLabel(applicationInfo).toString();
                    mDrawable = pm.getApplicationIcon(mNumber);
                } catch (Exception e) {
                    mDrawable = null;
                    return ret;
                }
                return ret;
            } else {
                if (!validateNumber())
                    return ret;

                if (LogMeter.hasPermission(mContext, Manifest.permission.READ_CONTACTS)) {
                    // resolve names only when permission is granted
                    try {
                        //noinspection ConstantConditions
                        Cursor c = mContext.getContentResolver().query(
                                Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, getTenDigitNumber()),
                                PROJECTION_CONTACT, null, null, null);
                        if (c != null) {
                            if (c.moveToFirst()) {
                                ret = c.getString(0);
                                String uri = (c.getString(1) != null) ? c.getString(1) : c.getString(2);
                                if (uri != null) {
                                    try {
                                        InputStream is = mContext.getContentResolver().openInputStream(Uri.parse(uri));
                                        mDrawable = Drawable.createFromStream(is, uri);
                                    }  catch (Exception e) {
                                        mDrawable = null;
                                    }
                                }
                            }
                            c.close();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "error loading name", e);
                    }
                }
            }
            return ret;
        }

        @Override
        protected void onPostExecute(final String result) {
            NameCacheItem e = new NameCacheItem(result, mDrawable);
            getInstance().put(mNumber, e);
        }

        private Drawable getDrawableIcon() {
            return mDrawable;
        }

        private boolean validateNumber() {
            return (mNumber.length() >= 10);
        }

        private String getTenDigitNumber() {
            return mNumber.substring(mNumber.length() - 10);
        }
    }
}
