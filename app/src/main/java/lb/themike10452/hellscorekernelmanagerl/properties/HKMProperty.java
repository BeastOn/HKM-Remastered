package lb.themike10452.hellscorekernelmanagerl.properties;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 2/23/2015.
 */
public class HKMProperty {

    protected View mContainer;

    public HKMProperty() {
        //required empty constructor
    }

    public String readDisplayedValue() {
        String value = null;
        if (mContainer instanceof Switch) {
            value = ((Switch) mContainer).isChecked() ? "1" : "0";
        } else {
            View disp = mContainer.findViewById(R.id.value);
            if (disp != null) {
                if (disp instanceof Switch) {
                    value = ((Switch) disp).isChecked() ? "1" : "0";
                } else if (disp instanceof TextView) {
                    try {
                        value = ((TextView) disp).getText().toString();
                    } catch (Exception ignored) {
                    }
                }
            } else {
                disp = mContainer.findViewById(R.id.mswitch);
                if (disp != null && disp instanceof Switch) {
                    value = ((Switch) disp).isChecked() ? "1" : "0";
                }
            }
        }
        return value;
    }
}
