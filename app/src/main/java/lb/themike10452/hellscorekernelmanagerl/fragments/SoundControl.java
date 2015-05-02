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

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.Settings;
import lb.themike10452.hellscorekernelmanagerl.properties.FauxSoundProperty;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SET_SND_SETTINGS_ON_BOOT;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHARED_PREFS_ID;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SND_CTL_LINK_LEFT_RIGHT;
import static lb.themike10452.hellscorekernelmanagerl.utils.HKMTools.ScriptUtils.SND_SETTINGS_SCRIPT_NAME;

/**
 * Created by Mike on 3/6/2015.
 */
public class SoundControl extends Fragment implements Observer {

    public static SoundControl instance;

    private FauxSoundProperty leftHeadphoneGainProperty;
    private FauxSoundProperty rightHeadphoneGainProperty;
    private FauxSoundProperty leftPowerAmpGainProperty;
    private FauxSoundProperty rightPowerAmpGainProperty;
    private FauxSoundProperty speakerGainProperty;
    private FauxSoundProperty micGainProperty;
    private FauxSoundProperty camcorderGain;
    private Activity mActivity;
    private LinearLayout mContainer;
    private SharedPreferences sharedPreferences;
    private View mView;
    private FauxSoundProperty[] properties;

    public SoundControl() {
        instance = this;
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
        Switch setOnBootSwitch = (Switch) menu.findItem(R.id.action_setOnBoot).getActionView().findViewById(R.id.sob_switch);
        boolean sobEnabled = sharedPreferences.getBoolean(SET_SND_SETTINGS_ON_BOOT, false);
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
            default:
                return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_sound, container, false);
        mContainer = (LinearLayout) findViewById(R.id.container);
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
        leftHeadphoneGainProperty = new FauxSoundProperty(Library.SOUND_HP_GAIN, getContainerView(), R.string.pref_hp_gain, -10, 10, FauxSoundProperty.MODE_LEFT);
        rightHeadphoneGainProperty = new FauxSoundProperty(Library.SOUND_HP_GAIN, getContainerView(), -1, -10, 10, FauxSoundProperty.MODE_RIGHT);
        leftPowerAmpGainProperty = new FauxSoundProperty(Library.SOUND_HP_PA_GAIN, getContainerView(), R.string.pref_pa_gain, -6, 6, FauxSoundProperty.MODE_LEFT_AMP);
        rightPowerAmpGainProperty = new FauxSoundProperty(Library.SOUND_HP_PA_GAIN, getContainerView(), -1, -6, 6, FauxSoundProperty.MODE_RIGHT_AMP);
        speakerGainProperty = new FauxSoundProperty(Library.SOUND_SPEAKER_GAIN, getContainerView(), R.string.pref_spk_gain, -10, 10, FauxSoundProperty.MODE_DUAL);
        micGainProperty = new FauxSoundProperty(Library.SOUND_MIC_GAIN, getContainerView(), R.string.pref_mic_gain, -10, 10, FauxSoundProperty.MODE_SINGLE);
        camcorderGain = new FauxSoundProperty(Library.SOUND_CAMMIC_GAIN, getContainerView(), R.string.pref_camc_gain, -10, 10, FauxSoundProperty.MODE_SINGLE);

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
                camcorderGain
        };
    }

    public void refresh(boolean fromUser) {
        for (FauxSoundProperty property : properties) {
            if (property != null) {
                property.setDisplayedValue(property.getValue());
            }
        }
    }

    public void saveAll(boolean fromUser) {
        HKMTools tools = HKMTools.getInstance();
        tools.getReady();
        for (FauxSoundProperty property : properties) {
            property.setValue(property.readDisplayedValue());
        }
        createScript(tools.getRecentCommandsList());
        tools.flush();
        if (fromUser) {
            Toast.makeText(mActivity, R.string.message_action_successful, Toast.LENGTH_SHORT).show();
        }
    }

    public void changeSetOnBootState(boolean enabled) {
        sharedPreferences.edit().putBoolean(SET_SND_SETTINGS_ON_BOOT, enabled).apply();
        saveAll(false);
        Toast.makeText(mActivity, enabled ? R.string.message_set_on_boot_enabled : R.string.message_set_on_boot_disabled, Toast.LENGTH_SHORT).show();
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
}
