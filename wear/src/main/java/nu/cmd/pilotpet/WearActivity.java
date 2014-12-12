package nu.cmd.pilotpet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;


public class WearActivity extends Activity implements
        DelayedConfirmationView.DelayedConfirmationListener {

    private static final String TAG = "WearActivity";

    private static final int INTENT_TIME_SAVED = 1;

    private DismissOverlayView mDismissOverlay;
    private GestureDetector mDetector;
    private DelayedConfirmationView mDelayedView;
    private GridViewPager mPager;
    private PilotGridPagerAdapter mPagerAdapter;
    private DotsPageIndicator mDotsIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);

        // Add a "Delayed touch to exit" view since we catch the swipe-to-exit event
        final BoxInsetLayout stub = (BoxInsetLayout) findViewById(R.id.watch_view);
        mDismissOverlay = new DismissOverlayView(WearActivity.this);
        stub.addView(mDismissOverlay,new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        mDismissOverlay.setIntroText("Long press to exit");
        mDismissOverlay.showIntroIfNecessary();

        mDelayedView =
                (DelayedConfirmationView) findViewById(R.id.delayed_confirm);
        mDelayedView.setVisibility(View.GONE);

        // Configure a gesture detector
        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent ev) {
                Log.v(TAG, "Long press, user trying to exit?");
                mDismissOverlay.show();
            }
            public boolean onDoubleTap(MotionEvent ev) {
                Point point = mPager.getCurrentItem();
                if (mPagerAdapter.isDoubleClickToProgress(point.y, point.x)) {
                    Log.v(TAG, "Double tap, user is done with step");
                    stepDone();
                    return true;
                } else {
                    Log.v(TAG, "Double tap, but no action associated");
                    return false;
                }
            }
        });

        // Add pages to our grid
        mPager = (GridViewPager) findViewById(R.id.pager);
        mPagerAdapter = new PilotGridPagerAdapter(this, getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mDotsIndicator = (DotsPageIndicator)findViewById(R.id.page_indicator);
        mDotsIndicator.setPager(mPager);
        mPagerAdapter.reset(mPager);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mDetector.onTouchEvent(ev)  || super.dispatchTouchEvent(ev);
    }

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
        view.setVisibility(View.GONE);
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Time saved");
        startActivityForResult(intent, INTENT_TIME_SAVED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_TIME_SAVED) {
            // "Time saved" display has finished
            mPagerAdapter.next(mPager);
        }
    }

    @Override
    public void onTimerSelected(View view) {
        Log.v(TAG, "Aborted!");
        ((DelayedConfirmationView) view).setListener(null);
        ((DelayedConfirmationView) view).reset();
        view.setVisibility(View.GONE);
    }
}
