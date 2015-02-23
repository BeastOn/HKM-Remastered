package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.util.Log;

import lb.themike10452.hellscorekernelmanagerl.utils.Tools;

/**
 * Created by Mike on 2/22/2015.
 */
public class intProperty implements intPropertyInterface {

    protected int DEFAULT_VALUE;
    protected int viewId;
    protected String filePath;

    public intProperty(@NonNull String path, int resId, int defaultValue) {
        DEFAULT_VALUE = defaultValue;
        filePath = path;
        viewId = resId;
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
    public int setValue(Object value) {
        try {
            return setValue((int) value, filePath);
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
}
