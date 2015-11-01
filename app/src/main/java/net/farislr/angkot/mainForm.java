package net.farislr.angkot;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.location.Location;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;



public class mainForm extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    GoogleMap mMap;
    Location location;
    private GoogleApiClient googleApiClient;
    private LocationRequest mLocationRequest;
    public static final String TAG = mainForm.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    CharSequence mTitle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_form);
        createMapView();
        buildGoogleApiClient();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                (Toolbar) findViewById(R.id.tool_bar));

        setSupportActionBar(mNavigationDrawerFragment.mToolbar);

        ImageButton myLocBtn = (ImageButton) findViewById(R.id.cusMyLocation);
        myLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (googleApiClient.isConnected()) {
                        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                        handleBtnLocation(location);
                    }
                } catch (NullPointerException exception) {
                    Log.e("LocBtn", exception.toString());
                }
            }
        });


    }

    @Override
    public void onNavigationDrawerItemSelected(TextView item, int position) {
        switch (position) {
            case 0:
                if (item.isActivated()) {
                    item.setActivated(true);
                    mMap.setTrafficEnabled(true);
                } else {
                    item.setActivated(false);
                    mMap.setTrafficEnabled(false);
                }
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        createMapView();
        googleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    public void createMapView() {

        try {
            if (mMap == null) {
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();
                if (mMap != null) {
                    mapLoc();
                }
            }
        } catch (NullPointerException exception) {
            Log.e("mapApp", exception.toString());
        }
    }

    private void mapLoc() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));


    }


    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        float zoomLevel = (float) 14;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }

    private void handleBtnLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        float zoomLevel = (float) 14;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main_form, menu);
            //restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.searchbar) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (location == null) {
            // get last location device
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
            }
            else {
                handleNewLocation(location);
            }
            Log.i(TAG, "Location services connected.");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    public boolean onKeyDown (int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.exit)
                    .setMessage(R.string.really_exit)
                    .setPositiveButton(R.string.yes_exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //stop Activity
                            mainForm.this.finish();
                        }
                    })
                    .setNegativeButton(R.string.no_exit, null)
                    .show();

            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
