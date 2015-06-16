package lb.themike10452.hellscorekernelmanagerl.CustomAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 6/15/2015.
 */
public class ProfileListAdapter extends BaseAdapter {
    private ArrayList<File> mFilesList;
    private File[] mFilesArray;
    private Context mContext;

    public ProfileListAdapter(Context context, ArrayList<File> listOfFiles) {
        mContext = context;
        mFilesList = listOfFiles;
    }

    public ProfileListAdapter(Context context, File[] files) {
        mContext = context;
        mFilesArray = files;
    }

    @Override
    public int getCount() {
        return mFilesList != null ? mFilesList.size() : mFilesArray != null ? mFilesArray.length : 0;
    }

    @Override
    public Object getItem(int position) {
        return mFilesList != null ? mFilesList.get(position) : mFilesArray != null ? mFilesArray[position] : null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item, null, false);
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
            imageView.setImageResource(R.drawable.ic_profile);
            imageView.setVisibility(View.VISIBLE);
        }
        ((TextView) convertView.findViewById(R.id.text)).setText(((File) getItem(position)).getName());
        return convertView;
    }
}
