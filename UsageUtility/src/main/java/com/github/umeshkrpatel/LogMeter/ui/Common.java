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

import android.content.Context;

import java.text.DateFormat;
import java.util.Calendar;

import com.github.umeshkrpatel.LogMeter.utils.LogMeter;
import com.github.umeshkrpatel.LogMeter.data.DataProvider;
import com.github.umeshkrpatel.LogMeter.prefs.Preferences;

/**
 * Common UI methods.
 *
 * @author flx
 */
public final class Common {

    /**
     * Byte units.
     */
    private static final String BYTE_UNITS_B = "B";

    /**
     * Byte units: kB.
     */
    private static final String BYTE_UNITS_KB = "kB";

    /**
     * Byte units: MB.
     */
    private static final String BYTE_UNITS_MB = "MB";

    /**
     * Byte units: GB.
     */
    private static final String BYTE_UNITS_GB = "GB";

    /**
     * Byte units: TB.
     */
    private static final String BYTE_UNITS_TB = "TB";

    /**
     * Selected date format.
     */
    private static String dateFormat = null;

    /**
     * {@link DateFormat}.
     */
    private static DateFormat dateFormater = null;

    /**
     * Hide Constructor.
     */
    private Common() {

    }

    /**
     * Set {@link DateFormat}.
     *
     * @param format {@link DateFormat}
     */
    public static void setDateFormat(final String format) {
        dateFormat = format;
    }

    /**
     * Set {@link DateFormat} from Preferences.
     *
     * @param context {@link Context}
     */
    public static void setDateFormat(final Context context) {
        dateFormat = Preferences.getDateFormat(context);
    }

    /**
     * Format a {@link Calendar}.
     *
     * @param context {@link Context}
     * @param cal     {@link Calendar}
     * @return formatted date
     */
    public static String formatDate(final Context context, final Calendar cal) {
        if (dateFormat == null) {
            if (dateFormater == null) {
                dateFormater = android.text.format.DateFormat.getDateFormat(context);
            }
            return dateFormater.format(cal.getTime());
        } else {
            return String.format(dateFormat, cal, cal, cal);
        }
    }

    /**
     * Format a {@link Calendar}.
     *
     * @param context {@link Context}
     * @param date    time in milliseconds
     * @return formatted date
     */
    public static String formatDate(final Context context, final long date) {
        if (dateFormat == null) {
            if (dateFormater == null) {
                dateFormater = android.text.format.DateFormat.getDateFormat(context);
            }
            return dateFormater.format(date);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(date);
            return formatDate(context, cal);
        }
    }

    /**
     * Format amount regarding type of plan.
     *
     * @param pType     type of plan
     * @param amount    amount
     * @param showHours show hours and days
     * @return {@link String} representing amount
     */
    public static String formatAmount(final int pType, final float amount,
                                      final boolean showHours) {
        switch (pType) {
            case DataProvider.TYPE_DATA:
                return prettyBytes(amount);
            case DataProvider.TYPE_CALL:
                return prettySeconds(amount, showHours);
            case DataProvider.TYPE_MMS:
            case DataProvider.TYPE_SMS:
                return String.valueOf((long) amount);
            default:
                return String.format("%.2f", amount).replaceAll("[\\.,]?0*$", "");
        }
    }

    /**
     * Format bill periods start date.
     *
     * @param context    {@link Context}
     * @param billperiod bill period type
     * @param billDay    {@link Calendar} for bill periods bill day
     * @return formated date
     */
    public static String formatDate(final Context context, final int billperiod,
                                    final Calendar billDay) {
        if (billperiod == DataProvider.BILLPERIOD_INFINITE && billDay == null) {
            return "\u221E";
        } else {
            return formatDate(context, billDay);
        }
    }

    /**
     * Format count/amount along with type.
     *
     * @param context    {@link Context}
     * @param now        current time
     * @param pType      type of plan
     * @param count      count
     * @param amount     amount
     * @param billperiod bill period type
     * @param billday    bill period's bill day
     * @param showHours  show hours
     * @return string
     */
    public static String formatValues(final Context context, final long now, final int pType,
                                      final long count, final float amount, final int billperiod, final long billday,
                                      final boolean showHours) {
        switch (pType) {
            case DataProvider.TYPE_BILLPERIOD:
                Calendar billDay = Calendar.getInstance();
                billDay.setTimeInMillis(billday);
                return formatDate(context, billperiod, billDay);
            case DataProvider.TYPE_CALL:
                return formatAmount(pType, amount, showHours) + " (" + count + ")";
            case DataProvider.TYPE_DATA:
            case DataProvider.TYPE_SMS:
            case DataProvider.TYPE_MMS:
            case DataProvider.TYPE_MIXED:
                return formatAmount(pType, amount, showHours);
            default:
                return "";
        }
    }

    /**
     * Return pretty bytes.
     *
     * @param value bytes
     * @return pretty bytes
     * @author Cyril Jaquier, flx
     */
    public static String prettyBytes(final float value) {
        StringBuilder sb = new StringBuilder();
        if (value < LogMeter.kBytesPerKiloByte) {
            sb.append(String.format("%.1f", value));
            sb.append(BYTE_UNITS_B);
        } else if (value < LogMeter.kBytesPerMegaByte) {
            sb.append(String.format("%.1f", value / LogMeter.kBytesPerKiloByte));
            sb.append(BYTE_UNITS_KB);
        } else if (value < LogMeter.kBytesPerGigaByte) {
            sb.append(String.format("%.2f", value / LogMeter.kBytesPerMegaByte));
            sb.append(BYTE_UNITS_MB);
        } else if (value < LogMeter.kBytesPerTeraByte) {
            sb.append(String.format("%.3f", value / LogMeter.kBytesPerGigaByte));
            sb.append(BYTE_UNITS_GB);
        } else {
            sb.append(String.format("%.4f", value / LogMeter.kBytesPerTeraByte));
            sb.append(BYTE_UNITS_TB);
        }
        return sb.toString();
    }

    /**
     * Parse number of seconds to a readable time format.
     *
     * @param seconds   seconds
     * @param showHours show hours and days
     * @return parsed string
     */
    public static String prettySeconds(final float seconds, final boolean showHours) {
        String ret;
        final long ls = (long) seconds;
        long d, h, m;
        if (showHours) {
            d = ls / LogMeter.kSecondsPerDay;
            h = (ls % LogMeter.kSecondsPerDay) / LogMeter.kSecondsPerHour;
            m = (ls % LogMeter.kSecondsPerHour) / LogMeter.kSecondsPerMinute;
        } else {
            d = 0L;
            h = 0L;
            m = ls / LogMeter.kSecondsPerMinute;
        }
        final long s = ls % LogMeter.kSecondsPerMinute;
        if (d > 0L) {
            ret = d + "d ";
        } else {
            ret = "";
        }
        if (h > 0 || d > 0) {
            if (h < LogMeter.kTenth) {
                ret += "0";
            }
            ret += h + ":";
        }
        if (m > 0 || h > 0 || d > 0) {
            if (m < LogMeter.kTenth && h > 0) {
                ret += "0";
            }
            ret += m + ":";
        }
        if (s < LogMeter.kTenth && (m > 0 || h > 0 || d > 0)) {
            ret += "0";
        }
        ret += s;
        if (d == 0 && h == 0 && m == 0) {
            ret += "s";
        }
        return ret;
    }
}

