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
public class NumberModifier extends LinearLayout implements View.OnClickListener {

    private static final int DEFAULT_STEP = 1;

    public Button INCREMENT_BUTTON, DECREMENT_BUTTON;
    private Context context;
    private EditText value;

    private String[] displayedValues;
    private int selectionIndex;
    private int intMax;
    private int intMin;

    public NumberModifier(Context context) {
        super(context);
        init(context);
    }

    public NumberModifier(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    void init(Context context) {
        this.context = context;
        selectionIndex = 0;
        intMax = Integer.MAX_VALUE;
        intMin = Integer.MIN_VALUE;
        LinearLayout model = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.modifier_layout, null, false);
        setOrientation(HORIZONTAL);
        INCREMENT_BUTTON = (Button) model.findViewById(R.id.inc);
        DECREMENT_BUTTON = (Button) model.findViewById(R.id.dec);
        value = (EditText) model.findViewById(R.id.value);

        model.removeView(INCREMENT_BUTTON);
        model.removeView(DECREMENT_BUTTON);
        model.removeView(value);

        addView(DECREMENT_BUTTON);
        addView(value);
        addView(INCREMENT_BUTTON);

        INCREMENT_BUTTON.setOnClickListener(this);
        DECREMENT_BUTTON.setOnClickListener(this);
    }

    public void setMax(int max) {
        intMax = max;
    }

    public void setMin(int min) {
        intMin = min;
    }

    public void setDisplayedValues(String[] values) {
        displayedValues = values;
    }

    public void setValue(int value) {
        if (displayedValues != null) {
            setValue(displayedValues[selectionIndex = (value % displayedValues.length)]);
        } else {
            setValue(Integer.toString(value));
        }
    }

    public void setValue(String value) {
        this.value.setText(value);
    }

    public int getSelectionIndex() {
        return selectionIndex;
    }

    public String getValue() {
        Editable e = value.getText();
        return e != null ? e.toString() : "";
    }

    public void setInputType(int type) {
        value.setInputType(type);
    }

    @Override
    public void onClick(View v) {
        try {
            int currentValue;
            try {
                currentValue = Integer.parseInt(value.getText().toString());
            } catch (Exception e) {
                currentValue = 0;
            }
            if (v == INCREMENT_BUTTON) {
                if (displayedValues != null) {
                    if (intMax == -1 || selectionIndex < intMax) {
                        setValue(selectionIndex + 1);
                    }
                } else {
                    if (intMax == -1 || currentValue < intMax) {
                        setValue(Integer.toString(currentValue + DEFAULT_STEP));
                    }
                }
            } else {
                if (displayedValues != null) {
                    if (selectionIndex > intMin) {
                        setValue(selectionIndex - 1);
                    }
                } else {
                    if (currentValue > intMin) {
                        setValue(Long.toString(currentValue - DEFAULT_STEP));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
