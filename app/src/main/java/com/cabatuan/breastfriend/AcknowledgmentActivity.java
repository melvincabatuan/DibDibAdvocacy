package com.cabatuan.breastfriend;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cobalt on 10/19/15.
 */
public class AcknowledgmentActivity extends AppCompatActivity {

    public final static String TAG = "AcknowledgmentActivity";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Read raw file into string and populate TextView
        InputStream iFile = getResources().openRawResource(R.raw.help);

        try {
            TextView helpText = (TextView) findViewById(R.id.TextView_HelpText);
            String strFile = inputStreamToString(iFile);
            helpText.setText(strFile);

        } catch (Exception e) {
            // Handle Exception, i.e. toast cannot open file
            //Log.e(TAG,"ERROR: Cannot open file");
        }
    }

    /**
     * Converts an input stream to a string
     *
     * @param is The {@code InputStream} object to read from
     * @return A {@code String} object representing the string for of the input
     * @throws IOException Thrown on read failure from the input
     */
    public String inputStreamToString(InputStream is) throws IOException {
        StringBuffer sBuffer = new StringBuffer();
        DataInputStream dataIO = new DataInputStream(is);
        String strLine = null;

        while ((strLine = dataIO.readLine()) != null) {
            sBuffer.append(strLine + "\n");
        }

        dataIO.close();
        is.close();

        return sBuffer.toString();
    }
}