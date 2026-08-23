package com.keysmi.cinema;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    // Dedicated Local LAN Server URL for TV / FireStick
    public static final String CINEMA_URL = "http://192.168.68.116:5055";
    
    private WebView mWebView;
    private FrameLayout mCustomViewContainer;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep TV Screen On & Fullscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        mWebView = findViewById(R.id.cinemaWebView);
        mCustomViewContainer = findViewById(R.id.customViewContainer);

        configureWebView();

        // Directly load local LAN server
        mWebView.loadUrl(CINEMA_URL);
    }

    private void configureWebView() {
        mWebView.setBackgroundColor(Color.BLACK);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // Android TV Chrome User Agent
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 12; Android TV Build/STTE.220623.001) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.105 Safari/537.36 KeySmiCinema/2.2.0");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request != null && request.isForMainFrame()) {
                    showErrorScreen();
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                showErrorScreen();
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                mCustomView = view;
                mCustomViewCallback = callback;
                mCustomViewContainer.addView(view, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mCustomViewContainer.setVisibility(View.VISIBLE);
                mWebView.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                if (mCustomView == null) return;
                mCustomViewContainer.removeView(mCustomView);
                mCustomView = null;
                mCustomViewContainer.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
                if (mCustomViewCallback != null) {
                    mCustomViewCallback.onCustomViewHidden();
                    mCustomViewCallback = null;
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return true;
            }
        });
    }

    private void showErrorScreen() {
        String errorHtml = "<html><body style='background:#0b0d14;color:#fff;font-family:sans-serif;display:flex;flex-direction:column;align-items:center;justify-content:center;height:100vh;margin:0;text-align:center;'>"
                + "<h1 style='color:#e50914;font-size:2.5rem;margin-bottom:8px;'>KeySmi Cinema</h1>"
                + "<p style='color:#94a3b8;font-size:1.2rem;margin-bottom:24px;'>Connecting to KeySmi Server at 192.168.68.116:5055</p>"
                + "<div style='background:#161b26;border:1px solid rgba(255,255,255,0.1);padding:32px;border-radius:16px;max-width:550px;'>"
                + "<p style='margin:0 0 16px 0;font-size:1.1rem;color:#e2e8f0;'>Make sure your PC server is running on the same WiFi:</p>"
                + "<code style='background:#0f172a;color:#38bdf8;padding:12px 20px;border-radius:8px;font-size:1.3rem;display:inline-block;letter-spacing:1px;font-weight:bold;'>http://192.168.68.116:5055</code>"
                + "<br><br><button onclick='location.href=\"" + CINEMA_URL + "\"' style='background:#e50914;color:#fff;border:none;padding:14px 36px;border-radius:10px;font-size:1.2rem;cursor:pointer;font-weight:bold;margin-top:12px;'>RECONNECT</button>"
                + "</div>"
                + "</body></html>";
        mWebView.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mCustomView != null) {
                WebChromeClient.CustomViewCallback cb = mCustomViewCallback;
                if (cb != null) cb.onCustomViewHidden();
                return true;
            }
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}