package com.cabatuan.breastfriend;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

/**
 * Created by cobalt on 11/3/15.
 */
public class PlayVideoActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_play_video);
            playVideo();
        }

        private void playVideo(){
            VideoView videoview = (VideoView) findViewById(R.id.video);
            videoview.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bse_dr_joson));
            videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        }
    }