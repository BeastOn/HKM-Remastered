package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lb.themike10452.hellscorekernelmanagerl.properties.interfaces.HKMPropertyInterface;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;

/**
 * Created by Mike on 2/23/2015.
 */
public class MultiLineValueProperty extends HKMProperty {

    public MultiLineValueProperty(View container, String path) {
        filePath = path;
        mContainer = container;
        viewId = container.getId();
        FLAGS = 0;
    }

    public List<String> getValueAsList() {
        return HKMTools.getInstance().readFromFile(filePath);
    }

    public void setValue(@NonNull List<String> values, List<String> prefixes, List<String> suffixes) {
        List<String> _values = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            String line = "";
            if (prefixes != null && prefixes.size() > i)
                line = line.concat(prefixes.get(i)).concat(" ");
            line = line.concat(values.get(i));
            if (suffixes != null && suffixes.size() > i)
                line = line.concat(" ").concat(suffixes.get(i));

            _values.add(line);
        }

        setValue(_values);
    }

    protected void setValue(List<String> lines) {
        if (lines != null && lines.size() > 0) {
            for (String line : lines) {
                super.setValue(line);
            }
        }
    }

    public void setDisplayedValue(List<String> value) {
        super.setDisplayedValue(value != null ? Arrays.toString(value.toArray()) : null);
    }
}
