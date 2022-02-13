package com.createbest.sdk.bihu.hardware;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.createbest.sdk.bihu.cmd.Cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用硬件功能
 */
public class Hardware {
    /**
     * 获取系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取系统SDK版本
     */
    public static int getSDK_INT() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取设备序列号
     */
    public static String getDeviceSerial() {
        return Build.SERIAL;
    }

    /**
     * 获取设备厂商
     */
    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取设备型号
     *
     * @return 设备型号
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * 获取设备名
     *
     * @return 设备名
     */
    public static String getDeviceName() {
        return Build.DEVICE;
    }

    /**
     * 获取平台架构
     *
     * @return 平台架构
     */
    public static String getPlatFormArchitecture() {
        String abi = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            abi = Build.CPU_ABI;
        } else {
            abi = Build.SUPPORTED_ABIS[0];
        }
        return abi;
    }

    /**
     * 获取Ethernet的MAC地址
     *
     * @return
     */
    public static String getEthernetMac() {
        try {
            StringBuffer fileData = new StringBuffer(1000);
            String filePath = "/sys/class/net/eth0/address";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
            return fileData.toString().toUpperCase(Locale.ENGLISH).substring(0, 17);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 获取WIFI的MAC地址
     *
     * @return
     */
    public static String getWifiMac() {
        try {
            StringBuffer fileData = new StringBuffer(1000);
            String filePath = "/sys/class/net/wlan0/address";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
            return fileData.toString().toUpperCase(Locale.ENGLISH).substring(0, 17);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 获取蓝牙的MAC地址
     * @return
     */
    public static String getBlueToothMac() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String addr = mBluetoothAdapter.getAddress();
        return addr;
    }

    /**
     * 获取设备处理器
     *
     * @return 处理器
     */
    public static String getDeviceProcessor() {
        String processor = null;
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取设备CPU核数
     *
     * @return 核数
     */
    public static int getNumberOfCPUCores() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // Gingerbread doesn't support giving a single application access to both cores, but a
            // handful of devices (Atrix 4G and Droid X2 for example) were released with a dual-core
            // chipset and Gingerbread; that can let an app in the background run without impacting
            // the foreground application. But for our purposes, it makes them single core.
            return 1;  // 上面的意思就是2.3以前不支持多核,有些特殊的设备有双核...不考虑,就当单核!!
        }
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (SecurityException e) {
            cores = 0;   // 这个常量得自己约定
        } catch (NullPointerException e) {
            cores = 0;
        }
        return cores;
    }

    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String path = pathname.getName();
            // regex is slow, so checking char by char.
            if (path.startsWith("cpu")) {
                for (int i = 3; i < path.length(); i++) {
                    if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    };


    /**
     * 获取设备位数
     *
     * @return cpu位数
     */
    public static Integer getCPUBit() {
        Integer result = 0;
        String mProcessor = null;
        try {
            mProcessor = getFieldFromCpuinfo("Processor");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mProcessor != null) {
            if (mProcessor.contains("aarch64")) {
                result = 64;
            } else {
                result = 32;
            }
        }

        return result;
    }

    private static String getFieldFromCpuinfo(String field) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));
        Pattern p = Pattern.compile(field + "\\s*:\\s*(.*)");

        try {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    return m.group(1);
                }
            }
        } finally {
            br.close();
        }
        return null;
    }

    /**
     * 获取当前CPU频率
     *
     * @return 主频
     */
    public static int getDeviceBasicFrequency() {
        int result = 0;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            String CurPath = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
            fr = new FileReader(CurPath);
            br = new BufferedReader(fr);
            String text = br.readLine();
            result = Integer.parseInt(text.trim());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

    /**
     * 获取设备RAM(内存)大小
     *
     * @return 获取RAM(内存)
     */
    public static long getRamSize(Context context) {
        long totalSize = 0;
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(memoryInfo);
        totalSize = memoryInfo.totalMem;
        return totalSize;
    }

    /**
     * 获取内置存储器大小
     *
     * @return 返回存储器大小
     */
    public static StorageInfo getStorageInfo(Context context) {
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockCount = statFs.getBlockCountLong();
        long blockSize = statFs.getBlockSizeLong();
        long totalSpace = blockCount * blockSize;
        long aviableBlocks = statFs.getAvailableBlocksLong();
        long aviableSpace = aviableBlocks * blockSize;
        return new StorageInfo(blockCount, blockSize, totalSpace, aviableBlocks, aviableSpace);
    }

    public static class StorageInfo {
        public long blockCount;
        public long blockSize;
        public long totalSpace;
        public long aviableBlocks;
        public long aviableSpace;

        public StorageInfo(long blockCount, long blockSize, long totalSpace, long aviableBlocks, long aviableSpace) {
            this.blockCount = blockCount;
            this.blockSize = blockSize;
            this.totalSpace = totalSpace;
            this.aviableBlocks = aviableBlocks;
            this.aviableSpace = aviableSpace;
        }
    }
}
