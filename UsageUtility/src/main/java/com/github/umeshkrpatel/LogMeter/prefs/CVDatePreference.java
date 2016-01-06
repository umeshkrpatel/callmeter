package com.github.umeshkrpatel.LogMeter.prefs;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import com.github.umeshkrpatel.LogMeter.R;
import de.ub0r.android.logg0r.Log;

/**
 * DatePreference holding it's value in {@link ContentValues}.
 *
 * @author flx
 */
public final class CVDatePreference extends DialogPreference implements OnTimeSetListener {

    private static final String TAG = "CVDatePreference";

    /**
     * {@link ContentValues} for saving values.
     */
    private final ContentValues cv;

    /**
     * {@link UpdateListener}.
     */
    private final UpdateListener ul;

    /**
     * Show date and time?
     */
    private final boolean dt;

    /**
     * Current date.
     */
    private final Calendar v = Calendar.getInstance();
    /**
     * Show help.
     */
    private final boolean sh;
    /**
     * {@link DatePicker}.
     */
    private DatePicker dp = null;

    /**
     * Default constructor.
     *
     * @param context         {@link Context}
     * @param values          {@link ContentValues}
     * @param key             key
     * @param showDateAndTime Show date and time?
     */
    public CVDatePreference(final Context context, final ContentValues values, final String key,
                            final boolean showDateAndTime) {
        super(context, null);
        setPersistent(false);
        setKey(key);
        cv = values;
        dt = showDateAndTime;
        sh = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                Preferences.PREFS_SHOWHELP, true);
        if (context instanceof UpdateListener) {
            ul = (UpdateListener) context;
        } else {
            ul = null;
        }
    }

    @Override
    public void setTitle(final int titleResId) {
        super.setTitle(titleResId);
        setDialogTitle(titleResId);
    }

    @Override
    public void setTitle(final CharSequence title) {
        super.setTitle(title);
        setDialogTitle(title);
    }

    @Override
    protected View onCreateView(final ViewGroup parent) {
        View view = super.onCreateView(parent);
        TextView tv = (TextView) view.findViewById(android.R.id.summary);
        if (tv != null) {
            tv.setMaxLines(Integer.MAX_VALUE);
        }
        return view;
    }

    @Override
    protected View onCreateDialogView() {
        dp = new DatePicker(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            dp.setCalendarViewShown(true);
            dp.setSpinnersShown(false);
            dp.setMaxDate(System.currentTimeMillis());
        }
        updateDialog();
        return dp;
    }

    /**
     * Set date and time as unix time stamp.
     *
     * @param time time stamp
     */
    public void setValue(final long time) {
        v.setTimeInMillis(time);
        if (!this.sh) {
            setSummary(getContext().getString(R.string.value) + ": "
                    + DateFormat.getDateFormat(getContext()).format(v.getTime()));
        }
        updateDialog();
    }

    /**
     * Set date and time as unix time stamp.
     *
     * @param time calendar
     */
    public void setValue(final Calendar time) {
        if (time == null) {
            setValue(System.currentTimeMillis());
        } else {
            setValue(time.getTimeInMillis());
        }
    }

    /**
     * Update {@link DatePicker}.
     */
    private void updateDialog() {
        if (dp == null) {
            return;
        }
        try {
            dp.updateDate(v.get(Calendar.YEAR), v.get(Calendar.MONTH),
                    v.get(Calendar.DAY_OF_MONTH));
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "date array out of bound", e);
            v.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            setValue(v);
        }
    }

    @Override
    protected void onDialogClosed(final boolean positiveResult) {
        if (positiveResult) {
            v.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
            cv.put(getKey(), v.getTimeInMillis());
            if (ul != null) {
                ul.onUpdateValue(this);
            }
            if (dt) {
                TimePickerDialog tpd = new TimePickerDialog(getContext(), this,
                        v.get(Calendar.HOUR_OF_DAY), v.get(Calendar.MINUTE), true);
                tpd.setTitle(getTitle());
                tpd.setCancelable(true);
                tpd.show();
            }
        }
    }

    @Override
    public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
        v.set(Calendar.HOUR_OF_DAY, hourOfDay);
        v.set(Calendar.MINUTE, minute);
        cv.put(getKey(), v.getTimeInMillis());
        if (ul != null) {
            ul.onUpdateValue(this);
        }
    }
}
