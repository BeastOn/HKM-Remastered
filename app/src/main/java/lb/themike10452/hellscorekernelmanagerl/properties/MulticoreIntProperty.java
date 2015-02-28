package lb.themike10452.hellscorekernelmanagerl.properties;

import android.util.Log;
import android.view.View;

/**
 * Created by Mike on 2/22/2015.
 */
public class MultiCoreIntProperty extends intProperty {

    private String[] filePaths;

    public MultiCoreIntProperty(String pathLook, View container, int defaultValue) {
        super(String.format(pathLook, 0), container, defaultValue);
        filePaths = new String[4];
        for (int i = 0; i < filePaths.length; i++) {
            filePaths[i] = String.format(pathLook, i);
        }
    }

    @Override
    public int setValue(String value) {
        try {
            for (String path : filePaths)
                super.setValue(Integer.parseInt(value), path);
            return 0;
        } catch (Exception e) {
            Log.e("TAG", e.toString());
            return 1;
        }
    }

    public String[] getFilePaths() {
        return filePaths;
    }
}
