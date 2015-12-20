package com.cabatuan.breastfriend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Created by cobalt on 10/22/15.
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_splash);
        //playSound Service (ended on Main activity destroy)
        startService(new Intent(this, BackgroundSound.class));
        startAnimating();
    }

    /**
     * Helper method to start the animation on the splash screen
     */
    private void startAnimating() {
        // Fade in top title
        TextView logo1 = (TextView) findViewById(R.id.TextViewTopTitle);
        Animation fade1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo1.startAnimation(fade1);
        // Fade in bottom title after a built-in delay.
        TextView logo2 = (TextView) findViewById(R.id.TextViewBottomTitle);
        Animation fade2 = AnimationUtils.loadAnimation(this, R.anim.fade_in2);
        logo2.startAnimation(fade2);
        // Transition to Main Menu when bottom title finishes animating
        fade2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                // The animation has ended, transition to the Main Menu screen
                startActivity(new Intent(SplashActivity.this,
                        MainActivity.class));
                SplashActivity.this.finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
        // Load animations for all views within the TableLayout
        Animation spinin = AnimationUtils.loadAnimation(this,
                R.anim.custom_anim);
        LayoutAnimationController controller = new LayoutAnimationController(
                spinin);
        TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            row.setLayoutAnimation(controller);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Stop the animation
        TextView logo1 = (TextView) findViewById(R.id.TextViewTopTitle);
        logo1.clearAnimation();
        TextView logo2 = (TextView) findViewById(R.id.TextViewBottomTitle);
        logo2.clearAnimation();
        TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            row.clearAnimation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start animating at the beginning so we get the full splash screen
        // experience
        startAnimating();
    }
}