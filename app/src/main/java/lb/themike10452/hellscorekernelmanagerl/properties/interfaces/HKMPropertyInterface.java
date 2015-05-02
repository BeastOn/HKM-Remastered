package lb.themike10452.hellscorekernelmanagerl.properties.interfaces;

import android.view.View;

/**
 * Created by Mike on 2/22/2015.
 */
public interface HKMPropertyInterface {
    String getFilePath();

    View getTopAncestor();

    int getViewId();

    View getView();

    int getFlags();

    boolean hasFlag(int flag);

    boolean isVisible();

    String getValue();

    void setValue(String value);

    String readDisplayedValue();

    void setDisplayedValue(String value);

    void refresh();
}
