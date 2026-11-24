package com.mt.chess;
import android.app.Activity;
import android.os.Bundle;
import android.view.WindowInsets;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import androidx.webkit.WebViewAssetLoader;
public class MainActivity extends Activity {
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  WebView web = new WebView(this);
  web.setBackgroundColor(0xff101815);
  web.getSettings().setJavaScriptEnabled(true);
  web.getSettings().setDomStorageEnabled(true);
  web.getSettings().setAllowFileAccess(false);
  web.getSettings().setAllowContentAccess(false);
  WebViewAssetLoader loader = new WebViewAssetLoader.Builder().addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
  web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
    WebResourceResponse res = loader.shouldInterceptRequest(req.getUrl());
    return res != null ? res : new WebResourceResponse("text/plain","UTF-8",null);
   }
   @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req){return true;}
  });
  setContentView(web);
  if(android.os.Build.VERSION.SDK_INT >= 35) {
   web.setOnApplyWindowInsetsListener((v,i)-> {
    android.graphics.Insets bars=i.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
    v.setPadding(bars.left,bars.top,bars.right,bars.bottom);
    return WindowInsets.CONSUMED;
   });
   web.requestApplyInsets();
  }
  web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
 }
}
