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

import lb.themike10452.hellscorekernelmanagerl.CustomAdapters.SeekBarProgressAdapter;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.Settings;
import lb.themike10452.hellscorekernelmanagerl.properties.HKMPropertyInterface;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiLineValueProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiRootPathLongProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiRootPathStringProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.PropertyUtils;
import lb.themike10452.hellscorekernelmanagerl.properties.StringProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.intProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.longProperty;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SET_GPU_SETTINGS_ON_BOOT;
import static lb.themike10452.hellscorekernelmanagerl.properties.PropertyUtils.ERR_INT;
import static lb.themike10452.hellscorekernelmanagerl.properties.PropertyUtils.ERR_STR;
import static lb.themike10452.hellscorekernelmanagerl.utils.HKMTools.ScriptUtils.GPU_SETTINGS_SCRIPT_NAME;

/**
 * Created by Mike on 3/2/2015.
 */
public class GPUControl extends Fragment implements View.OnClickListener {

    private static GPUControl instance;

    private Activity mActivity;
    private SharedPreferences sharedPreferences;
    private View mView;

    private MultiRootPathStringProperty governorProperty;
    private MultiRootPathLongProperty maxFreqProperty;
    private MultiRootPathStringProperty policyProperty;

    private HKMPropertyInterface[] properties;
    private String[] available_governors;
    private String[] available_policies;
    private long[] available_freqs;

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
            textView.setText(getText(R.string.disclaimer_gpu_governors));
            textView.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
            popupWindow.setContentView(textView);
            popupWindow.setAnimationStyle(R.style.popUpAnimation);
            popupWindow.showAsDropDown(v, -textView.getMeasuredWidth(), 0);
        } else {
            final HKMPropertyInterface property = PropertyUtils.findProperty(properties, v);
            if (property != null) {
                if (property instanceof StringProperty) {
                    final ArrayList<String> choices = new ArrayList<>();
                    if (property == governorProperty) {
                        choices.addAll(Arrays.asList(available_governors));
                    } else if (property == policyProperty) {
                        choices.addAll(Arrays.asList(available_policies));
                    }
                    if (choices.size() > 0) {
                        PopupWindow popupWindow = getPopupWindow();
                        View layout = LayoutInflater.from(mActivity).inflate(R.layout.simple_picker_layout, null, false);
                        layout.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
                        popupWindow.setContentView(layout);
                        popupWindow.showAsDropDown(v, v.getMeasuredWidth() / 2 - layout.getMeasuredWidth() / 2, 0);
                        final NumberPicker picker = (NumberPicker) layout.findViewById(R.id.numberPicker);
                        picker.setDisplayedValues(choices.toArray(new String[choices.size()]));
                        picker.setMinValue(0);
                        picker.setMaxValue(choices.size() - 1);
                        picker.setValue(choices.indexOf(property.readDisplayedValue()));
                        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                property.setDisplayedValue(choices.get(picker.getValue()));
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
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
        boolean sobEnabled = sharedPreferences.getBoolean(SET_GPU_SETTINGS_ON_BOOT, false);
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
        mView = inflater.inflate(R.layout.fragment_gpu, null, false);
        ((ImageButton) mView.findViewById(R.id.govCfgBtn)).setImageResource(R.drawable.ic_info_24px);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        governorProperty = new MultiRootPathStringProperty(findViewById(R.id.govBtn), ERR_STR, Library.GPU_GOV_PATH0, Library.GPU_GOV_PATH1);
        maxFreqProperty = new MultiRootPathLongProperty(findViewById(R.id.maxFreqHolder), ERR_INT, Library.GPU_MAX_CLK_PATH0, Library.GPU_MAX_CLK_PATH1);
        policyProperty = new MultiRootPathStringProperty(findViewById(R.id.gpuPolicyBtn), ERR_STR, Library.GPU_POLICY_PATH0, Library.GPU_POLICY_PATH1);

        governorProperty.FLAGS = PropertyUtils.FLAG_VIEW_COMBO;

        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBarProgressAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxFreqProperty.setDisplayedValue(available_freqs[progress]);
            }
        });

        findViewById(R.id.govBtn).setOnClickListener(this);
        findViewById(R.id.govCfgBtn).setOnClickListener(this);
        findViewById(R.id.gpuPolicyBtn).setOnClickListener(this);

        initProperties();
        refresh(false);
    }

    private void initProperties() {
        properties = new HKMPropertyInterface[]{
                policyProperty,
                governorProperty,
                maxFreqProperty
        };
    }

    public void refresh(boolean fromUser) {
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

                if (available_freqs == null)
                    fetchFrequencies();

                if (available_governors == null)
                    fetchGovernors();

                if (available_policies == null) {
                    fetchPolicies();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                for (int i = 0; i < properties.length; i++) {
                    HKMPropertyInterface property = properties[i];
                    View holder = property.getView();
                    try {
                        if ((PropertyUtils.FLAG_VIEW_COMBO & property.getFlags()) == PropertyUtils.FLAG_VIEW_COMBO) {
                            while (holder.getId() != R.id.firstChild) {
                                holder = (View) holder.getParent();
                            }
                            holder = (View) holder.getParent();
                        }
                    } catch (Exception ignored) {
                    }

                    property.setDisplayedValue(values[i]);

                    if (property instanceof longProperty) {
                        long value = (long) values[i];
                        View disp = holder.findViewById(R.id.seekBar);
                        if (disp != null && disp instanceof SeekBar) {
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
            Toast.makeText(mActivity.getApplicationContext(), R.string.message_applied_successfully, Toast.LENGTH_SHORT).show();
    }

    public void changeSetOnBootState(boolean enabled) {
        sharedPreferences.edit().putBoolean(SET_GPU_SETTINGS_ON_BOOT, enabled).apply();
        saveAll(false);
        Toast.makeText(mActivity, enabled ? R.string.message_set_on_boot_enabled : R.string.message_set_on_boot_disabled, Toast.LENGTH_SHORT).show();
    }

    private void fetchFrequencies() {
        String tmp = HKMTools.getInstance().readLineFromFile(Library.GPU_AVAIL_FREQ_PATH0);
        if (tmp == null) {
            tmp = HKMTools.getInstance().readLineFromFile(Library.GPU_AVAIL_FREQ_PATH1);
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
        String tmp = HKMTools.getInstance().readLineFromFile(Library.GPU_AVAILABLE_POLICIES);
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
        boolean sobEnabled = sharedPreferences.getBoolean(SET_GPU_SETTINGS_ON_BOOT, false);
        if (!sobEnabled) {
            HKMTools.ScriptUtils.clearScript(mActivity.getApplicationContext(), GPU_SETTINGS_SCRIPT_NAME);
        } else {
            try {
                HKMTools.ScriptUtils.writeScript(mActivity.getApplicationContext(), GPU_SETTINGS_SCRIPT_NAME, commandList, false);
            } catch (IOException e) {
                Toast.makeText(mActivity.getApplicationContext(), R.string.message_script_failed, Toast.LENGTH_SHORT).show();
                Toast.makeText(mActivity.getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private View findViewById(int resId) {
        return mView.findViewById(resId);
    }
}
