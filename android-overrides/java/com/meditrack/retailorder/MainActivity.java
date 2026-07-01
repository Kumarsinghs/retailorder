package com.meditrack.retailorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.getcapacitor.BridgeActivity;

/**
 * MediTrack Retail Order - main native shell.
 *
 * Responsibilities beyond the Capacitor default:
 *  - Native "no internet" overlay that appears/disappears automatically
 *    (in addition to the web app's own offline banner / local-first sync).
 *  - Hardware back button delegates to WebView history first (handled by
 *    Capacitor's BridgeActivity out of the box); a "press back again to
 *    exit" guard is added on the root screen so users don't leave the app
 *    by accident mid-order.
 *  - WebView tuned for smooth scrolling / native-like performance.
 */
public class MainActivity extends BridgeActivity {

    private FrameLayout offlineOverlay;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private long lastBackPressTime = 0;
    private static final long BACK_PRESS_EXIT_WINDOW_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tuneWebViewForPerformance();
        buildOfflineOverlay();
        observeConnectivity();
    }

    /** Smooth scrolling + native-like rendering. */
    private void tuneWebViewForPerformance() {
        WebView webView = this.bridge.getWebView();
        if (webView == null) return;

        webView.getSettings().setDomStorageEnabled(true);      // localStorage / offline caching
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setCacheMode(android.webkit.WebSettings.LOAD_DEFAULT);
        webView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);   // GPU-accelerated compositing
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
    }

    /** Simple full-screen "You're offline" view, shown only when there is truly no network. */
    private void buildOfflineOverlay() {
        offlineOverlay = new FrameLayout(this);
        offlineOverlay.setBackgroundColor(0xFF0A1628);
        offlineOverlay.setVisibility(View.GONE);

        AppCompatTextView text = new AppCompatTextView(this);
        text.setText("No internet connection\nWaiting to reconnect… your drafts are saved locally.");
        text.setTextColor(0xFFFFFFFF);
        text.setTextSize(16);
        text.setGravity(android.view.Gravity.CENTER);
        text.setPadding(64, 0, 64, 0);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = android.view.Gravity.CENTER;
        offlineOverlay.addView(text, params);

        addContentView(offlineOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void observeConnectivity() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> {
                    if (offlineOverlay != null) offlineOverlay.setVisibility(View.GONE);
                });
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> {
                    if (offlineOverlay != null) offlineOverlay.setVisibility(View.VISIBLE);
                });
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(request, networkCallback);

        // Set correct initial state
        Network active = connectivityManager.getActiveNetwork();
        if (active == null && offlineOverlay != null) {
            offlineOverlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        WebView webView = this.bridge.getWebView();
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }

        // On the root screen: require a second back-press within 2s to exit,
        // to avoid accidentally losing an in-progress order.
        long now = System.currentTimeMillis();
        if (now - lastBackPressTime < BACK_PRESS_EXIT_WINDOW_MS) {
            super.onBackPressed();
        } else {
            lastBackPressTime = now;
            android.widget.Toast.makeText(this, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (IllegalArgumentException ignored) {
            }
        }
        super.onDestroy();
    }
}
