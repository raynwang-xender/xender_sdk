1.manifest文件中集成：

<service android:name="cn.xender.core.ap.service.OAPService"
    android:process=":oap" />

<service android:name="cn.xender.core.server.service.HttpServerService"/>


 如果使用我们的界面，还需要集成：

<activity android:name=“cn.xender.transfer.ShareActivity">
</activity>

2.如果需要混淆：

-keep public class cn.xender.**{*;}

3.权限：
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />

4.依赖：
implementation 'com.google.zxing:core:3.3.0'

implementation 'com.android.support:design:27.1.1'

5.启用方法：

Intent intent = new Intent(MainActivity.this,ShareActivity.class);
startActivity(intent);