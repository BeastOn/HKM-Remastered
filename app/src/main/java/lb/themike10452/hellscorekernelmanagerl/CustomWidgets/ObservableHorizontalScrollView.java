package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.HorizontalScrollView;

/**
 * Created by Mike on 4/4/2015.
 */
public class ObservableHorizontalScrollView extends HorizontalScrollView implements ObservableScrollViewInterface {

    private ObservableScrollView.CallBack callBack;

    private boolean isTouched, isAnimating;
    private int scrollNormalization;

    public ObservableHorizontalScrollView(Context context) {
        this(context, null);
    }

    public ObservableHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCallBack(ObservableScrollView.CallBack callBack) {
        this.callBack = callBack;
    }

    public boolean isTouched() {
        return isTouched;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouched = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouched = false;
                break;
        }
        if (callBack != null) {
            callBack.touchChanged(null, isTouched);
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (callBack != null) {
            callBack.scrollChanged(this, l, t, oldl, oldt);
        }
    }

    @Override
    public boolean isAnimating() {
        return isAnimating;
    }

    @Override
    public void scrollTo(int x) {
        blockActiveScrolling();
        ValueAnimator animator = ValueAnimator.ofInt(getScrollX(), x);
        int snap = scrollNormalization != 0 ? scrollNormalization - (Math.abs(getScrollX() - x)) : 300;
        while (snap <= 0) snap += 500;
        animator.setDuration(snap);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scrollTo((Integer) animation.getAnimatedValue(), 0);
                isAnimating = animation.getAnimatedFraction() != 1f;
                if (!isAnimating) {
                    callBack.onAnimationEnd();
                }
            }
        });
        animator.start();
        callBack.onAnimationStart();
    }

    public void blockActiveScrolling() {
        dispatchTouchEvent(MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(), MotionEvent.ACTION_DOWN, getRight(), getBottom(), KeyEvent.META_SCROLL_LOCK_ON));
    }

    public void setScrollNormalization(int n) {
        scrollNormalization = n;
    }
}
