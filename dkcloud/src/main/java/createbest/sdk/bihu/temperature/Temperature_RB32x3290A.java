package createbest.sdk.bihu.temperature;

import android.text.TextUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android_serialport_api.SerialPort;

/**
 * 体温检测
 */
public class Temperature_RB32x3290A implements ITemperature {
    private static Temperature_RB32x3290A instance;

    /**
     * 获取单例实例
     *
     * @return
     */
    public static Temperature_RB32x3290A getInstance() {
        if (instance == null) {
            instance = new Temperature_RB32x3290A();
        }
        return instance;
    }

    private Temperature_RB32x3290A() {
    }

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private SerialPort serialUtils = new SerialPort();
    private boolean reading;
    private Reader reader;
    private int baudrate = 115200;
    private String path = "dev/ttyS4";
    private float wd;//温度
    private long wdTime;//温度获取时间
    private boolean jarless;//测温数值是否稳定

    /**
     * 获取当前温度
     *
     * @return
     */
    public float getCurrTemperature() {
        if (System.currentTimeMillis() - wdTime < 600) {
            return wd;
        }
        return 0;
    }

    /**
     * 获取有效的当前温度,没有有效温度时返回0
     *
     * @return 有效的当前温度, 没有有效温度时返回0
     */
    public float getValidTemperature() {
        if (jarless && System.currentTimeMillis() - wdTime < 600 && wd > 35.8 && wd < 45) {
            return wd;
        }
        return 0;
    }

    /**
     * 获取稳定的温度
     * 可能需要一点时间，所以可能会阻塞线程，不能在UI线程调用!
     *
     * @return
     */
    public float getJarlessTemperature() {
        if (System.currentTimeMillis() - wdTime < 600) {
            if (jarless) {
                return wd;
            } else {
                try {
                    Thread.sleep(100);
                    return getJarlessTemperature();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    /**
     * 设置温度读取器
     *
     * @param reader
     */
    public synchronized void setReader(Reader reader) {
        this.reader = reader;
    }

    /**
     * 开启测温
     */
    public synchronized void open(String path) {
        if (reading) {
            return;
        }
        try {
            serialUtils.open(TextUtils.isEmpty(path) ? this.path : path, baudrate);
            read();
            send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭测温
     */
    public synchronized void close() {
        reading = false;
        if (serialUtils != null) {
            serialUtils.close();
        }
    }

    private void send() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                byte[] cmd = new byte[]{(byte) 0xA5, (byte) 0x55, (byte) 0x01, (byte) 0xFB};
                while (reading) {
                    try {
                        serialUtils.getOutputStream().write(cmd);
                        serialUtils.getOutputStream().flush();
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void read() {
        if (reading) {
            return;
        }
        reading = true;
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[7];
                while (reading) {
                    try {
                        if (serialUtils.getInputStream().available() > 0) {
                            Thread.sleep(50);
                            int size = serialUtils.getInputStream().read(buffer);
                            if (size == 7 && (buffer[0] & 0xff) == 0xA5 && (buffer[1] & 0xff) == 0x55) {
                                float currWd = ((buffer[2] & 0xff) + (buffer[3] & 0xff) * 256) / 10 / 10f;
                                jarless = currWd == wd;
                                wd = currWd;
                                wdTime = System.currentTimeMillis();
                                if (reader != null) {
                                    reader.onGetTemperature(wd, jarless);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
