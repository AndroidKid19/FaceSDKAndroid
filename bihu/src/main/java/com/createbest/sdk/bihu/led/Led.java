package com.createbest.sdk.bihu.led;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 补光灯
 */
public class Led {
    private static Led instance;

    /**
     * 灯色
     */
    public static enum LedColor {
        WIHTE, RED, GREEN;
    }

    /**
     * LED类型，不同的设备使用的LED类型可能不同，请根据您的硬件设备配置选择正确的类型。
     */
    public static enum LedType {
        /**
         * 单色补光灯
         */
        SINGLE {
            private static final String PATH = "/sys/class/leds/wled/brightness";

            @Override
            void setLed(LedColor ledColor, String brightness) {
                send(PATH, brightness);
            }
        },
        /**
         * 三色补光灯A型
         */
        THREE_A {
            private static final String PATH_WHITE = "/sys/class/leds/wled/brightness";
            private static final String PATH_RED = "/sys/class/leds/rled/brightness";
            private static final String PATH_GREEN = "/sys/class/leds/gled/brightness";

            @Override
            void setLed(LedColor ledColor, String brightness) {
                send(PATH_RED, ledColor == LedColor.RED ? brightness : "0");
                send(PATH_GREEN, ledColor == LedColor.GREEN ? brightness : "0");
                send(PATH_WHITE, ledColor == LedColor.WIHTE ? brightness : "0");
            }
        },
        /**
         * 三色补光灯B型
         */
        THREE_B {
            private static final String PATH_WHITE = "/sys/class/leds/white/brightness";
            private static final String PATH_RED = "/sys/class/leds/red/brightness";
            private static final String PATH_GREEN = "/sys/class/leds/green/brightness";

            @Override
            void setLed(LedColor ledColor, String brightness) {
                send(PATH_RED, ledColor == LedColor.RED ? brightness : "0");
                send(PATH_GREEN, ledColor == LedColor.GREEN ? brightness : "0");
                send(PATH_WHITE, ledColor == LedColor.WIHTE ? brightness : "0");
            }
        };

        abstract void setLed(LedColor ledColor, String brightness);
    }

    /**
     * 补光灯控制模式
     */
    public static enum LedControlMode {
        KEEP_OPEN(1), KEEP_CLOSE(2), AUTO(3), BY_TIME(4);
        private int mode = 2;//开关模式，1开，2关，3自动，4按时段
        private String startTime;//开始时间，开关模式＝4时有效
        private String endTime;//结束时间，开关模式＝4时有效

        private LedControlMode(int mode) {
            this.mode = mode;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }

    public static Led getInstance() {
        if (instance == null) {
            instance = new Led();
        }
        return instance;
    }

    private Led() {
    }

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future future;
    private static ExecutorService executorService2 = Executors.newSingleThreadExecutor();
    private boolean byHasPerson;
    private long hasPersonLastTime;
    private LedType ledType = LedType.SINGLE;
    private LedColor ledColor = LedColor.WIHTE;
    private int maxBrightness = 255;
    private LedControlMode ledControlMode;

    /**
     * 设置LED控制模式
     *
     * @param ledControlMode 请查阅{@link LedControlMode}
     */
    public void setLedControlMode(LedControlMode ledControlMode) {
        this.ledControlMode = ledControlMode;
    }

    /**
     * @param ledType LED类型 请查阅{@link LedType}
     */
    public void setLedType(LedType ledType) {
        this.ledType = ledType;
    }

    /**
     * @param ledColor 灯色 如果机器只有白色也是兼容的，不用担心。请查阅{@link LedColor}
     */
    public void setLedColor(LedColor ledColor) {
        this.ledColor = ledColor;
    }

    /**
     * @param maxBrightness
     */
    public void setMaxBrightness(int maxBrightness) {
        this.maxBrightness = maxBrightness;
    }

    /**
     * 更新补光灯状态
     */
    public void updateLed() {
        updateLed(null, false);
    }

    /**
     * 更新补光灯状态
     *
     * @param data      摄像头的原始图像数据（NV21）
     * @param hasPerson 是否有人
     */
    public void updateLed(final byte[] data, final boolean hasPerson) {
        if (hasPerson) {
            byHasPerson = true;
            hasPersonLastTime = System.currentTimeMillis();
        }

        if (future != null && !future.isDone()) {
            return;
        }
        future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (data == null) {
                    setLed(ledType, ledColor, (byte) 0);
                    return;
                }

                if (ledControlMode == LedControlMode.KEEP_CLOSE) {
                    setLed(ledType, ledColor, (byte) 0);
                } else if (ledControlMode == LedControlMode.KEEP_OPEN) {
                    setLed(ledType, ledColor, (byte) maxBrightness);
                } else if (ledControlMode == LedControlMode.BY_TIME) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Calendar now = Calendar.getInstance();
                        String ymd = sdf3.format(now.getTime());
                        Date dateStart = sdf2.parse(ymd + " " + ledControlMode.startTime);
                        Date dateEnd = sdf2.parse(ymd + " " + ledControlMode.endTime);
                        Calendar calendarStart = Calendar.getInstance();
                        calendarStart.setTime(dateStart);
                        Calendar calendarEnd = Calendar.getInstance();
                        calendarEnd.setTime(dateEnd);

                        if (dateStart.after(dateEnd)) {
                            calendarEnd.add(Calendar.DAY_OF_MONTH, 1);
                        }

                        if (calendarStart.before(now) && calendarEnd.after(now)) {
                            setLed(ledType, ledColor, (byte) maxBrightness);
                        } else {
                            setLed(ledType, ledColor, (byte) 0);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        setLed(ledType, ledColor, (byte) 0);
                    }
                } else if (ledControlMode == LedControlMode.AUTO) {
                    if (!hasPerson) {//没有人
                        if (byHasPerson) {
                            if (System.currentTimeMillis() - hasPersonLastTime > 400) {
                                byHasPerson = false;
                                setLed(ledType, ledColor, (byte) 0);
                            }
                        } else {
                            int p = getImageBrightness(data);
                            setLed(ledType, ledColor, p < 60 ? (byte) maxBrightness : 0);
                        }
                    } else {//有人
                        int p = getImageBrightness(data);
                        if (p < 100 || ledColor != LedColor.WIHTE) {
                            setLed(ledType, ledColor, (byte) maxBrightness);
                        }
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 计算图像亮度
     *
     * @param data
     * @return
     */
    private int getImageBrightness(byte[] data) {
        //计算出亮度之和sum、个数count
        long sum = 0;
        int count = 0;
        int max = data.length * 2 / 3;
        for (int i = 0; i < max; i++) {
            sum += (data[i] & 0xff);
            count++;
        }
        //p表示平均亮度
        return (int) (sum / count);
    }

    /**
     * 设置补光灯
     *
     * @param ledType    LED类型 请查阅{@link LedType}
     * @param ledColor   灯色 如果机器只有白色也是兼容的，不用担心。请查阅{@link LedColor}
     * @param brightness 亮度值（0~255）
     */
    public static void setLed(final LedType ledType, final LedColor ledColor, byte brightness) {
        executorService2.submit(new Runnable() {
            @Override
            public void run() {
                String status = String.valueOf(brightness & 0xff);
                ledType.setLed(ledColor, status);
            }
        });
    }

    private static boolean send(String path, String data) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(data.getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return false;
    }
}
