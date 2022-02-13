package com.createbest.sdk.bihu.temperature;

/**
 * 体温检测接口
 */
public interface ITemperature {

    /**
     * 获取当前温度
     *
     * @return
     */
    public float getCurrTemperature();

    /**
     * 获取有效的当前温度,没有有效温度时返回0
     *
     * @return 有效的当前温度, 没有有效温度时返回0
     */
    public float getValidTemperature();

    /**
     * 获取稳定的温度
     * 可能需要一点时间，所以可能会阻塞线程，不能在UI线程调用!
     *
     * @return
     */
    public float getJarlessTemperature();

    public interface Reader {
        /**
         * 获取到温度数据
         *
         * @param temp    温度数据
         * @param jarless
         */
        void onGetTemperature(float temp, boolean jarless);
    }

    /**
     * 设置温度读取器
     *
     * @param reader
     */
    public void setReader(Reader reader);

    /**
     * 开启测温
     * @param path 可以为空，默认路径是dev/ttyS4
     */
    public void open(String path);

    /**
     * 关闭测温
     */
    public void close();

}
