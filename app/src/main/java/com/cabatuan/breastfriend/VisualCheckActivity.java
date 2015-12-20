package com.cabatuan.breastfriend;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by cobalt on 10/19/15.
 */
public class VisualCheckActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{


    public final static String TAG = "VisualCheckActivity";
    private ViewPager viewPager = null;
    private MyPagerAdapter myPagerAdapter = null;
    private TextToSpeech tts = null;
    private boolean ttsLoaded = false;
    private String[] infotext;
    private int endPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_check);

        // Extract String info array for slides
        infotext = getResources().getStringArray(R.array.visualinfotext);

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

                // Initialize view pager
                initializePager();
            }
        }
        else{
            Toast.makeText(this, "TTS Initialization failed", Toast.LENGTH_LONG).show();
        }
    }




    public void initializePager(){
        viewPager = (ViewPager)findViewById(R.id.myviewpager);
        myPagerAdapter = new MyPagerAdapter();
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                readMessage(infotext[position], "page" + position);
                if (position == myPagerAdapter.getCount()-1){
                    ++endPage;
                }
                if (endPage>2) {
                    endPage = 0;
                    String message = getString(R.string.title_visual_check) + " completed!";
                    showToast(message);
                    NavUtils.navigateUpTo(VisualCheckActivity.this, new Intent(VisualCheckActivity.this, MainActivity.class));
                }
            }

            private void showToast(String message) {

                // Inflate the Layout
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.mytoast,
                        (ViewGroup) findViewById(R.id.custom_toast_layout));

                // Retrieve the ImageView and TextView
                ImageView iv = (ImageView) layout.findViewById(R.id.toastImageView);
                TextView text = (TextView) layout.findViewById(R.id.textToShow);

                // Set the image
                iv.setImageResource(R.mipmap.ic_eye);

                // Set the Text to show in TextView
                text.setText(message);
                text.setBackgroundColor(Color.BLACK);

                final Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();
            }

            @Override
            public void onPageSelected(int position) {
                readMessage(infotext[position], "page" + position);
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    public void readMessage(String message, String utteranceId) {
        HashMap<String, String> params = new HashMap<>();
        if (ttsLoaded) {
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    @Override
    public void onPause() {
        if (tts != null) {
            tts.stop();
        }
        super.onPause();
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
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        // Go back to MainActivity
        NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.basic_facts_menu, menu);

        if (viewPager == null){ // Solves null pointer exception
            initializePager();
        }
        menu.findItem(R.id.action_previous).setEnabled(viewPager.getCurrentItem() > 0);

        // Add either a "next" or "finish" button to the action bar, depending on which page
        // is currently selected.
        MenuItem item = menu.add(Menu.NONE, R.id.action_next, Menu.NONE,
                (viewPager.getCurrentItem() == myPagerAdapter.getCount() - 1)
                        ? R.string.action_finish
                        : R.string.action_next);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Log.d(TAG,"home was pressed");
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                return true;

            case R.id.action_previous:
                //Log.d(TAG,"action_previous was pressed");
                // Go to the previous step in the wizard. If there is no previous step,
                // setCurrentItem will do nothing.
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                return true;

            case R.id.action_next:
                //Log.d(TAG,"action_next was pressed");
                // Advance to the next step in the wizard. If there is no next step,
                // go back to MainActivity.
                if (viewPager.getCurrentItem() == myPagerAdapter.getCount() - 1)
                    NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                else
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }




    private class MyPagerAdapter extends PagerAdapter {

        private final int NumberOfPages = infotext.length;

        private int pos;

        private int[] res = {
                R.drawable.size_change,
                R.drawable.redness,
                R.drawable.discharge,
                R.drawable.armpit_swelling,
                R.drawable.lump_or_thickening,
                R.drawable.texture_change,
                R.drawable.inverted_nipple,
                R.drawable.constant_pain};

        private int[] backgroundcolor = {
                0xFFFFFFFF,
                0xFFFFFFFF,
                0xFFFFFFFF,
                0xFFFFFFFF,
                0xFFFFFFFF,
                0xFFFFFFFF,
                0xFFFFFFFF,
                0xFFFFFFFF
        };


        @Override
        public int getCount() {
            return NumberOfPages;
        }

        public int getPosition() {
            return pos;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            pos = position;

            TextView textView = new TextView(VisualCheckActivity.this);
            textView.setTextColor(getResources().getColor(R.color.pink));
            textView.setTextSize(24);
            textView.setGravity(Gravity.CENTER);
            textView.setText(String.valueOf(position + 1) + ". " + infotext[position]);

            ImageView imageView = new ImageView(VisualCheckActivity.this);
            imageView.setImageResource(res[position]);
            ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(imageParams);

            LinearLayout layout = new LinearLayout(VisualCheckActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layout.setBackgroundColor(backgroundcolor[position]);
            layout.setVerticalGravity(Gravity.CENTER_VERTICAL);
            layout.setLayoutParams(layoutParams);
            layout.addView(textView);
            layout.addView(imageView);

            final int page = position;

            // Listen for clicks
            layout.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    readMessage(infotext[page],"page" + page);
                }});

            container.addView(layout);
            return layout;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout)object);
        }


    }
}
