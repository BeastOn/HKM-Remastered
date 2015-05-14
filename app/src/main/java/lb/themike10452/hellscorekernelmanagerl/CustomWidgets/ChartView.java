package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Mike on 5/7/2015.
 */
public class ChartView extends View {
    private RectF rectF;

    private int[] values;
    private Paint[] paints;

    public ChartView(Context context) {
        this(context, null);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rectF = new RectF();
    }

    public void update(int[] values, int[] colors, int[] legends, String unit, ViewGroup legendView) {
        this.values = values;
        if (legends != null && legendView != null) {
            legendView.removeAllViews();
        }

        paints = new Paint[colors.length];

        for (int i = 0; i < colors.length; i++) {
            paints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            paints[i].setColor(colors[i]);
            paints[i].setStyle(Paint.Style.FILL);
            if (legends != null && legendView != null) {
                legendView.addView(new ChartLegend(getContext(), colors[i], legends[i], String.format("%s %s", values[i], unit)));
            }
        }
        updateView();
    }

    public void updateView() {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (values != null) {
            final int width = getMeasuredWidth();
            final int height = getMeasuredHeight();
            final int r = Math.min(width, height) / 2;
            final int left = (width / 2) - r;
            final int top = (height / 2) - r;
            final int right = (width / 2) + r;
            final int bottom = (height / 2) + r;
            rectF.set(left, top, right, bottom);

            int sum = 0;
            for (int i : values) {
                sum += i;
            }

            int angle;
            int sumOfAngles = 0;
            int startAngle = -120;
            for (int i = 0; i < values.length; i++) {
                if (i == values.length - 1) {
                    angle = 360 - sumOfAngles;
                } else {
                    angle = values[i] * 360 / sum;
                }
                canvas.drawArc(rectF, startAngle, angle, true, paints[i]);
                startAngle += angle;
                sumOfAngles += angle;
            }
        }
    }
}
