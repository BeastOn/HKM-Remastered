package lb.themike10452.hellscorekernelmanagerl.properties;

import android.view.View;

/**
 * Created by Mike on 2/22/2015.
 */
public class StringProperty extends HKMProperty {

    public StringProperty(String path, View container) {
        filePath = path;
        mContainer = container;
        viewId = container.getId();
        FLAGS = 0;
    }
}
