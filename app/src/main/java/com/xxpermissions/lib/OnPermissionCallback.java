package com.xxpermissions.lib;

import java.util.List;

public interface OnPermissionCallback {

    /**
     * Call back when permission is granted
     *
     * @param permissions The permission group for which the request is successful
     * @param all is all granted
     */
    void onGranted(List<String> permissions, boolean all);

    /**
     * Call back when permission is denied
     *
     * @param permissions The permission group for which the request failed
     * @param never Is a permission permanently denied?
     */
    default void onDenied(List<String> permissions, boolean never) {}
}