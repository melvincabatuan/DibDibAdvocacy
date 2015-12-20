package com.cabatuan.breastfriend;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends ListActivity implements TextToSpeech.OnInitListener{

    public final static String TAG = "MainActivity";

    private TextToSpeech tts = null;
    private boolean ttsLoaded = false;
    private static TopLevel[] items;

    private static int [] images={R.mipmap.ic_facts,R.mipmap.ic_alert,R.mipmap.ic_check,R.mipmap.ic_eye,R.mipmap.ic_clippy,R.mipmap.ic_video, R.mipmap.ic_location,R.mipmap.ic_about};
    private static String [] titles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TextToSpeech engine initialization + View pager initialization
        tts = new TextToSpeech(this /* context */, this /* listener */);


    }

    @Override
    public void onInit(int status) {

        if(status == TextToSpeech.SUCCESS) {
            int temp = tts.setLanguage(Locale.US);
            if (temp == TextToSpeech.LANG_MISSING_DATA ||
                    temp == TextToSpeech.LANG_NOT_SUPPORTED) {
                //Log.e(TAG, "Language is not available.");
                ttsLoaded = false;
            }
            else {
                ttsLoaded = true;
                tts.setSpeechRate(0.8f);

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {

                    }

                    @Override
                    public void onDone(String utteranceId) {
// Launch the top-level items associated with this list position.
                        int position = Integer.parseInt(utteranceId);
                        startActivity(new Intent(MainActivity.this, items[position].activityClass));
                    }

                    @Override
                    public void onError(String utteranceId) {

                    }
                });

                // Initialize the list menu
                initializeList();
            }
        }
        else{
            Toast.makeText(this, "TTS Initialization failed", Toast.LENGTH_LONG).show();
        }
    }


    public void readMessage(String message, String utteranceId) {
        HashMap<String, String> params = new HashMap<>();
        if (ttsLoaded) {
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    public void initializeList(){
        // Instantiate the list of top level items.
        items = new TopLevel[]{
                new TopLevel(R.string.title_basic_facts, BasicFactsActivity.class),
                new TopLevel(R.string.title_risk_factors, RiskFactorsActivity.class),
                new TopLevel(R.string.title_prevention, DecreaseRiskActivity.class),
                new TopLevel(R.string.title_visual_check, VisualCheckActivity.class),
                new TopLevel(R.string.title_palpation, PalpationActivity.class),
                new TopLevel(R.string.title_camera_based_check, CameraBasedCheckActivity.class),
                new TopLevel(R.string.title_contacts, ContactsActivity.class),
                new TopLevel(R.string.title_credits, AcknowledgmentActivity.class)
        };

        // Extract titles
        titles = new String[items.length];
        for(int position = 0; position < items.length; ++position) {
            titles[position] = items[position].title.toString();
        }

        setListAdapter(new MyListAdapter(this, titles, images));
    }


    @Override
    public void onStop() {
        if (tts != null) {
            tts.stop();
        }
        super.onStop();
    }


    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        MainActivity.this.finish();
    }


    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        readMessage(titles[position], ""+position);
    }


    /**
     * This class describes an individual top level items (the sample title, and the activity class)
     */
    private class TopLevel {
        private CharSequence title;
        private Class<? extends AppCompatActivity> activityClass;

        public TopLevel(int titleResId, Class<? extends AppCompatActivity> activityClass) {
            this.activityClass = activityClass;
            this.title = getResources().getString(titleResId);
        }


        @Override
        public String toString() {
            return title.toString();
        }
    }
}
