package com.nexa.bug;
import android.content.Intent; import android.content.SharedPreferences; import android.os.Bundle; import android.view.View; import android.widget.Button; import android.widget.EditText; import android.widget.TextView; import androidx.appcompat.app.AppCompatActivity;
public class LoginActivity extends AppCompatActivity {
    EditText etKey, etPass; TextView tvError; SharedPreferences prefs;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.login_activity);
        etKey=findViewById(R.id.etKey); etPass=findViewById(R.id.etPassword); tvError=findViewById(R.id.tvError);
        Button btn=findViewById(R.id.btnLogin);
        prefs=getSharedPreferences("nexa_bug",MODE_PRIVATE);
        if(prefs.getBoolean("logged_in",false)){startActivity(new Intent(this,VideoActivity.class));finish();return;}
        btn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){String key=etKey.getText().toString().trim();String pass=etPass.getText().toString().trim();if(key.isEmpty()||pass.isEmpty()){tvError.setText("Isi key dan password!");return;}if(key.startsWith("NEXA-")&&key.length()>=12&&pass.length()>=4){prefs.edit().putBoolean("logged_in",true).putString("user_key",key).apply();startActivity(new Intent(LoginActivity.this,VideoActivity.class));finish();}else{tvError.setText("Key atau password salah!");}}});
    }
}
