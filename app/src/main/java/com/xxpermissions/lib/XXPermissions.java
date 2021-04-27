package com.xxpermissions.lib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import java.util.ArrayList;
import java.util.List;

public final class XXPermissions {

    /** Jump request code for permission setting page */
    public static final int REQUEST_CODE = 1024 + 1;

    /** Permission request interceptor */
    private static IPermissionInterceptor sPermissionInterceptor;

    /** Debug mode */
    private static Boolean sDebugMode;

    /** Partition storage */
    private static boolean sScopedStorage;

    /**
     * Set the requested object
     *
     * @param context current Activity, which can be passed to the Activity at the top of the stack
     */
    public static XXPermissions with(Context context) {
        return new XXPermissions(context);
    }

    public static XXPermissions with(Fragment fragment) {
        return with(fragment.getActivity());
    }

    /**
     * Is it in debug mode
     */
    public static void setDebugMode(boolean debug) {
        sDebugMode = debug;
    }

    private static boolean isDebugMode(Context context) {
        if (sDebugMode == null) {
            sDebugMode = (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        }
        return sDebugMode;
    }

    /**
     * Whether it has been adapted to the Android 10 partition storage feature
     */
    public static void setScopedStorage(boolean scopedStorage) {
        sScopedStorage = scopedStorage;
    }

    private static boolean isScopedStorage() {
        return sScopedStorage;
    }

    /**
     * Set up permission request interceptor
     */
    public static void setInterceptor(IPermissionInterceptor interceptor) {
        sPermissionInterceptor = interceptor;
    }

    /**
     * Obtain permission request interceptor
     */
    static IPermissionInterceptor getInterceptor() {
        if (sPermissionInterceptor == null) {
            sPermissionInterceptor = new IPermissionInterceptor() {};
        }
        return sPermissionInterceptor;
    }

    /** Context object */
    private final Context mContext;

    /** Permission list */
    private List<String> mPermissions;

    /**
     * Privatization constructor
     */
    private XXPermissions(Context context) {
        mContext = context;
    }

    /**
     * Add permissions
     */
    public XXPermissions permission(String permission) {
        if (mPermissions == null) {
            mPermissions = new ArrayList<>(1);
        }
        mPermissions.add(permission);
        return this;
    }

    /**
     * Add permission group
     */
    public XXPermissions permission(String[] permissions) {
        return permission(PermissionUtils.asArrayList(permissions));
    }

    public XXPermissions permission(List<String> permissions) {
        if (mPermissions == null) {
            mPermissions = permissions;
        } else {
            mPermissions.addAll(permissions);
        }
        return this;
    }

    /**
     * Request permission
     */
    public void request(OnPermissionCallback callback) {
        if (mContext == null) {
            return;
        }

        // Is it currently in debug mode?
        boolean debugMode = isDebugMode(mContext);

        // Check whether the current Activity state is normal, if not, do not request permission
        FragmentActivity fragmentActivity = PermissionUtils.findFragmentActivity(mContext);
        if (!PermissionChecker.checkActivityStatus(fragmentActivity, debugMode)) {
            return;
        }

        // You must pass in normal permissions or permission groups to apply for permissions
        if (!PermissionChecker.checkPermissionArgument(mPermissions, debugMode)) {
            return;
        }

        if (debugMode) {
            // Check whether the requested storage permissions meet the specifications
            PermissionChecker.checkStoragePermission(mContext, mPermissions, isScopedStorage());
            // Check whether the applied positioning permission meets the specifications
            PermissionChecker.checkLocationPermission(mPermissions);
            // Check whether the requested permissions and targetSdk version can match
            PermissionChecker.checkTargetSdkVersion(mContext, mPermissions);
        }

        // Optimize the list of permissions requested
        PermissionChecker.optimizeDeprecatedPermission(mPermissions);

        if (debugMode) {
            // Check whether the permission is registered in the manifest file
            PermissionChecker.checkPermissionManifest(mContext, mPermissions);
        }

        if (PermissionUtils.isGrantedPermissions(mContext, mPermissions)) {
            // Prove that all these permissions have been granted, and the direct callback is successful
            if (callback != null) {
                callback.onGranted(mPermissions, true);
            }
            return;
        }

        // Application has not been granted permission
        getInterceptor().requestPermissions(fragmentActivity, callback, mPermissions);
    }

    /**
     * Determine whether one or more permissions are all granted
     */
    public static boolean isGranted(Context context, String permission) {
        return PermissionUtils.isGrantedPermission(context, permission);
    }

    public static boolean isGranted(Context context, String[] permissions) {
        return isGranted(context, PermissionUtils.asArrayList(permissions));
    }

    public static boolean isGranted(Context context, List<String> permissions) {
        return PermissionUtils.isGrantedPermissions(context, permissions);
    }

    /**
     * Get permission not granted
     */
    public static List<String> getDenied(Context context, String[] permissions) {
        return getDenied(context, PermissionUtils.asArrayList(permissions));
    }

    public static List<String> getDenied(Context context, List<String> permissions) {
        return PermissionUtils.getDeniedPermissions(context, permissions);
    }


    /**
     * Determine whether a permission is a special permission
     */
    public static boolean isSpecial(String permission) {
        return PermissionUtils.isSpecialPermission(permission);
    }

    /**
     * Determine whether one or more permissions are permanently denied (note that it cannot be called before the permission is requested, it should be called in the {@link OnPermissionCallback#onDenied(List, boolean)} method)
     */
    public static boolean isPermanentDenied(Activity activity, String permission) {
        return PermissionUtils.isPermissionPermanentDenied(activity, permission);
    }

    public static boolean isPermanentDenied(Activity activity, String[] permissions) {
        return isPermanentDenied(activity, PermissionUtils.asArrayList(permissions));
    }

    public static boolean isPermanentDenied(Activity activity, List<String> permissions) {
        return PermissionUtils.isPermissionPermanentDenied(activity, permissions);
    }

    public static void startPermissionActivity(Context context) {
        startPermissionActivity(context, (List<String>) null);
    }

    public static void startPermissionActivity(Context context, String permission) {
        startPermissionActivity(context, PermissionUtils.asArrayList(permission));
    }

    public static void startPermissionActivity(Context context, String[] permissions) {
        startPermissionActivity(context, PermissionUtils.asArrayList(permissions));
    }

    /**
     * Jump to the application permission setting page
     *
     * @param permissions permission group not granted or denied
     */
    public static void startPermissionActivity(Context context, List<String> permissions) {
        Activity activity = PermissionUtils.findFragmentActivity(context);
        if (activity != null) {
            startPermissionActivity(activity, permissions);
            return;
        }
        Intent intent = PermissionSettingPage.getSmartPermissionIntent(context, permissions);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startPermissionActivity(Activity activity, String permission) {
        startPermissionActivity(activity, PermissionUtils.asArrayList(permission));
    }

    public static void startPermissionActivity(Activity activity, String[] permissions) {
        startPermissionActivity(activity, PermissionUtils.asArrayList(permissions));
    }

    public static void startPermissionActivity(Activity activity, List<String> permissions) {
        activity.startActivityForResult(PermissionSettingPage.getSmartPermissionIntent(activity, permissions), REQUEST_CODE);
    }

    public static void startPermissionActivity(Fragment fragment) {
        startPermissionActivity(fragment, (List<String>) null);
    }

    public static void startPermissionActivity(Fragment fragment, String permissions) {
        startPermissionActivity(fragment, PermissionUtils.asArrayList(permissions));
    }

    public static void startPermissionActivity(Fragment fragment, String[] permissions) {
        startPermissionActivity(fragment, PermissionUtils.asArrayList(permissions));
    }

    /**
     * Jump to the application permission setting page
     *
     * @param permissions permission group not granted or denied
     */
    public static void startPermissionActivity(Fragment fragment, List<String> permissions) {
        FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            return;
        }
        fragment.startActivityForResult(PermissionSettingPage.getSmartPermissionIntent(activity, permissions), REQUEST_CODE);
    }
}