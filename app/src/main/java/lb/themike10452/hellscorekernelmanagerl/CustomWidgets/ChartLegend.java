package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;

/**
 * Created by Mike on 5/7/2015.
 */
public class ChartLegend extends LinearLayout {
    public ChartLegend(Context context) {
        this(context, null);
    }

    public ChartLegend(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ChartLegend(Context context, int color, int textId, String value) {
        this(context, null);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        int dp5 = HKMTools.dpToPx(context, 5);

        TextView textView = new TextView(context);
        textView.setTextAppearance(context, R.style.buttonDetailStyle);
        textView.setText(textId);
        textView.setPadding(dp5, 0, 0, 0);

        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams params = new LayoutParams(dp5, dp5);
        imageView.setLayoutParams(params);
        imageView.setBackgroundColor(color);

        addView(imageView);
        addView(textView);

        if (value != null) {
            textView.setText(textView.getText() + String.format(": %s", value));
        }
    }
}
