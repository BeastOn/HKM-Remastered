package lb.themike10452.hellscorekernelmanagerl.CustomAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import java.util.List;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.properties.VoltageProperty;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;

/**
 * Created by Mike on 3/29/2015.
 */
public class GPUVoltagesAdapter extends BaseAdapter {

    private Context mContext;
    private LinearLayout mContainer;
    private String filePath;
    private VoltageProperty lowVoltage, normVoltage, highVoltage;
    private VoltageProperty[] properties;

    public GPUVoltagesAdapter(Context context, LinearLayout containerView) {
        mContext = context;
        mContainer = containerView;
        filePath = Library.GPU_MV_TABLE;
        init();
    }

    private void init() {
        try {
            List<String> values = HKMTools.getInstance().readFromFile(filePath);
            if (values != null) {
                lowVoltage = new VoltageProperty(getString(R.string.pref_vdd_dig_low), extract(values.get(0)), getVddEntryView());
                normVoltage = new VoltageProperty(getString(R.string.pref_vdd_dig_normal), extract(values.get(1)), getVddEntryView());
                highVoltage = new VoltageProperty(getString(R.string.pref_vdd_dig_high), extract(values.get(2)), getVddEntryView());
                properties = new VoltageProperty[]{lowVoltage, normVoltage, highVoltage};

                mContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        for (VoltageProperty property : properties)
                            mContainer.addView(property.getView());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recycle() {
        if (properties != null) {
            List<String> values = HKMTools.getInstance().readFromFile(filePath);
            if (values != null) {
                lowVoltage.setDisplayedValue(extract(values.get(0)));
                normVoltage.setDisplayedValue(extract(values.get(1)));
                highVoltage.setDisplayedValue(extract(values.get(2)));
            }
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        return properties[position];
    }

    @Override
    public long getItemId(int position) {
        return ((VoltageProperty) getItem(position)).getView().getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return properties[position].getView();
    }

    private String extract(String line) {
        return line.trim().split(" ")[0];
    }

    private View getVddEntryView() {
        return LayoutInflater.from(mContext).inflate(R.layout.voltage_ctrl_layout, null, false);
    }

    private String getString(int id) {
        return mContext.getString(id);
    }
}
