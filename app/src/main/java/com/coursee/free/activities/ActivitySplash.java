package com.coursee.free.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.coursee.free.R;
import com.coursee.free.callbacks.CallbackAds;
import com.coursee.free.config.AppConfig;
import com.coursee.free.models.Ads;
import com.coursee.free.rests.RestAdapter;
import com.coursee.free.utils.AdsPref;
import com.coursee.free.utils.SharedPref;
import com.coursee.free.utils.Tools;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplash extends AppCompatActivity {

    Boolean isCancelled = false;
    private ProgressBar progressBar;
    String id = "0";
    String url = "";
    ImageView img_splash;
    SharedPref sharedPref;
    AdsPref adsPref;
    Ads ads;
    Call<CallbackAds> callbackCall = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_splash);

        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);

        img_splash = findViewById(R.id.img_splash);
        if (sharedPref.getIsDarkTheme()) {
            img_splash.setImageResource(R.drawable.bg_splash_dark);
        } else {
            img_splash.setImageResource(R.drawable.bg_splash_default);
        }

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if(getIntent().hasExtra("nid")) {
            id = getIntent().getStringExtra("nid");
            url = getIntent().getStringExtra("external_link");
        }

        if (Tools.isConnect(this)) {
            requestAds();
        } else {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }, AppConfig.SPLASH_TIME + 1000);
        }

    }

    private void requestAds() {
        this.callbackCall = RestAdapter.createAPI().getAds(AppConfig.API_KEY);
        this.callbackCall.enqueue(new Callback<CallbackAds>() {
            public void onResponse(Call<CallbackAds> call, Response<CallbackAds> response) {
                CallbackAds resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    ads = resp.ads;
                    if (ads.date_time.equals(adsPref.getDateTime())) {
                        Log.d("Response", "Ads Data is updated");
                    } else {
                        adsPref.saveAds(
                                ads.ad_status,
                                ads.ad_type,
                                ads.admob_publisher_id,
                                ads.admob_app_id,
                                ads.admob_banner_unit_id,
                                ads.admob_interstitial_unit_id,
                                ads.admob_native_unit_id,
                                ads.fan_banner_unit_id,
                                ads.fan_interstitial_unit_id,
                                ads.fan_native_unit_id,
                                ads.startapp_app_id,
                                ads.interstitial_ad_interval,
                                ads.native_ad_interval,
                                ads.native_ad_index,
                                ads.date_time,
                                ads.youtube_api_key
                        );
                        Log.d("Response", "Ads Data is saved");
                    }
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }, AppConfig.SPLASH_TIME);
                } else {
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }, AppConfig.SPLASH_TIME + 1000);
                }
            }

            public void onFailure(Call<CallbackAds> call, Throwable th) {
                Log.e("onFailure", "" + th.getMessage());
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }, AppConfig.SPLASH_TIME);
            }
        });
    }

}
