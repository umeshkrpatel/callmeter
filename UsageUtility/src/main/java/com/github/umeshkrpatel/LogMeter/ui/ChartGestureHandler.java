package com.github.umeshkrpatel.LogMeter.ui;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

public class ChartGestureHandler implements OnChartGestureListener {

    private static final String TAG = "ChartGestureHandler";
    private final View mView;
    private final Animation mAnimation;
    public ChartGestureHandler(View view, Animation animation) {
        mView = view;
        mAnimation = animation;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        ((PieChart)mView).setAnimation(mAnimation);
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        ((PieChart)mView).setAnimation(mAnimation);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        ((PieChart)mView).setAnimation(mAnimation);
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        ((PieChart)mView).setAnimation(mAnimation);
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.d(TAG, "onChartSingleTapped");
        //mView.setAnimation(mAnimation);
        PieChart chart = (PieChart)mView;
        chart.setAnimation(mAnimation);
        chart.invalidate();
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }
}
