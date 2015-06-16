package lb.themike10452.hellscorekernelmanagerl.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.NumberModifier;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiRootPathIntProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.PropertyUtils;
import lb.themike10452.hellscorekernelmanagerl.properties.interfaces.HKMPropertyInterface;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.SysfsLib;
import lb.themike10452.hellscorekernelmanagerl.utils.UIHelper;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SET_TCC_SETTINGS_ON_BOOT;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHARED_PREFS_ID;
import static lb.themike10452.hellscorekernelmanagerl.utils.HKMTools.ScriptUtils.TTC_SETTINGS_SCRIPT_NAME;
import static lb.themike10452.hellscorekernelmanagerl.utils.HKMTools.ScriptUtils.getScriptsDir;

/**
 * Created by Mike on 3/18/2015.
 */
public class TouchControl extends Fragment implements HKMFragment, View.OnClickListener {

    private static final String[] s2wModes = new String[]{"off", "s2w+s2s", "s2s"};
    private static final String[] dt2wZones = new String[]{"Disabled", "Center", "Full", "Bottom", "Top"};

    private static TouchControl instance;
    private static Activity mActivity;
    private static SharedPreferences sharedPreferences;
    private static View mView;

    private static MultiRootPathIntProperty dt2wProperty;
    private static MultiRootPathIntProperty s2wProperty;
    private static MultiRootPathIntProperty s2dProperty;
    private static MultiRootPathIntProperty twProperty;
    private static MultiRootPathIntProperty dt2wFeatherProperty;
    private static MultiRootPathIntProperty dt2wZoneProperty;
    private static MultiRootPathIntProperty twTimeoutProperty;
    private static HKMPropertyInterface[] properties;

    public TouchControl() {
        instance = this;
    }

    public static TouchControl getInstance() {
        return instance != null ? instance : new TouchControl();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        sharedPreferences = activity.getSharedPreferences(SHARED_PREFS_ID, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Switch setOnBootSwitch = ((Switch) menu.findItem(R.id.action_setOnBoot).getActionView().findViewById(R.id.sob_switch));
        boolean sobEnabled = new File(getScriptsDir(mActivity), TTC_SETTINGS_SCRIPT_NAME).canExecute();
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
            case R.id.action_apply:
                saveAll(true);
                return true;
            case R.id.action_refresh:
                refresh(true);
                return true;
            default:
                return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mView = inflater.inflate(R.layout.fragment_touch, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        initProperties();
        refresh(false);
    }

    private void initProperties() {
        twTimeoutProperty = new MultiRootPathIntProperty(findViewById(R.id.twTimeoutBtn), SysfsLib.WAKE_TOUCHWAKE_DELAY);
        dt2wFeatherProperty = new MultiRootPathIntProperty(findViewById(R.id.dt2wFeatherBtn), SysfsLib.WAKE_DT2W_FEATHER);
        dt2wZoneProperty = new MultiRootPathIntProperty(findViewById(R.id.dt2wZoneBtn), SysfsLib.WAKE_DT2W_1, SysfsLib.WAKE_DT2W_2) {
            @Override
            public void setDisplayedValue(String value) {
                if (value != null) {
                    setDisplayedValue(Integer.parseInt(value));
                } else {
                    super.setDisplayedValue(null);
                }
            }

            @Override
            public void setDisplayedValue(int value) {
                super.setDisplayedValue(dt2wZones[value]);
            }

            @Override
            public String readDisplayedValue() {
                return Integer.toString(HKMTools.indexOf(super.readDisplayedValue(), dt2wZones));
            }
        };

        dt2wProperty = new MultiRootPathIntProperty(findViewById(R.id.dt2wHolder), SysfsLib.WAKE_DT2W_1, SysfsLib.WAKE_DT2W_2);
        s2dProperty = new MultiRootPathIntProperty(findViewById(R.id.s2dHolder), SysfsLib.WAKE_S2D_PATH);
        twProperty = new MultiRootPathIntProperty(findViewById(R.id.twHolder), SysfsLib.WAKE_TOUCHWAKE);
        s2wProperty = new MultiRootPathIntProperty(findViewById(R.id.s2wHolder), SysfsLib.WAKE_S2W_PATH) {
            @Override
            public void setDisplayedValue(String value) {
                if (value != null) {
                    setDisplayedValue(Integer.parseInt(value));
                } else {
                    super.setDisplayedValue(null);
                }
            }

            @Override
            public void setDisplayedValue(int value) {
                super.setDisplayedValue(s2wModes[value]);
            }

            @Override
            public String readDisplayedValue() {
                return Integer.toString(HKMTools.indexOf(super.readDisplayedValue(), s2wModes));
            }
        };

        dt2wZoneProperty.FLAGS = PropertyUtils.FLAG_VIEW_COMBO;

        s2wProperty.setDisplayedValues(s2wModes);
        s2wProperty.setMin(0);
        s2wProperty.setMax(2);
        dt2wZoneProperty.setDisplayedValues(dt2wZones);
        dt2wZoneProperty.setMin(0);
        dt2wZoneProperty.setMax(dt2wZones.length - 1);

        dt2wZoneProperty.getTopAncestor().findViewById(R.id.noteBtn).setOnClickListener(this);

        properties = new HKMPropertyInterface[]{
                dt2wProperty,
                s2wProperty,
                twProperty,
                s2dProperty,
                dt2wZoneProperty,
                dt2wFeatherProperty,
                twTimeoutProperty
        };
    }

    public void refresh(final boolean fromUser) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                for (HKMPropertyInterface property : properties) {
                    property.refresh();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                UIHelper.removeEmptyCards((LinearLayout) findViewById(R.id.cardHolder));
                if (!new File(getScriptsDir(mActivity), HKMTools.ScriptUtils.TTC_SETTINGS_SCRIPT_NAME).exists()) {
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
                String value;
                if (property == dt2wZoneProperty) {
                    String dtv = dt2wProperty.readDisplayedValue();
                    String zv = dt2wZoneProperty.readDisplayedValue();
                    if (!dtv.equals("0") && !zv.equals("0")) {
                        value = zv;
                    } else {
                        value = dtv;
                    }
                } else {
                    value = property.readDisplayedValue();
                }
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

    @Override
    public void onClick(View v) {
        final PopupWindow window = getPopupWindow();
        final NumberModifier modifier = new NumberModifier(mActivity);

        if (v.getId() == R.id.noteBtn) {
            PopupWindow popupWindow = getPopupWindow();
            TextView textView = new TextView(mActivity);
            textView.setPadding(20, 20, 20, 20);
            textView.setText(getText(R.string.note_unsupported_options));
            textView.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
            popupWindow.setContentView(textView);
            popupWindow.setAnimationStyle(R.style.flyInAnimation);
            popupWindow.showAsDropDown(v, -textView.getMeasuredWidth(), 0);
            return;
        } else if (v == s2wProperty.getView()) {
            modifier.setMin(0);
            modifier.setMax(2);
            modifier.setDisplayedValues(s2wModes);
            modifier.setValue(Integer.parseInt(s2wProperty.readDisplayedValue()));
            modifier.setInputType(TypedValue.TYPE_NULL);
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    s2wProperty.setDisplayedValue(modifier.getSelectedPosition());
                }
            });
        } else if (v == dt2wZoneProperty.getView()) {
            String dtv = dt2wProperty.readDisplayedValue();
            modifier.setMin("1".equals(dtv) ? 1 : 0);
            modifier.setMax(dt2wZones.length - 1);
            modifier.setDisplayedValues(dt2wZones);
            modifier.setValue(Integer.parseInt(dt2wZoneProperty.readDisplayedValue()));
            modifier.setInputType(TypedValue.TYPE_NULL);
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    dt2wZoneProperty.setDisplayedValue(Integer.toString(modifier.getSelectedPosition()));
                }
            });
        } else {
            return;
        }

        modifier.setPadding(20, 20, 20, 20);
        modifier.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        window.setContentView(modifier);
        window.showAsDropDown(v, v.getMeasuredWidth() - modifier.getMeasuredWidth(), 0);
    }

    private void createScript(List<String> commandList) {
        HKMTools.ScriptUtils.createScript(mActivity.getApplicationContext(), sharedPreferences, SET_TCC_SETTINGS_ON_BOOT, TTC_SETTINGS_SCRIPT_NAME, commandList);
    }

    public void changeSetOnBootState(boolean state, boolean updateScript) {
        final boolean oldState = sharedPreferences.getBoolean(SET_TCC_SETTINGS_ON_BOOT, false);
        sharedPreferences.edit().putBoolean(SET_TCC_SETTINGS_ON_BOOT, state).apply();
        if (updateScript) saveAll(false);
        if (oldState != state) {
            Toast.makeText(mActivity, state ? R.string.message_set_on_boot_enabled : R.string.message_set_on_boot_disabled, Toast.LENGTH_SHORT).show();
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

    private View findViewById(int id) {
        return mView.findViewById(id);
    }

    @Override
    public int getTitleId() {
        return R.string.touchCtl;
    }
}
