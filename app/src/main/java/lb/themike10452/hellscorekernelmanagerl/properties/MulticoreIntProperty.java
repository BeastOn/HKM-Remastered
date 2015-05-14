package lb.themike10452.hellscorekernelmanagerl.properties;

import android.util.Log;
import android.view.View;

/**
 * Created by Mike on 2/22/2015.
 */
public class MultiCoreIntProperty extends intProperty {
    private String[] filePaths;

    public MultiCoreIntProperty(String pathLook, int coreMax, View container) {
        super(String.format(pathLook, 0), container);
        filePaths = new String[coreMax + 1];
        for (int i = 0; i < filePaths.length; i++) {
            filePaths[i] = String.format(pathLook, i);
        }
    }

    @Override
    public void setValue(String value) {
        try {
            for (String path : filePaths)
                super.setValue(value, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
