package lb.themike10452.hellscorekernelmanagerl;

import android.animation.Animator;
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
import android.view.ViewAnimationUtils;
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
import lb.themike10452.hellscorekernelmanagerl.CustomClasses.AnimatorListener;
import lb.themike10452.hellscorekernelmanagerl.fragments.CPUControl;
import lb.themike10452.hellscorekernelmanagerl.fragments.GPUControl;
import lb.themike10452.hellscorekernelmanagerl.fragments.HKMFragment;
import lb.themike10452.hellscorekernelmanagerl.fragments.LCDControl;
import lb.themike10452.hellscorekernelmanagerl.fragments.MiscControls;
import lb.themike10452.hellscorekernelmanagerl.fragments.Monitoring;
import lb.themike10452.hellscorekernelmanagerl.fragments.ProfileManager;
import lb.themike10452.hellscorekernelmanagerl.fragments.SoundControl;
import lb.themike10452.hellscorekernelmanagerl.fragments.TouchControl;
import lb.themike10452.hellscorekernelmanagerl.properties.PropertyUtils;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.SysfsLib;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.REFERENCE_TOKEN;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHARED_PREFS_ID;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHUTDOWN_TOKEN;

/**
 * Created by Mike on 2/21/2015.
 */
public class MainActivity extends Activity {

    private static final String KEY_ACTIVE_FRAGMENT = "active_fragment";
    private static final int ACTION_DRAWER_ITEM_PRESSED = 1 << 1;
    private static final int ACTION_INIT_SYSTEM_SCRIPT = 1 << 2;

    public static TransactionManager transactionManager;
    public static CloseActionCallback closeActionCallback;
    public static OnBackPressedCallBack onBackPressedCallBack;

    private ActionView mActionView;
    private DrawerLayout drawerLayout;
    private Fragment activeFragment;
    private ListView listView;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handle(msg);
        }
    };

    private int active_fragment_position;

    public static void setCloseActionCallback(CloseActionCallback callback) {
        closeActionCallback = callback;
    }

    public static void setOnBackPressedCallBack(OnBackPressedCallBack callback) {
        onBackPressedCallBack = callback;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        transactionManager = new TransactionManager(this, R.id.fragContainer);
        sharedPreferences = getSharedPreferences(SHARED_PREFS_ID, MODE_PRIVATE);
        PropertyUtils.init(this);

        assert getActionBar() != null;

        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setCustomView(R.layout.actionbar_layout);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setElevation(0);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        listView = (ListView) findViewById(R.id.left_drawer);
        int[] icons = new int[]{
                R.drawable.ic_cpu_control,
                R.drawable.ic_gpu_control,
                R.drawable.ic_lcd_control,
                R.drawable.ic_touch_control,
                R.drawable.ic_sound_control,
                R.drawable.ic_misc_control,
                R.drawable.ic_monitoring,
                R.drawable.ic_profile_mgr,
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
                    transactionManager.setDrawerEnabled(true);
                    if (closeActionCallback != null) {
                        closeActionCallback.onClose();
                    }
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

        if (savedInstanceState == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.dialog_message_reqRoot);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();

            progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    try {
                        if (!HKMTools.ScriptUtils.checkSystemScript(getApplicationContext())) {
                            mHandler.sendEmptyMessage(ACTION_INIT_SYSTEM_SCRIPT);
                        } else {
                            launch(0);
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            findViewById(R.id.barrier).setVisibility(View.GONE);
        }

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_ACTIVE_FRAGMENT, active_fragment_position);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        launch(savedInstanceState.getInt(KEY_ACTIVE_FRAGMENT, 0));
    }

    @Override
    public void onBackPressed() {
        if (onBackPressedCallBack != null) {
            onBackPressedCallBack.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    private void launch(int fragmentPos) {
        if (sharedPreferences.getInt(Settings.Constants.CORE_MAX, -1) == -1) {
            int coreMax;
            try {
                coreMax = Integer.parseInt(HKMTools.getInstance().readLineFromFile(SysfsLib.KERNEL_MAX));
            } catch (Exception e) {
                e.printStackTrace();
                coreMax = 3;
            }
            sharedPreferences.edit().putInt(Settings.Constants.CORE_MAX, coreMax).apply();
        }

        if (findViewById(R.id.barrier).getVisibility() == View.VISIBLE) reveal();

        HKMFragment fragment = getFragmentByPosition(fragmentPos);

        if (fragment == null) fragment = getFragmentByPosition(0);

        transactionManager.performTransaction(fragment, false, true, null);
        transactionManager.setActionBarTitle(getString(fragment.getTitleId()));

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

    private void reveal() {
        final View barrier = findViewById(R.id.barrier);
        Animator animator = ViewAnimationUtils.createCircularReveal(barrier, barrier.getWidth() / 2, barrier.getHeight() / 2, Math.max(barrier.getHeight(), barrier.getWidth()), 0f);
        animator.setDuration(600);
        animator.setStartDelay(200);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
        animator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                barrier.setVisibility(View.GONE);
            }
        });
    }

    private HKMFragment getFragmentByPosition(int pos) {
        active_fragment_position = pos;
        switch (pos) {
            case 0:
                return CPUControl.getInstance();
            case 1:
                return GPUControl.getInstance();
            case 2:
                return LCDControl.getInstance();
            case 3:
                return TouchControl.getInstance();
            case 4:
                return SoundControl.getInstance();
            case 5:
                return MiscControls.getInstance();
            case 6:
                return Monitoring.getInstance();
            case 7:
                return ProfileManager.getInstance();
            default:
                return null;
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
                transactionManager.performTransaction(getFragmentByPosition(msg.arg2), false, true);
                return;
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
                                    launch(0);
                                    Toast.makeText(getApplicationContext(),
                                            b ? R.string.message_action_successful : R.string.message_script_failed,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }.execute();
                        }
                    })
                    .setNegativeButton(R.string.button_later, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            launch(0);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    public class TransactionManager {
        private Activity mActivity;
        private int containerId;

        private TransactionManager(Activity activity, int redId) {
            mActivity = activity;
            containerId = redId;
        }

        @SafeVarargs
        public final void performTransaction(HKMFragment fragment, boolean addToBackTrace, boolean animate, @Nullable Pair<View, String>... sharedViews) {
            if (fragment != activeFragment) {
                FragmentManager manager = mActivity.getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                if (animate) {
                    if (activeFragment != null)
                        activeFragment.setExitTransition(new Explode().setInterpolator(new AccelerateInterpolator()));
                    ((Fragment) fragment).setEnterTransition(new Slide().setDuration(500));
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
                transaction.replace(containerId, (Fragment) fragment);
                transaction.commit();
                activeFragment = (Fragment) fragment;
            }
            invalidateOptionsMenu();
            setActionBarTitle(getString(fragment.getTitleId()));
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

    public interface CloseActionCallback {
        void onClose();
    }

    public interface OnBackPressedCallBack {
        void onBackPressed();
    }
}
