package com.xxpermissions.lib;

import android.app.Activity;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Build;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

final class PermissionChecker {

    /**
     * Check whether the status of the Activity is normal
     *
     * @param debugMode is the debug mode
     * @return is the check passed
     */
    static boolean checkActivityStatus(Activity activity, boolean debugMode) {
        // Check whether the current Activity state is normal, if not, do not request permission
        if (activity == null) {
            if (debugMode) {
                // This Activity object must be a subclass of FragmentActivity, please directly inherit AppCompatActivity
                throw new IllegalArgumentException("The Activity must be a subclass of FragmentActivity, Please directly inherit AppCompatActivity");
            }
            return false;
        }

        if (activity.isFinishing()) {
            if (debugMode) {
                // This Activity object cannot currently be in the closed state. This situation often occurs when you apply for permission after executing an asynchronous request. Please judge whether the Activity status is normal at the outer layer before entering the permission application
                throw new IllegalStateException("The Activity has been finishing, Please manually determine the status of the Activity");
            }
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            if (debugMode) {
                // This Activity object cannot currently be in the destroyed state. This situation often occurs when you apply for permissions after performing asynchronous requests. Please judge whether the Activity status is normal at the outer layer before entering the permission application
                throw new IllegalStateException("The Activity has been destroyed, Please manually determine the status of the Activity");
            }
            return false;
        }

        return true;
    }

    /**
     * Check whether the incoming permissions meet the requirements
     *
     * @param requestPermissions requested permission group
     * @param debugMode is the debug mode
     * @return is the check passed
     */
    static boolean checkPermissionArgument(List<String> requestPermissions, boolean debugMode) {
        if (requestPermissions == null || requestPermissions.isEmpty()) {
            if (debugMode) {
                // Don't pass the permission, just want to apply for permission?
                throw new IllegalArgumentException("The requested permission cannot be empty");
            }
            return false;
        }
        if (debugMode) {
            List<String> allPermissions = new ArrayList<>();
            Field[] fields = Permission.class.getDeclaredFields();
            // After turning on the code obfuscation, reflecting the fields in the Permission class will get an empty field array
            // This is because the constant will be directly referenced in the code after compilation, so the Permission constant field will be removed when confused
            if (fields.length == 0) {
                return true;
            }
            for (Field field: fields) {
                if (!String.class.equals(field.getType())) {
                    continue;
                }
                try {
                    allPermissions.add((String) field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            for (String permission: requestPermissions) {
                if (!allPermissions.contains(permission)) {
                    // Please do not apply for permissions other than dangerous permissions and special permissions
                    throw new IllegalArgumentException("The "+ permission +" is not a dangerous permission or special permission");
                }
            }
        }
        return true;
    }

    /**
     * Check storage permissions
     *
     * @param requestPermissions requested permission group
     * @param scopedStorage is adapted to partition storage
     */
    @SuppressWarnings({"ConstantConditions"})
    static void checkStoragePermission(Context context, List<String> requestPermissions, boolean scopedStorage) {
        // If the requested permissions do not include permissions related to external storage, then return directly
        if (!requestPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE) &&
                !requestPermissions.contains(Permission.READ_EXTERNAL_STORAGE) &&
                !requestPermissions.contains(Permission.WRITE_EXTERNAL_STORAGE)) {
            return;
        }

        int cookie = PermissionUtils.findApkPathCookie(context);
        if (cookie == 0) {
            return;
        }

        try {
            XmlResourceParser parser = context.getAssets().openXmlResourceParser(cookie, "AndroidManifest.xml");
            // Whether to read to the end of the file
            while (parser.getEventType() != XmlResourceParser.END_DOCUMENT) {
                // Whether the current node is the label head
                if (parser.getEventType() == XmlResourceParser.START_TAG) {
                    // Get the label name
                    String nodeName = parser.getName();
                    if ("application".equals(nodeName)) {
                        String namespace = "http://schemas.android.com/apk/res/android";
                        int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;

                        boolean requestLegacyExternalStorage = parser.getAttributeBooleanValue(namespace, "requestLegacyExternalStorage", false);
                        // If you have already adapted to Android 10
                        if (targetSdkVersion >= Build.VERSION_CODES.Q && !requestLegacyExternalStorage &&
                                (requestPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE) || !scopedStorage)) {
                            // Please register the android:requestLegacyExternalStorage="true" attribute in the Application node of the manifest file
                            // Otherwise, even if you apply for permission, you will not be able to read and write files on external storage normally on Android 10 devices
                            // If your project is fully adapted to partitioned storage, please call XXPermissions.setScopedStorage(true) to skip this check
                            throw new IllegalStateException("Please register the android:requestLegacyExternalStorage=\"true\" attribute in the manifest file");
                        }

                        // If you have already adapted to Android 11
                        if (targetSdkVersion >= Build.VERSION_CODES.R &&
                                !requestPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE) && !scopedStorage) {
                            // 1. Adapt to the characteristics of partitioned storage, and call XXPermissions.setScopedStorage(true) when Application is initialized
                            // 2. If you do not want to adapt to partition storage, you need to use Permission.MANAGE_EXTERNAL_STORAGE to apply for permission
                            // The above two methods need to choose one of the two, otherwise the files on the external storage cannot be read and written normally on the Android 11 device
                            // If you don’t know how to choose, you can read the document: https://github.com/getActivity/XXPermissions/blob/master/HelpDoc
                            throw new IllegalArgumentException("Please adapt the scoped storage, or use the MANAGE_EXTERNAL_STORAGE permission");
                        }
                        break;
                    }
                }
                // read the next label
                parser.next();
            }
            parser.close();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check positioning permissions
     *
     * @param requestPermissions requested permission group
     */
    static void checkLocationPermission(List<String> requestPermissions) {
        // Determine whether the background location permission is included
        if (!requestPermissions.contains(Permission.ACCESS_BACKGROUND_LOCATION)) {
            return;
        }

        for (String permission: requestPermissions) {
            if (Permission.ACCESS_FINE_LOCATION.equals(permission)
                    || Permission.ACCESS_COARSE_LOCATION.equals(permission)
                    || Permission.ACCESS_BACKGROUND_LOCATION.equals(permission)) {
                continue;
            }

            // Because the background location permission is included, please do not apply for permission that has nothing to do with the location, because on Android 11, the background location permission cannot be applied for with other non-location permissions
            // Otherwise, only the background positioning permission will be applied, and other permissions will be rejected, because on Android 11, the background positioning permission is to jump to Activity, not to pop up Dialog
            // If your project does not require background positioning, please do not apply for Permission.ACCESS_BACKGROUND_LOCATION together
            throw new IllegalArgumentException("Because it includes background location permissions, do not apply for permissions unrelated to location");
        }
    }

    /**
     * Check whether the targetSdkVersion meets the requirements
     *
     * @param requestPermissions requested permission group
     */
    static void checkTargetSdkVersion(Context context, List<String> requestPermissions) {
        // minimum targetSdk version requirements
        int targetSdkMinVersion;
        if (requestPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE)) {
            // You must set targetSdkVersion >= 30 to detect permissions normally, otherwise please use Permission.Group.STORAGE to apply for storage permissions
            targetSdkMinVersion = Build.VERSION_CODES.R;
        } else if (requestPermissions.contains(Permission.ACCEPT_HANDOVER)) {
            targetSdkMinVersion = Build.VERSION_CODES.P;
        } else if (requestPermissions.contains(Permission.ACCESS_BACKGROUND_LOCATION) ||
                requestPermissions.contains(Permission.ACTIVITY_RECOGNITION) ||
                requestPermissions.contains(Permission.ACCESS_MEDIA_LOCATION)) {
            targetSdkMinVersion = Build.VERSION_CODES.Q;
        } else if (requestPermissions.contains(Permission.REQUEST_INSTALL_PACKAGES) ||
                requestPermissions.contains(Permission.ANSWER_PHONE_CALLS) ||
                requestPermissions.contains(Permission.READ_PHONE_NUMBERS)) {
            targetSdkMinVersion = Build.VERSION_CODES.O;
        } else {
            targetSdkMinVersion = Build.VERSION_CODES.M;
        }

        // The correct targetSdkVersion must be set to detect permissions normally
        if (context.getApplicationInfo().targetSdkVersion <targetSdkMinVersion) {
            throw new RuntimeException("The targetSdkVersion SDK must be "+ targetSdkMinVersion +" or more");
        }
    }

    /**
     * Handling and optimizing outdated permissions
     *
     * @param requestPermissions requested permission group
     */
    static void optimizeDeprecatedPermission(List<String> requestPermissions) {
        // If this application includes Android 11 storage permissions
        if (requestPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE)) {

            if (requestPermissions.contains(Permission.READ_EXTERNAL_STORAGE) ||
                    requestPermissions.contains(Permission.WRITE_EXTERNAL_STORAGE)) {
                // Check whether there is an old version of storage permissions, if any, throw an exception directly, please do not apply for these two permissions dynamically by yourself
                throw new IllegalArgumentException("Please do not apply for these two permissions dynamically");
            }

            if (!PermissionUtils.isAndroid11()) {
                // Automatically add the storage permission of the old version, because the old version of the system does not support applying for the storage permission of the new version
                requestPermissions.add(Permission.READ_EXTERNAL_STORAGE);
                requestPermissions.add(Permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!PermissionUtils.isAndroid8() &&
                requestPermissions.contains(Permission.READ_PHONE_NUMBERS) &&
                !requestPermissions.contains(Permission.READ_PHONE_STATE)) {
            // Automatically add the old version of the read phone number permission, because the old version of the system does not support the new version of the permission
            requestPermissions.add(Permission.READ_PHONE_STATE);
        }

        if (!PermissionUtils.isAndroid10() &&
                requestPermissions.contains(Permission.ACTIVITY_RECOGNITION) &&
                !requestPermissions.contains(Permission.BODY_SENSORS)) {
            // Automatically add sensor permissions, because this permission is stripped from the sensor permissions into independent permissions starting from Android 10
            requestPermissions.add(Permission.BODY_SENSORS);
        }
    }

    /**
     * Check whether the permission is registered in the manifest file
     *
     * @param requestPermissions requested permission group
     */
    static void checkPermissionManifest(Context context, List<String> requestPermissions) {
        List<String> manifestPermissions = PermissionUtils.getManifestPermissions(context);
        if (manifestPermissions == null || manifestPermissions.isEmpty()) {
            throw new ManifestRegisterException();
        }

        int minSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            minSdkVersion = context.getApplicationInfo().minSdkVersion;
        } else {
            minSdkVersion = Build.VERSION_CODES.M;
        }

        for (String permission: requestPermissions) {

            if (minSdkVersion <Build.VERSION_CODES.R) {
                if (Permission.MANAGE_EXTERNAL_STORAGE.equals(permission)) {

                    if (!manifestPermissions.contains(Permission.READ_EXTERNAL_STORAGE)) {
                        // In order to ensure normal operation on the old version of the system, this permission must be registered in the manifest file
                        throw new ManifestRegisterException(Permission.READ_EXTERNAL_STORAGE);
                    }

                    if (!manifestPermissions.contains(Permission.WRITE_EXTERNAL_STORAGE)) {
                        // In order to ensure normal operation on the old version of the system, this permission must be registered in the manifest file
                        throw new ManifestRegisterException(Permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
            }

            if (minSdkVersion <Build.VERSION_CODES.Q) {
                if (Permission.ACTIVITY_RECOGNITION.equals(permission) &&
                        !manifestPermissions.contains(Permission.BODY_SENSORS)) {
                    // In order to ensure normal operation on the old version of the system, this permission must be registered in the manifest file
                    throw new ManifestRegisterException(Permission.BODY_SENSORS);
                }
            }

            if (minSdkVersion <Build.VERSION_CODES.O) {
                if (Permission.READ_PHONE_NUMBERS.equals(permission) &&
                        !manifestPermissions.contains(Permission.READ_PHONE_STATE)) {
                    // In order to ensure normal operation on the old version of the system, this permission must be registered in the manifest file
                    throw new ManifestRegisterException(Permission.READ_PHONE_STATE);
                }
            }

            if (Permission.NOTIFICATION_SERVICE.equals(permission)) {
                // Do not check whether the notification bar permission is registered in the manifest file, because this permission is virtualized by the framework, it does not matter whether it is registered in the manifest file or not
                continue;
            }

            if (!manifestPermissions.contains(permission)) {
                throw new ManifestRegisterException(permission);
            }
        }
    }
}