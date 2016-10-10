package org.codelanka.datacollector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.codelanka.datacollector.model.Place;
import org.codelanka.datacollector.model.Site;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, View.OnClickListener, OnMapReadyCallback {

    protected final int PERMISSIONS_REQUEST_LOCATION = 1;

    private TextView mTxtDisplayName, mTxtLatlng;
    private EditText mEditSiteName, mEditProvince, mEditDistrict, mEditDsDivision;
    private EditText mEditGnDivision, mEditNearestTown, mEditLatitude, mEditLongitude;
    private EditText mEditNameOfOwner, mEditNameOfUser, mEditDescription;
    private Button mBtnSubmit;
    private Spinner mSpinnerCategory;
    private FirebaseUser mUser;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private GoogleMap mMap;
    private String mDisplayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        mUser = auth.getCurrentUser();
        mDisplayName = mUser.getDisplayName();

        mTxtDisplayName = (TextView) findViewById(R.id.txt_name);
        mTxtLatlng = (TextView) findViewById(R.id.txt_latlng);
        mEditSiteName = (EditText) findViewById(R.id.edit_site_name);
        mEditProvince = (EditText) findViewById(R.id.edit_province);
        mEditDistrict = (EditText) findViewById(R.id.edit_district);
        mEditDsDivision = (EditText) findViewById(R.id.edit_ds_division);
        mEditGnDivision = (EditText) findViewById(R.id.edit_gn_division);
        mEditNearestTown = (EditText) findViewById(R.id.edit_nearest_town);
        mEditNameOfOwner = (EditText) findViewById(R.id.edit_name_of_owner);
        mEditNameOfUser = (EditText) findViewById(R.id.edit_name_of_user);
        mEditDescription = (EditText) findViewById(R.id.edit_description);
        mSpinnerCategory = (Spinner) findViewById(R.id.spinner_category);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mBtnSubmit.setOnClickListener(this);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestEmail().requestId().build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .addApi(LocationServices.API)
                .build();

        mTxtDisplayName.setText(mDisplayName);

        // populate spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCategory.setAdapter(adapter);

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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        getLastLocation();
    }

    private void getLastLocation() {
        //noinspection MissingPermission
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null)
            return;
        if (mMap == null)
            return;

        LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13));
        mMap.addMarker(new MarkerOptions()
                            .title("My Location")
                            .snippet("My Current Location")
                            .position(currentLocation));
        // Set latlng in textview
        mTxtLatlng.setText("Lat:" + String.valueOf(mLastLocation.getLatitude()) + " Lng:" + String.valueOf(mLastLocation.getLongitude()));

        // Set nearest city using lat, lng
        try {
            setNearestCity(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Access coarse location granted
                    getLastLocation();
                } else {
                    // Permission denied
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                writeNewValues();
                break;
        }
    }

    private void writeNewValues() {
        // Save to firebase db
        String username = mUser.getDisplayName();
        String email = mUser.getEmail();
        String siteName = mEditSiteName.getText().toString();
        String category = mSpinnerCategory.getSelectedItem().toString();
        String province = mEditProvince.getText().toString();
        String district = mEditDistrict.getText().toString();
        String dsDivision = mEditDsDivision.getText().toString();
        String gnDivision = mEditGnDivision.getText().toString();
        String nearestTown = mEditNearestTown.getText().toString();
        double lat = mLastLocation.getLatitude();
        double lng = mLastLocation.getLongitude();
        String nameOfOwner = mEditNameOfOwner.getText().toString();
        String nameOfUser = mEditNameOfUser.getText().toString();
        String description = mEditDescription.getText().toString();

        /**
         * TODO
         * Input validation
         */

        // Site details
        Site siteModel = new Site(
                username, email, siteName, category, province, district, dsDivision, gnDivision,
                nearestTown, nameOfOwner, nameOfUser, description
        );
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().push();
        mDatabase.setValue(siteModel);

        // Place child
        Place placeModel = new Place(mDatabase.getKey(), lat, lng, siteName);
        DatabaseReference mDataPlace = FirebaseDatabase.getInstance().getReference().child("places").push();
        mDataPlace.setValue(placeModel);

        Toast.makeText(this, "New data written successfully", Toast.LENGTH_LONG).show();

        // Reset the input fields
        resetInputs();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        getLastLocation();
    }

    /**
     * Reset the input fields
     */
    private void resetInputs() {
        mEditSiteName.setText(null);
        mEditProvince.setText(null);
        mEditDistrict.setText(null);
        mEditDsDivision.setText(null);
        mEditGnDivision.setText(null);
        mEditNearestTown.setText(null);
        mEditNameOfOwner.setText(null);
        mEditNameOfUser.setText(null);
        mEditDescription.setText(null);
    }

    /**
     * Get the nearest city using lat, lng
     */
    private void setNearestCity(double lat, double lng) throws IOException {
        Geocoder geocoder = new Geocoder(this);

        List<Address> addrs = geocoder.getFromLocation(lat, lng, 1);
        if (addrs.size() > 0) {
            mEditNearestTown.setText(addrs.get(0).getLocality());
        }
    }

}
