package createbest.sdk.bihu.distance;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 距离感应
 */
public class Distance implements SensorEventListener {
    public static Distance INSTANCE = new Distance();
    private SensorManager sensorManager;
    private int threshold = 1000;//有效感应距离阈值(单位：mm)
    private long waitTimeout = -1;//业务等待超时时间（单位：ms）
    private int validContinuityCount = 0;
    private int invalidContinuityCount = 0;
    private boolean hasPerson = true;
    private Callback callback;
    private List<Integer> values = new ArrayList<>();

    private Distance() {
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            callback.onWaitTimeout();
        }
    };

    public static interface Callback {
        /**
         * 有人来了
         */
        void onSomeoneCome();

        /**
         * 距离感应数值回调
         *
         * @param originalValue 距离感应原始数值
         * @param validValue    距离感应有效数值，因为环境干扰，原始数值可能不准确，所以对原始数值作了处理
         */
        void onGetValue(int originalValue, int validValue);

        /**
         * 人员离开了
         */
        void onPersonLeft();

        /**
         * 超过业务等待时间
         */
        void onWaitTimeout();
    }

    /**
     * 设置最大有效距离，单位：mm
     *
     * @param threshold
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold > 2000 ? 2000 : threshold;
    }

    /**
     * 设置业务等待超时时间，单位：ms
     *
     * @param waitTimeout
     */
    public void setWaitTimeout(long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    /**
     * 注册距离监听
     *
     * @param context
     * @param callback 监听的回调
     */
    public void register(Context context, Callback callback) {
        this.callback = callback;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * 注销距离监听
     */
    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int originalValue = (int) event.values[0];
        p(originalValue);
    }

    private void p(int originalValue) {
        int validValue = originalValue;
        if (originalValue < 200 || originalValue > threshold) {
            validValue = threshold * 3;
        }
        if (callback != null) {
            callback.onGetValue(originalValue, hasPerson ? validValue : threshold * 3);
        }
        if (validValue <= threshold) {//有人
            invalidContinuityCount = 0;
            validContinuityCount++;
            if (validContinuityCount == 5) {
                handler.removeMessages(1);
                if (!hasPerson) {
                    hasPerson = true;
                    callback.onSomeoneCome();
                }
            }
        } else {//无人
            validContinuityCount = 0;
            invalidContinuityCount++;
            if (invalidContinuityCount == 1) {
                if (hasPerson) {
                    hasPerson = false;
                    callback.onPersonLeft();
                    if (waitTimeout > 0) {
                        handler.removeMessages(1);
                        handler.sendEmptyMessageDelayed(1, waitTimeout);
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
