package lb.themike10452.hellscorekernelmanagerl.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.NumberModifier;
import lb.themike10452.hellscorekernelmanagerl.MainActivity.mTransactionManager;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.Library;
import lb.themike10452.hellscorekernelmanagerl.utils.Tools;

/**
 * Created by Mike on 2/28/2015.
 */
public class CPUGovernorCfg extends Fragment implements HKMFragment {

    private static CPUGovernorCfg instance;

    private Activity mActivity;
    private LinearLayout list;
    private View mView;
    private mAdapter adapter;
    private mTransactionManager transactionManager;

    public static CPUGovernorCfg getInstance(mTransactionManager manager) {
        if (instance == null) {
            instance = new CPUGovernorCfg();
        }
        if (manager != null)
            instance.transactionManager = manager;
        return instance;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_gov_cfg, null, false);
        String governor = getArguments().getString("title");
        ((TextView) mView.findViewById(R.id.title)).setText(governor);

        mView.findViewById(R.id.img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionManager.popBackStack();
            }
        });

        File cfgDir = new File(String.format(Library.CPU_GOV_CFG_PATH, governor));
        if (cfgDir.exists() && cfgDir.isDirectory()) {
            final File[] params = cfgDir.listFiles();
            adapter = new mAdapter(mActivity, params);
            list = (LinearLayout) mView.findViewById(R.id.listView);
            for (int i = 0; i < adapter.getCount(); i++) {
                View v = adapter.getView(i, null, null);
                if (v != null)
                    list.addView(v);
            }
        }

        return mView;
    }

    @Override
    public void refresh(boolean byUser) {
        adapter.reset();
        list.removeAllViews();
        for (int i = 0; i < adapter.getCount(); i++) {
            View v = adapter.getView(i, null, null);
            if (v != null)
                list.addView(v);
        }
    }

    @Override
    public void saveAll() {
        for (int i = 0; i < adapter.getCount(); i++) {
            String value = adapter.getValue(i);
            if (value != null) {
                Tools.getInstance().exec("echo ".concat(value).concat(" > ").concat(adapter.getItem(i).getAbsolutePath()));
            }
        }
    }

    private PopupWindow getPopupWindow() {
        PopupWindow popupWindow = new PopupWindow(mActivity);
        popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(1);
        popupWindow.setWidth(1000);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_window_background));
        popupWindow.setClippingEnabled(false);
        popupWindow.setElevation(10f);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        return popupWindow;
    }

    class mAdapter extends ArrayAdapter<File> {

        private Context mContext;
        private ArrayList<TextView> valueHolders;
        private ArrayList<String> values;

        public mAdapter(Context context, File[] objects) {
            super(context, R.layout.gov_cfg_list_item, objects);
            mContext = context;
            values = new ArrayList<>();
            valueHolders = new ArrayList<>();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View mView = convertView;
            if (mView == null)
                mView = LayoutInflater.from(mContext).inflate(R.layout.gov_cfg_list_item, null, false);

            String value = Tools.getInstance().readBlockFromFile(getItem(position));
            try {
                Integer.parseInt(value);
            } catch (Exception e) {
                Log.d("TAG", e.toString());
                values.add(null);
                valueHolders.add(null);
                return null;
            }

            values.add(value);

            ((TextView) mView.findViewById(R.id.fileName)).setText(getItem(position).getName());

            final TextView textView = (TextView) mView.findViewById(R.id.value);
            textView.setText(value);
            valueHolders.add(textView);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupWindow popupWindow = getPopupWindow();
                    String value = textView.getText().toString();
                    final NumberModifier modifier = new NumberModifier(mActivity);
                    modifier.setValue(value);
                    modifier.setPadding(20, 20, 20, 20);
                    modifier.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    popupWindow.setContentView(modifier);
                    //popupWindow.showAsDropDown(view, view.getMeasuredWidth() - modifier.getMeasuredWidth(), -(view.getMeasuredHeight() / 2 + modifier.getMeasuredHeight() / 2));
                    popupWindow.showAsDropDown(view, view.getMeasuredWidth() - modifier.getMeasuredWidth(), 0);
                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            updateValue(position, modifier.getValue());
                        }
                    });
                }
            });

            return mView;
        }

        public void reset() {
            values.clear();
            valueHolders.clear();
        }

        public String getValue(int position) {
            return values.get(position);
        }

        public TextView getValueHolder(int position) {
            return valueHolders.get(position);
        }

        public void updateValue(int position, String value) {
            values.remove(position);
            values.add(position, value);
            getValueHolder(position).setText(value);
        }

    }

}
