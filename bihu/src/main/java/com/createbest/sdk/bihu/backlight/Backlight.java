package com.createbest.sdk.bihu.backlight;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 屏幕背光控制工具
 */
public class Backlight {
    private static final String path1 = "/sys/class/backlight/rk28_bl/brightness";
    private static final String path2 = "/sys/class/backlight/backlight/brightness";

    private static String getPath() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return path2;
        } else {
            return path1;
        }
    }

    /**
     * 获取调节后背光亮度
     *
     * @return
     */
    public static int getBacklightBrightness() {
        FileInputStream fis = null;
        byte[] buffer = new byte[32];
        try {
            fis = new FileInputStream(getPath());
            int len = fis.read(buffer, 0, buffer.length);
            if (len > 0) {
                return Integer.parseInt(new String(buffer, 0, len).trim(), 10);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        }

        return 0;
    }

    /**
     * 设置背光亮度
     *
     * @param brightness
     */
    public static void setBacklightBrightness(int brightness) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(getPath());
            fos.write(String.valueOf(brightness).getBytes());
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取系统原来设置的背光亮度
     *
     * @param context
     * @return
     */
    public static int getSystemBacklightBrightnes(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
    }

    /**
     * 关闭屏幕背光
     */
    public static void closeBacklightBrightnes() {
        setBacklightBrightness(0);
    }

    /**
     * 打开屏幕背光
     *
     * @param context
     */
    public static void openBacklightBrightnes(Context context) {
        setBacklightBrightness(getSystemBacklightBrightnes(context));
    }
}
