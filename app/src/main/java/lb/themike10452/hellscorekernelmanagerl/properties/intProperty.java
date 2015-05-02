package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by Mike on 2/22/2015.
 */
public class intProperty extends HKMProperty {

    public intProperty(@NonNull String path, View container) {
        filePath = path;
        mContainer = container;
        viewId = container.getId();
        FLAGS = 0;
    }

    public void setDisplayedValue(int value) {
        super.setDisplayedValue(Integer.toString(value));
    }
}
