package com.hk.dropapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;

import com.hk.dropapp.Helpers.EdgeInsets;
import com.hk.dropapp.Helpers.SharedHelper;
import com.hk.dropapp.R;

import java.text.NumberFormat;

public class CalculateActivity extends AppCompatActivity {

    /** Metronomun en kısa vuruş aralığı (ms). Saniyede 4 vuruştan hızlısı gözle izlenemez. */
    static final long MIN_INTERVAL_MS = 250;
    static final double FAST_DROPS_PER_MIN = 120;
    static final double SLOW_DROPS_PER_MIN = 5;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ImageView imageView;
    private Animation dropAnimation;
    private SoundPool soundPool;
    private int beepId;
    private boolean beepLoaded;
    private Vibrator vibrator;
    private long intervalMs;
    private long nextTickAt;
    private boolean running;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            imageView.setVisibility(View.VISIBLE);
            imageView.clearAnimation();
            imageView.startAnimation(dropAnimation);
            // Zamanı sabit bir başlangıca göre hesapla ki aralık kaymasın.
            nextTickAt += intervalMs;
            long now = SystemClock.uptimeMillis();
            if (nextTickAt < now) nextTickAt = now + intervalMs;
            handler.postAtTime(this, nextTickAt);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_calculate);
        EdgeInsets.apply(findViewById(R.id.calculate_root), findViewById(R.id.calculate_appbar));

        Intent intent = getIntent();
        double dropsPerMin = intent.getDoubleExtra(MainActivity.EXTRA_DROPS_PER_MIN, Double.NaN);
        double secondsPerDrop = intent.getDoubleExtra(MainActivity.EXTRA_SECONDS_PER_DROP, Double.NaN);
        double mlPerHour = intent.getDoubleExtra(MainActivity.EXTRA_ML_PER_HOUR, Double.NaN);
        double conc = intent.getDoubleExtra(MainActivity.EXTRA_CONCENTRATION, Double.NaN);
        boolean concIsUnits = intent.getBooleanExtra(MainActivity.EXTRA_CONC_IS_UNITS, false);

        Toolbar toolbar = findViewById(R.id.calculate_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ((TextView) findViewById(R.id.calculate_result_text)).setText(format(dropsPerMin, 1));
        ((TextView) findViewById(R.id.calculate_range_text)).setText(format(secondsPerDrop, 2));
        ((TextView) findViewById(R.id.calculate_mlh_text)).setText(format(mlPerHour, 2));
        ((TextView) findViewById(R.id.calculate_conc_text)).setText(formatConcentration(conc, concIsUnits));

        TextView warning = findViewById(R.id.calculate_warning_text);
        if (dropsPerMin > FAST_DROPS_PER_MIN) {
            warning.setText(R.string.uyari_cok_hizli);
            warning.setVisibility(View.VISIBLE);
        } else if (dropsPerMin < SLOW_DROPS_PER_MIN) {
            warning.setText(R.string.uyari_cok_yavas);
            warning.setVisibility(View.VISIBLE);
        }

        imageView = findViewById(R.id.calculata_image);
        intervalMs = Math.max(MIN_INTERVAL_MS, Math.round(secondsPerDrop * 1000.0));
        if (intervalMs <= 0 || Double.isNaN(secondsPerDrop)) intervalMs = 1000; // güvenlik

        dropAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        // Damla bir sonraki vuruştan önce yere düşsün.
        dropAnimation.setDuration(Math.min(1000, (long) (intervalMs * 0.8)));
        dropAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation a) {
            }

            @Override
            public void onAnimationEnd(Animation a) {
                if (running) feedback();
            }

            @Override
            public void onAnimationRepeat(Animation a) {
            }
        });

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> beepLoaded = status == 0);
        beepId = soundPool.load(this, R.raw.beep, 1);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void feedback() {
        if (SharedHelper.getSound(this) && beepLoaded) {
            soundPool.play(beepId, 1f, 1f, 1, 0, 1f);
        }
        if (SharedHelper.getVibration(this) && vibrator != null && vibrator.hasVibrator()) {
            long ms = Math.min(150, intervalMs / 3);
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(ms);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        nextTickAt = SystemClock.uptimeMillis();
        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    @Override
    protected void onPause() {
        // Ekrandan çıkınca metronom durur (v1.0'da arka planda bip sesi devam ediyordu).
        running = false;
        handler.removeCallbacks(tick);
        imageView.clearAnimation();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        super.onDestroy();
    }

    private static String format(double d, int maxFraction) {
        if (Double.isNaN(d) || Double.isInfinite(d)) return "—";
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(maxFraction);
        return nf.format(d);
    }

    private String formatConcentration(double conc, boolean isUnits) {
        if (isUnits) return getString(R.string.conc_u_ml, format(conc, 3));
        if (conc >= 1000) return getString(R.string.conc_mg_ml, format(conc / 1000.0, 3));
        return getString(R.string.conc_mcg_ml, format(conc, 3));
    }
}
