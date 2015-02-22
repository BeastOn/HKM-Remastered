package lb.themike10452.hellscorekernelmanagerl.properties;

import android.util.Log;

/**
 * Created by Mike on 2/22/2015.
 */
public class MulticoreIntProperty extends intProperty {

    private String[] filePaths;

    public MulticoreIntProperty(String pathLook, int resId, int defaultValue) {
        super(String.format(pathLook, 0), resId, defaultValue);
        filePaths = new String[4];
        for (int i = 0; i < filePaths.length; i++) {
            filePaths[i] = String.format(pathLook, i);
        }
    }

    @Override
    public int setValue(Object value) {
        try {
            for (String path : filePaths)
                super.setValue((int) value, path);
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
