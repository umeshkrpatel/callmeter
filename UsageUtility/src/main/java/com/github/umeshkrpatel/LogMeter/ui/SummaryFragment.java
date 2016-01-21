package com.github.umeshkrpatel.LogMeter.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.umeshkrpatel.LogMeter.R;
import com.github.umeshkrpatel.LogMeter.data.DataProvider;
import com.github.umeshkrpatel.LogMeter.IDataDefs;
import com.github.umeshkrpatel.LogMeter.ui.prefs.Preferences;

import java.util.ArrayList;

/**
 * Callmeter's Log {@link LogsFragment}.
 *
 * @author flx
 */
public final class SummaryFragment extends ListFragment implements OnClickListener,
        OnItemLongClickListener, LoaderCallbacks<Cursor> {
    private static final String TAG = "LogsFragment";

    /**
     * Unique id for this {@link LogsFragment}s loader.
     */
    private static final int LOADER_UID = -2;
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

        if (planId >= 0L) {
            setPlanId(planId);
        }

        Cursor cursor = getActivity().getContentResolver().query(DataProvider.Logs.SUM_URI,
                        DataProvider.Logs.PROJECTION_SUM, null, null, null);

        PieChart pcCallDuration = (PieChart) v.findViewById(R.id.callDurationChart);
        PieChart pcCallCount = (PieChart) v.findViewById(R.id.callCountChart);
        PieChart pcSmsMms = (PieChart) v.findViewById(R.id.smsMmsChart);
        PieChart pcData = (PieChart) v.findViewById(R.id.dataChart);

        ArrayList<Entry> entriesCallDuration = new ArrayList<>();
        ArrayList<Entry> entriesCallCount = new ArrayList<>();
        ArrayList<Entry> entriesSmsMms = new ArrayList<>();
        ArrayList<Entry> entriesData = new ArrayList<>();

        int calli = 0, smsi = 0, mmsi = 0, datai = 0;
        if (cursor != null) {
            while (cursor.moveToNext()) {
//                DataProvider.Plans.Plan plan =
//                        new DataProvider.Plans.Plan(cursor);
//                if (plan.name.contains("2") || plan.type < 4 || plan.type > 6 || plan.atBa <= 0) {
//                    Log.d(TAG, "No data");
//                } else if (plan.type == 4) {
//                    entriesCallDuration.add(new Entry(plan.atBa, calli));
//                    entriesCallCount.add(new Entry(plan.atCount, calli));
//                    calli++;
//                } else if (plan.type == 5) {
//                    entriesSmsMms.add(new Entry(plan.atBa, smsi));
//                    smsi++;
//                } else if (plan.type == 6) {
//                    entriesSmsMms.set(mmsi,
//                            new Entry(plan.atBa + entriesSmsMms.get(mmsi).getVal(), mmsi));
//                    mmsi++;
//                } else if (plan.type == 7) {
//                    entriesData.add(new Entry(plan.atBa, datai));
//                    datai++;
//                }
//                if (type == null)
//                    continue;
//                switch (type) {
//                    case "CallsIn":
//                    //case "CallsIn2":
//                    case "CallsOut":
//                    //case "CallsOut2":
//                        entriesCallDuration.add(new Entry(cursor.getLong(4), calli));
//                        entriesCallCount.add(new Entry(cursor.getLong(3), calli));
//                        calli++;
//                        break;
//                    case "SMSIn":
//                    //case "SMSIn2":
//                    case "SMSOut":
//                    //case "SMSOut2":
//                        entriesSmsMms.add(new Entry(cursor.getLong(3), smsi));
//                        smsi++;
//                        break;
//                    case "MMSIn":
//                    case "MMSOut":
//                        //entriesSmsMms.set(mmsi,
//                        //    new Entry(cursor.getLong(3) + entriesSmsMms.get(mmsi).getVal(), mmsi));
//                        //mmsi++;
//                        break;
//                    case "DataInOut":
//                        entriesData.add(new Entry(cursor.getLong(3), datai));
//                        datai++;
//                        break;
//                }
                if (cursor.getInt(0) == IDataDefs.Type.TYPE_CALL.toInt()) {
                    entriesCallDuration.add(new Entry(cursor.getLong(3), calli));
                    entriesCallCount.add(new Entry(cursor.getLong(2), calli));
                    calli++;
                } else if (cursor.getInt(0) == IDataDefs.Type.TYPE_SMS.toInt()) {
                    entriesSmsMms.add(new Entry(cursor.getLong(3), smsi));
                    smsi++;
                } else if (cursor.getInt(0) == IDataDefs.Type.TYPE_MMS.toInt()) {
                    entriesSmsMms.set(mmsi,
                            new Entry(cursor.getLong(3) + entriesSmsMms.get(mmsi).getVal(), mmsi));
                    mmsi++;
                } else if (cursor.getInt(0) == IDataDefs.Type.TYPE_DATA_MOBILE.toInt()) {
                    entriesData.add(new Entry(cursor.getLong(3), datai));
                    datai++;
                }
            }
            cursor.close();
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
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
        /*
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
        */
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        Log.d(TAG, "onCreateLoader(" + id + "," + args + ")");
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
            Log.w(TAG, "error removing cursor e:" + e.getMessage());
        }
    }

    /**
     * Adapter binding logs to View.
     *
     * @author flx
     */
    public class SummaryAdapter extends ResourceCursorAdapter {

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
            return super.swapCursor(cursor);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void bindView(final View view, final Context context, final Cursor cursor) {

        }
    }
}
