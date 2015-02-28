package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.Tools;

/**
 * Created by Mike on 2/22/2015.
 */
public class intProperty extends HKMProperty implements intPropertyInterface {

    public int FLAGS;
    protected int DEFAULT_VALUE;
    protected int viewId;
    protected String filePath;

    public intProperty(@NonNull String path, View container, int defaultValue) {
        DEFAULT_VALUE = defaultValue;
        filePath = path;
        mContainer = container;
        viewId = container.getId();
        FLAGS = 0;
    }

    @Override
    public int getValue() {
        return getValue(filePath);
    }

    protected int getValue(String path) {
        try {
            return Integer.parseInt(Tools.getInstance().readLineFromFile(path));
        } catch (Exception e) {
            return DEFAULT_VALUE;
        }
    }

    @Override
    public int setValue(String value) {
        try {
            return setValue(Integer.parseInt(value), filePath);
        } catch (ClassCastException e) {
            Log.e("TAG", e.toString());
            return 1;
        }
    }

    protected int setValue(int value, String path) {
        Tools.getInstance().exec("echo ".concat("\"" + value + "\"").concat(" > ").concat(path));
        return 0;
    }

    @Override
    public int getViewId() {
        return viewId;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public int getFlags() {
        return FLAGS;
    }

    @Override
    public void setDisplayedValue(Object _value) {
        int value;
        if (_value instanceof String) {
            value = Integer.parseInt((String) _value);
        } else {
            value = (int) _value;
        }
        if (value == DEFAULT_VALUE) {
            mContainer.setVisibility(View.GONE);
        } else {
            {
                View disp = mContainer.findViewById(R.id.value);
                if (disp != null) {
                    if (disp instanceof Switch) {
                        ((Switch) disp).setChecked(value == 1);
                    } else if (disp instanceof TextView) {
                        ((TextView) disp).setText(Integer.toString(value));
                    }
                } else if (mContainer instanceof Switch) {
                    ((Switch) mContainer).setChecked(value == 1);
                    return;
                }
            }
            {
                View disp = mContainer.findViewById(R.id.mswitch);
                if (disp != null)
                    if (disp instanceof Switch)
                        ((Switch) disp).setChecked(value == 1);
            }
        }
    }

}
