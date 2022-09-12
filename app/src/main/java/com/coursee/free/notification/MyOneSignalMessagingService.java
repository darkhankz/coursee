package com.coursee.free.notification;

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
import androidx.core.content.ContextCompat;

import com.coursee.free.R;
import com.coursee.free.activities.MainActivity;
import com.coursee.free.utils.Constant;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyOneSignalMessagingService extends NotificationExtenderService {

    public static final int NOTIFICATION_ID = 1;
    String message, bigpicture, title, cname, url;
    String nid;

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {

        title = receivedResult.payload.title;
        message = receivedResult.payload.body;
        bigpicture = receivedResult.payload.bigPicture;

        try {
            nid = receivedResult.payload.additionalData.getString("cat_id");
            cname = receivedResult.payload.additionalData.getString("cat_name");
            url = receivedResult.payload.additionalData.getString("external_link");
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendNotification();

        return true;
    }

    private void sendNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent;
        if (nid.equals("0") && !url.equals("false") && !url.trim().isEmpty()) {
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("nid", nid);
            intent.putExtra("external_link", url);
            intent.putExtra("cname", cname);
        } else {
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("nid", nid);
            intent.putExtra("external_link", url);
            intent.putExtra("cname", cname);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(Constant.NOTIFICATION_CHANNEL_NAME, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, Constant.NOTIFICATION_CHANNEL_NAME)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_large))
                .setContentTitle(title)
                .setTicker(message)
                .setAutoCancel(true)
                .setSound(uri)
                .setChannelId(Constant.NOTIFICATION_CHANNEL_NAME)
                .setLights(Color.RED, 800, 800);

        mBuilder.setSmallIcon(getNotificationIcon(mBuilder));

        if (bigpicture != null) {
            mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(bigpicture)).setSummaryText(Html.fromHtml(message)));
            mBuilder.setContentText(Html.fromHtml(message));
        } else {
            mBuilder.setContentText(Html.fromHtml(message));
        }

        mBuilder.setContentIntent(contentIntent);
        notificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());

    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
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