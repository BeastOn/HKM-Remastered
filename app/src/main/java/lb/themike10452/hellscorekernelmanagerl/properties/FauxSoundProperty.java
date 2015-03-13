package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.CustomAdapters.SeekBarProgressAdapter;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.fragments.SoundControl;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;

/**
 * Created by Mike on 3/7/2015.
 */
public class FauxSoundProperty extends intProperty {

    public static final int MODE_SINGLE = 0;
    public static final int MODE_LEFT = 1;
    public static final int MODE_RIGHT = 2;
    public static final int MODE_LEFT_AMP = 3;
    public static final int MODE_RIGHT_AMP = 4;
    public static final int MODE_DUAL = 5;

    private FauxSoundProperty complement;
    private SeekBar seekBar;
    private int min, max, opMode;

    public FauxSoundProperty(@NonNull String path, View container, int titleId, int defaultValue, int minValue, int maxValue, int mode) {
        super(path, container, defaultValue);
        seekBar = (SeekBar) container.findViewById(R.id.seekBar);
        min = minValue;
        max = maxValue;
        opMode = mode;

        int range = max - min;
        seekBar.setMax(range);
        seekBar.setOnSeekBarChangeListener(new SeekBarProgressAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                super.onProgressChanged(seekBar, progress, fromUser);
                if (fromUser) {
                    setDisplayedValue(min + progress);
                    setChanged();
                    notifyObservers(min + progress);
                }
            }
        });

        if (titleId > 0) {
            setTitle(SoundControl.instance.getString(titleId));
        }
        if (opMode == MODE_LEFT || opMode == MODE_LEFT_AMP) {
            setSubTitle(SoundControl.instance.getString(R.string.left));
        } else if (opMode == MODE_RIGHT || opMode == MODE_RIGHT_AMP) {
            setSubTitle(SoundControl.instance.getString(R.string.right));
        }
    }

    @Override
    public void setDisplayedValue(Object _value) {
        super.setDisplayedValue(_value);
        int value;
        if (_value instanceof String) {
            value = Integer.parseInt((String) _value);
        } else {
            value = (int) _value;
        }
        seekBar.setProgress(value - min);
    }

    @Override
    public int getValue() {
        switch (opMode) {
            case MODE_SINGLE:
                return decode(HKMTools.getInstance().readLineFromFile(filePath));
            case MODE_LEFT:
            case MODE_LEFT_AMP:
            case MODE_DUAL:
                return decode(HKMTools.getInstance().readLineFromFile(filePath).split(" ")[0]);
            case MODE_RIGHT:
            case MODE_RIGHT_AMP:
                return decode(HKMTools.getInstance().readLineFromFile(filePath).split(" ")[1]);
        }
        return DEFAULT_VALUE;
    }

    @Override
    public int setValue(String value) {
        switch (opMode) {
            case MODE_SINGLE:
                value = encode(parse(value), 0);
                break;
            case MODE_DUAL:
                value = encode(parse(value), parse(value));
                break;
            case MODE_LEFT:
            case MODE_LEFT_AMP:
                if (complement != null) {
                    value = encode(parse(value), parse(complement.readDisplayedValue()));
                } else {
                    return 1;
                }
                break;
            case MODE_RIGHT:
            case MODE_RIGHT_AMP:
                if (complement != null) {
                    value = encode(parse(complement.readDisplayedValue()), parse(value));
                } else {
                    return 1;
                }
                break;
        }

        HKMTools.getInstance().addCommand("echo ".concat("\"" + value + "\"").concat(" > ").concat(filePath));
        return 0;
    }

    public FauxSoundProperty getComplement() {
        return complement;
    }

    public void setComplement(FauxSoundProperty complement) {
        this.complement = complement;
    }

    public void setTitle(String title) {
        TextView titleView = (TextView) mContainer.findViewById(R.id.title);
        titleView.setText(title);
        titleView.setVisibility(View.VISIBLE);
    }

    public void setSubTitle(String subTitle) {
        TextView subtitleView = (TextView) mContainer.findViewById(R.id.subtitle);
        subtitleView.setText(subTitle);
        subtitleView.setVisibility(View.VISIBLE);
    }

    private String encode(int value1, int value2) {
        if (opMode == MODE_LEFT_AMP || opMode == MODE_RIGHT_AMP) {
            value1 = 15 & (6 - value1);
            value2 = 15 & (6 - value2);
        }
        if (value1 < 0)
            value1 += 256;
        if (value2 < 0)
            value2 += 256;

        int key = ((255 & (2147483647 ^ (value1 & 255) + (value2 & 255))));
        if (opMode == MODE_SINGLE) {
            return value1 + " " + key;
        } else {
            return (value1 + " " + value2 + " " + " " + key).trim();
        }
    }

    private int decode(String value) {
        int a = Integer.parseInt(value.trim());
        switch (opMode) {
            case MODE_LEFT_AMP:
            case MODE_RIGHT_AMP:
                return (short) (22 - a);
            default:
                if (a > 10) {
                    a -= 256;
                }
                return a;
        }

    }

    private int parse(String str) {
        return Integer.parseInt(str);
    }
}
