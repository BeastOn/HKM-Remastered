package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * Created by Mike on 2/23/2015.
 */
public class MultiRootPathStringProperty extends StringProperty {

    String[] filePaths;

    public MultiRootPathStringProperty(int resId, String defaultValue, @NonNull String... paths) {
        super("", resId, defaultValue);
        filePaths = paths;
    }

    @Override
    public String getValue() {
        if ((PropertyUtils.FLAG_MULTIROOT_SIMUL & FLAGS) == PropertyUtils.FLAG_MULTIROOT_SIMUL) {
            String[] values = new String[filePaths.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = super.getValue(filePaths[i]);
            }
            return Arrays.toString(values);
        } else {
            if ("".equals(filePath)) {
                String tmp;
                for (String path : filePaths) {
                    tmp = super.getValue(path);
                    if (!DEFAULT_VALUE.equals(tmp)) {
                        filePath = path;
                        return tmp;
                    }
                }
            } else {
                return super.getValue();
            }
        }

        return DEFAULT_VALUE;
    }
}
