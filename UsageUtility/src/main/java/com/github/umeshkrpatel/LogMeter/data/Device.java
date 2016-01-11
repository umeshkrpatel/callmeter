/*
 * Copyright (C) 2009-2013 Cyril Jaquier, Felix Bechstein
 * 
 * This file is part of NetCounter.
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

package com.github.umeshkrpatel.LogMeter.data;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.TrafficStats;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import de.ub0r.android.logg0r.Log;

/**
 * Representation of a device.
 */
public abstract class Device {

    /**
     * Tag for output.
     */
    private static final String TAG = "Device";

    /**
     * Size of read buffer.
     */
    private static final int BUFSIZE = 8;

    /**
     * Single instance.
     */
    private static Device instance = null;

    /**
     * @return single instance
     */
    public static synchronized Device getDevice() {
        Log.d(TAG, "Device: ", Build.DEVICE);
        if (instance == null) {
            Log.d(TAG, "Device: ", Build.DEVICE);
            if (Build.PRODUCT.equals("sdk")) {
                instance = new EmulatorDevice();
            } else {
                instance = new TargetDevice();
            }
            Log.i(TAG, "Device: " + Build.DEVICE + "/ Interface: " + instance.getCell());
        }
        return instance;
    }

    /**
     * @return device's device file: cell
     */
    protected abstract String getCell();

    /**
     * @return received bytes on cell device
     * @throws IOException IOException
     */
    public abstract long getCellRxBytes() throws IOException;

    /**
     * @return transmitted bytes on cell device
     * @throws IOException IOException
     */
    public abstract long getCellTxBytes() throws IOException;

    /**
     * @return device's device file: wifi
     */
    protected abstract String getWiFi();

    /**
     * @return received bytes on wifi device
     * @throws IOException IOException
     */
    public abstract long getWiFiRxBytes() throws IOException;

    /**
     * @return transmitted bytes on wifi device
     * @throws IOException IOException
     */
    public abstract long getWiFiTxBytes() throws IOException;
}

/**
 * Emulator Device showing all traffic on cell and wifi.
 */
final class EmulatorDevice extends Device {

    /**
     * Tag for output.
     */
    private static final String TAG = "EmulatorDevice";

    /**
     * My cell interface.
     */
    private final String mCell = "eth0";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCell() {
        Log.d(TAG, "Cell interface: ", mCell);
        return mCell;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getWiFi() {
        Log.d(TAG, "WiFi interface: ", mCell);
        return "ethh0";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCellRxBytes() throws IOException {
        String dev = getCell();
        if (dev == null) {
            return 0L;
        }
        return SysClassNet.getRxBytes(dev);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCellTxBytes() throws IOException {
        String dev = getCell();
        if (dev == null) {
            return 0L;
        }
        return SysClassNet.getTxBytes(dev);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getWiFiRxBytes() throws IOException {
        String dev = getWiFi();
        if (dev == null) {
            return 0L;
        }
        return SysClassNet.getRxBytes(dev);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getWiFiTxBytes() throws IOException {
        return SysClassNet.getTxBytes(getWiFi());
    }
}

/**
 * Common Device for API>=8.
 */
@TargetApi(8)
final class TargetDevice extends Device {
    /** Tag for output. */
    // private static final String TAG = "TargetDevice";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCell() {
        return "TrafficStats";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCellRxBytes() throws IOException {
        final long l = TrafficStats.getMobileRxBytes();
        if (l < 0L) {
            return 0L;
        }
        return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCellTxBytes() throws IOException {
        final long l = TrafficStats.getMobileTxBytes();
        if (l < 0L) {
            return 0L;
        }
        return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getWiFi() {
        return "TrafficStats";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getWiFiRxBytes() throws IOException {
        final long l = TrafficStats.getMobileRxBytes();
        final long la = TrafficStats.getTotalRxBytes();
        if (la < 0L || la < l) {
            return 0L;
        }
        return la - l;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getWiFiTxBytes() throws IOException {
        final long l = TrafficStats.getMobileTxBytes();
        final long la = TrafficStats.getTotalTxBytes();
        if (la < 0L || la < l) {
            return 0L;
        }
        return la - l;
    }

    private class TraficRecord {
        long mIncoming = 0;
        long mOutgoing = 0;
        String appTag = null;
        TraficRecord() {
            mIncoming = TrafficStats.getTotalRxBytes();
            mOutgoing = TrafficStats.getTotalTxBytes();
        }
        TraficRecord(int uid, String tag) {
            mIncoming = TrafficStats.getUidRxBytes(uid);
            mOutgoing = TrafficStats.getUidTxBytes(uid);
            appTag = tag;
        }
    };

    private class TrafficMonitor {
        HashMap<Integer, TraficRecord> apps = new HashMap<>();
        TrafficMonitor(Context context) {
            for (ApplicationInfo app : context.getPackageManager().getInstalledApplications(0)) {
                apps.put(app.uid, new TraficRecord(app.uid, app.packageName));
            }
        }
    };
}
