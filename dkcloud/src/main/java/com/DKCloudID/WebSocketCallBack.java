package com.DKCloudID;

import okio.ByteString;

public abstract class WebSocketCallBack {
    public abstract void onConnectError(Throwable t);

    public abstract void onClose();

    public abstract void onMessage(String text);

    public abstract void onMessage(ByteString bytes);

    public abstract void onOpen();
}
