package lb.themike10452.hellscorekernelmanagerl.CustomWidgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;

/**
 * Created by Mike on 5/12/2015.
 */
public class CpuFreqLiveView extends RelativeLayout {

    private Context context;
    private ProgressBar progressBar;
    private String sourceFile, dummy;
    private TextView valueTv, titleTv;

    private int MAX_VALUE, value;

    public CpuFreqLiveView(Context context) {
        this(context, null);
    }

    public CpuFreqLiveView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        inflate(context, R.layout.cpu_freq_live_view, this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        valueTv = (TextView) findViewById(R.id.value);
        titleTv = (TextView) findViewById(R.id.title);
    }

    public void brief(int max, int index, String sourceFile) {
        this.sourceFile = sourceFile;
        progressBar.setMax(MAX_VALUE = max);
        titleTv.setText("CPU" + index);
    }

    public void update() {
        if (sourceFile != null) {
            dummy = HKMTools.getInstance().readLineFromFile(sourceFile);
            value = dummy != null ? Integer.parseInt(dummy) : 0;
            progressBar.setProgress(Math.min(value, MAX_VALUE));
            if (value == 0) {
                valueTv.setText(context.getString(R.string.cpu_offline));
            } else {
                valueTv.setText(Integer.toString(value));
            }
        }
    }
}
