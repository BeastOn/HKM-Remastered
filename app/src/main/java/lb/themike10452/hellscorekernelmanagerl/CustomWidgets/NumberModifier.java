package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 2/27/2015.
 */
public class NumberModifier extends LinearLayout {
    public Button INCREMENT_BUTTON, DECREMENT_BUTTON;
    private Context context;
    private EditText value;

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
    }

    public void setValue(String value) {
        this.value.setText(value);
    }
}
