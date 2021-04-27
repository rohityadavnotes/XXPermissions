package com.xxpermissions.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PermissionFragment extends Fragment implements Runnable {

    /** Requested permission group */
    private static final String REQUEST_PERMISSIONS = "request_permissions";

    /** Request code (automatically generated)*/
    private static final String REQUEST_CODE = "request_code";

    /** Permission request code storage collection */
    private final static SparseBooleanArray REQUEST_CODE_ARRAY = new SparseBooleanArray();

    /**
     * Open permission application
     */
    public static void beginRequest(FragmentActivity activity, ArrayList<String> permissions, OnPermissionCallback callback) {
        PermissionFragment fragment = new PermissionFragment();
        Bundle bundle = new Bundle();
        int requestCode;
        // The request code is randomly generated to avoid random generation of the previous request code, and a circular judgment must be performed
        do {
            requestCode = PermissionUtils.getRandomRequestCode();
        } while (REQUEST_CODE_ARRAY.get(requestCode));
        // mark this request code has been taken
        REQUEST_CODE_ARRAY.put(requestCode, true);
        bundle.putInt(REQUEST_CODE, requestCode);
        bundle.putStringArrayList(REQUEST_PERMISSIONS, permissions);
        fragment.setArguments(bundle);
        // Set a reserved instance, it will not be recreated due to screen orientation or configuration changes
        fragment.setRetainInstance(true);
        // Set permission callback listener
        fragment.setCallBack(callback);
        // Bind to Activity
        fragment.attachActivity(activity);
    }

    /** Whether to apply for special permissions */
    private boolean mSpecialRequest;

    /** Whether to apply for dangerous permissions */
    private boolean mDangerousRequest;

    /** Permission callback object */
    private OnPermissionCallback mCallBack;

    /** Activity screen orientation */
    private int mScreenOrientation;

    /**
     * Binding Activity
     */
    public void attachActivity(FragmentActivity activity) {
        activity.getSupportFragmentManager().beginTransaction().add(this, this.toString()).commitAllowingStateLoss();
    }

    /**
     * Unbind Activity
     */
    public void detachActivity(FragmentActivity activity) {
        activity.getSupportFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
    }

    /**
     * Set permission to monitor callback monitor
     */
    public void setCallBack(OnPermissionCallback callback) {
        mCallBack = callback;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        // If the screen orientation is not currently locked, get the current screen orientation and lock it
        mScreenOrientation = activity.getRequestedOrientation();
        if (mScreenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            return;
        }
        int activityOrientation = activity.getResources().getConfiguration().orientation;
        try {
            // Compatibility issue: The direction of the Activity can be fixed on Android 8.0 phones, but the Activity cannot be transparent, otherwise an exception will be thrown
            // Replay the scene: just set the <item name="android:windowIsTranslucent">true</item> attribute to the Activity theme
            if (activityOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else if (activityOrientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } catch (IllegalStateException e) {
            // java.lang.IllegalStateException: Only fullscreen activities can request orientation
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        FragmentActivity activity = getActivity();
        if (activity == null || mScreenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            return;
        }
        // Why is there no need to try catch as above? Because the activity direction is unfixed here, the crash may only be triggered when the horizontal or vertical screen is set
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Dereference the listener to avoid memory leaks
        mCallBack = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // If you add a Fragment while the Activity is invisible and apply for permission, the authorization dialog will not be displayed
        // So you must apply for permission in the onResume of Fragment, so that you can ensure that the application will only apply for permission when it returns to the foreground
        if (mSpecialRequest) {
            return;
        }

        mSpecialRequest = true;
        requestSpecialPermission();
    }

    /**
     * Apply for special permissions
     */
    public void requestSpecialPermission() {
        Bundle arguments = getArguments();
        FragmentActivity activity = getActivity();
        if (arguments == null || activity == null) {
            return;
        }

        List<String> permissions = arguments.getStringArrayList(REQUEST_PERMISSIONS);

        // Do you need to apply for special permissions
        boolean requestSpecialPermission = false;

        // Determine whether the current contains special permissions
        if (PermissionUtils.containsSpecialPermission(permissions)) {

            if (permissions.contains(Permission.MANAGE_EXTERNAL_STORAGE) && !PermissionUtils.isGrantedStoragePermission(activity)) {
                // The current version must be Android 11 and above, because hasStoragePermission is judged by the old permission on the old version, so here you need to judge the version more than once
                if (PermissionUtils.isAndroid11()) {
                    // Jump to the storage permission setting interface
                    startActivityForResult(PermissionSettingPage.getStoragePermissionIntent(activity), getArguments().getInt(REQUEST_CODE));
                    requestSpecialPermission = true;
                }
            }
            if (permissions.contains(Permission.REQUEST_INSTALL_PACKAGES) && !PermissionUtils.isGrantedInstallPermission(activity)) {
                // Jump to the installation permission setting interface
                startActivityForResult(PermissionSettingPage.getInstallPermissionIntent(activity), getArguments().getInt(REQUEST_CODE));
                requestSpecialPermission = true;
            }

            if (permissions.contains(Permission.SYSTEM_ALERT_WINDOW) && !PermissionUtils.isGrantedWindowPermission(activity)) {
                // Jump to the floating window setting page
                startActivityForResult(PermissionSettingPage.getWindowPermissionIntent(activity), getArguments().getInt(REQUEST_CODE));
                requestSpecialPermission = true;
            }

            if (permissions.contains(Permission.NOTIFICATION_SERVICE) && !PermissionUtils.isGrantedNotifyPermission(activity)) {
                // Jump to the notification bar permission setting page
                startActivityForResult(PermissionSettingPage.getNotifyPermissionIntent(activity), getArguments().getInt(REQUEST_CODE));
                requestSpecialPermission = true;
            }

            if (permissions.contains(Permission.WRITE_SETTINGS) && !PermissionUtils.isGrantedSettingPermission(activity)) {
                // Jump to the system settings permission setting page
                startActivityForResult(PermissionSettingPage.getSettingPermissionIntent(activity), getArguments().getInt(REQUEST_CODE));
                requestSpecialPermission = true;
            }
        }

        // Currently there must be no jump to the floating window or installation permission interface
        if (!requestSpecialPermission) {
            requestDangerousPermission();
        }
    }

    /**
     * Apply for dangerous permissions
     */
    public void requestDangerousPermission() {
        FragmentActivity activity = getActivity();
        Bundle arguments = getArguments();
        if (activity == null || arguments == null) {
            return;
        }

        final ArrayList<String> allPermissions = arguments.getStringArrayList(REQUEST_PERMISSIONS);
        if (allPermissions == null || allPermissions.size() == 0) {
            return;
        }

        ArrayList<String> locationPermission = null;
        // Android 10 positioning strategy has changed. The prerequisite to apply for background positioning permission is to have the foreground positioning permission (either precise or fuzzy permission is granted)
        if (PermissionUtils.isAndroid10() && allPermissions.contains(Permission.ACCESS_BACKGROUND_LOCATION)) {
            locationPermission = new ArrayList<>();
            if (allPermissions.contains(Permission.ACCESS_COARSE_LOCATION) &&
                    !PermissionUtils.isGrantedPermission(activity, Permission.ACCESS_COARSE_LOCATION)) {
                locationPermission.add(Permission.ACCESS_COARSE_LOCATION);
            }

            if (allPermissions.contains(Permission.ACCESS_FINE_LOCATION) &&
                    !PermissionUtils.isGrantedPermission(activity, Permission.ACCESS_FINE_LOCATION)) {
                locationPermission.add(Permission.ACCESS_FINE_LOCATION);
            }
        }

        // If you don't need to apply for the front desk location permission, apply for the dangerous permission directly
        if (locationPermission == null || locationPermission.isEmpty()) {
            requestPermissions(allPermissions.toArray(new String[allPermissions.size()-1]), getArguments().getInt(REQUEST_CODE));
            return;
        }

        // On Android 10 models, you need to apply for the foreground location permission first, and then apply for the background location permission
        PermissionFragment.beginRequest(activity, locationPermission, new OnPermissionCallback() {

            @Override
            public void onGranted(List<String> permissions, boolean all) {
                if (!all || !isAdded()) {
                    return;
                }
                requestPermissions(allPermissions.toArray(new String[allPermissions.size()-1]), arguments.getInt(REQUEST_CODE));
            }

            @Override
            public void onDenied(List<String> permissions, boolean never) {
                if (!isAdded()) {
                    return;
                }
                // If the requested permission only contains the permission related to positioning, then the callback will fail directly
                if (permissions.size() == allPermissions.size()-1) {
                    int[] grantResults = new int[allPermissions.size()];
                    Arrays.fill(grantResults, PackageManager.PERMISSION_DENIED);
                    onRequestPermissionsResult(arguments.getInt(REQUEST_CODE), allPermissions.toArray(new String[0]), grantResults);
                    return;
                }
                // If there are other types of permission groups, continue to apply
                requestPermissions(allPermissions.toArray(new String[allPermissions.size()-1]), arguments.getInt(REQUEST_CODE));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Bundle arguments = getArguments();
        FragmentActivity activity = getActivity();
        if (activity == null || arguments == null || mCallBack == null || requestCode != arguments.getInt(REQUEST_CODE)) {
            return;
        }

        OnPermissionCallback callBack = mCallBack;
        mCallBack = null;

        for (int i = 0; i <permissions.length; i++) {

            String permission = permissions[i];

            if (PermissionUtils.isSpecialPermission(permission)) {
                // If this permission is a special permission, then re-check the permission
                grantResults[i] = PermissionUtils.getPermissionStatus(activity, permission);
                continue;
            }

            // Recheck Android 11 background location permissions
            if (PermissionUtils.isAndroid11() &&
                    Permission.ACCESS_BACKGROUND_LOCATION.equals(permission)) {
                // This permission is the background location permission and the current mobile phone version is Android 11 and above, then it needs to be tested again
                // Because as long as you apply for this background location permission, the grantResults array always returns -1 (rejected) for the result of this permission application
                grantResults[i] = PermissionUtils.getPermissionStatus(activity, permission);
                continue;
            }

            // Recheck the three new permissions of Android 10.0
            if (!PermissionUtils.isAndroid10() &&
                    (Permission.ACCESS_BACKGROUND_LOCATION.equals(permission) ||
                            Permission.ACTIVITY_RECOGNITION.equals(permission) ||
                            Permission.ACCESS_MEDIA_LOCATION.equals(permission))) {
                // If the current version does not meet the minimum requirements, then re-check the permissions
                grantResults[i] = PermissionUtils.getPermissionStatus(activity, permission);
                continue;
            }

            // Recheck a new permission of Android 9.0
            if (!PermissionUtils.isAndroid9() &&
                    Permission.ACCEPT_HANDOVER.equals(permission)) {
                // If the current version does not meet the minimum requirements, then re-check the permissions
                grantResults[i] = PermissionUtils.getPermissionStatus(activity, permission);
                continue;
            }

            // Recheck the two new permissions of Android 8.0
            if (!PermissionUtils.isAndroid8() &&
                    (Permission.ANSWER_PHONE_CALLS.equals(permission) ||
                            Permission.READ_PHONE_NUMBERS.equals(permission))) {
                // If the current version does not meet the minimum requirements, then re-check the permissions
                grantResults[i] = PermissionUtils.getPermissionStatus(activity, permission);
            }
        }

        // Release the occupation of this request code
        REQUEST_CODE_ARRAY.delete(requestCode);
        // Remove Fragment from Activity
        detachActivity(activity);

        // Get granted permissions
        List<String> grantedPermission = PermissionUtils.getGrantedPermissions(permissions, grantResults);

        // If the requested permission set size is as large as the requested array, it proves that all permissions have been granted
        if (grantedPermission.size() == permissions.length) {
            // All permissions applied for on behalf of have been granted
            XXPermissions.getInterceptor().grantedPermissions(activity, callBack, grantedPermission, true);
            return;
        }

        // Get denied permission
        List<String> deniedPermission = PermissionUtils.getDeniedPermissions(permissions, grantResults);

        // Some of the requested permissions do not agree to be granted. If a permission is permanently denied, it will return true to the developer and let the developer guide the user to set the interface to open the permission
        XXPermissions.getInterceptor().deniedPermissions(activity, callBack, deniedPermission, PermissionUtils.isPermissionPermanentDenied(activity, deniedPermission));

        // It proves that some permissions have been successfully granted, and the success interface is called back
        if (!grantedPermission.isEmpty()) {
            XXPermissions.getInterceptor().grantedPermissions(activity, callBack, grantedPermission, false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        FragmentActivity activity = getActivity();
        Bundle arguments = getArguments();
        if (activity == null || arguments == null || requestCode != arguments.getInt(REQUEST_CODE) || mDangerousRequest) {
            return;
        }

        mDangerousRequest = true;
        // Need to delay execution, otherwise some Huawei models are authorized but cannot get permission
        activity.getWindow().getDecorView().postDelayed(this, 200);
    }

    @Override
    public void run() {
        // If the user leaves for too long, it will cause the Activity to be recycled
        // So here to determine whether the current Fragment has been added to the Activity
        // You can reproduce this bug by turning on non-reserved activities in developer mode
        if (!isAdded()) {
            return;
        }
        // Request other dangerous permissions
        requestDangerousPermission();
    }
}