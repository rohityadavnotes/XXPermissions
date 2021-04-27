package com.xxpermissions.lib;

import androidx.fragment.app.FragmentActivity;
import java.util.ArrayList;
import java.util.List;

public interface IPermissionInterceptor {

    /**
     * To apply for permission to block, you can first pop up Dialog here and then apply for permission
     */
    default void requestPermissions(FragmentActivity activity, OnPermissionCallback callback, List<String> permissions) {
        PermissionFragment.beginRequest(activity, new ArrayList<>(permissions), callback);
    }

    /**
     * Permission granted callback interception, see {@link OnPermissionCallback#onGranted(List, boolean)}
     */
    default void grantedPermissions(FragmentActivity activity, OnPermissionCallback callback, List<String> permissions, boolean all) {
        callback.onGranted(permissions, all);
    }

    /**
     * Permission denied callback interception, see {@link OnPermissionCallback#onDenied(List, boolean)}
     */
    default void deniedPermissions(FragmentActivity activity, OnPermissionCallback callback, List<String> permissions, boolean never) {
        callback.onDenied(permissions, never);
    }
}