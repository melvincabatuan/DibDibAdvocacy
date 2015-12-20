package com.cabatuan.breastfriend;

/**
 * Created by cobalt on 11/3/15.
 */
<<<<<<< HEAD
=======
import android.util.Log;

>>>>>>> 35416f1914e6ec10b7c14a90236881adfd7e4a99
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http {

    private final String TAG = "Exception reading Http";

    public String read(String httpUrl) throws IOException {
        String httpData = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(httpUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            httpData = stringBuffer.toString();
            bufferedReader.close();
        } catch (Exception e) {
<<<<<<< HEAD
            //Log.d(TAG, e.toString());
=======
            Log.d(TAG, e.toString());
>>>>>>> 35416f1914e6ec10b7c14a90236881adfd7e4a99
        } finally {
            inputStream.close();
            httpURLConnection.disconnect();
        }
        return httpData;
    }
}
