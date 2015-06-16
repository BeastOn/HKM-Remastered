package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;

/**
 * Created by Mike on 4/21/2015.
 */
public class PreferenceButton extends LinearLayout {
    public static final String xmlns = "http://schemas.android.com/apk/res/android";

    public PreferenceButton(Context context) {
        super(context, null, R.attr.clickableLayoutStyle);
        init(context, null);
    }

    public PreferenceButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.clickableLayoutStyle);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {
        setOrientation(LinearLayout.VERTICAL);
        final TextView titleTv = new TextView(context);
        titleTv.setId(R.id.title);
        titleTv.setTextAppearance(context, R.style.buttonTitleStyle);
        addView(titleTv);

        final TextView summaryTv = new TextView(context);
        summaryTv.setTextAppearance(context, R.style.buttonDetailStyle);
        addView(summaryTv);

        if (attrs == null || !"none".equals(attrs.getAttributeValue(xmlns, "value"))) {
            final TextView valueTv = new TextView(context);
            valueTv.setId(R.id.value);
            valueTv.setTextAppearance(context, R.style.buttonDetailStyle);
            addView(valueTv);
        } else if ("none".equals(attrs.getAttributeValue(xmlns, "summary"))) {
            int dp10 = HKMTools.dpToPx(context, 10);
            titleTv.setPadding(0, dp10, 0, dp10);
        }

        if (attrs != null) {
            int title = attrs.getAttributeResourceValue(xmlns, "title", -1);
            if (title != -1) {
                titleTv.setText(title);
            }
            int summary = attrs.getAttributeResourceValue(xmlns, "summary", -1);
            if (summary != -1) {
                summaryTv.setText(summary);
            } else {
                summaryTv.setVisibility(View.GONE);
            }
        } else {
            summaryTv.setVisibility(View.GONE);
        }
    }

    public void setTitle(int titleId) {
        ((TextView) findViewById(R.id.title)).setText(titleId);
    }

    public void setValue(String value) {
        try {
            ((TextView) findViewById(R.id.value)).setText(value);
        } catch (NullPointerException ignored) {
        }
    }
}
