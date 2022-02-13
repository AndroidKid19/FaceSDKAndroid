package com.baidu.facesdklibrary.model;

import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;

public class RecognizeOption {
    /**
     * 特征类型
     * 模型生活照模型
     */
    public BDFaceSDKCommon.FeatureType featureType =
            BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO;

    /**
     *
     */
    public float threshold = 0.9f;

    /**
     * M:N 最大识别人脸数
     */
    public int topNum = 1;

    /**
     *
     */
    public boolean isPercent = true;

    /**
     * 识别错误帧数
     */
    public int errorNum = 2;
}
