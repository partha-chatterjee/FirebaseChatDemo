package partha.firebasechatdemo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by DAT-Asset-110 on 04-08-2017.
 */

public class Utils {

    public static boolean checkCamerapermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            askCallPermission(context, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
            return false;
        } else {
//            uploadImageDialog();
            return true;
        }
    }

    public static void askCallPermission(Context context, String[] permissions, int request_code) {
        ActivityCompat.requestPermissions((Activity) context, permissions, request_code);
    }
}
