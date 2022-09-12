package com.coursee.free.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.coursee.free.BuildConfig;
import com.coursee.free.R;
import com.coursee.free.adapters.AdapterRecent;
import com.coursee.free.callbacks.CallbackCategoryDetails;
import com.coursee.free.config.AppConfig;
import com.coursee.free.models.Category;
import com.coursee.free.models.Video;
import com.coursee.free.notification.NotificationUtils;
import com.coursee.free.rests.ApiInterface;
import com.coursee.free.rests.RestAdapter;
import com.coursee.free.utils.AdsPref;
import com.coursee.free.utils.AppBarLayoutBehavior;
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
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.appbar.AppBarLayout;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.adsbase.StartAppAd;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.coursee.free.utils.Constant.ADMOB;
import static com.coursee.free.utils.Constant.AD_STATUS_ON;
import static com.coursee.free.utils.Constant.FAN;
import static com.coursee.free.utils.Constant.STARTAPP;
import static com.coursee.free.utils.Constant.VIDEO_LIST_COMPACT;

public class ActivityVideoByCategory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdapterRecent adapterRecent;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Call<CallbackCategoryDetails> callbackCall = null;
    private int post_total = 0;
    private int failed_page = 0;
    private Category category;
    BroadcastReceiver broadcastReceiver;
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
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_category_details);
        view = findViewById(android.R.id.content);

        sharedPref = new SharedPref(this);
        sharedPref.setDefaultSortVideos();
        adsPref = new AdsPref(this);

        AppBarLayout appBarLayout = findViewById(R.id.appbarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        if (AppConfig.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        initAdNetwork();

        category = (Category) getIntent().getSerializableExtra(Constant.EXTRA_OBJC);

        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        if (sharedPref.getVideoViewType() == VIDEO_LIST_COMPACT) {
            recyclerView.setPadding(0, getResources().getDimensionPixelOffset(R.dimen.spacing_small), 0, 0);
        }

        //set data and list adapter
        adapterRecent = new AdapterRecent(this, recyclerView, new ArrayList<>());
        recyclerView.setAdapter(adapterRecent);

        // on item list clicked
        adapterRecent.setOnItemClickListener((v, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityVideoDetail.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj);
            startActivity(intent);

            showInterstitialAdNetwork();
        });

        // detect when scroll reach bottom
        adapterRecent.setOnLoadMoreListener(current_page -> {
            if (post_total > adapterRecent.getItemCount() && current_page != 0) {
                int next_page = current_page + 1;
                requestAction(next_page);
            } else {
                adapterRecent.setLoaded();
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) {
                callbackCall.cancel();
            }
            adapterRecent.resetListData();
            requestAction(1);
        });

        requestAction(1);
        initShimmerLayout();
        setupToolbar();
        onReceiveNotification();
//        handleNotification();

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
            getSupportActionBar().setTitle(category.category_name);
        }
    }

    private void displayApiResult(final List<Video> videos) {
        adapterRecent.insertData(videos);
        swipeProgress(false);
        if (videos.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestPostApi(final int page_no) {

        ApiInterface apiInterface = RestAdapter.createAPI();

        if (sharedPref.getCurrentSortVideos() == 0) {
            callbackCall = apiInterface.getCategoryVideos(category.cid, page_no, AppConfig.LOAD_MORE, Constant.MOST_POPULAR, AppConfig.API_KEY);
        } else if (sharedPref.getCurrentSortVideos() == 1) {
            callbackCall = apiInterface.getCategoryVideos(category.cid, page_no, AppConfig.LOAD_MORE, Constant.ADDED_OLDEST, AppConfig.API_KEY);
        } else if (sharedPref.getCurrentSortVideos() == 2) {
            callbackCall = apiInterface.getCategoryVideos(category.cid, page_no, AppConfig.LOAD_MORE, Constant.ADDED_NEWEST, AppConfig.API_KEY);
        } else {
            callbackCall = apiInterface.getCategoryVideos(category.cid, page_no, AppConfig.LOAD_MORE, Constant.ADDED_NEWEST, AppConfig.API_KEY);
        }

        callbackCall.enqueue(new Callback<CallbackCategoryDetails>() {
            @Override
            public void onResponse(Call<CallbackCategoryDetails> call, Response<CallbackCategoryDetails> response) {
                CallbackCategoryDetails resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post_total = resp.count_total;
                    displayApiResult(resp.posts);
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(Call<CallbackCategoryDetails> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }

        });
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        adapterRecent.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(getApplicationContext())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            adapterRecent.setLoading();
        }
        new Handler().postDelayed(() -> requestPostApi(page_no), Constant.DELAY_TIME);
    }

    private void showFailedView(boolean show, String message) {
        View view = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view1 -> requestAction(failed_page));
    }

    private void showNoItemView(boolean show) {
        View view = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.msg_no_item);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
        });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lyt_shimmer.stopShimmer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_search:
                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                startActivity(intent);
                return true;

            case R.id.menu_sort:
                String[] items = getResources().getStringArray(R.array.dialog_single_choice_array);
                int itemSelected = sharedPref.getCurrentSortVideos();
                new AlertDialog.Builder(ActivityVideoByCategory.this)
                        .setTitle(R.string.title_sort)
                        .setSingleChoiceItems(items, itemSelected, (dialogInterface, position) -> {
                            if (callbackCall != null && callbackCall.isExecuted())
                                callbackCall.cancel();
                            adapterRecent.resetListData();
                            requestAction(1);
                            sharedPref.updateSortVideos(position);
                            dialogInterface.dismiss();
                        })
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

//    public void handleNotification() {
//        Intent intent = getIntent();
//        String id = intent.getStringExtra("id");
//        String url = intent.getStringExtra("link");
//        if (id != null) {
//            if (id.equals("0")) {
//                if (!url.equals("")) {
//                    Intent e = new Intent(Intent.ACTION_VIEW);
//                    e.setData(Uri.parse(url));
//                    startActivity(e);
//                }
//                Log.d("FCM_INFO", " id : " + id);
//            } else {
//                Intent action = new Intent(ActivityVideoByCategory.this, ActivityNotificationDetail.class);
//                action.putExtra("id", id);
//                startActivity(action);
//                Log.d("FCM_INFO", "id : " + id);
//            }
//        }
//    }

    public void onReceiveNotification() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constant.PUSH_NOTIFICATION)) {
                    NotificationUtils.showDialogNotification(ActivityVideoByCategory.this, intent);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Constant.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Constant.PUSH_NOTIFICATION));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
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
                    }

                    @Override
                    public void onAdFailedToLoad(int error) {
                        adContainerView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAdLeftApplication() {
                    }

                    @Override
                    public void onAdOpened() {
                    }

                    @Override
                    public void onAdLoaded() {
                        adContainerView.setVisibility(View.VISIBLE);
                    }
                });
            });
        }
    }

    private void loadAdMobInterstitialAd() {
        if (!adsPref.getAdMobInterstitialId().equals("0")) {
            adMobInterstitialAd = new InterstitialAd(getApplicationContext());
            adMobInterstitialAd.setAdUnitId(adsPref.getAdMobInterstitialId());
            adMobInterstitialAd.loadAd(Tools.getAdRequest(ActivityVideoByCategory.this));
            adMobInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    adMobInterstitialAd.loadAd(Tools.getAdRequest(ActivityVideoByCategory.this));
                }
            });
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
                if (adMobInterstitialAd != null && adMobInterstitialAd.isLoaded()) {
                    if (counter == adsPref.getInterstitialAdInterval()) {
                        adMobInterstitialAd.show();
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

}
