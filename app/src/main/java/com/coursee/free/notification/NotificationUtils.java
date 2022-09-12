package com.coursee.free.notification;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.coursee.free.R;
import com.coursee.free.activities.ActivityNotificationDetail;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationUtils {

    private Context context;

    public NotificationUtils(Context context) {
        this.context = context;
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    public void playNotificationSound() {
        try {
            Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "");
            Ringtone r = RingtoneManager.getRingtone(context, sound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Clears notification tray messages
    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void oneSignalNotificationHandler(Activity activity, Intent intent) {

        if (intent.hasExtra("nid")) {

            String nid = intent.getStringExtra("nid");
            String url = intent.getStringExtra("external_link");

            if (nid.equals("0")) {
                if (url.equals("") || url.equals("no_url")) {
                    Log.d("OneSignal", "do nothing");
                } else {
                    Intent b = new Intent(Intent.ACTION_VIEW);
                    b.setData(Uri.parse(url));
                    activity.startActivity(b);
                }
            } else {
                Intent act2 = new Intent(activity, ActivityNotificationDetail.class);
                act2.putExtra("id", nid);
                activity.startActivity(act2);
            }

        }

    }

    public static void fcmNotificationHandler(Activity activity, Intent intent) {

        String id = intent.getStringExtra("id");
        String url = intent.getStringExtra("link");
        if (id != null) {
            if (id.equals("0")) {
                if (!url.equals("")) {
                    Intent e = new Intent(Intent.ACTION_VIEW);
                    e.setData(Uri.parse(url));
                    activity.startActivity(e);
                }
                Log.d("FCM_INFO", " id : " + id);
            } else {
                Intent action = new Intent(activity, ActivityNotificationDetail.class);
                action.putExtra("id", id);
                activity.startActivity(action);
                Log.d("FCM_INFO", "id : " + id);
            }
        }

    }

    public static void showDialogNotification(Activity activity, Intent intent) {

        final String id = intent.getStringExtra("id");
        final String title = intent.getStringExtra("title");
        final String message = intent.getStringExtra("message");
        final String image_url = intent.getStringExtra("image_url");
        final String url = intent.getStringExtra("link");

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(activity);
        View view = layoutInflaterAndroid.inflate(R.layout.custom_dialog, null);

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setView(view);

        final TextView notification_title = view.findViewById(R.id.title);
        final TextView notification_message = view.findViewById(R.id.message);
        final ImageView notification_image = view.findViewById(R.id.big_image);

        if (id != null) {
            if (id.equals("0")) {
                if (!url.equals("")) {
                    notification_title.setText(title);
                    notification_message.setText(Html.fromHtml(message));
                    Picasso.get()
                            .load(image_url.replace(" ", "%20"))
                            .placeholder(R.drawable.ic_thumbnail)
                            .into(notification_image);
                    alert.setPositiveButton("Open link", (dialogInterface, i) -> {
                        Intent intent1 = new Intent(Intent.ACTION_VIEW);
                        intent1.setData(Uri.parse(url));
                        activity.startActivity(intent1);
                    });
                    alert.setNegativeButton(activity.getResources().getString(R.string.dialog_dismiss), null);
                } else {
                    notification_title.setText(title);
                    notification_message.setText(Html.fromHtml(message));
                    Picasso.get()
                            .load(image_url.replace(" ", "%20"))
                            .placeholder(R.drawable.ic_thumbnail)
                            .into(notification_image);
                    alert.setPositiveButton(activity.getResources().getString(R.string.dialog_ok), null);
                }
            } else {
                notification_title.setText(title);
                notification_message.setText(Html.fromHtml(message));
                Picasso.get()
                        .load(image_url.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(notification_image);

                alert.setPositiveButton(activity.getResources().getString(R.string.dialog_read_more), (dialog, which) -> {
                    Intent intent12 = new Intent(activity, ActivityNotificationDetail.class);
                    intent12.putExtra("id", id);
                    activity.startActivity(intent12);
                });
                alert.setNegativeButton(activity.getResources().getString(R.string.dialog_dismiss), null);
            }
            alert.setCancelable(false);
            alert.show();

        }

    }

}
