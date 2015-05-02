package lb.themike10452.hellscorekernelmanagerl.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiRootPathStringProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.StringProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.interfaces.HKMPropertyInterface;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SET_MISC_SETTINGS_ON_BOOT;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHARED_PREFS_ID;

/**
 * Created by Mike on 5/3/2015.
 */
public class MiscControls extends Fragment {
    private static MiscControls instance;

    private Activity mActivity;
    private SharedPreferences sharedPreferences;
    private View mView;

    private StringProperty ioSchedProperty;
    private StringProperty readAheadProperty;
    private StringProperty tcpCongestProperty;

    private HKMPropertyInterface[] properties;

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
        ioSchedProperty = new MultiRootPathStringProperty(findViewById(R.id.ioschedulerBtn), Library.MISC_IO_SCHED);
        readAheadProperty = new MultiRootPathStringProperty(findViewById(R.id.readaheadBtn), Library.MISC_READ_AHEAD_BUFFER);
        tcpCongestProperty = new MultiRootPathStringProperty(findViewById(R.id.tcpcongestionBtn), Library.MISC_NET_TCP_CONGST);
        properties = new HKMPropertyInterface[]{
                ioSchedProperty,
                readAheadProperty,
                tcpCongestProperty
        };
    }

    public void refresh(final boolean fromUser) {
        for (HKMPropertyInterface property : properties) {
            property.refresh();
        }
    }

    public void changeSetOnBootState(boolean enabled) {
        sharedPreferences.edit().putBoolean(SET_MISC_SETTINGS_ON_BOOT, enabled).apply();
        //saveAll(false);
        Toast.makeText(mActivity, enabled ? R.string.message_set_on_boot_enabled : R.string.message_set_on_boot_disabled, Toast.LENGTH_SHORT).show();
    }

    private View findViewById(int id) {
        return mView.findViewById(id);
    }
}
