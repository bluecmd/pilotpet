package nu.cmd.pilotpet;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

public class NumberFragment extends Fragment {

    private String mTitle;
    private int mDials;
    private int mMax;
    private int mDefault;
    private int mMin;
    private View mSavedView = null;

    static public NumberFragment newInstance(String title, int dials) {
        return NumberFragment.newInstance(title, dials, 9, 0, 0);
    }

    static public NumberFragment newInstance(String title, int dials, int max, int min, int def) {
        NumberFragment fragment = new NumberFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putInt("dials", dials);
        bundle.putInt("max", max);
        bundle.putInt("min", min);
        bundle.putInt("default", def);
        fragment.setArguments(bundle);
        return fragment;
    }

    public NumberFragment() {
        mTitle = "<Unknown Title>";
        mDials = 3;
        mMax = 9;
        mMin = 0;
        mDefault = 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);

        if (this.getArguments() != null) {
            mTitle = this.getArguments().getString("title");
            mDials = this.getArguments().getInt("dials");
            mMax = this.getArguments().getInt("max");
            mMin = this.getArguments().getInt("min");
            mDefault = this.getArguments().getInt("default");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mSavedView != null)
            return mSavedView;

        // Inflate the layout for this fragment
        int layout = mDials == 3 ? R.layout.fragment_number3 : R.layout.fragment_number1;
        View view = inflater.inflate(layout, container, false);
        TextView text = (TextView) view.findViewById(R.id.title);
        text.setText(mTitle);

        Log.v("NumberFragment", "onCreateView");

        NumberPicker np1 = (NumberPicker) view.findViewById(R.id.numPicker1);

        if (mDials == 1) {
            setupPicker(np1, mMax, mMin);
            // TODO(bluecmd): Support default for mDials > 1
            np1.setValue(mDefault);
        }
        else
            setupPicker(np1, 9, 0);


        if (mDials >= 3) {
            NumberPicker np10 = (NumberPicker) view.findViewById(R.id.numPicker10);
            NumberPicker np100 = (NumberPicker) view.findViewById(R.id.numPicker100);
            setupPicker(np10, 9, 0);
            setupPicker(np100, mMax, mMin);
        }

        mSavedView = view;
        return view;
    }

    public void setupPicker(NumberPicker np, int max, int min) {
        np.setMaxValue(max);
        np.setMinValue(min);
        np.setWrapSelectorWheel(false);
    }
}
