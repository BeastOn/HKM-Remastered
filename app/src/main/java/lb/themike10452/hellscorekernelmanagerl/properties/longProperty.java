package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by Mike on 2/22/2015.
 */
public class longProperty extends HKMProperty {

    public longProperty(@NonNull String path, View container) {
        filePath = path;
        mContainer = container;
        viewId = container.getId();
        FLAGS = 0;
    }

    public void setDisplayedValue(long value) {
        super.setDisplayedValue(Long.toString(value));
    }
}
