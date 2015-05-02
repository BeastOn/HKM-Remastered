package lb.themike10452.hellscorekernelmanagerl.properties;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.CustomAdapters.CPUVoltagesAdapter;
import lb.themike10452.hellscorekernelmanagerl.CustomWidgets.NumberModifier;
import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 2/27/2015.
 */
public class VoltageProperty implements View.OnClickListener {

    public Button INC;
    public Button DEC;
    private String mInitialVoltage;
    private String mVolt, mTitle;
    private View mView;

    public VoltageProperty(String title, String initialVoltage, View container) {
        mInitialVoltage = initialVoltage.trim();
        mView = container;
        setDisplayedValue(title.trim(), mInitialVoltage);

        INC = ((NumberModifier) container.findViewById(R.id.modifier)).INCREMENT_BUTTON;
        DEC = ((NumberModifier) container.findViewById(R.id.modifier)).DECREMENT_BUTTON;

        INC.setOnClickListener(this);
        DEC.setOnClickListener(this);
    }

    public String getVoltage() {
        return mVolt;
    }

    public String getTitle() {
        return mTitle;
    }

    public View getView() {
        return mView;
    }

    public void modifyValue(long offset) {
        long newValue = Long.parseLong(mVolt) + offset;
        setDisplayedValue(mTitle, Long.toString(newValue));
    }

    public void setDisplayedValue(final String voltage) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mVolt = voltage;
                ((TextView) mView.findViewById(R.id.diff)).setText(compare(mInitialVoltage, voltage));
                ((NumberModifier) mView.findViewById(R.id.modifier)).setValue(voltage);
            }
        };
        if (mView.getParent() != null) {
            mView.post(runnable);
        } else {
            runnable.run();
        }
    }

    public void setDisplayedValue(final String frequency, final String voltage) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mTitle = frequency;
                mVolt = voltage;
                ((TextView) mView.findViewById(R.id.freq)).setText(frequency);
                ((TextView) mView.findViewById(R.id.diff)).setText(compare(mInitialVoltage, voltage));
                ((NumberModifier) mView.findViewById(R.id.modifier)).setValue(voltage);
            }
        };
        if (mView.getParent() != null) {
            mView.post(runnable);
        } else {
            runnable.run();
        }
    }

    private String compare(String initial, String actual) {
        long diff = Long.parseLong(actual) - Long.parseLong(initial);
        return diff != 0 ? diff > 0 ? "+" + diff : Long.toString(diff) : "";
    }

    @Override
    public void onClick(View v) {
        long offset = 0;
        switch (CPUVoltagesAdapter.mode) {
            case 1:
                offset = CPUVoltagesAdapter.VDD_STEP;
                break;
            case 2:
                offset = CPUVoltagesAdapter.UV_MV_STEP;
                break;
        }
        modifyValue(v == INC ? offset : -offset);
    }
}
