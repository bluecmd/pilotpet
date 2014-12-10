package nu.cmd.pilotpet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;


public class WearActivity extends Activity implements
        DelayedConfirmationView.DelayedConfirmationListener {

    private static final String TAG = "WearActivity";

    private DismissOverlayView mDismissOverlay;
    private GestureDetector mDetector;
    private DelayedConfirmationView mDelayedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mDismissOverlay = new DismissOverlayView(WearActivity.this);
                stub.addView(mDismissOverlay,new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT));

                mDismissOverlay.setIntroText("Long press to exit");
                mDismissOverlay.showIntroIfNecessary();


                mDelayedView =
                        (DelayedConfirmationView) findViewById(R.id.delayed_confirm);
                mDelayedView.setVisibility(View.GONE);
            }
        });

        // Configure a gesture detector
        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent ev) {
                Log.v(TAG, "Long press, user trying to exit?");
                mDismissOverlay.show();
            }
            public boolean onDoubleTap(MotionEvent ev) {
                Log.v(TAG, "Double tap, user is done with step");
                stepDone();
                return true;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mDetector.onTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    /*
        private CardFragment mCardFragment;

            mCardFragment = CardFragment.create("Step Done", "Time recorded");

    public void stepDone() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (mCardFragment.isVisible())
          fragmentTransaction.remove(mCardFragment);
        else
          fragmentTransaction.add(R.id.frame_layout, mCardFragment);

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
        Log.v(TAG, "Step done");
    }*/

    public void stepDone() {

        // Two seconds to cancel the action
        mDelayedView.setTotalTimeMs(2000);
        // Start the timer
        mDelayedView.start();
        mDelayedView.setListener(this);
        mDelayedView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTimerFinished(View view) {
        Log.v(TAG, "Done!");
        ((DelayedConfirmationView) view).setVisibility(View.GONE);
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Time saved");
        startActivity(intent);
    }

    @Override
    public void onTimerSelected(View view) {
        Log.v(TAG, "Aborted!");
        ((DelayedConfirmationView) view).setListener(null);
        ((DelayedConfirmationView) view).reset();
        ((DelayedConfirmationView) view).setVisibility(View.GONE);
    }
}
