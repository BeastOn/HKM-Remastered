package lb.themike10452.hellscorekernelmanagerl;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import eu.chainfire.libsuperuser.Shell;
import lb.themike10452.hellscorekernelmanagerl.fragments.CPUControl;
import lb.themike10452.hellscorekernelmanagerl.fragments.HKMFragment;
import lb.themike10452.hellscorekernelmanagerl.utils.Tools;

/**
 * Created by Mike on 2/21/2015.
 */
public class MainActivity extends Activity {

    public static final String ACTION_SHOW_TOUCH_BARRIER = "show_touch_barrier";
    public static final String ACTION_HIDE_TOUCH_BARRIER = "hide_touch_barrier";

    private ActionBarDrawerToggle drawerToggle;
    private BroadcastReceiver broadcastReceiver;
    private DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;
    private ListView listView;
    private ProgressDialog progressDialog;
    private HKMFragment activeFragment;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handle(msg);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            switch (item.getItemId()) {
                case R.id.action_refresh:
                    if (activeFragment != null)
                        activeFragment.refresh();
                    return true;
                case R.id.action_apply:
                    if (activeFragment != null)
                        activeFragment.saveAll();
                    return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getFragmentManager();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.dialog_message_reqRoot);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_SHOW_TOUCH_BARRIER.equals(intent.getAction())) {
                    findViewById(R.id.touchBarrier).setVisibility(View.VISIBLE);
                    findViewById(R.id.fragContainer).setEnabled(false);
                } else if (ACTION_HIDE_TOUCH_BARRIER.equals(intent.getAction())) {
                    findViewById(R.id.touchBarrier).setVisibility(View.GONE);
                    findViewById(R.id.fragContainer).setEnabled(true);
                }
            }
        };

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        listView = (ListView) findViewById(R.id.left_drawer);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_item, R.id.text, getResources().getStringArray(R.array.drawer_items)));
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(drawerToggle);

        assert getActionBar() != null;
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                launch();
            }
        });

        Tools tools = Tools.getInstance();
        tools.initRootShell(mHandler);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerToggle != null)
            drawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_HIDE_TOUCH_BARRIER);
        filter.addAction(ACTION_SHOW_TOUCH_BARRIER);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void launch() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragContainer, CPUControl.getInstance());
        transaction.commit();
        assert getActionBar() != null;
        getActionBar().setTitle(R.string.cpuCtl);
        activeFragment = CPUControl.getInstance();
    }

    private void handle(Message msg) {
        switch (msg.arg1) {
            case Tools.FLAG_ROOT_STATE:
                if (msg.arg2 == Shell.OnCommandResultListener.SHELL_RUNNING) {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                } else {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.findViewById(android.R.id.progress).setVisibility(View.GONE);
                        progressDialog.setMessage(getString(R.string.dialog_message_failRoot));
                    }
                }
                break;
        }
    }

    private void toast(int resId) {
        toast(getString(resId));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
