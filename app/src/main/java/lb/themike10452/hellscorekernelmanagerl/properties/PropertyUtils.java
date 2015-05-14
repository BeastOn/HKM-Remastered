package lb.themike10452.hellscorekernelmanagerl.properties;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.PopupWindow;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.properties.interfaces.HKMPropertyInterface;

/**
 * Created by Mike on 2/23/2015.
 */
public class PropertyUtils {
    public static final int FLAG_CPU_CORES = 0x00000001;
    public static final int FLAG_CPU_CORES_ALLOW_ZERO = 0x00000010;
    public static final int FLAG_VIEW_COMBO = 0x00000100;
    public static final int FLAG_MULTIROOT_SIMUL = 0x00001000;

    private static Context appContext;

    public static void init(Context context) {
        appContext = context;
    }

    public static HKMPropertyInterface findProperty(HKMPropertyInterface[] properties, View v) {
        int id = v.getId();
        for (HKMPropertyInterface property : properties)
            if (property.getViewId() == id)
                return property;

        return null;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static PopupWindow getPopupWindow() {
        PopupWindow popupWindow = new PopupWindow(appContext);
        popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(1);
        popupWindow.setWidth(1000);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(appContext.getResources().getDrawable(R.drawable.popup_window_background));
        popupWindow.setClippingEnabled(true);
        popupWindow.setElevation(10f);
        return popupWindow;
    }
}
