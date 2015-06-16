package lb.themike10452.filebrowser;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 9/22/2014.
 */
public class FileBrowser extends Activity implements AdapterView.OnItemClickListener {

    public static final String EXTRA_ALLOWED_EXTENSIONS = "ALLOWED_EXTENSIONS";
    public static final String EXTRA_SHOW_FOLDERS_ONLY = "SHOW_FOLDERS_ONLY";
    public static final String EXTRA_START_DIRECTORY = "START";
    public static final String EXTRA_USE_DESCRIPTION_CALLBACK = "USE_DESCRIPTION_CALLBACK";

    public static final String DATA_DIRECTORY = "data_directory";
    public static final String DATA_FILENAME = "data_filename";
    public static final String DATA_FILEPATH = "data_filepath";

    public static final int RESULT_DIRECTORY_SELECTED = 1;
    public static final int RESULT_FILE_SELECTED = 2;
    public static final int RESULT_CANCELED = -1;

    private static FileDescriptionCallback mCallback;

    public static void setFileDescriptionCallback(FileDescriptionCallback callback) {
        mCallback = callback;
    }

    private static Comparator<File> comparator = new Comparator<File>() {
        @Override
        public int compare(File f1, File f2) {
            if (f1.isDirectory() && f2.isFile()) {
                return -2;
            } else if (f1.isFile() && f2.isDirectory()) {
                return 2;
            } else {
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        }
    };

    private Adapter adapter;
    private ArrayList<File> listItems;
    private File root;
    private File workingDirectory;
    private HashMap<String, Parcelable> scrollHistory;
    private ListView listView;
    private String[] ALLOWED_EXTENSIONS;
    private TextView titleView;

    private boolean SHOW_FOLDERS_ONLY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_btt, R.anim.stay_still);
        setContentView(R.layout.file_browser);

        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setElevation(0);
        actionBar.setCustomView(titleView = new TextView(this));
        actionBar.setDisplayShowCustomEnabled(true);

        Bundle extras = getIntent().getExtras();
        scrollHistory = new HashMap<>();

        listView = (ListView) findViewById(R.id.list);

        try {
            ALLOWED_EXTENSIONS = extras.getStringArray(EXTRA_ALLOWED_EXTENSIONS);
        } catch (NullPointerException e) {
            ALLOWED_EXTENSIONS = null;
        }
        try {
            SHOW_FOLDERS_ONLY = extras.getBoolean(EXTRA_SHOW_FOLDERS_ONLY);
        } catch (NullPointerException e) {
            SHOW_FOLDERS_ONLY = false;
        }
        try {
            final File f = new File(extras.getString(EXTRA_START_DIRECTORY));
            if (!f.exists() || !f.isDirectory()) {
                throw new IllegalArgumentException();
            }
            Bundle bundle = new Bundle();
            bundle.putString(DATA_DIRECTORY, f.toString());
            updateScreen(bundle);
        } catch (Exception ignored) {
            updateScreen(null);
        }

        findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra(DATA_DIRECTORY, workingDirectory.toString());
                setResult(RESULT_DIRECTORY_SELECTED, data);
                finish();
            }
        });

        findViewById(R.id.btn_select).setVisibility(SHOW_FOLDERS_ONLY ? View.VISIBLE : View.INVISIBLE);

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    public void updateScreen(Bundle pac) {
        root = (pac == null) ? Environment.getExternalStorageDirectory() : new File(pac.getString(DATA_DIRECTORY));
        workingDirectory = root;
        titleView.setText(root.toString());

        if (listItems == null) {
            listItems = new ArrayList<>();
        } else {
            listItems.clear();
        }

        File[] fileList = root.listFiles();
        if (fileList != null) {
            for (File f : fileList) {
                if (f.isDirectory()) {
                    listItems.add(f);
                } else if (!SHOW_FOLDERS_ONLY) {
                    if (ALLOWED_EXTENSIONS == null) {
                        listItems.add(f);
                    } else if (arrayContains(ALLOWED_EXTENSIONS, Adapter.getFileExtension(f))) {
                        listItems.add(f);
                    }
                }
            }
            Collections.sort(listItems, comparator);
        }

        if (root.getParentFile() != null) {
            listItems.add(0, root.getParentFile());
        }
        if (adapter == null) {
            listView.setAdapter(adapter = new Adapter(FileBrowser.this, listItems, getIntent().getBooleanExtra(EXTRA_USE_DESCRIPTION_CALLBACK, false) ? mCallback : null));
            listView.setOnItemClickListener(this);
        } else {
            adapter.notifyDataSetChanged();
            listView.setAdapter(adapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapter.listItems.get(i).isDirectory()) {
            if (i == 0) {
                if (root.toString().equalsIgnoreCase(Environment.getExternalStorageDirectory().toString())) {
                    return;
                } else {
                    onBackPressed();
                    return;
                }
            }

            scrollHistory.put(workingDirectory.toString(), listView.onSaveInstanceState());

            Bundle pac = new Bundle();
            pac.putString(DATA_DIRECTORY, adapter.get(i).toString());
            updateScreen(pac);
        } else {
            Intent data = new Intent();
            data.putExtra(DATA_DIRECTORY, workingDirectory.toString() + File.separator);
            data.putExtra(DATA_FILEPATH, adapter.get(i).toString());
            data.putExtra(DATA_FILENAME, adapter.get(i).getName());
            setResult(RESULT_FILE_SELECTED, data);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (workingDirectory.toString().equalsIgnoreCase(Environment.getExternalStorageDirectory().toString()))
            return;

        String path = adapter.get(0).toString();

        Bundle pac = new Bundle();
        pac.putString(DATA_DIRECTORY, path);
        updateScreen(pac);

        if (scrollHistory.containsKey(path)) {
            listView.onRestoreInstanceState(scrollHistory.get(path));
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_in_ttb);
    }

    private boolean arrayContains(String[] array, String element) {
        for (String s : array) {
            if (s.equalsIgnoreCase(element))
                return true;
        }
        return false;
    }


    public interface FileDescriptionCallback {
        String onLoadDescription(File f);
    }
}
