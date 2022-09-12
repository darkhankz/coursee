package com.coursee.free.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.coursee.free.BuildConfig;
import com.coursee.free.R;
import com.coursee.free.adapters.AdapterSuggested;
import com.coursee.free.callbacks.CallbackVideoDetail;
import com.coursee.free.config.AppConfig;
import com.coursee.free.databases.DatabaseHandlerFavorite;
import com.coursee.free.models.Video;
import com.coursee.free.notification.NotificationUtils;
import com.coursee.free.rests.RestAdapter;
import com.coursee.free.utils.AdsPref;
import com.coursee.free.utils.AppBarLayoutBehavior;
import com.coursee.free.utils.Constant;
import com.coursee.free.utils.NativeTemplateStyle;
import com.coursee.free.utils.SharedPref;
import com.coursee.free.utils.TemplateView;
import com.coursee.free.utils.Tools;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AdSize;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.coursee.free.utils.Constant.ADMOB;
import static com.coursee.free.utils.Constant.AD_STATUS_ON;
import static com.coursee.free.utils.Constant.FAN;
import static com.coursee.free.utils.Constant.STARTAPP;
import static com.coursee.free.utils.Constant.STARTAPP_IMAGE_LARGE;
import static com.coursee.free.utils.Constant.STARTAPP_IMAGE_XSMALL;

public class ActivityVideoDetail extends AppCompatActivity {

    private Call<CallbackVideoDetail> callbackCall = null;
    private LinearLayout lyt_main_content;
    private Video post;
    TextView txt_title, txt_category, txt_duration, txt_total_views, txt_date_time;
    LinearLayout lyt_view, lyt_date;
    ImageView video_thumbnail;
    private WebView video_description;
    DatabaseHandlerFavorite databaseHandler;
    CoordinatorLayout parent_view;
    BroadcastReceiver broadcastReceiver;
    private ShimmerFrameLayout lyt_shimmer;
    RelativeLayout lyt_suggested;
    private SwipeRefreshLayout swipe_refresh;
    SharedPref sharedPref;
    ImageButton image_favorite, btn_share;
    private FrameLayout adContainerView;
    com.facebook.ads.AdView fanAdView;
    private StartAppNativeAd startAppNativeAd;
    private NativeAdDetails nativeAd = null;
    private AdView adView;
    private TemplateView native_template;
    private MediaView mediaView;
    AdsPref adsPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_video_detail);

        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);

        if (AppConfig.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        databaseHandler = new DatabaseHandlerFavorite(getApplicationContext());

        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        swipe_refresh = findViewById(R.id.swipe_refresh_layout);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        swipe_refresh.setRefreshing(false);

        lyt_main_content = findViewById(R.id.lyt_main_content);
        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        parent_view = findViewById(R.id.lyt_content);

        video_thumbnail = findViewById(R.id.video_thumbnail);
        txt_title = findViewById(R.id.video_title);
        txt_category = findViewById(R.id.category_name);
        txt_duration = findViewById(R.id.video_duration);
        video_description = findViewById(R.id.video_description);
        txt_total_views = findViewById(R.id.total_views);
        txt_date_time = findViewById(R.id.date_time);
        lyt_view = findViewById(R.id.lyt_view_count);
        lyt_date = findViewById(R.id.lyt_date);
        image_favorite = findViewById(R.id.img_favorite);
        btn_share = findViewById(R.id.btn_share);

        native_template = findViewById(R.id.native_template);
        mediaView = findViewById(R.id.media_view);

        lyt_suggested = findViewById(R.id.lyt_suggested);

        post = (Video) getIntent().getSerializableExtra(Constant.EXTRA_OBJC);

        requestAction();

        swipe_refresh.setOnRefreshListener(() -> {
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
            lyt_main_content.setVisibility(View.GONE);
            requestAction();
        });

        initToolbar();
        initAdNetwork();
        onReceiveNotification();

    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        new Handler().postDelayed(this::requestPostData, 200);
    }

    private void requestPostData() {
        this.callbackCall = RestAdapter.createAPI().getVideoDetail(post.vid);
        this.callbackCall.enqueue(new Callback<CallbackVideoDetail>() {
            public void onResponse(Call<CallbackVideoDetail> call, Response<CallbackVideoDetail> response) {
                CallbackVideoDetail responseHome = response.body();
                if (responseHome == null || !responseHome.status.equals("ok")) {
                    onFailRequest();
                    return;
                }
                displayAllData(responseHome);
                swipeProgress(false);
                lyt_main_content.setVisibility(View.VISIBLE);
            }

            public void onFailure(Call<CallbackVideoDetail> call, Throwable th) {
                Log.e("onFailure", th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        lyt_main_content.setVisibility(View.GONE);
        if (Tools.isConnect(ActivityVideoDetail.this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            lyt_main_content.setVisibility(View.VISIBLE);
            return;
        }
        lyt_main_content.setVisibility(View.GONE);
    }

    private void displayAllData(CallbackVideoDetail responseHome) {
        displayData(responseHome.post);
        displaySuggested(responseHome.suggested);
    }

    @SuppressLint("SetTextI18n")
    public void displayData(final Video post) {

        txt_title.setText(post.video_title);
        txt_duration.setText(post.video_duration);

        if (AppConfig.ENABLE_VIEW_COUNT) {
            txt_total_views.setText(Tools.withSuffix(post.total_views) + " " + getResources().getString(R.string.views_count));
        } else {
            lyt_view.setVisibility(View.GONE);
        }

        if (AppConfig.ENABLE_DATE_DISPLAY && AppConfig.DISPLAY_DATE_AS_TIME_AGO) {
            PrettyTime prettyTime = new PrettyTime();
            long timeAgo = Tools.timeStringtoMilis(post.date_time);
            txt_date_time.setText(prettyTime.format(new Date(timeAgo)));
        } else if (AppConfig.ENABLE_DATE_DISPLAY && !AppConfig.DISPLAY_DATE_AS_TIME_AGO) {
            txt_date_time.setText(Tools.getFormatedDateSimple(post.date_time));
        } else {
            lyt_date.setVisibility(View.GONE);
        }

        if (post.video_type != null && post.video_type.equals("youtube")) {
            Picasso.get()
                    .load(Constant.YOUTUBE_IMAGE_FRONT + post.video_id + Constant.YOUTUBE_IMAGE_BACK_HQ)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(video_thumbnail);
        } else {
            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + post.video_thumbnail)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(video_thumbnail);
        }

        video_thumbnail.setOnClickListener(view -> {

            if (Tools.isNetworkAvailable(ActivityVideoDetail.this)) {

                if (post.video_type != null && post.video_type.equals("youtube")) {
                    Intent intent = new Intent(ActivityVideoDetail.this, ActivityYoutubePlayer.class);
                    intent.putExtra(Constant.KEY_VIDEO_ID, post.video_id);
                    startActivity(intent);
                } else if (post.video_type != null && post.video_type.equals("Upload")) {
                    Intent intent = new Intent(ActivityVideoDetail.this, ActivityVideoPlayer.class);
                    intent.putExtra("url", AppConfig.ADMIN_PANEL_URL + "/upload/video/" + post.video_url);
                    startActivity(intent);
                } else {
                    if (post.video_url != null && post.video_url.startsWith("rtmp://")) {
                        Intent intent = new Intent(ActivityVideoDetail.this, ActivityRtmpPlayer.class);
                        intent.putExtra("url", post.video_url);
                        startActivity(intent);
                    } else if (post.video_url != null && post.video_url.startsWith("rtsp://")) {
                        Intent intent = new Intent(ActivityVideoDetail.this, ActivityRtmpPlayer.class);
                        intent.putExtra("url", post.video_url);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(ActivityVideoDetail.this, ActivityVideoPlayer.class);
                        intent.putExtra("url", post.video_url);
                        startActivity(intent);
                    }
                }

                loadViewed();

            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_required), Toast.LENGTH_SHORT).show();
            }

        });

        video_description.setBackgroundColor(Color.TRANSPARENT);
        video_description.setFocusableInTouchMode(false);
        video_description.setFocusable(false);
        video_description.getSettings().setDefaultTextEncodingName("UTF-8");

        WebSettings webSettings = video_description.getSettings();
        Resources res = getResources();
        int fontSize = res.getInteger(R.integer.font_size);
        webSettings.setDefaultFontSize(fontSize);

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";
        String htmlText = post.video_description;

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
            video_description.loadDataWithBaseURL(null, text_rtl, mimeType, encoding, null);
        } else {
            video_description.loadDataWithBaseURL(null, text, mimeType, encoding, null);
        }

        btn_share.setOnClickListener(view -> {
            String share_title = android.text.Html.fromHtml(post.video_title).toString();
            String share_content = android.text.Html.fromHtml(getResources().getString(R.string.share_text)).toString();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        });

        addToFavorite();

        new Handler().postDelayed(() -> lyt_suggested.setVisibility(View.VISIBLE), 1000);

    }

    private void displaySuggested(List<Video> list) {

        RecyclerView recyclerView = findViewById(R.id.recycler_view_suggested);
        recyclerView.setLayoutManager(new LinearLayoutManager(ActivityVideoDetail.this));
        AdapterSuggested adapterSuggested = new AdapterSuggested(ActivityVideoDetail.this, recyclerView, list);
        recyclerView.setAdapter(adapterSuggested);
        recyclerView.setNestedScrollingEnabled(false);
        adapterSuggested.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityVideoDetail.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj);
            startActivity(intent);
        });

        TextView txt_suggested = findViewById(R.id.txt_suggested);
        if (list.size() > 0) {
            txt_suggested.setText(getResources().getString(R.string.txt_suggested));
        } else {
            txt_suggested.setText("");
        }

    }

    private void initToolbar() {
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

        txt_category.setText(post.category_name);

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void addToFavorite() {

        List<Video> data = databaseHandler.getFavRow(post.vid);
        if (data.size() == 0) {
            image_favorite.setImageResource(R.drawable.ic_fav_outline);
        } else {
            if (data.get(0).getVid().equals(post.vid)) {
                image_favorite.setImageResource(R.drawable.ic_fav);
            }
        }

        image_favorite.setOnClickListener(view -> {
            List<Video> data1 = databaseHandler.getFavRow(post.vid);
            if (data1.size() == 0) {
                databaseHandler.AddtoFavorite(new Video(
                        post.category_name,
                        post.vid,
                        post.video_title,
                        post.video_url,
                        post.video_id,
                        post.video_thumbnail,
                        post.video_duration,
                        post.video_description,
                        post.video_type,
                        post.total_views,
                        post.date_time
                ));
                Snackbar.make(parent_view, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
                image_favorite.setImageResource(R.drawable.ic_fav);

            } else {
                if (data1.get(0).getVid().equals(post.vid)) {
                    databaseHandler.RemoveFav(new Video(post.vid));
                    Snackbar.make(parent_view, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
                    image_favorite.setImageResource(R.drawable.ic_fav_outline);
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void loadViewed() {
        new MyTask().execute(AppConfig.ADMIN_PANEL_URL + "/api/get_total_views/?id=" + post.vid);
    }

    private static class MyTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return Tools.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null == result || result.length() == 0) {
                Log.d("TAG", "no data found!");
            } else {

                try {

                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray("result");
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void initAdNetwork() {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(ADMOB)) {
            loadAdMobBannerAd();
            loadAdMobNativeAd();
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(FAN)) {
            loadFanBannerAd();
            loadFanNativeAd();
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(STARTAPP)) {
            loadStartAppBannerAd();
            loadStartAppNativeAd();
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

    private void loadAdMobNativeAd() {
        if (!adsPref.getAdMobNativeId().equals("0")) {
            AdLoader adLoader = new AdLoader.Builder(this, adsPref.getAdMobNativeId())
                    .forUnifiedNativeAd(unifiedNativeAd -> {
                        if (sharedPref.getIsDarkTheme()) {
                            ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(this, R.color.colorBackgroundDark));
                            NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                            native_template.setStyles(styles);
                        } else {
                            ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(this, R.color.colorBackgroundLight));
                            NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                            native_template.setStyles(styles);
                        }
                        mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                        native_template.setNativeAd(unifiedNativeAd);
                        native_template.setVisibility(View.VISIBLE);
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            native_template.setVisibility(View.GONE);
                        }
                    })
                    .build();
            adLoader.loadAd(Tools.getAdRequest(this));
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
                public void onError(com.facebook.ads.Ad ad, AdError adError) {
                    adContainer.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded(com.facebook.ads.Ad ad) {
                    adContainer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdClicked(com.facebook.ads.Ad ad) {

                }

                @Override
                public void onLoggingImpression(com.facebook.ads.Ad ad) {

                }
            };
            com.facebook.ads.AdView.AdViewLoadConfig loadAdConfig = fanAdView.buildLoadAdConfig().withAdListener(adListener).build();
            fanAdView.loadAd(loadAdConfig);
        }
    }

    private void loadFanNativeAd() {
        if (!adsPref.getFanNativeUnitId().equals("0")) {
            final NativeAd nativeAd;
            final RelativeLayout lyt_fan_native = findViewById(R.id.lyt_fan_native);
            final NativeAdLayout nativeAdLayout = findViewById(R.id.native_ad_container);
            if (BuildConfig.DEBUG) {
                nativeAd = new NativeAd(this, "IMG_16_9_APP_INSTALL#" + adsPref.getFanNativeUnitId());
            } else {
                nativeAd = new NativeAd(this, adsPref.getFanNativeUnitId());
            }
            NativeAdListener nativeAdListener = new NativeAdListener() {
                @Override
                public void onMediaDownloaded(com.facebook.ads.Ad ad) {

                }

                @Override
                public void onError(com.facebook.ads.Ad ad, AdError adError) {

                }

                @Override
                public void onAdLoaded(com.facebook.ads.Ad ad) {
                    // Race condition, load() called again before last ad was displayed
                    lyt_fan_native.setVisibility(View.VISIBLE);
                    if (nativeAd != ad) {
                        return;
                    }
                    // Inflate Native Ad into Container
                    //inflateAd(nativeAd);
                    nativeAd.unregisterView();
                    // Add the Ad view into the ad container.
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
                    LinearLayout nativeAdView = (LinearLayout) inflater.inflate(R.layout.gnt_fan_big_template, nativeAdLayout, false);
                    nativeAdLayout.addView(nativeAdView);

                    // Add the AdOptionsView
                    LinearLayout adChoicesContainer = nativeAdView.findViewById(R.id.ad_choices_container);
                    AdOptionsView adOptionsView = new AdOptionsView(getApplicationContext(), nativeAd, nativeAdLayout);
                    adChoicesContainer.removeAllViews();
                    adChoicesContainer.addView(adOptionsView, 0);

                    // Create native UI using the ad metadata.
                    TextView nativeAdTitle = nativeAdView.findViewById(R.id.native_ad_title);
                    com.facebook.ads.MediaView nativeAdMedia = nativeAdView.findViewById(R.id.native_ad_media);
                    TextView nativeAdSocialContext = nativeAdView.findViewById(R.id.native_ad_social_context);
                    TextView nativeAdBody = nativeAdView.findViewById(R.id.native_ad_body);
                    TextView sponsoredLabel = nativeAdView.findViewById(R.id.native_ad_sponsored_label);
                    Button nativeAdCallToAction = nativeAdView.findViewById(R.id.native_ad_call_to_action);
                    LinearLayout ad_unit = nativeAdView.findViewById(R.id.ad_unit);

                    if (sharedPref.getIsDarkTheme()) {
                        nativeAdTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                        nativeAdSocialContext.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                        sponsoredLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.sub_text_color));
                        nativeAdBody.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.sub_text_color));
                    }

                    // Set the Text.
                    nativeAdTitle.setText(nativeAd.getAdvertiserName());
                    nativeAdBody.setText(nativeAd.getAdBodyText());
                    nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                    nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                    nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                    sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

                    // Create a list of clickable views
                    List<View> clickableViews = new ArrayList<>();
                    clickableViews.add(nativeAdTitle);
                    clickableViews.add(ad_unit);
                    clickableViews.add(nativeAdCallToAction);

                    // Register the Title and CTA button to listen for clicks.
                    nativeAd.registerViewForInteraction(nativeAdView, nativeAdMedia, clickableViews);

                }

                @Override
                public void onAdClicked(com.facebook.ads.Ad ad) {

                }

                @Override
                public void onLoggingImpression(com.facebook.ads.Ad ad) {

                }
            };

            NativeAd.NativeLoadAdConfig loadAdConfig = nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build();
            nativeAd.loadAd(loadAdConfig);
        }
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

    private void loadStartAppNativeAd() {
        if (!adsPref.getStartappAppID().equals("0")) {
            View lyt_startapp_native = findViewById(R.id.startapp_native_template);
            ImageView startapp_native_image = findViewById(R.id.startapp_native_image);
            ImageView startapp_native_secondary_image = findViewById(R.id.startapp_native_secondary_image);
            TextView startapp_native_title = findViewById(R.id.startapp_native_title);
            TextView startapp_native_description = findViewById(R.id.startapp_native_description);
            Button startapp_native_button = findViewById(R.id.startapp_native_button);
            startapp_native_button.setOnClickListener(v1 -> lyt_startapp_native.performClick());

            startAppNativeAd = new StartAppNativeAd(this);
            NativeAdPreferences nativePrefs = new NativeAdPreferences()
                    .setAdsNumber(1)
                    .setAutoBitmapDownload(true)
                    .setPrimaryImageSize(STARTAPP_IMAGE_LARGE)
                    .setSecondaryImageSize(STARTAPP_IMAGE_XSMALL);
            AdEventListener adListener = new AdEventListener() {
                @Override
                public void onReceiveAd(Ad arg0) {
                    ArrayList<NativeAdDetails> nativeAdsList = startAppNativeAd.getNativeAds();
                    if (nativeAdsList.size() > 0) {
                        nativeAd = nativeAdsList.get(0);
                    }
                    if (nativeAd != null) {;
                        startapp_native_image.setImageBitmap(nativeAd.getImageBitmap());
                        startapp_native_secondary_image.setImageBitmap(nativeAd.getSecondaryImageBitmap());
                        startapp_native_title.setText(nativeAd.getTitle());
                        startapp_native_description.setText(nativeAd.getDescription());
                        startapp_native_button.setText(nativeAd.isApp() ? "Install" : "Open");
                        nativeAd.registerViewForInteraction(lyt_startapp_native);
                    }
                    lyt_startapp_native.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailedToReceiveAd(Ad arg0) {
                    lyt_startapp_native.setVisibility(View.GONE);
                }
            };
            startAppNativeAd.loadAd(nativePrefs, adListener);
        }
    }

    public void onDestroy() {
        if (!(callbackCall == null || callbackCall.isCanceled())) {
            this.callbackCall.cancel();
        }
        lyt_shimmer.stopShimmer();
        super.onDestroy();
    }

    public void onReceiveNotification() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constant.PUSH_NOTIFICATION)) {
                    NotificationUtils.showDialogNotification(ActivityVideoDetail.this, intent);
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

}
