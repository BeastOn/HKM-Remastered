package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by Mike on 5/12/2015.
 */
public class GridLayout extends LinearLayout {

    private final LinearLayout column1;
    private final LinearLayout column2;
    private int childCount;

    public GridLayout(Context context) {
        this(context, null);
    }

    public GridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        childCount = 0;
        setOrientation(HORIZONTAL);
        setWeightSum(2f);
        column1 = new LinearLayout(context);
        column2 = new LinearLayout(context);
        LayoutParams params1 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.weight = 1;
        column1.setOrientation(VERTICAL);
        column1.setGravity(Gravity.CENTER);
        column2.setOrientation(VERTICAL);
        column2.setGravity(Gravity.CENTER);
        super.addView(column1, params1);
        super.addView(column2, params1);
    }

    @Override
    public void addView(View child) {
        if (childCount++ % 2 == 0) {
            column1.addView(child);
        } else {
            column2.addView(child);
        }
    }

    @Override
    public void removeView(View view) {
        column1.removeView(view);
        column2.removeView(view);
        childCount--;
    }

    @Override
    public void removeAllViews() {
        column1.removeAllViews();
        column2.removeAllViews();
        childCount = 0;
    }
}
