package com.coursee.free.activities;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.coursee.free.R;
import com.coursee.free.config.AppConfig;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

public class ActivityRtmpPlayer extends AppCompatActivity {

    private ExoPlayer player;
    String url;
    ProgressBar progressBar;
    boolean fullscreen = false;
    private ImageView fullscreenButton;
    PlayerView playerView;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_rtmp_player);

        if (AppConfig.FORCE_PLAYER_TO_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        url = getIntent().getStringExtra("url");
        progressBar = findViewById(R.id.progressBar);
        playerView = findViewById(R.id.simple_player);

        // Create player
        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(
                        new DefaultDataSource.Factory(this,
                                new DefaultHttpDataSource.Factory())))
                .build();

        playerView.setPlayer(player);

        // Create RTMP DataSource
        RtmpDataSource.Factory rtmpDataSourceFactory = new RtmpDataSource.Factory();
        MediaSource mediaSource = new DefaultMediaSourceFactory(rtmpDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(url)));

        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);

        new Handler().postDelayed(() -> progressBar.setVisibility(View.GONE), 5000);

        playerOrientation();
    }

    private void playerOrientation() {
        fullscreenButton = playerView.findViewById(R.id.exo_fullscreen_icon);
        fullscreenButton.setOnClickListener(view -> {
            if (fullscreen) {
                fullscreenButton.setImageDrawable(ContextCompat.getDrawable(ActivityRtmpPlayer.this, R.drawable.ic_fullscreen_open));
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playerView.getLayoutParams();
                params.width = params.MATCH_PARENT;
                params.height = (int) (200 * getApplicationContext().getResources().getDisplayMetrics().density);
                playerView.setLayoutParams(params);
                fullscreen = false;
            } else {
                fullscreenButton.setImageDrawable(ContextCompat.getDrawable(ActivityRtmpPlayer.this, R.drawable.ic_fullscreen_close));
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playerView.getLayoutParams();
                params.width = params.MATCH_PARENT;
                params.height = params.MATCH_PARENT;
                playerView.setLayoutParams(params);
                fullscreen = true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        ActivityRtmpPlayer.this.finish();
        player.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}