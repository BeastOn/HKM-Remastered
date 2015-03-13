package lb.themike10452.hellscorekernelmanagerl.CustomAdapters;

import android.content.Context;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.NumberModifier;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.properties.CPUVoltageProperty;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;

/**
 * Created by Mike on 2/27/2015.
 */
public class VoltagesAdapter extends ArrayAdapter<CPUVoltageProperty> {

    public static final long VDD_STEP = 12500;
    public static final long UV_MV_STEP = 10;

    public static int mode;

    private Context mContext;
    private View containerView;
    private List<CPUVoltageProperty> list;

    public VoltagesAdapter(Context context, View container) {
        super(context, R.layout.modifier_layout, new CPUVoltageProperty[0]);
        mContext = context;
        containerView = container;
        list = new ArrayList<>();
        mode = -1;
    }

    public List<Pair<String, String>> readPairs() {
        List<Pair<String, String>> pairList = new ArrayList<>();
        mode = -1;
        File voltage_table;
        voltage_table = new File(Library.VDD_LEVELS);
        if (voltage_table.exists()) {
            mode = 1;
        } else {
            voltage_table = new File(Library.UV_MV_TABLE);
            if (voltage_table.exists()) {
                mode = 2;
            }
        }
        if (mode != -1) {
            List<String> rawData = HKMTools.getInstance().readFromFile(voltage_table);
            for (String line : rawData) {
                if (line.contains(":")) {
                    String[] couple = line.replace("mV", "").replace("mhz", "").split(":");
                    Pair<String, String> pair = new Pair<>(couple[0].trim(), couple[1].trim());
                    pairList.add(pair);
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
            list.add(new CPUVoltageProperty(pair.first, pair.second, container));
        }
    }

    public void show() {
        containerView.post(new Runnable() {
            @Override
            public void run() {
                ((LinearLayout) containerView).removeAllViews();
                for (int i = 0; i < getCount(); i++) {
                    ((LinearLayout) containerView).addView(getView(i, null, null));
                }
            }
        });
    }

    private void recycle() {
        int i = 0;
        for (final Pair<String, String> pair : readPairs()) {
            final CPUVoltageProperty property = list.get(i++);
            containerView.post(new Runnable() {
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
                    File vdd_table = new File(Library.VDD_LEVELS);
                    for (CPUVoltageProperty property : list) {
                        HKMTools.getInstance().addCommand(
                                String.format("echo %s %s > %s",
                                        property.getFrequency(),
                                        property.getVoltage(),
                                        vdd_table.getAbsolutePath()
                                ));
                    }
                    break;
                case 2:
                    File uv_mv_table = new File(Library.UV_MV_TABLE);
                    String params = "";
                    for (CPUVoltageProperty property : list) {
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
    public int getPosition(CPUVoltageProperty item) {
        return list != null ? list.indexOf(item) : -1;
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
                            for (CPUVoltageProperty property : list)
                                property.INC.performClick();
                        else
                            for (CPUVoltageProperty property : list)
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
