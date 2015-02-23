package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;

/**
 * Created by Mike on 2/23/2015.
 */
public class MultiRootPathIntProperty extends intProperty {

    String[] filePaths;

    public MultiRootPathIntProperty(int resId, int defaultValue, @NonNull String... paths) {
        super("", resId, defaultValue);
        filePaths = paths;
    }

    @Override
    public int getValue() {
        if ("".equals(filePath)) {
            for (String path : filePaths) {
                int i = super.getValue(path);
                if (i != DEFAULT_VALUE) {
                    filePath = path;
                    return i;
                }
            }
        } else {
            return super.getValue();
        }
        return DEFAULT_VALUE;
    }
}
