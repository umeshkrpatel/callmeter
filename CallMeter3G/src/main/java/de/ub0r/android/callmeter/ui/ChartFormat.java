package de.ub0r.android.callmeter.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;

import de.ub0r.android.callmeter.data.DataProvider;

public class ChartFormat {
    public static final int[] CHART_COLORS = {
            Color.rgb(255, 102, 0), Color.rgb(192, 255, 140), Color.rgb(255, 247, 140)
    };
    /*
     * Handle to empty DataSet
     */
    public static float INVALID = (-1.0f);
    public static ArrayList<Integer> COLORS = null;
    public static ArrayList<Integer> GRAY_COLOR = null;

    public static void SetupPieChart(PieChart pieChart, ArrayList<Entry> entries, String centerText, ValueFormatter format) {
        pieChart.setDescription("");

        // enable hole and configure
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColorTransparent(true);
        //pieChart.setHoleColor(Color.LTGRAY);

        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);

        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);

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

        PieData pieData = new PieData(GetStringArray(), dataSet);
        pieData.setValueTypeface(Typeface.SANS_SERIF);
        pieData.setValueTextColor(Color.BLUE);

        if (format != null) {
            pieData.setValueFormatter(format);
        }

        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        pieChart.setData(pieData);

        pieChart.getLegend().setEnabled(false);
    }

    public static ArrayList<Integer> GetColorSet(boolean isGray) {
        if (isGray) {
            if (GRAY_COLOR == null) {
                GRAY_COLOR = new ArrayList<Integer>();
                GRAY_COLOR.add(Color.rgb(205, 201, 201));
            }
            return GRAY_COLOR;
        }

        if (COLORS != null)
            return COLORS;

        COLORS = new ArrayList<Integer>();

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

    public static ArrayList<String> GetStringArray() {
        ArrayList<String> strings = new ArrayList<String>();
        for (int i = 0; i < 4; i++) {
            strings.add(" ");
        }
        return strings;
    }

    public static class TimeFormatter implements ValueFormatter {

        @Override
        public String getFormattedValue(float v, Entry e, int i, ViewPortHandler h) {
            if (v == INVALID)
                return "";
            return Common.formatAmount(DataProvider.TYPE_CALL, v, true);
        }
    }

    public static class ByteFormatter implements ValueFormatter {

        @Override
        public String getFormattedValue(float v, Entry e, int i, ViewPortHandler h) {
            if (v == INVALID)
                return "";
            return Common.formatAmount(DataProvider.TYPE_DATA, v, false);
        }
    }

    public static class CountFormatter implements ValueFormatter {

        @Override
        public String getFormattedValue(float v, Entry e, int i, ViewPortHandler h) {
            if (v == INVALID)
                return "";
            return Common.formatAmount(DataProvider.TYPE_SMS, v, false);
        }
    }
}
