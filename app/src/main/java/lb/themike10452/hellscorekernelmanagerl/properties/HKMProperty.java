package lb.themike10452.hellscorekernelmanagerl.properties;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 2/23/2015.
 */
public class HKMProperty {

    public HKMProperty() {

    }

    public String readDisplayedValue(View parent) {
        String value = null;
        if (parent instanceof Switch) {
            value = ((Switch) parent).isChecked() ? "1" : "0";
        } else {
            View disp = parent.findViewById(R.id.value);
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
                disp = parent.findViewById(R.id.mswitch);
                if (disp != null && disp instanceof Switch) {
                    value = ((Switch) disp).isChecked() ? "1" : "0";
                }
            }
        }
        return value;
    }
}
