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

package de.ub0r.android.callmeter.data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import de.ub0r.android.logg0r.Log;

/**
 * Acces /sys/class/net/ files.
 */
public final class SysClassNet {

    /**
     * Prefix of all interfaces.
     */
    public static final String SYS_CLASS_NET = "/sys/class/net/";
    /**
     * type postfix.
     */
    public static final String TYPE = "/type";
    /**
     * carrier postfix.
     */
    public static final String CARRIER = "/carrier";
    /**
     * Postfix: received bytes.
     */
    public static final String RX_BYTES = "/statistics/rx_bytes";
    /**
     * Postfix: sent bytes.
     */
    public static final String TX_BYTES = "/statistics/tx_bytes";
    /**
     * Tag for output.
     */
    private static final String TAG = "SysClassNet";

    /**
     * Private constructor. This is an utility class.
     */
    private SysClassNet() {
    }

    /**
     * @param inter interface
     * @return bytes received
     * @throws IOException IOException
     */
    public static long getRxBytes(final String inter) throws IOException {
        return readLong(inter, RX_BYTES);
    }

    /**
     * @param inter interface
     * @return bytes sent
     * @throws IOException IOException
     */
    public static long getTxBytes(final String inter) throws IOException {
        return readLong(inter, TX_BYTES);
    }

    /**
     * @param filename filename
     * @return RandomAccessFile
     * @throws IOException IOException
     */
    private static RandomAccessFile getFile(final String filename) throws IOException {
        File f = new File(filename);
        return new RandomAccessFile(f, "r");
    }

    /**
     * @param inter interface
     * @param file  file (rx or tx)
     * @return bytes received or sent
     */
    private static long readLong(final String inter, final String file) {
        Log.d(TAG, "readLong(", inter, ",", file, ")");
        StringBuilder sb = new StringBuilder();
        sb.append(SYS_CLASS_NET).append(inter).append(file);
        RandomAccessFile raf = null;
        try {
            raf = getFile(sb.toString());
            String l = raf.readLine();
            Log.d(TAG, "readLong(): ", l);
            return Long.valueOf(l);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + " / error reading long for inter: " + inter);
            return 0;
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    Log.e(TAG, null, e);
                }
            }
        }
    }
}
