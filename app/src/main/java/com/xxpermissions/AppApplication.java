package com.xxpermissions;

import android.app.Application;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.WhiteToastStyle;
import com.xxpermissions.lib.XXPermissions;

public final class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the toast tool class
        ToastUtils.init(this, new WhiteToastStyle());

        // Set permission request interceptor
        XXPermissions.setInterceptor(new PermissionInterceptor());

        // Tell the framework that the current project has been adapted to the partition storage feature
        //XXPermissions.setScopedStorage(true);
    }
}