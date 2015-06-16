package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 5/16/2015.
 */
public class CardHeader extends LinearLayout {

    public static final String xmlns = "http://schemas.android.com/apk/res/android";

    public CardHeader(Context context) {
        this(context, null);
    }

    public CardHeader(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOrientation(VERTICAL);
        inflate(context, R.layout.card_header, this);
        if (attributeSet != null) {
            ((TextView) findViewById(R.id.title)).setText(attributeSet.getAttributeResourceValue(xmlns, "title", 0));
        }
    }
}
