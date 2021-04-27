package com.xxpermissions.lib;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import java.util.List;

final class PermissionSettingPage {

    /**
     * Automatically select the most suitable permission setting page according to the incoming permissions
     */
    static Intent getSmartPermissionIntent(Context context, List<String> deniedPermissions) {
        // If the failed permissions do not contain special permissions
        if (deniedPermissions == null || deniedPermissions.isEmpty() ||
                !PermissionUtils.containsSpecialPermission(deniedPermissions)) {
            return getApplicationDetailsIntent(context);
        }

        if (PermissionUtils.isAndroid11() && deniedPermissions.size() == 3 &&
                (deniedPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE) &&
                        deniedPermissions.contains(Permission.READ_EXTERNAL_STORAGE) &&
                        deniedPermissions.contains(Permission.WRITE_EXTERNAL_STORAGE))) {
            return getStoragePermissionIntent(context);
        }

        // If only one permission is currently denied
        if (deniedPermissions.size() == 1) {

            String permission = deniedPermissions.get(0);
            if (Permission.MANAGE_EXTERNAL_STORAGE.equals(permission)) {
                return getStoragePermissionIntent(context);
            }

            if (Permission.REQUEST_INSTALL_PACKAGES.equals(permission)) {
                return getInstallPermissionIntent(context);
            }

            if (Permission.SYSTEM_ALERT_WINDOW.equals(permission)) {
                return getWindowPermissionIntent(context);
            }

            if (Permission.NOTIFICATION_SERVICE.equals(permission)) {
                return getNotifyPermissionIntent(context);
            }

            if (Permission.WRITE_SETTINGS.equals(permission)) {
                return getSettingPermissionIntent(context);
            }
        }

        return getApplicationDetailsIntent(context);
    }

    /**
     * Get the intent of the application details interface
     */
    static Intent getApplicationDetailsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(getPackageNameUri(context));
        return intent;
    }

    /**
     * Get installation permission setting interface intention
     */
    static Intent getInstallPermissionIntent(Context context) {
        Intent intent = null;
        if (PermissionUtils.isAndroid8()) {
            intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(getPackageNameUri(context));
        }
        if (intent == null || !areActivityIntent(context, intent)) {
            intent = getApplicationDetailsIntent(context);
        }
        return intent;
    }

    /**
     * Get the intent of the floating window permission setting interface
     */
    static Intent getWindowPermissionIntent(Context context) {
        Intent intent = null;
        if (PermissionUtils.isAndroid6()) {
            intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            // Adding the package name to jump on Android 11 is also ineffective, the official document link:
            // https://developer.android.google.com/reference/android/provider/Settings#ACTION_MANAGE_OVERLAY_PERMISSION
            intent.setData(getPackageNameUri(context));
        }

        if (intent == null || !areActivityIntent(context, intent)) {
            intent = getApplicationDetailsIntent(context);
        }
        return intent;
    }

    /**
     * Get the intent of the notification bar permission setting interface
     */
    static Intent getNotifyPermissionIntent(Context context) {
        Intent intent = null;
        if (PermissionUtils.isAndroid8()) {
            intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            //intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);
        }
        if (intent == null || !areActivityIntent(context, intent)) {
            intent = getApplicationDetailsIntent(context);
        }
        return intent;
    }

    /**
     * Get the intention of the system settings permission interface
     */
    static Intent getSettingPermissionIntent(Context context) {
        Intent intent = null;
        if (PermissionUtils.isAndroid6()) {
            intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(getPackageNameUri(context));
        }
        if (intent == null || !areActivityIntent(context, intent)) {
            intent = getApplicationDetailsIntent(context);
        }
        return intent;
    }

    /**
     * Get the intent of the storage permission setting interface
     */
    static Intent getStoragePermissionIntent(Context context) {
        Intent intent = null;
        if (PermissionUtils.isAndroid11()) {
            intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(getPackageNameUri(context));
        }
        if (intent == null || !areActivityIntent(context, intent)) {
            intent = getApplicationDetailsIntent(context);
        }
        return intent;
    }

    /**
     * Determine whether the activity of this intention exists
     */
    private static boolean areActivityIntent(Context context, Intent intent) {
        return !context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty();
    }

    /**
     * Get the package name Uri object
     */
    private static Uri getPackageNameUri(Context context) {
        return Uri.parse("package:" + context.getPackageName());
    }
}