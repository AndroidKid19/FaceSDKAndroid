package com.baidu.facesdklibrary.callback;

import com.baidu.facesdklibrary.model.DetectionErrorType;
import com.baidu.idl.main.facesdk.model.BDFaceDriverMonitorInfo;

public interface DriveCallback {
    void onDetectionError(DetectionErrorType detectionErrorType);
    void onSuccess(BDFaceDriverMonitorInfo monitorInfo);
}
