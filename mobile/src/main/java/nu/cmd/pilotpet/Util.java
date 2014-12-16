package nu.cmd.pilotpet;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by aa on 12/16/2014.
 */
public class Util {
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static boolean checkForPlayServices(FragmentActivity activity) {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(activity);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("PilotPet", "Google Play Services detected.");
            return true;
        } else {
            Log.w("PilotPet", "Failed to detect Google Play Services!");
            // Google Play services was not available for some reason
            GooglePlayServicesUtil.showErrorDialogFragment(
                    resultCode, activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            return false;
        }
    }

}
