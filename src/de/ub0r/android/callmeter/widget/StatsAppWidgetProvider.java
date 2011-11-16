/*
 * Copyright (C) 2010-2011 Felix Bechstein
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
package de.ub0r.android.callmeter.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;
import de.ub0r.android.callmeter.CallMeter;
import de.ub0r.android.callmeter.R;
import de.ub0r.android.callmeter.data.DataProvider;
import de.ub0r.android.callmeter.data.DataProvider.Plans.Plan;
import de.ub0r.android.callmeter.data.LogRunnerService;
import de.ub0r.android.callmeter.ui.Plans;
import de.ub0r.android.callmeter.ui.prefs.Preferences;
import de.ub0r.android.lib.Log;
import de.ub0r.android.lib.Utils;

/**
 * Stats Widget.
 * 
 * @author flx
 */
public final class StatsAppWidgetProvider extends AppWidgetProvider {
	/** Tag for debug out. */
	private static final String TAG = "wdgt";

	/** Plan.id for widget. */
	static final String WIDGET_PLANID = "widget_planid_";
	/** Hide name for widget. */
	static final String WIDGET_HIDETNAME = "widget_hidename_";
	/** Show short name for widget. */
	static final String WIDGET_SHORTNAME = "widget_shortname_";
	/** Show cost for widget. */
	static final String WIDGET_COST = "widget_cost_";
	/** Show progress of billing period for widget. */
	static final String WIDGET_BILLPERIOD = "widget_billp_";
	/** Size of the plan name text. */
	static final String WIDGET_PLAN_TEXTSIZE = "widget_plan_textsize_";
	/** Size of the statistics text. */
	static final String WIDGET_STATS_TEXTSIZE = "widget_stats_textsize_";
	/** Widget's text color. */
	static final String WIDGET_TEXTCOLOR = "widget_textcolor_";
	/** Widget's background color. */
	static final String WIDGET_BGCOLOR = "widget_bgcolor_";
	/** Widget's icon. */
	static final String WIDGET_ICON = "widget_icon_";
	/** Must widget be small? */
	static final String WIDGET_SMALL = "widget_small_";

	/** Width of the widget. */
	private static final int WIDGET_WIDTH = 100;
	/** Height of progress bars. */
	private static final int PB_HEIGHT = 4;
	/** Round corners. */
	private static final float WIDGET_RCORNER = 10f;
	/** Red color for progress bars. */
	private static final int PB_COLOR_GREY = 0xD0D0D0D0;
	/** Red light color for progress bars. */
	private static final int PB_COLOR_LGREY = 0x50D0D0D0;
	/** Red color for progress bars. */
	private static final int PB_COLOR_RED = 0xFFFF0000;
	/** Yellow color for progress bars. */
	private static final int PB_COLOR_YELLOW = 0xFFFFD000;
	/** Green color for progress bars. */
	private static final int PB_COLOR_GREEN = 0xFF00FF00;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		Log.d(TAG, "onUpdate()");
		Utils.setLocale(context);
		// Update logs and run rule matcher
		LogRunnerService.update(context, LogRunnerService.ACTION_RUN_MATCHER);

		final int count = appWidgetIds.length;

		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		for (int i = 0; i < count; i++) {
			updateWidget(context, appWidgetManager, appWidgetIds[i]);
		}
	}

	/**
	 * Update all running widgets.
	 * 
	 * @param context
	 *            {@link Context}
	 */
	public static void updateWidgets(final Context context) {
		Log.d(TAG, "updateWidgets()");
		final AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		final int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(new ComponentName(context,
						StatsAppWidgetProvider.class));
		final int count = appWidgetIds.length;
		for (int i = 0; i < count; i++) {
			updateWidget(context, appWidgetManager, appWidgetIds[i]);
		}
	}

	/**
	 * Update a single widget.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param appWidgetManager
	 *            {@link AppWidgetManager}
	 * @param appWidgetId
	 *            id of widget
	 */
	static void updateWidget(final Context context,
			final AppWidgetManager appWidgetManager, final int appWidgetId) {
		Log.d(TAG, "updateWidget(" + appWidgetId + ")");
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		final long pid = p.getLong(WIDGET_PLANID + appWidgetId, -1L);
		final boolean hideName = p.getBoolean(WIDGET_HIDETNAME + appWidgetId,
				false);
		final boolean showShortname = p.getBoolean(WIDGET_SHORTNAME
				+ appWidgetId, false);
		final boolean showCost = p.getBoolean(WIDGET_COST + appWidgetId, false);
		final boolean showBillPeriod = p.getBoolean(WIDGET_BILLPERIOD
				+ appWidgetId, false);
		final boolean showIcon = p.getBoolean(WIDGET_ICON + appWidgetId, false);
		final boolean smallWidget = p.getBoolean(WIDGET_SMALL + appWidgetId,
				false);
		final Float statsTextSize = p.getFloat(WIDGET_STATS_TEXTSIZE
				+ appWidgetId, StatsAppWidgetConfigure.DEFAULT_TEXTSIZE);
		final Float planTextSize = p.getFloat(WIDGET_PLAN_TEXTSIZE
				+ appWidgetId, StatsAppWidgetConfigure.DEFAULT_TEXTSIZE);
		final int textColor = p.getInt(WIDGET_TEXTCOLOR + appWidgetId,
				StatsAppWidgetConfigure.DEFAULT_TEXTCOLOR);
		final int bgColor = p.getInt(WIDGET_BGCOLOR + appWidgetId,
				StatsAppWidgetConfigure.DEFAULT_BGCOLOR);
		Log.d(TAG, "planid: " + pid);
		final ContentResolver cr = context.getContentResolver();

		Plan plan = Plan.getPlan(cr, pid, -1);
		if (plan == null) {
			return;
		}

		Log.d(TAG, "plan: " + plan.id);
		Log.d(TAG, "plan name: " + plan.name);
		Log.d(TAG, "count: " + plan.bpCount);
		Log.d(TAG, "cost: " + plan.cost);
		Log.d(TAG, "billedAmount: " + plan.bpBa);

		int bpos = (int) (plan.getBillPlanUsage() * CallMeter.HUNDRET);
		int bmax;
		if (!showBillPeriod || bpos < 0) {
			bpos = 0;
			bmax = -1;
		} else if (bpos == 0) {
			bpos = 0;
			bmax = 0;
		} else {
			bmax = CallMeter.HUNDRET;
		}
		Log.d(TAG, "bpos/bmax: " + bpos + "/" + bmax);

		String stats = Plans.formatValues(context, -1, plan.type, plan.bpCount,
				plan.bpBa, plan.billperiod, plan.billday);
		if (plan.limit > 0) {
			stats += "\n" + ((int) plan.usage) + "%";
		}
		if (showCost && plan.cost > 0F) {
			stats += "\n"
					+ String.format(Preferences.getCurrencyFormat(context),
							plan.cost);
		}

		Log.d(TAG, "limit: " + plan.limit);
		Log.d(TAG, "used: " + plan.usage);
		Log.d(TAG, "stats: " + stats);

		int widgetLayout = R.layout.stats_appwidget;
		if (smallWidget) {
			widgetLayout = R.layout.stats_appwidget_small;
		}
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				widgetLayout);
		views.setImageViewBitmap(
				R.id.widget_bg,
				getBackground(bgColor, bmax, bpos, plan.limit,
						(long) (plan.usage * plan.limit / CallMeter.HUNDRET)));
		if (hideName) {
			views.setViewVisibility(R.id.plan, View.GONE);
		} else {
			if (showShortname) {
				views.setTextViewText(R.id.plan, plan.sname);
			} else {
				views.setTextViewText(R.id.plan, plan.name);
			}
			views.setViewVisibility(R.id.plan, View.VISIBLE);
		}
		views.setTextViewText(R.id.stats, stats);
		views.setFloat(R.id.plan, "setTextSize", planTextSize);
		views.setFloat(R.id.stats, "setTextSize", statsTextSize);
		views.setTextColor(R.id.plan, textColor);
		views.setTextColor(R.id.stats, textColor);
		views.setOnClickPendingIntent(R.id.widget, PendingIntent.getActivity(
				context, 0, new Intent(context, Plans.class), 0));
		if (showIcon) {
			views.setViewVisibility(R.id.widget_icon, // .
					android.view.View.VISIBLE);
			switch (plan.type) {
			case DataProvider.TYPE_DATA:
				views.setImageViewResource(R.id.widget_icon, R.drawable.data);
				break;
			case DataProvider.TYPE_CALL:
				views.setImageViewResource(R.id.widget_icon, R.drawable.phone);
				break;
			case DataProvider.TYPE_SMS:
			case DataProvider.TYPE_MMS:
				views.setImageViewResource(R.id.widget_icon, // .
						R.drawable.message);
				break;
			case DataProvider.TYPE_MIXED:
				views.setImageViewResource(R.id.widget_icon, R.drawable.phone);
				break;
			default:
				views.setViewVisibility(R.id.widget_icon,
						android.view.View.GONE);
				break;
			}
		}

		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	/**
	 * Get {@link Bitmap} for background.
	 * 
	 * @param bgColor
	 *            background color
	 * @param bmax
	 *            max position of bill period ProgressBar
	 * @param bpos
	 *            position of bill period ProgressBar
	 * @param limit
	 *            limit
	 * @param used
	 *            usage
	 * @return {@link Bitmap}
	 */
	private static Bitmap getBackground(final int bgColor, final int bmax,
			final int bpos, final long limit, final long used) {
		final Bitmap bitmap = Bitmap.createBitmap(WIDGET_WIDTH, WIDGET_WIDTH,
				Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(bgColor);
		final RectF base = new RectF(0f, 0f, WIDGET_WIDTH, WIDGET_WIDTH);
		canvas.drawRoundRect(base, WIDGET_RCORNER, WIDGET_RCORNER, paint);
		if (bmax > 0) {
			// paint progress bar background
			Bitmap bitmapPb = Bitmap.createBitmap(WIDGET_WIDTH, WIDGET_WIDTH,
					Bitmap.Config.ARGB_8888);
			Canvas canvasPb = new Canvas(bitmapPb);
			final Paint paintPb = new Paint();
			paintPb.setAntiAlias(true);
			paintPb.setStyle(Paint.Style.FILL);
			paintPb.setColor(PB_COLOR_LGREY);
			canvasPb.drawRoundRect(base, WIDGET_RCORNER, WIDGET_RCORNER,
					paintPb);
			Rect copy = new Rect(0, 0, WIDGET_WIDTH, PB_HEIGHT);
			canvas.drawBitmap(bitmapPb, copy, copy, null);
			// paint progress bar
			bitmapPb = Bitmap.createBitmap(WIDGET_WIDTH, WIDGET_WIDTH,
					Bitmap.Config.ARGB_8888);
			canvasPb = new Canvas(bitmapPb);
			paintPb.setColor(PB_COLOR_GREY);
			canvasPb.drawRoundRect(base, WIDGET_RCORNER, WIDGET_RCORNER,
					paintPb);
			copy = new Rect(0, 0, (WIDGET_WIDTH * bpos) / bmax, PB_HEIGHT);
			canvas.drawBitmap(bitmapPb, copy, copy, null);
		}
		if (limit > 0L) {
			// paint progress bar background
			Bitmap bitmapPb = Bitmap.createBitmap(WIDGET_WIDTH, WIDGET_WIDTH,
					Bitmap.Config.ARGB_8888);
			Canvas canvasPb = new Canvas(bitmapPb);
			final Paint paintPb = new Paint();
			paintPb.setAntiAlias(true);
			paintPb.setStyle(Paint.Style.FILL);
			paintPb.setColor(PB_COLOR_LGREY);
			canvasPb.drawRoundRect(base, WIDGET_RCORNER, WIDGET_RCORNER,
					paintPb);
			Rect copy = new Rect(0, WIDGET_WIDTH - PB_HEIGHT, WIDGET_WIDTH,
					WIDGET_WIDTH);
			canvas.drawBitmap(bitmapPb, copy, copy, null);
			// paint progress bar
			bitmapPb = Bitmap.createBitmap(WIDGET_WIDTH, WIDGET_WIDTH,
					Bitmap.Config.ARGB_8888);
			canvasPb = new Canvas(bitmapPb);
			int u = (int) ((used * CallMeter.HUNDRET) / limit);
			if (u > CallMeter.HUNDRET) {
				paintPb.setColor(PB_COLOR_RED);
			} else if (bmax < 0 && u >= CallMeter.EIGHTY || bmax > 0
					&& (float) used / limit > (float) bpos / bmax) {
				paintPb.setColor(PB_COLOR_YELLOW);
			} else {
				paintPb.setColor(PB_COLOR_GREEN);
			}
			if (u > CallMeter.HUNDRET) {
				u = CallMeter.HUNDRET;
			}
			canvasPb.drawRoundRect(base, WIDGET_RCORNER, WIDGET_RCORNER,
					paintPb);
			copy = new Rect(0, WIDGET_WIDTH - PB_HEIGHT, (WIDGET_WIDTH * u)
					/ CallMeter.HUNDRET, WIDGET_WIDTH);
			canvas.drawBitmap(bitmapPb, copy, copy, null);
		}
		return bitmap;
	}
}
