package lb.themike10452.hellscorekernelmanagerl.properties;

import android.view.View;
import android.view.ViewParent;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Observable;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.properties.interfaces.HKMPropertyInterface;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;

/**
 * Created by Mike on 2/23/2015.
 */
public class HKMProperty extends Observable implements HKMPropertyInterface {

    public int FLAGS;

    protected String filePath;
    protected View mContainer;
    protected View topAncestor;
    protected int viewId;

    public View getTopAncestor() {
        if (topAncestor != null) return topAncestor;

        View tmp = mContainer;
        topAncestor = mContainer;
        if (hasFlag(PropertyUtils.FLAG_VIEW_COMBO)) {
            ViewParent parent;
            while ((parent = tmp.getParent()) != null && parent instanceof View) {
                if (((View) parent).getId() == R.id.topAncestor) {
                    topAncestor = (View) parent;
                    break;
                } else {
                    tmp = (View) parent;
                }
            }
        }
        return topAncestor;
    }

    public int getViewId() {
        return viewId;
    }

    public View getView() {
        return mContainer;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getFlags() {
        return FLAGS;
    }

    public boolean hasFlag(int flag) {
        return (FLAGS & flag) == flag;
    }

    public boolean isVisible() {
        if (topAncestor == null) {
            topAncestor = hasFlag(PropertyUtils.FLAG_VIEW_COMBO) ? getTopAncestor() : mContainer;
        }
        return topAncestor.getVisibility() == View.VISIBLE && mContainer.getVisibility() == View.VISIBLE;
    }

    public String getValue() {
        return HKMTools.getInstance().readLineFromFile(filePath);
    }

    public void setValue(String value) {
        HKMTools.getInstance().addCommand("echo ".concat("\"" + value + "\"").concat(" > ").concat(filePath));
    }

    protected void setValue(String value, String altPath) {
        HKMTools.getInstance().addCommand("echo ".concat("\"" + value + "\"").concat(" > ").concat(altPath));
    }

    public String readDisplayedValue() {
        if (!isVisible()) return null;
        String value = null;
        if (mContainer instanceof Switch) {
            value = ((Switch) mContainer).isChecked() ? "1" : "0";
        } else if (mContainer instanceof TextView) {
            value = ((TextView) mContainer).getText().toString();
        } else {
            View disp = mContainer.findViewById(R.id.value);
            if (disp != null) {
                if (disp instanceof Switch) {
                    value = ((Switch) disp).isChecked() ? "1" : "0";
                } else if (disp instanceof TextView) {
                    value = ((TextView) disp).getText().toString();
                }
            } else {
                disp = mContainer.findViewById(R.id.mswitch);
                if (disp != null && disp instanceof Switch) {
                    value = ((Switch) disp).isChecked() ? "1" : "0";
                }
            }
        }
        return value;
    }

    public void setDisplayedValue(final String value) {
        if (value == null) {
            getTopAncestor().setVisibility(View.GONE);
            return;
        }
        if (mContainer instanceof Switch) {
            ((Switch) mContainer).setChecked(value.equals("Y") || !value.equals("0"));
        } else if (mContainer instanceof TextView) {
            ((TextView) mContainer).setText(value);
        } else {
            View disp = mContainer.findViewById(R.id.value);
            if (disp != null) {
                if (disp instanceof Switch) {
                    ((Switch) disp).setChecked(value.equals("Y") || !value.equals("0"));
                } else if (disp instanceof TextView) {
                    ((TextView) disp).setText(value);
                }
            } else {
                disp = mContainer.findViewById(R.id.mswitch);
                if (disp != null && disp instanceof Switch) {
                    ((Switch) disp).setChecked(value.equals("Y") || !value.equals("0"));
                }
            }
        }
    }

    public void refresh() {
        if (mContainer != null) {
            final String value = getValue();
            mContainer.post(new Runnable() {
                @Override
                public void run() {
                    setDisplayedValue(value);
                }
            });
        }
    }
}
