package lb.themike10452.hellscorekernelmanagerl.properties;

import android.view.View;

/**
 * Created by Mike on 2/23/2015.
 */
public class PropertyUtils {
    public static final int FLAG_CPU_CORES              = 0x00000001;
    public static final int FLAG_CPU_CORES_ALLOW_ZERO   = 0x00000010;
    public static final int FLAG_VIEW_COMBO             = 0x00000100;
    public static final int FLAG_MULTIROOT_SIMUL        = 0x00001000;

    public static HKMPropertyInterface findProperty(HKMPropertyInterface[] properties, View v) {
        int id = v.getId();
        for (HKMPropertyInterface property : properties)
            if (property.getViewId() == id)
                return property;

        return null;
    }
}
