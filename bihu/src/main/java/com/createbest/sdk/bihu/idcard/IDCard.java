package com.createbest.sdk.bihu.idcard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.createbest.sdk.bihu.R;
import com.wrfid.dev.USBMsg;
import com.wrfid.dev.WRFIDApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

/**
 * 身份证
 */
public class IDCard {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yy年MM月dd日");
    private static final String TAG = "Card2";
    private static IDCard instance;
    private boolean reading;
    private String filepath;
    private WRFIDApi api;
    private ReadCardCallback readCardCallback;
    Object key = new Object();
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case USBMsg.ReadIdCardSusse://读卡成功
                    IDCardInfo IDCardInfo = (IDCardInfo) msg.obj;
                    if (readCardCallback != null) {
                        readCardCallback.onReadSuccess(IDCardInfo);
                    }
                    break;
                case USBMsg.ReadIdCardFail://读卡失败
                    int code = (int) msg.obj;
                    if (readCardCallback != null) {
                        readCardCallback.onReadFail(code);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 身份证读取回调
     */
    public interface ReadCardCallback {

        /**
         * 读取到身份证信息
         *
         * @param IDCardInfo 身份证信息
         */
        void onReadSuccess(IDCardInfo IDCardInfo);

        /**
         * 读卡失败
         *
         * @param code 1:读卡失败 2：图像解码失败
         */
        void onReadFail(int code);
    }

    /**
     * 监听身份证
     *
     * @param context
     * @param readCardCallback 监听回调
     */
    public void startFindCard(Activity context, final ReadCardCallback readCardCallback) {
        if (init(context)) {
            reading = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (reading) {
                        int ret = -1;
                        synchronized (key) {
                            ret = api.WRFID_Authenticate();// 卡认证
                        }
                        if (ret == 0) {// 卡认证成功
                            Log.d(TAG, "找到卡");
                            readCard(readCardCallback);
                        } else {
//                            Log.v(TAG, "卡认证失败");
                        }
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * 停止寻卡
     */
    public void stopFindCard() {
        reading = false;
        readCardCallback = null;
    }

    /**
     * 读取身份证信息
     *
     * @param readCardCallback 回调
     */
    private void readCard(ReadCardCallback readCardCallback) {
        this.readCardCallback = readCardCallback;
        if (reading) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            com.wrfid.dev.IDCardInfo ic = new com.wrfid.dev.IDCardInfo();
            deleteFile(filepath + "/zp.bmp");// 删除上一身份证的头像
            deleteFile(filepath + "/zp.wlt");// 删除上一身份证的头像源始数据
            int ret = -1;
            synchronized (key) {
                ret = api.WRFID_Read_Content(ic);// 读卡
            }
            if (ret != 0) {// 读卡失败
                Log.v(TAG, "读卡失败:ret=" + ret);
                mHandler.sendMessage(mHandler.obtainMessage(USBMsg.ReadIdCardFail, 1));
            } else {
                IDCardInfo IDCardInfo = new IDCardInfo();
                IDCardInfo.address = ic.getAddr();
                IDCardInfo.birthDay = sdf.format(ic.getBirthDay());
                IDCardInfo.department = ic.getDepartment();
                IDCardInfo.id = ic.getIDCard();
                IDCardInfo.name = ic.getPeopleName();
                IDCardInfo.people = ic.getPeople();
                IDCardInfo.sex = ic.getSex();
                IDCardInfo.effext = ic.getStrartDate() + "-" + ic.getEndDate();
                byte[] bmpdata = new byte[38862];
                int ret1 = api.Unpack(filepath, ic.getwltdata(), bmpdata);// 照片解码
                if (ret1 != 0) {// 照片解码失败
                    Log.v(TAG, "照片解码失败");
                    mHandler.sendMessage(mHandler.obtainMessage(USBMsg.ReadIdCardFail, 2));
                } else {
                    String filePath = new File(filepath, "zp.bmp").getAbsolutePath();
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    IDCardInfo.photoBase64 = bitmapToBase64(bitmap);
                    bitmap.recycle();
                    if (IDCardInfo.photoBase64 != null) {
                        mHandler.sendMessage(mHandler.obtainMessage(USBMsg.ReadIdCardSusse, IDCardInfo));
                    }
                }
            }
        }
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static IDCard getInstance() {
        if (instance == null) {
            instance = new IDCard();
        }
        return instance;
    }

    private IDCard() {
    }

    private boolean init(Activity context) {
        filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BiHu/auth";// 授权目录
        copyfile(context, filepath, "base.dat", R.raw.base);
        copyfile(context, filepath, "license.lic", R.raw.license);
        api = new WRFIDApi(mHandler);
        return api.WRFID_Init(context);
    }

    private void copyfile(Context context, String fileDirPath, String fileName, int id) {
        String filePath = fileDirPath + "/" + fileName;// 文件路径
        try {
            File dir = new File(fileDirPath);// 目录路径
            if (!dir.exists()) {// 如果不存在，则创建路径名
                dir.mkdirs();
            }
            File file = new File(filePath);
            if (!file.exists()) {// 文件不存在
                InputStream is = context.getResources().openRawResource(
                        id);// 通过raw得到数据资源
                FileOutputStream fs = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int count = 0;// 循环写出
                while ((count = is.read(buffer)) > 0) {
                    fs.write(buffer, 0, count);
                }
                fs.close();// 关闭流
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            }
        }
    }
}
