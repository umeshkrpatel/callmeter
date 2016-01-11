package com.github.umeshkrpatel.LogMeter.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.github.umeshkrpatel.LogMeter.LogMeter;
import com.github.umeshkrpatel.LogMeter.R;
import com.github.umeshkrpatel.LogMeter.data.DataProvider;
import com.github.umeshkrpatel.LogMeter.data.LogRunnerReceiver;
import com.github.umeshkrpatel.LogMeter.data.LogRunnerService;
import com.github.umeshkrpatel.LogMeter.ui.prefs.Preferences;
import com.viewpagerindicator.TitlePageIndicator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import de.ub0r.android.lib.Utils;
import de.ub0r.android.logg0r.Log;

/**
 * Callmeter's Main Activity
 *
 * @author flx
 */
public final class UtilityActivity
        extends AppCompatActivity
        implements OnPageChangeListener,
            DetailFragment.OnFragmentInteractionListener,
            LogsFragment.OnFragmentInteractionListener {

    /**
     * {@link Message} for {@link Handler}: start background: LogMatcher.
     */
    public static final int MSG_BACKGROUND_START_MATCHER = 1;
    /**
     * {@link Message} for {@link Handler}: stop background: LogMatcher.
     */
    public static final int MSG_BACKGROUND_STOP_MATCHER = 2;
    /**
     * {@link Message} for {@link Handler}: start background: LogRunner.
     */
    public static final int MSG_BACKGROUND_START_RUNNER = 3;
    /**
     * {@link Message} for {@link Handler}: stop background: LogRunner.
     */
    public static final int MSG_BACKGROUND_STOP_RUNNER = 4;
    /**
     * {@link Message} for {@link Handler}: progress: LogMatcher.
     */
    public static final int MSG_BACKGROUND_PROGRESS_MATCHER = 5;
    /**
     * Tag for output.
     */
    private static final String TAG = "UtilityActivity";
    private static final int PERMISSION_REQUEST_READ_CALL_LOG = 1;
    private static final int PERMISSION_REQUEST_READ_SMS = 2;
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 3;

    /**
     * Delay for LogRunnerService to run.
     */
    private static final long DELAY_LOGRUNNER = 1500;

    /**
     * {@link Handler} for outside.
     */
    private static Handler currentHandler = null;
    /**
     * {@link ViewPager}.
     */
    private ViewPager pager;
    /**
     * {@link PlansFragmentAdapter}.
     */
    private PlansFragmentAdapter fadapter;
    /**
     * Number of background tasks.
     */
    private int progressCount = 0;

    @Override
    public String onDetailFragmentInteraction() {
        return "";
    }

    @Override
    public void onLogFragmentInteraction(String number) {
        showDetailFragment(number);
    }

    /**
     * {@link Handler} for handling messages from background process.
     */
    private static class MessageHandler extends Handler {
        /** LogRunner running in background? */
        private boolean inProgressRunner = false;

        /** {@link ProgressDialog} showing LogMatcher's status. */
        private ProgressDialog statusMatcher = null;

        /** Is statusMatcher a {@link ProgressDialog}? */
        private boolean statusMatcherProgress = false;

        /** String for recalculate message. */
        private String recalc = null;

        private WeakReference<UtilityActivity> mActivity;
        MessageHandler(UtilityActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public synchronized void handleMessage(final Message msg) {
            Log.d(TAG, "handleMessage(", msg.what, ")");

            UtilityActivity activity = mActivity.get();
            if (activity == null || activity.getSupportActionBar() == null)
                return;

            switch (msg.what) {
                case MSG_BACKGROUND_START_RUNNER:
                    inProgressRunner = true;
                case MSG_BACKGROUND_START_MATCHER:
                    statusMatcherProgress = false;
                    activity.setInProgress(1);
                    break;
                case MSG_BACKGROUND_STOP_RUNNER:
                    inProgressRunner = false;
                    activity.setInProgress(-1);
                    activity.getSupportActionBar().setSubtitle(null);
                    break;
                case MSG_BACKGROUND_STOP_MATCHER:
                    activity.setInProgress(-1);
                    activity.getSupportActionBar().setSubtitle(null);
                    Fragment f = activity.fadapter.getActiveFragment(activity.pager,
                            activity.fadapter.getHomeFragmentPos());
                    if (f != null && f instanceof PlansFragment) {
                        ((PlansFragment) f).reQuery(true);
                    }
                    break;
                case MSG_BACKGROUND_PROGRESS_MATCHER:
                    if (activity.progressCount == 0) {
                        activity.setProgress(1);
                    }
                    if (statusMatcher == null
                            || (!this.statusMatcherProgress || statusMatcher.isShowing())) {
                        Log.d(TAG, "matcher progress: ", msg.arg1);
                        if (statusMatcher == null || !this.statusMatcherProgress) {
                            final ProgressDialog dold = statusMatcher;
                            statusMatcher = new ProgressDialog(activity);
                            statusMatcher.setCancelable(true);
                            if (recalc == null) {
                                recalc = activity.getString(R.string.reset_data_progr2);
                            }
                            statusMatcher.setMessage(recalc);
                            statusMatcher.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            statusMatcher.setMax(msg.arg2);
                            statusMatcher.setIndeterminate(false);
                            statusMatcherProgress = true;
                            Log.d(TAG, "showing dialog..");
                            try {
                                statusMatcher.show();
                                if (dold != null) {
                                    dold.dismiss();
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "activity already finished?", e);
                            }
                        }
                        statusMatcher.setProgress(msg.arg1);
                    }
                    if (recalc == null) {
                        recalc = activity.getString(R.string.reset_data_progr2);
                    }
                    activity.getSupportActionBar().setSubtitle(
                            recalc + " " + msg.arg1 + "/" + msg.arg2);
                    break;
                default:
                    break;
            }

            if (inProgressRunner) {
                if (statusMatcher == null
                        || (msg.arg1 <= 0 && !this.statusMatcher.isShowing())) {
                    statusMatcher = new ProgressDialog(activity);
                    statusMatcher.setCancelable(true);
                    statusMatcher.setMessage(activity.getString(R.string.reset_data_progr1));
                    statusMatcher.setIndeterminate(true);
                    statusMatcherProgress = false;
                    try {
                        statusMatcher.show();
                    } catch (Exception e) {
                        Log.w(TAG, "activity already finished?", e);
                    }
                }
            } else {
                if (statusMatcher != null && statusMatcher.isShowing()) {
                    try {
                        statusMatcher.dismiss();
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "error dismissing dialog", e);
                    }
                    statusMatcher = null;
                }
            }
        }
    }

    /**
     * Get the current {@link Handler}.
     *
     * @return {@link Handler}.
     */
    public static Handler getHandler() {
        return currentHandler;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setTheme(Preferences.getTheme(this));
        Utils.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plans);
		assert getSupportActionBar()!=null;
        getSupportActionBar().setHomeButtonEnabled(true);

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        //noinspection ConstantConditions
        if (p.getAll().isEmpty()) {
            // set date of recordings to beginning of last month
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 0);
            c.add(Calendar.MONTH, -1);
            Log.i(TAG, "set date of recording: " + c);
            p.edit().putLong(Preferences.PREFS_DATE_BEGIN, c.getTimeInMillis()).apply();
        }

        pager = (ViewPager) findViewById(R.id.pager);
        initAdapter();
    }

    private void initAdapter() {
        // request permissions before doing any real work
        if (!LogMeter.requestPermission(this, Manifest.permission.READ_CALL_LOG,
                PERMISSION_REQUEST_READ_CALL_LOG, R.string.permissions_read_call_log,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })) {
            return;
        }
        if (!LogMeter.requestPermission(this, Manifest.permission.READ_SMS,
                PERMISSION_REQUEST_READ_SMS, R.string.permissions_read_sms,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })) {
            return;
        }

        // request semi optional permission
        LogMeter.requestPermission(this, Manifest.permission.READ_CONTACTS,
                PERMISSION_REQUEST_READ_CONTACTS, R.string.permissions_read_contacts, null);

        fadapter = new PlansFragmentAdapter(this, getSupportFragmentManager());
        pager.setAdapter(fadapter);
        pager.setCurrentItem(fadapter.getHomeFragmentPos());

        TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.titles);
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();
        Utils.setLocale(this);
        if (currentHandler==null) {
            currentHandler = new MessageHandler(this);
        }
        setInProgress(0);
        PlansFragment.reloadPreferences(this);
        runLogRunner();
        //mAdView.resume();
    }

    private void runLogRunner() {
        // schedule next update
        LogRunnerReceiver.schedNext(this, DELAY_LOGRUNNER, LogRunnerService.ACTION_RUN_MATCHER);
        LogRunnerReceiver.schedNext(this, LogRunnerService.ACTION_SHORT_RUN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onNewIntent(final Intent intent) {
        setIntent(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        currentHandler = null;
        //mAdView.pause();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode,
            @NonNull final String permissions[],
            @NonNull final int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_CALL_LOG:
            case PERMISSION_REQUEST_READ_SMS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // just try again.
                    initAdapter();
                    runLogRunner();
                } else {
                    // this app is useless without permission for reading sms
                    Log.e(TAG, "permission denied: " + requestCode + " , exit");
                    finish();
                }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        /* if (prefsNoAds) {
            menu.removeItem(R.id.item_donate);
        } */
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_settings:
                startActivity(new Intent(this, Preferences.class));
                return true;
            case R.id.item_logs:
                showLogsFragment(-1L);
                return true;
            case android.R.id.home:
                pager.setCurrentItem(fadapter.getHomeFragmentPos(), true);
                Fragment f = fadapter.getActiveFragment(pager,
                        fadapter.getLogsFragmentPos());
                if (f != null && f instanceof LogsFragment) {
                    ((LogsFragment) f).setPlanId(-1L);
                }
                Fragment f1 = fadapter.getActiveFragment(pager,
                        fadapter.getSummaryFragmentPos());
                if (f1 != null && f1 instanceof SummaryFragment) {
                    ((SummaryFragment) f1).setPlanId(-1L);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void showLogsFragment(final long planId) {
        int p = fadapter.getLogsFragmentPos();
        Fragment f = fadapter.getActiveFragment(pager, p);
        if (f != null && f instanceof LogsFragment) {
            ((LogsFragment) f).setPlanId(planId);
        }
        pager.setCurrentItem(p, true);
    }

    public void showDetailFragment(final String number) {
        int p = fadapter.getDetailFragmentPos();
        Fragment f = fadapter.getActiveFragment(pager, p);
        if (f != null && f instanceof DetailFragment) {
            ((DetailFragment) f).updateDetailFragment(number);
        }
        pager.setCurrentItem(p, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageScrolled(final int position, final float positionOffset,
                               final int positionOffsetPixels) {
        // nothing to do

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageSelected(final int position) {
        Log.d(TAG, "onPageSelected(", position, ")");
        if (position == fadapter.getLogsFragmentPos()) {
            Fragment f = fadapter.getActiveFragment(pager,
                    fadapter.getLogsFragmentPos());
            if (f != null && f instanceof LogsFragment) {
                ((LogsFragment) f).setAdapter(false);
            }
        } else if (position == fadapter.getSummaryFragmentPos()) {
            Fragment f = fadapter.getActiveFragment(pager,
                    fadapter.getSummaryFragmentPos());
            if (f != null && f instanceof SummaryFragment) {
                ((SummaryFragment) f).setAdapter(false);
            }
        } else {
            Fragment f = fadapter.getActiveFragment(pager, position);
            if (f != null && f instanceof PlansFragment) {
                ((PlansFragment) f).reQuery(false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageScrollStateChanged(final int state) {
        // nothing to do
    }

    /**
     * Set progress indicator.
     *
     * @param add add number of running tasks
     */
    public synchronized void setInProgress(final int add) {
        Log.d(TAG, "setInProgress(", add, ")");
        progressCount += add;

        if (progressCount < 0) {
            Log.w(TAG, "this.progressCount: " + progressCount);
            progressCount = 0;
        }
    }

    /**
     * Show all {@link PlansFragment}s.
     *
     * @author flx
     */
    private static class PlansFragmentAdapter extends FragmentPagerAdapter {

        /**
         * {@link FragmentManager} .
         */
        private final FragmentManager mFragmentManager;

        /**
         * List of positions.
         */
        private final Long[] positions;

        /**
         * List of bill days.
         */
        private final Long[] billDays;

        /**
         * List of titles.
         */
        private final String[] titles;

        /**
         * {@link Context}.
         */
        private final Context ctx;

        /**
         * Default constructor.
         *
         * @param context {@link Context}
         * @param fm      {@link FragmentManager}
         */
        public PlansFragmentAdapter(final Context context, final FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
            ctx = context;
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(DataProvider.Logs.CONTENT_URI,
                    new String[]{DataProvider.Logs.DATE}, null, null, DataProvider.Logs.DATE
                            + " ASC LIMIT 1");
            if (c == null || !c.moveToFirst()) {
                positions = new Long[]{-1L, -1L, -1L, -1L};
                billDays = positions;
                if (c != null && !c.isClosed()) {
                    c.close();
                }
            } else {
                final long minDate = c.getLong(0);
                c.close();
                c = cr.query(
                        DataProvider.Plans.CONTENT_URI,
                        DataProvider.Plans.PROJECTION,
                        DataProvider.Plans.TYPE + "=? and " + DataProvider.Plans.BILLPERIOD + "!=?",
                        new String[]{String.valueOf(DataProvider.TYPE_BILLPERIOD),
                                String.valueOf(DataProvider.BILLPERIOD_INFINITE)},
                        DataProvider.Plans.ORDER + " LIMIT 1");
                if (minDate < 0L || c == null || !c.moveToFirst()) {
                    positions = new Long[]{-1L, -1L, -1L, -1L};
                    billDays = positions;
                    if (c != null) {
                        c.close();
                    }
                } else {
                    ArrayList<Long> list = new ArrayList<>();
                    int bptype = c.getInt(DataProvider.Plans.INDEX_BILLPERIOD);
                    ArrayList<Long> bps = DataProvider.Plans.getBillDays(bptype,
                            c.getLong(DataProvider.Plans.INDEX_BILLDAY), minDate, -1);
                    if (bps != null) {
                        Log.d(TAG, "bill periods: ", bps.size());
                        if (!bps.isEmpty()) {
                            bps.remove(bps.size() - 1);
                            list.addAll(bps);
                        }
                    }
                    c.close();
                    list.add(-1L); // now
                    list.add(-1L); // summary
                    list.add(-1L); // logs
                    list.add(-1L); // detail
                    positions = list.toArray(new Long[list.size()]);
                    int l = positions.length;
                    Arrays.sort(positions, 0, l - 2);

                    billDays = new Long[l];
                    for (int i = 0; i < l - 1; i++) {
                        long pos = positions[i];
                        billDays[i] = DataProvider.Plans.getBillDay(bptype, pos + 1L, pos,
                                false).getTimeInMillis();
                    }
                }
            }
            Common.setDateFormat(context);
            final int l = positions.length;
            titles = new String[l];
            titles[l - 4] = context.getString(R.string.now);
            titles[l - 3] = context.getString(R.string.summary);
            titles[l - 2] = context.getString(R.string.logs);
            titles[l - 1] = context.getString(R.string.detail);
        }

        /**
         * Get a {@link Fragment}'s name.
         *
         * @param viewId container view
         * @param index  position
         * @return name of {@link Fragment}
         */
        private static String makeFragmentName(final int viewId, final int index) {
            // this might change in underlying method!
            return "android:switcher:" + viewId + ":" + index;
        }

        /**
         * Get an active fragment.
         *
         * @param container {@link ViewPager}
         * @param position  position in container
         * @return null if no fragment was initialized
         */
        public Fragment getActiveFragment(final ViewPager container, final int position) {
            String name = makeFragmentName(container.getId(), position);
            return mFragmentManager.findFragmentByTag(name);
        }

        @Override
        public int getCount() {
            return positions.length;
        }

        @Override
        public Fragment getItem(final int position) {
            if (position == getSummaryFragmentPos()) {
                return new SummaryFragment();
            } else if (position == getDetailFragmentPos()){
                return DetailFragment.newInstance("+918867630185");
            } else if (position == getLogsFragmentPos()) {
                return new LogsFragment();
            } else {
                return PlansFragment.newInstance(position, positions[position]);
            }
        }

        /**
         * Get position of home {@link Fragment}.
         *
         * @return position of home {@link Fragment}
         */
        public int getHomeFragmentPos() {
            return positions.length - 3;
        }

        /**
         * Get position of Logs {@link Fragment}.
         *
         * @return position of Logs {@link Fragment}
         */
        public int getSummaryFragmentPos() {
            return positions.length - 3;
        }
        public int getLogsFragmentPos() {
            return positions.length - 2;
        }
        public int getDetailFragmentPos() { return positions.length -1; }
        /**
         * {@inheritDoc}
         */
        @Override
        public CharSequence getPageTitle(final int position) {
            String ret;
            if (titles[position] == null) {
                ret = Common.formatDate(ctx, billDays[position]);
                titles[position] = ret;
            } else {
                ret = titles[position];
            }
            return ret;
        }
    }
}
