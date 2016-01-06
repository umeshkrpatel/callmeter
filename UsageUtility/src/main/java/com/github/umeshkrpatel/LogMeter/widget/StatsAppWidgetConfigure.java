/*
 * Copyright (C) 2010-2013 Felix Bechstein
 * 
 * This file is part of Call Meter 3G.
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
package com.github.umeshkrpatel.LogMeter.widget;


import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.github.umeshkrpatel.LogMeter.R;
import com.github.umeshkrpatel.LogMeter.data.DataProvider;
import de.ub0r.android.lib.Utils;
import de.ub0r.android.logg0r.Log;
import com.github.umeshkrpatel.LogMeter.utils.AmbilWarnaDialog;
import com.github.umeshkrpatel.LogMeter.utils.AmbilWarnaDialog.OnAmbilWarnaListener;

/**
 * Configure a stats widget.
 *
 * @author flx
 */
public final class StatsAppWidgetConfigure extends AppCompatActivity implements OnClickListener,
        OnCheckedChangeListener, OnSeekBarChangeListener {

    /**
     * Default text size.
     */
    static final float DEFAULT_TEXTSIZE = 10f;
    /**
     * Default text color.
     */
    static final int DEFAULT_TEXTCOLOR = 0xffffffff;
    /**
     * Default background color.
     */
    static final int DEFAULT_BGCOLOR = 0x80000000;
    /**
     * Tag for logging.
     */
    private static final String TAG = "wdgtcfg";
    /**
     * Bit mask for colors.
     */
    private static final int BITMASK_COLOR = 0x00FFFFFF;
    /**
     * Shift for transparency.
     */
    private static final int BITSHIFT_TRANSPARENCY = 24;
    /**
     * Projection for {@link SimpleCursorAdapter} query.
     */
    private static final String[] PROJ_ADAPTER = new String[]{DataProvider.Plans.ID,
            DataProvider.Plans.NAME, DataProvider.Plans.SHORTNAME};
    /**
     * Widget id.
     */
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    /**
     * {@link Spinner} holding the plan.
     */
    private Spinner spinner;
    /**
     * {@link CheckBox}s.
     */
    private CheckBox cbHideName, cbShowShortname, cbShowCost, cbShowBillp, cbShowIcon,
            cbSmallWidget;
    /**
     * {@link EditText}s.
     */
    private EditText etPlanTextSize, etStatsTextSize;
    /**
     * {@link Button}s.
     */
    private Button btnTextColor, btnBgColor;
    /**
     * {@link View}s.
     */
    private View vTextColor, vBgColor;
    /**
     * {@link SeekBar}.
     */
    private SeekBar sbBgTransparency;
    /**
     * Does the widget already exist?
     */
    private boolean isExistingWidget = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        Utils.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_appwidget_config);
        spinner = (Spinner) findViewById(R.id.spinner);
        cbHideName = (CheckBox) findViewById(R.id.hide_name);
        cbHideName.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                StatsAppWidgetConfigure.this.cbShowShortname.setEnabled(!isChecked);
            }
        });
        cbShowShortname = (CheckBox) findViewById(R.id.shortname);
        cbShowShortname.setOnCheckedChangeListener(this);
        cbShowCost = (CheckBox) findViewById(R.id.cost);
        cbShowBillp = (CheckBox) findViewById(R.id.pbillp);
        cbShowIcon = (CheckBox) findViewById(R.id.show_icon);
        cbSmallWidget = (CheckBox) findViewById(R.id.small_widget);
        etPlanTextSize = (EditText) findViewById(R.id.widget_plan_textsize);
        etStatsTextSize = (EditText) findViewById(R.id.widget_stats_textsize);
        btnTextColor = (Button) findViewById(R.id.textcolor);
        btnBgColor = (Button) findViewById(R.id.bgcolor);
        vTextColor = findViewById(R.id.textcolorfield);
        vBgColor = findViewById(R.id.bgcolorfield);
        sbBgTransparency = (SeekBar) findViewById(R.id.bgtransparency);
        setAdapter();
        findViewById(R.id.ok).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        btnTextColor.setOnClickListener(this);
        btnBgColor.setOnClickListener(this);
        sbBgTransparency.setOnSeekBarChangeListener(this);
        setTextColor(DEFAULT_TEXTCOLOR);
        setBgColor(DEFAULT_BGCOLOR, false);
    }

    /**
     * Set {@link SimpleCursorAdapter} for {@link Spinner}.
     */
    private void setAdapter() {
        final Cursor c = getContentResolver().query(DataProvider.Plans.CONTENT_URI,
                PROJ_ADAPTER, DataProvider.Plans.WHERE_PLANS, null, DataProvider.Plans.NAME);
        String[] fieldName;
        if (cbShowShortname.isChecked()) {
            fieldName = new String[]{DataProvider.Plans.SHORTNAME};
        } else {
            fieldName = new String[]{DataProvider.Plans.NAME};
        }
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item, c, fieldName,
                new int[]{android.R.id.text1});
        final int pos = spinner.getSelectedItemPosition();
        spinner.setAdapter(adapter);
        if (pos >= 0 && pos < spinner.getCount()) {
            spinner.setSelection(pos);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();
        Utils.setLocale(this);

        final Intent intent = getIntent();
        if (intent != null) {
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        isExistingWidget = mAppWidgetId > 0;
        load();
    }

    /**
     * {@inheritDoc}
     */
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.ok:
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .edit();
                editor.putLong(StatsAppWidgetProvider.WIDGET_PLANID + mAppWidgetId,
                        spinner.getSelectedItemId());
                editor.putBoolean(StatsAppWidgetProvider.WIDGET_HIDETNAME + mAppWidgetId,
                        cbHideName.isChecked());
                editor.putBoolean(StatsAppWidgetProvider.WIDGET_SHORTNAME + mAppWidgetId,
                        cbShowShortname.isChecked());
                editor.putBoolean(StatsAppWidgetProvider.WIDGET_COST + mAppWidgetId,
                        cbShowCost.isChecked());
                editor.putBoolean(StatsAppWidgetProvider.WIDGET_BILLPERIOD + mAppWidgetId,
                        cbShowBillp.isChecked());
                editor.putBoolean(StatsAppWidgetProvider.WIDGET_ICON + mAppWidgetId,
                        cbShowIcon.isChecked());
                editor.putBoolean(StatsAppWidgetProvider.WIDGET_SMALL + mAppWidgetId,
                        cbSmallWidget.isChecked());
                editor.putFloat(StatsAppWidgetProvider.WIDGET_STATS_TEXTSIZE + mAppWidgetId,
                        Utils.parseFloat(etStatsTextSize.getText().toString(),
                                DEFAULT_TEXTSIZE));
                editor.putFloat(StatsAppWidgetProvider.WIDGET_PLAN_TEXTSIZE + mAppWidgetId,
                        Utils.parseFloat(etPlanTextSize.getText().toString(),
                                DEFAULT_TEXTSIZE));
                editor.putInt(StatsAppWidgetProvider.WIDGET_TEXTCOLOR + mAppWidgetId,
                        getTextColor());
                editor.putInt(StatsAppWidgetProvider.WIDGET_BGCOLOR + mAppWidgetId,
                        getBgColor());
                editor.commit();

                final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                StatsAppWidgetProvider.updateWidget(this, appWidgetManager, mAppWidgetId);

                final Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
                break;
            case R.id.cancel:
                finish();
                break;
            case R.id.textcolor:
                new AmbilWarnaDialog(this, getTextColor(), new OnAmbilWarnaListener() {
                    @Override
                    public void onOk(final AmbilWarnaDialog dialog, final int color) {
                        StatsAppWidgetConfigure.this.setTextColor(color);
                    }

                    @Override
                    public void onCancel(final AmbilWarnaDialog dialog) {
                        // nothing to do
                    }

                    public void onReset(final AmbilWarnaDialog dialog) {
                        StatsAppWidgetConfigure.this.setTextColor(DEFAULT_TEXTCOLOR);
                    }
                }).show();
                break;
            case R.id.bgcolor:
                new AmbilWarnaDialog(this, getBgColor(), new OnAmbilWarnaListener() {
                    @Override
                    public void onOk(final AmbilWarnaDialog dialog, final int color) {
                        StatsAppWidgetConfigure.this.setBgColor(color, false);
                    }

                    @Override
                    public void onCancel(final AmbilWarnaDialog dialog) {
                        // nothing to do
                    }

                    public void onReset(final AmbilWarnaDialog dialog) {
                        StatsAppWidgetConfigure.this.setBgColor(DEFAULT_BGCOLOR, false);
                    }
                }).show();
                break;
            default:
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.shortname:
                setAdapter();
                return;
            default:
                return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress,
                                  final boolean fromUser) {
        Log.d(TAG, "onProgressChanged(", progress, ")");
        final int tp = 255 - progress;
        int c = getBgColor();
        Log.d(TAG, "color: ", c);
        c = c & BITMASK_COLOR;
        Log.d(TAG, "color: ", c);
        Log.i(TAG, "transparency: " + Integer.toHexString(tp << BITSHIFT_TRANSPARENCY));
        c = c | tp << BITSHIFT_TRANSPARENCY;
        Log.d(TAG, "color: ", c);
        setBgColor(c, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
        // nothing todo
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
        // nothing todo
    }

    /**
     * Load widget's configuration.
     */
    private void load() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        long pid = p.getLong(StatsAppWidgetProvider.WIDGET_PLANID + mAppWidgetId, -1);
        SpinnerAdapter adapter = spinner.getAdapter();
        int l = spinner.getCount();
        for (int i = 0; i < l; i++) {
            if (adapter.getItemId(i) == pid) {
                spinner.setSelection(i);
                break;
            }
        }
        cbHideName.setChecked(p.getBoolean(StatsAppWidgetProvider.WIDGET_HIDETNAME
                + mAppWidgetId, false));
        cbShowShortname.setChecked(p.getBoolean(StatsAppWidgetProvider.WIDGET_SHORTNAME
                + mAppWidgetId, false));
        cbShowCost.setChecked(p.getBoolean(StatsAppWidgetProvider.WIDGET_COST
                + mAppWidgetId, false));
        cbShowBillp.setChecked(p.getBoolean(StatsAppWidgetProvider.WIDGET_BILLPERIOD
                + mAppWidgetId, false));
        cbShowIcon.setChecked(p.getBoolean(StatsAppWidgetProvider.WIDGET_ICON
                + mAppWidgetId, false));
        cbSmallWidget.setChecked(p.getBoolean(StatsAppWidgetProvider.WIDGET_SMALL
                + mAppWidgetId, false));
        float f = p.getFloat(StatsAppWidgetProvider.WIDGET_STATS_TEXTSIZE + mAppWidgetId, -1);
        if (f > 0f && f != DEFAULT_TEXTSIZE) {
            etStatsTextSize.setText(String.valueOf(f));
        } else {
            etStatsTextSize.setText(null);
        }
        f = p.getFloat(StatsAppWidgetProvider.WIDGET_PLAN_TEXTSIZE + mAppWidgetId, -1);
        if (f > 0f && f != DEFAULT_TEXTSIZE) {
            etPlanTextSize.setText(String.valueOf(f));
        } else {
            etPlanTextSize.setText(null);
        }
        setTextColor(p.getInt(StatsAppWidgetProvider.WIDGET_TEXTCOLOR + mAppWidgetId,
                DEFAULT_TEXTCOLOR));
        setBgColor(p.getInt(StatsAppWidgetProvider.WIDGET_BGCOLOR + mAppWidgetId,
                DEFAULT_BGCOLOR), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (isExistingWidget) {
            getMenuInflater().inflate(R.menu.menu_widget, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_del:
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .remove(StatsAppWidgetProvider.WIDGET_PLANID + mAppWidgetId).commit();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Get background color currently set.
     *
     * @return color
     */
    private int getBgColor() {
        return Long.decode(btnBgColor.getText().toString()).intValue();
    }

    /**
     * Set the background color to btnBgColor and vBgColorField.
     *
     * @param color           color to set
     * @param fromProgressBar true, if setColor is called from onProgessChanged()
     */
    private void setBgColor(final int color, final boolean fromProgressBar) {
        Log.d(TAG, "setBgColor(", color, ", ", fromProgressBar, ")");
        String hex = AmbilWarnaDialog.colorToString(color);
        Log.d(TAG, "color: ", hex);
        while (hex.length() < 9) {
            hex = "#0" + hex.substring(1);
            Log.d(TAG, "color: ", hex);
        }
        btnBgColor.setText(hex);
        vBgColor.setBackgroundColor(color);
        if (!fromProgressBar) {
            int trans = color >> BITSHIFT_TRANSPARENCY;
            Log.d(TAG, "transparency: ", trans);
            if (trans < 0) {
                trans = 256 + trans;
                Log.d(TAG, "transparency: ", trans);
            }
            sbBgTransparency.setProgress(255 - trans);
        }
    }

    /**
     * Get text color currently set.
     *
     * @return color
     */
    private int getTextColor() {
        return Long.decode(btnTextColor.getText().toString()).intValue();
    }

    /**
     * Set the text color to btnTextColor and vTextColorField.
     *
     * @param color color to set
     */
    private void setTextColor(final int color) {
        Log.d(TAG, "setTextColor(", color, ")");
        String hex = AmbilWarnaDialog.colorToString(color);
        Log.d(TAG, "color: ", hex);
        while (hex.length() < 9) {
            hex = "#0" + hex.substring(1);
            Log.d(TAG, "color: ", hex);
        }
        btnTextColor.setText(hex);
        vTextColor.setBackgroundColor(color);
    }
}
