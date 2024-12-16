package com.coursee.free.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coursee.free.BuildConfig;
import com.coursee.free.R;
import com.coursee.free.adapters.AdapterRecent;
import com.coursee.free.callbacks.CallbackListVideo;
import com.coursee.free.config.AppConfig;
import com.coursee.free.rests.ApiInterface;
import com.coursee.free.rests.RestAdapter;
import com.coursee.free.utils.AdsPref;
import com.coursee.free.utils.Constant;
import com.coursee.free.utils.SharedPref;
import com.coursee.free.utils.Tools;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSize;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.adsbase.StartAppAd;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.coursee.free.utils.Constant.ADMOB;
import static com.coursee.free.utils.Constant.AD_STATUS_ON;
import static com.coursee.free.utils.Constant.FAN;
import static com.coursee.free.utils.Constant.STARTAPP;
import static com.coursee.free.utils.Constant.VIDEO_LIST_COMPACT;

public class ActivitySearch extends AppCompatActivity {

    private EditText et_search;
    private RecyclerView recyclerView;
    private AdapterRecent mAdapterSearch;
    private ImageButton bt_clear;
    private View view;
    private Call<CallbackListVideo> callbackCall = null;
    int counter = 1;
    SharedPref sharedPref;
    private ShimmerFrameLayout lyt_shimmer;
    private FrameLayout adContainerView;
    private com.facebook.ads.AdView fanAdView;
    private InterstitialAd adMobInterstitialAd;
    private com.facebook.ads.InterstitialAd fanInterstitialAd;
    private StartAppAd startAppAd;
    private AdView adView;
    AdsPref adsPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_search);
        view = findViewById(android.R.id.content);

        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);

        if (AppConfig.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        initAdNetwork();

        et_search = findViewById(R.id.et_search);
        bt_clear = findViewById(R.id.bt_clear);
        bt_clear.setVisibility(View.GONE);
        //progressBar = findViewById(R.id.progressBar);
        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        swipeProgress(false);

        recyclerView = findViewById(R.id.recyclerView);
        if (sharedPref.getVideoViewType() == VIDEO_LIST_COMPACT) {
            recyclerView.setPadding(0, getResources().getDimensionPixelOffset(R.dimen.spacing_small), 0, 0);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        et_search.addTextChangedListener(textWatcher);

        //set data and list mAdapterSearch
        mAdapterSearch = new AdapterRecent(this, recyclerView, new ArrayList<>());
        recyclerView.setAdapter(mAdapterSearch);

        bt_clear.setOnClickListener(view -> et_search.setText(""));

        et_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                searchAction();
                return true;
            }
            return false;
        });

        mAdapterSearch.setOnItemClickListener((v, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityVideoDetail.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj);
            startActivity(intent);

            showInterstitialAdNetwork();
        });

        setupToolbar();
        initShimmerLayout();

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
            getSupportActionBar().setTitle("");
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                bt_clear.setVisibility(View.GONE);
            } else {
                bt_clear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void requestSearchApi(final String query) {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getSearchPosts(query, Constant.MAX_SEARCH_RESULT, AppConfig.API_KEY);
        callbackCall.enqueue(new Callback<CallbackListVideo>() {
            @Override
            public void onResponse(Call<CallbackListVideo> call, Response<CallbackListVideo> response) {
                CallbackListVideo resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    mAdapterSearch.insertData(resp.posts);
                    if (resp.posts.size() == 0) showNotFoundView(true);
                } else {
                    onFailRequest();
                }
                swipeProgress(false);
            }

            @Override
            public void onFailure(Call<CallbackListVideo> call, Throwable t) {
                onFailRequest();
                swipeProgress(false);
            }

        });
    }

    private void onFailRequest() {
        if (Tools.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.no_internet_text));
        }
    }

    private void searchAction() {
        showFailedView(false, "");
        showNotFoundView(false);
        final String query = et_search.getText().toString().trim();
        if (!query.equals("")) {
            mAdapterSearch.resetListData();
            swipeProgress(true);
            new Handler().postDelayed(() -> requestSearchApi(query), Constant.DELAY_TIME);
        } else {
            Toast.makeText(getApplicationContext(),  getResources().getString(R.string.msg_search_input), Toast.LENGTH_SHORT).show();
            swipeProgress(false);
        }
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

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> searchAction());
    }

    private void showNotFoundView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            return;
        } else {
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
        }
    }

    private void initShimmerLayout() {
        View lyt_shimmer_default = findViewById(R.id.lyt_shimmer_default);
        View lyt_shimmer_compact = findViewById(R.id.lyt_shimmer_compact);
        if (sharedPref.getVideoViewType() == VIDEO_LIST_COMPACT) {
            lyt_shimmer_default.setVisibility(View.GONE);
            lyt_shimmer_compact.setVisibility(View.VISIBLE);
        } else {
            lyt_shimmer_default.setVisibility(View.VISIBLE);
            lyt_shimmer_compact.setVisibility(View.GONE);
        }
    }

    public void initAdNetwork() {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(ADMOB)) {
            loadAdMobBannerAd();
            loadAdMobInterstitialAd();
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(FAN)) {
            loadFanBannerAd();
            loadFanInterstitialAd();
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(STARTAPP)) {
            loadStartAppBannerAd();
            loadStartAppInterstitialAd();
        }
    }

    public void loadAdMobBannerAd() {
        if (!adsPref.getAdMobBannerId().equals("0")) {
            adContainerView = findViewById(R.id.admob_banner_view_container);
            adContainerView.post(() -> {
                adView = new AdView(this);
                adView.setAdUnitId(adsPref.getAdMobBannerId());
                adContainerView.removeAllViews();
                adContainerView.addView(adView);
                adView.setAdSize(Tools.getAdSize(this));
                adView.loadAd(Tools.getAdRequest(this));
                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        adContainerView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAdOpened() {
                        super.onAdOpened();
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        adContainerView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                    }
                });
            });
        }
    }
    private void loadAdMobInterstitialAd() {
        if (!adsPref.getAdMobInterstitialId().equals("0")) {
            InterstitialAd.load(
                    this,
                    adsPref.getAdMobInterstitialId(),
                    Tools.getAdRequest(this),
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            adMobInterstitialAd = interstitialAd;
                            adMobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    loadAdMobInterstitialAd();
                                }
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            adMobInterstitialAd = null;
                        }
                    }
            );
        }
    }

    private void loadFanBannerAd() {
        if (!adsPref.getFanBannerUnitId().equals("0")) {
            if (BuildConfig.DEBUG) {
                fanAdView = new com.facebook.ads.AdView(this, "IMG_16_9_APP_INSTALL#" + adsPref.getFanBannerUnitId(), AdSize.BANNER_HEIGHT_50);
            } else {
                fanAdView = new com.facebook.ads.AdView(this, adsPref.getFanBannerUnitId(), AdSize.BANNER_HEIGHT_50);
            }
            LinearLayout adContainer = findViewById(R.id.fan_banner_view_container);
            // Add the ad view to your activity layout
            adContainer.addView(fanAdView);
            com.facebook.ads.AdListener adListener = new com.facebook.ads.AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    adContainer.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    adContainer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };
            com.facebook.ads.AdView.AdViewLoadConfig loadAdConfig = fanAdView.buildLoadAdConfig().withAdListener(adListener).build();
            fanAdView.loadAd(loadAdConfig);
        }
    }

    private void loadFanInterstitialAd() {
        if (BuildConfig.DEBUG) {
            fanInterstitialAd = new com.facebook.ads.InterstitialAd(getApplicationContext(), "IMG_16_9_APP_INSTALL#" + adsPref.getFanInterstitialUnitId());
        } else {
            fanInterstitialAd = new com.facebook.ads.InterstitialAd(getApplicationContext(), adsPref.getFanInterstitialUnitId());
        }
        com.facebook.ads.InterstitialAdListener adListener = new InterstitialAdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {

            }

            @Override
            public void onAdLoaded(Ad ad) {

            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }

            @Override
            public void onInterstitialDisplayed(Ad ad) {

            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                fanInterstitialAd.loadAd();
            }
        };

        com.facebook.ads.InterstitialAd.InterstitialLoadAdConfig loadAdConfig = fanInterstitialAd.buildLoadAdConfig().withAdListener(adListener).build();
        fanInterstitialAd.loadAd(loadAdConfig);
    }

    private void loadStartAppBannerAd() {
        if (!adsPref.getStartappAppID().equals("0")) {
            RelativeLayout bannerLayout = findViewById(R.id.startapp_banner_view_container);
            Banner banner = new Banner(this, new BannerListener() {
                @Override
                public void onReceiveAd(View banner) {
                    bannerLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailedToReceiveAd(View banner) {
                    bannerLayout.setVisibility(View.GONE);
                }

                @Override
                public void onImpression(View view) {

                }

                @Override
                public void onClick(View banner) {
                }
            });
            bannerLayout.addView(banner);
        }
    }

    private void loadStartAppInterstitialAd() {
        if (!adsPref.getStartappAppID().equals("0")) {
            startAppAd = new StartAppAd(this);
        }
    }

    private void showInterstitialAdNetwork() {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(ADMOB)) {
            if (!adsPref.getAdMobInterstitialId().equals("0")) {
                if (adMobInterstitialAd != null) {
                    if (counter == adsPref.getInterstitialAdInterval()) {
                        adMobInterstitialAd.show(ActivitySearch.this);
                        counter = 1;
                    } else {
                        counter++;
                    }
                }
            }
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(FAN)) {
            if (!adsPref.getFanInterstitialUnitId().equals("0")) {
                if (fanInterstitialAd != null && fanInterstitialAd.isAdLoaded()) {
                    if (counter == adsPref.getInterstitialAdInterval()) {
                        fanInterstitialAd.show();
                        counter = 1;
                    } else {
                        counter++;
                    }
                }
            }
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(STARTAPP)) {
            if (!adsPref.getStartappAppID().equals("0")) {
                if (counter == adsPref.getInterstitialAdInterval()) {
                    startAppAd.showAd();
                    counter = 1;
                } else {
                    counter++;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (et_search.length() > 0) {
            et_search.setText("");
        } else {
            super.onBackPressed();
        }
    }

}
