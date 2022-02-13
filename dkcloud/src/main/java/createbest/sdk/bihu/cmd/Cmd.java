package createbest.sdk.bihu.cmd;


import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Cmd {
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static interface Callback {
        void onMessage(String message);

        void onError(String errorMessage);

        void onResult(int res);
    }

    public static void copy(File src, File targetDir, Callback callback) {
        String cmd = "cp -f " + src.getAbsolutePath() + " " + targetDir.getAbsolutePath();
        cmd += ";sync";
        exec(cmd, callback);
    }

    public static void move(File src, File targetDir, Callback callback) {
        String cmd = "mv " + src.getAbsolutePath() + " " + targetDir.getAbsolutePath();
        cmd += ";sync";
        exec(cmd, callback);
    }

    public static void rename(File src, String newName, Callback callback) {
        File file = new File(src.getParentFile(), newName);
        move(src, file, callback);
    }

    public static void install(File apkFile, Callback callback) {
        String cmd = "pm install -r " + apkFile.getAbsolutePath();
        exec(cmd, callback);
    }

    public static void uninstall(String packageName, Callback callback) {
        String cmd = "pm uninstall " + packageName;
        exec(cmd, callback);
    }

    public static void setTime(Calendar calendar, Callback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmyyyy.ss");
            String time = sdf.format(calendar.getTime());
            String cmd = "date " + time;
            exec(cmd, callback);
        }else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss");
            String time = sdf.format(calendar.getTime());
            String cmd = "date -s " + time;
            exec(cmd, callback);
        }
    }

    public static void sync(Callback callback) {
        exec("sync", callback);
    }

    public static void exec(String cmd, Callback callback) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Process p = null;
                try {
                    p = Runtime.getRuntime().exec("su");
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader ie = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
                    w.write(cmd);
                    w.flush();
                    w.close();
                    String infoLine = "";
                    while ((infoLine = in.readLine()) != null) {
                        Log.d("cmd", "输出:" + infoLine);
                        if (callback != null) {
                            callback.onMessage(infoLine);
                        }
                    }
                    while ((infoLine = ie.readLine()) != null) {
                        Log.d("cmd", "错误:" + infoLine);
                        if (callback != null) {
                            callback.onError(infoLine);
                        }
                    }
                    in.close();
                    ie.close();
                    int res = p.waitFor();
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    if (callback != null) {
                        callback.onResult(res);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
