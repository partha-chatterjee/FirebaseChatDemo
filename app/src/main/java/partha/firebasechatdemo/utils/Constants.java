package partha.firebasechatdemo.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by DAT-Asset-110 on 01-08-2017.
 */

public class Constants {

    public static String SHARED_PREF_NAME="FirebaseChatDemo";
    public static String strShPrefFCMTokenID="fcm_token";

    public static String FROM_USER_ID = "from_user_id";
    public static String FROM_USER_NAME = "from_user_name";
    public static String FROM_USER_IMG = "from_user_img";
    public static String FROM_USER_MAIL = "from_user_mail";

    public static String TO_USER_ID = "to_user_id";
    public static String TO_USER_NAME = "to_user_name";
    public static String TO_USER_IMG = "to_user_img";
    public static String TO_USER_MAIL = "to_user_mail";

    public static String app_display_date_format="dd/MM/yy";
    public static String app_display_time_format="hh:mm a";

    public static String STRING_COPIED="";

    public static final String STDBY_SUFFIX = "-stdby";
    public static final String PUB_KEY = "demo"; // Use Your Pub Key
    public static final String SUB_KEY = "demo"; // Use Your Sub Key
    public static final String USER_NAME = "user_name";
    public static final String JSON_CALL_USER = "call_user";

}
