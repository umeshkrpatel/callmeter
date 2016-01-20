/*
 * Copyright (C) 2009-2013 Felix Bechstein
 * 
 * This file is part of CallMeter 3G.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.umeshkrpatel.LogMeter.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.umeshkrpatel.LogMeter.IDefs;
import com.github.umeshkrpatel.LogMeter.ui.prefs.Preferences;

import de.ub0r.android.lib.Utils;

/**
 * {@link BroadcastReceiver} running updates and postboot checks.
 *
 * @author Felix Bechstein
 */
public final class LogRunnerReceiver extends BroadcastReceiver {

    /**
     * Time between to update checks.
     */
    static final long DELAY = 30; // 30min
    /**
     * Time between to update checks for data.
     */
    static final float DELAY_DATA = 2f; // 2min
    /**
     * Factor for time between update checks.
     */
    static final long DELAY_FACTOR = IDefs.kSecondsPerMinute * IDefs.kMilliSecondsPerSecond;
    /**
     * Tag for output.
     */
    private static final String TAG = "LogRunnerReceiver";
    /**
     * ACTION for publishing information about sent websms.
     */
    private static final String ACTION_CM_WEBSMS = "com.github.umeshkrpatel.LogMeter.SAVE_WEBSMS";

    /**
     * Extra holding uri of sent sms.
     */
    private static final String EXTRA_WEBSMS_URI = "uri";

    /**
     * Extra holding name of connector.
     */
    private static final String EXTRA_WEBSMS_CONNECTOR = "connector";

    /**
     * ACTION for publishing information about calls.
     */
    private static final String ACTION_CM_SIP = "com.github.umeshkrpatel.LogMeter.SAVE_SIPCALL";

    /**
     * Extra holding uri of done call.
     */
    private static final String EXTRA_SIP_URI = "uri";

    /**
     * Extra holding name of provider.
     */
    private static final String EXTRA_SIP_PROVIDER = "provider";

    public static final int NO_DATA_NET = -1;
    public static int CONNECT_STATUS = NO_DATA_NET;

    /**
     * Schedule next update.
     *
     * @param context {@link Context}
     * @param a       action
     */
    public static void schedNext(final Context context, final String a) {
        Log.d(TAG, "schedNext(ctx, " + a + ")");
        long delay;
        if (a != null && a.equals(LogRunnerService.ACTION_SHORT_RUN)) {
            delay = (long) (Utils.parseFloat(PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(Preferences.PREFS_UPDATE_INTERVAL_DATA,
                                    String.valueOf(DELAY_DATA)),
                    DELAY_DATA) * DELAY_FACTOR);
        } else {
            delay = Utils.parseLong(PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(Preferences.PREFS_UPDATE_INTERVAL, String.valueOf(DELAY)), DELAY)
                    * DELAY_FACTOR;
        }
        Log.d(TAG, "schedNext(ctx, " + a + "): delay=" + delay);
        if (delay == 0L) {
            return;
        }
        schedNext(context, delay, a);
    }

    /**
     * Schedule next update.
     *
     * @param context {@link Context}
     * @param delay   delay in milliseconds
     * @param action  {@link Intent}'s action
     */
    public static void schedNext(final Context context, final long delay, final String action) {
        Log.d(TAG, "schedNext(ctx, " + delay + "," + action + ")");
        final Intent i = new Intent(context, LogRunnerReceiver.class);
        if (action != null) {
            i.setAction(action);
        }
        try {
            final PendingIntent pi = PendingIntent.getBroadcast(context, 0, i,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            final long t = SystemClock.elapsedRealtime() + delay;
            final AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.ELAPSED_REALTIME, t, pi);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "schedule next check failed", e);
        }
    }

    /**
     * Save sent WebSMS to db.
     *
     * @param context   {@link Context}
     * @param uri       {@link Uri} to sent message
     * @param mid       message id
     * @param connector connector name
     */
    private static void saveWebSMS(final Context context, final String uri, final long mid,
                                   final String connector) {
        final ContentResolver cr = context.getContentResolver();
        final Cursor c = cr.query(Uri.parse(uri), new String[]{"date"}, null, null, null);
        long date = -1;
        if (c != null && c.moveToFirst()) {
            date = c.getLong(0);
        }
        if (c != null && !c.isClosed()) {
            c.close();
        }
        final ContentValues cv = new ContentValues();
        cv.put(DataProvider.WebSMS.ID, mid);
        cv.put(DataProvider.WebSMS.CONNECTOR, connector);
        cv.put(DataProvider.WebSMS.DATE, date);
        cr.insert(DataProvider.WebSMS.CONTENT_URI, cv);
    }

    /**
     * Save call via SIP to db.
     *
     * @param context  {@link Context}
     * @param uri      {@link Uri} to call
     * @param cid      call id
     * @param provider provider name
     */
    private static void saveSipCall(final Context context, final String uri, final long cid,
                                    final String provider) {
        final ContentResolver cr = context.getContentResolver();
        final Cursor c = cr.query(Uri.parse(uri), new String[]{"date"}, null, null, null);
        long date = -1;
        if (c != null && c.moveToFirst()) {
            date = c.getLong(0);
        }
        if (c != null && !c.isClosed()) {
            c.close();
        }
        final ContentValues cv = new ContentValues();
        cv.put(DataProvider.SipCall.ID, cid);
        cv.put(DataProvider.SipCall.PROVIDER, provider);
        cv.put(DataProvider.SipCall.DATE, date);
        cr.insert(DataProvider.SipCall.CONTENT_URI, cv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(TAG, "wakeup");
        final String a = intent.getAction();
        Log.d(TAG, "action: " + a);
        if (a != null) {
            switch (a) {
                case ACTION_CM_WEBSMS: {
                    final String su = intent.getStringExtra(EXTRA_WEBSMS_URI);
                    if (su != null && su.length() > 0) {
                        final long si = Utils.parseLong(su.replaceAll(".*/", ""), -1);
                        final String sc = intent.getStringExtra(EXTRA_WEBSMS_CONNECTOR);
                        Log.d(TAG, "websms id:  " + si);
                        Log.d(TAG, "websms con: " + sc);
                        if (si >= 0L) {
                            saveWebSMS(context, su, si, sc);
                        }
                    }
                    break;
                }
                case ACTION_CM_SIP: {
                    final String su = intent.getStringExtra(EXTRA_SIP_URI);
                    if (su != null && su.length() > 0) {
                        final long si = Utils.parseLong(su.replaceAll(".*/", ""), -1);
                        final String sc = intent.getStringExtra(EXTRA_SIP_PROVIDER);
                        Log.d(TAG, "sip call id:  " + si);
                        Log.d(TAG, "sip call con: " + sc);
                        if (si >= 0L) {
                            saveSipCall(context, su, si, sc);
                        }
                    }
                    break;
                }
                case TelephonyManager.ACTION_PHONE_STATE_CHANGED: {
                    final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    if (state != null && !state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        return;
                    }
                    Log.d(TAG, "PHONE_STATE_CHANGE with state=" + state);
                    break;
                }
                case ConnectivityManager.CONNECTIVITY_ACTION: {
                    NetworkInfo net = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        CONNECT_STATUS = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);
                        if (CONNECT_STATUS == ConnectivityManager.TYPE_MOBILE
                                || CONNECT_STATUS == ConnectivityManager.TYPE_WIFI) {
                            ConnectivityManager connMgr =
                                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                            net = connMgr.getNetworkInfo(CONNECT_STATUS);
                        } else {
                            CONNECT_STATUS = NO_DATA_NET;
                            return;
                        }
                    } else {
                        net = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                        if (net.getType() != ConnectivityManager.TYPE_WIFI && net.getType() != ConnectivityManager.TYPE_MOBILE) {
                            CONNECT_STATUS = NO_DATA_NET;
                            return;
                        }
                    }

                    if (net.getState() == NetworkInfo.State.CONNECTED) {
                        LogRunnerService.update(context, LogRunnerService.ACTION_UPDATE_DATA);
                    } else {
                        CONNECT_STATUS = NO_DATA_NET;
                    }
                    return;
                }
                case Telephony.Sms.Intents.SMS_RECEIVED_ACTION:
                    LogRunnerService.update(context, LogRunnerService.ACTION_UPDATE_SMS_MMS);
                    return;
                default:
                    break;
            }
        }
        // run LogRunnerService
        LogRunnerService.update(context, a);
    }
}
