/*
 * Copyright (C) 2009-2013 Felix Bechstein
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
package com.github.umeshkrpatel.LogMeter.ui;


import android.app.AlertDialog.Builder;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

import com.github.umeshkrpatel.LogMeter.R;
import com.github.umeshkrpatel.LogMeter.data.DataProvider;
import com.github.umeshkrpatel.LogMeter.data.LogRunnerService;
import com.github.umeshkrpatel.LogMeter.data.NameLoader;
import com.github.umeshkrpatel.LogMeter.prefs.Preferences;
import de.ub0r.android.logg0r.Log;

/**
 * Callmeter's Log {@link LogsFragment}.
 *
 * @author flx
 */
public final class SummaryFragment extends ListFragment implements OnClickListener,
        OnItemLongClickListener, LoaderCallbacks<Cursor> {

    /**
     * Tag for output.
     */
    private static final String TAG = "LogsFragment";

    /**
     * Prefs: {@link ToggleButton} state for calls.
     */
    private static final String PREF_CALL = "_logs_call";

    /**
     * Prefs: {@link ToggleButton} state for sms.
     */
    private static final String PREF_SMS = "_logs_sms";

    /**
     * Prefs: {@link ToggleButton} state for mms.
     */
    private static final String PREF_MMS = "_logs_mms";

    /**
     * Prefs: {@link ToggleButton} state for data.
     */
    private static final String PREF_DATA = "_logs_data";

    /**
     * Prefs: {@link ToggleButton} state for in.
     */
    private static final String PREF_IN = "_in";

    /**
     * Prefs: {@link ToggleButton} state for out.
     */
    private static final String PREF_OUT = "_out";
    /**
     * Unique id for this {@link LogsFragment}s loader.
     */
    private static final int LOADER_UID = -2;
    /**
     * {@link ToggleButton}s.
     */
    //private ToggleButton tbCall, tbSMS, tbMMS, tbData, tbIn, tbOut, tbPlan;
    private PieChart pcCallDuration, pcCallCount, pcSmsMms, pcData;
    /**
     * Show my number.
     */
    private boolean showMyNumber = false;
    /**
     * Show hours and days.
     */
    private boolean showHours = true;
    /**
     * Currency format.
     */
    private String cformat;
    /**
     * Selected plan id.
     */
    private long planId = -1;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(new SummaryAdapter(getActivity()));
        getListView().setOnItemLongClickListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.summary, container, false);
        final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this
                .getActivity());
        String[] directions = getResources().getStringArray(R.array.direction_calls);

        if (planId >= 0L) {
            setPlanId(planId);
        }
        Cursor cursor = getActivity().getContentResolver().query(DataProvider.Plans.CONTENT_URI_SUM
                        .buildUpon()
                                //.appendQueryParameter(DataProvider.UtilityActivity.PARAM_DATE, String.valueOf(now))
                        .appendQueryParameter(DataProvider.Plans.PARAM_HIDE_ZERO,
                                String.valueOf(false))
                        .appendQueryParameter(DataProvider.Plans.PARAM_HIDE_NOCOST,
                                String.valueOf(false))
                        .appendQueryParameter(DataProvider.Plans.PARAM_HIDE_TODAY,
                                String.valueOf(false))
                        .appendQueryParameter(DataProvider.Plans.PARAM_HIDE_ALLTIME,
                                String.valueOf(false)).build(), DataProvider.Plans.PROJECTION_SUM,
                null, null, null);

        pcCallDuration = (PieChart) v.findViewById(R.id.callDurationChart);
        pcCallCount = (PieChart) v.findViewById(R.id.callCountChart);
        pcSmsMms = (PieChart) v.findViewById(R.id.smsMmsChart);
        pcData = (PieChart) v.findViewById(R.id.dataChart);

        ArrayList<Entry> entriesCallDuration = new ArrayList<Entry>();
        ArrayList<Entry> entriesCallCount = new ArrayList<Entry>();
        ArrayList<Entry> entriesSmsMms = new ArrayList<Entry>();
        ArrayList<Entry> entriesData = new ArrayList<Entry>();

        int calli = 1, smsi = 1, mmsi = 1, datai = 1;
        while (cursor.moveToNext()) {
            DataProvider.Plans.Plan plan =
                    new DataProvider.Plans.Plan(cursor);
            if (plan.name.contains("2") || plan.type < 4 || plan.type > 6 || plan.atBa <= 0) {
                continue;
            } else if (plan.type == 4) {
                entriesCallDuration.add(new Entry(plan.atBa, calli));
                entriesCallCount.add(new Entry(plan.atCount, calli));
                calli++;
            } else if (plan.type == 5) {
                entriesSmsMms.add(new Entry(plan.atBa, smsi));
                smsi++;
            } else if (plan.type == 6) {
                entriesSmsMms.set(mmsi,
                        new Entry(plan.atBa + entriesSmsMms.get(mmsi).getVal(), mmsi));
                mmsi++;
            } else if (plan.type == 7) {
                entriesData.add(new Entry(plan.atBa, datai));
                datai++;
            }
        }

        ValueFormatter formatter = new ChartFormat.CountFormatter();
        ChartFormat.SetupPieChart(pcCallDuration, entriesCallDuration, "Call Duration", new ChartFormat.TimeFormatter());
        ChartFormat.SetupPieChart(pcCallCount, entriesCallCount, "Call Count", formatter);
        ChartFormat.SetupPieChart(pcSmsMms, entriesSmsMms, "SMS/MMSs", formatter);
        ChartFormat.SetupPieChart(pcData, entriesData, "Data", new ChartFormat.ByteFormatter());

        pcCallDuration.invalidate();
        pcCallCount.invalidate();
        pcSmsMms.invalidate();
        pcData.invalidate();
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        Common.setDateFormat(getActivity());
        Cursor c = this
                .getActivity()
                .getContentResolver()
                .query(DataProvider.Rules.CONTENT_URI, new String[]{DataProvider.Rules.ID},
                        DataProvider.Rules.MYNUMBER + " like '___%'", null, null);
        if (c != null) {
            showMyNumber = c.getCount() > 0;
            c.close();
        } else {
            showMyNumber = false;
        }
        showHours = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(Preferences.PREFS_SHOWHOURS, true);
        cformat = Preferences.getCurrencyFormat(getActivity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
        //final Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        //e.putBoolean(PREF_CALL, tbCall.isChecked());
        //e.putBoolean(PREF_SMS, tbSMS.isChecked());
        //e.putBoolean(PREF_MMS, tbMMS.isChecked());
        //e.putBoolean(PREF_DATA, tbData.isChecked());
        //e.putBoolean(PREF_IN, tbIn.isChecked());
        //e.putBoolean(PREF_OUT, tbOut.isChecked());
        //e.commit();
    }

    /**
     * Set Adapter.
     *
     * @param forceUpdate force update
     */
    public void setAdapter(final boolean forceUpdate) {
        SummaryAdapter adapter = (SummaryAdapter) getListAdapter();
        if (!forceUpdate && adapter != null && !adapter.isEmpty()) {
            return;
        }

        String where[] = DataProvider.Plans.PROJECTION_SUM;
        Bundle args = new Bundle(1);
        args.putStringArray("where", where);

        LoaderManager lm = getLoaderManager();
        if (lm.getLoader(LOADER_UID) == null) {
            lm.initLoader(LOADER_UID, args, this);
        } else {
            lm.restartLoader(LOADER_UID, args, this);
        }
    }

    /**
     * Set filter to show only given plan. Set to -1 for no filter.
     *
     * @param id plan's id
     */
    public void setPlanId(final long id) {
        planId = id;
        //if (tbPlan != null) {
        //    if (id < 0L) {
        //        tbPlan.setVisibility(View.GONE);
        //    } else {
        //        String p = DataProvider.UtilityActivity.getName(getActivity().getContentResolver(),
        //                planId);
        //        tbPlan.setText(p);
        //        tbPlan.setTextOn(p);
        //        tbPlan.setTextOff(p);
        //        tbPlan.setVisibility(View.VISIBLE);
        //        tbPlan.setChecked(true);
        //        tbIn.setChecked(true);
        //        tbOut.setChecked(true);
        //        tbCall.setChecked(true);
        //        tbData.setChecked(true);
        //        tbMMS.setChecked(true);
        //        tbSMS.setChecked(true);
        //    }
        //}
        if (isVisible()) {
            setAdapter(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(final View v) {
        setAdapter(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_logs, menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add:
                getActivity().startActivity(new Intent(getActivity(), AddLogActivity.class));
                return true;
            case R.id.item_export_csv:
                getActivity().startActivity(
                        new Intent(Preferences.ACTION_EXPORT_CSV, null, getActivity(),
                                Preferences.class)
                );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                   final int position, final long id) {
        final Builder b = new Builder(getActivity());
        b.setCancelable(true);
        b.setItems(R.array.dialog_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                SummaryFragment.this
                        .getActivity()
                        .getContentResolver()
                        .delete(ContentUris.withAppendedId(DataProvider.Logs.CONTENT_URI, id),
                                null, null);
                SummaryFragment.this.setAdapter(true);
                LogRunnerService.update(SummaryFragment.this.getActivity(), null);
            }
        });
        b.setNegativeButton(android.R.string.cancel, null);
        b.show();
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        Log.d(TAG, "onCreateLoader(", id, ",", args, ")");
        getActivity().setProgress(1);
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        Log.d(TAG, "onLoadFinished()");
        ((SummaryAdapter) getListAdapter()).swapCursor(data);
        UtilityActivity activity = (UtilityActivity) getActivity();
        if (activity != null) {
            activity.setProgress(-1);
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset()");
        try {
            ((SummaryAdapter) getListAdapter()).swapCursor(null);
        } catch (Exception e) {
            Log.w(TAG, "error removing cursor", e);
        }
    }

    /**
     * Adapter binding logs to View.
     *
     * @author flx
     */
    public class SummaryAdapter extends ResourceCursorAdapter {

        /**
         * Column ids.
         */
        private int idPlanName, idRuleName;

        /**
         * Default Constructor.
         *
         * @param context {@link Context}
         */
        public SummaryAdapter(final Context context) {
            super(context, R.layout.summary_item, null, true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final Cursor swapCursor(final Cursor cursor) {
            Cursor c = super.swapCursor(cursor);
            idPlanName = cursor.getColumnIndex(DataProvider.Plans.NAME);
            idRuleName = cursor.getColumnIndex(DataProvider.Rules.NAME);
            return c;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void bindView(final View view, final Context context, final Cursor cursor) {
            /*
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder == null) {
                holder = new ViewHolder();
                holder.tvName = (TextView) view.findViewById(R.id.tvName);
                holder.tvNumber = (TextView) view.findViewById(R.id.tvNumber);
                holder.tvTime = (TextView) view.findViewById(R.id.tvTime);
                holder.tvDuration = (TextView) view.findViewById(R.id.tvDuration);
                holder.ivType = (ImageView) view.findViewById(R.id.ivType);
                view.setTag(holder);
            } else if (holder.loader != null && !holder.loader.isCancelled()) {
                holder.loader.cancel(true);
            }

            long [] calls = new long[2];
            long [] smss = new long[2];
            long [] mmss = new long[2];
            long datas = 0;
            int calli = 0, smsi = 0, mmsi = 0, datai = 0;
            while(cursor.moveToNext()) {
                DataProvider.UtilityActivity.Plan plan =
                        new DataProvider.UtilityActivity.Plan(cursor);
                if (plan.name.contains("2") || plan.type < 4 || plan.type > 6) {
                    continue;
                } else if (plan.type == 4) {
                    calls[calli] = (long) plan.atBa;
                    calli++;
                } else if (plan.type == 5) {
                    smss[smsi] = (long) plan.atBa;
                    smsi++;
                } else if (plan.type == 6) {
                    datas = (long) plan.atBa;
                }
            }

            /*
            if (plan.sname.contains("CallsIn")) {
                holder.tvDuration.setText(String.valueOf(plan.bpBa));
            } else if (plan.sname.contains("CallsOut")) {
                holder.callOut++;
            } else if (plan.sname.contains("SMSIn")) {
                holder.smsIn++;
            } else if (plan.sname.contains("SMSOut")) {
                holder.smsOut++;
            } else if (plan.sname.contains("MMSIn")) {
                holder.mmsIn++;
            } else if (plan.sname.contains("MMSOut")) {
                holder.mmsOut++;
            } else if (plan.sname.contains("DATAInOut")) {
                holder.dataIn++;
            } else {
                //holder.dataOut++;
            }
            holder.tvName.setText(String.valueOf(holder.callIn));
            */
        }

        /**
         * View holder.
         *
         * @author flx
         */
        private class ViewHolder {

            /**
             * Holder for item's view.
             */
            TextView tvName, tvNumber, tvTime, tvDuration;

            ImageView ivType;
            /**
             * Hold {@link NameLoader}.
             */
            NameLoader loader;
            long callIn, callOut, smsIn, smsOut, mmsIn, mmsOut, dataIn, dataOut;
        }
    }
}
