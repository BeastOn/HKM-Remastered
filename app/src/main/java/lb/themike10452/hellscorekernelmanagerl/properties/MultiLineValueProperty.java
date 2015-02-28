package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.Tools;

/**
 * Created by Mike on 2/23/2015.
 */
public class MultiLineValueProperty implements HKMPropertyInterface {

    public int FLAGS;
    protected int viewId;
    protected String DEFAULT_VALUE;
    protected String filePath;
    protected View mContainer;

    public MultiLineValueProperty(View container, String path, String defaultValue) {
        filePath = path;
        DEFAULT_VALUE = defaultValue;
        mContainer = container;
        viewId = container.getId();
        FLAGS = 0;
    }

    public List<String> getValue() {
        return getValue(filePath);
    }

    protected List<String> getValue(String path) {
        return Tools.getInstance().readFromFile(path);
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public int setValue(String value) {
        Tools.getInstance().exec("echo " + value + " > " + filePath);
        return 0;
    }

    public int setValue(@NonNull Object values, Object prefixes, Object suffixes) {
        List<String> valist = (List<String>) values;
        List<String> prelist = null;
        List<String> suflist = null;

        try {
            prelist = (List<String>) prefixes;
        } catch (Exception ignored) {
        }
        try {
            suflist = (List<String>) suffixes;
        } catch (Exception ignored) {
        }

        List<String> _values = new ArrayList<>();
        for (int i = 0; i < valist.size(); i++) {
            String line = "";
            if (prelist != null && prelist.size() > i)
                line = line.concat(prelist.get(i)).concat(" ");
            line = line.concat(valist.get(i));
            if (suflist != null && suflist.size() > i)
                line = line.concat(" ").concat(suflist.get(i));

            _values.add(line);
        }

        return setValue(_values, filePath);
    }

    protected int setValue(List<String> lines, String path) {
        if (lines != null && lines.size() > 0) {
            for (String line : lines) {
                Tools.getInstance().exec("echo ".concat(line).concat(" > ").concat(path));
            }
        }
        return 0;
    }

    @Override
    public int getViewId() {
        return viewId;
    }

    @Override
    public int getFlags() {
        return FLAGS;
    }

    @Override
    public String readDisplayedValue() {
        String value = null;
        if (mContainer instanceof Switch) {
            value = ((Switch) mContainer).isChecked() ? "1" : "0";
        } else {
            View disp = mContainer.findViewById(R.id.value);
            if (disp != null) {
                if (disp instanceof Switch) {
                    value = ((Switch) disp).isChecked() ? "1" : "0";
                } else if (disp instanceof TextView) {
                    try {
                        value = ((TextView) disp).getText().toString();
                    } catch (Exception ignored) {
                    }
                }
            } else {
                disp = mContainer.findViewById(R.id.mswitch);
                if (disp != null && disp instanceof Switch) {
                    value = ((Switch) disp).isChecked() ? "1" : "0";
                }
            }
        }
        return value;
    }

    @Override
    public void setDisplayedValue(Object _value) {
        List<String> value = (List<String>) _value;
        if (value == null || value.isEmpty()) {
            mContainer.setVisibility(View.GONE);
        } else {
            View disp = mContainer.findViewById(R.id.value);
            if (disp != null)
                if (disp instanceof TextView) {
                    if (mContainer instanceof TextView) {
                        ((TextView) mContainer).setText(Arrays.toString(value.toArray(new String[value.size()])));
                    } else {
                        View v = mContainer.findViewById(R.id.value);
                        if (v != null && v instanceof TextView) {
                            ((TextView) v).setText(Arrays.toString(value.toArray(new String[value.size()])));
                        }
                    }
                }
        }
    }
}
