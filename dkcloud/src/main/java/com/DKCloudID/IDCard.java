package com.DKCloudID;

import android.util.Log;

import com.Exception.CardNoResponseException;
import com.Exception.DKCloudIDException;
import com.Tool.StringTool;
import com.dk.uartnfc.SamVIdCard;

public class IDCard {
    private final static String TAG = "IDCard";
    SamVIdCard mSamVIdCard = null;
    DKCloudID dkCloudID = null;

    private onReceiveScheduleListener mOnReceiveScheduleListener;

    byte[] initData;

    public IDCard() {
    }

    public IDCard(SamVIdCard samVIdCard) {
        this.mSamVIdCard = samVIdCard;
    }

    //进度回调
    public interface onReceiveScheduleListener{
        void onReceiveSchedule(int rate);
    }

    /**
     * 获取身份证数据
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData() throws DKCloudIDException, CardNoResponseException {
        return getIDCardData(mSamVIdCard, null);
    }

    /**
     * 获取身份证数据，带进度回调
     * @param listener - 进度回调
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData(onReceiveScheduleListener listener) throws DKCloudIDException, CardNoResponseException {
        return getIDCardData(mSamVIdCard, listener);
    }

    /**
     * 获取身份证数据
     * @param samVIdCard - 获取到的B类型的tag
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData(SamVIdCard samVIdCard) throws DKCloudIDException, CardNoResponseException {
        return getIDCardData(samVIdCard, null);
    }

    /**
     * 获取身份证数据，带进度回调
     * @param samVIdCard - 获取到的B类型的tag
     * @param listener - 进度回调
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData(SamVIdCard samVIdCard, onReceiveScheduleListener listener) throws DKCloudIDException, CardNoResponseException {
        byte[] msgReturnBytes;

        mOnReceiveScheduleListener = listener;

        try {
            msgReturnBytes = samVIdCard.getSamVInitData();
            initData = msgReturnBytes;

            dkCloudID = new DKCloudID();
            if ( !dkCloudID.isConnected() ) {
                throw new DKCloudIDException("服务器连接失败");
            }
            Log.d(TAG, "向服务器发送数据：" + StringTool.byteHexToSting(msgReturnBytes));
            byte[] cloudReturnByte = dkCloudID.dkCloudTcpDataExchange(msgReturnBytes);
            Log.d(TAG, "接收到服务器数据：" + StringTool.byteHexToSting(cloudReturnByte));

            Log.d(TAG, "正在解析:1%");
            int schedule = 1;
            if ( (cloudReturnByte != null) && (cloudReturnByte.length >= 2)
                    && ((cloudReturnByte[0] == 0x03) || (cloudReturnByte[0] == 0x04)) ) {
                if ( mOnReceiveScheduleListener != null ) {
                    mOnReceiveScheduleListener.onReceiveSchedule(schedule);
                }
            }

            while (true) {
                if ( (cloudReturnByte == null) || (cloudReturnByte.length < 2)
                        || ((cloudReturnByte[0] != 0x03) && (cloudReturnByte[0] != 0x04)) ) {
                    if ( cloudReturnByte == null ) {
                        throw new DKCloudIDException("服务器返回数据为空");
                    }
                    else if (cloudReturnByte[0] == 0x05) {
                        throw new DKCloudIDException("解析失败, 请重新读卡");
                    }
                    else if (cloudReturnByte[0] == 0x06) {
                        throw new DKCloudIDException("该设备未授权, 请提供IMEI联系商家获取授权商家\r\n");
                    }
                    else if (cloudReturnByte[0] == 0x07) {
                        throw new DKCloudIDException("该设备已被禁用, 请联系商家");
                    }
                    else if (cloudReturnByte[0] == 0x08) {
                        throw new DKCloudIDException("该账号已被禁用, 请联系商家");
                    }
                    else if (cloudReturnByte[0] == 0x09) {
                        throw new DKCloudIDException("余额不足, 请联系商家充值\r\n");
                    }
                    else {
                        throw new DKCloudIDException("未知错误");
                    }
                }
                else if ((cloudReturnByte[0] == 0x04) && (cloudReturnByte.length > 300)) {
                    byte[] decrypted = new byte[cloudReturnByte.length - 3];
                    System.arraycopy(cloudReturnByte, 3, decrypted, 0, decrypted.length);

                    final IDCardData idCardData = new IDCardData(decrypted);
                    Log.d(TAG, "解析成功：" + idCardData.toString());
                    return idCardData;
                }

                msgReturnBytes = samVIdCard.transceive(cloudReturnByte);
                if (msgReturnBytes.length == 2) {
                    throw new CardNoResponseException("解析出错：" + String.format("%d", ((msgReturnBytes[0] & 0xff) << 8) | (msgReturnBytes[1] & 0xff) ));
                }

                Log.d(TAG, "向服务器发送数据：" + StringTool.byteHexToSting(msgReturnBytes));
                cloudReturnByte = dkCloudID.dkCloudTcpDataExchange(msgReturnBytes);
                Log.d(TAG, "接收到服务器数据：" + StringTool.byteHexToSting(cloudReturnByte));

                Log.d(TAG, String.format("正在解析%%%d", (int)((++schedule) * 100 / 4.0)));
                if ( mOnReceiveScheduleListener != null ) {
                    mOnReceiveScheduleListener.onReceiveSchedule((int)(schedule * 100 / 4.0));
                }
            }
        } catch (CardNoResponseException e) {
            throw e;
        }
        finally {
            if (dkCloudID != null) {
                dkCloudID.Close();
            }
        }
    }
}
