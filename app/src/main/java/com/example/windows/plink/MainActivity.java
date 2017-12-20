package com.example.windows.plink;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 2000;
    private Handler mHandler = new Handler();
    View logoView, titleView2, subHeadingView;
    TextView titleView;
    TextView eventDate;


    private Runnable mEndSplash = new Runnable() {
        public void run() {
            if (!isFinishing()) {
                mHandler.removeCallbacks(this);
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //remove title bar and make activity fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        logoView = findViewById(R.id.logoView);
        titleView = (TextView) findViewById(R.id.titleView);

        //creates a runnable thread
        mHandler.postDelayed(mEndSplash, SPLASH_DURATION_MS);
    }

    //called whenever app is in resume state to show animation
    @Override
    protected void onResume() {
        super.onResume();
        staggeredAnimate();
    }

    //function for splash screen
    private void staggeredAnimate() {
        View[] animatedViews = new View[]{logoView, titleView};
        Interpolator interpolator = new DecelerateInterpolator();

        for (int i = 0; i < animatedViews.length; ++i) {
            View v = animatedViews[i];
            v.setAlpha(0f);
            v.setTranslationY(120);
            v.animate()
                    .setInterpolator(interpolator)
                    .alpha(1.0f)
                    .translationY(0)
                    .setStartDelay(300 + 200 * i)
                    .start();
        }
    }
}

