package lb.themike10452.hellscorekernelmanagerl.CustomAdapters;

import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.SysfsLib;

/**
 * Created by Mike on 5/14/2015.
 */
public class TimeInStateAdapter extends BaseAdapter {

    private ArrayList<String> mStates;
    private Context mContext;
    private ViewGroup mContainer;
    public long sum;

    public TimeInStateAdapter(Context context, ViewGroup container) {
        mContext = context;
        mContainer = container;
        mStates = new ArrayList<>();
        sum = 0;
    }

    public void update() {
        boolean firstUpdate = mStates.size() == 0;
        mStates.clear();
        mStates.add(mContext.getString(R.string.idle).concat(" ") + (SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()) / 10);
        mStates.addAll(HKMTools.getInstance().readFromFile(SysfsLib.MON_CPU_TIME_IN_STATE));
        sum = 0;

        for (String str : mStates) {
            StateFormat.format(str);
            sum += StateFormat.time;
        }

        ((TextView) mContainer.findViewById(R.id.totalTime)).setText(mContext.getString(R.string.uptime, format(sum / 100)));

        if (firstUpdate) {
            int count = getCount();
            for (int i = 0; i < count; i++) {
                mContainer.addView(getView(i, null, mContainer));
            }
        } else {
            int count = mContainer.getChildCount();
            for (int i = 1; i < count; i++) {
                updateView(mContainer.getChildAt(i), i - 1);
            }
        }
    }

    @Override
    public int getCount() {
        return mStates == null ? 0 : mStates.size();
    }

    @Override
    public Object getItem(int position) {
        return mStates == null ? null : mStates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.time_in_state_layout, null);
        }

        updateView(convertView, position);

        return convertView;
    }

    private void updateView(View convertView, int position) {
        StateFormat.format(mStates.get(position));

        TextView freq = (TextView) convertView.findViewById(R.id.freqDisplay);
        TextView perc = (TextView) convertView.findViewById(R.id.percentageDisplay);
        TextView time = (TextView) convertView.findViewById(R.id.timeDisplay);
        ProgressBar bar = (ProgressBar) convertView.findViewById(R.id.progressBar);

        freq.setText(StateFormat.freq);

        int percent = (int) ((StateFormat.time * 100) / sum);

        perc.setText(percent + "%");
        bar.setProgress(percent);
        time.setText(format(StateFormat.time / 100));

        if (StateFormat.time == 0) {
            convertView.setVisibility(View.GONE);
        } else {
            convertView.setVisibility(View.VISIBLE);
        }

        convertView.invalidate();
    }

    private String format(long time) {
        return String.format("%02dh %02dm %02ds",
                (int) ((time) / 3600),
                (int) (((time) / 60) % 60),
                (int) ((time) % 60)
        );
    }

    static class StateFormat {
        public static String freq;
        public static int time;

        public static void format(String entry) {
            freq = entry.split(" ")[0];
            time = Integer.parseInt(entry.split(" ")[1]);
        }
    }
}
