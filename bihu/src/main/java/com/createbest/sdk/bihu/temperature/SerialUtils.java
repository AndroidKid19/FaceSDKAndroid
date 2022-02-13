package com.createbest.sdk.bihu.temperature;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 串口工具
 * Created by lianglh on 2017/2/23.
 */
public class SerialUtils {
    private static final String TAG = "SerialUtils";

    static {
        System.loadLibrary("serial_port");
    }

    private static native FileDescriptor open(String path, int baudrate, int flags);

    public native void closex();

    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    /**
     * 打开串口
     *
     * @param path     路径
     * @param baudrate 波特率
     * @throws IOException
     */
    public boolean open(String path, int baudrate) throws IOException {
        File device = new File(path);
        if (!device.exists()) {
            System.out.println("串口路径文件不存在");
            return false;
        }
        if (!device.canRead() || !device.canWrite()) {
            System.out.println("串口路径无操作权限");
            return false;
        }

        mFd = open(device.getAbsolutePath(), baudrate, 0);
        if (mFd == null) {
            System.out.println("native open returns null");
            return false;
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);

        System.out.println(path + " is opened");
        return true;
    }

    /**
     * 获取输入流
     *
     * @return
     */
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    /**
     * 获取输出流
     *
     * @return
     */
    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    /**
     * 关闭串口
     */
    public void close() {
        try {
            mFileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mFileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        closex();
    }
}
