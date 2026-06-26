package com.nexa.bug;
import android.content.SharedPreferences; import android.os.Bundle; import android.view.View; import android.widget.Button; import android.widget.EditText; import android.widget.Toast; import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*; import org.json.JSONObject; import java.io.IOException;
public class MainActivity extends AppCompatActivity {
    EditText etTarget; String apiBase="http://192.168.1.100:3000"; String currentKey=null; OkHttpClient client=new OkHttpClient();
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_main);
        SharedPreferences prefs=getSharedPreferences("nexa_bug",MODE_PRIVATE);
        if(!prefs.getBoolean("logged_in",false)){finish();return;}
        currentKey=prefs.getString("user_key",null);
        etTarget=findViewById(R.id.etTarget);
        findViewById(R.id.btnBugFI).setOnClickListener(v->sendBug("forclose_invisible"));
        findViewById(R.id.btnBugFH).setOnClickListener(v->sendBug("forclose_hard"));
        findViewById(R.id.btnBugFD).setOnClickListener(v->sendBug("forclose_delay"));
        findViewById(R.id.btnBugID).setOnClickListener(v->sendBug("invisible_delay"));
        findViewById(R.id.btnBugHD).setOnClickListener(v->sendBug("hard_delay"));
        findViewById(R.id.btnBugOH).setOnClickListener(v->sendBug("onehit"));
        findViewById(R.id.btnBugSC).setOnClickListener(v->sendBug("spam_call"));
        findViewById(R.id.btnBugSP).setOnClickListener(v->sendBug("spam_pair"));
    }
    void sendBug(String type){String target=etTarget.getText().toString().trim();if(target.isEmpty()){Toast.makeText(this,"Masukkan target!",Toast.LENGTH_SHORT).show();return;}if(currentKey==null){Toast.makeText(this,"Aktivasi key dulu!",Toast.LENGTH_SHORT).show();return;}JSONObject json=new JSONObject();try{json.put("target",target);json.put("bugType",type);json.put("key",currentKey);}catch(Exception e){}RequestBody body=RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json.toString());Request request=new Request.Builder().url(apiBase+"/api/send-bug").post(body).build();client.newCall(request).enqueue(new Callback(){public void onFailure(Call call,IOException e){runOnUiThread(()->Toast.makeText(MainActivity.this,"Gagal: "+e.getMessage(),Toast.LENGTH_SHORT).show());}public void onResponse(Call call,Response response)throws IOException{if(response.isSuccessful()){runOnUiThread(()->Toast.makeText(MainActivity.this,"✅ Bug "+type+" terkirim!",Toast.LENGTH_SHORT).show());}else{runOnUiThread(()->Toast.makeText(MainActivity.this,"Gagal kirim",Toast.LENGTH_SHORT).show());}}});
    }
}
