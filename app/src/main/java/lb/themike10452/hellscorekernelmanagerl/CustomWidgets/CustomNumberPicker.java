package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.lang.reflect.Field;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 2/23/2015.
 */
public class CustomNumberPicker extends NumberPicker {

    public CustomNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        Class<?> numberPickerClass = null;
        try {
            numberPickerClass = Class.forName("android.widget.NumberPicker");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Field selectionDivider = null;
        if (numberPickerClass != null)
            try {
                selectionDivider = numberPickerClass.getDeclaredField("mSelectionDivider");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

        if (selectionDivider != null)
            try {
                selectionDivider.setAccessible(true);
                selectionDivider.set(this, new ColorDrawable(getResources().getColor(R.color.colorAccent)));
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
        updateView(child);
    }

    private void updateView(View view) {
        if (view instanceof EditText) {
            ((EditText) view).setTextSize(14);
            ((EditText) view).setTextColor(getResources().getColor(R.color.primaryText));
        }
    }
}
