package lb.themike10452.hellscorekernelmanagerl.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lb.themike10452.hellscorekernelmanagerl.CustomAdapters.BatteryInfoAdapter;
import lb.themike10452.hellscorekernelmanagerl.CustomAdapters.TimeInStateAdapter;
import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.ChartView;
import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.CpuFreqLiveView;
import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.GridLayout;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.CORE_MAX;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHARED_PREFS_ID;
import static lb.themike10452.hellscorekernelmanagerl.utils.Library.DROP_CACHES;
import static lb.themike10452.hellscorekernelmanagerl.utils.Library.MON_CPU_FREQ;
import static lb.themike10452.hellscorekernelmanagerl.utils.Library.MON_CPU_TEMP;

/**
 * Created by Mike on 5/7/2015.
 */
public class Monitoring extends Fragment implements View.OnClickListener {
    private static Monitoring instance;

    private static Activity mActivity;
    private static BatteryInfoAdapter mBatteryAdapter;
    private static List<CpuFreqLiveView> cpuFreqLiveViews;
    private static SharedPreferences sharedPreferences;
    private static TimeInStateAdapter stateAdapter;
    private static View mView;

    private static int LOOP_KEY;

    private final Handler mHandler;

    private int[] available_freqs;
    private int[] colors;
    private int[] legend;
    private int[] portions;

    public static Monitoring getInstance() {
        return instance != null ? instance : new Monitoring();
    }

    public Monitoring() {
        instance = this;
        mHandler = new Handler();
        cpuFreqLiveViews = new ArrayList<>();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        sharedPreferences = mActivity.getSharedPreferences(SHARED_PREFS_ID, Context.MODE_PRIVATE);
        colors = new int[]{getResources().getColor(R.color.blue_light), getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary)};
        legend = new int[]{R.string.legend_used_memory, R.string.legend_cached_memory, R.string.legend_free_memory};
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_monitoring, container, false);
        mBatteryAdapter = new BatteryInfoAdapter(mActivity, (ViewGroup) findViewById(R.id.batteryChargeView));
        stateAdapter = new TimeInStateAdapter(mActivity, (ViewGroup) findViewById(R.id.timeInStateView));
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(false);
        findViewById(R.id.dropCachesBtn).setOnClickListener(this);
        final GridLayout grid = (GridLayout) findViewById(R.id.cpuFreqLiveView);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                cpuFreqLiveViews.clear();
                grid.removeAllViews();
            }

            @Override
            protected Void doInBackground(Void... params) {
                final int coreMax = sharedPreferences.getInt(CORE_MAX, 3);
                fetchFrequencies();
                if (available_freqs != null) {
                    for (int i = 0; i <= coreMax; i++) {
                        final CpuFreqLiveView view = new CpuFreqLiveView(mActivity);
                        view.brief(available_freqs[available_freqs.length - 1], i, String.format(MON_CPU_FREQ, i));
                        cpuFreqLiveViews.add(view);
                        grid.post(new Runnable() {
                            @Override
                            public void run() {
                                grid.addView(view);
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                refresh(-1);
            }
        }.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        loop(LOOP_KEY);
    }

    @Override
    public void onPause() {
        super.onPause();
        LOOP_KEY++;
    }

    public void refresh(int loopKey) {
        if (available_freqs == null) {
            fetchFrequencies();
        }

        updateChartView();
        updateCpuView();
        mBatteryAdapter.update();
        stateAdapter.update();

        if (loopKey == LOOP_KEY) {
            loop(loopKey);
        }
    }

    private void loop(final int loopKey) {
        if (loopKey == LOOP_KEY) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isVisible() && loopKey == LOOP_KEY) {
                        refresh(loopKey);
                    }
                }
            }, 1000);
        }
    }

    private void updateChartView() {
        final List<String> free = HKMTools.getInstance().readFromFile(Library.MON_MEM);
        if (free != null) {
            List<Integer> values = new ArrayList<>();
            for (String row : free) {
                if (row.startsWith("MemTotal:")) {
                    values.add(extractInteger(row.split(":")[1]));
                } else if (row.startsWith("MemFree:")) {
                    values.add(extractInteger(row.split(":")[1]));
                } else if (row.startsWith("Cached:")) {
                    values.add(extractInteger(row.split(":")[1]));
                    break;
                }
            }
            if (values.size() == 3) {
                portions = new int[]{values.get(0) - values.get(1) - values.get(2), values.get(2), values.get(1)};
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        ((ChartView) findViewById(R.id.chart)).update(portions, colors, legend, "kb", (LinearLayout) findViewById(R.id.chartLegend));
                    }
                });
            }
            values.clear();
        }
    }

    private void updateCpuView() {
        String str = HKMTools.getInstance().readLineFromFile(MON_CPU_TEMP);
        ((TextView) findViewById(R.id.cpuTempTv)).setText(getString(R.string.legend_temp) + String.format(": %s %sC", str, (char) 0x00B0));
        for (CpuFreqLiveView view : cpuFreqLiveViews) {
            view.update();
        }
    }

    private Integer extractInteger(String str) {
        Integer integer = null;
        if (str != null) {
            char[] chars = str.toCharArray();
            String intStr = "";
            int i;
            for (i = 0; i < str.length(); i++) {
                if (Character.isDigit(chars[i])) {
                    break;
                }
            }
            while (Character.isDigit(chars[i])) {
                intStr += chars[i];
                i++;
            }
            if (intStr.length() > 0) {
                integer = Integer.parseInt(intStr);
            }
        }
        return integer;
    }

    private void fetchFrequencies() {
        String tmp = HKMTools.getInstance().readLineFromFile(Library.CPU_AVAIL_FREQS);
        if (tmp != null) {
            String[] freqs = tmp.split(" ");
            available_freqs = new int[freqs.length];
            try {
                for (int i = 0; i < freqs.length; i++) {
                    available_freqs[i] = Integer.parseInt(freqs[i]);
                }
            } catch (NumberFormatException e) {
                available_freqs = null;
            }
        }
    }

    private View findViewById(int resId) {
        return mView.findViewById(resId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dropCachesBtn:
                HKMTools.getInstance().run("echo 3 > " + DROP_CACHES);
                break;
        }
    }
}
