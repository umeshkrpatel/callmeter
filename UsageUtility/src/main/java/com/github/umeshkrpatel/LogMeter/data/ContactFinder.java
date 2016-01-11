package com.github.umeshkrpatel.LogMeter.data;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.PhoneLookup;
import android.widget.TextView;

import com.github.umeshkrpatel.LogMeter.LogMeter;

import java.util.ArrayList;

import de.ub0r.android.logg0r.Log;

/**
 * Load name from number in background.
 *
 * @author flx
 */
public class ContactFinder extends AsyncTask<Void, Void, String> {

    /**
     * TAG for Log.
     */
    private static final String TAG = "ContactFinder";

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
                        Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, getTenDigitNumber()),
                        new String[]{PhoneLookup.DISPLAY_NAME, PhoneLookup.PHOTO_THUMBNAIL_URI}, null, null, null);
                if (c != null) {
                    if (c.moveToFirst()) {
                        ret = c.getString(0);
                        mPhotoUri = c.getString(1);
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
        ContactCache.getInstance().put(mNumber, e);
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
