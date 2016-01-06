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
package com.github.umeshkrpatel.LogMeter.prefs;


import android.app.AlertDialog.Builder;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;

import com.github.umeshkrpatel.LogMeter.R;
import com.github.umeshkrpatel.LogMeter.data.DataProvider;
import com.github.umeshkrpatel.LogMeter.data.RuleMatcher;
import com.github.umeshkrpatel.LogMeter.prefs.UpDownPreference.OnUpDownClickListener;
import de.ub0r.android.lib.Utils;

/**
 * Activity for setting rules.
 *
 * @author flx
 */
public final class Rules extends PreferenceActivity
        implements OnPreferenceClickListener,
        OnUpDownClickListener {
    /** Tag for output. */
    // private static final String TAG = "pr";

    /**
     * Item menu: edit.
     */
    private static final int WHICH_EDIT = 0;

    /**
     * Item menu: delete.
     */
    private static final int WHICH_DELETE = 1;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setLocale(this);
        addPreferencesFromResource(R.xml.group_prefs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    /**
     * Reload rules from ContentProvider.
     */
    @SuppressWarnings("deprecation")
    private void reload() {
        PreferenceScreen ps = (PreferenceScreen) findPreference("container");
        ps.removeAll();
        Cursor c = getContentResolver().query(DataProvider.Rules.CONTENT_URI,
                DataProvider.Rules.PROJECTION, null, null, null);
        if (c == null)
            return;

        if (c.moveToFirst()) {
            String[] types = getResources().getStringArray(R.array.rules_type);
            do {
                UpDownPreference p = new UpDownPreference(this, this);
                p.setKey("group_" + c.getInt(DataProvider.Rules.INDEX_ID));
                p.setTitle(c.getString(DataProvider.Rules.INDEX_NAME));

                String hint = "";
                final int t = c.getInt(DataProvider.Rules.INDEX_WHAT);
                if (t >= 0 && t < types.length) {
                    hint += types[t];
                } else {
                    hint += "???";
                }
                int i = c.getInt(DataProvider.Rules.INDEX_LIMIT_NOT_REACHED);
                if (i == 1) {
                    hint += " & " + getString(R.string.limitnotreached_);
                }
                i = c.getInt(DataProvider.Rules.INDEX_DIRECTION);
                if (i >= 0 && i < DataProvider.Rules.NO_MATTER) {
                    String[] strs;
                    final Resources r = getResources();
                    if (t == DataProvider.TYPE_SMS) {
                        strs = r.getStringArray(R.array.direction_sms);
                    } else if (t == DataProvider.TYPE_MMS) {
                        strs = r.getStringArray(R.array.direction_mms);
                    } else if (t == DataProvider.TYPE_DATA) {
                        strs = r.getStringArray(R.array.direction_data);
                    } else {
                        strs = r.getStringArray(R.array.direction_calls);
                    }
                    hint += " & " + strs[i];
                }
                i = c.getInt(DataProvider.Rules.INDEX_ROAMED);
                if (i == 0) {
                    hint += " & " + getString(R.string.roamed_);
                } else if (i == 1) {
                    hint += " & \u00AC " + getString(R.string.roamed_);
                }
                String s = c.getString(DataProvider.Rules.INDEX_INHOURS_ID);
                if (s != null && !s.equals("-1")) {
                    hint += " & " + getString(R.string.hourgroup_);
                }
                s = c.getString(DataProvider.Rules.INDEX_EXHOURS_ID);
                if (s != null && !s.equals("-1")) {
                    hint += " & " + getString(R.string.exhourgroup_);
                }
                s = c.getString(DataProvider.Rules.INDEX_INNUMBERS_ID);
                if (s != null && !s.equals("-1")) {
                    hint += " & " + getString(R.string.numbergroup_);
                }
                s = c.getString(DataProvider.Rules.INDEX_EXNUMBERS_ID);
                if (s != null && !s.equals("-1")) {
                    hint += " & " + getString(R.string.exnumbergroup_);
                }
                p.setSummary(hint);

                p.setOnPreferenceClickListener(this);
                ps.addPreference(p);
            } while (c.moveToNext());
        }
        c.close();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add:
                Preferences.setDefaultPlan(this, false);
                final ContentValues cv = new ContentValues();
                cv.put(DataProvider.Rules.NAME, getString(R.string.rules_new));
                final Uri uri = getContentResolver().insert(DataProvider.Rules.CONTENT_URI, cv);
                final Intent intent = new Intent(this, RuleEdit.class);
                intent.setData(uri);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    /**
     * Move an item.
     *
     * @param u        item's {@link Uri}
     * @param diretion +1/-1
     */
    private void move(final Uri u, final int diretion) {
        Cursor c0 = getContentResolver().query(u, DataProvider.Rules.PROJECTION, null, null,
                null);
        if (c0 == null)
            return;

        if (c0.moveToFirst()) {
            int o0;
            if (c0.isNull(DataProvider.Rules.INDEX_ORDER)) {
                o0 = c0.getInt(DataProvider.Rules.INDEX_ID);
            } else {
                o0 = c0.getInt(DataProvider.Rules.INDEX_ORDER);
            }
            String w;
            String o;
            if (diretion < 0) {
                w = DataProvider.Rules.ORDER + "<? or (" + DataProvider.Rules.ORDER
                        + " is null and " + DataProvider.Rules.ID + "<?)";
                o = DataProvider.Rules.REVERSE_ORDER;
            } else {
                w = DataProvider.Rules.ORDER + ">? or (" + DataProvider.Rules.ORDER
                        + " is null and " + DataProvider.Rules.ID + ">?)";
                o = DataProvider.Rules.DEFAULT_ORDER;
            }
            String s0 = String.valueOf(o0);
            Cursor c1 = getContentResolver().query(DataProvider.Rules.CONTENT_URI,
                    DataProvider.Rules.PROJECTION, w, new String[]{s0, s0}, o);
            if (c1 == null) {
                c0.close();
                return;
            }

            if (c1.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(DataProvider.Rules.ORDER, o0);
                getContentResolver().update(
                        ContentUris.withAppendedId(DataProvider.Rules.CONTENT_URI,
                                c1.getInt(DataProvider.Rules.INDEX_ID)), values, null, null);
            }
            c1.close();

            ContentValues values = new ContentValues();
            values.put(DataProvider.Rules.ORDER, o0 + diretion);
            getContentResolver().update(u, values, null, null);

            reload();
        }
        c0.close();
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        String k = preference.getKey();
        if (k != null && k.startsWith("group_")) {
            final int id = Integer.parseInt(k.substring("group_".length()));
            final Uri uri = ContentUris.withAppendedId(DataProvider.Rules.CONTENT_URI, id);
            final Builder builder = new Builder(this);
            builder.setItems(R.array.dialog_edit_delete,
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            switch (which) {
                                case WHICH_EDIT:
                                    final Intent intent = new Intent(Rules.this, RuleEdit.class);
                                    intent.setData(uri);
                                    Rules.this.startActivity(intent);
                                    break;
                                case WHICH_DELETE:
                                    Builder b = new Builder(Rules.this);
                                    b.setTitle(R.string.delete_);
                                    b.setMessage(R.string.delete_rule_hint);
                                    b.setNegativeButton(android.R.string.no, null);
                                    b.setPositiveButton(android.R.string.yes,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog,
                                                                    final int which) {
                                                    Rules.this
                                                            .getContentResolver()
                                                            .delete(ContentUris.withAppendedId(
                                                                            DataProvider.Rules.CONTENT_URI,
                                                                            id),
                                                                    null, null);
                                                    Rules.this.reload();
                                                    Preferences.setDefaultPlan(Rules.this, false);
                                                    RuleMatcher.unmatch(Rules.this);
                                                }
                                            });
                                    b.show();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
            return true;
        }
        return false;
    }

    @Override
    public void onUpDownClick(final Preference preference, final int direction) {
        String k = preference.getKey();
        int id = Integer.parseInt(k.substring("group_".length()));
        Uri uri = ContentUris.withAppendedId(DataProvider.Rules.CONTENT_URI, id);
        move(uri, direction);
    }
}
