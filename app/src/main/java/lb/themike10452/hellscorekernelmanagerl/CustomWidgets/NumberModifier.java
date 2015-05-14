package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 2/27/2015.
 */
public class NumberModifier extends LinearLayout implements View.OnClickListener, View.OnLongClickListener {

    public Button INCREMENT_BUTTON, DECREMENT_BUTTON;
    private EditText VALUE_EDITTEXT;

    private String[] DISPLAYED_VALUES;
    private int SELECTED_POSITION;
    private int ADJUST_STEP = 1;
    private int MAX_VALUE;
    private int MIN_VALUE;

    public NumberModifier(Context context) {
        super(context);
        init(context);
    }

    public NumberModifier(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    void init(Context context) {
        SELECTED_POSITION = 0;
        MAX_VALUE = Integer.MAX_VALUE;
        MIN_VALUE = Integer.MIN_VALUE;
        LinearLayout model = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.modifier_layout, null, false);
        setOrientation(HORIZONTAL);
        INCREMENT_BUTTON = (Button) model.findViewById(R.id.inc);
        DECREMENT_BUTTON = (Button) model.findViewById(R.id.dec);
        VALUE_EDITTEXT = (EditText) model.findViewById(R.id.value);

        model.removeView(INCREMENT_BUTTON);
        model.removeView(DECREMENT_BUTTON);
        model.removeView(VALUE_EDITTEXT);

        addView(DECREMENT_BUTTON);
        addView(VALUE_EDITTEXT);
        addView(INCREMENT_BUTTON);

        INCREMENT_BUTTON.setOnClickListener(this);
        DECREMENT_BUTTON.setOnClickListener(this);
        INCREMENT_BUTTON.setOnLongClickListener(this);
        DECREMENT_BUTTON.setOnLongClickListener(this);
    }

    public void setAdjustStep(int step) {
        ADJUST_STEP = step;
    }

    public void setMax(int max) {
        MAX_VALUE = max;
    }

    public void setMin(int min) {
        MIN_VALUE = min;
    }

    public void setDisplayedValues(String[] values) {
        DISPLAYED_VALUES = values;
    }

    public void setValue(int value) {
        if (DISPLAYED_VALUES != null) {
            setValue(DISPLAYED_VALUES[SELECTED_POSITION = (value % DISPLAYED_VALUES.length)]);
        } else {
            setValue(Integer.toString(value));
        }
    }

    public void setValue(String value) {
        this.VALUE_EDITTEXT.setText(value);
    }

    public int getSelectedPosition() {
        return SELECTED_POSITION;
    }

    public String getValue() {
        Editable e = VALUE_EDITTEXT.getText();
        return e != null ? e.toString() : null;
    }

    public void setInputType(int type) {
        VALUE_EDITTEXT.setInputType(type);
    }

    @Override
    public void onClick(View v) {
        try {
            int currentValue;
            try {
                currentValue = Integer.parseInt(VALUE_EDITTEXT.getText().toString());
            } catch (Exception e) {
                currentValue = 0;
            }
            if (v == INCREMENT_BUTTON) {
                if (DISPLAYED_VALUES != null) {
                    if (SELECTED_POSITION < MAX_VALUE) {
                        setValue(SELECTED_POSITION + 1);
                    }
                } else {
                    if (currentValue < MAX_VALUE) {
                        setValue(Integer.toString(currentValue + ADJUST_STEP));
                    }
                }
            } else {
                if (DISPLAYED_VALUES != null) {
                    if (SELECTED_POSITION > MIN_VALUE) {
                        setValue(SELECTED_POSITION - 1);
                    }
                } else {
                    if (currentValue > MIN_VALUE) {
                        setValue(Long.toString(currentValue - ADJUST_STEP));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onLongClick(final View v) {
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                onClick(v);
                if (v.isPressed()) onLongClick(v);
            }
        }, 75);
        return true;
    }
}
