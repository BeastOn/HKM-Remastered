package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

/**
 * Created by Mike on 4/4/2015.
 */
public interface ObservableScrollViewInterface {
    public boolean isTouched();

    public boolean isAnimating();

    public void scrollTo(int y);

    public void scrollTo(int x, int y);

    public int getScrollY();

    public int getScrollX();
}
