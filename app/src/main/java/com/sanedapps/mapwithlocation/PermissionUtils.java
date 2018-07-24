package com.sanedapps.mapwithlocation;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;


public class PermissionUtils {

    public static boolean isPermissionGranted(Activity activity, String permissionName, int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(permissionName)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Tag", "Permission is granted");
                return true;
            } else {
                Log.v("tag", "Permission is revoked");
                ActivityCompat.requestPermissions(activity,
                        new String[]{permissionName}, requestCode);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("tag", "Permission is granted");
            return true;
        }
    }

    public static final String[] IMAGE_PERMISSIONS =
            {permission.CAMERA,
                    permission.READ_EXTERNAL_STORAGE,
                    permission.WRITE_EXTERNAL_STORAGE};

    public static final String[] DOWNLOAD_PERMISSIONS =
            {permission.READ_EXTERNAL_STORAGE,
                    permission.WRITE_EXTERNAL_STORAGE};

    public static final String[] GPS_PERMISSION =
            {permission.ACCESS_FINE_LOCATION,
                    permission.ACCESS_COARSE_LOCATION};

    public static final String[] CALL_PHONE =
            {permission.CALL_PHONE};

    public static final int IMAGE_PERMISSION_RESPONSE = 1;

    public static final int DOWNLOAD_PERMISSION_RESPONSE = 2;

    /**
     * @return whether all the required permission for taking and picking an image are granted or not
     */
    public static boolean areImagePermissionsGranted(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        for (String permission : IMAGE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllPermissionGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("SSSs", permission.toString());
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean canMakeSmores(int BuildVersion) {
        return (Build.VERSION.SDK_INT > BuildVersion);

//        Build.VERSION_CODES.LOLLIPOP_MR1
    }
}