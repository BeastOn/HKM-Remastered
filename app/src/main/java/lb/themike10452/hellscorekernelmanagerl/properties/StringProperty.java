package lb.themike10452.hellscorekernelmanagerl.properties;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.PopupWindow;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;

/**
 * Created by Mike on 2/22/2015.
 */
public class StringProperty extends HKMProperty implements View.OnClickListener, PopupWindow.OnDismissListener {

    private NumberPicker numberPicker;

    private String[] DISPLAYED_VALUES;

    public StringProperty(String path, View container) {
        filePath = path;
        mContainer = container;
        viewId = container.getId();
        FLAGS = 0;
        mContainer.setOnClickListener(this);
    }

    public void setDisplayedValues(String... values) {
        DISPLAYED_VALUES = values;
    }

    @Override
    public void onClick(View v) {
        if (DISPLAYED_VALUES != null) {
            View layout = LayoutInflater.from(PropertyUtils.getAppContext()).inflate(R.layout.simple_picker_layout, null, false);
            layout.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
            numberPicker = (NumberPicker) layout.findViewById(R.id.numberPicker);
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue(DISPLAYED_VALUES.length - 1);
            numberPicker.setDisplayedValues(DISPLAYED_VALUES);
            numberPicker.setValue(HKMTools.indexOf(readDisplayedValue(), DISPLAYED_VALUES));
            numberPicker.setWrapSelectorWheel(false);
            PopupWindow popupWindow = PropertyUtils.getPopupWindow();
            popupWindow.setContentView(layout);
            popupWindow.showAsDropDown(v, v.getMeasuredWidth() / 2 - layout.getMeasuredWidth() / 2, 0);
            popupWindow.setOnDismissListener(this);
        } else {
            Log.d("TAG", "woof");
        }
    }

    @Override
    public void onDismiss() {
        if (numberPicker != null && DISPLAYED_VALUES != null) {
            setDisplayedValue(DISPLAYED_VALUES[numberPicker.getValue()]);
        }
    }
}
