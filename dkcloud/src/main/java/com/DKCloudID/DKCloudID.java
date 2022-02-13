package com.DKCloudID;

import com.Exception.DKCloudIDException;
import com.Tool.UtilTool;

public class DKCloudID {
    private static final String TAG = "DKCloudID";

    static private boolean isIp1OK = true;
    private String current_ip = ip1;
    private final static String ip1 = "wss://gtm.dkcloudid.cn/wss";        //"ws://yjm1.dkcloudid.cn:8899/ws";
    private final static String ip2 = "wss://gtm.dkcloudid.cn/wss";         //"ws://www.dkcloudid.cn:8899/ws";
    private WebSocketHandler client;
    private boolean closed = false;

    public DKCloudID (){
        //创建一个客户端socket
        client = new WebSocketHandler();
        current_ip = isIp1OK ? ip1 : ip2;

        try {
            client.connect(current_ip, 2000);
        }catch (Exception e) {
            //连接服务器失败
            System.err.println("连接服务器失败：" + current_ip);
            e.printStackTrace();

            //连接到备用服务器
            client = new WebSocketHandler();
            try {
                client.connect(current_ip, 2000);
            }catch (Exception e2) {
                Close();
                //连接备用服务器失败
                System.err.println("连接服务器失败：" + ip2);
                e.printStackTrace();
                isIp1OK = !isIp1OK;
                return;
            }
        }

        isIp1OK = !isIp1OK;
    }

    /**
     * 获取client连接的状态
     * @return true - 已经连接， false - 已经断开
     */
    public boolean isConnected() {
        return client.getStatus() == ConnectStatus.Open;
    }

    /**
     * 使用TCP与云解析服务器进行数据交换，同步阻塞方式，必须在子线程中运行
     * @param initData 读卡模块发出来的数据
     * @return 服务器返回的数据
     */
    public byte[] dkCloudTcpDataExchange(byte[] initData) throws DKCloudIDException {
        if ( (initData == null) || closed ) {
            return null;
        }

        //发送解析请求
        byte[] rspBytes = SendPacket(initData);
        if (rspBytes == null || rspBytes.length < 3) {
            return null;
        }

        int len = UtilTool.byteToShort(rspBytes);
        if (len != rspBytes.length - 2) {
            throw new DKCloudIDException("Receive data len error");
        }

        byte[] returnBytes = new byte[len];
        System.arraycopy(rspBytes, 2, returnBytes, 0, len);

        return returnBytes;
    }

    // send packet to Server
    private byte[] SendPacket( byte[] res )  throws DKCloudIDException {
        byte[] headLen = UtilTool.shortToByte((short) res.length);
        byte[] body = UtilTool.mergeByte(headLen, res, 0, res.length);
        //body[2] = 0x01;
        int timeout = body.length > 1000 ? 3000 : 3000;
        return client.send(body, timeout);
    }

    // close the tcp connection
    public void Close() {
        if ( this.closed ) {
            return;
        }

        this.closed = true;
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}