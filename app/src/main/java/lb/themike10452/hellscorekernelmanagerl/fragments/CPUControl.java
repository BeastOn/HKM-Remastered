package lb.themike10452.hellscorekernelmanagerl.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.MainActivity;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.properties.HKMProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.MulticoreIntProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.MulticoreLongProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.StringProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.intProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.longProperty;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;
import lb.themike10452.hellscorekernelmanagerl.utils.Tools;

/**
 * Created by Mike on 2/22/2015.
 */
public class CPUControl extends Fragment implements HKMFragment {

    private static final String ERR_STR = "hkm_n_a";
    private static final int ERR_INT = -1;

    private static CPUControl instance;

    private Activity mActivity;
    private long[] available_freqs;

    private intProperty maxCoresProperty;
    private intProperty minCoresProperty;
    private intProperty boostedCoresProperty;
    private intProperty boostDurationProperty;
    private intProperty screenoffMaxStateProperty;
    private intProperty screenoffSglCoreProperty;
    private MulticoreIntProperty c0wfiProperty;
    private MulticoreIntProperty c1retProperty;
    private MulticoreIntProperty c2spcProperty;
    private MulticoreIntProperty c3pcProperty;
    private MulticoreLongProperty maxFreqProperty;
    private MulticoreLongProperty minFreqProperty;
    private longProperty screenoffMaxProperty;
    private StringProperty governorProperty;

    private HKMProperty[] properties;

    private View mView;

    public static CPUControl getInstance() {
        return instance != null ? instance : new CPUControl();
    }

    public CPUControl() {
        instance = this;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        governorProperty = new StringProperty(Library.GOV0, R.id.cpuGovHolder, ERR_STR);
        maxFreqProperty = new MulticoreLongProperty(Library.MAX_FREQ_PATH, R.id.maxFreqHolder, ERR_INT);
        minFreqProperty = new MulticoreLongProperty(Library.MIN_FREQ_PATH, R.id.minFreqHolder, ERR_INT);
        maxCoresProperty = new intProperty(Library.MAX_CPUS_ONLINE_PATH0, R.id.maxCoresOnBtn, ERR_INT);
        minCoresProperty = new intProperty(Library.MIN_CPUS_ONLINE_PATH0, R.id.minCoresOnBtn, ERR_INT);
        boostedCoresProperty = new intProperty(Library.BOOSTED_CPUS_PATH, R.id.boostedCoresBtn, ERR_INT);
        boostDurationProperty = new intProperty(Library.BOOST_LOCK_DURATION_PATH, R.id.boostDurationBtn, ERR_INT);
        screenoffMaxProperty = new longProperty(Library.SCREEN_OFF_MAX_FREQ, R.id.screenOffMaxHolder, ERR_INT);
        screenoffMaxStateProperty = new intProperty(Library.SCREEN_OFF_MAX_STATE, R.id.screenOffMaxHolder, ERR_INT);
        screenoffSglCoreProperty = new intProperty(Library.SCREEN_OFF_SINGLE_CORE_PATH, R.id.screenOffSglCoreHolder, ERR_INT);
        c0wfiProperty = new MulticoreIntProperty(Library.CPU_IDLE_C0_PATH, R.id.c0_switch, ERR_INT);
        c1retProperty = new MulticoreIntProperty(Library.CPU_IDLE_C1_PATH, R.id.c1_switch, ERR_INT);
        c2spcProperty = new MulticoreIntProperty(Library.CPU_IDLE_C2_PATH, R.id.c2_switch, ERR_INT);
        c3pcProperty = new MulticoreIntProperty(Library.CPU_IDLE_C3_PATH, R.id.c3_switch, ERR_INT);
        initProperties();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mView = inflater.inflate(R.layout.fragment_cpu, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refresh();

        final SeekBar maxFreq = (SeekBar) findViewById(maxFreqProperty.getViewId()).findViewById(R.id.seekBar);
        final SeekBar minFreq = (SeekBar) findViewById(minFreqProperty.getViewId()).findViewById(R.id.seekBar);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == maxFreq) {
                    if (progress < minFreq.getProgress()) {
                        minFreq.setProgress(progress);
                    }
                    ((TextView) findViewById(maxFreqProperty.getViewId()).findViewById(R.id.value)).setText(Long.toString(available_freqs[progress]));
                } else {
                    if (progress > maxFreq.getProgress()) {
                        maxFreq.setProgress(progress);
                    }
                    ((TextView) findViewById(minFreqProperty.getViewId()).findViewById(R.id.value)).setText(Long.toString(available_freqs[progress]));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        maxFreq.setOnSeekBarChangeListener(listener);
        minFreq.setOnSeekBarChangeListener(listener);
    }

    private void initProperties() {
        properties = new HKMProperty[]{
                governorProperty,
                maxFreqProperty,
                minFreqProperty,
                maxCoresProperty,
                minCoresProperty,
                boostedCoresProperty,
                boostDurationProperty,
                screenoffMaxProperty,
                screenoffMaxStateProperty,
                screenoffSglCoreProperty,
                c0wfiProperty,
                c1retProperty,
                c2spcProperty,
                c3pcProperty
        };
    }

    public void refresh() {
        new AsyncTask<Void, Void, Void>() {
            Object[] values;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mActivity.sendBroadcast(new Intent(MainActivity.ACTION_SHOW_TOUCH_BARRIER));
                values = new Object[properties.length];
            }

            @Override
            protected Void doInBackground(Void... params) {
                for (int i = 0; i < properties.length; i++) {
                    HKMProperty property = properties[i];
                    if (property instanceof intProperty) {
                        values[i] = ((intProperty) property).getValue();
                    } else if (property instanceof StringProperty) {
                        values[i] = ((StringProperty) property).getValue();
                    } else if (property instanceof longProperty) {
                        values[i] = ((longProperty) property).getValue();
                    }
                }

                String tmp = Tools.getInstance().readLineFromFile(Library.AVAIL_FREQ_PATH);

                if (tmp != null) {
                    String[] freqs = tmp.split(" ");
                    available_freqs = new long[freqs.length];
                    try {
                        for (int i = 0; i < freqs.length; i++) {
                            available_freqs[i] = Long.parseLong(freqs[i]);
                        }
                    } catch (NumberFormatException e) {
                        available_freqs = null;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                for (int i = 0; i < properties.length; i++) {
                    HKMProperty property = properties[i];
                    View parent = findViewById(property.getViewId());
                    if (property instanceof intProperty) {
                        int value = (int) values[i];
                        if (value == ERR_INT)
                            parent.setVisibility(View.GONE);
                        else {
                            {
                                View disp = parent.findViewById(R.id.value);
                                if (disp != null) {
                                    if (disp instanceof TextView) {
                                        ((TextView) disp).setText(Integer.toString(value));
                                    } else if (disp instanceof Switch) {
                                        ((Switch) disp).setChecked(value == 1);
                                    }
                                } else if (parent instanceof Switch) {
                                    ((Switch) parent).setChecked(value == 1);
                                    continue;
                                }
                            }
                            {
                                View disp = parent.findViewById(R.id.mswitch);
                                if (disp != null)
                                    if (disp instanceof Switch)
                                        ((Switch) disp).setChecked(value == 1);

                            }
                        }
                    } else if (property instanceof StringProperty) {
                        String value = (String) values[i];
                        if (ERR_STR.equals(value)) {
                            parent.setVisibility(View.GONE);
                        } else {
                            View disp = parent.findViewById(R.id.value);
                            if (disp != null)
                                if (disp instanceof TextView) {
                                    ((TextView) disp).setText(value);
                                }
                        }
                    } else if (property instanceof longProperty) {
                        long value = (long) values[i];
                        if (value == ERR_INT) {
                            parent.setVisibility(View.GONE);
                        } else {
                            {
                                View disp = parent.findViewById(R.id.value);
                                if (disp instanceof TextView)
                                    ((TextView) disp).setText(Long.toString(value));
                            }
                            {
                                View disp = parent.findViewById(R.id.seekBar);
                                if (disp != null && disp instanceof SeekBar) {
                                    SeekBar seekBar = (SeekBar) disp;
                                    if (available_freqs != null) {
                                        if (property == maxFreqProperty || property == minFreqProperty) {
                                            seekBar.setMax(available_freqs.length - 1);
                                            seekBar.setProgress(indexOf(value, available_freqs));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                mActivity.sendBroadcast(new Intent(MainActivity.ACTION_HIDE_TOUCH_BARRIER));
            }
        }.execute();

    }

    public void saveAll() {
        for (HKMProperty property : properties) {
            View parent = findViewById(property.getViewId());
            if (parent.getVisibility() == View.VISIBLE) {

                String value = null;
                if (parent instanceof Switch) {
                    value = ((Switch) parent).isChecked() ? "1" : "0";
                } else {
                    View disp = parent.findViewById(R.id.value);
                    if (disp != null) {
                        if (disp instanceof TextView) {
                            try {
                                value = ((TextView) disp).getText().toString();
                            } catch (Exception ignored) {
                            }
                        }
                    } else {
                        disp = parent.findViewById(R.id.mswitch);
                        if (disp != null && disp instanceof Switch) {
                            value = ((Switch) disp).isChecked() ? "1" : "0";
                        }
                    }
                }
                if (value != null) {
                    if (property instanceof intProperty) {
                        property.setValue(Integer.parseInt(value));
                    } else if (property instanceof longProperty) {
                        property.setValue(Long.parseLong(value));
                    } else if (property instanceof StringProperty) {
                        property.setValue(value);
                    }
                }
            }
        }
    }

    private View findViewById(int id) {
        return mView.findViewById(id);
    }

    private int indexOf(long obj, long[] objs) {
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] == obj)
                return i;
        }
        return -1;
    }
}
