package com.hk.dropapp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hk.dropapp.Helpers.SharedHelper;
import com.hk.dropapp.R;

public class CalculateActivity extends AppCompatActivity {
    private TextView resultText, rangeText;
    private ImageView imageView;
    private Activity mActivity;
    private String resultString;
    private String rangeString;
    private MediaPlayer mp;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate);
        Intent intent = getIntent();
        resultString = intent.getStringExtra("sonuc");
        rangeString = intent.getStringExtra("aralik");
        bindIDs();
    }

    private void bindIDs() {
        mActivity = this;
        mp = MediaPlayer.create(mActivity,R.raw.beep);
        resultText = findViewById(R.id.calculate_result_text);
        rangeText = findViewById(R.id.calculate_range_text);
        imageView = findViewById(R.id.calculata_image);
        resultText.setText(resultString);
        rangeText.setText(rangeString);

        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.slide_up);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //imageView.setVisibility(View.INVISIBLE);
                if(SharedHelper.getSound(mActivity)){
                    mp.start();
                }
                if(SharedHelper.getVibration(mActivity)){
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        v.vibrate(250);
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.VISIBLE);
                imageView.startAnimation(animation);
                String toast = "" + (Long.parseLong(rangeString.replace(".", "").replace(",","")) / 1000000);
                handler.postDelayed(this, Long.parseLong(toast));

            }
        });

        Toolbar toolbar = findViewById(R.id.calculate_toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}