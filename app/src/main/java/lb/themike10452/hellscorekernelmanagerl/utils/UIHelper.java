package lb.themike10452.hellscorekernelmanagerl.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import java.util.ArrayList;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 5/8/2015.
 */
public class UIHelper {

    public static final class Constants {
        public static final int GRAVITY_LEFT = 0;
        public static final int GRAVITY_RIGHT = 1;
        public static final int GRAVITY_TOP = 2;
        public static final int GRAVITY_BOTTOM = 3;
    }

    public static void removeEmptyCards(LinearLayout container) {
        int count = container.getChildCount();
        for (int i = 0; i < count; i++) {
            if (container.getChildAt(i).getVisibility() == View.VISIBLE) {
                LinearLayout cardLayout = (LinearLayout) container.getChildAt(i).findViewById(R.id.prefsHolder);
                if (cardLayout != null) {
                    int prefCount;
                    int visibleViews = prefCount = cardLayout.getChildCount();
                    for (int j = 0; j < prefCount; j++) {
                        if (cardLayout.getChildAt(j).getVisibility() != View.VISIBLE) {
                            visibleViews--;
                        }
                    }
                    if (visibleViews < 1) {
                        final View v = container.getChildAt(i);
                        v.post(new Runnable() {
                            @Override
                            public void run() {
                                v.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }
        }
    }

    public static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    public static void fadeOut(final View view) {
        fadeOut(view, Constants.GRAVITY_LEFT);
    }

    public static void fadeOut(final View view, final int gravity) {
        final Animation fadeAnimation = new AlphaAnimation(1, 0);
        fadeAnimation.setDuration(500);
        fadeAnimation.setInterpolator(new DecelerateInterpolator(2));
        fadeAnimation.setFillAfter(false);
        fadeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        Animation translateAnimation;
        switch (gravity) {
            case Constants.GRAVITY_TOP:
                translateAnimation = new TranslateAnimation(0, 0, 0, -100);
                break;
            case Constants.GRAVITY_BOTTOM:
                translateAnimation = new TranslateAnimation(0, 0, 0, 100);
                break;
            case Constants.GRAVITY_RIGHT:
                translateAnimation = new TranslateAnimation(0, 100, 0, 0);
                break;
            default:
                translateAnimation = new TranslateAnimation(0, -100, 0, 0);
        }
        translateAnimation.setDuration(500);
        translateAnimation.setInterpolator(new DecelerateInterpolator(2));

        final AnimationSet set = new AnimationSet(false);
        set.addAnimation(fadeAnimation);
        set.addAnimation(translateAnimation);

        view.startAnimation(set);
    }

    public static void fadeIn(final View view) {
        fadeIn(view, Constants.GRAVITY_LEFT);
    }

    public static void fadeIn(final View view, final int gravity) {
        final Animation fadeAnimation = new AlphaAnimation(0, 1);
        fadeAnimation.setDuration(500);
        fadeAnimation.setInterpolator(new DecelerateInterpolator(2));
        fadeAnimation.setFillAfter(false);
        fadeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        Animation translateAnimation;
        switch (gravity) {
            case Constants.GRAVITY_TOP:
                translateAnimation = new TranslateAnimation(0, 0, -100, 0);
                break;
            case Constants.GRAVITY_BOTTOM:
                translateAnimation = new TranslateAnimation(0, 0, 100, 0);
                break;
            case Constants.GRAVITY_RIGHT:
                translateAnimation = new TranslateAnimation(100, 0, 0, 0);
                break;
            default:
                translateAnimation = new TranslateAnimation(-100, 0, 0, 0);
        }
        translateAnimation.setDuration(500);
        translateAnimation.setInterpolator(new DecelerateInterpolator(2));

        final AnimationSet set = new AnimationSet(false);
        set.addAnimation(fadeAnimation);
        set.addAnimation(translateAnimation);

        view.startAnimation(set);
    }
}
