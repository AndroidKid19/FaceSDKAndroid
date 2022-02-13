package com.DKCloudID;


import androidx.annotation.Nullable;

import com.Exception.DKCloudIDException;
import com.Tool.StringTool;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketHandler extends WebSocketListener {
    private static final String TAG = "WebSocketHandler ";

    private String wsUrl;

    private WebSocket webSocket;

    private ConnectStatus status;

    private OkHttpClient client;

    private onClosedListener mOnClosedListener;
    private onConnectListener mOnConnectListener;
    private onReadListener mOnReadListener;

    public WebSocketHandler() {
    }

    public WebSocketHandler(String wsUrl) {
        this.wsUrl = wsUrl;
    }

//    private static WebSocketHandler INST;
//
//    public static WebSocketHandler getInstance(String url) {
//        if (INST == null) {
//            synchronized (WebSocketHandler.class) {
//                INST = new WebSocketHandler(url);
//            }
//        }
//
//        return INST;
//    }

    public interface onConnectListener {
        void onConnect(boolean isSuc, String err);
    }

    public interface onClosedListener {
        void onClose(boolean isSuc);
    }

    public interface onReadListener {
        void onRead(boolean isSuc, byte[] bytes);
    }

    public ConnectStatus getStatus() {
        return status;
    }

    public void connect(onConnectListener listener) {
        mOnConnectListener = listener;

        //构造request对象
        Request request = new Request.Builder()
                .url(wsUrl)
                .build();

        client = new OkHttpClient.Builder()
                .connectTimeout(500, TimeUnit.MILLISECONDS)//设置连接超时时间
                .readTimeout(2000, TimeUnit.MILLISECONDS)//设置读取超时时间
                .build();

        webSocket = client.newWebSocket(request, this);
        status = ConnectStatus.Connecting;
    }

    public boolean connect() throws DKCloudIDException {
        return connect(wsUrl, 600);
    }

    public boolean connect(String url, int timeout) throws DKCloudIDException {
        final String[] errString = new String[1];
        final boolean[] isCmdRunSucFlag = {false};

        final Semaphore semaphore = new Semaphore(0);
        errString[0] = null;

        this.wsUrl = url;

        connect(new onConnectListener() {
            @Override
            public void onConnect(boolean isSuc, String err) {
                if (isSuc) {
                    isCmdRunSucFlag[0] = true;
                }
                else {
                    isCmdRunSucFlag[0] = false;
                    errString[0] = err;
                }
                semaphore.release();
            }
        });

        try {
            if ( !semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS) ) {
                throw new DKCloudIDException(errString[0]);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new DKCloudIDException(errString[0]);
        }

        return isCmdRunSucFlag[0];
    }

    public void reConnect() {
        if (webSocket != null) {
            webSocket = client.newWebSocket(webSocket.request(), this);
        }
    }

    public void send(String text) {
        if (webSocket != null) {
            log("send： " + text);
            webSocket.send(text);
        }
    }

    public void send(byte[] cmd, onReadListener listener) {
        mOnReadListener = listener;

        if (webSocket != null) {
            log("send： " + StringTool.byteHexToSting(cmd));
            ByteString cmdBytes = new ByteString(cmd);
            webSocket.send(cmdBytes);
        }
    }

    public byte[] send(byte[] cmd, int timeout) throws DKCloudIDException{
        synchronized(this) {
            if (cmd == null || cmd.length == 0) {
                throw new DKCloudIDException("数据不能为null");
            }

            final byte[][] returnBytes = new byte[1][1];
            final boolean[] isCmdRunSucFlag = {false};

            final Semaphore semaphore = new Semaphore(0);
            returnBytes[0] = null;

            send(cmd, new onReadListener() {
                @Override
                public void onRead(boolean isCmdRunSuc, byte[] bytApduRtnData) {
                    if (isCmdRunSuc) {
                        returnBytes[0] = bytApduRtnData;
                        isCmdRunSucFlag[0] = true;
                    } else {
                        returnBytes[0] = null;
                        isCmdRunSucFlag[0] = false;
                    }
                    semaphore.release();
                }
            });

            try {
                semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new DKCloudIDException("Websocket read timeout");
            }
            if (!isCmdRunSucFlag[0]) {
                throw new DKCloudIDException("Websocket read timeout");
            }
            return returnBytes[0];
        }
    }

    public void cancel() {
        if (webSocket != null) {
            webSocket.cancel();
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, null);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        log("onOpen");
        this.status = ConnectStatus.Open;
        if (mSocketIOCallBack != null) {
            mSocketIOCallBack.onOpen();
        }

        if (mOnConnectListener != null) {
            mOnConnectListener.onConnect(true, null);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        log("onMessage: " + text);
        if (mSocketIOCallBack != null) {
            mSocketIOCallBack.onMessage(text);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
        log("onMessage: " + bytes.hex());
        if (mSocketIOCallBack != null) {
            mSocketIOCallBack.onMessage(bytes);
        }

        if (mOnReadListener != null) {
            mOnReadListener.onRead(true, bytes.toByteArray());
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        this.status = ConnectStatus.Closing;
        log("onClosing");
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        log("onClosed");
        this.status = ConnectStatus.Closed;
        if (mSocketIOCallBack != null) {
            mSocketIOCallBack.onClose();
        }

        if (mOnClosedListener != null) {
            mOnClosedListener.onClose(true);
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        log("onFailure: " + t.toString());
        t.printStackTrace();
        this.status = ConnectStatus.Canceled;
        if (mSocketIOCallBack != null) {
            mSocketIOCallBack.onConnectError(t);
        }

        if (mOnConnectListener != null) {
            mOnConnectListener.onConnect(false, t.toString());
        }
    }


    private WebSocketCallBack mSocketIOCallBack;

    public void setSocketIOCallBack(WebSocketCallBack callBack) {
        mSocketIOCallBack = callBack;
    }

    public void removeSocketIOCallBack() {
        mSocketIOCallBack = null;
    }

    private void log(String s) {
        //Log.d(TAG, s);
    }
}
