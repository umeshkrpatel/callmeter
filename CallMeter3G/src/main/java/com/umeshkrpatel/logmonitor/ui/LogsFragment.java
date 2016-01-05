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
package com.umeshkrpatel.logmonitor.ui;


import android.app.AlertDialog.Builder;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.format.DateFormat;
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

import java.util.Date;

import de.ub0r.android.callmeter.R;
import com.umeshkrpatel.logmonitor.data.DataProvider;
import com.umeshkrpatel.logmonitor.data.LogRunnerService;
import com.umeshkrpatel.logmonitor.data.NameCache;
import com.umeshkrpatel.logmonitor.data.NameLoader;
import com.umeshkrpatel.logmonitor.ui.prefs.Preferences;
import de.ub0r.android.lib.DbUtils;
import de.ub0r.android.logg0r.Log;

/**
 * Callmeter's Log {@link LogsFragment}.
 *
 * @author flx
 */
public final class LogsFragment extends ListFragment implements OnClickListener,
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
    private ToggleButton tbCall, tbSMS, tbMMS, tbData, tbIn, tbOut, tbPlan;
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
        setListAdapter(new LogAdapter(getActivity()));
        getListView().setOnItemLongClickListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.logs, container, false);
        tbCall = (ToggleButton) v.findViewById(R.id.calls);
        tbCall.setOnClickListener(this);
        tbSMS = (ToggleButton) v.findViewById(R.id.sms);
        tbSMS.setOnClickListener(this);
        tbMMS = (ToggleButton) v.findViewById(R.id.mms);
        tbMMS.setOnClickListener(this);
        tbData = (ToggleButton) v.findViewById(R.id.data);
        tbData.setOnClickListener(this);
        tbIn = (ToggleButton) v.findViewById(R.id.in);
        tbIn.setOnClickListener(this);
        tbOut = (ToggleButton) v.findViewById(R.id.out);
        tbOut.setOnClickListener(this);
        tbPlan = (ToggleButton) v.findViewById(R.id.plan);
        tbPlan.setOnClickListener(this);
        final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this
                .getActivity());
        tbCall.setChecked(p.getBoolean(PREF_CALL, true));
        tbSMS.setChecked(p.getBoolean(PREF_SMS, true));
        tbMMS.setChecked(p.getBoolean(PREF_MMS, true));
        tbData.setChecked(p.getBoolean(PREF_DATA, true));
        tbIn.setChecked(p.getBoolean(PREF_IN, true));
        tbOut.setChecked(p.getBoolean(PREF_OUT, true));

        String[] directions = getResources().getStringArray(R.array.direction_calls);
        tbIn.setText(directions[DataProvider.DIRECTION_IN]);
        tbIn.setTextOn(directions[DataProvider.DIRECTION_IN]);
        tbIn.setTextOff(directions[DataProvider.DIRECTION_IN]);
        tbOut.setText(directions[DataProvider.DIRECTION_OUT]);
        tbOut.setTextOn(directions[DataProvider.DIRECTION_OUT]);
        tbOut.setTextOff(directions[DataProvider.DIRECTION_OUT]);

        if (planId >= 0L) {
            setPlanId(planId);
        }
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
        final Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        e.putBoolean(PREF_CALL, tbCall.isChecked());
        e.putBoolean(PREF_SMS, tbSMS.isChecked());
        e.putBoolean(PREF_MMS, tbMMS.isChecked());
        e.putBoolean(PREF_DATA, tbData.isChecked());
        e.putBoolean(PREF_IN, tbIn.isChecked());
        e.putBoolean(PREF_OUT, tbOut.isChecked());
        e.commit();
    }

    /**
     * Set Adapter.
     *
     * @param forceUpdate force update
     */
    public void setAdapter(final boolean forceUpdate) {
        LogAdapter adapter = (LogAdapter) getListAdapter();
        if (!forceUpdate && adapter != null && !adapter.isEmpty()) {
            return;
        }

        String where = DataProvider.Logs.TABLE + "." + DataProvider.Logs.TYPE + " in (-1";
        if (tbCall != null && tbCall.isChecked()) {
            where += "," + DataProvider.TYPE_CALL;
        }
        if (tbSMS != null && tbSMS.isChecked()) {
            where += "," + DataProvider.TYPE_SMS;
        }
        if (tbMMS != null && tbMMS.isChecked()) {
            where += "," + DataProvider.TYPE_MMS;
        }
        if (tbData != null && tbData.isChecked()) {
            where += "," + DataProvider.TYPE_DATA;
        }
        where += ") and " + DataProvider.Logs.TABLE + "." + DataProvider.Logs.DIRECTION + " in (-1";
        if (tbIn != null && tbIn.isChecked()) {
            where += "," + DataProvider.DIRECTION_IN;
        }
        if (tbOut != null && tbOut.isChecked()) {
            where += "," + DataProvider.DIRECTION_OUT;
        }
        where += ")";

        if (planId > 0L && tbPlan != null && tbPlan.isChecked()) {
            String plans = DataProvider.Plans.parseMergerWhere(getActivity()
                    .getContentResolver(), planId);
            where = DbUtils.sqlAnd(plans, where);
            Log.d(TAG, "where: ", where);
        }
        Bundle args = new Bundle(1);
        args.putString("where", where);

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
        if (tbPlan != null) {
            if (id < 0L) {
                tbPlan.setVisibility(View.GONE);
            } else {
                String p = DataProvider.Plans.getName(getActivity().getContentResolver(),
                        planId);
                tbPlan.setText(p);
                tbPlan.setTextOn(p);
                tbPlan.setTextOff(p);
                tbPlan.setVisibility(View.VISIBLE);
                tbPlan.setChecked(true);
                tbIn.setChecked(true);
                tbOut.setChecked(true);
                tbCall.setChecked(true);
                tbData.setChecked(true);
                tbMMS.setChecked(true);
                tbSMS.setChecked(true);
            }
        }
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
                LogsFragment.this
                        .getActivity()
                        .getContentResolver()
                        .delete(ContentUris.withAppendedId(DataProvider.Logs.CONTENT_URI, id),
                                null, null);
                LogsFragment.this.setAdapter(true);
                LogRunnerService.update(LogsFragment.this.getActivity(), null);
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
        String where = null;
        if (args != null) {
            where = args.getString("where");
        }
        return new CursorLoader(getActivity(), DataProvider.Logs.CONTENT_URI_JOIN,
                DataProvider.Logs.PROJECTION_JOIN, where, null, DataProvider.Logs.DATE + " DESC");
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        Log.d(TAG, "onLoadFinished()");
        ((LogAdapter) getListAdapter()).swapCursor(data);
        Plans activity = (Plans) getActivity();
        if (activity != null) {
            activity.setProgress(-1);
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset()");
        try {
            ((LogAdapter) getListAdapter()).swapCursor(null);
        } catch (Exception e) {
            Log.w(TAG, "error removing cursor", e);
        }
    }

    /**
     * Adapter binding logs to View.
     *
     * @author flx
     */
    public class LogAdapter extends ResourceCursorAdapter {

        /**
         * Column ids.
         */
        private int idPlanName, idRuleName;

        /**
         * Default Constructor.
         *
         * @param context {@link Context}
         */
        public LogAdapter(final Context context) {
            super(context, R.layout.logs_item, null, true);
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

            StringBuilder buf = new StringBuilder();
            final int t = cursor.getInt(DataProvider.Logs.INDEX_TYPE);
            final int pt = cursor.getInt(DataProvider.Logs.INDEX_PLAN_TYPE);
            final long date = cursor.getLong(DataProvider.Logs.INDEX_DATE);
            buf.append(Common.formatDate(context, date));
            buf.append(" ");
            buf.append(DateFormat.getTimeFormat(context).format(new Date(date)));
            holder.tvTime.setText(buf.toString());

            String type = cursor.getString(idRuleName);
            if (type.contains("In")) {
                holder.ivType.setImageResource(R.drawable.ic_incoming);
            } else {
                holder.ivType.setImageResource(R.drawable.ic_outgoing);
            }

            String s = cursor.getString(DataProvider.Logs.INDEX_REMOTE);
            if (s == null || s.trim().length() == 0) {
                holder.tvNumber.setVisibility(View.GONE);
            } else {
                holder.tvName.setText(s);
                holder.tvNumber.setText(s);
                holder.tvName.setVisibility(View.VISIBLE);
                holder.tvNumber.setVisibility(View.VISIBLE);
                String format = "%s <" + s + ">";
                String name = NameCache.getInstance().get(s, format);
                if (name != null) {
                    holder.tvName.setText(name);
                }
            }

/*            s = cursor.getString(DataProvider.Logs.INDEX_MYNUMBER);
            boolean b = s != null && s.length() <= 2 && Utils.parseInt(s, -1) >= 0;

            if (LogsFragment.this.showMyNumber || b) {
                holder.tvNumber.setText(b ? R.string.my_sim_id_ : R.string.my_number_);
                //holder.tvMyNumber.setText(s);
                holder.tvNumber.setVisibility(View.VISIBLE);
                //holder.tvMyNumber.setVisibility(View.VISIBLE);
            } else {
                //holder.tvMyNumberLabel.setVisibility(View.GONE);
                holder.tvNumber.setVisibility(View.GONE);
            }
*/
            final long amount = cursor.getLong(DataProvider.Logs.INDEX_AMOUNT);
            s = Common.formatAmount(t, amount, LogsFragment.this.showHours);
            if (s == null || s.trim().length() == 0 || s.equals("1")) {
                holder.tvDuration.setVisibility(View.GONE);
            } else {
                holder.tvDuration.setVisibility(View.VISIBLE);
                holder.tvDuration.setText(s);
            }

            /*
            final float ba = cursor.getFloat(DataProvider.Logs.INDEX_BILL_AMOUNT);
            if (amount != ba || pt == DataProvider.TYPE_MIXED) {
                holder.tvBilledLength.setText(Common.formatAmount(pt, ba,
                        LogsFragment.this.showHours));
                holder.tvBilledLength.setVisibility(View.VISIBLE);
                holder.tvBilledLengthLabel.setVisibility(View.VISIBLE);
            } else {
                holder.tvBilledLength.setVisibility(View.GONE);
                holder.tvBilledLengthLabel.setVisibility(View.GONE);
            }
            final float cost = cursor.getFloat(DataProvider.Logs.INDEX_COST);
            final float free = cursor.getFloat(DataProvider.Logs.INDEX_FREE);

            if (cost > 0f) {
                String c;
                if (free == 0f) {
                    c = String.format(LogsFragment.this.cformat, cost);
                } else if (free >= cost) {
                    c = "(" + String.format(LogsFragment.this.cformat, cost) + ")";
                } else {
                    c = "(" + String.format(LogsFragment.this.cformat, free) + ") "
                            + String.format(LogsFragment.this.cformat, cost - free);
                }
                holder.tvCost.setText(c);
                holder.tvCost.setVisibility(View.VISIBLE);
                holder.tvCostLabel.setVisibility(View.VISIBLE);
            } else {
                holder.tvCost.setVisibility(View.GONE);
                holder.tvCostLabel.setVisibility(View.GONE);
            }
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
        }
    }
}
