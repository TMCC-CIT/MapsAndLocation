package edu.tmcc.cit230.mapsandlocation;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapsActivity";
    private static final int REQUEST_CURRENT_LOCATION = 1;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "requestLocationUpdates";
    private static final String LOCATION_KEY = "location";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "lastUpdatedTimeString";
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private int permissionRequestCount;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    private final LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = true;

    public MapsActivity() {
        mLocationRequest = LocationRequest.create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionRequestCount = 0;
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        updateValuesFromBundle(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        // this code is for demonstrating the Polyline Maps API
        double lat = -34.852;
        double lon = 151.211;
        mLastLocation = new Location("MapsActivity");
        mLastLocation.setLatitude(lat);
        mLastLocation.setLongitude(lon);
        AddMarkerToMap("Sydney, Australia", mLastLocation.getLatitude(), mLastLocation.getLongitude());

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);

    }

    private void AddMarkerToMap(String title, double lat, double lon) {
        LatLng location = new LatLng(lat, lon);
        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(title));
        marker.setTag(0);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation != null) {
            Log.d(MapsActivity.TAG, String.format("Last Known Location: LAT=%s, LON=%s", String.valueOf(mCurrentLocation.getLatitude()), String.valueOf(mCurrentLocation.getLongitude())));
            updateMapUI("Current Location");
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(MapsActivity.TAG, "Potential location change...");
        if(mLastLocation.distanceTo(location) > 0.0f) {
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            if (location != null) {
                Log.d(MapsActivity.TAG, String.format("Last Known Location: LAT=%s, LON=%s", String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude())));
                updateMapUI("Current Location");
                    mLastLocation = mCurrentLocation;
                    mCurrentLocation = location;
            }
        }
    }

    private void updateMapUI(String locationTitle) {
        if(mCurrentLocation == null || mLastLocation == null) {
            return;
        }
        AddMarkerToMap(locationTitle, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        AddPolyLineToMap(mLastLocation, mCurrentLocation);
    }

    private void AddPolyLineToMap(Location mLastLocation, Location mCurrentLocation) {
        if(mCurrentLocation == null || mLastLocation == null) {
            return;
        }
        if(mLastLocation.distanceTo(mCurrentLocation) > 3.0f) { // distance in meters
            mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .add(
                            new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                            new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));
        }
    }

    private void requestPermissions() {
        if(permissionRequestCount < 3) {
            permissionRequestCount++;
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CURRENT_LOCATION
            );
        }
        else {
            Log.e(MapsActivity.TAG, String.format("Attemped to request permissions %d times. User did not grant permission.", permissionRequestCount));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == REQUEST_CURRENT_LOCATION) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions();
            }
        }
    }

    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            Toast.makeText(this,
                    marker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_LONG).show();
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
