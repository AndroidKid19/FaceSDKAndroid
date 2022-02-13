package createbest.sdk.bihu.rfid;

import android.app.Instrumentation;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.createbest.cbtest.utils.SerialUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum Rfid {
    SERIAL_RFID {
        private ExecutorService service1 = Executors.newSingleThreadExecutor();
        private SerialUtils serialUtils = new SerialUtils();
        private InputStream inputStream;
        private boolean listening;
        private String serialPath;
        private int serialBaudrate = 9600;
        private ReadCallback mReadCallback;

        @Override
        public void startListen() {
            startListen(null);
        }

        @Override
        public void startListen(ReadCallback readCallback) {
            this.mReadCallback = readCallback;
            service1.submit(new Runnable() {
                @Override
                public void run() {
                    if (!listening) {
                        listening = true;
                        if (TextUtils.isEmpty(serialPath)) {
                            serialPath = getDefaultPath();
                        }
                        String path = serialPath;
                        if (TextUtils.isEmpty(serialPath)) {
                            listening = false;
                            return;
                        }
                        try {
                            serialUtils.open(path, serialBaudrate);
                            Log.d("RFID", "RFID 打开串口 ：path=" + path + " baudrate=" + serialBaudrate);
                            inputStream = serialUtils.getInputStream();
                            byte[] buffer = new byte[1024];
                            while (listening && inputStream != null) {
                                try {
                                    if (inputStream.available() > 0) {
                                        Thread.sleep(200);
                                        int count = inputStream.read(buffer);
                                        if (count > 3) {
                                            int dataLength = buffer[2] & 0xff;
                                            if (count > dataLength + 3) {
                                                StringBuffer sb = new StringBuffer();
                                                for (int i = dataLength + 3 - 1; i >= 3; i--) {
                                                    String t = Integer.toHexString(buffer[i] & 0xff);
                                                    if (t.length() == 1) {
                                                        sb.append(0);
                                                    }
                                                    sb.append(t);
                                                }
                                                long cardNum = Long.valueOf(sb.toString(), 16);
                                                String cardNumStr = new DecimalFormat("0000000000").format(cardNum);
                                                Log.d("RFID", "卡ID：[" + cardNumStr + "]");
                                                if (mReadCallback != null) {
                                                    mReadCallback.onReadedRfid(cardNumStr);
                                                } else {
                                                    sendKeyEvent(cardNumStr);
                                                }
                                            }
                                        }
                                    }
                                } catch (IOException | InterruptedException e) {
                                    e.printStackTrace();
                                    listening = false;
                                    try {
                                        inputStream.close();
                                    } catch (Exception e1) {
                                        e.printStackTrace();
                                    }
                                    serialUtils.close();
                                    startListen(mReadCallback);
                                    return;
                                }
                            }
                            listening = false;
                            try {
                                inputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            serialUtils.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            listening = false;
                            startListen(mReadCallback);
                        }
                    }
                }
            });
        }

        /**
         * 停止读卡
         */
        public void stopListen() {
            listening = false;
        }

        private String getDefaultPath() {
            if (!listening) return "";
            String path = null;
            File dir = new File("/dev");
            File[] list = dir.listFiles();
            for (File t : list) {
                if (t.getName().startsWith("ttyACM") && t.exists() && t.canRead() && t.canWrite()) {
                    path = t.getAbsolutePath();
                    break;
                }
            }
            if (path == null) {
                Log.d("RFID", "Cannot find RFID!");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getDefaultPath();
            } else {
                return path;
            }
        }
    },
    HID_RFID {
        @Override
        public void startListen() {
        }

        @Override
        public void startListen(ReadCallback readCallback) {
        }

        @Override
        public void stopListen() {
        }
    },
    I2C_RFID {
        private ExecutorService service1 = Executors.newSingleThreadExecutor();
        private boolean listening;
        private ReadCallback mReadCallback;

        @Override
        public void startListen() {
            startListen(null);
        }

        @Override
        public void startListen(ReadCallback readCallback) {
            this.mReadCallback = readCallback;
            service1.submit(new Runnable() {
                @Override
                public void run() {
                    if (listening) return;
                    listening = true;
                    com.createbest.rfid.RFID.init();
                    byte[] uid = new byte[32];
                    String uidHex = null;
                    do {
                        int len = com.createbest.rfid.RFID.Iso14443BGetUID(uid);
                        if (len <= 0) {
                            len = com.createbest.rfid.RFID.Iso14443AGetUID(uid);
                        }
                        if (len > 0) {
                            String curUidHex = bytesToHex(uid, 0, len);
                            if (!curUidHex.equals(uidHex)) {
                                uidHex = curUidHex;
                                long cardNum = Long.valueOf(curUidHex, 16);
                                String cardNumStr = new DecimalFormat("0000000000").format(cardNum);
                                Log.d("RFID", "卡ID：[" + cardNumStr + "]");
                                if (mReadCallback != null) {
                                    mReadCallback.onReadedRfid(cardNumStr);
                                } else {
                                    sendKeyEvent(cardNumStr);
                                }
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            uidHex = null;
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (listening);
                    com.createbest.rfid.RFID.deinit();
                }
            });
        }

        @Override
        public void stopListen() {
            listening = false;
        }

        private String bytesToHex(byte[] buffer, int offset, int length) {
            StringBuilder sb = new StringBuilder();
            for (int idx = offset; buffer != null && idx < offset + length; idx++) {
                sb.append(String.format("%02X", buffer[idx]));
            }
            return sb.toString();
        }
    };

    /**
     * 读卡回调
     */
    public static interface ReadCallback {
        /**
         * 读取到卡号
         */
        void onReadedRfid(String rfidCardId);
    }

    /**
     * 开始监听刷卡，刷卡后数据以KEY方式发送
     */
    public abstract void startListen();

    /**
     * 开始监听刷卡，刷卡后数据从{@link ReadCallback#onReadedRfid(String)}回调
     *
     * @param readCallback 读卡回调
     */
    public abstract void startListen(ReadCallback readCallback);

    /**
     * 停止监听刷卡
     */
    public abstract void stopListen();

    private ExecutorService service = Executors.newSingleThreadExecutor();

    /**
     * 转成KEY事件
     *
     * @param text
     */
    void sendKeyEvent(final String text) {
        service.submit(new Runnable() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendStringSync(text);
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
