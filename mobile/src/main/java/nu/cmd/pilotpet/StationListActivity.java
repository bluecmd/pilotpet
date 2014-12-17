package nu.cmd.pilotpet;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nu.cmd.pilotpet.dao.MetarDao;
import nu.cmd.pilotpet.dao.impl.AviationweathergovMetarDao;
import nu.cmd.pilotpet.models.Metar;


/**
 * An activity representing a list of Stations. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link StationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link StationListFragment} and the item details
 * (if present) is a {@link StationDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link StationListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class StationListActivity extends ActionBarActivity
        implements StationListFragment.Callbacks, GoogleApiClient.ConnectionCallbacks,
        LocationListener, AdapterView.OnItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private MetarDao mMetarDao;
    private ProgressBar mProgress;
    private Location mLastLocation;
    private int mSelectedDistance;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.actionbar, menu);

        Spinner spinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.distances_array, android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        menu.findItem(R.id.menu_distance_item).setActionView(spinner);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Dependency injection
        mMetarDao = new AviationweathergovMetarDao();



        setContentView(R.layout.activity_station_list);

        if (findViewById(R.id.station_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((StationListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.station_list))
                    .setActivateOnItemClick(true);
        }

        mProgress = (ProgressBar) findViewById(R.id.progressBar);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.checkForPlayServices(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh_button:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Callback method from {@link StationListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(StationDetailFragment.ARG_ITEM_ID, id);
            StationDetailFragment fragment = new StationDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.station_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, StationDetailActivity.class);
            detailIntent.putExtra(StationDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mProgress.setVisibility(View.GONE);

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setFastestInterval(30 * 1000)
                .setInterval(5 * 60 * 1000);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, Util.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Show error dialog
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        mProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String distance_text = (String)parent.getItemAtPosition(position);
        mSelectedDistance = Integer.parseInt(distance_text.split(" ")[0]);
        refresh();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void refresh() {
        if (mGoogleApiClient.isConnected()) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location != null) {
                refreshNearestStationsList(location);
            }
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        Log.i("METAR", "Listing METARs near location: " + location);
        mLastLocation = location;
        refreshNearestStationsList(location);
    }

    private static class MetarByStationIcaoComparator implements Comparator<Metar> {
        @Override
        public int compare(Metar lhs, Metar rhs) {
            return lhs.getStation().getIcaoCode().compareTo(rhs.getStation().getIcaoCode());
        }
    }

    public void refreshNearestStationsList(final Location location) {
        final ArrayAdapter<Metar> stationListAdapter = ((StationListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.station_list)).getStationListAdapter();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgress.setVisibility(View.GONE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                final List<Metar> metars = mMetarDao.getLatestForClosestStations(mSelectedDistance, location);

                // Sort to make sure the items don't move around on screen as the location changes.
                Collections.sort(metars, new MetarByStationIcaoComparator());
                StationListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stationListAdapter.clear();
                        for (Metar m : metars) {
                            stationListAdapter.add(m);
                        }
                    }
                });
                return null;
            }
        }.execute();
    }
}
