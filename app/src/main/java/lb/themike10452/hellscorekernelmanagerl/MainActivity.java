package lb.themike10452.hellscorekernelmanagerl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

import at.markushi.ui.ActionView;
import at.markushi.ui.action.Action;
import at.markushi.ui.action.CloseAction;
import at.markushi.ui.action.DrawerAction;
import lb.themike10452.hellscorekernelmanagerl.CustomAdapters.DrawerAdapter;
import lb.themike10452.hellscorekernelmanagerl.fragments.CPUControl;
import lb.themike10452.hellscorekernelmanagerl.fragments.GPUControl;
import lb.themike10452.hellscorekernelmanagerl.fragments.LCDControl;
import lb.themike10452.hellscorekernelmanagerl.fragments.MiscControls;
import lb.themike10452.hellscorekernelmanagerl.fragments.Monitoring;
import lb.themike10452.hellscorekernelmanagerl.fragments.SoundControl;
import lb.themike10452.hellscorekernelmanagerl.fragments.TouchControl;
import lb.themike10452.hellscorekernelmanagerl.properties.PropertyUtils;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.REFERENCE_TOKEN;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHARED_PREFS_ID;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHUTDOWN_TOKEN;

/**
 * Created by Mike on 2/21/2015.
 */
public class MainActivity extends Activity {

    private static final int ACTION_DRAWER_ITEM_PRESSED = 1 << 1;
    private static final int ACTION_INIT_SYSTEM_SCRIPT = 1 << 2;

    private ActionView mActionView;
    private DrawerLayout drawerLayout;
    private Fragment activeFragment;
    private ListView listView;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;
    private mTransactionManager transactionManager;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handle(msg);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(SHARED_PREFS_ID, MODE_PRIVATE);
        PropertyUtils.init(this);

        assert getActionBar() != null;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.dialog_message_reqRoot);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setCustomView(R.layout.actionbar_layout);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        listView = (ListView) findViewById(R.id.left_drawer);
        int[] icons = new int[]{
                R.drawable.ic_cpu_control,
                R.drawable.ic_gpu_control,
                R.drawable.ic_lcd_control,
                R.drawable.ic_touch_control,
                R.drawable.ic_sound_control,
                R.drawable.ic_misc_control,
                R.drawable.ic_monitoring
        };
        listView.setAdapter(new DrawerAdapter(this, getResources().getStringArray(R.array.drawer_items), icons));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message message = new Message();
                message.arg1 = ACTION_DRAWER_ITEM_PRESSED;
                message.arg2 = position;
                mHandler.sendMessageDelayed(message, 300);
                drawerLayout.closeDrawer(Gravity.START);
            }
        });

        mActionView = (ActionView) getActionBar().getCustomView().findViewById(R.id.home);

        mActionView.setAction(new CloseAction(), false);
        mActionView.setAction(new DrawerAction(), false);
        mActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActionView.getAction() instanceof CloseAction) {
                    mActionView.setAction(new DrawerAction(), true);
                    transactionManager.popBackStack();
                } else if (drawerLayout.isDrawerOpen(Gravity.START)) {
                    drawerLayout.closeDrawer(Gravity.START);
                } else {
                    drawerLayout.openDrawer(Gravity.START);
                }
            }
        });

        drawerLayout.setDrawerListener(new DrawerAdapter(null, null, null) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mActionView.setAnimationProgress(1 - slideOffset);
            }
        });

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    launch();
                    if (!HKMTools.ScriptUtils.checkSystemScript(getApplicationContext())) {
                        mHandler.sendEmptyMessage(ACTION_INIT_SYSTEM_SCRIPT);
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                HKMTools.getInstance().initRootShell(mHandler);
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        long st = sharedPreferences.getLong(SHUTDOWN_TOKEN, -1);
        long rt = sharedPreferences.getLong(REFERENCE_TOKEN, -1);
        long time = Calendar.getInstance().getTimeInMillis();
        sharedPreferences
                .edit()
                .putLong(REFERENCE_TOKEN, time)
                .apply();

        //avoid false dirty reboot detection
        //if app was launched before set on boot triggers
        if (st > rt) {
            sharedPreferences
                    .edit()
                    .putLong(SHUTDOWN_TOKEN, time)
                    .apply();
        }
    }

    private void launch() {
        if (sharedPreferences.getInt(Settings.Constants.CORE_MAX, -1) == -1) {
            int coreMax;
            try {
                coreMax = Integer.parseInt(HKMTools.getInstance().readLineFromFile(Library.KERNEL_MAX));
            } catch (Exception e) {
                e.printStackTrace();
                coreMax = 3;
            }
            sharedPreferences.edit().putInt(Settings.Constants.CORE_MAX, coreMax).apply();
        }
        transactionManager = new mTransactionManager(this, R.id.fragContainer);
        Fragment fragment = CPUControl.getInstance(transactionManager);
        transactionManager.performTransaction(fragment, false, true, null);
        transactionManager.setActionBarTitle(getString(R.string.cpuCtl));
        if (!Build.DEVICE.equalsIgnoreCase("mako")) {
            String sample = getString(R.string.lcdCtl);
            for (int i = 0; i < listView.getChildCount(); i++) {
                if (sample.equals(((TextView) listView.getChildAt(i).findViewById(R.id.text)).getText())) {
                    ViewGroup.LayoutParams params = listView.getChildAt(i).getLayoutParams();
                    params.height = 1;
                    listView.getChildAt(i).setLayoutParams(params);
                }
            }
        }
    }

    private void handle(Message msg) {
        switch (msg.arg1) {
            case HKMTools.FLAG_ROOT_STATE:
                if (msg.arg2 == 0) {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                } else {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.findViewById(android.R.id.progress).setVisibility(View.GONE);
                        progressDialog.setMessage(getString(R.string.dialog_message_failRoot));
                    }
                }
                return;
            case ACTION_DRAWER_ITEM_PRESSED:
                switch (msg.arg2) {
                    case 0:
                        transactionManager.performTransaction(CPUControl.getInstance(transactionManager), false, true, getString(R.string.cpuCtl));
                        return;
                    case 1:
                        transactionManager.performTransaction(GPUControl.getInstance(), false, true, getString(R.string.gpuCtl));
                        return;
                    case 2:
                        transactionManager.performTransaction(LCDControl.getInstance(), false, true, getString(R.string.lcdCtl));
                        return;
                    case 3:
                        transactionManager.performTransaction(TouchControl.getInstance(), false, true, getString(R.string.touchCtl));
                        return;
                    case 4:
                        transactionManager.performTransaction(SoundControl.getInstance(), false, true, getString(R.string.soundCtl));
                        return;
                    case 5:
                        transactionManager.performTransaction(MiscControls.getInstance(), false, true, getString(R.string.miscCtl));
                        return;
                    case 6:
                        transactionManager.performTransaction(Monitoring.getInstance(), false, true, getString(R.string.monitoring));
                        return;
                }
        }
        if (msg.what == ACTION_INIT_SYSTEM_SCRIPT) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.dialog_message_bootScript)
                    .setPositiveButton(R.string.button_install, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new AsyncTask<Void, Void, Boolean>() {
                                ProgressDialog dialog;

                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    dialog = new ProgressDialog(MainActivity.this);
                                    dialog.setMessage(getString(R.string.dialog_message_installing));
                                    dialog.setIndeterminate(true);
                                    dialog.setCancelable(false);
                                    dialog.show();
                                }

                                @Override
                                protected Boolean doInBackground(Void... params) {
                                    try {
                                        HKMTools.ScriptUtils.createSystemScript(MainActivity.this);
                                        return true;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        return false;
                                    }
                                }

                                @Override
                                protected void onPostExecute(Boolean b) {
                                    super.onPostExecute(b);
                                    if (dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                    Toast.makeText(getApplicationContext(),
                                            b ? R.string.message_action_successful : R.string.message_script_failed,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }.execute();
                        }
                    })
                    .setNegativeButton(R.string.button_later, null)
                    .setCancelable(false)
                    .show();
        }
    }

    public class mTransactionManager {
        private Activity mActivity;
        private int containerId;

        public mTransactionManager(Activity activity, int redId) {
            mActivity = activity;
            containerId = redId;
        }

        @SafeVarargs
        public final void performTransaction(Fragment fragment, boolean addToBackTrace, boolean animate, @Nullable String actionBarTitle, @Nullable Pair<View, String>... sharedViews) {
            if (fragment != activeFragment) {
                FragmentManager manager = mActivity.getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                if (animate) {
                    if (activeFragment != null)
                        activeFragment.setExitTransition(new Explode().setInterpolator(new AccelerateInterpolator()));
                    fragment.setEnterTransition(new Slide().setDuration(500));
                }
                if (sharedViews != null) {
                    for (Pair<View, String> p : sharedViews) {
                        transaction.addSharedElement(p.first, p.second);
                    }
                }
                if (addToBackTrace) {
                    transaction.addToBackStack(fragment.toString());
                } else {
                    try {
                        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    } catch (IllegalStateException ignored) {
                    }
                }
                transaction.replace(containerId, fragment);
                transaction.commit();
                activeFragment = fragment;
            }
            invalidateOptionsMenu();
            if (actionBarTitle != null) {
                setActionBarTitle(actionBarTitle);
            }
        }

        public void popBackStack() {
            mActivity.getFragmentManager().popBackStack();
        }

        public void setActiveFragment(Fragment fragment) {
            activeFragment = fragment;
        }

        public void setDrawerIndicator(Action action) {
            mActionView.setAnimationDuration(800);
            mActionView.setAction(action);
        }

        public void setDrawerEnabled(boolean enabled) {
            drawerLayout.setDrawerLockMode(enabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        public void setActionBarTitle(String title) {
            assert mActivity.getActionBar() != null;
            ((TextView) mActivity.getActionBar().getCustomView().findViewById(R.id.actionbar_title)).setText(title);
        }
    }

}
