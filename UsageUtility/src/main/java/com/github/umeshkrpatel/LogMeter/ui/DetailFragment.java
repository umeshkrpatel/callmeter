package com.github.umeshkrpatel.LogMeter.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.umeshkrpatel.LogMeter.R;
import com.github.umeshkrpatel.LogMeter.data.DataProvider;
import com.github.umeshkrpatel.LogMeter.IDataDefs;
import com.github.umeshkrpatel.LogMeter.data.NameCache;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    private static final String ARG_NUMBER = "Number";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mNumber;

    private OnFragmentInteractionListener mListener;
    private ImageView ivProfilePic;
    private TextView tvProfileName;
    private TextView tvNumber;
    private PieChart callLogChart, smsLogChart;
    private BarChart callDetail, smsDetail;

    public DetailFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param number Contact Number(@link String).
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(String number) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NUMBER, number);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNumber = getArguments().getString(ARG_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.detail_fragment, container, false);
        ivProfilePic = (ImageView) v.findViewById(R.id.ivProfilePic);
        tvProfileName = (TextView) v.findViewById(R.id.tvProfileName);
        tvNumber = (TextView) v.findViewById(R.id.tvProfileNumber);
        callLogChart = (PieChart) v.findViewById(R.id.calllogChart);
        smsLogChart = (PieChart) v.findViewById(R.id.smslogChart);
        callDetail = (BarChart) v.findViewById(R.id.callDetail);
        smsDetail = (BarChart) v.findViewById(R.id.smsDetail);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateDetailFragment(final String number) {
        if (number == null || mNumber.equals(number))
            return;

        mNumber = number;
        try {
            tvNumber.setText(mNumber);
            tvProfileName.setText(NameCache.getInstance().getName(getContext(), mNumber, IDataDefs.Type.TYPE_CALL));
            Drawable drawable = NameCache.getInstance().getDrawableIcon(getContext(), mNumber, IDataDefs.Type.TYPE_CALL);
            if (drawable == null) {
                ivProfilePic.setImageResource(R.drawable.ic_face_empty_photo_id);
            } else {
                ivProfilePic.setImageDrawable(drawable);
            }

            Cursor cursor =
                    getActivity().getContentResolver().query(
                            DataProvider.Logs.SUM_URI,
                            DataProvider.Logs.PROJECTION_SUM,
                            IDataDefs.ILogs.REMOTE + " LIKE ? ", new String[]{mNumber}, null);
            if (cursor == null)
                return;

            ArrayList<Entry> entriesCalls = new ArrayList<>();
            ArrayList<Entry> entriesSms = new ArrayList<>();
            while (cursor.moveToNext()) {
                if (cursor.getInt(0) == IDataDefs.Type.TYPE_CALL.toInt()) {
                    if (cursor.getInt(1) == 15)
                        entriesCalls.add(new Entry(cursor.getInt(3), 1));
                    else
                        entriesCalls.add(new Entry(cursor.getInt(3), 0));
                } else if (cursor.getInt(0) == IDataDefs.Type.TYPE_SMS.toInt()) {
                    if (cursor.getInt(1) == 15)
                        entriesSms.add(new Entry(cursor.getInt(3), 1));
                    else
                        entriesSms.add(new Entry(cursor.getInt(3), 0));
                }
            }
            cursor.close();
            ChartFormat.SetupPieChart(callLogChart, entriesCalls, "Call Duration", new ChartFormat.TimeFormatter());
            ChartFormat.SetupPieChart(smsLogChart, entriesSms, "SMS Count", new ChartFormat.CountFormatter());
            smsLogChart.invalidate();
            callLogChart.invalidate();

            cursor =
                    getActivity().getContentResolver().query(
                            DataProvider.Logs.CONTENT_URI,
                            DataProvider.Logs.PROJECTION_DETAILS,
                            IDataDefs.ILogs.REMOTE + " LIKE ? ", new String[]{mNumber},
                            IDataDefs.ILogs.DATE + " DESC");
            ArrayList<BarEntry> barCallsIn = new ArrayList<>();
            ArrayList<BarEntry> barSmsIn = new ArrayList<>();
            ArrayList<BarEntry> barCallsOut = new ArrayList<>();
            ArrayList<BarEntry> barSmsOut = new ArrayList<>();

            if (cursor == null)
                return;
            int calli = 0, callo = 0, smsi = 0, smso = 0;
            while (cursor.moveToNext()) {
                if (cursor.getInt(0) == IDataDefs.Type.TYPE_CALL.toInt()) {
                    if (cursor.getInt(1) == 15) {
                        barCallsIn.add(new BarEntry(cursor.getInt(4), calli));
                        calli++;
                    } else {
                        barCallsOut.add(new BarEntry(cursor.getInt(4), callo));
                        callo++;
                    }
                } else if (cursor.getInt(0) == IDataDefs.Type.TYPE_SMS.toInt()) {
                    if (cursor.getInt(1) == 15) {
                        barSmsIn.add(new BarEntry(cursor.getInt(4), smsi));
                        smsi++;
                    } else {
                        barSmsOut.add(new BarEntry(cursor.getInt(4), smso));
                        smso++;
                    }
                }
            }
            cursor.close();

            ArrayList<BarDataSet> barDataSetsCall = new ArrayList<>();
            BarDataSet incoming = new BarDataSet(barCallsIn, "Incoming");
            incoming.setColor(Color.GREEN);
            barDataSetsCall.add(incoming);
            BarDataSet outgoing = new BarDataSet(barCallsOut, "Outgoing");
            outgoing.setColor(Color.RED);
            barDataSetsCall.add(outgoing);

            ArrayList<BarDataSet> barDataSetsSMS = new ArrayList<>();
            barDataSetsSMS.add(new BarDataSet(barSmsIn, "Incoming"));
            barDataSetsSMS.add(new BarDataSet(barSmsOut, "Outgoing"));
            barDataSetsSMS.get(0).setColor(Color.GREEN);
            barDataSetsSMS.get(1).setColor(Color.RED);
            ChartFormat.SetupBarChart(callDetail, barDataSetsCall);
            ChartFormat.SetupBarChart(smsDetail, barDataSetsSMS);
            smsDetail.invalidate();
            callDetail.invalidate();
        } catch (Exception e) {
            Log.d("DetailFragement", "Null getView()");
        }
    }

    public interface OnFragmentInteractionListener {
        String onDetailFragmentInteraction();
    }
}
