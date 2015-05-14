package lb.themike10452.hellscorekernelmanagerl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

import lb.themike10452.hellscorekernelmanagerl.services.DeviceBootService;

import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.REFERENCE_TOKEN;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHARED_PREFS_ID;
import static lb.themike10452.hellscorekernelmanagerl.Settings.Constants.SHUTDOWN_TOKEN;

/**
 * Created by Mike on 3/9/2015.
 */
public class DeviceBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION_APPLY_SETTINGS = "lb.themike10452.action.APPLY_SETTINGS";

    private Context mContext;

    public static void showNotification(Context context, int msgId) {
        Intent i = new Intent(context, DeviceBroadcastReceiver.class);
        i.setAction(ACTION_APPLY_SETTINGS);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_info_24px)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(msgId))
                .setContentIntent(pi)
                .setStyle(new Notification.BigTextStyle().bigText(context.getString(msgId)))
                .build();

        notification.flags = Notification.FLAG_AUTO_CANCEL;
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notification);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_ID, Context.MODE_PRIVATE);
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || ACTION_APPLY_SETTINGS.equals(action)) {
            long rt = preferences.getLong(REFERENCE_TOKEN, -1);
            long st = preferences.getLong(SHUTDOWN_TOKEN, -1);
            preferences.edit().putLong(REFERENCE_TOKEN, Calendar.getInstance().getTimeInMillis()).apply();
            if (st >= rt || ACTION_APPLY_SETTINGS.equals(action)) {
                applyBootSettings();
            } else {
                showNotification(mContext, R.string.notification_message_dirty_reboot);
            }
        } else if (Intent.ACTION_SHUTDOWN.equals(action)) {
            preferences.edit().putLong(SHUTDOWN_TOKEN, Calendar.getInstance().getTimeInMillis()).apply();
        }
    }

    private void applyBootSettings() {
        mContext.startService(new Intent(mContext, DeviceBootService.class));
    }
}
