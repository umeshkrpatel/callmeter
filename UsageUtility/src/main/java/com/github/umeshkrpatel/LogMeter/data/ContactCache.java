
package com.github.umeshkrpatel.LogMeter.data;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.util.LruCache;
import android.widget.TextView;

import com.github.umeshkrpatel.LogMeter.LogMeter;

import java.util.ArrayList;

import android.util.Log;

/**
 * {@link LruCache} Holding the Contact Name and Photo_ID for Number
 *
 * @author Umesh Kumar Patel
 */
public final class ContactCache extends LruCache<String, ArrayList<String> > {

    /**
     * Maximum cache size.
     */
    private static final String TAG = "ContactCache";
    private static final int MAX_CACHE_SIZE = 100;

    /**
     * Single instance.
     */
    private static ContactCache instance;

    /**
     * hide public constructor.
     */
    private ContactCache() {
        super(MAX_CACHE_SIZE);
    }

    /**
     * Get the cache.
     *
     * @return the single {@link ContactCache} instance
     */
    public static ContactCache getInstance() {
        if (instance == null) {
            instance = new ContactCache();
        }
        return instance;
    }

    /**
     * Return get(key) formatted with format.
     *
     * @param key    key
     * @param format format
     * @return Cached Name {@link String}
     */
    public String getName(final Context context, final String key, final String format) {
        ArrayList<String> strings = get(key);
        if (strings == null) {
            strings = ContactFinder.findContactForNumber(context, key, format);
        }
        if (format!=null)
            return String.format(format, strings.get(0));
        else
            return strings.get(0);
    }

    /**
     * Return get(key) formatted with format.
     *
     * @param key    key
     * @param format format
     * @return Cached Photo_ID {@link String}
     */
    public String getPhotoUri(final Context context, final String key, final String format) {
        ArrayList<String> strings = get(key);
        if (strings == null) {
            strings = ContactFinder.findContactForNumber(context, key, format);
        }
        return strings.get(1);
    }

    /**
     * Load name from number in background.
     *
     * @author flx
     */
    private static class ContactFinder extends AsyncTask<Void, Void, String> {
        /**
         * {@link Context}.
         */
        private final Context mContext;

        /**
         * {@link TextView}.
         */
        private final TextView mTvView;

        /**
         * Number.
         */
        private final String mNumber;

        /**
         * {@link String} format.
         */
        private final String mFormat;

        private String mPhotoUri;
        private final String mEmptyPhotoUri = "<empty>";
        private static String[] PROJECTION_CONTACT = new String[] {
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI,
            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,
        };
        /**
         * Default constructor.
         *
         * @param context {@link Context}
         * @param number  phone number
         * @param format  format to format the {@link String} with
         * @param view    {@link TextView} to set the result on
         */
        public ContactFinder(final Context context, final String number, final String format,
                             final TextView view) {
            mContext = context;
            mNumber = number;
            mFormat = format;
            mTvView = view;
        }

        /**
         * Get name for number synchronously.
         *
         * @param context {@link Context}
         * @param number  phone number
         * @param format  format to format the {@link String} with
         * @return name or formatted {@link String}
         */
        public static ArrayList<String> findContactForNumber(final Context context, final String number,
                                                  final String format) {
            ContactFinder loader = new ContactFinder(context, number, format, null);
            String name = loader.doInBackground((Void) null);
            String uri = loader.getPhotoUri();
            loader.onPostExecute(name);
            ArrayList<String> e = new ArrayList<>();
            e.add(name);
            e.add(uri);
            return e;
        }

        @Override
        protected String doInBackground(final Void... params) {
            String ret = mNumber;
            if (!validateNumber())
                return mNumber;

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
                            mPhotoUri = (!c.getString(1).isEmpty()) ? c.getString(1) : c.getString(2);
                            if (mPhotoUri == null)
                                mPhotoUri = mEmptyPhotoUri;
                        }
                        c.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error loading name", e);
                }
            }
            return ret;
        }

        @Override
        protected void onPostExecute(final String result) {
            ArrayList<String> e = new ArrayList<>();
            e.add(result);
            e.add(mPhotoUri);
            getInstance().put(mNumber, e);
            if (mTvView != null && !this.isCancelled()) {
                String s = result;
                if (mFormat != null) {
                    s = String.format(mFormat, s);
                }
                mTvView.setText(s);
            }
        }

        protected String getPhotoUri() {
            return mPhotoUri;
        }
        private boolean validateNumber() {
            return (mNumber.length() >= 10);
        }

        private String getTenDigitNumber() {
            return mNumber.substring(mNumber.length() - 10);
        }
    }
}
