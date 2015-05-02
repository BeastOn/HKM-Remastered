package lb.themike10452.hellscorekernelmanagerl.properties;

import android.util.Log;
import android.view.View;

/**
 * Created by Mike on 2/22/2015.
 */
public class MultiCoreLongProperty extends longProperty {
    private String[] filePaths;

    public MultiCoreLongProperty(String pathLook, View container) {
        super(String.format(pathLook, 0), container);
        filePaths = new String[4];
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
            Log.e("TAG", e.toString());
        }
    }
}
