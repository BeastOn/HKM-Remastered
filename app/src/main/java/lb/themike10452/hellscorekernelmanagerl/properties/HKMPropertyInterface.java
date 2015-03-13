package lb.themike10452.hellscorekernelmanagerl.properties;

import android.view.View;

/**
 * Created by Mike on 2/22/2015.
 */
public interface HKMPropertyInterface {
    String getFilePath();

    int setValue(String value);

    int getViewId();

    View getView();

    int getFlags();

    String readDisplayedValue();

    void setDisplayedValue(Object value);
}
