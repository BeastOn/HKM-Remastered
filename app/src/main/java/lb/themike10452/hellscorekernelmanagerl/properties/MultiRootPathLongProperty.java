package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by Mike on 3/2/2015.
 */
public class MultiRootPathLongProperty extends longProperty {

    String[] filePaths;

    public MultiRootPathLongProperty(View container, int defaultValue, @NonNull String... paths) {
        super("", container, defaultValue);
        filePaths = paths;
    }

    @Override
    public long getValue() {
        if ("".equals(filePath)) {
            for (String path : filePaths) {
                long i = super.getValue(path);
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
