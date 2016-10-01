package org.codelanka.datacollector;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, View.OnClickListener, TextWatcher, OnMapReadyCallback,
        LocationListener, GpsStatus.Listener {

    public final String tag = MainActivity.class.getCanonicalName();

    /** Used when Location Permissions are not granted. */
    private final int PERMISSIONS_RESPONSE_CODE = 7591;

    /** The time in milliseconds in which we want to update our position */
    private final int milli_update = 5000; // 5 seconds

    /** The distance we must travel in meters in which we want to update out position */
    private final int meters_distance = 0; // 0 meter

    private TextView mTxtDisplayName;
    private EditText mEditSiteName, mEditProvince, mEditDistrict, mEditDsDivision;
    private EditText mEditGnDivision, mEditNearestTown, mEditLatitude, mEditLongitude;
    private EditText mEditNameOfOwner, mEditNameOfUser, mEditDescription;
    private Button mBtnSubmit;
    private Spinner mSpinnerCategory;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private String mDisplayName;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        FirebaseUser user = auth.getCurrentUser();
        mDisplayName = user.getDisplayName();

        mTxtDisplayName = (TextView) findViewById(R.id.txt_name);
        mEditSiteName = (EditText) findViewById(R.id.edit_site_name);
        mEditProvince = (EditText) findViewById(R.id.edit_province);
        mEditDistrict = (EditText) findViewById(R.id.edit_district);
        mEditDsDivision = (EditText) findViewById(R.id.edit_ds_division);
        mEditGnDivision = (EditText) findViewById(R.id.edit_gn_division);
        mEditNearestTown = (EditText) findViewById(R.id.edit_nearest_town);
        mEditLatitude = (EditText) findViewById(R.id.edit_latitude);
        mEditLongitude = (EditText) findViewById(R.id.edit_longitude);
        mEditNameOfOwner = (EditText) findViewById(R.id.edit_name_of_owner);
        mEditNameOfUser = (EditText) findViewById(R.id.edit_name_of_user);
        mEditDescription = (EditText) findViewById(R.id.edit_description);
        mSpinnerCategory = (Spinner) findViewById(R.id.spinner_category);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mBtnSubmit.setOnClickListener(this);
        mEditLatitude.addTextChangedListener(this);
        mEditLongitude.addTextChangedListener(this);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestEmail().requestId().build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();

        mTxtDisplayName.setText(mDisplayName);

        // populate spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCategory.setAdapter(adapter);

        initLocationServices();
    }

    private void initLocationServices() {

        // Need check if we have permission to do this

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(tag, "ACCESS_FINE_LOCATION is not granted.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_RESPONSE_CODE);
            return;
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationManager.addGpsStatusListener(this);

        // Set the Listener to update the location when we move
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, milli_update,
                meters_distance, this);

        // Disable the User from editing this as they should be updated automatically.
        mEditLongitude.setEnabled(false);
        mEditLatitude.setEnabled(false);
        mEditNearestTown.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_RESPONSE_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Call initLocationServices again.
                initLocationServices();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                try {
                    new Uploader().execute(getJSONObject());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private JSONObject getJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("display_name", mTxtDisplayName.getText().toString().trim());
        jsonObject.put("site_name", mEditSiteName.getText().toString().trim());
        jsonObject.put("category", mSpinnerCategory.getSelectedItem().toString().trim());
        jsonObject.put("province", mEditProvince.getText().toString().trim());
        jsonObject.put("district", mEditDistrict.getText().toString().trim());
        jsonObject.put("ds_division", mEditDsDivision.getText().toString().trim());
        jsonObject.put("gn_division", mEditGnDivision.getText().toString().trim());
        jsonObject.put("nearest_town", mEditNearestTown.getText().toString().trim());
        jsonObject.put("latitude", mEditLatitude.getText().toString().trim());
        jsonObject.put("longitude", mEditLongitude.getText().toString().trim());
        jsonObject.put("name_of_owner", mEditNameOfOwner.getText().toString().trim());
        jsonObject.put("name_of_user", mEditNameOfUser.getText().toString().trim());
        jsonObject.put("description", mEditDescription.getText().toString().trim());

        return jsonObject;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mMap == null | mEditLatitude.getText() == null || mEditLongitude.getText() == null)
            return;

        // check if empty
        if (TextUtils.isEmpty(mEditLatitude.getText()) || TextUtils.isEmpty(mEditLongitude.getText()))
            return;

        float latitude = Float.valueOf(mEditLatitude.getText().toString().trim());
        float longitude = Float.valueOf(mEditLongitude.getText().toString().trim());
        Log.d("arch", "latitude: " + latitude + " longitude: " + longitude);
        // check range
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180)
            return;

        // show position on map
        final LatLng position = new LatLng(latitude, longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(position));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14  ));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("arch", "got a map");
    }

    /**
     * Used to monitor the GPS Status.
     *
     * Currently just being used for LogCat.
     *
     * @param event The GPS Status event.
     */
    public void onGpsStatusChanged(int event) {

        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                Log.i(tag, "GPS - Searching");
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                Log.i(tag, "GPS - Stopped Searching");
                break;

            // Called when we get a satellite fix
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Log.i(tag, "GPS - Locked");

                // Removing the GPS status listener once GPS is locked
                locationManager.removeGpsStatusListener(this);

                break;
        }
    }

    /**
     * Listener that updates whenever our location changes.
     *
     * Triggered by time or distance.
     *
     * @param location The new location object.
     */
    public void onLocationChanged(Location location) {
        Log.i(tag, "Updating Location.");

        if (location != null) {

            if (location.getLongitude() != 0 || location.getLatitude() != 0) {

                mEditLongitude.setText(Double.toString(location.getLongitude()));
                mEditLatitude.setText(Double.toString(location.getLatitude()));

                setCurrentCity(location.getLongitude(), location.getLatitude());

            } else {
                Log.d(tag, "GPS not receiving valid data");
            }

        } else {
            Log.d(tag, "GPS not receiving valid data");
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * Sets the Current City.
     */
    public void setCurrentCity(Double longitude, Double latitude) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {

            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses.size() > 0) {
                Log.d(tag, "City: " + addresses.get(0).getLocality());
                mEditNearestTown.setText(addresses.get(0).getLocality());
                return;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        // We couldn't find a city and need to enable the field again.
        mEditNearestTown.setEnabled(true);
    }
}
