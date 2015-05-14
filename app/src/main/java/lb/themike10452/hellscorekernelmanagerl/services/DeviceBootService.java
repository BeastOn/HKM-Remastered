package lb.themike10452.hellscorekernelmanagerl.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import lb.themike10452.hellscorekernelmanagerl.DeviceBroadcastReceiver;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;

/**
 * Created by Mike on 5/3/2015.
 */
public class DeviceBootService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        final MyHandler handler = new MyHandler(getApplicationContext());
        new Thread(new Runnable() {
            @Override
            public void run() {
                HKMTools.getInstance().initRootShell(handler);
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class MyHandler extends Handler {
        private final Context mContext;
        private final String scriptsDir;

        public MyHandler(Context context) {
            mContext = context;
            scriptsDir = HKMTools.ScriptUtils.getScriptsDir(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == HKMTools.FLAG_ROOT_STATE && msg.arg2 == 0) {
                HKMTools.getInstance().run("busybox run-parts -a --force " + scriptsDir);
                showNotification(mContext, R.string.notification_message_settings_applied);
            } else if (msg.arg2 == 1) {
                showNotification(mContext, R.string.dialog_message_failRoot);
            }
            mContext.stopService(new Intent(mContext, DeviceBootService.class));
        }

        private void showNotification(Context context, int msgId) {
            DeviceBroadcastReceiver.showNotification(context, msgId);
        }
    }
}
