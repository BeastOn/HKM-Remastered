package lb.themike10452.hellscorekernelmanagerl.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import lb.themike10452.hellscorekernelmanagerl.CustomAdapters.GPUVoltagesAdapter;
import lb.themike10452.hellscorekernelmanagerl.CustomClasses.SeekBarProgressAdapter;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiRootPathLongProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiRootPathStringProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.PropertyUtils;
import lb.themike10452.hellscorekernelmanagerl.properties.intProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.interfaces.HKMPropertyInterface;
import lb.themike10452.hellscorekernelmanagerl.properties.longProperty;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.SysfsLib;
import lb.themike10452.hellscorekernelmanagerl.utils.UIHelper;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SET_GPU_SETTINGS_ON_BOOT;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHARED_PREFS_ID;
import static lb.themike10452.hellscorekernelmanagerl.utils.HKMTools.ScriptUtils.GPU_SETTINGS_SCRIPT_NAME;
import static lb.themike10452.hellscorekernelmanagerl.utils.HKMTools.ScriptUtils.getScriptsDir;

/**
 * Created by Mike on 3/2/2015.
 */
public class GPUControl extends Fragment implements HKMFragment, View.OnClickListener {

    private static GPUControl instance;

    private static Activity mActivity;
    private static GPUVoltagesAdapter voltagesAdapter;
    private static SharedPreferences sharedPreferences;
    private static View mView;

    private static MultiRootPathStringProperty governorProperty;
    private static MultiRootPathLongProperty maxFreqProperty;
    private static intProperty simpleAlghorithmProperty;
    private static intProperty simpleLazinessProperty;
    private static intProperty simpleRampProperty;
    private static MultiRootPathStringProperty policyProperty;

    private static HKMPropertyInterface[] properties;
    private static String[] available_governors;
    private static String[] available_policies;
    private static long[] available_freqs;

    public static GPUControl getInstance() {
        if (instance == null)
            instance = new GPUControl();

        return instance;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.govCfgBtn) {
            PopupWindow popupWindow = getPopupWindow();
            TextView textView = new TextView(mActivity);
            textView.setPadding(20, 20, 20, 20);
            textView.setText(getText(R.string.note_unsupported_options));
            textView.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
            popupWindow.setContentView(textView);
            popupWindow.setAnimationStyle(R.style.flyInAnimation);
            popupWindow.showAsDropDown(v, -textView.getMeasuredWidth(), 0);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        sharedPreferences = mActivity.getSharedPreferences(SHARED_PREFS_ID, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Switch setOnBootSwitch = ((Switch) menu.findItem(R.id.action_setOnBoot).getActionView().findViewById(R.id.sob_switch));
        boolean sobEnabled = new File(getScriptsDir(mActivity), GPU_SETTINGS_SCRIPT_NAME).canExecute();
        setOnBootSwitch.setChecked(sobEnabled);
        setOnBootSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeSetOnBootState(isChecked, true);
            }
        });
        changeSetOnBootState(sobEnabled, false);
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
        mView = inflater.inflate(R.layout.fragment_gpu, null, false);
        ((ImageButton) mView.findViewById(R.id.govCfgBtn)).setImageResource(R.drawable.ic_info_24px);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        governorProperty = new MultiRootPathStringProperty(findViewById(R.id.govBtn), SysfsLib.GPU_GOVERNOR_1, SysfsLib.GPU_GOVERNOR_2);
        maxFreqProperty = new MultiRootPathLongProperty(findViewById(R.id.maxFreqHolder), SysfsLib.GPU_MAX_FREQ_1, SysfsLib.GPU_MAX_FREQ_2);
        policyProperty = new MultiRootPathStringProperty(findViewById(R.id.gpuPolicyBtn), SysfsLib.GPU_POLICY_1, SysfsLib.GPU_POLICY_2);

        simpleAlghorithmProperty = new intProperty(SysfsLib.GPU_SIMPLE_ALGORITHM, findViewById(R.id.simpleGpuGovSwitch));
        simpleLazinessProperty = new intProperty(SysfsLib.GPU_SIMPLE_LAZINESS, findViewById(R.id.simpleLazinessBtn));
        simpleRampProperty = new intProperty(SysfsLib.GPU_SIMPLE_RAMP, findViewById(R.id.simpleRampThreshBtn));

        simpleLazinessProperty.setMax(10);
        simpleRampProperty.setMax(10000);
        simpleRampProperty.setAdjustStep(1000);

        voltagesAdapter = new GPUVoltagesAdapter(mActivity, (LinearLayout) findViewById(R.id.vdd_panel).findViewById(R.id.prefsHolder));

        governorProperty.FLAGS = PropertyUtils.FLAG_VIEW_COMBO;

        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBarProgressAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxFreqProperty.setDisplayedValue(available_freqs[progress]);
            }
        });

        findViewById(R.id.govCfgBtn).setOnClickListener(this);

        initProperties();
        refresh(false);
    }

    private void initProperties() {
        properties = new HKMPropertyInterface[]{
                policyProperty,
                governorProperty,
                maxFreqProperty,
                simpleAlghorithmProperty,
                simpleLazinessProperty,
                simpleRampProperty
        };
    }

    public void refresh(boolean fromUser) {
        new AsyncTask<Void, Void, Void>() {
            String[] values;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                values = new String[properties.length];
            }

            @Override
            protected Void doInBackground(Void... params) {
                for (int i = 0; i < properties.length; i++) {
                    values[i] = properties[i].getValue();
                }

                if (available_freqs == null) {
                    fetchFrequencies();
                }
                if (available_governors == null) {
                    fetchGovernors();
                }
                governorProperty.setDisplayedValues(available_governors);

                if (available_policies == null) {
                    fetchPolicies();
                }
                policyProperty.setDisplayedValues(available_policies);

                voltagesAdapter.recycle();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                for (int i = 0; i < properties.length; i++) {
                    HKMPropertyInterface property = properties[i];

                    property.setDisplayedValue(values[i]);

                    if (property instanceof longProperty) {
                        Long value = HKMTools.parseLong(values[i]);
                        View disp = property.getTopAncestor().findViewById(R.id.seekBar);
                        if (disp != null && value != null && disp instanceof SeekBar) {
                            SeekBar seekBar = (SeekBar) disp;
                            if (available_freqs != null && available_freqs.length > 0) {
                                if (property == maxFreqProperty) {
                                    seekBar.setMax(available_freqs.length - 1);
                                    seekBar.setProgress(HKMTools.indexOf(value, available_freqs));
                                }
                            }
                        }
                    }
                }
                UIHelper.removeEmptyCards((LinearLayout) findViewById(R.id.cardHolder));
                if (!new File(getScriptsDir(mActivity), HKMTools.ScriptUtils.GPU_SETTINGS_SCRIPT_NAME).exists()) {
                    mView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            saveAll(false);
                        }
                    }, 1000);
                }
            }
        }.execute();
    }

    public void saveAll(boolean fromUser) {
        HKMTools tools = HKMTools.getInstance();
        tools.clear();
        for (HKMPropertyInterface property : properties) {
            if (property.isVisible()) {
                String value = property.readDisplayedValue();
                if (value != null) {
                    property.setValue(value);
                }
            }
        }

        createScript(tools.getRecentCommandsList());
        tools.flush();

        mView.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh(false);
            }
        }, 100);

        if (fromUser)
            Toast.makeText(mActivity.getApplicationContext(), R.string.message_action_successful, Toast.LENGTH_SHORT).show();
    }

    public void changeSetOnBootState(boolean state, boolean updateScript) {
        boolean oldState = sharedPreferences.getBoolean(SET_GPU_SETTINGS_ON_BOOT, false);
        sharedPreferences.edit().putBoolean(SET_GPU_SETTINGS_ON_BOOT, state).apply();
        if (updateScript) saveAll(false);
        if (oldState != state) {
            Toast.makeText(mActivity, state ? R.string.message_set_on_boot_enabled : R.string.message_set_on_boot_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchFrequencies() {
        String tmp = HKMTools.getInstance().readLineFromFile(SysfsLib.GPU_AVAIL_FREQS_1);
        if (tmp == null) {
            tmp = HKMTools.getInstance().readLineFromFile(SysfsLib.GPU_AVAIL_FREQ2_2);
        }
        if (tmp != null) {
            String[] freqs = tmp.split(" ");
            available_freqs = new long[freqs.length];
            try {
                for (int i = 0; i < freqs.length; i++) {
                    available_freqs[i] = Long.parseLong(freqs[i]);
                }
                if (available_freqs[0] > available_freqs[1]) {
                    HKMTools.reverseArray(available_freqs);
                }
            } catch (NumberFormatException e) {
                available_freqs = null;
            }
        } else {
            available_freqs = new long[]{};
        }
    }

    private void fetchPolicies() {
        String tmp = HKMTools.getInstance().readLineFromFile(SysfsLib.GPU_AVAILABLE_POLICIES);
        if (tmp != null) {
            available_policies = tmp.replace("none", "").trim().split(" ");
        } else {
            available_policies = new String[]{};
        }
    }

    private void fetchGovernors() {
        available_governors = getResources().getStringArray(R.array.gpu_governors);
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
        HKMTools.ScriptUtils.createScript(mActivity.getApplicationContext(), sharedPreferences, SET_GPU_SETTINGS_ON_BOOT, GPU_SETTINGS_SCRIPT_NAME, commandList);
    }

    private View findViewById(int resId) {
        return mView.findViewById(resId);
    }

    @Override
    public int getTitleId() {
        return R.string.gpuCtl;
    }
}
