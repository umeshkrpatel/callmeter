/*
 * Copyright (C) 2009-2013 Felix Bechstein
 * 
 * This file is part of CallMeter 3G.
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
package com.umeshkrpatel.logmonitor.ui;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import de.ub0r.android.callmeter.BuildConfig;
import de.ub0r.android.callmeter.R;
import de.ub0r.android.lib.Utils;

/**
 * Display About Activity.
 *
 * @author flx
 */
public final class AboutActivity extends AppCompatActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        Utils.setLocale(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about);
        setTitle(getString(R.string.about_) + " " + getString(R.string.app_name));
        getSupportActionBar().setSubtitle("v" + BuildConfig.VERSION_NAME);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
