package com.coursee.free.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Html;

import androidx.core.app.NotificationCompat;

import com.coursee.free.R;
import com.coursee.free.activities.ActivitySplash;
import com.onesignal.OneSignal;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.onesignal.OSNotification;
import com.onesignal.OSNotificationReceivedEvent;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationExtenderExample implements OneSignal.OSRemoteNotificationReceivedHandler {

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    String id, message, bigpicture, title, cname, url;
    private String NOTIFICATION_CHANNEL_ID = "your_videos_channel_app_channel_01";

    @Override
    public void remoteNotificationReceived(Context context, OSNotificationReceivedEvent notificationReceivedEvent) {
        OSNotification notification = notificationReceivedEvent.getNotification();

        title = notification.getTitle();
        message = notification.getBody();
        bigpicture = notification.getBigPicture();

        try {
            JSONObject additionalData = notification.getAdditionalData();
            if (additionalData != null) {
                id = additionalData.getString("cat_id");
                cname = additionalData.getString("cat_name");
                url = additionalData.getString("external_link");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendNotification(context);
        notificationReceivedEvent.complete(notification);
    }

    private void sendNotification(Context context) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent;
        if (id.equals("0") && !url.equals("false") && !url.trim().isEmpty()) {
            intent = new Intent(context, ActivitySplash.class);
            intent.putExtra("nid", id);
            intent.putExtra("external_link", url);
            intent.putExtra("cname", cname);
        } else {
            intent = new Intent(context, ActivitySplash.class);
            intent.putExtra("nid", id);
            intent.putExtra("external_link", url);
            intent.putExtra("cname", cname);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Your Videos Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, flags);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification_large_icon))
                .setAutoCancel(true)
                .setSound(uri)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setLights(Color.RED, 800, 800);

        mBuilder.setSmallIcon(getNotificationIcon(context, mBuilder));
        mBuilder.setContentTitle(title);
        mBuilder.setTicker(message);

        if (bigpicture != null) {
            mBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(getBitmapFromURL(bigpicture))
                    .setSummaryText(Html.fromHtml(message)));
            mBuilder.setContentText(Html.fromHtml(message));
        } else {
            mBuilder.setContentText(Html.fromHtml(message));
        }

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private int getNotificationIcon(Context context, NotificationCompat.Builder notificationBuilder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(getColour());
            return R.drawable.ic_stat_onesignal_default;
        } else {
            return R.drawable.ic_stat_onesignal_default;
        }
    }

    private int getColour() {
        return 0x3F51B5;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

}