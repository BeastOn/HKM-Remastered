package lb.themike10452.hellscorekernelmanagerl.CustomAdapters;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 3/4/2015.
 */
public class DrawerAdapter extends BaseAdapter implements DrawerLayout.DrawerListener {

    private Context mContext;
    private String[] mLabels;
    private int[] mIcons;

    public DrawerAdapter(Context context, String[] labels, int[] icons) {
        mContext = context;
        mLabels = labels;
        mIcons = icons;
    }

    @Override
    public int getCount() {
        return mLabels != null ? mLabels.length : 0;
    }

    @Override
    public Object getItem(int position) {
        return mLabels != null ? mLabels[position] : null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.drawer_item, null);
        }

        ((TextView) view.findViewById(R.id.text)).setText(mLabels[position]);
        ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(mContext.getDrawable(mIcons[position % mIcons.length]));

        return view;
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}
