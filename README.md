# Permission request framework

![](art/logo.png)

* Code cloud address: [Github](https://github.com/getActivity/XXPermissions)

* Code cloud address: [Gitee](https://gitee.com/getActivity/XXPermissions)

* Blog post address: [One sentence of code to get permission request has never been easier] (https://www.jianshu.com/p/c69ff8a445ed)

* Click here [Download Demo](XXPermissions.apk) for demo or test

* In addition, if you want to know more about Android 6.0 permissions, you can read this article [Android 6.0 operating permissions analysis](https://www.jianshu.com/p/6a4dff744031)

#### Integration steps

* Add to the `build.gradle` file in the project root directory

```groovy
buildscript {
    ......
}
allprojects {
    repositories {
        // JitPack remote warehouse: https://jitpack.io
        maven {url'https://jitpack.io'}
    }
}
```

* Add in the `build.gradle` file under the app module of the project

```groovy
android {
    // Support JDK 1.8
    compileOptions {
        targetCompatibility JavaVersion.VERSION_1_8
        sourceCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Permission request framework: https://github.com/getActivity/XXPermissions
    implementation'com.github.getActivity:XXPermissions:10.8'
}
```

#### AndroidX

* If the project is based on the **AndroidX** package, please add it in the project `gradle.properties` file

```groovy
# Means to migrate third-party libraries to AndroidX
android.enableJetifier = true
```

* If the project is based on the **Support** package, you do not need to add this configuration

#### Partition storage

* If the project has been adapted to the Android 10 partition storage feature, please add it when the Application is initialized

```java
public final class XxxApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Whether the current project has adapted the characteristics of partition storage
        XXPermissions.setScopedStorage(true);
    }
}
```

* If the current project does not adapt to this feature, then this step can be ignored

#### One sentence of code has never been easier to get permission requests

```java
XXPermissions.with(this)
        // Apply for installation package permissions
        //.permission(Permission.REQUEST_INSTALL_PACKAGES)
        // apply for floating window permissions
        //.permission(Permission.SYSTEM_ALERT_WINDOW)
        // Apply for notification bar permissions
        //.permission(Permission.NOTIFICATION_SERVICE)
        // Apply for system setting permissions
        //.permission(Permission.WRITE_SETTINGS)
        // apply for a single permission
        .permission(Permission.RECORD_AUDIO)
        // apply for multiple permissions
        .permission(Permission.Group.CALENDAR)
        .request(new OnPermissionCallback() {

            @Override
            public void onGranted(List<String> permissions, boolean all) {
                if (all) {
                    toast("Successfully obtained recording and calendar permissions");
                } else {
                    toast("Successfully obtained partial permissions, but some permissions were not granted normally");
                }
            }

            @Override
            public void onDenied(List<String> permissions, boolean never) {
                if (never) {
                    toast("Authorization is permanently denied, please manually grant recording and calendar permissions");
                    // If it is permanently rejected, jump to the application permission system settings page
                    XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                } else {
                    toast("Failed to obtain recording and calendar permissions");
                }
            }
        });
```

#### Return to the judgment from the system permission setting page

```java
public class XxxActivity extends AppCompatActivity {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == XXPermissions.REQUEST_CODE) {
            if (XXPermissions.isGranted(this, Permission.RECORD_AUDIO) &&
                    XXPermissions.isGranted(this, Permission.Group.CALENDAR)) {
                toast("The user has granted recording and calendar permissions on the permission setting page");
            } else {
                toast("The user did not grant permission on the permission setting page");
            }
        }
    }
}
```

#### About the permission monitoring callback parameter description

* We all know that if the user grants all of them, only the **onGranted** method will be called, and if the users refuse all of them, only the **onDenied** method will be called.

* But there is another situation. If multiple permissions are requested, these permissions are not all granted or denied, but partially authorized and partially denied. How will the framework handle the callback?

* The framework will call the **onDenied** method first, and then the **onGranted** method. Among them, we can use the **all** parameter in the **onGranted** method to determine whether all permissions have been granted.

* If you want to know whether a permission in the callback is authorized or denied, you can call the **contains(Permission.XXX)** method in the **List** class to determine whether the permission is included in the collection.

### [About Android 11 adaptation and common questions, please click here to view](HelpDoc.md)

#### Comparison between different permission request frameworks

| Features and details | [XXPermissions](https://github.com/getActivity/XXPermissions) | [AndPermission](https://github.com/yanzhenjie/AndPermission) | [RxPermissions](https://github.com /tbruyelle/RxPermissions) | [PermissionsDispatcher](https://github.com/permissions-dispatcher/PermissionsDispatcher) | [EasyPermissions](https://github.com/googlesamples/easypermissions) | [PermissionX](https:// github.com/guolindev/PermissionX) | [AndroidUtilCode](https://github.com/Blankj/AndroidUtilCode) |
| :--------: | :------------: | :------------: | :------------: | :------------: | :------------: | :------------: | :------------: |
| Corresponding version | 10.8 | 2.0.3 | 0.12 | 4.8.0 | 3.0.0 | 1.4.0 | 1.30.5 |
| Frame volume | [24 KB](https://bintray.com/getactivity/maven/xxpermissions#files/com/hjq/xxpermissions)  | [127 KB](https://mvnrepository.com/artifact/com.yanzhenjie/permission)  |  [28 KB](https://jitpack.io/#com.github.tbruyelle/rxpermissions)  |   [91 KB](https://bintray.com/hotchemi/org.permissionsdispatcher/permissionsdispatcher-processor#files/org/permissionsdispatcher/permissionsdispatcher-processor)  |  [48 KB](https://bintray.com/easygoogle/EasyPermissions/easypermissions#files/pub/devrel/easypermissions)   |   [32 KB](https://bintray.com/guolindev/maven/permissionx#files/com/permissionx/guolindev/permissionx)  |   [483 KB](https://bintray.com/blankj/maven/UtilCode#files/com/blankj/utilcode)  |
| Installation package permissions | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Floating window permissions | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Notification bar permissions | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| System setting permissions | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Android 8.0 permission adaptation | ✅ | ✅ | ❌ | ✅ | ❌ | ✅ | ❌ |
| Android 9.0 permission adaptation | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| Android 10.0 permission adaptation | ✅ | ✅ | ❌ | ✅ | ❌ | ✅ | ❌ |
| Android 11 new storage permissions | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Android 11 new version positioning strategy | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Screen orientation rotation scene adaptation | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ |
| Background application permission scenario adaptation | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Low-level error detection mechanism | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

#### Screen rotation scene adaptation introduction

* When the system permission application dialog box pops up and the activity is rotated on the screen, the permission application callback will become invalid, because the screen rotation will cause the Fragment in the frame to be destroyed and rebuilt, which will cause the callback object inside to be directly recycled, and eventually cause the callback to be abnormal . There are several solutions. One is to add the `android:configChanges="orientation"` attribute to the manifest file so that the Activity and Fragment will not be destroyed and rebuilt when the screen rotates. The second is to fix the orientation of the Activity display directly in the manifest file. However, the above two solutions have to be handled by the framework person, which is obviously not flexible enough. The ringer must be tied to the ring. The frame problem should be solved by the framework, and the solution of **RxPermissions** is to give the PermissionFragment object Set `fragment.setRetainInstance(true)`, so that even if the screen rotates, the Activity object will be destroyed and rebuilt, and the Fragment will not be destroyed and rebuilt. It will still reuse the previous object, but there is a problem if the Activity is overwritten* The *onSaveInstanceState** method will directly cause this method to fail. Obviously, this is only a temporary solution, but the **XXPermissions** method will be more direct. When **PermissionFragment** is bound to the Activity, the current Activity The **screen orientation is fixed**, and the **screen orientation is restored after the permission application is completed**.

#### Background application permission scenario introduction

* When we apply for permission after doing a time-consuming operation (for example, obtain the privacy agreement on the splash screen and then apply for permission), return the Activity to the desktop (return to the background) during the network request process, and then cause the permission request to be in the background state In this case, the permission application may be abnormal at this time, and the authorization dialog box will not be displayed, and improper handling will cause a crash, such as [RxPeremission/issues/249](https://github.com/ tbruyelle/RxPermissions/issues/249). The reason is that the PermissionFragment in the framework will do a check when **commit / commitNow** reaches the Activity. If the status of the Activity is invisible, it will throw an exception, and **RxPeremission** uses **commitNow ** Will guide Cause a crash, use **commitAllowingStateLoss / commitNowAllowingStateLoss** to avoid this detection. Although this can avoid the crash, another problem will occur. The **requestPermissions** API provided by the system will not pop up when the Activity is not visible. In the authorization dialog box, the solution of **XXPermissions** is to transfer the timing of **requestPermissions** from **create** to **resume**, because the life cycle methods of Activity and Fragment are bundled together. If Activity It is invisible, so even if the Fragment is created, it will only call the **onCreate** method instead of its **onResume** method. Finally, when the Activity returns from the background to the foreground, not only will it be triggered** The Activity.onResume** method will also trigger the **onResume** method of **PermissionFragment**. Applying for permission in this method can ensure that the final requestPermissions** application time is in the visible state of Activity ** Situation **.

#### Introduction to low-level error detection mechanism

* In the daily maintenance of the framework, many people have reported to me that there are bugs in the framework, but after investigation and positioning, it is found that 95% of the problems are caused by some irregular operations of the caller, which not only caused a lot of trouble to me A big trouble, but also a great waste of time and energy of many small partners, so I added a lot of review elements to the framework, in the **Debug** mode, once some operations do not meet the specifications, then the framework will Throw an exception directly to the caller, and correctly guide the caller to correct the error in the exception information, for example:

1. The Activity object passed in is not a subclass of FragmentActivity, the framework will throw an exception, or the status of the passed Activity is abnormal (has been **Finishing** or **Destroyed**), this situation is generally asynchronous The framework will also throw exceptions caused by applying for permissions. Please apply for permissions at the right time. If the timing of the application cannot be estimated, please make a judgment on the Activity status in the outer layer before applying for permissions.

2. If the caller applies for permission without passing in any permission, the framework will throw an exception. If the permission passed by the caller is not a dangerous permission or a special permission, the framework will also throw an exception, because some people will use ordinary permissions. As dangerous permissions are passed to the framework, the system will directly reject them.

3. If you apply for external storage permission without a suitable partition for storage, you must register `android:requestLegacyExternalStorage="true"` in the manifest file, otherwise the framework will throw an exception. If you do not add it, the storage permission will be obtained. However, files on external storage cannot be read and written normally on Android 10 devices.

4. If the requested permission includes background positioning permission, then it cannot contain permissions that are not related to positioning, otherwise the framework will throw an exception, because `ACCESS_BACKGROUND_LOCATION` and other non-positioning permission positioning are mixed together to apply, in Android 11 There will be cases where you are directly rejected without applying.

5. If the requested permission does not match the **targetSdkVersion** in the project, the framework will throw an exception, because **targetSdkVersion** represents which Android version the project is adapted to, and the system will automatically do downward compatibility. Assuming that the requested permission only appeared in Android 11, but the **targetSdkVersion** still stays at 29, then the authorization result will be abnormal in the application result on some models.

6. If the dynamically applied permission is not registered in the manifest file, the framework will throw an exception, because if you don’t do this, you can apply for permission, but there will be no authorization pop-up window, and it will be directly rejected by the system. Will give any pop-up windows and prompts, and this problem is **must appear** on each model.

* The reason for these problems is that we are not very familiar with these mechanisms, and if the framework is not restricted, then various strange and strange problems will arise. As the author of the framework, not only is it painful for you, but also as the author of the framework. Very hurt. Because these problems are not caused by the framework, but caused by some irregular operations of the caller. I think the best way to solve this problem is to make a unified inspection by the framework, because I am the author of the framework and have strong professional ability and sufficient experience** for the knowledge point of permission application, and know what to do What to do and what not to do, so that you can intercept these sorrows one by one.

* When there is a problem with the permission application, do you want someone to come over to remind you and tell you what went wrong? How to correct it? However, these XXPermissions have been achieved. Among all the permission request frameworks, I am the first person to do this. I think that **making a framework** is not only to do a good job in the function, and to make complex scenes. To deal with it well, it is more important to be people-oriented, because the framework itself is for people. What we need to do is not only solve everyone's needs, but also help everyone avoid detours in the process.

#### Frame highlights

* The first and only permission request framework adapted to Android 11

* The first and only permission request framework that adapts to all Android versions

* Concise and easy to use: using chain call method, use only one sentence of code

* Touching size: The function is the most complete in the same framework, but the size is the smallest

* Adapt to extreme situations: No matter how extreme the environment is to apply for permission, the framework is still strong

* Backward compatibility attributes: New permissions can be applied for normally in the old system, and the framework will automatically adapt, without the caller adapting

* Automatic error detection: If there is a low-level error, the framework will actively throw an exception to the caller (only judge under Debug, and kill the bug in the cradle)
