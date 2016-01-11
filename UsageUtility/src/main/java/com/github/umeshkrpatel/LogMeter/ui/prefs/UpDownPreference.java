package com.github.umeshkrpatel.LogMeter.ui.prefs;

import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;

import com.github.umeshkrpatel.LogMeter.R;

public class UpDownPreference extends Preference implements OnClickListener {

    private final OnUpDownClickListener mCallback;

    public UpDownPreference(final Context context, final OnUpDownClickListener callback) {
        super(context);
        setPersistent(false);
        mCallback = callback;
        if (callback != null) {
            setWidgetLayoutResource(R.layout.preference_widget_updown);
        }
    }

    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);
        view.setOnClickListener(this);
        if (mCallback != null) {
            view.findViewById(R.id.button_up).setOnClickListener(this);
            view.findViewById(R.id.button_down).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_up:
                mCallback.onUpDownClick(this, -1);
                return;
            case R.id.button_down:
                mCallback.onUpDownClick(this, 1);
                return;
            default:
                if (getOnPreferenceClickListener() != null) {
                    getOnPreferenceClickListener().onPreferenceClick(this);
                }
                return;
        }
    }

    interface OnUpDownClickListener {

        void onUpDownClick(Preference preference, int direction);
    }
}
