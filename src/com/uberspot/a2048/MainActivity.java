package com.uberspot.a2048;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Locale;

import de.cketti.changelog.dialog.DialogChangeLog;

public class MainActivity extends Activity {

    private static final String MAIN_ACTIVITY_TAG = "2048_MainActivity";

    private WebView mWebView;
    private long mLastBackPress;
    private static final long mBackPressThreshold = 3500;
    private static final String IS_FULLSCREEN_PREF = "is_fullscreen_pref";
    private long mLastTouch;
    private static final long mTouchThreshold = 2000;
    private Toast pressBackToast;

    @SuppressLint({"SetJavaScriptEnabled", "ShowToast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // ✅ Active le debug WebView (laisse sans condition le temps des tests)
        android.webkit.WebView.setWebContentsDebuggingEnabled(true);
        Log.i(MAIN_ACTIVITY_TAG, "WebView debug ENABLED");
        
        super.onCreate(savedInstanceState);

        // Don't show an action bar or title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Enable hardware acceleration
        getWindow().setFlags(LayoutParams.FLAG_HARDWARE_ACCELERATED,
                LayoutParams.FLAG_HARDWARE_ACCELERATED);

        // Apply previous setting about showing status bar or not
        applyFullScreen(isFullScreen());

        // Check if screen rotation is locked in settings
        boolean isOrientationEnabled = false;
        try {
            isOrientationEnabled = Settings.System.getInt(getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION) == 1;
        } catch (SettingNotFoundException e) {
            Log.d(MAIN_ACTIVITY_TAG, "Settings could not be loaded");
        }

        // If rotation isn't locked and it's a LARGE screen then add orientation changes based on sensor
        int screenLayout = getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (((screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE)
                || (screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE))
                && isOrientationEnabled) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        setContentView(R.layout.activity_main);

        // WebView setup
        mWebView = findViewById(R.id.mainWebView);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setRenderPriority(RenderPriority.HIGH);
        settings.setDatabasePath(getFilesDir().getParentFile().getPath() + "/databases");

        // Inject background override after each page load
        mWebView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                // applique (native + CSS) en fonction du flag
                applyBackgroundFromFlag();
            }
        });

        // If there is a previous instance restore it in the webview
        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            // Load webview with current Locale language
            mWebView.loadUrl("file:///android_asset/2048/index.html?lang=" + Locale.getDefault().getLanguage());
        }

        Toast.makeText(getApplication(), R.string.toggle_fullscreen, Toast.LENGTH_SHORT).show();
        // Set fullscreen toggle on webview LongClick
        mWebView.setOnTouchListener((v, event) -> {
            // Implement a long touch action by comparing
            // time between action up and action down
            long currentTime = System.currentTimeMillis();
            if ((event.getAction() == MotionEvent.ACTION_UP)
                    && (Math.abs(currentTime - mLastTouch) > mTouchThreshold)) {
                boolean toggledFullScreen = !isFullScreen();
                saveFullScreen(toggledFullScreen);
                applyFullScreen(toggledFullScreen);
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mLastTouch = currentTime;
            }
            // return so that the event isn't consumed but used by the webview as well
            return false;
        });

        pressBackToast = Toast.makeText(getApplicationContext(), R.string.press_back_again_to_exit,
                Toast.LENGTH_SHORT);

        // Met aussi la couleur dès maintenant (avant la fin de chargement) pour le décor
        applyBackgroundFromFlag();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ré-applique (utile si le flag a changé pendant que l’app était en pause)
        applyBackgroundFromFlag();
        mWebView.loadUrl("file:///android_asset/2048/index.html?lang=" + Locale.getDefault().getLanguage());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mWebView.saveState(outState);
    }

    /**
     * Sauve le mode plein écran dans les SharedPreferences
     */
    private void saveFullScreen(boolean isFullScreen) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(IS_FULLSCREEN_PREF, isFullScreen);
        editor.apply();
    }

    private boolean isFullScreen() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(IS_FULLSCREEN_PREF, true);
    }

    /**
     * Active/désactive le plein écran via le flag de fenêtre
     */
    private void applyFullScreen(boolean isFullScreen) {
        if (isFullScreen) {
            getWindow().clearFlags(LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * Empêche la fermeture accidentelle via back
     */
    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - mLastBackPress) > mBackPressThreshold) {
            pressBackToast.show();
            mLastBackPress = currentTime;
        } else {
            pressBackToast.cancel();
            super.onBackPressed();
        }
    }

    // ---------- Background helpers ----------

    private void applyBackgroundFromFlag() {
        String value = App.flags.backgroundColor.getValue();

        // 1) couleur int pour les vues natives
        int color = colorFor(value);

        // Applique au decorView et à la WebView (fond de vue)
        View root = getWindow().getDecorView();
        root.setBackgroundColor(color);
        if (mWebView != null) {
            mWebView.setBackgroundColor(color);
        }

        // 2) injection CSS dans la page 2048 (le HTML définit son propre background)
        String hex = hexFor(value);
        injectCssToPage(hex);
    }

    private int colorFor(String value) {
        if ("Blue".equals(value))       return Color.parseColor("#0D47A1");
        else if ("Green".equals(value)) return Color.parseColor("#1B5E20");
        else if ("Yellow".equals(value))return Color.parseColor("#FBC02D");
        else if ("Dark".equals(value))  return Color.parseColor("#121212");
        else                            return Color.WHITE; // "White" ou défaut
    }

    private String hexFor(String value) {
        if ("Blue".equals(value))       return "#0D47A1";
        else if ("Green".equals(value)) return "#1B5E20";
        else if ("Yellow".equals(value))return "#FBC02D";
        else if ("Dark".equals(value))  return "#121212";
        else                            return "#FFFFFF";
    }

    /**
     * Injecte/Met à jour un <style id="__bg"> pour forcer le fond de la page
     */
    private void injectCssToPage(String hex) {
        if (mWebView == null) return;

        // CSS robuste avec !important
        String js = "(function(){"
                + "var css='html,body{background:"+hex+" !important;}';"
                + "var s=document.getElementById('__bg');"
                + "if(!s){s=document.createElement('style');s.id='__bg';document.head.appendChild(s);} "
                + "s.innerHTML=css;"
                + "})();";

        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.evaluateJavascript(js, null);
        } else {
            mWebView.loadUrl("javascript:" + js);
        }
    }
}
