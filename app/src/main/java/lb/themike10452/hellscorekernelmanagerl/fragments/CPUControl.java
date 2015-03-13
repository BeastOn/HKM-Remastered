package lb.themike10452.hellscorekernelmanagerl.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.markushi.ui.action.CloseAction;
import at.markushi.ui.action.DrawerAction;
import lb.themike10452.hellscorekernelmanagerl.CustomAdapters.SeekBarProgressAdapter;
import lb.themike10452.hellscorekernelmanagerl.CustomAdapters.VoltagesAdapter;
import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.NumberModifier;
import lb.themike10452.hellscorekernelmanagerl.MainActivity;
import lb.themike10452.hellscorekernelmanagerl.MainActivity.mTransactionManager;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.Settings;
import lb.themike10452.hellscorekernelmanagerl.properties.HKMPropertyInterface;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiCoreIntProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiCoreLongProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiLineValueProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiRootPathIntProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.PropertyUtils;
import lb.themike10452.hellscorekernelmanagerl.properties.StringProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.intProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.longProperty;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SET_CPU_SETTINGS_ON_BOOT;
import static lb.themike10452.hellscorekernelmanagerl.properties.PropertyUtils.ERR_INT;
import static lb.themike10452.hellscorekernelmanagerl.properties.PropertyUtils.ERR_STR;

/**
 * Created by Mike on 2/22/2015.
 */
public class CPUControl extends Fragment implements View.OnClickListener {

    private static CPUControl instance;

    private MainActivity mActivity;
    private SharedPreferences sharedPreferences;
    private VoltagesAdapter mVoltagesAdapter;
    private mTransactionManager transactionManager;

    private MultiRootPathIntProperty maxCoresProperty;
    private MultiRootPathIntProperty minCoresProperty;
    private intProperty msmHotplugEnabled;
    private intProperty msmMPDecisionEnabled;
    private intProperty boostedCoresProperty;
    private intProperty boostDurationProperty;
    private intProperty screenoffMaxStateProperty;
    private intProperty screenoffSglCoreProperty;
    private intProperty maxCoresSuspProperty;
    private MultiCoreIntProperty c0wfiProperty;
    private MultiCoreIntProperty c1retProperty;
    private MultiCoreIntProperty c2spcProperty;
    private MultiCoreIntProperty c3pcProperty;
    private MultiCoreLongProperty maxFreqProperty;
    private MultiCoreLongProperty minFreqProperty;
    private longProperty screenoffMaxProperty;
    private MultiLineValueProperty touchBoostProperty;
    private intProperty touchBoostStateProperty;
    private StringProperty governorProperty;

    private HKMPropertyInterface[] clickableProperties;
    private HKMPropertyInterface[] properties;
    private String[] available_governors;
    private long[] available_freqs;

    private View mView;
    private boolean displayVoltages;

    public CPUControl() {
        instance = this;
    }

    public static CPUControl getInstance(mTransactionManager manager) {
        if (instance == null) {
            instance = new CPUControl();
        }
        if (manager != null) {
            instance.transactionManager = manager;
        }
        return instance;
    }

    @Override
    public void onClick(final View v) {
        if (v == findViewById(R.id.govCfgBtn)) {
            Fragment fragment = CPUGovernorCfg.getNewInstance(governorProperty.readDisplayedValue());
            int fast = getResources().getInteger(R.integer.duration_transition_fast);
            setExitTransition(new Explode().setDuration(fast + 300).setStartDelay(0).setInterpolator(new AccelerateInterpolator()));
            setSharedElementReturnTransition(TransitionInflater.from(mActivity).inflateTransition(R.transition.move));
            fragment.setReturnTransition(new Fade().setDuration(fast));
            fragment.setSharedElementEnterTransition(TransitionInflater.from(mActivity).inflateTransition(R.transition.move));
            fragment.setEnterTransition(new Fade().setStartDelay(fast));
            fragment.setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                    super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
                    sharedElements.get(0).setVisibility(View.INVISIBLE);
                }
            });
            Pair<View, String> pair1 = new Pair<>(findViewById(R.id.card), "card");
            Pair<View, String> pair2 = new Pair<>(findViewById(R.id.govCfgBtn), "govCfgBtn");
            transactionManager.setDrawerEnabled(false);
            transactionManager.setDrawerIndicator(new CloseAction());
            transactionManager.performTransaction(fragment, true, false, null, pair1, pair2);
            return;
        }

        final HKMPropertyInterface property = PropertyUtils.findProperty(properties, v);
        if (property != null) {
            final List<String> choices = new ArrayList<>();
            if (property instanceof intProperty) {
                int flags = ((intProperty) property).FLAGS;
                if ((PropertyUtils.FLAG_CPU_CORES & flags) == PropertyUtils.FLAG_CPU_CORES) {
                    if ((PropertyUtils.FLAG_CPU_CORES_ALLOW_ZERO & flags) == PropertyUtils.FLAG_CPU_CORES_ALLOW_ZERO) {
                        choices.add("0");
                    }
                    for (int i = 1; i <= 4; i++) {
                        choices.add(Integer.toString(i));
                    }
                } else {
                    final NumberModifier modifier = new NumberModifier(mActivity);
                    modifier.setValue(property.readDisplayedValue());
                    modifier.setPadding(20, 20, 20, 20);
                    modifier.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

                    PopupWindow popupWindow = getPopupWindow();
                    popupWindow.setContentView(modifier);
                    popupWindow.showAsDropDown(v, v.getMeasuredWidth() - modifier.getMeasuredWidth(), 0);
                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            property.setDisplayedValue(modifier.getValue());
                        }
                    });
                }
            } else if (property instanceof StringProperty) {
                if (property == governorProperty && available_governors != null) {
                    choices.addAll(Arrays.asList(available_governors));
                }
                View layout = LayoutInflater.from(mActivity).inflate(R.layout.simple_picker_layout, null, false);
                layout.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
                PopupWindow popupWindow = getPopupWindow();
                popupWindow.setContentView(layout);
                popupWindow.showAsDropDown(v, v.getMeasuredWidth() / 2 - layout.getMeasuredWidth() / 2, 0);
                final NumberPicker picker = (NumberPicker) layout.findViewById(R.id.numberPicker);
                picker.setMinValue(0);
                picker.setMaxValue(choices.size() - 1);
                picker.setDisplayedValues(choices.toArray(new String[choices.size()]));
                picker.setValue(choices.indexOf(property.readDisplayedValue()));
                picker.setWrapSelectorWheel(false);
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        property.setDisplayedValue(choices.get(picker.getValue()));
                    }
                });
                return;
            } else if (property instanceof longProperty || property instanceof MultiLineValueProperty) {
                if (available_freqs != null) {
                    for (long l : available_freqs) {
                        choices.add(Long.toString(l));
                    }
                }
            }
            if (!choices.isEmpty()) {
                PopupWindow popupWindow = getPopupWindow();
                if (property instanceof MultiLineValueProperty) {
                    View layout = ((LayoutInflater) mActivity
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                            .inflate(R.layout.quad_picker_layout, null, false);
                    popupWindow.setContentView(layout);
                    popupWindow.showAsDropDown(v);
                    LinearLayout parent = (LinearLayout) layout.findViewById((R.id.parent));
                    int count = parent.getChildCount();
                    final NumberPicker[] pickers = new NumberPicker[count];
                    String str = ((TextView) findViewById(property.getViewId()).findViewById(R.id.value)).getText().toString();
                    str = str.substring(1);
                    str = str.substring(0, str.length() - 1);
                    String[] currentValues = str.split(",");
                    for (int i = 0; i < count; i++) {
                        TextView title = (TextView) parent.getChildAt(i).findViewById(R.id.title);
                        title.setVisibility(View.VISIBLE);
                        title.setText("CPU" + i);
                        pickers[i] = (NumberPicker) parent.getChildAt(i).findViewById(R.id.numberPicker);
                        pickers[i].setMinValue(0);
                        pickers[i].setMaxValue(available_freqs.length - 1);
                        pickers[i].setValue(choices.indexOf(currentValues[i].trim()));
                        pickers[i].setDisplayedValues(choices.toArray(new String[choices.size()]));
                        pickers[i].setWrapSelectorWheel(false);
                        pickers[i].setPadding(3, 0, 3, 0);
                    }
                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            String newValues = "";
                            for (NumberPicker picker : pickers)
                                newValues = newValues.concat(choices.get(picker.getValue())).concat(" ");
                            newValues = newValues.trim();
                            touchBoostProperty.setDisplayedValue(Arrays.asList(newValues.split(" ")));
                        }
                    });
                } else {
                    final NumberModifier modifier = new NumberModifier(mActivity);
                    modifier.setPadding(20, 20, 20, 20);
                    modifier.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    popupWindow.setContentView(modifier);
                    popupWindow.showAsDropDown(v, v.getMeasuredWidth() - modifier.getMeasuredWidth(), 0);
                    modifier.setMin(0);
                    modifier.setMax(choices.size() - 1);
                    modifier.setDisplayedValues(choices.toArray(new String[choices.size()]));
                    modifier.setValue(choices.indexOf(((TextView) findViewById(property.getViewId()).findViewById(R.id.value)).getText().toString()));
                    modifier.setInputType(TypedValue.TYPE_NULL);
                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            String newValue = choices.get(modifier.getSelectionIndex());
                            property.setDisplayedValue(newValue);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
        sharedPreferences = mActivity.getSharedPreferences(Settings.Constants.SHARED_PREFS_ID, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Switch setOnBootSwitch = ((Switch) menu.findItem(R.id.action_setOnBoot).getActionView().findViewById(R.id.sob_switch));
        boolean sobEnabled = sharedPreferences.getBoolean(SET_CPU_SETTINGS_ON_BOOT, false);
        setOnBootSwitch.setChecked(sobEnabled);
        setOnBootSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeSetOnBootState(isChecked);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh(true);
                return true;
            case R.id.action_apply:
                saveAll(true);
                return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if (transactionManager != null) {
            transactionManager.setActiveFragment(this);
            transactionManager.setDrawerEnabled(true);
            transactionManager.setDrawerIndicator(new DrawerAction());
        }
        mView = inflater.inflate(R.layout.fragment_cpu, container, false);
        mVoltagesAdapter = new VoltagesAdapter(mActivity, findViewById(R.id.vdd_panel));
        FrameLayout goContainer = (FrameLayout) mView.findViewById(R.id.globalOffsetContainer);
        goContainer.addView(mVoltagesAdapter.getView(-1, null, null));
        displayVoltages = false;
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        governorProperty = new StringProperty(Library.CPU0_GOV_PATH, findViewById(R.id.govBtn), ERR_STR);
        maxFreqProperty = new MultiCoreLongProperty(Library.MAX_FREQ_PATH, findViewById(R.id.maxFreqHolder), ERR_INT);
        minFreqProperty = new MultiCoreLongProperty(Library.MIN_FREQ_PATH, findViewById(R.id.minFreqHolder), ERR_INT);
        msmHotplugEnabled = new intProperty(Library.MSM_HOTPLUG_ENABLED, findViewById(R.id.enable_msm_hotplug), ERR_INT);
        msmMPDecisionEnabled = new intProperty(Library.MSM_MPDECISION_ENABLED, findViewById(R.id.enable_msm_mpdec), ERR_INT);
        maxCoresSuspProperty = new intProperty(Library.MAX_CPUS_SUSP, findViewById(R.id.maxCoresSuspBtn), ERR_INT);
        boostedCoresProperty = new intProperty(Library.BOOSTED_CPUS_PATH, findViewById(R.id.boostedCoresBtn), ERR_INT);
        boostDurationProperty = new intProperty(Library.BOOST_LOCK_DURATION_PATH, findViewById(R.id.boostDurationBtn), ERR_INT);
        screenoffMaxProperty = new longProperty(Library.SCREEN_OFF_MAX_FREQ, findViewById(R.id.screenOffMaxBtn), ERR_INT);
        screenoffMaxStateProperty = new intProperty(Library.SCREEN_OFF_MAX_STATE, findViewById(R.id.screenOffMaxStateSwitch), ERR_INT);
        screenoffSglCoreProperty = new intProperty(Library.SCREEN_OFF_SINGLE_CORE_PATH, findViewById(R.id.screenOffSglCoreHolder), ERR_INT);
        touchBoostProperty = new MultiLineValueProperty(findViewById(R.id.touchBoostBtn), Library.TOUCH_BOOST_FREQS_PATH, ERR_STR);
        touchBoostStateProperty = new intProperty(Library.TOUCH_BOOST_PATH, findViewById(R.id.touchBoostSwitch), ERR_INT);
        c0wfiProperty = new MultiCoreIntProperty(Library.CPU_IDLE_C0_PATH, findViewById(R.id.c0_switch), ERR_INT);
        c1retProperty = new MultiCoreIntProperty(Library.CPU_IDLE_C1_PATH, findViewById(R.id.c1_switch), ERR_INT);
        c2spcProperty = new MultiCoreIntProperty(Library.CPU_IDLE_C2_PATH, findViewById(R.id.c2_switch), ERR_INT);
        c3pcProperty = new MultiCoreIntProperty(Library.CPU_IDLE_C3_PATH, findViewById(R.id.c3_switch), ERR_INT);

        maxCoresProperty = new MultiRootPathIntProperty(findViewById(R.id.maxCoresOnBtn), ERR_INT, Library.MAX_CPUS_ONLINE_PATH0, Library.MAX_CPUS_ONLINE_PATH1) {
            @Override
            public void setDisplayedValue(Object _value) {
                super.setDisplayedValue(_value);
                if (_value instanceof String) {
                    int thisValue = Integer.parseInt((String) _value);
                    int thatValue = Integer.parseInt(minCoresProperty.readDisplayedValue());
                    minCoresProperty.setDisplayedValue(Math.min(thisValue, thatValue));
                }
            }
        };
        minCoresProperty = new MultiRootPathIntProperty(findViewById(R.id.minCoresOnBtn), ERR_INT, Library.MIN_CPUS_ONLINE_PATH0, Library.MIN_CPUS_ONLINE_PATH1) {
            @Override
            public void setDisplayedValue(Object _value) {
                super.setDisplayedValue(_value);
                if (_value instanceof String) {
                    int thisValue = Integer.parseInt((String) _value);
                    int thatValue = Integer.parseInt(maxCoresProperty.readDisplayedValue());
                    maxCoresProperty.setDisplayedValue(Math.max(thisValue, thatValue));
                }
            }
        };

        governorProperty.FLAGS = PropertyUtils.FLAG_VIEW_COMBO;
        maxCoresProperty.FLAGS = PropertyUtils.FLAG_CPU_CORES;
        minCoresProperty.FLAGS = PropertyUtils.FLAG_CPU_CORES;
        maxCoresSuspProperty.FLAGS = PropertyUtils.FLAG_CPU_CORES;
        boostedCoresProperty.FLAGS = PropertyUtils.FLAG_CPU_CORES | PropertyUtils.FLAG_CPU_CORES_ALLOW_ZERO;
        screenoffMaxProperty.FLAGS = PropertyUtils.FLAG_VIEW_COMBO;
        touchBoostProperty.FLAGS = PropertyUtils.FLAG_VIEW_COMBO;

        initProperties();
        initListeners();

        refresh(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mVoltagesAdapter.init();
            }
        }).start();
    }

    private void initProperties() {
        properties = new HKMPropertyInterface[]{
                governorProperty,
                maxFreqProperty,
                minFreqProperty,
                msmMPDecisionEnabled,
                msmHotplugEnabled,
                maxCoresSuspProperty,
                maxCoresProperty,
                minCoresProperty,
                boostedCoresProperty,
                boostDurationProperty,
                screenoffMaxStateProperty,
                screenoffMaxProperty,
                screenoffSglCoreProperty,
                touchBoostStateProperty,
                touchBoostProperty,
                c0wfiProperty,
                c1retProperty,
                c2spcProperty,
                c3pcProperty
        };
        clickableProperties = new HKMPropertyInterface[]{
                governorProperty,
                maxCoresProperty,
                minCoresProperty,
                maxCoresSuspProperty,
                boostedCoresProperty,
                screenoffMaxProperty,
                touchBoostProperty,
                boostDurationProperty
        };
    }

    private void initListeners() {
        final SeekBar maxFreq = (SeekBar) findViewById(maxFreqProperty.getViewId()).findViewById(R.id.seekBar);
        final SeekBar minFreq = (SeekBar) findViewById(minFreqProperty.getViewId()).findViewById(R.id.seekBar);

        SeekBar.OnSeekBarChangeListener listener = new SeekBarProgressAdapter() {
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
        };

        maxFreq.setOnSeekBarChangeListener(listener);
        minFreq.setOnSeekBarChangeListener(listener);

        findViewById(R.id.govCfgBtn).setOnClickListener(this);
        findViewById(R.id.expandVddBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayVoltages = true;
                mVoltagesAdapter.show();
            }
        });

        Switch hotPlugSwitch = (Switch) findViewById(msmHotplugEnabled.getViewId()).findViewById(R.id.mswitch);
        hotPlugSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findViewById(R.id.msm_custom).setVisibility(!isChecked ? View.GONE : View.VISIBLE);
            }
        });

        for (HKMPropertyInterface property : clickableProperties) {
            findViewById(property.getViewId()).setOnClickListener(this);
        }
    }

    public void refresh(final boolean fromUser) {
        new AsyncTask<Void, Void, Void>() {
            Object[] values;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                values = new Object[properties.length];
            }

            @Override
            protected Void doInBackground(Void... params) {
                for (int i = 0; i < properties.length; i++) {
                    HKMPropertyInterface property = properties[i];
                    if (property instanceof intProperty) {
                        values[i] = ((intProperty) property).getValue();
                    } else if (property instanceof StringProperty) {
                        values[i] = ((StringProperty) property).getValue();
                    } else if (property instanceof longProperty) {
                        values[i] = ((longProperty) property).getValue();
                    } else if (property instanceof MultiLineValueProperty) {
                        values[i] = ((MultiLineValueProperty) property).getValue();
                    }
                }
                if (available_freqs == null) {
                    fetchFrequencies();
                }
                if (available_governors == null) {
                    fetchGovernors();
                }
                if (displayVoltages && fromUser) {
                    mVoltagesAdapter.invalidate();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                for (int i = 0; i < properties.length; i++) {
                    HKMPropertyInterface property = properties[i];
                    View parent = findViewById(property.getViewId());
                    try {
                        if ((PropertyUtils.FLAG_VIEW_COMBO & property.getFlags()) == PropertyUtils.FLAG_VIEW_COMBO)
                            parent = (View) parent.getParent();
                    } catch (Exception ignored) {
                    }
                    property.setDisplayedValue(values[i]);
                    if (property instanceof longProperty) {
                        long value = (long) values[i];
                        View disp = parent.findViewById(R.id.seekBar);
                        if (disp != null && disp instanceof SeekBar) {
                            SeekBar seekBar = (SeekBar) disp;
                            if (available_freqs != null) {
                                if (property == maxFreqProperty || property == minFreqProperty) {
                                    seekBar.setMax(available_freqs.length - 1);
                                    seekBar.setProgress(HKMTools.indexOf(value, available_freqs));
                                }
                            }
                        }
                    }
                }
                mActivity.sendBroadcast(new Intent(MainActivity.ACTION_HIDE_TOUCH_BARRIER));
            }
        }.execute();

    }

    public void saveAll(boolean fromUser) {
        HKMTools tools = HKMTools.getInstance();
        tools.getReady();
        for (HKMPropertyInterface property : properties) {
            boolean isCombo = (PropertyUtils.FLAG_VIEW_COMBO & property.getFlags()) == PropertyUtils.FLAG_VIEW_COMBO;
            View holder = property.getView();
            View parentView = holder;
            if (isCombo) {
                while (parentView.getId() != R.id.firstChild) {
                    parentView = (View) parentView.getParent();
                }
                parentView = (View) parentView.getParent();
            }
            if (holder.getVisibility() == View.VISIBLE && (!isCombo || parentView.getVisibility() == View.VISIBLE)) {
                String value = property.readDisplayedValue();
                if (value != null) {
                    if (property == touchBoostProperty) {
                        String str = value;
                        str = str
                                .substring(1)
                                .substring(0, str.length() - 1)
                                .trim()
                                .replace(" ", "");
                        List<String> values = Arrays.asList(str.split(","));
                        List<String> prefixes = Arrays.asList("0", "1", "2", "3");
                        touchBoostProperty.setValue(values, prefixes, null);
                    } else {
                        property.setValue(value);
                    }
                }
            }
        }
        mVoltagesAdapter.flush();
        createScript(tools.getRecentCommandsList());
        tools.flush();
        if (fromUser) {
            Toast.makeText(mActivity.getApplicationContext(), R.string.message_applied_successfully, Toast.LENGTH_SHORT).show();
        }
    }

    public void changeSetOnBootState(boolean enabled) {
        sharedPreferences.edit().putBoolean(SET_CPU_SETTINGS_ON_BOOT, enabled).apply();
        saveAll(false);
        Toast.makeText(mActivity, enabled ? R.string.message_set_on_boot_enabled : R.string.message_set_on_boot_disabled, Toast.LENGTH_SHORT).show();
    }

    private void fetchFrequencies() {
        String tmp = HKMTools.getInstance().readLineFromFile(Library.AVAIL_FREQ_PATH);
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
    }

    private void fetchGovernors() {
        String tmp = HKMTools.getInstance().readLineFromFile(Library.AVAIL_GOV_PATH);
        if (tmp != null) {
            try {
                available_governors = tmp.split(" ");
            } catch (Exception e) {
                available_governors = null;
            }
        }
    }

    private PopupWindow getPopupWindow() {
        PopupWindow popupWindow = new PopupWindow(mActivity);
        popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(1);
        popupWindow.setWidth(1000);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_window_background));
        popupWindow.setClippingEnabled(true);
        popupWindow.setElevation(10f);
        return popupWindow;
    }

    private void createScript(List<String> commandList) {
        boolean sobEnabled = sharedPreferences.getBoolean(SET_CPU_SETTINGS_ON_BOOT, false);
        if (!sobEnabled) {
            HKMTools.ScriptUtils.clearScript(mActivity.getApplicationContext(), HKMTools.ScriptUtils.CPU_SETTINGS_SCRIPT_NAME);
        } else {
            try {
                HKMTools.ScriptUtils.writeScript(mActivity.getApplicationContext(), HKMTools.ScriptUtils.CPU_SETTINGS_SCRIPT_NAME, commandList, true);
            } catch (IOException e) {
                Toast.makeText(mActivity.getApplicationContext(), R.string.message_script_failed, Toast.LENGTH_SHORT).show();
                Toast.makeText(mActivity.getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private View findViewById(int id) {
        return mView.findViewById(id);
    }
}
