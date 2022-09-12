package com.coursee.free.utils;

import com.coursee.free.config.AppConfig;

public class Constant {

    //global variables
    public static final String POSITION = "POSITION_ID";
    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";
    public static final String KEY_VIDEO_CATEGORY_ID = "category_id";
    public static final String KEY_VIDEO_CATEGORY_NAME = "category_name";
    public static final String KEY_VID = "vid";
    public static final String KEY_VIDEO_TITLE = "video_title";
    public static final String KEY_VIDEO_URL = "video_url";
    public static final String KEY_VIDEO_ID = "video_id";
    public static final String KEY_VIDEO_THUMBNAIL = "video_thumbnail";
    public static final String KEY_VIDEO_DURATION = "video_duration";
    public static final String KEY_VIDEO_DESCRIPTION = "video_description";
    public static final String KEY_VIDEO_TYPE = "video_type";
    public static final String KEY_VIDEO_SIZE = "size";
    public static final String KEY_TOTAL_VIEWS = "total_views";
    public static final String KEY_DATE_TIME = "date_time";
    public static final String YOUTUBE_IMAGE_FRONT = "http://img.youtube.com/vi/";
    public static final String YOUTUBE_IMAGE_BACK_MQ = "/mqdefault.jpg";
    public static final String YOUTUBE_IMAGE_BACK_HQ = "/hqdefault.jpg";
    public static final String TOKEN_URL = AppConfig.ADMIN_PANEL_URL + "/register.php";
    public static final String TOPIC_GLOBAL = "global";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";
    public static final String SHARED_PREF = "ah_firebase";
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;
    public static final long DELAY_TIME = 250;
    public static final int MAX_SEARCH_RESULT = 100;
    public static final String NOTIFICATION_CHANNEL_NAME = "videos_channel_01";

    public static final String MOST_POPULAR = "n.total_views DESC";
    public static final String ADDED_NEWEST = "n.id DESC";
    public static final String ADDED_OLDEST = "n.id ASC";

    public static final int VIDEO_LIST_DEFAULT = 0;
    public static final int VIDEO_LIST_COMPACT = 1;

    public static final int CATEGORY_LIST = 0;
    public static final int CATEGORY_GRID_2_COLUMN = 1;
    public static final int CATEGORY_GRID_3_COLUMN = 2;

    public static final String AD_STATUS_ON = "on";
    public static final String ADMOB = "admob";
    public static final String FAN = "fan";
    public static final String STARTAPP = "startapp";

    //startapp native ad image parameters
    public static final int STARTAPP_IMAGE_XSMALL = 1; //for image size 100px X 100px
    public static final int STARTAPP_IMAGE_SMALL = 2; //for image size 150px X 150px
    public static final int STARTAPP_IMAGE_MEDIUM = 3; //for image size 340px X 340px
    public static final int STARTAPP_IMAGE_LARGE = 4; //for image size 1200px X 628px

}