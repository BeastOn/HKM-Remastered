package lb.themike10452.hellscorekernelmanagerl.CustomAdapters;

import android.content.Context;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.NumberModifier;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.properties.VoltageProperty;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;

/**
 * Created by Mike on 2/27/2015.
 */
public class CPUVoltagesAdapter extends BaseAdapter {

    public static final long VDD_STEP = 12500;
    public static final long UV_MV_STEP = 10;

    public static int mode;

    private Context mContext;
    private View mContainer;
    private List<VoltageProperty> list;

    public CPUVoltagesAdapter(Context context, View containerView) {
        mContext = context;
        mContainer = containerView;
        list = new ArrayList<>();
        mode = -1;
    }

    public List<Pair<String, String>> readPairs() {
        List<Pair<String, String>> pairList = new ArrayList<>();
        mode = -1;
        File voltage_table;
        if ((voltage_table = new File(Library.CPU_VDD_LEVELS)).exists()) {
            mode = 1;
        } else if ((voltage_table = new File(Library.CPU_UV_MV_TABLE)).exists()) {
            mode = 2;
        } else {
            voltage_table = null;
        }
        if (mode != -1 && voltage_table != null) {
            List<String> rawData = HKMTools.getInstance().readFromFile(voltage_table);
            for (String line : rawData) {
                if (line.contains(":")) {
                    String[] couple = line.replace("mV", "").replace("mhz", "").split(":");
                    pairList.add(new Pair<>(couple[0].trim(), couple[1].trim()));
                }
            }
        }
        return pairList;
    }

    public void init() {
        list.clear();
        for (Pair<String, String> pair : readPairs()) {
            View container = LayoutInflater.from(mContext).inflate(R.layout.voltage_ctrl_layout, null, false);
            ((EditText) container.findViewById(R.id.value)).setInputType(TypedValue.TYPE_NULL);
            list.add(new VoltageProperty(pair.first, pair.second, container));
        }
    }

    public void show() {
        mContainer.post(new Runnable() {
            @Override
            public void run() {
                ((LinearLayout) mContainer).removeAllViews();
                for (int i = 0; i < getCount(); i++) {
                    ((LinearLayout) mContainer).addView(getView(i, null, null));
                }
            }
        });
    }

    private void recycle() {
        int i = 0;
        for (final Pair<String, String> pair : readPairs()) {
            final VoltageProperty property = list.get(i++);
            mContainer.post(new Runnable() {
                @Override
                public void run() {
                    property.setDisplayedValue(pair.first, pair.second);
                }
            });
        }
    }

    public void invalidate() {
        if (getCount() == 0) {
            init();
            show();
        } else {
            recycle();
        }
    }

    public void flush() {
        if (mode > 0 && list != null && list.size() > 0) {
            switch (mode) {
                case 1:
                    File vdd_table = new File(Library.CPU_VDD_LEVELS);
                    for (VoltageProperty property : list) {
                        HKMTools.getInstance().addCommand(
                                String.format("echo %s %s > %s",
                                        property.getTitle(),
                                        property.getVoltage(),
                                        vdd_table.getAbsolutePath()
                                ));
                    }
                    break;
                case 2:
                    File uv_mv_table = new File(Library.CPU_UV_MV_TABLE);
                    String params = "";
                    for (VoltageProperty property : list) {
                        params = params.concat(property.getVoltage()).concat(" ");
                    }
                    HKMTools.getInstance().addCommand(
                            String.format("echo %s > %s",
                                    params,
                                    uv_mv_table.getAbsolutePath()
                            ));
                    break;
            }
        }
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ((VoltageProperty) getItem(position)).getView().getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == -1) {
            final NumberModifier modifier = new NumberModifier(mContext);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (list != null && list.size() > 0) {
                        if (v == modifier.INCREMENT_BUTTON)
                            for (VoltageProperty property : list)
                                property.INC.performClick();
                        else
                            for (VoltageProperty property : list)
                                property.DEC.performClick();

                    }
                }
            };
            modifier.INCREMENT_BUTTON.setOnClickListener(listener);
            modifier.DECREMENT_BUTTON.setOnClickListener(listener);
            modifier.findViewById(R.id.value).setVisibility(View.GONE);
            return modifier;
        } else {
            return list.get(position).getView();
        }
    }
}
