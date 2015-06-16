package lb.themike10452.hellscorekernelmanagerl.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import lb.themike10452.hellscorekernelmanagerl.CustomAdapters.KCalAdapter;
import lb.themike10452.hellscorekernelmanagerl.CustomClasses.MakoColorProfile;
import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.ObservableHorizontalScrollView;
import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.ObservableScrollView;
import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.ObservableScrollViewInterface;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.Settings;
import lb.themike10452.hellscorekernelmanagerl.properties.MultiRootPathStringProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.PropertyUtils;
import lb.themike10452.hellscorekernelmanagerl.properties.StringProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.intProperty;
import lb.themike10452.hellscorekernelmanagerl.properties.interfaces.HKMPropertyInterface;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.SysfsLib;
import lb.themike10452.hellscorekernelmanagerl.utils.UIHelper;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SET_LCD_SETTINGS_ON_BOOT;
import static lb.themike10452.hellscorekernelmanagerl.utils.HKMTools.ScriptUtils.LCD_SETTINGS_SCRIPT_NAME;
import static lb.themike10452.hellscorekernelmanagerl.utils.HKMTools.ScriptUtils.getScriptsDir;

/**
 * Created by Mike on 3/29/2015.
 */
public class LCDControl extends Fragment implements HKMFragment, ObservableScrollView.CallBack, View.OnClickListener {

    private static final String PROFILES_CONTAINER_FILE = "mako_color_profiles";
    private static LCDControl instance;

    private static Activity mActivity;
    private static ArrayList<MakoColorProfile> profiles;
    private static KCalAdapter kCalAdapter;
    private static ObservableScrollView osv;
    private static ObservableHorizontalScrollView ohsv;
    private static SharedPreferences sharedPreferences;
    private static String activeProfile;
    private static View mView;

    private static StringProperty kgammaRedProperty;
    private static StringProperty kgammaGreenProperty;
    private static StringProperty kgammaBlueProperty;
    private static intProperty expBrightnessProperty;
    private static intProperty maxBrightnessProperty;
    private static intProperty minBrightnessProperty;

    private static HKMPropertyInterface[] properties;

    private static int lockYPosition;

    public LCDControl() {
        instance = this;
    }

    public static LCDControl getInstance() {
        return instance != null ? instance : new LCDControl();
    }

    @Override
    public void scrollChanged(ObservableScrollViewInterface v, int l, final int t, int oldl, int oldt) {
        View bg = mView.findViewById(R.id.slider);
        Rect rect = new Rect();
        bg.getLocalVisibleRect(rect);
        float f = (float) (rect.top / 1.5);
        bg.setY(f < 0 ? -f : f);
        boolean touched = osv.isTouched() || ohsv.isTouched();
        if (!v.isAnimating() && !touched && t < lockYPosition) {
            v.scrollTo(0, lockYPosition);
        }
    }

    @Override
    public void touchChanged(ObservableScrollViewInterface v, boolean isTouched) {
        if (v != null && !isTouched) {
            if (v.getScrollY() < lockYPosition) {
                int i = v.getScrollY();
                if (i < lockYPosition & i > lockYPosition / 3) {
                    v.scrollTo(lockYPosition);
                } else {
                    v.scrollTo(0);
                }
            }
        }
    }

    @Override
    public void onAnimationStart() {
    }

    @Override
    public void onAnimationEnd() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        sharedPreferences = activity.getSharedPreferences(Settings.Constants.SHARED_PREFS_ID, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final Switch setOnBootSwitch = ((Switch) menu.findItem(R.id.action_setOnBoot).getActionView().findViewById(R.id.sob_switch));
        final boolean sobEnabled = new File(getScriptsDir(mActivity), LCD_SETTINGS_SCRIPT_NAME).canExecute();
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
        mView = inflater.inflate(R.layout.fragment_lcd, container, false);
        osv = (ObservableScrollView) mView;
        ohsv = (ObservableHorizontalScrollView) findViewById(R.id.hsv);

        findViewById(R.id.activeProfileClickable).setOnClickListener(this);
        findViewById(R.id.addProfileClickable).setOnClickListener(this);
        findViewById(R.id.saveProfileBtn).setOnClickListener(this);
        findViewById(R.id.delProfileBtn).setOnClickListener(this);

        osv.setCallBack(this);
        ohsv.setCallBack(this);

        mView.postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.osv).scrollTo(0, lockYPosition = findViewById(R.id.slider).getBottom());
            }
        }, 10);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initColorProfiles(false);
                    detectActiveProfile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        initProperties();
        refresh(false);
        mView.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((ImageView) findViewById(R.id.preview1)).setImageResource(R.drawable.palette_1);
                ((ImageView) findViewById(R.id.preview2)).setImageResource(R.drawable.palette_2);
            }
        }, 1000);
    }

    private void initProperties() {
        kCalAdapter = new KCalAdapter(findViewById(R.id.pref_kcal), SysfsLib.LCD_KGAMMA_KCAL);
        kgammaRedProperty = new MultiRootPathStringProperty(findViewById(R.id.kgamma_r), SysfsLib.LCD_KGAMMA_RED, SysfsLib.LCD_KGAMMA_R);
        kgammaGreenProperty = new MultiRootPathStringProperty(findViewById(R.id.kgamma_g), SysfsLib.LCD_KGAMMA_GREEN, SysfsLib.LCD_KGAMMA_G);
        kgammaBlueProperty = new MultiRootPathStringProperty(findViewById(R.id.kgamma_b), SysfsLib.LCD_KGAMMA_BLUE, SysfsLib.LCD_KGAMMA_B);
        maxBrightnessProperty = new intProperty(SysfsLib.LCD_MAX_BRIGHTNESS, findViewById(R.id.maxBrightness));
        minBrightnessProperty = new intProperty(SysfsLib.LCD_MIN_BRIGHTNESS, findViewById(R.id.minBrightness));
        expBrightnessProperty = new intProperty(SysfsLib.LCD_BRIGHTNESS_MODE, findViewById(R.id.expBr_switch)) {
            @Override
            public void setDisplayedValue(String value) {
                if (value != null) {
                    if (value.equals("1")) value = "0";
                    else if (value.equals("0")) value = "1";
                }
                super.setDisplayedValue(value);
            }

            @Override
            public String readDisplayedValue() {
                String value = super.readDisplayedValue();
                if (value != null) {
                    if (value.equals("1")) value = "0";
                    else if (value.equals("0")) value = "1";
                }
                return value;
            }
        };

        kgammaRedProperty.FLAGS = PropertyUtils.FLAG_VIEW_COMBO;
        kgammaGreenProperty.FLAGS = PropertyUtils.FLAG_VIEW_COMBO;
        kgammaBlueProperty.FLAGS = PropertyUtils.FLAG_VIEW_COMBO;

        maxBrightnessProperty.setMax(114);
        minBrightnessProperty.setMax(114);

        properties = new HKMPropertyInterface[]{
                kgammaRedProperty,
                kgammaGreenProperty,
                kgammaBlueProperty,
                expBrightnessProperty,
                maxBrightnessProperty,
                minBrightnessProperty
        };
    }

    private void initColorProfiles(boolean recreate) throws Exception {
        File src = new File(mActivity.getFilesDir() + File.separator + PROFILES_CONTAINER_FILE);
        if (!src.exists() || recreate) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(mActivity.getAssets().open(PROFILES_CONTAINER_FILE)));
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(src));
            profiles = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 1) {
                    profiles.add(new MakoColorProfile(line));
                }
            }
            reader.close();
            out.writeObject(profiles);
            out.close();
        } else {
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(src));
                Object obj = in.readObject();
                if (obj instanceof ArrayList) {
                    profiles = (ArrayList) obj;
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
                initColorProfiles(true);
            }
        }
    }

    public void refresh(final boolean fromUser) {
        new AsyncTask<Void, Void, Void>() {
            String[] values;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                values = new String[properties.length];
                if (fromUser) {
                    mView.clearFocus();
                    findViewById(R.id.extraContent).setVisibility(View.GONE);
                    ((EditText) findViewById(R.id.newProfileName)).setText("");
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                for (int i = 0; i < properties.length; i++) {
                    values[i] = properties[i].getValue();
                }
                kCalAdapter.reload();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                for (int i = 0; i < properties.length; i++) {
                    properties[i].setDisplayedValue(values[i]);
                }
                detectActiveProfile();
                UIHelper.removeEmptyCards((LinearLayout) findViewById(R.id.cardHolder));
                if (!new File(getScriptsDir(mActivity), HKMTools.ScriptUtils.LCD_SETTINGS_SCRIPT_NAME).exists()) {
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

    public void saveAll(final boolean fromUser) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
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

                kCalAdapter.flush();

                createScript(tools.getRecentCommandsList());
                tools.flush();
                tools.run("echo 1 > " + SysfsLib.LCD_KGAMMA_APPLY);
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

    public void changeSetOnBootState(boolean state, boolean updateScript) {
        final boolean oldState = sharedPreferences.getBoolean(SET_LCD_SETTINGS_ON_BOOT, false);
        sharedPreferences.edit().putBoolean(SET_LCD_SETTINGS_ON_BOOT, state).apply();
        if (updateScript) saveAll(false);
        if (oldState != state) {
            Toast.makeText(mActivity, state ? R.string.message_set_on_boot_enabled : R.string.message_set_on_boot_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    private void createScript(List<String> commandList) {
        HKMTools.ScriptUtils.createScript(mActivity.getApplicationContext(), sharedPreferences, SET_LCD_SETTINGS_ON_BOOT, LCD_SETTINGS_SCRIPT_NAME, commandList);
    }

    private MakoColorProfile packProfile(String alias) {
        return new MakoColorProfile(
                alias,
                kgammaRedProperty.readDisplayedValue(),
                kgammaGreenProperty.readDisplayedValue(),
                kgammaBlueProperty.readDisplayedValue(),
                kCalAdapter.readDisplayedValues()
        );
    }

    private boolean dumpProfilesToDisk(String alias) {
        if (alias != null) {
            MakoColorProfile profile = packProfile(alias);
            profile.isUserProfile = true;
            profiles.add(profile);
        }
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(mActivity.getFilesDir() + File.separator + PROFILES_CONTAINER_FILE));
            outputStream.writeObject(profiles);
            outputStream.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void detectActiveProfile() {
        try {
            if (profiles == null) return;
            activeProfile = getString(R.string.profile_custom);
            MakoColorProfile active = packProfile(activeProfile);
            for (MakoColorProfile profile : profiles) {
                if (profile.compareTo(active) == 0) {
                    activeProfile = profile.getAlias();
                    break;
                }
            }
            setActiveProfileAlias(activeProfile);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void setActiveProfileAlias(String alias) {
        ((TextView) findViewById(R.id.activeProfileClickable).findViewById(R.id.value)).setText(alias);
    }

    private void setActiveProfile(MakoColorProfile profile) {
        kCalAdapter.setDisplayedValues(profile.getRGB());
        kgammaRedProperty.setDisplayedValue(profile.getGammaR());
        kgammaGreenProperty.setDisplayedValue(profile.getGammaG());
        kgammaBlueProperty.setDisplayedValue(profile.getGammaB());
        setActiveProfileAlias(profile.getAlias());
    }

    private View findViewById(int id) {
        return mView.findViewById(id);
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.activeProfileClickable: {
                final String[] profs = new String[profiles.size()];
                int i = 0, j = -1;
                for (MakoColorProfile profile : profiles) {
                    profs[i] = profile.getAlias();
                    if (profs[i].equals(activeProfile)) {
                        j = i;
                    }
                    i++;
                }
                new AlertDialog.Builder(mActivity)
                        .setSingleChoiceItems(profs, j, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setActiveProfile(profiles.get(which));
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(true)
                        .show();
                break;
            }
            case R.id.addProfileClickable: {
                final View extra = v.findViewById(R.id.extraContent);
                extra.setVisibility(extra.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            }
            case R.id.saveProfileBtn: {
                final EditText editText = (EditText) findViewById(R.id.newProfileName);
                final String alias = editText.getText().toString();
                if (alias.length() > 0) {
                    if (dumpProfilesToDisk(alias)) {
                        Toast.makeText(mActivity, R.string.message_action_successful, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity, R.string.message_action_unsuccessful, Toast.LENGTH_SHORT).show();
                    }
                    final View extra = findViewById(R.id.extraContent);
                    extra.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            editText.setText("");
                            extra.setVisibility(View.GONE);
                            hideSoftKeyboard();
                        }
                    }, 500);
                } else {
                    Toast.makeText(mActivity, R.string.message_name_empty, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.delProfileBtn: {
                final ArrayList<String> customs = new ArrayList<>();
                int i = 0, j = -1;
                for (MakoColorProfile profile : profiles) {
                    if (profile.isUserProfile) {
                        customs.add(profile.getAlias());
                        if (profile.getAlias().equals(activeProfile)) {
                            j = i;
                        }
                        i++;
                    }
                }
                if (i > 0) {
                    new AlertDialog.Builder(mActivity)
                            .setSingleChoiceItems(customs.toArray(new String[i]), j, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String toDrop = customs.get(which);
                                    for (MakoColorProfile profile : profiles) {
                                        if (profile.getAlias().equals(toDrop)) {
                                            profiles.remove(profile);
                                            dumpProfilesToDisk(null);
                                            Toast.makeText(mActivity, R.string.message_action_successful, Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(true)
                            .show();
                } else {
                    Toast.makeText(mActivity, R.string.message_profiles_empty, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager manager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(mView.getWindowToken(), 0);
    }

    @Override
    public int getTitleId() {
        return R.string.lcdCtl;
    }
}
