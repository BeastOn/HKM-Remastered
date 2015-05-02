package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.view.View;

import java.util.Arrays;

/**
 * Created by Mike on 2/23/2015.
 */
public class MultiRootPathStringProperty extends StringProperty {
    String[] filePaths;

    public MultiRootPathStringProperty(View container, @NonNull String... paths) {
        super("", container);
        filePaths = paths;
    }

    @Override
    public String getValue() {
        if ((PropertyUtils.FLAG_MULTIROOT_SIMUL & FLAGS) == PropertyUtils.FLAG_MULTIROOT_SIMUL) {
            String[] values = new String[filePaths.length];
            for (int i = 0; i < values.length; i++) {
                filePath = filePaths[i];
                values[i] = super.getValue();
            }
            return Arrays.toString(values);
        } else {
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
        }

        return null;
    }
}
