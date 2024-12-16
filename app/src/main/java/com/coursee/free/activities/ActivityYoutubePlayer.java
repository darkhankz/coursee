package com.coursee.free.activities;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.coursee.free.R;
import com.coursee.free.utils.Constant;

public class ActivityYoutubePlayer extends AppCompatActivity {

    private static final String STATE_SCREEN_LOCKED = "screen_locked_state";

    private WebView webView;
    private ProgressBar progressBar;
    private String videoId;
    private boolean fullscreen = false;
    private ImageView fullscreenButton;
    private ImageView lockButton;
    private View touchBlocker;
    private boolean isScreenLocked = false;
    private RelativeLayout controlsLayout; // Добавьте это

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_youtube_player);

        videoId = getIntent().getStringExtra(Constant.KEY_VIDEO_ID);
        if (videoId == null) {
            Toast.makeText(this, "Error loading video", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupWebView();
        setupControls();
        loadYouTubePlayer();

        // Восстанавливаем состояние после поворота экрана
        if (savedInstanceState != null) {
            isScreenLocked = savedInstanceState.getBoolean(STATE_SCREEN_LOCKED, false);
            if (isScreenLocked) {
                lockScreen();
            }
        }
    }

    private void initViews() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        fullscreenButton = findViewById(R.id.exo_fullscreen_icon);
        lockButton = findViewById(R.id.lockButton);
        touchBlocker = findViewById(R.id.touchBlocker);
        controlsLayout = findViewById(R.id.controls_layout); // Добавьте в layout
    }

    private void setupControls() {
        // Настройка кнопки блокировки
        lockButton.setOnClickListener(v -> {
            isScreenLocked = !isScreenLocked;
            if (isScreenLocked) {
                lockScreen();
            } else {
                unlockScreen();
            }
        });

        // Настройка кнопки полноэкранного режима
        fullscreenButton.setOnClickListener(v -> {
            if (fullscreen) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                fullscreenButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_open));
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                fullscreenButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_close));
            }
            fullscreen = !fullscreen;
        });
    }

    private void lockScreen() {
        lockButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock));
        fullscreenButton.setVisibility(View.GONE);
        touchBlocker.setVisibility(View.VISIBLE);

        // Блокируем все касания на WebView
        webView.setOnTouchListener((v, event) -> true);
    }

    private void unlockScreen() {
        lockButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_unlock));
        fullscreenButton.setVisibility(View.VISIBLE);
        touchBlocker.setVisibility(View.GONE);

        // Разблокируем касания на WebView
        webView.setOnTouchListener(null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isScreenLocked) {
            // Определяем координаты касания
            float x = ev.getRawX();
            float y = ev.getRawY();

            // Получаем координаты кнопки блокировки
            int[] lockButtonLocation = new int[2];
            lockButton.getLocationOnScreen(lockButtonLocation);

            // Определяем область кнопки
            Rect lockButtonRect = new Rect(
                    lockButtonLocation[0],
                    lockButtonLocation[1],
                    lockButtonLocation[0] + lockButton.getWidth(),
                    lockButtonLocation[1] + lockButton.getHeight()
            );

            // Если касание в области кнопки блокировки
            if (lockButtonRect.contains((int)x, (int)y)) {
                return super.dispatchTouchEvent(ev);
            }

            // Блокируем все остальные касания
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SCREEN_LOCKED, isScreenLocked);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Пересоздаем состояние блокировки после поворота
        if (isScreenLocked) {
            lockScreen();
        }
    }


    private void setupWebView() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Блокируем все переходы по ссылкам
                return true;
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setMediaPlaybackRequiresUserGesture(false);  // Разрешаем автовоспроизведение
    }

    private void loadYouTubePlayer() {
        String html = "<!DOCTYPE html><html>" +
                "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">" +
                "<style>" +
                "body { margin: 0; padding: 0; width: 100%; height: 100%; }" +
                "#player { position: fixed; width: 100%; height: 100%; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div id='player'></div>" +
                "<script src='https://www.youtube.com/iframe_api'></script>" +
                "<script>" +
                "var player;" +
                "function onYouTubeIframeAPIReady() {" +
                "    player = new YT.Player('player', {" +
                "        height: '100%'," +
                "        width: '100%'," +
                "        videoId: '" + videoId + "'," +
                "        playerVars: {" +
                "            autoplay: 1," +
                "            playsinline: 1," +
                "            rel: 0," +                    // отключает похожие видео в конце
                "            modestbranding: 1," +         // убирает логотип YouTube (но не полностью)
                "            controls: 1," +               // оставляем элементы управления
                "            showinfo: 0," +               // скрывает информацию о видео
                "            fs: 1," +                     // разрешает полноэкранный режим
                "            iv_load_policy: 3," +         // отключает аннотации
                "            disablekb: 1," +              // отключает управление с клавиатуры
                "            cc_load_policy: 0," +         // отключает субтитры по умолчанию
                "            origin: window.location.origin" +
                "        }," +
                "        events: {" +
                "            onError: onPlayerError" +
                "        }" +
                "    });" +
                "}" +
                "function onPlayerError(event) {" +
                "    Android.onError(event.data);" +
                "}" +
                "</script>" +
                "</body></html>";

        webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null);
    }
    private void setupFullscreenButton() {
        fullscreenButton.setOnClickListener(v -> {
            if (fullscreen) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                fullscreenButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_open));
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                fullscreenButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_close));
            }
            fullscreen = !fullscreen;
        });
    }



    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.destroy();
        }
        super.onDestroy();
    }
}