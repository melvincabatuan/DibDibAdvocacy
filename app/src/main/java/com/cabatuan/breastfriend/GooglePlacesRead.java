package com.cabatuan.breastfriend;

import android.os.AsyncTask;
<<<<<<< HEAD
=======
import android.util.Log;
>>>>>>> 35416f1914e6ec10b7c14a90236881adfd7e4a99

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by cobalt on 11/3/15.
 */
public class GooglePlacesRead extends AsyncTask<Object, Integer, String> {

    private static final String TAG = "GooglePlacesRead helper";
    String googlePlacesData = null;
    GoogleMap googleMap;

    @Override
    protected String doInBackground(Object... inputObj) {
        try {
            googleMap = (GoogleMap) inputObj[0];
            String googlePlacesUrl = (String) inputObj[1];
            Http http = new Http();
            googlePlacesData = http.read(googlePlacesUrl);
        } catch (Exception e) {
<<<<<<< HEAD
            //Log.d("Google Place Read Task", e.toString());
=======
            Log.d("Google Place Read Task", e.toString());
>>>>>>> 35416f1914e6ec10b7c14a90236881adfd7e4a99
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        PlacesDisplay placesDisplay = new PlacesDisplay();
        Object[] toPass = new Object[2];
        toPass[0] = googleMap;

        if(result != null) {
            toPass[1] = result;
            // Debugging: REQUEST_DENIED: "This IP, site or mobile application is not authorized to use this API key."
<<<<<<< HEAD
            //Log.d("Google Place Read:", "toPass[1] = " + toPass[1]);
            placesDisplay.execute(toPass);
        }
        else {
            //Log.d(TAG, "Google Place failed to provide results.");
=======
            Log.d("Google Place Read:", "toPass[1] = " + toPass[1]);
            placesDisplay.execute(toPass);
        }
        else {
            Log.d(TAG, "Google Place failed to provide results.");
>>>>>>> 35416f1914e6ec10b7c14a90236881adfd7e4a99
        }
    }
}