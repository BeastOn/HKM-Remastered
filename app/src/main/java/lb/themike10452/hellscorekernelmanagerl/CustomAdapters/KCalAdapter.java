package lb.themike10452.hellscorekernelmanagerl.CustomAdapters;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import lb.themike10452.hellscorekernelmanagerl.CustomClasses.SeekBarProgressAdapter;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;

/**
 * Created by Mike on 4/4/2015.
 */
public class KCalAdapter extends SeekBarProgressAdapter {

    private View mView;
    private String filePath;
    private SeekBar rs, gs, bs;
    private String _R, _G, _B;
    private TextView rt, gt, bt;

    public KCalAdapter(View container, String file) {
        mView = container;
        filePath = file;

        rs = (SeekBar) findViewById(R.id.kcal_r_seekbar);
        gs = (SeekBar) findViewById(R.id.kcal_g_seekbar);
        bs = (SeekBar) findViewById(R.id.kcal_b_seekbar);

        rt = (TextView) findViewById(R.id.kcal_r_textview);
        gt = (TextView) findViewById(R.id.kcal_g_textview);
        bt = (TextView) findViewById(R.id.kcal_b_textview);

        init();
    }

    private void init() {
        rs.setOnSeekBarChangeListener(this);
        gs.setOnSeekBarChangeListener(this);
        bs.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.kcal_r_seekbar:
                rt.setText(_R = Integer.toString(seekBar.getProgress()));
                break;
            case R.id.kcal_g_seekbar:
                gt.setText(_G = Integer.toString(seekBar.getProgress()));
                break;
            case R.id.kcal_b_seekbar:
                bt.setText(_B = Integer.toString(seekBar.getProgress()));
                break;
        }
    }

    public void reload() {
        if (mView != null && mView.getVisibility() == View.VISIBLE)
            try {
                String[] values = HKMTools.getInstance().readLineFromFile(filePath).split(" ");
                final int r = Integer.parseInt(values[0]);
                final int g = Integer.parseInt(values[1]);
                final int b = Integer.parseInt(values[2]);

                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        rs.setProgress(r);
                        gs.setProgress(g);
                        bs.setProgress(b);

                        rt.setText(_R = Integer.toString(r));
                        gt.setText(_R = Integer.toString(g));
                        bt.setText(_R = Integer.toString(b));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                mView.setVisibility(View.GONE);
            }
    }

    public String readDisplayedValues() {
        return _R.concat(" ").concat(_G).concat(" ").concat(_B);
    }

    public void setDisplayedValues(String rgb) {
        String[] kcal = rgb.split(" ");
        if (kcal.length == 3) {
            rt.setText(_R = kcal[0]);
            gt.setText(_G = kcal[1]);
            bt.setText(_B = kcal[2]);
        }
    }

    public void flush() {
        String str = _R.concat(" ").concat(_G).concat(" ").concat(_B);
        HKMTools.getInstance().addCommand("echo " + str + " > " + filePath);
    }

    private View findViewById(int id) {
        return mView.findViewById(id);
    }

}
