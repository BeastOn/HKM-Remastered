package lb.themike10452.hellscorekernelmanagerl;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import java.util.Calendar;

import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.REFERENCE_TOKEN;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHARED_PREFS_ID;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHUTDOWN_TOKEN;

/**
 * Created by Mike on 3/9/2015.
 */
public class DeviceBroadcastReceiver extends BroadcastReceiver {

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_ID, Context.MODE_PRIVATE);
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || "a".equals(action)) {
            long bt = preferences.getLong(REFERENCE_TOKEN, -99);
            long st = preferences.getLong(SHUTDOWN_TOKEN, -99);
            preferences.edit().putLong(REFERENCE_TOKEN, Calendar.getInstance().getTimeInMillis()).apply();
            if (bt == st) {
                applyBootSettings();
            } else {
                showNotification("Dirty shutdown detected");
            }
        } else if (Intent.ACTION_SHUTDOWN.equals(action)) {
            preferences.edit().putLong(SHUTDOWN_TOKEN, preferences.getLong(REFERENCE_TOKEN, -99)).apply();
        }
    }

    private void applyBootSettings() {
        final String scriptsDir = HKMTools.ScriptUtils.getScriptsDir(mContext);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == HKMTools.FLAG_ROOT_STATE && msg.arg2 == 0) {
                    HKMTools.getInstance().run("busybox run-parts -a --force " + scriptsDir);
                    showNotification("Settings applied successfully");
                } else if (msg.arg2 == 1) {
                    showNotification("Failed to get root access");
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                HKMTools.getInstance().initRootShell(handler);
            }
        }).start();
    }

    private void showNotification(String message) {
        Notification notification = new Notification.Builder(mContext)
                .setSmallIcon(R.drawable.ic_info_24px)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(message)
                .build();

        notification.flags = Notification.FLAG_AUTO_CANCEL;
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notification);
    }

}
