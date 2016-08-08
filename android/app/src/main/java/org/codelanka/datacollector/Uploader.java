package org.codelanka.datacollector;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Upload functions
 */
public class Uploader extends AsyncTask<JSONObject, Void, Boolean>{

    private String mError = null;

    @Override
    protected Boolean doInBackground(JSONObject... jsonObjects) {

        // Open connection
        HttpURLConnection httpConnection;
        try {
            httpConnection = initializeConnection();
        } catch (IOException e) {
            e.printStackTrace();
            mError = "Initialization error. " + e.getLocalizedMessage();
            return false;
        }

        // Upload
        try {
            uploadJSONobject(httpConnection, jsonObjects[0]);
            Log.i("arch", "Response: " + httpConnection.getResponseMessage());
        } catch (IOException e) {
            e.printStackTrace();
            mError = "Upload error. " + e.getLocalizedMessage();
            return false;
        }

        // Disconnect http connection
        httpConnection.disconnect();

        return true;
    }

    public String getError() {
        return mError;
    }

    /*
     * Open http connection to the server
     */
    private HttpURLConnection initializeConnection() throws IOException {
        URL url = new URL("http", "server", 80, "url");     // TODO
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("POST");
        httpConnection.setRequestProperty("Accept", "application/json");
        httpConnection.setRequestProperty("Content-type", "application/json");
        httpConnection.setDoOutput(true);
        httpConnection.connect();
        return httpConnection;
    }

    private void uploadJSONobject(HttpURLConnection httpConnection, JSONObject jsonObject) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(httpConnection.getOutputStream());
        outputStream.write(jsonObject.toString().getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }
}
