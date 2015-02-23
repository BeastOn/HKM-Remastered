package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.util.Log;

import lb.themike10452.hellscorekernelmanagerl.utils.Tools;

/**
 * Created by Mike on 2/22/2015.
 */
public class longProperty implements longPropertyInterface {

    public int FLAGS;
    private int viewId;
    private long DEFAULT_VALUE;
    private String filePath;

    public longProperty(@NonNull String path, int resId, long defaultValue) {
        DEFAULT_VALUE = defaultValue;
        filePath = path;
        viewId = resId;
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
    public int setValue(Object value) {
        try {
            return setValue((long) value, filePath);
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
}
