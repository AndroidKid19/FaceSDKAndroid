package com.dk.uartnfc;

import com.Exception.CardNoResponseException;
import com.Exception.DeviceNoResponseException;
import com.Tool.UtilTool;

import java.util.Arrays;

import static com.DKCloudID.ClientDispatcher.SAM_V_APDU_COM;
import static com.DKCloudID.ClientDispatcher.SAM_V_FRAME_START_CODE;

public class SamVIdCard {
    private SerialManager serialManager;
    private byte[] initData;

    public SamVIdCard(SerialManager serialManager, byte[] initData) {
        this.serialManager = serialManager;
        this.initData = initData;
    }

    /**
     * 获取云解码初始化数据
     * @return         云解码初始化数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] getSamVInitData() throws CardNoResponseException {
        if (initData == null) {
            throw new CardNoResponseException("初始化数据为空");
        }
        return initData;
    }

    /**
     * cpu卡指令传输，同步阻塞方式，注意：不能在蓝牙初始化的线程里运行
     * @param data     发送的数据
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] transceive(byte[] data) throws CardNoResponseException {
        synchronized(this) {
            if (data == null || data.length == 0) {
                throw new CardNoResponseException("数据不能为null");
            }

            //将数据发送给NFC模块
            byte[] bytes = new byte[data.length + 5];
            int cmdLen = data.length + 1;
            bytes[0] = SAM_V_FRAME_START_CODE;
            bytes[1] = (byte)((cmdLen & 0xff00) >> 8);
            bytes[2] = (byte)(cmdLen & 0x00ff);
            bytes[3] = SAM_V_APDU_COM;
            System.arraycopy(data, 0, bytes, 4, data.length);
            bytes[bytes.length - 1] = UtilTool.bcc_check( bytes );
            final byte[] sendApduBytes = bytes;
            try {
                byte[] nfc_return_bytes = serialManager.sendWithReturn(sendApduBytes, 2000);
                verify(nfc_return_bytes);
                byte[] apduBytes = Arrays.copyOfRange( nfc_return_bytes, 4, nfc_return_bytes.length - 1 );
                return apduBytes;
            } catch (DeviceNoResponseException e) {
                e.printStackTrace();
                throw new CardNoResponseException("读取身份证数据失败，请不要移动身份证");
            }
        }
    }

    public static boolean verify(byte[] data)  throws CardNoResponseException{
        //数据长度校验
        if ( data.length < 6 ) {
            throw new CardNoResponseException( "数据长度错误" );
        }

        //和校验
        byte bcc_sum = 0;
        for ( int i=0; i<data.length - 1; i++ ) {
            bcc_sum ^= data[i];
        }
        if ( bcc_sum != data[data.length - 1] ) {
            System.out.println("和校验失败");
            throw new CardNoResponseException( "和校验失败" );
        }

        return true;
    }
}
