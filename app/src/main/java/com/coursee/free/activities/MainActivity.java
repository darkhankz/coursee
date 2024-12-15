package com.coursee.free.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.coursee.free.BuildConfig;
import com.coursee.free.R;
import com.coursee.free.callbacks.CallbackUser;
import com.coursee.free.config.AppConfig;
import com.coursee.free.fragments.FragmentCategory;
import com.coursee.free.fragments.FragmentFavorite;
import com.coursee.free.fragments.FragmentRecent;
import com.coursee.free.fragments.FragmentSettings;
import com.coursee.free.models.Setting;
import com.coursee.free.models.User;
import com.coursee.free.notification.NotificationUtils;
import com.coursee.free.rests.ApiInterface;
import com.coursee.free.rests.RestAdapter;
import com.coursee.free.utils.AdsPref;
import com.coursee.free.utils.AppBarLayoutBehavior;
import com.coursee.free.utils.Constant;
import com.coursee.free.utils.GDPR;
import com.coursee.free.utils.HttpTask;
import com.coursee.free.utils.SharedPref;
import com.coursee.free.utils.Tools;
import com.duolingo.open.rtlviewpager.RtlViewPager;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationBarView;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.coursee.free.utils.Constant.ADMOB;
import static com.coursee.free.utils.Constant.AD_STATUS_ON;
import static com.coursee.free.utils.Constant.STARTAPP;

public class MainActivity extends AppCompatActivity {

    private long exitTime = 0;
    MyApplication myApplication;
    private BottomNavigationView navigation;
    private ViewPager viewPager;
    private RtlViewPager viewPagerRTL;
    private TextView title_toolbar;
    MenuItem prevMenuItem;
    int pager_number = 4;
    SharedPreferences preferences;
    BroadcastReceiver broadcastReceiver;
    View view;
    String androidId;
    ImageButton btn_search;
    SharedPref sharedPref;
    User user;
    private Call<CallbackUser> callbackCall = null;
    public ImageButton btn_sort;
    CoordinatorLayout coordinatorLayout;
    AdsPref adsPref;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(STARTAPP)) {
            StartAppSDK.init(MainActivity.this, adsPref.getStartappAppID(), false);
            //StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG);
        }
        if (AppConfig.ENABLE_RTL_MODE) {
            setContentView(R.layout.activity_main_rtl);
        } else {
            setContentView(R.layout.activity_main);
        }
        view = findViewById(android.R.id.content);
        if (AppConfig.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(STARTAPP)) {
            StartAppSDK.setUserConsent(this, "pas", System.currentTimeMillis(), true);
            StartAppAd.disableSplash();
        }

        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(ADMOB)) {
            GDPR.updateConsentStatus(this);

            MobileAds.initialize(MainActivity.this, initializationStatus -> {
                // Callback после инициализации
            });
                    }

        getAdsLog();

        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        AppBarLayout appBarLayout = findViewById(R.id.appbarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        myApplication = MyApplication.getInstance();

        title_toolbar = findViewById(R.id.title_toolbar);
        btn_sort = findViewById(R.id.btn_sort);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        navigation = findViewById(R.id.navigation);
        navigation.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);


        if (AppConfig.ENABLE_RTL_MODE) {
            initRTLViewPager();
        } else {
            initViewPager();
        }

        onReceiveNotification();

        NotificationUtils.oneSignalNotificationHandler(this, getIntent());
        NotificationUtils.fcmNotificationHandler(this, getIntent());
        Tools.getCategoryPosition(this, getIntent());

        requestDetailsPostApi();
        initToolbarIcon();
        validate();

    }

    public void initViewPager() {
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(pager_number);
        navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_category:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_favorite:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.navigation_settings:
                    viewPager.setCurrentItem(3);
                    return true;
            }
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPager.getCurrentItem() == 0) {
                    title_toolbar.setText(getResources().getString(R.string.app_name));
                    showSortMenu(true);
                } else if (viewPager.getCurrentItem() == 1) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_category));
                    showSortMenu(false);
                } else if (viewPager.getCurrentItem() == 2) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_favorite));
                    showSortMenu(false);
                } else if (viewPager.getCurrentItem() == 3) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_settings));
                    showSortMenu(false);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void initRTLViewPager() {
        viewPagerRTL = findViewById(R.id.viewpager_rtl);
        viewPagerRTL.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPagerRTL.setOffscreenPageLimit(pager_number);
        navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPagerRTL.setCurrentItem(0);
                    return true;
                case R.id.navigation_category:
                    viewPagerRTL.setCurrentItem(1);
                    return true;
                case R.id.navigation_favorite:
                    viewPagerRTL.setCurrentItem(2);
                    return true;
                case R.id.navigation_settings:
                    viewPagerRTL.setCurrentItem(3);
                    return true;
            }
            return false;
        });

        viewPagerRTL.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPagerRTL.getCurrentItem() == 0) {
                    title_toolbar.setText(getResources().getString(R.string.app_name));
                    showSortMenu(true);
                } else if (viewPagerRTL.getCurrentItem() == 1) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_category));
                    showSortMenu(false);
                } else if (viewPagerRTL.getCurrentItem() == 2) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_favorite));
                    showSortMenu(false);
                } else if (viewPagerRTL.getCurrentItem() == 3) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_settings));
                    showSortMenu(false);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void selectCategory() {
        viewPager.setCurrentItem(1);
    }

    public void showSortMenu(Boolean show) {
        if (show) {
            btn_sort.setVisibility(View.VISIBLE);
        } else {
            btn_sort.setVisibility(View.GONE);
        }
    }

    public class MyAdapter extends FragmentPagerAdapter {

        MyAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new FragmentRecent();
                case 1:
                    return new FragmentCategory();
                case 2:
                    return new FragmentFavorite();
                case 3:
                    return new FragmentSettings();
            }
            return null;
        }

        @Override
        public int getCount() {
            return pager_number;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void initToolbarIcon() {

        if (sharedPref.getIsDarkTheme()) {
            findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
            navigation.setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
        } else {
            findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        btn_search = findViewById(R.id.btn_search);
        btn_search.setOnClickListener(view -> new Handler().postDelayed(() -> startActivity(new Intent(getApplicationContext(), ActivitySearch.class)), 50));

    }

    @Override
    public void onBackPressed() {
        if (AppConfig.ENABLE_RTL_MODE) {
            if (viewPagerRTL.getCurrentItem() != 0) {
                viewPagerRTL.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        } else {
            if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        }
    }

    public void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public void validate() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<Setting> call = apiInterface.getPackageName();
        call.enqueue(new Callback<Setting>() {
            @Override
            public void onResponse(Call<Setting> call, Response<Setting> response) {
                String package_name = response.body().getPackage_name();
                try {
                    if (BuildConfig.APPLICATION_ID.equals(package_name)) {
                        Log.d("INFO", "Validated");
                    } else {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setTitle(getResources().getString(R.string.whops));
                        dialog.setMessage(getResources().getString(R.string.msg_validate));
                        dialog.setPositiveButton(getResources().getString(R.string.dialog_ok), (dialogInterface, i) -> finish());
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Setting> call, Throwable t) {
            }

        });
    }


    private void requestDetailsPostApi() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getUserToken('"' + androidId + '"');
        callbackCall.enqueue(new Callback<CallbackUser>() {
            @Override
            public void onResponse(Call<CallbackUser> call, Response<CallbackUser> response) {
                CallbackUser resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    user = resp.response;
                    String token = user.token;
                    String unique_id = user.user_unique_id;
                    String pref_token = preferences.getString("fcm_token", null);

                    if (token.equals(pref_token) && unique_id.equals(androidId)) {
                        Log.d("TOKEN", "FCM Token already exists");
                    } else {
                        updateRegistrationIdToBackend();
                    }
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackUser> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void onFailRequest() {
        if (Tools.isConnect(this)) {
            sendRegistrationIdToBackend();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.failed_text), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRegistrationIdToBackend() {

        Log.d("FCM_TOKEN", "Send data to server...");

        String token = preferences.getString("fcm_token", null);
        String appVersion = BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")";
        String osVersion = currentVersion() + " " + Build.VERSION.RELEASE;
        String model = android.os.Build.MODEL;
        String manufacturer = android.os.Build.MANUFACTURER;

        if (token != null) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("user_android_token", token));
            nameValuePairs.add(new BasicNameValuePair("user_unique_id", androidId));
            nameValuePairs.add(new BasicNameValuePair("user_app_version", appVersion));
            nameValuePairs.add(new BasicNameValuePair("user_os_version", osVersion));
            nameValuePairs.add(new BasicNameValuePair("user_device_model", model));
            nameValuePairs.add(new BasicNameValuePair("user_device_manufacturer", manufacturer));
            new HttpTask(null, MainActivity.this, AppConfig.ADMIN_PANEL_URL + "/token-register.php", nameValuePairs, false).execute();
            Log.d("FCM_TOKEN_VALUE", token + " " + androidId);
        }

    }

    private void updateRegistrationIdToBackend() {

        Log.d("FCM_TOKEN", "Update data to server...");
        String token = preferences.getString("fcm_token", null);
        if (token != null) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("user_android_token", token));
            nameValuePairs.add(new BasicNameValuePair("user_unique_id", androidId));
            new HttpTask(null, MainActivity.this, AppConfig.ADMIN_PANEL_URL + "/token-update.php", nameValuePairs, false).execute();
            Log.d("FCM_TOKEN_VALUE", token + " " + androidId);
        }

    }

    public static String currentVersion() {
        double release = Double.parseDouble(Build.VERSION.RELEASE.replaceAll("(\\d+[.]\\d+)(.*)", "$1"));
        String codeName = "Android";
        if (release >= 4.1 && release < 4.4) codeName = "Jelly Bean";
        else if (release < 5) codeName = "Kit Kat";
        else if (release < 6) codeName = "Lollipop";
        else if (release < 7) codeName = "Marshmallow";
        else if (release < 8) codeName = "Nougat";
        else if (release < 9) codeName = "Oreo";
        else if (release < 10) codeName = "Pie";
        else if (release < 11) codeName = "Q";
        return codeName;
    }

    public void onReceiveNotification() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constant.PUSH_NOTIFICATION)) {
                    NotificationUtils.showDialogNotification(MainActivity.this, intent);
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

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    public void getAdsLog() {
        Log.d("ads_response", adsPref.getAdStatus());
        Log.d("ads_response", adsPref.getAdType());
        Log.d("ads_response", adsPref.getAdMobPublisherId());
        Log.d("ads_response", adsPref.getAdMobAppId());
        Log.d("ads_response_fan", adsPref.getFanBannerUnitId());
        Log.d("ads_response", "" + adsPref.getNativeAdIndex());
        Log.d("ads_response", "" + adsPref.getYoutubeAPIKey());
    }

}
