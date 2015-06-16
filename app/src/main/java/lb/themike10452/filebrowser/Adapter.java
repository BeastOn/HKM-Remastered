package lb.themike10452.filebrowser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.fragments.ProfileManager;

/**
 * Created by Mike on 9/22/2014.
 */
public class Adapter extends BaseAdapter {

    private static FileBrowser.FileDescriptionCallback mCallback;

    public ArrayList<File> listItems;
    private Context mContext;

    public Adapter(Context context, ArrayList<File> files, FileBrowser.FileDescriptionCallback fileDescriptionCallback) {
        listItems = files;
        mContext = context;
        mCallback = fileDescriptionCallback;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.file_browser_list_item, null);
        }

        convertView.setHasTransientState(true);

        if (listItems.get(position).isDirectory()) {
            convertView.findViewById(R.id.imageView1).setBackground(mContext.getDrawable(R.drawable.ic_folder));
        } else if (getFileExtension(listItems.get(position)).equalsIgnoreCase("zip")) {
            convertView.findViewById(R.id.imageView1).setBackground(mContext.getDrawable(R.drawable.ic_zip));
        } else if (getFileExtension(listItems.get(position)).equalsIgnoreCase(ProfileManager.EXT)) {
            convertView.findViewById(R.id.imageView1).setBackground(mContext.getDrawable(R.drawable.ic_profile));
        } else {
            convertView.findViewById(R.id.imageView1).setBackground(mContext.getDrawable(R.drawable.ic_file));
        }

        ((TextView) convertView.findViewById(R.id.text1)).setText(listItems.get(position).isDirectory() ? position == 0 ? ".." : listItems.get(position).getName() : listItems.get(position).getName());

        if (mCallback != null) {
            String desc = mCallback.onLoadDescription(get(position));
            if (desc != null) {
                final TextView tv = (TextView) convertView.findViewById(R.id.text2);
                tv.setText(desc);
                tv.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public File get(int position) {
        return (File) getItem(position);
    }

    public static String getFileExtension(File f) {
        try {
            return f.isFile() ? f.getName().substring(f.getName().lastIndexOf(".") + 1) : "";
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }
}
