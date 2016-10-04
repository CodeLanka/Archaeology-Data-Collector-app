package org.codelanka.datacollector;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
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

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, View.OnClickListener, TextWatcher, OnMapReadyCallback {

    private TextView mTxtDisplayName;
    private EditText mEditSiteName, mEditProvince, mEditDistrict, mEditDsDivision;
    private EditText mEditGnDivision, mEditNearestTown, mEditLatitude, mEditLongitude;
    private EditText mEditNameOfOwner, mEditNameOfUser, mEditDescription;
    private Button mBtnSubmit;
    private Spinner mSpinnerCategory;
    private GoogleApiClient mGoogleApiClient;
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
        FirebaseUser user = auth.getCurrentUser();
        mDisplayName = user.getDisplayName();

        mTxtDisplayName = (TextView) findViewById(R.id.txt_name);
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
                .build();

        mTxtDisplayName.setText(mDisplayName);
        mEditSiteName.addTextChangedListener(this);

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
        displayCurrentLocation();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("arch", "got a map");
    }

    public void displayCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange (Location location) {
                LatLng loc = new LatLng (location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            }
        };
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
    }
}
