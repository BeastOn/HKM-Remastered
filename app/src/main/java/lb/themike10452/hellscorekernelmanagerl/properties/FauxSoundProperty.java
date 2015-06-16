package lb.themike10452.hellscorekernelmanagerl.properties;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.CustomClasses.SeekBarProgressAdapter;
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

    public FauxSoundProperty(@NonNull String path, View container, int titleId, int minValue, int maxValue, int mode) {
        super(path, container, false);
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
    public void setDisplayedValue(String value) {
        super.setDisplayedValue(value);
        if (value != null) {
            seekBar.setProgress(Integer.parseInt(value) - min);
        }
    }

    @Override
    public void setDisplayedValue(int value) {
        super.setDisplayedValue(value);
        seekBar.setProgress(value - min);
    }

    public void setDisplayedValue(int value, boolean convert) {
        if (!convert) {
            setDisplayedValue(value);
        } else {
            setDisplayedValue(decode(Integer.toString(value)));
        }
    }

    @Override
    public String getValue() {
        return getValue(false);
    }

    public String getValue(boolean convert) {
        if (convert) {
            switch (opMode) {
                case MODE_SINGLE: {
                    return decode(HKMTools.getInstance().readLineFromFile(filePath));
                }
                case MODE_LEFT:
                case MODE_LEFT_AMP:
                case MODE_DUAL: {
                    String str = HKMTools.getInstance().readLineFromFile(filePath);
                    if (str != null) {
                        return decode(str.split(" ")[0]);
                    } else {
                        return null;
                    }
                }
                case MODE_RIGHT:
                case MODE_RIGHT_AMP: {
                    String str = HKMTools.getInstance().readLineFromFile(filePath);
                    if (str != null) {
                        return decode(str.split(" ")[1]);
                    } else {
                        return null;
                    }
                }
                default:
                    return null;
            }
        } else {
            switch (opMode) {
                case MODE_SINGLE: {
                    return HKMTools.getInstance().readLineFromFile(filePath);
                }
                case MODE_LEFT:
                case MODE_LEFT_AMP:
                case MODE_DUAL: {
                    String str = HKMTools.getInstance().readLineFromFile(filePath);
                    if (str != null) {
                        return str.split(" ")[0];
                    } else {
                        return null;
                    }
                }
                case MODE_RIGHT:
                case MODE_RIGHT_AMP: {
                    String str = HKMTools.getInstance().readLineFromFile(filePath);
                    if (str != null) {
                        return str.split(" ")[1];
                    } else {
                        return null;
                    }
                }
                default:
                    return null;
            }
        }
    }

    @Override
    public void setValue(String value) {
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
                }
                break;
            case MODE_RIGHT:
            case MODE_RIGHT_AMP:
                if (complement != null) {
                    value = encode(parse(complement.readDisplayedValue()), parse(value));
                }
                break;
        }

        super.setValue(value);
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
            value1 = 0xF & 6 - value1;
            value2 = 0xF & 6 - value2;
        }

        if (value1 < 0)
            value1 += 256;
        if (value2 < 0)
            value2 += 256;

        int key = 0xFF & (0xFFFFFFFF ^ (value1 & 0xFF) + (value2 & 0xFF));
        if (opMode == MODE_SINGLE) {
            return value1 + " " + key;
        } else {
            return (value1 + " " + value2 + " " + " " + key).trim();
        }
    }

    private String decode(String value) {
        if (value == null) return null;
        int a = Integer.parseInt(value.trim());
        switch (opMode) {
            case MODE_LEFT_AMP:
            case MODE_RIGHT_AMP:
                return Integer.toString(6 - (0xF & a));
            default:
                if (a > 127) {
                    a = -1 * (256 - a);
                }
                return Integer.toString(a);
        }

    }

    private int parse(String str) {
        return Integer.parseInt(str);
    }
}
