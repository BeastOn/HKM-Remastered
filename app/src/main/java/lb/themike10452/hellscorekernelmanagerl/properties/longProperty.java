package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.Tools;

/**
 * Created by Mike on 2/22/2015.
 */
public class longProperty extends HKMProperty implements longPropertyInterface {

    public int FLAGS;
    protected int viewId;
    protected long DEFAULT_VALUE;
    protected String filePath;

    public longProperty(@NonNull String path, View container, long defaultValue) {
        DEFAULT_VALUE = defaultValue;
        filePath = path;
        mContainer = container;
        viewId = container.getId();
        FLAGS = 0;
    }

    @Override
    public long getValue() {
        try {
            return Long.parseLong(Tools.getInstance().readLineFromFile(filePath));
        } catch (Exception e) {
            return DEFAULT_VALUE;
        }
    }

    @Override
    public int setValue(String value) {
        try {
            return setValue(Long.parseLong(value), filePath);
        } catch (ClassCastException e) {
            Log.e("TAG", e.toString());
            return 1;
        }
    }

    protected int setValue(long value, String path) {
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
        long value;
        if (_value instanceof String) {
            value = Long.parseLong((String) _value);
        } else {
            value = (long) _value;
        }
        if (value == DEFAULT_VALUE) {
            mContainer.setVisibility(View.GONE);
        } else {
            View disp = mContainer.findViewById(R.id.value);
            if (disp instanceof TextView)
                ((TextView) disp).setText(Long.toString(value));
        }
    }

}
