package lb.themike10452.hellscorekernelmanagerl.CustomAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.NumberModifier;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.properties.CPUVoltageProperty;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;
import lb.themike10452.hellscorekernelmanagerl.utils.Tools;

/**
 * Created by Mike on 2/27/2015.
 */
public class VoltagesAdapter extends ArrayAdapter<CPUVoltageProperty> {

    public static final long VDD_STEP = 12500;

    private Context mContext;
    private View containerView;
    private List<CPUVoltageProperty> list;

    private int mode;

    public VoltagesAdapter(Context context, View container) {
        super(context, R.layout.modifier_layout, new CPUVoltageProperty[0]);
        mContext = context;
        containerView = container;
        list = new ArrayList<>();
        mode = -1;
    }

    private void init() {
        list.clear();
        File vdd_table = new File(Library.VDD_LEVELS);
        if (vdd_table.exists()) {
            mode = 1;
            List<String> rawData = Tools.getInstance().readFromFile(vdd_table);
            for (String line : rawData) {
                if (line.contains(":")) {
                    String[] couple = line.split(":");
                    couple[0] = couple[0].replace("mhz", "").trim();
                    couple[1] = couple[1].trim();

                    View container = LayoutInflater.from(mContext).inflate(R.layout.voltage_ctrl_layout, null, false);
                    list.add(new CPUVoltageProperty(couple[0], couple[1], container));
                }
            }
        }
    }

    private void recycle() {
        switch (mode) {
            case 1:
                File vdd_table = new File(Library.VDD_LEVELS);
                List<String> rawData = Tools.getInstance().readFromFile(vdd_table);
                int i = 0;
                for (String line : rawData) {
                    if (line.contains(":")) {
                        final CPUVoltageProperty property = list.get(i++);
                        final String[] couple = line.split(":");
                        couple[0] = couple[0].replace("mhz", "").trim();
                        couple[1] = couple[1].trim();
                        containerView.post(new Runnable() {
                            @Override
                            public void run() {
                                property.setDisplayedValue(couple[0], couple[1]);
                            }
                        });
                    }
                }
        }
    }

    public void invalidate() {
        if (getCount() == 0)
            init();
        else
            recycle();
    }

    public void flush() {
        if (mode > 0 && list != null && list.size() > 0) {
            switch (mode) {
                case 1:
                    File vdd_table = new File(Library.VDD_LEVELS);
                    for (CPUVoltageProperty property : list) {
                        Tools.getInstance().exec("echo " + property.getFrequency() + " " + property.getVoltage() + " > " + vdd_table.getAbsolutePath());
                    }
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
