
package com.github.umeshkrpatel.LogMeter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.github.umeshkrpatel.LogMeter.R;
import de.ub0r.android.lib.Utils;
import de.ub0r.android.logg0r.Log;

/**
 * @author flx
 */
public final class LogMeter extends Application {

    public static final long kMinimumDate = 10000000000L;
    public static final long kMilliSecondsPerSecond = 1000L;
    public static final int kEightyth = 80;
    public static final int kHundredth = 100;
    public static final int kSecondsPerMinute = 60;
    public static final int kSecondsPerHour = 60 * kSecondsPerMinute;
    public static final int kSecondsPerDay = 24 * kSecondsPerHour;
    public static final int kTenth = 10;
    public static final long kBytesPerKiloByte = 1024L;
    public static final long kBytesPerMegaByte = kBytesPerKiloByte * kBytesPerKiloByte;
    public static final long kBytesPerGigaByte = kBytesPerMegaByte * kBytesPerKiloByte;
    public static final long kBytesPerTeraByte = kBytesPerGigaByte * kBytesPerKiloByte;
    private static final String TAG = "LogMeter";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void setActivitySubtitle(final Activity a, final String t) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            a.getActionBar().setSubtitle(t);
        }
    }

    public static boolean hasPermission(final Context context, final String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasPermissions(final Context context, final String... permissions) {
        for (String p : permissions) {
            if (!hasPermission(context, p)) {
                return false;
            }
        }
        return true;
    }

    public static boolean requestPermission(final Activity activity, final String permission,
                                            final int requestCode, final int message,
                                            final DialogInterface.OnClickListener onCancelListener) {
        Log.i(TAG, "requesting permission: " + permission);
        if (!hasPermission(activity, permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.permissions_)
                        .setMessage(message)
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.cancel, onCancelListener)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialogInterface,
                                                        final int i) {
                                        ActivityCompat.requestPermissions(activity,
                                                new String[]{permission}, requestCode);
                                    }
                                })
                        .show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // fix AsyncTask for some old devices + broken gms
            // http://stackoverflow.com/a/27239869/2331953
            try {
                Class.forName("android.os.AsyncTask");
            } catch (Throwable ignore) {
            }
        }

        super.onCreate();
        Utils.setLocale(this);
    }
}
