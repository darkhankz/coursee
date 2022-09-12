package com.coursee.free.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.coursee.free.BuildConfig;
import com.coursee.free.R;
import com.coursee.free.config.AppConfig;
import com.coursee.free.databases.DatabaseHandlerFavorite;
import com.coursee.free.models.Video;
import com.coursee.free.utils.AdsPref;
import com.coursee.free.utils.Constant;
import com.coursee.free.utils.NativeTemplateStyle;
import com.coursee.free.utils.SharedPref;
import com.coursee.free.utils.TemplateView;
import com.coursee.free.utils.Tools;
import com.balysv.materialripple.MaterialRippleLayout;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.formats.MediaView;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.coursee.free.utils.Constant.ADMOB;
import static com.coursee.free.utils.Constant.AD_STATUS_ON;
import static com.coursee.free.utils.Constant.FAN;
import static com.coursee.free.utils.Constant.STARTAPP;
import static com.coursee.free.utils.Constant.STARTAPP_IMAGE_MEDIUM;
import static com.coursee.free.utils.Constant.VIDEO_LIST_COMPACT;

public class AdapterFavorite extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;

    private List<Video> items;

    private Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemClickListener mOnItemOverflowClickListener;
    private Video pos;
    private CharSequence charSequence = null;
    private DatabaseHandlerFavorite databaseHandler;

    private StartAppNativeAd startAppNativeAd;
    private NativeAdDetails nativeAdDetails = null;

    public interface OnItemClickListener {
        void onItemClick(View view, Video obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setOnItemOverflowClickListener(final OnItemClickListener mItemOverflowClickListener) {
        this.mOnItemOverflowClickListener = mItemOverflowClickListener;
    }

    public AdapterFavorite(Context context, RecyclerView view, List<Video> items) {
        this.items = items;
        this.context = context;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView category_name;
        public TextView video_title;
        public TextView video_duration;
        public TextView total_views;
        public TextView date_time;
        public LinearLayout lyt_view;
        public LinearLayout lyt_date;
        public ImageView video_thumbnail;
        public MaterialRippleLayout lyt_parent;
        public ImageButton overflow;

        TemplateView admob_native_template;
        MediaView admob_media_view;

        private NativeAd nativeAd;
        RelativeLayout lyt_fan_native;
        private NativeAdLayout nativeAdLayout;
        private LinearLayout nativeAdView;

        RelativeLayout lyt_startapp_native;
        ImageView startapp_native_image;
        TextView startapp_native_title;
        TextView startapp_native_description;
        Button startapp_native_button;

        public OriginalViewHolder(View v) {
            super(v);
            category_name = v.findViewById(R.id.category_name);
            video_title = v.findViewById(R.id.video_title);
            video_duration = v.findViewById(R.id.video_duration);
            date_time = v.findViewById(R.id.date_time);
            total_views = v.findViewById(R.id.total_views);
            lyt_view = v.findViewById(R.id.lyt_view_count);
            lyt_date = v.findViewById(R.id.lyt_date);
            video_thumbnail = v.findViewById(R.id.video_thumbnail);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            overflow = v.findViewById(R.id.overflow);

            //admob native ad
            admob_native_template = v.findViewById(R.id.native_template);
            admob_media_view = v.findViewById(R.id.media_view);

            //fan native ad
            lyt_fan_native = v.findViewById(R.id.lyt_fan_native);
            nativeAdLayout = v.findViewById(R.id.native_ad_container);

            //startapp native ad
            lyt_startapp_native = v.findViewById(R.id.lyt_startapp_native);
            startapp_native_image = v.findViewById(R.id.startapp_native_image);
            startapp_native_title = v.findViewById(R.id.startapp_native_title);
            startapp_native_description = v.findViewById(R.id.startapp_native_description);
            startapp_native_button = v.findViewById(R.id.startapp_native_button);
            startapp_native_button.setOnClickListener(v1 -> itemView.performClick());
        }

        private void bindAdMobNativeAdView() {
            final SharedPref sharedPref = new SharedPref(context);
            final AdsPref adsPref = new AdsPref(context);
            AdLoader adLoader = new AdLoader.Builder(context, adsPref.getAdMobNativeId())
                    .forUnifiedNativeAd(unifiedNativeAd -> {
                        if (sharedPref.getIsDarkTheme()) {
                            ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.colorBackgroundDark));
                            NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                            admob_native_template.setStyles(styles);
                        } else {
                            ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.colorBackgroundLight));
                            NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                            admob_native_template.setStyles(styles);
                        }
                        admob_media_view.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                        admob_native_template.setNativeAd(unifiedNativeAd);
                    }).withAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            if (getAdapterPosition() % adsPref.getNativeAdInterval() == adsPref.getNativeAdIndex()) {
                                admob_native_template.setVisibility(View.VISIBLE);
                            } else {
                                admob_native_template.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            admob_native_template.setVisibility(View.GONE);
                        }
                    })
                    .build();
            adLoader.loadAd(Tools.getAdRequest((Activity) context));

        }

        private void bindFanNativeAdView() {
            final AdsPref adsPref = new AdsPref(context);
            final SharedPref sharedPref = new SharedPref(context);
            if (BuildConfig.DEBUG) {
                nativeAd = new NativeAd(context, "IMG_16_9_APP_INSTALL#" + adsPref.getFanNativeUnitId());
            } else {
                nativeAd = new NativeAd(context, adsPref.getFanNativeUnitId());
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
                    if (getAdapterPosition() % adsPref.getNativeAdInterval() == adsPref.getNativeAdIndex()) {
                        lyt_fan_native.setVisibility(View.VISIBLE);
                        if (nativeAd == null || nativeAd != ad) {
                            return;
                        }
                        // Inflate Native Ad into Container
                        //inflateAd(nativeAd);
                        nativeAd.unregisterView();
                        // Add the Ad view into the ad container.
                        LayoutInflater inflater = LayoutInflater.from(context);
                        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.

                        if (sharedPref.getVideoViewType() == VIDEO_LIST_COMPACT) {
                            nativeAdView = (LinearLayout) inflater.inflate(R.layout.gnt_fan_small_template, nativeAdLayout, false);
                        } else {
                            nativeAdView = (LinearLayout) inflater.inflate(R.layout.gnt_fan_medium_template, nativeAdLayout, false);
                        }
                        nativeAdLayout.addView(nativeAdView);

                        // Add the AdOptionsView
                        LinearLayout adChoicesContainer = nativeAdView.findViewById(R.id.ad_choices_container);
                        AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, nativeAdLayout);
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
                    } else {
                        lyt_fan_native.setVisibility(View.GONE);
                    }
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

        private void bindStartAppNativeAdView() {
            startAppNativeAd = new StartAppNativeAd(context);
            final AdsPref adsPref = new AdsPref(context);
            NativeAdPreferences nativePrefs = new NativeAdPreferences()
                    .setAdsNumber(1)
                    .setAutoBitmapDownload(true)
                    .setPrimaryImageSize(STARTAPP_IMAGE_MEDIUM);
            AdEventListener adListener = new AdEventListener() {
                @Override
                public void onReceiveAd(Ad arg0) {
                    if (getAdapterPosition() % adsPref.getNativeAdInterval() == adsPref.getNativeAdIndex()) {
                        ArrayList<NativeAdDetails> nativeAdsList = startAppNativeAd.getNativeAds();
                        if (nativeAdsList.size() > 0) {
                            nativeAdDetails = nativeAdsList.get(0);
                        }
                        if (nativeAdDetails != null) {
                            startapp_native_image.setImageBitmap(nativeAdDetails.getImageBitmap());
                            startapp_native_title.setText(nativeAdDetails.getTitle());
                            startapp_native_description.setText(nativeAdDetails.getDescription());
                            startapp_native_button.setText(nativeAdDetails.isApp() ? "Install" : "Open");
                            nativeAdDetails.registerViewForInteraction(itemView);
                        }
                        lyt_startapp_native.setVisibility(View.VISIBLE);
                    } else {
                        lyt_startapp_native.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailedToReceiveAd(Ad arg0) {
                    lyt_startapp_native.setVisibility(View.GONE);
                }
            };
            startAppNativeAd.loadAd(nativePrefs, adListener);
        }


    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        SharedPref sharedPref = new SharedPref(context);
        if (sharedPref.getVideoViewType() == VIDEO_LIST_COMPACT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_compact, parent, false);
            vh = new OriginalViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_default, parent, false);
            vh = new OriginalViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final Video p = items.get(position);
        final OriginalViewHolder vItem = (OriginalViewHolder) holder;

        final AdsPref adsPref = new AdsPref(context);
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(ADMOB)) {
            if (!adsPref.getAdMobNativeId().equals("0")) {
                if (holder.getAdapterPosition() % adsPref.getNativeAdInterval() == adsPref.getNativeAdIndex()) {
                    vItem.bindAdMobNativeAdView();
                } else {
                    vItem.admob_native_template.setVisibility(View.GONE);
                }
            }
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(FAN)) {
            if (!adsPref.getFanNativeUnitId().equals("0")) {
                if (holder.getAdapterPosition() % adsPref.getNativeAdInterval() == adsPref.getNativeAdIndex()) {
                    vItem.bindFanNativeAdView();
                } else {
                    vItem.lyt_fan_native.setVisibility(View.GONE);
                }
            }
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(STARTAPP)) {
            if (!adsPref.getStartappAppID().equals("0")) {
                if (holder.getAdapterPosition() % adsPref.getNativeAdInterval() == adsPref.getNativeAdIndex()) {
                    vItem.bindStartAppNativeAdView();
                } else {
                    vItem.lyt_startapp_native.setVisibility(View.GONE);
                }
            }
        }

        vItem.category_name.setText(p.category_name);
        vItem.video_title.setText(p.video_title);
        vItem.video_duration.setText(p.video_duration);

        if (AppConfig.ENABLE_VIEW_COUNT) {
            vItem.total_views.setText(Tools.withSuffix(p.total_views) + " " + context.getResources().getString(R.string.views_count));
        } else {
            vItem.lyt_view.setVisibility(View.GONE);
        }

        if (AppConfig.ENABLE_DATE_DISPLAY && AppConfig.DISPLAY_DATE_AS_TIME_AGO) {
            PrettyTime prettyTime = new PrettyTime();
            long timeAgo = Tools.timeStringtoMilis(p.date_time);
            vItem.date_time.setText(prettyTime.format(new Date(timeAgo)));
        } else if (AppConfig.ENABLE_DATE_DISPLAY && !AppConfig.DISPLAY_DATE_AS_TIME_AGO) {
            vItem.date_time.setText(Tools.getFormatedDateSimple(p.date_time));
        } else {
            vItem.lyt_date.setVisibility(View.GONE);
        }

        if (p.video_type != null && p.video_type.equals("youtube")) {
            SharedPref sharedPref = new SharedPref(context);
            if (sharedPref.getVideoViewType() == VIDEO_LIST_COMPACT) {
                Picasso.get()
                        .load(Constant.YOUTUBE_IMAGE_FRONT + p.video_id + Constant.YOUTUBE_IMAGE_BACK_MQ)
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.video_thumbnail);
            } else {
                Picasso.get()
                        .load(Constant.YOUTUBE_IMAGE_FRONT + p.video_id + Constant.YOUTUBE_IMAGE_BACK_HQ)
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.video_thumbnail);
            }
        } else {
            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + p.video_thumbnail)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(vItem.video_thumbnail);
        }

        vItem.lyt_parent.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, p, position);
            }
        });

        vItem.overflow.setOnClickListener(view -> {
            if (mOnItemOverflowClickListener != null) {
                mOnItemOverflowClickListener.onItemClick(view, p, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_ITEM;
    }

}