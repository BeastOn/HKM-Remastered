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
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.util.List;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiRootPathIntProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiRootPathStringProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.StringProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.intProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.interfaces.HKMPropertyInterface;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;
import lb.themike10452.hellscorekernelmanagerl.utils.UIHelper;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SET_MISC_SETTINGS_ON_BOOT;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHARED_PREFS_ID;
import static lb.themike10452.hellscorekernelmanagerl.utils.HKMTools.ScriptUtils.MSC_SETTINGS_SCRIPT_NAME;

/**
 * Created by Mike on 5/3/2015.
 */
public class MiscControls extends Fragment {
    private static MiscControls instance;

    private static Activity mActivity;
    private static SharedPreferences sharedPreferences;
    private static View mView;

    private static StringProperty ioSchedProperty;
    private static intProperty readAheadProperty;
    private static StringProperty tcpCongestProperty;
    private static intProperty vibratorAmpProperty;
    private static intProperty msmThermalProperty;
    private static intProperty blxLimitProperty;
    private static intProperty dynFSyncProperty;
    private static intProperty fastChargeProperty;

    private static HKMPropertyInterface[] properties;
    private static String[] ioSchedulers;
    private static String[] congestAlgorithms;

    public MiscControls() {
        instance = this;
    }

    public static MiscControls getInstance() {
        return instance != null ? instance : new MiscControls();
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
        Switch setOnBootSwitch = ((Switch) menu.findItem(R.id.action_setOnBoot).getActionView().findViewById(R.id.sob_switch));
        boolean sobEnabled = sharedPreferences.getBoolean(SET_MISC_SETTINGS_ON_BOOT, false);
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
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_misc, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initProperties();
        refresh(false);
    }

    private void initProperties() {
        ioSchedProperty = new MultiRootPathStringProperty(findViewById(R.id.ioschedulerBtn), Library.MISC_IO_SCHED) {
            @Override
            public void setDisplayedValue(String value) {
                int x, y;
                if (value != null && ((x = value.indexOf("[")) < (y = value.indexOf("]")))) {
                    value = value.substring(x + 1, y);
                }
                super.setDisplayedValue(value);
            }
        };
        readAheadProperty = new intProperty(Library.MISC_READ_AHEAD_BUFFER, findViewById(R.id.readaheadBtn));
        tcpCongestProperty = new StringProperty(Library.MISC_NET_TCP_CONGST, findViewById(R.id.tcpcongestionBtn));
        vibratorAmpProperty = new intProperty(Library.MISC_VIBRATOR_AMP, findViewById(R.id.vibratorampBtn));
        msmThermalProperty = new MultiRootPathIntProperty(findViewById(R.id.thermallimitBtn), Library.MISC_MSM_THERMAL_1, Library.MISC_MSM_THERMAL_2);
        blxLimitProperty = new intProperty(Library.MISC_BLX_LIMIT, findViewById(R.id.blxlimitBtn));
        dynFSyncProperty = new intProperty(Library.MISC_DYN_FSYNC, findViewById(R.id.dynfsyncSwitch));
        fastChargeProperty = new intProperty(Library.MISC_FASTCHARGE, findViewById(R.id.fastchargeSwitch));

        vibratorAmpProperty.setMax(100);
        readAheadProperty.setMin(128);
        readAheadProperty.setAdjustStep(128);
        blxLimitProperty.setMax(100);

        properties = new HKMPropertyInterface[]{
                ioSchedProperty,
                readAheadProperty,
                tcpCongestProperty,
                vibratorAmpProperty,
                msmThermalProperty,
                blxLimitProperty,
                dynFSyncProperty,
                fastChargeProperty
        };
    }

    public void refresh(final boolean fromUser) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (HKMPropertyInterface property : properties) {
                    property.refresh();
                }
                if (ioSchedulers == null) {
                    fetchSchedulers();
                }
                ioSchedProperty.setDisplayedValues(ioSchedulers);

                if (congestAlgorithms == null) {
                    fetchCongestAlgorithms();
                }
                tcpCongestProperty.setDisplayedValues(congestAlgorithms);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                UIHelper.removeEmptyCards((LinearLayout) findViewById(R.id.cardHolder));
            }
        }.execute();
    }

    public void saveAll(final boolean fromUser) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                HKMTools tools = HKMTools.getInstance();
                tools.getReady();
                for (HKMPropertyInterface property : properties) {
                    if (property.isVisible()) {
                        property.setValue(property.readDisplayedValue());
                    }
                }
                createScript(tools.getRecentCommandsList());
                tools.flush();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (fromUser) {
                    Toast.makeText(mActivity.getApplicationContext(), R.string.message_action_successful, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public void changeSetOnBootState(boolean enabled) {
        sharedPreferences.edit().putBoolean(SET_MISC_SETTINGS_ON_BOOT, enabled).apply();
        saveAll(false);
        Toast.makeText(mActivity, enabled ? R.string.message_set_on_boot_enabled : R.string.message_set_on_boot_disabled, Toast.LENGTH_SHORT).show();
    }

    private void fetchSchedulers() {
        String str = ioSchedProperty.getValue();
        if (str != null) {
            str = str.replace("[", "").replace("]", "");
            ioSchedulers = str.split(" ");
        }
    }

    private void fetchCongestAlgorithms() {
        String str = HKMTools.getInstance().readLineFromFile(Library.MISC_NET_TCP_AVAILABLE);
        if (str != null) {
            congestAlgorithms = str.split(" ");
        }
    }

    private void createScript(List<String> commandList) {
        HKMTools.ScriptUtils.createScript(mActivity.getApplicationContext(), sharedPreferences, SET_MISC_SETTINGS_ON_BOOT, MSC_SETTINGS_SCRIPT_NAME, commandList);
    }

    private View findViewById(int id) {
        return mView.findViewById(id);
    }
}
