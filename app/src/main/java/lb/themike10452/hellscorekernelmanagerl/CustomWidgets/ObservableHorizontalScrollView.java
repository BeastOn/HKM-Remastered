package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by Mike on 4/4/2015.
 */
public class ObservableHorizontalScrollView extends HorizontalScrollView {

    private ObservableScrollView.CallBack callBack;

    private boolean isTouched;

    public ObservableHorizontalScrollView(Context context) {
        super(context);
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
}
