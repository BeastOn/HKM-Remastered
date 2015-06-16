package lb.themike10452.hellscorekernelmanagerl.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.WheelPicker;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.Settings;
import lb.themike10452.hellscorekernelmanagerl.properties.FauxSoundProperty;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.SysfsLib;
import lb.themike10452.hellscorekernelmanagerl.utils.UIHelper;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SET_SND_SETTINGS_ON_BOOT;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHARED_PREFS_ID;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SND_CTL_LINK_LEFT_RIGHT;
import static lb.themike10452.hellscorekernelmanagerl.utils.HKMTools.ScriptUtils.SND_SETTINGS_SCRIPT_NAME;
import static lb.themike10452.hellscorekernelmanagerl.utils.HKMTools.ScriptUtils.getScriptsDir;

/**
 * Created by Mike on 3/6/2015.
 */
public class SoundControl extends Fragment implements HKMFragment, Observer {

    private static final int[][] SoundProfiles = new int[][]{
            {0, 0, 0, 0, 0, 0, 0},//stock
            {-5, -5, -2, -2, -3, 0, 0},//quiet
            {9, 9, 3, 3, 5, 0, 0},//loudness
            {-2, -2, 5, 5, -2, 0, 0},//quality
    };

    public static SoundControl instance;

    private static FauxSoundProperty leftHeadphoneGainProperty;
    private static FauxSoundProperty rightHeadphoneGainProperty;
    private static FauxSoundProperty leftPowerAmpGainProperty;
    private static FauxSoundProperty rightPowerAmpGainProperty;
    private static FauxSoundProperty speakerGainProperty;
    private static FauxSoundProperty micGainProperty;
    private static FauxSoundProperty camcorderGainProperty;
    private static Activity mActivity;
    private static LinearLayout mContainer;
    private static SharedPreferences sharedPreferences;
    private static View mView;
    private static FauxSoundProperty[] properties;
    private static int[] arrayOfInt1;

    public SoundControl() {
        instance = this;
        arrayOfInt1 = new int[7];
    }

    public static SoundControl getInstance() {
        return instance != null ? instance : new SoundControl();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        sharedPreferences = activity.getSharedPreferences(SHARED_PREFS_ID, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final Switch setOnBootSwitch = (Switch) menu.findItem(R.id.action_setOnBoot).getActionView().findViewById(R.id.sob_switch);
        final boolean sobEnabled = new File(getScriptsDir(mActivity), SND_SETTINGS_SCRIPT_NAME).canExecute();
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
            default:
                return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_sound, container, false);
        mContainer = (LinearLayout) findViewById(R.id.prefsHolder);
        final RadioButton linkToggle = (RadioButton) findViewById(R.id.linkToggle);
        linkToggle.setChecked(sharedPreferences.getBoolean(SND_CTL_LINK_LEFT_RIGHT, false));
        linkToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newState;
                sharedPreferences.edit().putBoolean(SND_CTL_LINK_LEFT_RIGHT, newState = !sharedPreferences.getBoolean(SND_CTL_LINK_LEFT_RIGHT, false)).apply();
                linkToggle.setChecked(newState);
            }
        });
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        initProperties();
        for (FauxSoundProperty property : properties) {
            if (property != null) {
                mContainer.addView(property.getView());
            }
        }

        final WheelPicker wheelPicker = (WheelPicker) findViewById(R.id.wheel);
        final String[] profiles = getResources().getStringArray(R.array.sound_profiles);
        wheelPicker.setDisplayedValues(profiles);
        wheelPicker.setOnSelectionChangedListener(new WheelPicker.OnSelectionChangedListener() {
            @Override
            public void selectionChanged(int i) {
                if (i >= 1) {
                    i -= 1;
                    leftHeadphoneGainProperty.setDisplayedValue(SoundProfiles[i][0]);
                    rightHeadphoneGainProperty.setDisplayedValue(SoundProfiles[i][1]);
                    leftPowerAmpGainProperty.setDisplayedValue(SoundProfiles[i][2]);
                    rightPowerAmpGainProperty.setDisplayedValue(SoundProfiles[i][3]);
                    speakerGainProperty.setDisplayedValue(SoundProfiles[i][4]);
                    micGainProperty.setDisplayedValue(SoundProfiles[i][5]);
                    camcorderGainProperty.setDisplayedValue(SoundProfiles[i][6]);
                }
            }
        });

        mContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh(false);
            }
        }, 100);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (sharedPreferences.getBoolean(Settings.Constants.SND_CTL_LINK_LEFT_RIGHT, false)) {
            FauxSoundProperty complement = ((FauxSoundProperty) observable).getComplement();
            if (complement != null) {
                complement.setDisplayedValue((int) data);
            }
        }
    }

    private void initProperties() {
        leftHeadphoneGainProperty = new FauxSoundProperty(SysfsLib.SOUND_HP_GAIN, getContainerView(), R.string.pref_hp_gain, -10, 10, FauxSoundProperty.MODE_LEFT);
        rightHeadphoneGainProperty = new FauxSoundProperty(SysfsLib.SOUND_HP_GAIN, getContainerView(), -1, -10, 10, FauxSoundProperty.MODE_RIGHT);
        leftPowerAmpGainProperty = new FauxSoundProperty(SysfsLib.SOUND_HP_PA_GAIN, getContainerView(), R.string.pref_pa_gain, -6, 6, FauxSoundProperty.MODE_LEFT_AMP);
        rightPowerAmpGainProperty = new FauxSoundProperty(SysfsLib.SOUND_HP_PA_GAIN, getContainerView(), -1, -6, 6, FauxSoundProperty.MODE_RIGHT_AMP);
        speakerGainProperty = new FauxSoundProperty(SysfsLib.SOUND_SPEAKER_GAIN, getContainerView(), R.string.pref_spk_gain, -10, 10, FauxSoundProperty.MODE_DUAL);
        micGainProperty = new FauxSoundProperty(SysfsLib.SOUND_MIC_GAIN, getContainerView(), R.string.pref_mic_gain, -10, 10, FauxSoundProperty.MODE_SINGLE);
        camcorderGainProperty = new FauxSoundProperty(SysfsLib.SOUND_CAMMIC_GAIN, getContainerView(), R.string.pref_camc_gain, -10, 10, FauxSoundProperty.MODE_SINGLE);

        leftHeadphoneGainProperty.setComplement(rightHeadphoneGainProperty);
        rightHeadphoneGainProperty.setComplement(leftHeadphoneGainProperty);
        leftPowerAmpGainProperty.setComplement(rightPowerAmpGainProperty);
        rightPowerAmpGainProperty.setComplement(leftPowerAmpGainProperty);

        leftHeadphoneGainProperty.addObserver(this);
        rightHeadphoneGainProperty.addObserver(this);
        leftPowerAmpGainProperty.addObserver(this);
        rightPowerAmpGainProperty.addObserver(this);

        properties = new FauxSoundProperty[]{
                leftHeadphoneGainProperty,
                rightHeadphoneGainProperty,
                leftPowerAmpGainProperty,
                rightPowerAmpGainProperty,
                speakerGainProperty,
                micGainProperty,
                camcorderGainProperty
        };
    }

    private void storeActiveProfileToArray() {
        arrayOfInt1[0] = Integer.parseInt(leftHeadphoneGainProperty.getValue(true));
        arrayOfInt1[1] = Integer.parseInt(rightHeadphoneGainProperty.getValue(true));
        arrayOfInt1[2] = Integer.parseInt(leftPowerAmpGainProperty.getValue(true));
        arrayOfInt1[3] = Integer.parseInt(rightPowerAmpGainProperty.getValue(true));
        arrayOfInt1[4] = Integer.parseInt(speakerGainProperty.getValue(true));
        arrayOfInt1[5] = Integer.parseInt(micGainProperty.getValue(true));
        arrayOfInt1[6] = Integer.parseInt(camcorderGainProperty.getValue(true));
    }

    public void refresh(boolean fromUser) {
        for (FauxSoundProperty property : properties) {
            if (property != null) {
                property.setDisplayedValue(property.getValue(true));
            }
        }
        storeActiveProfileToArray();
        String activeProfile = Arrays.toString(arrayOfInt1);
        int[] array;
        boolean bool = false;
        for (int i = 0; i < SoundProfiles.length; i++) {
            array = SoundProfiles[i];
            if (Arrays.toString(array).equals(activeProfile)) {
                ((WheelPicker) findViewById(R.id.wheel)).setSelection(i + 1);
                bool = true;
                break;
            }
        }
        if (!bool) ((WheelPicker) findViewById(R.id.wheel)).setSelection(0);
        UIHelper.removeEmptyCards((LinearLayout) findViewById(R.id.cardHolder));
        if (!new File(getScriptsDir(mActivity), HKMTools.ScriptUtils.SND_SETTINGS_SCRIPT_NAME).exists()) {
            mView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    saveAll(false);
                }
            }, 1000);
        }
    }

    public void saveAll(boolean fromUser) {
        HKMTools tools = HKMTools.getInstance();
        tools.clear();
        for (FauxSoundProperty property : properties) {
            property.setValue(property.readDisplayedValue());
        }
        createScript(tools.getRecentCommandsList());
        tools.flush();
        if (fromUser) {
            Toast.makeText(mActivity, R.string.message_action_successful, Toast.LENGTH_SHORT).show();
        }
        mContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh(false);
            }
        }, 100);
    }

    public void changeSetOnBootState(boolean state, boolean updateScript) {
        final boolean oldState = sharedPreferences.getBoolean(SET_SND_SETTINGS_ON_BOOT, false);
        sharedPreferences.edit().putBoolean(SET_SND_SETTINGS_ON_BOOT, state).apply();
        if (updateScript) saveAll(false);
        if (oldState != state) {
            Toast.makeText(mActivity, state ? R.string.message_set_on_boot_enabled : R.string.message_set_on_boot_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    private void createScript(List<String> commandList) {
        HKMTools.ScriptUtils.createScript(mActivity.getApplicationContext(), sharedPreferences, SET_SND_SETTINGS_ON_BOOT, SND_SETTINGS_SCRIPT_NAME, commandList);
    }

    private View findViewById(int id) {
        return mView.findViewById(id);
    }

    private View getContainerView() {
        return LayoutInflater.from(mActivity).inflate(R.layout.sound_ctrl_entry_layout, null, false);
    }

    @Override
    public int getTitleId() {
        return R.string.soundCtl;
    }
}
