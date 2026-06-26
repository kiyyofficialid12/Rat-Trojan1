package com.nexa.bug;
import android.content.Intent; import android.os.Bundle; import android.os.Handler; import android.widget.ProgressBar; import android.widget.TextView; import androidx.appcompat.app.AppCompatActivity;
public class SplashActivity extends AppCompatActivity {
    ProgressBar pb; TextView tv; int p=0; Handler h=new Handler();
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.splash_activity);
        pb=findViewById(R.id.progressBar); tv=findViewById(R.id.tvProgress);
        h.postDelayed(new Runnable(){public void run(){if(p<100){p+=3;pb.setProgress(p);tv.setText(p+"%");h.postDelayed(this,50);}else{startActivity(new Intent(SplashActivity.this,LoginActivity.class));finish();}}},50);
    }
}
