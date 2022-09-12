package com.coursee.free.activities;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.coursee.free.R;
import com.coursee.free.config.AppConfig;
import com.coursee.free.models.Setting;
import com.coursee.free.rests.ApiInterface;
import com.coursee.free.rests.RestAdapter;
import com.coursee.free.utils.SharedPref;
import com.coursee.free.utils.Tools;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityPrivacyPolicy extends AppCompatActivity {

    WebView wv_privacy_policy;
    ProgressBar progressBar;
    Button btn_failed_retry;
    View lyt_failed;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_privacy_policy);

        sharedPref = new SharedPref(this);

        wv_privacy_policy = findViewById(R.id.privacy_policy);
        wv_privacy_policy.setBackgroundColor(Color.TRANSPARENT);

        progressBar = findViewById(R.id.progressBar);
        btn_failed_retry = findViewById(R.id.failed_retry);
        lyt_failed = findViewById(R.id.lyt_failed);

        setupToolbar();
        displayData();

        btn_failed_retry.setOnClickListener(view -> {
            lyt_failed.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            displayData();
        });
    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(R.string.about_app_privacy_policy);
        }
    }

    public void displayData() {
        Handler handler = new Handler();
        handler.postDelayed(this::loadData, 1000);
    }

    public void loadData() {
        ApiInterface api = RestAdapter.createAPI();
        Call<Setting> call = api.getPrivacyPolicy(AppConfig.API_KEY);
        call.enqueue(new Callback<Setting>() {
            @Override
            public void onResponse(Call<Setting> call, Response<Setting> response) {
                String privacy_policy = response.body().getPrivacy_policy();
                try {
                    wv_privacy_policy.setFocusableInTouchMode(false);
                    wv_privacy_policy.setFocusable(false);
                    wv_privacy_policy.getSettings().setDefaultTextEncodingName("UTF-8");

                    WebSettings webSettings = wv_privacy_policy.getSettings();
                    Resources res = getResources();
                    int fontSize = res.getInteger(R.integer.font_size);
                    webSettings.setDefaultFontSize(fontSize);

                    String mimeType = "text/html; charset=UTF-8";
                    String encoding = "utf-8";
                    String htmlText = privacy_policy;

                    String bg_paragraph;
                    if (sharedPref.getIsDarkTheme()) {
                        bg_paragraph = "<style type=\"text/css\">body{color: #eeeeee;} a{color:#ffffff;}";
                    } else {
                        bg_paragraph = "<style type=\"text/css\">body{color: #000000;}";
                    }

                    String text = "<html><head>"
                            + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                            + bg_paragraph
                            + "</style></head>"
                            + "<body>"
                            + htmlText
                            + "</body></html>";

                    String text_rtl = "<html dir='rtl'><head>"
                            + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                            + bg_paragraph
                            + "</style></head>"
                            + "<body>"
                            + htmlText
                            + "</body></html>";

                    if (AppConfig.ENABLE_RTL_MODE) {
                        wv_privacy_policy.loadDataWithBaseURL(null, text_rtl, mimeType, encoding, null);
                    } else {
                        wv_privacy_policy.loadDataWithBaseURL(null, text, mimeType, encoding, null);
                    }

                    progressBar.setVisibility(View.GONE);
                    lyt_failed.setVisibility(View.GONE);

                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Setting> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                lyt_failed.setVisibility(View.VISIBLE);
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

}
