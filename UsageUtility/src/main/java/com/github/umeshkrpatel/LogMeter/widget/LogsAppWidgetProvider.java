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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.RemoteViews;

import com.github.umeshkrpatel.LogMeter.R;
import com.github.umeshkrpatel.LogMeter.data.ContactCache;
import com.github.umeshkrpatel.LogMeter.data.DataProvider;
import com.github.umeshkrpatel.LogMeter.data.LogRunnerService;
import com.github.umeshkrpatel.LogMeter.ui.prefs.Preferences;
import com.github.umeshkrpatel.LogMeter.ui.Common;
import com.github.umeshkrpatel.LogMeter.ui.UtilityActivity;

import java.util.Date;

import de.ub0r.android.lib.Utils;
import de.ub0r.android.logg0r.Log;

/**
 * Stats Widget.
 *
 * @author flx
 */
public final class LogsAppWidgetProvider extends AppWidgetProvider {

    /**
     * Plan.id for widget.
     */
    public static final String WIDGET_PLANID = "logswidget_planid_";
    /**
     * Show cost for widget.
     */
    static final String WIDGET_COST = "logswidget_cost_";
    /**
     * Size of the plan name text.
     */
    static final String WIDGET_PLAN_TEXTSIZE = "logswidget_plan_textsize_";
    /**
     * Size of the statistics text.
     */
    static final String WIDGET_STATS_TEXTSIZE = "logswidget_stats_textsize_";
    /**
     * Widget's text color.
     */
    static final String WIDGET_TEXTCOLOR = "logswidget_textcolor_";
    /**
     * Widget's background color.
     */
    static final String WIDGET_BGCOLOR = "logswidget_bgcolor_";
    /**
     * Widget's icon.
     */
    static final String WIDGET_ICON = "logswidget_icon_";
    /**
     * Must widget be small?
     */
    static final String WIDGET_SMALL = "logswidget_small_";
    /**
     * Tag for debug out.
     */
    private static final String TAG = "wdgt";

    /**
     * Update all widgets.
     *
     * @param context          {@link Context}
     * @param appWidgetManager {@link AppWidgetManager}
     * @param appWidgetIds     app widget ids
     */
    private static void updateWidgets(final Context context,
                                      final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);

        for (int id : appWidgetIds) {
            Log.d(TAG, "update widget: ", id);
            if (p.getLong(WIDGET_PLANID + id, -1) <= 0) {
                Log.w(TAG, "skip stale widget: " + id);
            } else {
                updateWidget(context, appWidgetManager, id);
            }
        }
    }

    /**
     * Update all running widgets.
     *
     * @param context {@link Context}
     */
    public static void updateWidgets(final Context context) {
        Log.d(TAG, "updateWidgets()");
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        assert appWidgetManager != null;
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                LogsAppWidgetProvider.class));
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    /**
     * Update a single widget.
     *
     * @param context          {@link Context}
     * @param appWidgetManager {@link AppWidgetManager}
     * @param appWidgetId      id of widget
     */
    static void updateWidget(final Context context, final AppWidgetManager appWidgetManager,
                             final int appWidgetId) {
        Log.d(TAG, "updateWidget(", appWidgetId, ")");
        final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean showHours = p.getBoolean(Preferences.PREFS_SHOWHOURS, true);
        final String cformat = Preferences.getCurrencyFormat(context);
        final long pid = p.getLong(WIDGET_PLANID + appWidgetId, -1L);
        final boolean showCost = p.getBoolean(WIDGET_COST + appWidgetId, false);
        final boolean showIcon = p.getBoolean(WIDGET_ICON + appWidgetId, false);
        final boolean smallWidget = p.getBoolean(WIDGET_SMALL + appWidgetId, false);
        final Float statsTextSize = p.getFloat(WIDGET_STATS_TEXTSIZE + appWidgetId,
                StatsAppWidgetConfigure.DEFAULT_TEXTSIZE);
        final Float planTextSize = p.getFloat(WIDGET_PLAN_TEXTSIZE + appWidgetId,
                StatsAppWidgetConfigure.DEFAULT_TEXTSIZE);
        final int textColor = p.getInt(WIDGET_TEXTCOLOR + appWidgetId,
                StatsAppWidgetConfigure.DEFAULT_TEXTCOLOR);
        final int bgColor = p.getInt(WIDGET_BGCOLOR + appWidgetId,
                StatsAppWidgetConfigure.DEFAULT_BGCOLOR);
        Log.d(TAG, "planid: ", pid);
        int widgetLayout = R.layout.stats_appwidget;
        if (smallWidget) {
            widgetLayout = R.layout.stats_appwidget_small;
        }
        final RemoteViews views = new RemoteViews(context.getPackageName(), widgetLayout);
        views.setImageViewBitmap(R.id.widget_bg, getBackground(bgColor));
        views.setFloat(R.id.plan, "setTextSize", planTextSize);
        views.setFloat(R.id.stats, "setTextSize", statsTextSize);
        views.setTextColor(R.id.plan, textColor);
        views.setTextColor(R.id.stats, textColor);
        views.setOnClickPendingIntent(R.id.widget,
                PendingIntent.getActivity(context, 0, new Intent(context, UtilityActivity.class), 0));

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(DataProvider.Logs.CONTENT_URI, DataProvider.Logs.PROJECTION,
                DataProvider.Logs.PLAN_ID + "=?", new String[]{String.valueOf(pid)},
                DataProvider.Logs.DATE + " DESC");
        assert c != null;
        if (c.moveToFirst()) {
            final int t = c.getInt(DataProvider.Logs.INDEX_TYPE);
            final long date = c.getLong(DataProvider.Logs.INDEX_DATE);
            final float cost = c.getFloat(DataProvider.Logs.INDEX_COST);
            final float free = c.getFloat(DataProvider.Logs.INDEX_FREE);
            StringBuilder buf1 = new StringBuilder();
            if (t == DataProvider.TYPE_MMS || t == DataProvider.TYPE_SMS
                    || t == DataProvider.TYPE_CALL) {
                String number = c.getString(DataProvider.Logs.INDEX_REMOTE);
                if (TextUtils.isEmpty(number)) {
                    buf1.append("???");
                } else {
                    String name = ContactCache.getInstance().getName(context, number, null);
                    if (name == null) {
                        buf1.append(number);
                    } else {
                        buf1.append(name);
                    }
                }
                buf1.append("\n");
            }
            buf1.append(
                    Common.formatAmount(t, c.getLong(DataProvider.Logs.INDEX_AMOUNT), showHours));
            if (showCost && cost > 0f) {
                buf1.append("\n");
                if (free == 0f) {
                    buf1.append(String.format(cformat, cost));
                } else if (free >= cost) {
                    buf1.append("(").append(String.format(cformat, cost)).append(")");
                } else {
                    buf1.append("(").append(String.format(cformat, free)).append(") ")
                            .append(String.format(cformat, cost - free));
                }
            }

            views.setTextViewText(R.id.plan, Common.formatDate(context, date) + " " + DateFormat.getTimeFormat(context).format(new Date(date)));
            views.setTextViewText(R.id.stats, buf1.toString());
            if (showIcon) {
                views.setViewVisibility(R.id.widget_icon, View.VISIBLE);
                switch (t) {
                    case DataProvider.TYPE_DATA:
                        views.setImageViewResource(R.id.widget_icon, R.drawable.ic_widget_data);
                        break;
                    case DataProvider.TYPE_CALL:
                    case DataProvider.TYPE_MIXED:
                        views.setImageViewResource(R.id.widget_icon, R.drawable.ic_widget_phone);
                        break;
                    case DataProvider.TYPE_SMS:
                    case DataProvider.TYPE_MMS:
                        views.setImageViewResource(R.id.widget_icon, R.drawable.ic_widget_message);
                        break;
                    default:
                        views.setViewVisibility(R.id.widget_icon, android.view.View.GONE);
                        break;
                }
            }
        } else {
            views.setImageViewResource(R.id.widget_icon, R.mipmap.ic_launcher);
            views.setViewVisibility(R.id.widget_icon, View.VISIBLE);
            views.setTextViewText(R.id.plan, "");
            views.setTextViewText(R.id.stats, "");
        }
        c.close();
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Get {@link Bitmap} for background.
     *
     * @param bgColor background color
     * @return {@link Bitmap}
     */
    private static Bitmap getBackground(final int bgColor) {
        //noinspection SuspiciousNameCombination
        final Bitmap bitmap = Bitmap.createBitmap(StatsAppWidgetProvider.WIDGET_WIDTH,
                StatsAppWidgetProvider.WIDGET_WIDTH, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bgColor);
        //noinspection SuspiciousNameCombination
        final RectF base = new RectF(0f, 0f, StatsAppWidgetProvider.WIDGET_WIDTH,
                StatsAppWidgetProvider.WIDGET_WIDTH);
        canvas.drawRoundRect(base, StatsAppWidgetProvider.WIDGET_RCORNER,
                StatsAppWidgetProvider.WIDGET_RCORNER, paint);
        return bitmap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
                         final int[] appWidgetIds) {
        Log.d(TAG, "onUpdate()");
        Utils.setLocale(context);
        Common.setDateFormat(context);

        // Update logs and run rule matcher
        LogRunnerService.update(context, LogRunnerService.ACTION_RUN_MATCHER);

        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(final Context context, final int[] appWidgetIds) {
        Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
        for (int id : appWidgetIds) {
            Log.d(TAG, "delete widget: ", id);
            e.remove(WIDGET_PLANID + id);
        }
        e.apply();
    }
}
