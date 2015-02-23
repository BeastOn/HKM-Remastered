package lb.themike10452.hellscorekernelmanagerl.properties;

import android.util.Log;

import lb.themike10452.hellscorekernelmanagerl.utils.Tools;

/**
 * Created by Mike on 2/22/2015.
 */
public class StringProperty implements StringPropertyInterface {

    public int FLAGS;
    private String filePath;
    private String DEFAULT_VALUE;
    private int viewId;

    public StringProperty(String path, int resId, String defaultValue) {
        filePath = path;
        DEFAULT_VALUE = defaultValue;
        viewId = resId;
        FLAGS = 0;
    }

    @Override
    public String getValue() {
        String value = Tools.getInstance().readLineFromFile(filePath);
        return value != null ? value : DEFAULT_VALUE;
    }

    @Override
    public int setValue(Object value) {
        try {
            return setValue((String) value, filePath);
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
}
