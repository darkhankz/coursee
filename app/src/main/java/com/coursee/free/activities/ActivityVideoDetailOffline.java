package com.coursee.free.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.coursee.free.BuildConfig;
import com.coursee.free.R;
import com.coursee.free.config.AppConfig;
import com.coursee.free.databases.DatabaseHandlerFavorite;
import com.coursee.free.models.Video;
import com.coursee.free.notification.NotificationUtils;
import com.coursee.free.utils.AppBarLayoutBehavior;
import com.coursee.free.utils.Constant;
import com.coursee.free.utils.SharedPref;
import com.coursee.free.utils.Tools;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

public class ActivityVideoDetailOffline extends AppCompatActivity {

    String str_category, str_vid, str_title, str_url, str_video_id, str_thumbnail, str_duration, str_description, str_type, str_date_time;
    String str_cid;
    long long_total_views;
    ImageView video_thumbnail;
    TextView txt_title, txt_category, txt_duration, txt_total_views, txt_date_time;
    LinearLayout lyt_view, lyt_date;
    WebView video_description;
    Snackbar snackbar;
    ImageButton image_favorite, btn_share;
    DatabaseHandlerFavorite databaseHandler;
    BroadcastReceiver broadcastReceiver;
    SharedPref sharedPref;
    int position;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_video_detail_offline);
        view = findViewById(android.R.id.content);

        sharedPref = new SharedPref(this);

        if (AppConfig.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        databaseHandler = new DatabaseHandlerFavorite(getApplicationContext());

        Intent intent = getIntent();
        if (null != intent) {
            position = intent.getIntExtra(Constant.POSITION, 0);
            str_cid = intent.getStringExtra(Constant.KEY_VIDEO_CATEGORY_ID);
            str_category = intent.getStringExtra(Constant.KEY_VIDEO_CATEGORY_NAME);
            str_vid = intent.getStringExtra(Constant.KEY_VID);
            str_title = intent.getStringExtra(Constant.KEY_VIDEO_TITLE);
            str_url = intent.getStringExtra(Constant.KEY_VIDEO_URL);
            str_video_id = intent.getStringExtra(Constant.KEY_VIDEO_ID);
            str_thumbnail = intent.getStringExtra(Constant.KEY_VIDEO_THUMBNAIL);
            str_duration = intent.getStringExtra(Constant.KEY_VIDEO_DURATION);
            str_description = intent.getStringExtra(Constant.KEY_VIDEO_DESCRIPTION);
            str_type = intent.getStringExtra(Constant.KEY_VIDEO_TYPE);
            long_total_views = intent.getLongExtra(Constant.KEY_TOTAL_VIEWS, 0);
            str_date_time = intent.getStringExtra(Constant.KEY_DATE_TIME);
        }

        setupToolbar();

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

        displayData();
        addFavorite();
        onReceiveNotification();
        //handleNotification();

    }

    private void setupToolbar() {
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
            getSupportActionBar().setTitle(str_category);
        }
    }

    @SuppressLint("SetTextI18n")
    public void displayData() {

        txt_title.setText(str_title);
        txt_category.setText(str_category);
        txt_duration.setText(str_duration);

        if (AppConfig.ENABLE_VIEW_COUNT) {
            txt_total_views.setText(Tools.withSuffix(long_total_views) + " " + getResources().getString(R.string.views_count));
        } else {
            lyt_view.setVisibility(View.GONE);
        }

        if (AppConfig.ENABLE_DATE_DISPLAY && AppConfig.DISPLAY_DATE_AS_TIME_AGO) {
            PrettyTime prettyTime = new PrettyTime();
            long timeAgo = Tools.timeStringtoMilis(str_date_time);
            txt_date_time.setText(prettyTime.format(new Date(timeAgo)));
        } else if (AppConfig.ENABLE_DATE_DISPLAY && !AppConfig.DISPLAY_DATE_AS_TIME_AGO) {
            txt_date_time.setText(Tools.getFormatedDateSimple(str_date_time));
        } else {
            lyt_date.setVisibility(View.GONE);
        }


        if (str_type != null && str_type.equals("youtube")) {
            Picasso.get()
                    .load(Constant.YOUTUBE_IMAGE_FRONT + str_video_id + Constant.YOUTUBE_IMAGE_BACK_HQ)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(video_thumbnail);
        } else {
            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + str_thumbnail)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(video_thumbnail);
        }

        video_thumbnail.setOnClickListener(view -> {

            if (Tools.isNetworkAvailable(ActivityVideoDetailOffline.this)) {

                if (str_type != null && str_type.equals("youtube")) {
                    Intent intent = new Intent(ActivityVideoDetailOffline.this, ActivityYoutubePlayer.class);
                    intent.putExtra(Constant.KEY_VIDEO_ID, str_video_id);
                    startActivity(intent);
                } else if (str_type != null && str_type.equals("Upload")) {
                    Intent intent = new Intent(ActivityVideoDetailOffline.this, ActivityVideoPlayer.class);
                    intent.putExtra("url", AppConfig.ADMIN_PANEL_URL + "/upload/video/" + str_url);
                    startActivity(intent);
                } else {
                    if (str_url != null && str_url.startsWith("rtmp://")) {
                        Intent intent = new Intent(ActivityVideoDetailOffline.this, ActivityRtmpPlayer.class);
                        intent.putExtra("url", str_url);
                        startActivity(intent);
                    } else if (str_url != null && str_url.startsWith("rtsp://")) {
                        Intent intent = new Intent(ActivityVideoDetailOffline.this, ActivityRtmpPlayer.class);
                        intent.putExtra("url", str_url);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(ActivityVideoDetailOffline.this, ActivityVideoPlayer.class);
                        intent.putExtra("url", str_url);
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
        String htmlText = str_description;

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
            String share_title = android.text.Html.fromHtml(str_title).toString();
            String share_content = android.text.Html.fromHtml(getResources().getString(R.string.share_text)).toString();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        });

    }

    private void loadViewed() {
        new MyTask().execute(AppConfig.ADMIN_PANEL_URL + "/api/get_total_views/?id=" + str_vid);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
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

    public void addFavorite() {

        List<Video> data = databaseHandler.getFavRow(str_vid);
        if (data.size() == 0) {
            image_favorite.setImageResource(R.drawable.ic_fav_outline);
        } else {
            if (data.get(0).getVid().equals(str_vid)) {
                image_favorite.setImageResource(R.drawable.ic_fav);
            }
        }

        image_favorite.setOnClickListener(view -> {
            List<Video> data1 = databaseHandler.getFavRow(str_vid);
            if (data1.size() == 0) {
                databaseHandler.AddtoFavorite(new Video(
                        str_category,
                        str_vid,
                        str_title,
                        str_url,
                        str_video_id,
                        str_thumbnail,
                        str_duration,
                        str_description,
                        str_type,
                        long_total_views,
                        str_date_time
                ));
                snackbar = Snackbar.make(view, getResources().getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
                snackbar.show();

                image_favorite.setImageResource(R.drawable.ic_fav);

            } else {
                if (data1.get(0).getVid().equals(str_vid)) {
                    databaseHandler.RemoveFav(new Video(str_vid));
                    snackbar = Snackbar.make(view, getResources().getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    image_favorite.setImageResource(R.drawable.ic_fav_outline);
                }
            }
        });

    }

    public void onReceiveNotification() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constant.PUSH_NOTIFICATION)) {
                    NotificationUtils.showDialogNotification(ActivityVideoDetailOffline.this, intent);
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
