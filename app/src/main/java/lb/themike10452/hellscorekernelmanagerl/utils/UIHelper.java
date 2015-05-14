package lb.themike10452.hellscorekernelmanagerl.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 5/8/2015.
 */
public class UIHelper {

    public static void removeEmptyCards(LinearLayout container) {
        int count = container.getChildCount();
        for (int i = 0; i < count; i++) {
            if (container.getChildAt(i).getVisibility() == View.VISIBLE) {
                LinearLayout cardLayout = (LinearLayout) container.getChildAt(i).findViewById(R.id.usefulContent);
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
}
