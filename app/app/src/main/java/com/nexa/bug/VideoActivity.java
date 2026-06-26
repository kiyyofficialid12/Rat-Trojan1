package com.nexa.bug;
import android.content.Intent; import android.os.Bundle; import android.webkit.WebChromeClient; import android.webkit.WebView; import android.webkit.WebViewClient; import android.widget.Button; import androidx.appcompat.app.AppCompatActivity;
public class VideoActivity extends AppCompatActivity {
    WebView webView;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.video_activity);
        webView=findViewById(R.id.webView);
        Button skip=findViewById(R.id.btnSkip);
        webView.getSettings().setJavaScriptEnabled(true); webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        String videoUrl="https://storage.googleapis.com/store-screenapp-production/vid/6a3e87c1569c8f8eeeed48f0/4550dae1-a531-4768-ae1f-cd679b9b82f8.mp4";
        String html="<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><style>body{margin:0;padding:0;background:#000;display:flex;justify-content:center;align-items:center;height:100vh;}video{width:100%;height:100%;object-fit:contain;}</style></head><body><video src='"+videoUrl+"' autoplay playsinline controls style='width:100%;height:100%;'></video></body></html>";
        webView.loadDataWithBaseURL(null,html,"text/html","UTF-8",null);
        skip.setOnClickListener(v->{startActivity(new Intent(VideoActivity.this,MainActivity.class));finish();});
    }
}
