package lb.themike10452.hellscorekernelmanagerl.CustomAdapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Map;
import java.util.TreeMap;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.SysfsLib;

/**
 * Created by Mike on 5/8/2015.
 */
public class BatteryInfoAdapter {

    private static final Integer KEY_HEALTH = R.string.legend_health;
    private static final Integer KEY_CAPACITY = R.string.legend_capacity;
    private static final Integer KEY_STATUS = R.string.legend_status;
    private static final Integer KEY_CHARGE_TYPE = R.string.legend_charge_type;
    private static final Integer KEY_TEMP = R.string.legend_temp;

    private Context mContext;
    private ViewGroup mContainer;

    public BatteryInfoAdapter(Context context, ViewGroup container) {
        mContext = context;
        mContainer = container;
    }

    public void update() {
        new AsyncTask<Void, Void, Void>() {
            Map<Integer, String> batteryInfo;

            @Override
            protected void onPreExecute() {
                batteryInfo = new TreeMap<>();
            }

            @Override
            protected Void doInBackground(Void... params) {
                HKMTools tools = HKMTools.getInstance();
                batteryInfo.put(KEY_HEALTH, tools.readLineFromFile(SysfsLib.MON_BATTERY_HEALTH));
                batteryInfo.put(KEY_CAPACITY, tools.readLineFromFile(SysfsLib.MON_BATTERY_CAPACITY));
                batteryInfo.put(KEY_STATUS, tools.readLineFromFile(SysfsLib.MON_BATTERY_STATUS));
                batteryInfo.put(KEY_CHARGE_TYPE, tools.readLineFromFile(SysfsLib.MON_BATTERY_CHARGE_TYPE));
                batteryInfo.put(KEY_TEMP, tools.readLineFromFile(SysfsLib.MON_BATTERY_TEMP));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                ViewGroup group = (ViewGroup) findViewById(R.id.batteryInfoTextHolder);
                group.removeAllViews();

                String tmp = batteryInfo.get(KEY_HEALTH);
                if (tmp != null) {
                    ((TextView) findViewById(R.id.batteryHealthText)).setText(tmp);
                }

                tmp = batteryInfo.get(KEY_CAPACITY);
                if (tmp != null) {
                    try {
                        ((ProgressBar) findViewById(R.id.batteryCapacityProgress)).setProgress(Integer.parseInt(tmp));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                updateInfo(group, batteryInfo);
            }
        }.execute();
    }

    private void updateInfo(ViewGroup group, Map<Integer, String> map) {
        for (Integer i : map.keySet()) {
            String value = map.get(i);
            if (value != null) {
                if (i.intValue() == KEY_TEMP) {
                    try {
                        addText(group, String.format("%s: %s %s", getString(R.string.legend_temp), (Double.parseDouble(value) / 10), (char) 0x00B0 + "C"));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else {
                    addText(group, getString(i) + ": " + value);
                }
            }
        }
    }

    private void addText(ViewGroup group, String text) {
        TextView textView = new TextView(mContext);
        textView.setTextAppearance(mContext, R.style.smallTextStyle);
        textView.setText(text);
        group.addView(textView);
    }

    private String getString(int resId) {
        return mContext.getString(resId);
    }

    private View findViewById(int id) {
        return mContainer.findViewById(id);
    }
}
