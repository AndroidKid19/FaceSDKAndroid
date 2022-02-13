package createbest.sdk.bihu.android;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

/**
 * 常用系统功能
 */
public class OS {
    /**
     * 设备型号
     * @return
     */
    public static String getModel() {
        return Build.MODEL;
    }

    /**
     * 安卓版本
     * @return
     */
    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 打开系统设置
     *
     * @param context
     */
    public static void startSystemSettings(@NonNull Context context) {
        context.startActivity(new Intent(Settings.ACTION_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * 打开WIFI设置
     *
     * @param context
     */
    public static void startSystemWIFISettings(@NonNull Context context) {
        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * 打开蓝牙设置
     *
     * @param context
     */
    public static void startSystemBluetoothSettings(@NonNull Context context) {
        context.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * 打开语言设置
     *
     * @param context
     */
    public static void startSystemLanguageSettings(@NonNull Context context) {
        context.startActivity(new Intent(Settings.ACTION_LOCALE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * 重启
     *
     * @param context
     */
    public static void reboot(@NonNull Context context) {
        context.sendBroadcast(new Intent("android.intent.action.reboot"));
    }

    /**
     * 关机
     *
     * @param context
     */
    public static void shutdown(@NonNull Context context) {
        context.sendBroadcast(new Intent("android.intent.action.shutdown"));
    }

    /**
     * 显示导航栏
     *
     * @param context
     */
    public static void showNavigationBar(@NonNull Context context) {
        context.sendBroadcast(new Intent("com.cbest.show_nav_bar"));
    }

    /**
     * 隐藏导航栏
     *
     * @param context
     */
    public static void hideNavigationBar(@NonNull Context context) {
        context.sendBroadcast(new Intent("com.cbest.hide_nav_bar"));
    }

    /**
     * 显示状态栏
     *
     * @param context
     */
    public static void showStatusBar(@NonNull Context context) {
        context.sendBroadcast(new Intent("com.cbest.show_status_bar"));
    }

    /**
     * 隐藏状态栏
     *
     * @param context
     */
    public static void hideStatusBar(@NonNull Context context) {
        context.sendBroadcast(new Intent("com.cbest.hide_status_bar"));
    }
}
