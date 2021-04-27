package com.xxpermissions.lib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.fragment.app.FragmentActivity;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class PermissionUtils {

    /**
     * Is it Android 11 and above
     */
    static boolean isAndroid11() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    /**
     * Is it Android 10 and above
     */
    static boolean isAndroid10() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    /**
     * Is it Android 9.0 and above
     */
    static boolean isAndroid9() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    /**
     * Whether it is Android 8.0 and above
     */
    static boolean isAndroid8() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * Whether it is Android 7.0 and above
     */
    static boolean isAndroid7() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    /**
     * Whether it is Android 6.0 and above
     */
    static boolean isAndroid6() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Return the permissions registered by the application in the manifest file
     */
    static List<String> getManifestPermissions(Context context) {
        try {
            String[] requestedPermissions = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions;
            // When the manifest file does not have any permissions registered, then this array object is empty
            // https://github.com/getActivity/XXPermissions/issues/35
            return asArrayList(requestedPermissions);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Is there storage permission
     */
    static boolean isGrantedStoragePermission(Context context) {
        if (isAndroid11()) {
            return Environment.isExternalStorageManager();
        }
        return XXPermissions.isGranted(context, Permission.Group.STORAGE);
    }

    /**
     * Is there installation permission
     */
    static boolean isGrantedInstallPermission(Context context) {
        if (isAndroid8()) {
            return context.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    /**
     * Is there permission for floating window
     */
    static boolean isGrantedWindowPermission(Context context) {
        if (isAndroid6()) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * Is there a notification bar permission
     */
    @SuppressWarnings("ConstantConditions")
    static boolean isGrantedNotifyPermission(Context context) {
        if (isAndroid7()) {
            return context.getSystemService(NotificationManager.class).areNotificationsEnabled();
        }

        if (isAndroid6()) {
            // Refer to the method in the Support library: NotificationManagerCompat.from(context).areNotificationsEnabled()
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Method method = appOps.getClass().getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String.class);
                Field field = appOps.getClass().getDeclaredField("OP_POST_NOTIFICATION");
                int value = (int) field.get(Integer.class);
                return ((int) method.invoke(appOps, value, context.getApplicationInfo().uid, context.getPackageName())) == AppOpsManager.MODE_ALLOWED;
            } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException | IllegalAccessException | RuntimeException e) {
                e.printStackTrace();
                return true;
            }
        }

        return true;
    }

    /**
     * Whether there is system setting authority
     */
    static boolean isGrantedSettingPermission(Context context) {
        if (isAndroid6()) {
            return Settings.System.canWrite(context);
        }
        return true;
    }

    /**
     * Determine whether a certain permission set contains special permissions
     */
    static boolean containsSpecialPermission(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        for (String permission: permissions) {
            if (isSpecialPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether a permission is a special permission
     */
    static boolean isSpecialPermission(String permission) {
        return Permission.MANAGE_EXTERNAL_STORAGE.equals(permission) ||
                Permission.REQUEST_INSTALL_PACKAGES.equals(permission) ||
                Permission.SYSTEM_ALERT_WINDOW.equals(permission) ||
                Permission.NOTIFICATION_SERVICE.equals(permission) ||
                Permission.WRITE_SETTINGS.equals(permission);
    }

    /**
     * Determine whether certain permissions are all granted
     */
    static boolean isGrantedPermissions(Context context, List<String> permissions) {
        // If it is Android 6.0 or lower, it will directly return true
        if (!isAndroid6()) {
            return true;
        }

        for (String permission: permissions) {
            if (!isGrantedPermission(context, permission)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get permission not granted
     */
    static List<String> getDeniedPermissions(Context context, List<String> permissions) {
        List<String> deniedPermission = new ArrayList<>(permissions.size());

        // If it is Android 6.0 or lower, it will be granted by default
        if (!isAndroid6()) {
            return deniedPermission;
        }

        for (String permission : permissions) {
            if (!isGrantedPermission(context, permission)) {
                deniedPermission.add(permission);
            }
        }
        return deniedPermission;
    }

    /**
     * Determine whether a permission is granted
     */
    static boolean isGrantedPermission(Context context, String permission) {
        // If it is Android 6.0 or lower, it will be granted by default
        if (!isAndroid6()) {
            return true;
        }

        // Check storage permissions
        if (Permission.MANAGE_EXTERNAL_STORAGE.equals(permission)) {
            return isGrantedStoragePermission(context);
        }

        // Check installation permissions
        if (Permission.REQUEST_INSTALL_PACKAGES.equals(permission)) {
            return isGrantedInstallPermission(context);
        }

        // Check the permission of the floating window
        if (Permission.SYSTEM_ALERT_WINDOW.equals(permission)) {
            return isGrantedWindowPermission(context);
        }

        // Check the notification bar permissions
        if (Permission.NOTIFICATION_SERVICE.equals(permission)) {
            return isGrantedNotifyPermission(context);
        }

        // Check system permissions
        if (Permission.WRITE_SETTINGS.equals(permission)) {
            return isGrantedSettingPermission(context);
        }

        // Check the three new permissions of 10.0
        if (!isAndroid10()) {
            if (Permission.ACCESS_BACKGROUND_LOCATION.equals(permission) ||
                    Permission.ACCESS_MEDIA_LOCATION.equals(permission)) {
                return true;
            }

            if (Permission.ACTIVITY_RECOGNITION.equals(permission)) {
                return context.checkSelfPermission(Permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED;
            }
        }

        // Detect a new permission in 9.0
        if (!isAndroid9()) {
            if (Permission.ACCEPT_HANDOVER.equals(permission)) {
                return true;
            }
        }

        // detect two new permissions in 8.0
        if (!isAndroid8()) {
            if (Permission.ANSWER_PHONE_CALLS.equals(permission)) {
                return true;
            }

            if (Permission.READ_PHONE_NUMBERS.equals(permission)) {
                return context.checkSelfPermission(Permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
            }
        }

        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Get the status of a certain permission
     *
     * @return is authorized to return {@link PackageManager#PERMISSION_GRANTED}
     * Unauthorized return {@link PackageManager#PERMISSION_DENIED}
     */
    static int getPermissionStatus(Context context, String permission) {
        return PermissionUtils.isGrantedPermission(context, permission) ?
                PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
    }

    /**
     * Check whether a certain permission is permanently denied in the permission group
     *
     * @param activity Activity object
     * @param permissions requested permissions
     */
    static boolean isPermissionPermanentDenied(Activity activity, List<String> permissions) {
        for (String permission : permissions) {
            if (isPermissionPermanentDenied(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether a permission is permanently denied
     *
     * @param activity Activity object
     * @param permission requested permission
     */
    static boolean isPermissionPermanentDenied(Activity activity, String permission) {
        if (!isAndroid6()) {
            return false;
        }

        // Special permissions are not counted. The application method is different from the dangerous permission application method. Because there is no option for permanent rejection, false is returned here.
        if (isSpecialPermission(permission)) {
            return false;
        }

        // Check the three new permissions of 10.0
        if (!isAndroid10()) {
            if (Permission.ACCESS_BACKGROUND_LOCATION.equals(permission) ||
                    Permission.ACCESS_MEDIA_LOCATION.equals(permission)) {
                return false;
            }

            if (Permission.ACTIVITY_RECOGNITION.equals(permission) ) {
                return activity.checkSelfPermission(Permission.BODY_SENSORS) == PackageManager.PERMISSION_DENIED &&
                        !activity.shouldShowRequestPermissionRationale(permission);
            }
        }

        // Detect a new permission in 9.0
        if (!isAndroid9()) {
            if (Permission.ACCEPT_HANDOVER.equals(permission)) {
                return false;
            }
        }

        // detect two new permissions in 8.0
        if (!isAndroid8()) {
            if (Permission.ANSWER_PHONE_CALLS.equals(permission)) {
                return true;
            }

            if (Permission.READ_PHONE_NUMBERS.equals(permission)) {
                return activity.checkSelfPermission(Permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED &&
                        !activity.shouldShowRequestPermissionRationale(permission);
            }
        }

        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED &&
                !activity.shouldShowRequestPermissionRationale(permission);
    }

    /**
     * Get permission not granted
     *
     * @param permissions requires the requested permission group
     * @param grantResults allows result groups
     */
    static List<String> getDeniedPermissions(String[] permissions, int[] grantResults) {
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i <grantResults.length; i++) {
            // Add permissions that have not been granted to the collection
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permissions[i]);
            }
        }
        return deniedPermissions;
    }

    /**
     * Get granted permissions
     *
     * @param permissions requires the requested permission group
     * @param grantResults allows result groups
     */
    static List<String> getGrantedPermissions(String[] permissions, int[] grantResults) {
        List<String> grantedPermissions = new ArrayList<>();
        for (int i = 0; i <grantResults.length; i++) {
            // Add the granted permissions to the collection
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permissions[i]);
            }
        }
        return grantedPermissions;
    }

    /**
     * Convert the array to ArrayList
     *
     * Here to explain why you don't use Arrays.asList
     * The first is that the returned type is not java.util.ArrayList but java.util.Arrays.ArrayList
     * The second is that the returned ArrayList object is read-only, that is, no elements can be added, otherwise an exception will be thrown
     */
    @SuppressWarnings("all")
    static <T> ArrayList<T> asArrayList(T... array) {
        if (array == null || array.length == 0) {
            return null;
        }
        ArrayList<T> list = new ArrayList<>(array.length);
        for (T t : array) {
            list.add(t);
        }
        return list;
    }

    /**
     * Get random RequestCode
     */
    static int getRandomRequestCode() {
        // The new version of the Support library limit request code must be less than 65536
        // The limit request code of the old version of the Support library must be less than 256
        return new Random().nextInt((int) Math.pow(2, 8));
    }

    /**
     * Find the Activity object in the context
     */
    static FragmentActivity findFragmentActivity(Context context) {
        do {
            if (context instanceof FragmentActivity) {
                return (FragmentActivity) context;
            } else if (context instanceof ContextWrapper){
                context = ((ContextWrapper) context).getBaseContext();
            } else {
                return null;
            }
        } while (context != null);
        return null;
    }

    /**
     * Get the cookie of the current application Apk in AssetManager
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint("PrivateApi")
    static int findApkPathCookie(Context context) {
        AssetManager assets = context.getAssets();
        String path = context.getApplicationInfo().sourceDir;
        int cookie = 0;
        try {
            try {
                // Why not judge directly by reflecting the AssetManager.findCookieForPath method? Because this API belongs to the reflection blacklist, reflection cannot be executed
                Method method = assets.getClass().getDeclaredMethod("addOverlayPath", String.class);
                cookie = (int) method.invoke(assets, path);
            } catch (Exception e) {
                // NoSuchMethodException
                // IllegalAccessException
                // InvocationTargetException
                e.printStackTrace();
                Method method = assets.getClass().getDeclaredMethod("getApkPaths");
                String[] apkPaths = (String[]) method.invoke(assets);
                for (int i = 0; i <apkPaths.length; i++) {
                    if (apkPaths[i].equals(path)) {
                        cookie = i + 1;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // NoSuchMethodException
            // IllegalAccessException
            // InvocationTargetException
            e.printStackTrace();
        }
        return cookie;
    }
}