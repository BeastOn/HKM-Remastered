package lb.themike10452.hellscorekernelmanagerl.properties;

import android.view.View;

/**
 * Created by Mike on 2/22/2015.
 */
public interface HKMPropertyInterface {
    String getFilePath();

    int setValue(String value);

    int getViewId();

    int getFlags();

    String readDisplayedValue(View holder);

    void setDisplayedValue(Object value, View parent);
}
