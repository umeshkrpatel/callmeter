package com.github.umeshkrpatel.LogMeter.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.github.umeshkrpatel.LogMeter.IDataDefs;

import java.util.ArrayList;

public class ChartFormat {
    public static final int[] CHART_COLORS = {
            Color.rgb(255, 102, 0),
            Color.rgb(192, 255, 140),
            Color.rgb(255, 247, 140),
            Color.rgb(255, 247, 100)
    };
    /*
     * Handle to empty DataSet
     */
    public static float INVALID = (-1.0f);
    public static ArrayList<Integer> COLORS = null;
    public static ArrayList<Integer> GRAY_COLOR = null;

    public static void SetupBarChart(BarChart barChart, ArrayList<BarDataSet> dataSets) {
        barChart.setDescription("");

        // scaling can now only be done on x- and y-axis separately
        barChart.setPinchZoom(false);
        barChart.setTouchEnabled(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);

        barChart.getLegend().setEnabled(false);

        barChart.getXAxis().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);

        BarData barData = new BarData(generateChartDateList(1, 1), dataSets);
        barChart.setData(barData);
    }

    public static ArrayList<String> generateChartDateList(int startDate, int currentMonth) {
        ArrayList<String> dateStrings = new ArrayList<>();
        int endLimit;
        switch (currentMonth) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                endLimit = 31;
                break;
            case 2:
                endLimit = 28;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
            default:
                endLimit = 30;
                break;
        }
        for (int i = startDate; i <= endLimit; i++) {
            dateStrings.add(String.valueOf(i));
        }
        for (int i = 1; i < startDate; i++) {
            dateStrings.add(String.valueOf(i));
        }
        return dateStrings;
    }

    public static void SetupPieChart(PieChart pieChart, ArrayList<Entry> entries, String centerText,
                                     ValueFormatter valueFormat) {
        SetupPieChart(pieChart, entries, centerText, valueFormat, false, null);
    }

    public static void SetupPieChart(PieChart pieChart, ArrayList<Entry> entries, String centerText,
                                     ValueFormatter valueFormat, boolean enableLegent,
                                     ArrayList<String> legentStr) {
        pieChart.setDescription("");

        // enable hole and configure
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColorTransparent(true);
        //pieChart.setHoleColor(Color.LTGRAY);

        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);

        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(68f);

        pieChart.setDrawCenterText(true);
        pieChart.setCenterTextTypeface(Typeface.SANS_SERIF);
        pieChart.setCenterText(generateCenterSpannableText(centerText));

        // enable rotation of the chart by touch
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);

        boolean isGray = false;
        if (entries.isEmpty()) {
            entries.add(new Entry(INVALID, 0));
            isGray = true;
        }

        PieDataSet dataSet = new PieDataSet(entries, null);
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(10);
        dataSet.setColors(GetColorSet(isGray));

        legentStr = GetStringArray(legentStr);

        PieData pieData = new PieData(legentStr, dataSet);
        pieData.setValueTypeface(Typeface.SANS_SERIF);
        pieData.setValueTextColor(Color.BLUE);
        pieData.setValueTextSize(10.0f);

        if (valueFormat != null) {
            pieData.setValueFormatter(valueFormat);
        } else {
            pieData.setValueFormatter(EmptyFormatter.getInstance());
        }

        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawSliceText(false);

        pieChart.getLegend().setEnabled(enableLegent);
        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);
    }

    public static ArrayList<Integer> GetColorSet(boolean isGray) {
        if (isGray) {
            if (GRAY_COLOR == null) {
                GRAY_COLOR = new ArrayList<>();
                GRAY_COLOR.add(Color.rgb(205, 201, 201));
            }
            return GRAY_COLOR;
        }

        if (COLORS != null)
            return COLORS;

        COLORS = new ArrayList<>();

        for (int c : CHART_COLORS)
            COLORS.add(c);

        // 3*
        //for (int c : ColorTemplate.VORDIPLOM_COLORS)
        //    COLORS.add(c);

        // 2*
        //for (int c : ColorTemplate.JOYFUL_COLORS)
        //    COLORS.add(c);

        // 2*
        //for (int c : ColorTemplate.COLORFUL_COLORS)
        //    COLORS.add(c);

        // 3*
        //for (int c : ColorTemplate.LIBERTY_COLORS)
        //    COLORS.add(c);

        // 2*
        //for (int c : ColorTemplate.PASTEL_COLORS)
        //    COLORS.add(c);

        COLORS.add(ColorTemplate.getHoloBlue());
        return COLORS;
    }

    public static SpannableString generateCenterSpannableText(String string) {
        SpannableString s = new SpannableString(string);
        s.setSpan(new StyleSpan(Typeface.ITALIC), 0, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), 0, s.length(), 0);
        return s;
    }

    public static ArrayList<String> GetStringArray(ArrayList<String> strings) {
        if (strings == null)
            strings = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            strings.add(" ");
        }
        return strings;
    }

    public static class TimeFormatter implements ValueFormatter {

        public String getFormattedValue(Long v) {
            return Common.formatAmount(IDataDefs.Type.TYPE_CALL, v, true);
        }

        @Override
        public String getFormattedValue(float v, Entry e, int i, ViewPortHandler h) {
            if (v == INVALID)
                return "";
            return Common.formatAmount(IDataDefs.Type.TYPE_CALL, v, true);
        }
    }

    public static class ByteFormatter implements ValueFormatter {

        @Override
        public String getFormattedValue(float v, Entry e, int i, ViewPortHandler h) {
            if (v == INVALID)
                return "";
            return Common.formatAmount(IDataDefs.Type.TYPE_DATA_MOBILE, v, false);
        }
    }

    public static class CountFormatter implements ValueFormatter {

        @Override
        public String getFormattedValue(float v, Entry e, int i, ViewPortHandler h) {
            if (v == INVALID)
                return "";
            return Common.formatAmount(IDataDefs.Type.TYPE_SMS, v, false);
        }
    }

    private static class EmptyFormatter implements ValueFormatter {
        private static EmptyFormatter instance = null;
        public static EmptyFormatter getInstance() {
            if (instance == null) {
                instance = new EmptyFormatter();
            }
            return instance;
        }
        @Override
        public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
            return "";
        }
    }
}
