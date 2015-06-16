package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 5/16/2015.
 */
public class WheelPicker extends FrameLayout implements View.OnClickListener {

    private ArrayList<TextView> textViews;
    private Context mContext;
    private ObservableHorizontalScrollView scrollView;
    private OnSelectionChangedListener listener;

    private int width, selectionIndex, scanSize;

    public WheelPicker(Context context) {
        this(context, null);
    }

    public WheelPicker(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
        textViews = new ArrayList<>();
        inflate(context, R.layout.wheel_selector, this);
        scrollView = (ObservableHorizontalScrollView) findViewById(R.id.horizontalScrollView);
        scrollView.setCallBack(new ObservableScrollView.CallBack() {
            @Override
            public void scrollChanged(ObservableScrollViewInterface v, int l, int t, int oldl, int oldt) {
                selectionIndex = v.getScrollX() / (width / 6);
                if (selectionIndex % 2 == 0) {
                    selectionIndex /= 2;
                } else {
                    selectionIndex /= 2;
                    selectionIndex += 1;
                }
            }

            @Override
            public void touchChanged(ObservableScrollViewInterface v, boolean isTouched) {
                if (!isTouched) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int sx = getScroll() / (width / 6);
                            if (sx % 2 == 0) {
                                sx = sx / 2;
                            } else {
                                sx = sx / 2;
                                sx += 1;
                            }
                            scrollTo(sx * (width / 3));
                        }
                    }, 100);
                }
            }

            @Override
            public void onAnimationStart() {
            }

            @Override
            public void onAnimationEnd() {
                if (listener != null) {
                    listener.selectionChanged(selectionIndex);
                }
            }
        });
    }

    public void setDisplayedValues(final String[] values) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                textViews.clear();

                width = getRight() - getLeft();
                scanSize = width / 3;

                RelativeLayout.LayoutParams leftParams = (RelativeLayout.LayoutParams) findViewById(R.id.leftFade).getLayoutParams();
                RelativeLayout.LayoutParams rightParams = (RelativeLayout.LayoutParams) findViewById(R.id.rightFade).getLayoutParams();
                leftParams.width = scanSize;
                rightParams.width = scanSize;

                findViewById(R.id.leftFade).setLayoutParams(leftParams);
                findViewById(R.id.rightFade).setLayoutParams(rightParams);

                ViewGroup holder = (ViewGroup) findViewById(R.id.itemHolder);
                holder.removeAllViews();

                Space space = new Space(mContext);
                space.setMinimumWidth(scanSize);
                holder.addView(space);

                for (String str : values) {
                    TextView textView = new TextView(mContext);
                    textView.setText(str);
                    textView.setWidth(scanSize);
                    textView.setGravity(Gravity.CENTER);
                    textView.setOnClickListener(WheelPicker.this);

                    holder.addView(new Space(mContext));
                    holder.addView(textView);

                    textViews.add(textView);
                }

                space = new Space(mContext);
                space.setMinimumWidth(scanSize);
                holder.addView(space);
                scrollView.setScrollNormalization(scanSize);
            }
        }, 10);
    }

    public int getScroll() {
        return scrollView.getScrollX();
    }

    public void scrollTo(int x) {
        scrollView.scrollTo(x);
    }

    public int getSelectionIndex() {
        return selectionIndex;
    }

    public void setSelection(int index) {
        scrollTo(index * scanSize);
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    public interface OnSelectionChangedListener {
        void selectionChanged(int index);
    }

    @Override
    public void onClick(View v) {
        int index = textViews.indexOf(v);
        if (index >= 0) {
            setSelection(index);
        }
    }
}
