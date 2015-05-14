package lb.themike10452.hellscorekernelmanagerl.properties;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Switch;

import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.NumberModifier;
import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 2/22/2015.
 */
public class intProperty extends HKMProperty implements View.OnClickListener, PopupWindow.OnDismissListener {

    private NumberModifier modifier;
    private PopupWindow popupWindow;

    private String[] DISPLAYED_VALUES;
    private int ADJUST_STEP;
    private int MAX_VALUE;
    private int MIN_VALUE;

    public intProperty(@NonNull String path, View container) {
        this(path, container, true);
    }

    public intProperty(@NonNull String path, View container, boolean clickable) {
        filePath = path;
        mContainer = container;
        viewId = container.getId();
        FLAGS = 0;
        ADJUST_STEP = 1;
        MAX_VALUE = Integer.MAX_VALUE;
        MIN_VALUE = 0;
        if (clickable && (!(mContainer instanceof Switch) && mContainer.findViewById(R.id.mswitch) == null)) {
            mContainer.setOnClickListener(this);
        }
    }

    public void setDisplayedValue(int value) {
        super.setDisplayedValue(Integer.toString(value));
    }

    public void setAdjustStep(int step) {
        ADJUST_STEP = step;
    }

    public void setMin(int min) {
        MIN_VALUE = min;
    }

    public void setMax(int max) {
        MAX_VALUE = max;
    }

    public void setDisplayedValues(String[] values) {
        DISPLAYED_VALUES = values;
    }

    @Override
    public void onClick(View v) {
        String value = readDisplayedValue();
        int intValue = Integer.parseInt(value);
        if (modifier == null || popupWindow == null) {
            Context context = PropertyUtils.getAppContext();
            modifier = new NumberModifier(context);
            modifier.setPadding(20, 20, 20, 20);
            modifier.setDisplayedValues(DISPLAYED_VALUES);
            modifier.setMin(MIN_VALUE);
            modifier.setMax(MAX_VALUE);
            modifier.setAdjustStep(ADJUST_STEP);
            modifier.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            popupWindow = PropertyUtils.getPopupWindow();
            popupWindow.setContentView(modifier);
            popupWindow.setOnDismissListener(this);
        }
        modifier.setValue(intValue);
        popupWindow.showAsDropDown(v, v.getMeasuredWidth() - modifier.getMeasuredWidth(), 0);
    }

    @Override
    public void onDismiss() {
        if (modifier != null) {
            if (DISPLAYED_VALUES == null)
                setDisplayedValue(modifier.getValue());
            else
                setDisplayedValue(modifier.getSelectedPosition());
        }
    }
}
