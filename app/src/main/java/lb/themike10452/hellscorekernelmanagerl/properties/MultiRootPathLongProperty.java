package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by Mike on 3/2/2015.
 */
public class MultiRootPathLongProperty extends longProperty {
    String[] filePaths;

    public MultiRootPathLongProperty(View container, @NonNull String... paths) {
        super("", container);
        filePaths = paths;
    }

    @Override
    public String getValue() {
        if ("".equals(filePath)) {
            for (String path : filePaths) {
                filePath = path;
                String value = super.getValue();
                if (value != null) {
                    return value;
                }
            }
        } else {
            return super.getValue();
        }
        return null;
    }
}
