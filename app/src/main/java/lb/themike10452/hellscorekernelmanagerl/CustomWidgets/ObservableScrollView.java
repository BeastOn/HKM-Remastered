package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ScrollView;

/**
 * Created by Mike on 4/1/2015.
 */
public class ObservableScrollView extends ScrollView implements ObservableScrollViewInterface {

    private CallBack callBack;
    private boolean isAnimating;
    private boolean isTouched;

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (callBack != null) {
            callBack.scrollChanged(this, l, t, oldl, oldt);
        }
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
            callBack.touchChanged(this, isTouched);
        }
        return super.onTouchEvent(ev);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void scrollTo(int y) {
        blockActiveScrolling();
        ValueAnimator animator = ValueAnimator.ofInt(getScrollY(), y);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scrollTo(0, (Integer) animation.getAnimatedValue());
                isAnimating = animation.getAnimatedFraction() != 1;
            }
        });
        animator.start();
    }

    public void blockActiveScrolling() {
        dispatchTouchEvent(MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(), MotionEvent.ACTION_DOWN, getRight(), getBottom(), KeyEvent.META_SCROLL_LOCK_ON));
    }

    public interface CallBack {
        void scrollChanged(ObservableScrollViewInterface v, int l, int t, int oldl, int oldt);

        void touchChanged(ObservableScrollViewInterface v, boolean isTouched);

        void onAnimationStart();

        void onAnimationEnd();
    }
}
