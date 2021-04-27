package com.xxpermissions.lib;

public final class Permission {

    private Permission() {}

    /**
     * External storage permissions (special permissions, Android 11 and above required)
     *
     * If your application needs to be put on GooglePlay, you need to check it out: https://support.google.com/googleplay/android-developer/answer/9956427
     */
    public static final String MANAGE_EXTERNAL_STORAGE = "android.permission.MANAGE_EXTERNAL_STORAGE";

    /** Install application permissions (special permissions, Android 8.0 and above required) */
    public static final String REQUEST_INSTALL_PACKAGES = "android.permission.REQUEST_INSTALL_PACKAGES";

    /** Notification bar permission (special permission, Android 6.0 and above is required, note that this permission can be applied without registering in the manifest file) */
    public static final String NOTIFICATION_SERVICE = "android.permission.NOTIFICATION_SERVICE";

    /** Floating window permissions (special permissions, Android 6.0 and above required) */
    public static final String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";

    /** System setting permissions (special permissions, Android 6.0 and above required) */
    public static final String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";

    /** Read external storage */
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    /** Write to external storage */
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

    /** Camera permission */
    public static final String CAMERA = "android.permission.CAMERA";

    /** Recording permission */
    public static final String RECORD_AUDIO = "android.permission.RECORD_AUDIO";

    /** Get precise location */
    public static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    /** Get rough location */
    public static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    /** Get location in the background (requires Android 10.0 and above) */
    public static final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";

    /** Read contact */
    public static final String READ_CONTACTS = "android.permission.READ_CONTACTS";
    /** Modify contact */
    public static final String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    /** Access account list */
    public static final String GET_ACCOUNTS = "android.permission.GET_ACCOUNTS";

    /** Read calendar */
    public static final String READ_CALENDAR = "android.permission.READ_CALENDAR";
    /** Modify calendar */
    public static final String WRITE_CALENDAR = "android.permission.WRITE_CALENDAR";

    /** Read the geographic location in the photo (requires Android 10.0 and above)*/
    public static final String ACCESS_MEDIA_LOCATION = "android.permission.ACCESS_MEDIA_LOCATION";

    /** Read phone status (this permission failed to apply on some mobile phones) */
    public static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    /** dial number */
    public static final String CALL_PHONE = "android.permission.CALL_PHONE";
    /** Read call log */
    public static final String READ_CALL_LOG = "android.permission.READ_CALL_LOG";
    /** Modify call history */
    public static final String WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG";
    /** Add voice mail */
    public static final String ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL";
    /** Use SIP video */
    public static final String USE_SIP = "android.permission.USE_SIP";
    /**
     * Handling outgoing calls
     *
     * @deprecated is obsolete in Android 10, please see: https://developer.android.google.cn/reference/android/Manifest.permission?hl=zh_cn#PROCESS_OUTGOING_CALLS
     */
    public static final String PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS";
    /**
     * Answer calls (Android 8.0 and above are required, and Android 8.0 and below can use simulated headset key events to answer calls, this method does not require permission)
     */
    public static final String ANSWER_PHONE_CALLS = "android.permission.ANSWER_PHONE_CALLS";
    /** Read mobile phone number (Requires Android 8.0 and above) */
    public static final String READ_PHONE_NUMBERS = "android.permission.READ_PHONE_NUMBERS";

    /** Use sensor */
    public static final String BODY_SENSORS = "android.permission.BODY_SENSORS";
    /** Get activity steps (requires Android 10.0 and above) */
    public static final String ACTIVITY_RECOGNITION = "android.permission.ACTIVITY_RECOGNITION";

    /** send messages */
    public static final String SEND_SMS = "android.permission.SEND_SMS";
    /** Receive SMS */
    public static final String RECEIVE_SMS = "android.permission.RECEIVE_SMS";
    /** Read SMS */
    public static final String READ_SMS = "android.permission.READ_SMS";
    /** Receive WAP push messages */
    public static final String RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH";
    /** Receive MMS */
    public static final String RECEIVE_MMS = "android.permission.RECEIVE_MMS";

    /** Allow the calling app to continue the call initiated in another app (Requires Android 9.0 and above) */
    public static final String ACCEPT_HANDOVER = "android.permission.ACCEPT_HANDOVER";

    /**
     * Rights Groups
     */
    public static final class Group {

        /** Storage permissions */
        public static final String[] STORAGE = new String[]{
                Permission.READ_EXTERNAL_STORAGE,
                Permission.WRITE_EXTERNAL_STORAGE};

        /** Calendar permissions */
        public static final String[] CALENDAR = new String[]{
                Permission.READ_CALENDAR,
                Permission.WRITE_CALENDAR};

        /** Contact permissions */
        public static final String[] CONTACTS = new String[]{
                Permission.READ_CONTACTS,
                Permission.WRITE_CONTACTS,
                Permission.GET_ACCOUNTS};

        /** Sensor permission */
        public static final String[] SENSORS = new String[] {
                Permission.BODY_SENSORS,
                Permission.ACTIVITY_RECOGNITION};
    }
}