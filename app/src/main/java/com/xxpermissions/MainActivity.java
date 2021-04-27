package com.xxpermissions;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import com.hjq.toast.ToastUtils;
import com.xxpermissions.lib.OnPermissionCallback;
import com.xxpermissions.lib.Permission;
import com.xxpermissions.lib.XXPermissions;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_main_request_1).setOnClickListener(this);
        findViewById(R.id.btn_main_request_2).setOnClickListener(this);
        findViewById(R.id.btn_main_request_3).setOnClickListener(this);
        findViewById(R.id.btn_main_request_4).setOnClickListener(this);
        findViewById(R.id.btn_main_request_5).setOnClickListener(this);
        findViewById(R.id.btn_main_request_6).setOnClickListener(this);
        findViewById(R.id.btn_main_request_7).setOnClickListener(this);
        findViewById(R.id.btn_main_request_8).setOnClickListener(this);
        findViewById(R.id.btn_main_app_details).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btn_main_request_1) {

            XXPermissions.with(this)
                    .permission(Permission.CAMERA)
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (all) {
                                toast("Succeeded in obtaining photo permission");
                            }
                        }
                    });

        } else if (viewId == R.id.btn_main_request_2) {

            XXPermissions.with(this)
                    .permission(Permission.RECORD_AUDIO)
                    .permission(Permission.Group.CALENDAR)
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (all) {
                                toast("Succeeded in obtaining recording and calendar permissions");
                            }
                        }
                    });

        } else if (viewId == R.id.btn_main_request_3) {

            XXPermissions.with(this)
                    .permission(Permission.ACCESS_COARSE_LOCATION)
                    .permission(Permission.ACCESS_FINE_LOCATION)
                    .permission(Permission.ACCESS_BACKGROUND_LOCATION)
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (all) {
                                toast("Successfully obtained positioning permission");
                            }
                        }
                    });

        } else if (viewId == R.id.btn_main_request_4) {

            long delayMillis = 0;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                delayMillis = 2000;
                toast("The current version is not Android 11 or above, it will automatically change to the old version of the request method");
            }

            view.postDelayed(new Runnable() {

                @Override
                public void run() {
                    XXPermissions.with(MainActivity.this)
                            // Not compatible with Android 11, you can write like this
                            //.permission(Permission.Group.STORAGE)
                            // To adapt to Android 11, you need to write like this, there is no need to write Permission.Group.STORAGE here
                            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                            .request(new OnPermissionCallback() {

                                @Override
                                public void onGranted(List<String> permissions, boolean all) {
                                    if (all) {
                                        toast("Successfully obtained storage permissions");
                                    }
                                }
                            });
                }
            }, delayMillis);

        } else if (viewId == R.id.btn_main_request_5) {

            XXPermissions.with(this)
                    .permission(Permission.REQUEST_INSTALL_PACKAGES)
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            toast("Successfully obtain installation package permissions");
                        }
                    });

        } else if (viewId == R.id.btn_main_request_6) {

            XXPermissions.with(this)
                    .permission(Permission.SYSTEM_ALERT_WINDOW)
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            toast("Succeeded in obtaining the permission of the floating window");
                        }
                    });

        } else if (viewId == R.id.btn_main_request_7) {

            XXPermissions.with(this)
                    .permission(Permission.NOTIFICATION_SERVICE)
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            toast("Succeeded in obtaining notification bar permission");
                        }
                    });

        } else if (viewId == R.id.btn_main_request_8) {

            XXPermissions.with(this)
                    .permission(Permission.WRITE_SETTINGS)
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            toast("Succeeded in obtaining system setting permissions");
                        }
                    });

        } else if (viewId == R.id.btn_main_app_details) {

            XXPermissions.startPermissionActivity(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == XXPermissions.REQUEST_CODE) {
            toast("It is detected that you have just returned from the permission setting interface");
        }
    }

    public void toast(CharSequence text) {
        ToastUtils.show(text);
    }
}