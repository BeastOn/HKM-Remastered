package lb.themike10452.hellscorekernelmanagerl.properties;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.Tools;

/**
 * Created by Mike on 2/22/2015.
 */
public class StringProperty extends HKMProperty implements StringPropertyInterface {

    public int FLAGS;
    protected String filePath;
    protected String DEFAULT_VALUE;
    protected int viewId;

    public StringProperty(String path, View container, String defaultValue) {
        filePath = path;
        DEFAULT_VALUE = defaultValue;
        mContainer = container;
        viewId = container.getId();
        FLAGS = 0;
    }

    @Override
    public String getValue() {
        return getValue(filePath);
    }

    protected String getValue(String path) {
        String value = Tools.getInstance().readLineFromFile(path);
        return value != null ? value : DEFAULT_VALUE;
    }

    @Override
    public int setValue(String value) {
        try {
            return setValue(value, filePath);
        } catch (ClassCastException e) {
            Log.e("TAG", e.toString());
            return 1;
        }
    }

    protected int setValue(String value, String path) {
        Tools.getInstance().exec("echo ".concat("\"" + value + "\"").concat(" > ").concat(path));
        return 0;
    }

    @Override
    public int getViewId() {
        return viewId;
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public int getFlags() {
        return FLAGS;
    }

    @Override
    public void setDisplayedValue(Object _value) {
        String value = _value.toString();
        if (DEFAULT_VALUE.equals(value)) {
            mContainer.setVisibility(View.GONE);
        } else {
            View disp = mContainer.findViewById(R.id.value);
            if (disp != null)
                if (disp instanceof TextView) {
                    ((TextView) disp).setText(value);
                }
        }
    }

}
