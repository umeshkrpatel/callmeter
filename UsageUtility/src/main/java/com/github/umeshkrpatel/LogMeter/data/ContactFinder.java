package com.github.umeshkrpatel.LogMeter.data;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.PhoneLookup;
import android.widget.TextView;

import com.github.umeshkrpatel.LogMeter.LogMeter;
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
    public static String findContactForNumber(final Context context, final String number,
                                              final String format) {
        ContactFinder loader = new ContactFinder(context, number, format, null);
        String result = loader.doInBackground((Void) null);
        loader.onPostExecute(result);
        ContactCache.getInstance().put(number, result);
        if (format == null) {
            return result;
        } else {
            return String.format(format, result);
        }
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
                        new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
                if (c != null) {
                    if (c.moveToFirst()) {
                        ret = c.getString(0);
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
        ContactCache.getInstance().put(mNumber, result);
        if (mTvView != null && !this.isCancelled()) {
            String s = result;
            if (mFormat != null) {
                s = String.format(mFormat, s);
            }
            mTvView.setText(s);
        }
    }

    private boolean validateNumber() {
        return (mNumber.length() >= 10);
    }

    private String getTenDigitNumber() {
        return mNumber.substring(mNumber.length() - 10);
    }
}
