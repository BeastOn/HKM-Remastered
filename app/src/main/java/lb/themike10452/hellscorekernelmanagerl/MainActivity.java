package lb.themike10452.hellscorekernelmanagerl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ArrayAdapter;
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
import lb.themike10452.hellscorekernelmanagerl.fragments.SoundControl;
import lb.themike10452.hellscorekernelmanagerl.fragments.TouchControl;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.REFERENCE_TOKEN;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHARED_PREFS_ID;

/**
 * Created by Mike on 2/21/2015.
 */
public class MainActivity extends Activity {

    public static final String ACTION_SHOW_TOUCH_BARRIER = "show_touch_barrier";
    public static final String ACTION_HIDE_TOUCH_BARRIER = "hide_touch_barrier";
    public static final String ACTION_SHOW_SYSTEM_SCRIPT_DIALOG = "show_sys_script_dialog";

    private static final int ACTION_DRAWER_ITEM_PRESSED = 1 << 1;
    private static final int ACTION_INIT_SYSTEM_SCRIPT = 1 << 2;

    private ActionView mActionView;
    private BroadcastReceiver broadcastReceiver;
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
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_item, R.id.text, getResources().getStringArray(R.array.drawer_items)));
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

        drawerLayout.setDrawerListener(new DrawerAdapter() {
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

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null) {
                    switch (intent.getAction()) {
                        case ACTION_SHOW_TOUCH_BARRIER:
                            findViewById(R.id.touchBarrier).setVisibility(View.VISIBLE);
                            findViewById(R.id.fragContainer).setEnabled(false);
                            break;
                        case ACTION_HIDE_TOUCH_BARRIER:
                            findViewById(R.id.touchBarrier).setVisibility(View.GONE);
                            findViewById(R.id.fragContainer).setEnabled(true);
                            break;
                        case ACTION_SHOW_SYSTEM_SCRIPT_DIALOG:
                            mHandler.sendEmptyMessage(ACTION_INIT_SYSTEM_SCRIPT);
                            break;
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_HIDE_TOUCH_BARRIER);
        filter.addAction(ACTION_SHOW_TOUCH_BARRIER);
        registerReceiver(broadcastReceiver, filter);

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
        sharedPreferences
                .edit()
                .putLong(REFERENCE_TOKEN, Calendar.getInstance().getTimeInMillis())
                .apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        HKMTools.getInstance().stopShell();
    }

    private void launch() {
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
                    //case 5:
                    //    transactionManager.performTransaction(MiscControls.getInstance(), false, true, getString(R.string.miscCtl));
                    //    return;
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
                                    dialog.setMessage("Installing ...");
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
                transaction.commitAllowingStateLoss();
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
