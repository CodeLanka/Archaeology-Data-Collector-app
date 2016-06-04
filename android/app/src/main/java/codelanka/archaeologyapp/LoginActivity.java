package codelanka.archaeologyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    public static final int REQ_CODE_SIGN_IN = 0;
    public static final String SHARED_PREFS_TAG = "archaeoapp";
    public static final String SHARED_PREFS_VAL_TOKEN = "id_token";
    public static final String SHARED_PREFS_VAL_NAME = "name";

    private SharedPreferences mSharedPrefs;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount mGoogleSignInAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSharedPrefs = getSharedPreferences(SHARED_PREFS_TAG, MODE_PRIVATE);
        String idToken = mSharedPrefs.getString(SHARED_PREFS_VAL_TOKEN, null);
        if (idToken != null) {
            launchMainActivity();
        }

        // Google sign-in button
        findViewById(R.id.btn_sign_in).setOnClickListener(this);

        String serverClientId = getString(R.string.server_client_id);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestIdToken(serverClientId)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                signIn();
        }
    }

    private void launchMainActivity() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private void signIn() {
        // Show sign-in dialog
        startActivityForResult(
                Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient),
                REQ_CODE_SIGN_IN
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Google sign-in result
        if (requestCode == REQ_CODE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            mGoogleSignInAccount = result.getSignInAccount();
            assert mGoogleSignInAccount != null;
            saveToSharedPrefs(mGoogleSignInAccount);
            launchMainActivity();
        }
    }

    private void saveToSharedPrefs(GoogleSignInAccount signInAccount) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(SHARED_PREFS_VAL_TOKEN, mGoogleSignInAccount.getIdToken());
        Log.d("arch", "id token: " + mGoogleSignInAccount.getIdToken());
        editor.putString(SHARED_PREFS_VAL_NAME, mGoogleSignInAccount.getDisplayName());
        editor.apply();
    }

}
