package createbest.sdk.bihu.temperature;

import android.text.TextUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android_serialport_api.SerialPort;

/**
 * 体温检测
 */
public class Temperature_HM5_b implements ITemperature {
    private static Temperature_HM5_b instance;

    /**
     * 获取单例实例
     *
     * @return
     */
    public static Temperature_HM5_b getInstance() {
        if (instance == null) {
            instance = new Temperature_HM5_b();
        }
        return instance;
    }

    private Temperature_HM5_b() {
    }

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private SerialPort serialUtils = new SerialPort();
    private boolean reading;
    private Reader reader;
    private int baudrate = 921600;
    private String path = "dev/ttyS4";
    private float lastWd;
    private long lastWdtime;
    private boolean jarless;//测温数值是否稳定

    /**
     * 获取当前温度
     *
     * @return
     */
    public float getCurrTemperature() {
        if (System.currentTimeMillis() - lastWdtime < 600) {
            return lastWd;
        }
        return 0;
    }

    /**
     * 获取有效的当前温度,没有有效温度时返回0
     *
     * @return 有效的当前温度, 没有有效温度时返回0
     */
    public float getValidTemperature() {
        if (jarless && System.currentTimeMillis() - lastWdtime < 600 && lastWd > 35.8 && lastWd < 45) {
            return lastWd;
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
        if (System.currentTimeMillis() - lastWdtime < 600) {
            if (jarless) {
                return lastWd;
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
    }

    private void read() {
        if (reading) {
            return;
        }
        reading = true;
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[2064 * 5];
                while (reading) {
                    try {
                        if (serialUtils.getInputStream().available() > 0) {
                            Thread.sleep(50);
                            int size = serialUtils.getInputStream().read(buffer);
                            if (size == 2064 && (buffer[0] & 0xff) == 0xfe && (buffer[1] & 0xff) == 0x32) {
                                float wd = ((buffer[2057] & 0xff) + ((buffer[2056] & 0xff) << 8)) / 10f - 273.15f;
                                int n = (int) (wd * 100);
                                float currWd = n / 100f;
                                jarless = currWd == lastWd;
                                lastWd = currWd;
                                lastWdtime = System.currentTimeMillis();
                                if (reader != null) {
                                    reader.onGetTemperature(lastWd, jarless);
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
