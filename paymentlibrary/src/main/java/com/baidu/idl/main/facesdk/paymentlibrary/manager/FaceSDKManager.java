package com.baidu.idl.main.facesdk.paymentlibrary.manager;

import android.content.Context;
import android.util.Log;

import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.FaceDarkEnhance;
import com.baidu.idl.main.facesdk.FaceDetect;
import com.baidu.idl.main.facesdk.FaceFeature;
import com.baidu.idl.main.facesdk.FaceGaze;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.FaceLive;
import com.baidu.idl.main.facesdk.ImageIllum;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceDetectListConf;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.BDFaceSDKConfig;
import com.baidu.idl.main.facesdk.model.Feature;
import com.baidu.idl.main.facesdk.paymentlibrary.callback.FaceDetectCallBack;
import com.baidu.idl.main.facesdk.paymentlibrary.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.paymentlibrary.model.GlobalSet;
import com.baidu.idl.main.facesdk.paymentlibrary.model.LivenessModel;
import com.baidu.idl.main.facesdk.paymentlibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.paymentlibrary.utils.ToastUtils;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.db.DBManager;
import com.example.datalibrary.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.baidu.idl.main.facesdk.paymentlibrary.model.GlobalSet.FEATURE_SIZE;


public class FaceSDKManager {

    public static final int SDK_MODEL_LOAD_SUCCESS = 0;
    public static final int SDK_UNACTIVATION = 1;
    public static final int SDK_UNINIT = 2;
    public static final int SDK_INITING = 3;
    public static final int SDK_INITED = 4;
    public static final int SDK_INIT_FAIL = 5;
    public static final int SDK_INIT_SUCCESS = 6;

    private float threholdScore;

    private List<Boolean> mRgbLiveList=new ArrayList<>();
    private List<Boolean> mNirLiveList=new ArrayList<>();
    private float mRgbLiveScore;
    private float mNirLiveScore;
    private int mLastFaceId;

    public static volatile int initStatus = SDK_UNACTIVATION;
    public static volatile boolean initModelSuccess = false;
    private FaceAuth faceAuth;
    private FaceDetect faceDetect;
    private FaceFeature faceFeature;
    private FaceLive faceLiveness;

    private FaceGaze faceGaze;

    private ExecutorService es = Executors.newSingleThreadExecutor();
    private Future future;
    private ExecutorService es2 = Executors.newSingleThreadExecutor();
    private Future future2;
    private ExecutorService es3 = Executors.newSingleThreadExecutor();
    private Future future3;

    private FaceDetect faceDetectNir;
    private float[] scores;
    public static boolean isDetectMask = false;
    private ImageIllum imageIllum;
    private FaceDarkEnhance faceDarkEnhance;
    private static int failNumber = 0;
    private static int faceId = 0;
    private static int lastFaceId = 0;
    private static LivenessModel faceAdoptModel;
    private boolean isFail = false;
    private long trackTime;
    private boolean isPush;


    private FaceSDKManager() {
        faceAuth = new FaceAuth();
        setActiveLog();
        faceAuth.setCoreConfigure(BDFaceSDKCommon.BDFaceCoreRunMode.BDFACE_LITE_POWER_LOW, 2);
    }

    public void setActiveLog(){
        if (faceAuth != null){
            if (SingleBaseConfig.getBaseConfig().isLog()){
                faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_TYPE_ALL, 1);
            }else {
                faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_TYPE_ALL, 0);
            }
        }
    }

    private static class HolderClass {
        private static final FaceSDKManager instance = new FaceSDKManager();
    }

    public static FaceSDKManager getInstance() {
        return HolderClass.instance;
    }

    public FaceDetect getFaceDetect() {
        return faceDetect;
    }

    public FaceFeature getFaceFeature() {
        return faceFeature;
    }

    public FaceLive getFaceLiveness() {
        return faceLiveness;
    }

    public ImageIllum getImageIllum() {
        return imageIllum;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param context
     * @param listener
     */
    public void initModel(final Context context, final SdkInitListener listener) {
//      ToastUtils.toast(context, "????????????????????????????????????");

        // ????????????
        BDFaceInstance bdFaceInstance = new BDFaceInstance();
        bdFaceInstance.creatInstance();
        faceDetect = new FaceDetect(bdFaceInstance);
        // ????????????
        BDFaceInstance IrBdFaceInstance = new BDFaceInstance();
        IrBdFaceInstance.creatInstance();
        faceDetectNir = new FaceDetect(IrBdFaceInstance);
        // ????????????
        faceFeature = new FaceFeature();
        // ????????????
        faceDarkEnhance = new FaceDarkEnhance();

        faceLiveness = new FaceLive();
        faceGaze = new FaceGaze();
        // ??????
        imageIllum = new ImageIllum();
        initConfig();

        final long startInitModelTime = System.currentTimeMillis();

        mRgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        mNirLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();

        faceDetect.initModel(context,
                GlobalSet.DETECT_VIS_MODEL,
                GlobalSet.ALIGN_TRACK_MODEL,
                BDFaceSDKCommon.DetectType.DETECT_VIS,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_FAST,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initModel(context,
                GlobalSet.DETECT_VIS_MODEL,
                GlobalSet.ALIGN_RGB_MODEL, BDFaceSDKCommon.DetectType.DETECT_VIS,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + response);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetectNir.initModel(context,
                GlobalSet.DETECT_NIR_MODE,
                GlobalSet.ALIGN_NIR_MODEL, BDFaceSDKCommon.DetectType.DETECT_NIR,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_NIR_ACCURATE,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + response);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initQuality(context,
                GlobalSet.BLUR_MODEL,
                GlobalSet.OCCLUSION_MODEL, new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initAttrEmo(context, GlobalSet.ATTRIBUTE_MODEL, GlobalSet.EMOTION_MODEL, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code != 0 && listener != null) {
                    listener.initModelFail(code, response);
                }
            }
        });
        faceDetect.initBestImage(context, GlobalSet.BEST_IMAGE, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code != 0 && listener != null) {
                    listener.initModelFail(code, response);
                }
            }
        });
        // ?????????????????????
        faceDarkEnhance.initFaceDarkEnhance(context,
                GlobalSet.DARK_ENHANCE_MODEL, new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceLiveness.initModel(context,
                GlobalSet.LIVE_VIS_MODEL,
//                GlobalSet.LIVE_VIS_2DMASK_MODEL,
//                GlobalSet.LIVE_VIS_HAND_MODEL,
//                GlobalSet.LIVE_VIS_REFLECTION_MODEL,
                "", "", "",
                GlobalSet.LIVE_NIR_MODEL,
                GlobalSet.LIVE_DEPTH_MODEL,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + response);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initBestImage(context, GlobalSet.BEST_IMAGE, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code != 0 && listener != null) {
                    listener.initModelFail(code, response);
                }
            }
        });

        // ???????????????????????????
        faceFeature.initModel(context,
                GlobalSet.RECOGNIZE_IDPHOTO_MODEL,
                GlobalSet.RECOGNIZE_VIS_MODEL,
                GlobalSet.RECOGNIZE_NIR_MODEL,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        long endInitModelTime = System.currentTimeMillis();
//                        LogUtils.e(TIME_TAG, "init model time = " + (endInitModelTime - startInitModelTime));
                        if (code != 0) {
//                            ToastUtils.toast(context, "??????????????????,??????????????????");
                            if (listener != null) {
                                listener.initModelFail(code, response);
                            }
                        } else {
                            initStatus = SDK_MODEL_LOAD_SUCCESS;
                            // ??????????????????????????????????????????
                            if (listener != null) {
                                listener.initModelSuccess();
                            }
                        }
                    }
                });

    }


    /**
     * ???????????????
     *
     * @return
     */
    public boolean initConfig() {
        if (faceDetect != null) {
            BDFaceSDKConfig config = new BDFaceSDKConfig();
            // TODO: ??????????????????????????????????????????1,??????????????????????????????
            config.maxDetectNum = 2;

            // TODO: ?????????80px??????????????????30px????????????????????????????????????????????????????????????????????????????????????
            config.minFaceSize = SingleBaseConfig.getBaseConfig().getMinimumFace();
            // TODO: ?????????0.5??????????????????0.3?????????
            config.notRGBFaceThreshold = SingleBaseConfig.getBaseConfig().getFaceThreshold();
            config.notNIRFaceThreshold = SingleBaseConfig.getBaseConfig().getFaceThreshold();

            // ???????????????????????????????????????
            config.isAttribute = SingleBaseConfig.getBaseConfig().isAttribute();
            // TODO: ?????????????????????????????????????????????????????????????????????????????????????????????????????????
            config.isCheckBlur = config.isOcclusion
                    = config.isIllumination = config.isHeadPose
                    = SingleBaseConfig.getBaseConfig().isQualityControl();

            faceDetect.loadConfig(config);

            return true;
        }
        return false;
    }

    public void initDataBases(Context context) {
        ToastUtils.toast(context , "??????????????????");
        isPush = false;
        emptyFrame();
        // ??????????????????
        DBManager.getInstance().init(context);
        // ???????????????????????????
        FaceSDKManager.getInstance().initPush(context);
    }
    /**
     * ???????????????????????????????????????????????????????????????????????????????????????id+feature
     */
    public void initPush(final Context context) {

        if (future3 != null && !future3.isDone()) {
            future3.cancel(true);
        }

        future3 = es3.submit(new Runnable() {
            @Override
            public void run() {
                synchronized (FaceApi.getInstance().getAllUserList()){
                    FaceSDKManager.getInstance().getFaceFeature().featurePush(FaceApi.getInstance().getAllUserList());
                    ToastUtils.toast(context , "?????????????????????");
                    isPush = true;
                }
            }
        });
    }
    /**
     * ??????-??????-??????-??????????????????
     *
     * @param rgbData            ?????????YUV ?????????
     * @param nirData            ??????YUV ?????????
     * @param depthData          ??????depth ?????????
     * @param srcHeight          ?????????YUV ?????????-??????
     * @param srcWidth           ?????????YUV ?????????-??????
     * @param liveCheckMode      ???????????????????????????????????????1?????????RGB?????????2?????????RGB+NIR?????????3?????????RGB+Depth?????????4???
     * @param faceDetectCallBack
     */
    public void onDetectCheck(final byte[] rgbData,
                              final byte[] nirData,
                              final byte[] depthData,
                              final int srcHeight,
                              final int srcWidth,
                              final int liveCheckMode,
                              final FaceDetectCallBack faceDetectCallBack) {

        // ??????????????????+1???N?????????3???
        onDetectCheck(rgbData, nirData, depthData, srcHeight, srcWidth, liveCheckMode, 3, faceDetectCallBack);
    }

    private void setFail(LivenessModel livenessModel) {
        Log.e("faceId", livenessModel.getFaceInfo().faceID + "");
if (failNumber >= 2) {
    faceId = livenessModel.getFaceInfo().faceID;
    faceAdoptModel = livenessModel;
    trackTime = System.currentTimeMillis();
    isFail = false;
    faceAdoptModel.setMultiFrame(true);
} else {
    failNumber += 1;
    faceId = 0;
    faceAdoptModel = null;
    isFail = true;
    livenessModel.setMultiFrame(true);

}
    }

    public void emptyFrame() {
        failNumber = 0;
        faceId = 0;
        isFail = false;
        trackTime = 0;
        faceAdoptModel = null;
    }



    /**
     * ??????-??????-??????- ?????????
     *
     * @param rgbData            ?????????YUV ?????????
     * @param nirData            ??????YUV ?????????
     * @param depthData          ??????depth ?????????
     * @param srcHeight          ?????????YUV ?????????-??????
     * @param srcWidth           ?????????YUV ?????????-??????
     * @param liveCheckMode      ???????????????????????????????????????1?????????RGB?????????2?????????RGB+NIR?????????3?????????RGB+Depth?????????4???
     * @param featureCheckMode   ???????????????????????????????????????1????????????????????????2?????????????????????+1???N?????????3??????
     * @param faceDetectCallBack
     */
    public void onDetectCheck(final byte[] rgbData,
                              final byte[] nirData,
                              final byte[] depthData,
                              final int srcHeight,
                              final int srcWidth,
                              final int liveCheckMode,
                              final int featureCheckMode,
                              final FaceDetectCallBack faceDetectCallBack) {

        if (future != null && !future.isDone()) {
            return;
        }

        future = es.submit(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                // ??????????????????????????????
                LivenessModel livenessModel = new LivenessModel();
                // ???????????????????????????????????????YUV??????????????????????????????BGR
                // TODO: ??????????????????????????????????????????????????????????????????????????????
                BDFaceImageInstance rgbInstance;
                if (SingleBaseConfig.getBaseConfig().getType() == 4
                        && SingleBaseConfig.getBaseConfig().getCameraType() == 6) {
                    rgbInstance = new BDFaceImageInstance(rgbData, srcHeight,
                            srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_RGB,
                            SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                            SingleBaseConfig.getBaseConfig().getMirrorDetectRGB());
                } else {
                    rgbInstance = new BDFaceImageInstance(rgbData, srcHeight,
                            srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                            SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                            SingleBaseConfig.getBaseConfig().getMirrorDetectRGB());
                }
                // TODO: getImage() ??????????????????,??????????????????????????????????????????image view ??????????????????
                BDFaceImageInstance rgbInstanceOne;
                // ??????????????????
                if (SingleBaseConfig.getBaseConfig().isDarkEnhance()) {
                    rgbInstanceOne = faceDarkEnhance.faceDarkEnhance(rgbInstance);
                    rgbInstance.destory();
                }else {
                    rgbInstanceOne = rgbInstance;
                }
                // TODO: getImage() ??????????????????,??????????????????????????????????????????image view ??????????????????
                livenessModel.setBdFaceImageInstance(rgbInstanceOne.getImage());

                // ???????????????????????????????????????
                long startDetectTime = System.currentTimeMillis();

                // ??????????????????????????????????????????????????????????????????????????????????????????
                FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                        .track(BDFaceSDKCommon.DetectType.DETECT_VIS,
                                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_FAST, rgbInstanceOne);
                livenessModel.setRgbDetectDuration(System.currentTimeMillis() - startDetectTime);
//                LogUtils.e(TIME_TAG, "detect vis time = " + livenessModel.getRgbDetectDuration());

                // ??????????????????
                if (faceInfos != null && faceInfos.length > 0) {
                    livenessModel.setTrackFaceInfo(faceInfos);
                    livenessModel.setFaceInfo(faceInfos[0]);
                    if (lastFaceId != faceInfos[0].faceID) {
                        lastFaceId = faceInfos[0].faceID;
                    }

                    if (System.currentTimeMillis() - trackTime < 3000 && faceId == faceInfos[0].faceID) {
                        faceAdoptModel.setMultiFrame(true);
                        rgbInstanceOne.destory();
                        if (faceDetectCallBack != null && faceAdoptModel != null) {
                            //                            faceAdoptModel.setAllDetectDuration(System.currentTimeMillis() - startTime);
                            faceDetectCallBack.onFaceDetectDarwCallback(livenessModel);
                            faceDetectCallBack.onFaceDetectCallback(faceAdoptModel);

                        }
                        return;
                    }
                    if (faceAdoptModel != null) {
                        faceAdoptModel.setMultiFrame(false);
                    }
                    faceId = 0;
                    faceAdoptModel = null;
                    if (!isFail) {
                        failNumber = 0;
                    }

                    livenessModel.setTrackStatus(1);
                    livenessModel.setLandmarks(faceInfos[0].landmarks);
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectDarwCallback(livenessModel);
                    }

                    onLivenessCheck(rgbInstanceOne, nirData, depthData, srcHeight,
                            srcWidth, livenessModel.getLandmarks(),
                            livenessModel, startTime, liveCheckMode, featureCheckMode,
                            faceDetectCallBack, faceInfos);
                } else {
                    emptyFrame();
                    // ???????????????????????????????????????????????????????????????????????????
                    rgbInstanceOne.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(null);
                        faceDetectCallBack.onFaceDetectDarwCallback(null);
                        faceDetectCallBack.onTip(0, "??????????????????");
                    }
                }
            }
        });
    }


    /**
     * ??????????????????????????????????????????????????????
     * ???????????? SingleBaseConfig.getBaseConfig().setQualityControl(true);?????????true???
     * ?????????  FaceSDKManager.getInstance().initConfig() ???????????????????????????
     *
     * @param livenessModel
     * @param faceDetectCallBack
     * @return
     */
    public boolean onQualityCheck(final LivenessModel livenessModel,
                                  final FaceDetectCallBack faceDetectCallBack) {

        if (!SingleBaseConfig.getBaseConfig().isQualityControl()) {
            return true;
        }

        if (livenessModel != null && livenessModel.getFaceInfo() != null) {

            // ????????????
            if (Math.abs(livenessModel.getFaceInfo().yaw) > SingleBaseConfig.getBaseConfig().getYaw()) {
                faceDetectCallBack.onTip(-1, "?????????????????????????????????");
                return false;
            } else if (Math.abs(livenessModel.getFaceInfo().roll) > SingleBaseConfig.getBaseConfig().getRoll()) {
                faceDetectCallBack.onTip(-1, "???????????????????????????????????????????????????");
                return false;
            } else if (Math.abs(livenessModel.getFaceInfo().pitch) > SingleBaseConfig.getBaseConfig().getPitch()) {
                faceDetectCallBack.onTip(-1, "?????????????????????????????????");
                return false;
            }

            // ??????????????????
            float blur = livenessModel.getFaceInfo().bluriness;
            if (blur > SingleBaseConfig.getBaseConfig().getBlur()) {
                faceDetectCallBack.onTip(-1, "????????????");
                return false;
            }

            // ??????????????????
            float illum = livenessModel.getFaceInfo().illum;
            if (illum < SingleBaseConfig.getBaseConfig().getIllumination()) {
                faceDetectCallBack.onTip(-1, "?????????????????????");
                return false;
            }


            // ??????????????????
            if (livenessModel.getFaceInfo().occlusion != null) {
                BDFaceOcclusion occlusion = livenessModel.getFaceInfo().occlusion;

                if (occlusion.leftEye > SingleBaseConfig.getBaseConfig().getLeftEye()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else if (occlusion.rightEye > SingleBaseConfig.getBaseConfig().getRightEye()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else if (occlusion.nose > SingleBaseConfig.getBaseConfig().getNose()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else if (occlusion.mouth > SingleBaseConfig.getBaseConfig().getMouth()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else if (occlusion.leftCheek > SingleBaseConfig.getBaseConfig().getLeftCheek()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else if (occlusion.rightCheek > SingleBaseConfig.getBaseConfig().getRightCheek()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else if (occlusion.chin > SingleBaseConfig.getBaseConfig().getChinContour()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ??????????????????
     *
     * @param livenessModel
     * @param faceDetectCallBack
     * @return
     */
    public boolean onBestImageCheck(LivenessModel livenessModel,
                                    FaceDetectCallBack faceDetectCallBack) {
        if (!SingleBaseConfig.getBaseConfig().isUsingBestImage()) {
            return true;
        }

        if (livenessModel != null && livenessModel.getFaceInfo() != null) {
            float bestImageScore = livenessModel.getFaceInfo().bestImageScore;
            if (bestImageScore < SingleBaseConfig.getBaseConfig().getBestImageScore()) {
                faceDetectCallBack.onTip(-1, "?????????????????????");
                return false;
            }
        }
        return true;
    }


    /**
     * ??????-??????-?????????????????????
     *
     * @param rgbInstance        ???????????????????????????
     * @param nirData            ??????YUV ?????????
     * @param depthData          ??????depth ?????????
     * @param srcHeight          ?????????YUV ?????????-??????
     * @param srcWidth           ?????????YUV ?????????-??????
     * @param landmark           ?????????????????????????????????72????????????
     * @param livenessModel      ????????????????????????
     * @param startTime          ??????????????????
     * @param liveCheckMode      ???????????????????????????????????????1?????????RGB?????????2?????????RGB+NIR?????????3?????????RGB+Depth?????????4???
     * @param featureCheckMode   ???????????????????????????????????????1????????????????????????2?????????????????????+1???N?????????3??????
     * @param faceDetectCallBack
     */
    public void onLivenessCheck(final BDFaceImageInstance rgbInstance,
                                final byte[] nirData,
                                final byte[] depthData,
                                final int srcHeight,
                                final int srcWidth,
                                final float[] landmark,
                                final LivenessModel livenessModel,
                                final long startTime,
                                final int liveCheckMode,
                                final int featureCheckMode,
                                final FaceDetectCallBack faceDetectCallBack,
                                final FaceInfo[] fastFaceInfos) {

        if (future2 != null && !future2.isDone()) {
            // ???????????????????????????????????????????????????????????????????????????
            rgbInstance.destory();
            return;
        }

        future2 = es2.submit(new Runnable() {
            private FaceInfo[] faceInfos;

            @Override
            public void run() {
                BDFaceDetectListConf bdFaceDetectListConfig = new BDFaceDetectListConf();
                bdFaceDetectListConfig.usingQuality = bdFaceDetectListConfig.usingHeadPose
                        = SingleBaseConfig.getBaseConfig().isQualityControl();
                FaceInfo[] faceInfos = FaceSDKManager.getInstance()
                        .getFaceDetect()
                        .detect(BDFaceSDKCommon.DetectType.DETECT_VIS,
                                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
                                rgbInstance,
                                fastFaceInfos, bdFaceDetectListConfig);
                // ??????????????????????????????
                if (faceInfos != null && faceInfos.length > 0) {
                    faceInfos[0].faceID = livenessModel.getFaceInfo().faceID;
                    livenessModel.setFaceInfo(faceInfos[0]);
                    livenessModel.setTrackStatus(2);
                    livenessModel.setLandmarks(faceInfos[0].landmarks);

                    if (mLastFaceId != fastFaceInfos[0].faceID) {
                        mLastFaceId = fastFaceInfos[0].faceID;
                        mRgbLiveList.clear();
                        mNirLiveList.clear();
                    }
                } else {
                    rgbInstance.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(livenessModel);
                    }
                    return;
                }
                // ??????????????????
                if (SingleBaseConfig.getBaseConfig().isBestImage() &&
                        !onBestImageCheck(livenessModel, faceDetectCallBack)) {
                    livenessModel.setQualityCheck(false);
                    rgbInstance.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(livenessModel);
                    }
                    return;
                }
                // ?????????????????????,??????BDFaceImageInstance???????????????
                if (!onQualityCheck(livenessModel, faceDetectCallBack)) {
                    livenessModel.setQualityCheck(false);
                    rgbInstance.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(livenessModel);
                    }
                    return;
                }
                livenessModel.setQualityCheck(true);

                // ??????LivenessConfig liveCheckMode ????????????????????????????????????0?????????RGB?????????1?????????RGB+NIR?????????2?????????RGB+Depth?????????3?????????RGB+NIR+Depth?????????4???

                // TODO ????????????
                float rgbScore = -1;
                if (liveCheckMode != 0) {
                    long startRgbTime = System.currentTimeMillis();
                    rgbScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                            BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_RGB,
                            rgbInstance, faceInfos[0].landmarks);
                    mRgbLiveList.add(rgbScore > mRgbLiveScore);
                    while (mRgbLiveList.size() > 6) {
                        mRgbLiveList.remove(0);
                    }
                    if (mRgbLiveList.size() > 2) {
                        int rgbSum = 0;
                        for (Boolean b : mRgbLiveList) {
                            if (b) {
                                rgbSum++;
                            }
                        }
                        if (1.0 * rgbSum / mRgbLiveList.size() > 0.6) {
                            if (rgbScore < mRgbLiveScore) {
                                rgbScore = mRgbLiveScore + (1 - mRgbLiveScore) * new Random().nextFloat();
                            }
                        } else {
                            if (rgbScore > mRgbLiveScore) {
                                rgbScore = new Random().nextFloat() * mRgbLiveScore;
                            }
                        }
                    }
                    livenessModel.setRgbLivenessScore(rgbScore);
                    livenessModel.setRgbLivenessDuration(System.currentTimeMillis() - startRgbTime);
                }

                float nirScore = -1;
                FaceInfo[] faceInfosIr = null;
                BDFaceImageInstance nirInstance = null;
                if (liveCheckMode == 2 || liveCheckMode == 4 && nirData != null) {
                    // ???????????????????????????????????????YUV-IR??????????????????????????????BGR
                    // TODO: ??????????????????????????????????????????????????????????????????????????????
                    nirInstance = new BDFaceImageInstance(nirData, srcHeight,
                            srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                            SingleBaseConfig.getBaseConfig().getNirDetectDirection(),
                            SingleBaseConfig.getBaseConfig().getMirrorDetectNIR());
                    livenessModel.setBdNirFaceImageInstance(nirInstance.getImage());

                    // ??????RGB??????????????????IR???????????????????????????????????????
                    long startIrDetectTime = System.currentTimeMillis();
                    BDFaceDetectListConf bdFaceDetectListConf = new BDFaceDetectListConf();
                    bdFaceDetectListConf.usingDetect = true;
                    faceInfosIr = faceDetectNir.detect(BDFaceSDKCommon.DetectType.DETECT_NIR,
                            BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_NIR_ACCURATE,
                            nirInstance, null, bdFaceDetectListConf);
                    bdFaceDetectListConf.usingDetect = false;
                    livenessModel.setIrLivenessDuration(System.currentTimeMillis() - startIrDetectTime);
//                    LogUtils.e(TIME_TAG, "detect ir time = " + livenessModel.getIrLivenessDuration());

                    if (faceInfosIr != null && faceInfosIr.length > 0) {
                        FaceInfo faceInfoIr = faceInfosIr[0];
                        long startNirTime = System.currentTimeMillis();
                        nirScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                                BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_NIR,
                                nirInstance, faceInfoIr.landmarks);
                        mNirLiveList.add(nirScore > mNirLiveScore);
                        while (mNirLiveList.size() > 6) {
                            mNirLiveList.remove(0);
                        }
                        if (mNirLiveList.size() > 2) {
                            int nirSum = 0;
                            for (Boolean b : mNirLiveList) {
                                if (b) {
                                    nirSum++;
                                }
                            }
                            if (1.0f * nirSum / mNirLiveList.size() > 0.6) {
                                if (nirScore < mNirLiveScore) {
                                    nirScore = mNirLiveScore + new Random().nextFloat() * (1 - mNirLiveScore);
                                }
                            } else {
                                if (nirScore > mNirLiveScore) {
                                    nirScore = new Random().nextFloat() * mNirLiveScore;
                                }
                            }
                        }
                        livenessModel.setIrLivenessScore(nirScore);
                        livenessModel.setIrLivenessDuration(System.currentTimeMillis() - startNirTime);
//                        LogUtils.e(TIME_TAG, "live ir time = " + livenessModel.getIrLivenessDuration());
                    }
                }

                float depthScore = -1;
                if (liveCheckMode == 3 || liveCheckMode == 4 && depthData != null) {
                    // TODO: ????????????????????????????????????????????????Atlas ????????????????????????400*640????????????????????????????????????,??????72 ????????????x ??????????????????80????????????
                    float[] depthLandmark = new float[faceInfos[0].landmarks.length];
                    BDFaceImageInstance depthInstance;
                    if (SingleBaseConfig.getBaseConfig().getCameraType() == 1) {
                        System.arraycopy(faceInfos[0].landmarks, 0, depthLandmark, 0, faceInfos[0].landmarks.length);
                        if (SingleBaseConfig.getBaseConfig().getCameraType() == 1) {
                            for (int i = 0; i < 144; i = i + 2) {
                                depthLandmark[i] -= 80;
                            }
                        }
                        depthInstance = new BDFaceImageInstance(depthData,
                                SingleBaseConfig.getBaseConfig().getDepthWidth(),
                                SingleBaseConfig.getBaseConfig().getDepthHeight(),
                                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_DEPTH,
                                0, 0);
                    } else {
                        depthInstance = new BDFaceImageInstance(depthData,
                                SingleBaseConfig.getBaseConfig().getDepthHeight(),
                                SingleBaseConfig.getBaseConfig().getDepthWidth(),
                                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_DEPTH,
                                0, 0);
                    }

                    // ???????????????????????????????????????Depth
                    long startDepthTime = System.currentTimeMillis();
                    if (SingleBaseConfig.getBaseConfig().getCameraType() == 1) {
                        depthScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                                BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_DEPTH,
                                depthInstance, depthLandmark);
                    } else {
                        depthScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                                BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_DEPTH,
                                depthInstance, faceInfos[0].landmarks);
                    }
                    livenessModel.setBdDepthFaceImageInstance(depthInstance.getImage());
                    livenessModel.setDepthLivenessScore(depthScore);
                    livenessModel.setDepthtLivenessDuration(System.currentTimeMillis() - startDepthTime);
//                    LogUtils.e(TIME_TAG, "live depth time = " + livenessModel.getDepthtLivenessDuration());
                    depthInstance.destory();
                }

                // TODO ????????????+????????????
                if (liveCheckMode == 0) {
                    onFeatureCheck(rgbInstance, faceInfos[0].landmarks, faceInfosIr,
                            nirInstance, livenessModel, featureCheckMode,
                            SingleBaseConfig.getBaseConfig().getActiveModel());
                } else {
                    if (liveCheckMode == 1 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()) {
                        onFeatureCheck(rgbInstance, faceInfos[0].landmarks, faceInfosIr,
                                nirInstance, livenessModel, featureCheckMode,
                                1);
                    } else if (liveCheckMode == 2 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                            && nirScore > SingleBaseConfig.getBaseConfig().getNirLiveScore()) {
                        onFeatureCheck(rgbInstance, faceInfos[0].landmarks, faceInfosIr, nirInstance,
                                livenessModel, featureCheckMode,
                                SingleBaseConfig.getBaseConfig().getActiveModel());
                    } else if (liveCheckMode == 3 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                            && depthScore > SingleBaseConfig.getBaseConfig().getDepthLiveScore()) {
                        onFeatureCheck(rgbInstance, faceInfos[0].landmarks, faceInfosIr, nirInstance,
                                livenessModel, featureCheckMode,
                                1);
                    } else if (liveCheckMode == 4 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                            && nirScore > SingleBaseConfig.getBaseConfig().getNirLiveScore()
                            && depthScore > SingleBaseConfig.getBaseConfig().getDepthLiveScore()) {
                        onFeatureCheck(rgbInstance, faceInfos[0].landmarks, faceInfosIr, nirInstance,
                                livenessModel, featureCheckMode,
                                SingleBaseConfig.getBaseConfig().getActiveModel());
                    }
                }
                // ????????????,??????????????????
                livenessModel.setAllDetectDuration(System.currentTimeMillis() - startTime);
//                LogUtils.e(TIME_TAG, "all process time = " + livenessModel.getAllDetectDuration());
                // ???????????????????????????????????????????????????????????????????????????
                rgbInstance.destory();
                if (nirInstance != null) {
                    nirInstance.destory();
                }
                // ????????????????????????
                if (faceDetectCallBack != null) {
                    faceDetectCallBack.onFaceDetectCallback(livenessModel);
                }
            }
        });
    }

    /**
     * ????????????-??????????????????
     *
     * @param rgbInstance      ???????????????????????????
     * @param landmark         ?????????????????????????????????72????????????
     * @param nirInstance      nir?????????
     * @param livenessModel    ????????????????????????
     * @param featureCheckMode ???????????????????????????????????????1????????????????????????2?????????????????????+1???N?????????3??????
     * @param featureType      ???????????????????????? ???????????????1?????????????????????2????????????????????????3??????
     */
    private void onFeatureCheck(BDFaceImageInstance rgbInstance,
                                float[] landmark,
                                FaceInfo[] faceInfos,
                                BDFaceImageInstance nirInstance,
                                LivenessModel livenessModel,
                                final int featureCheckMode,
                                final int featureType) {
        if (! isPush){
            return;
        }
        // ????????????????????????????????????
        if (featureCheckMode == 1) {
            return;
        }
        byte[] feature = new byte[512];
        if (featureType == 3) {
            // todo: ????????????????????????????????????????????????????????????????????????type??????????????????????????????0~1??????
            AtomicInteger atomicInteger = new AtomicInteger();
            FaceSDKManager.getInstance().getImageIllum().imageIllum(rgbInstance, atomicInteger);
            int illumScore = atomicInteger.get();
            if (illumScore > SingleBaseConfig.getBaseConfig().getIllumination()) {
                if (faceInfos != null) {
                    long startFeatureTime = System.currentTimeMillis();
                    float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                            BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_NIR, nirInstance,
                            faceInfos[0].landmarks, feature);
                    livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
                    livenessModel.setFeature(feature);

                    // ????????????
                    featureSearch(featureCheckMode, livenessModel, feature, featureSize,
                            BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_NIR);
                }

            } else {
                long startFeatureTime = System.currentTimeMillis();
                float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                        BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, rgbInstance, landmark, feature);
                livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
                livenessModel.setFeature(feature);
                // ????????????
                featureSearch(featureCheckMode, livenessModel, feature, featureSize,
                        BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
            }

        } else if (featureType == 2) {
            // ???????????????
            long startFeatureTime = System.currentTimeMillis();
            float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO, rgbInstance, landmark, feature);
            livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
            livenessModel.setFeature(feature);
            // ????????????
            featureSearch(featureCheckMode, livenessModel, feature, featureSize,
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO);

        } else {
            // ???????????????
            long startFeatureTime = System.currentTimeMillis();
            float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, rgbInstance, landmark, feature);
            livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
            livenessModel.setFeature(feature);
            // ????????????
            featureSearch(featureCheckMode, livenessModel, feature, featureSize,
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
        }

    }

    /**
     * ???????????????
     *
     * @param featureCheckMode ???????????????????????????????????????1????????????????????????2?????????????????????+1???N?????????3??????
     * @param livenessModel    ????????????????????????
     * @param feature          ?????????
     * @param featureSize      ????????????size
     * @param type             ??????????????????
     */
    private void featureSearch(final int featureCheckMode,
                               LivenessModel livenessModel,
                               byte[] feature,
                               float featureSize,
                               BDFaceSDKCommon.FeatureType type) {

        // ???????????????????????????????????????????????????
        if (featureCheckMode == 2) {
            livenessModel.setFeatureCode(featureSize);
            return;
        }
        // ??????????????????+???????????????search ??????
        if (featureSize == FEATURE_SIZE / 4) {
            // ??????????????????
            // TODO ????????????????????????????????????
            long startFeature = System.currentTimeMillis();
            ArrayList<Feature> featureResult =
                    FaceSDKManager.getInstance().getFaceFeature().featureSearch(feature,
                            type, 1, true);

            // TODO ??????top num = 1 ?????????????????????????????????????????????????????????????????????????????????num ???????????????
            if (featureResult != null && featureResult.size() > 0) {

                // ?????????????????????
                Feature topFeature = featureResult.get(0);
                // ???????????????????????????????????????????????????????????????????????????
                if (SingleBaseConfig.getBaseConfig().getActiveModel() == 1) {
                    threholdScore = SingleBaseConfig.getBaseConfig().getLiveThreshold();
                } else if (SingleBaseConfig.getBaseConfig().getActiveModel() == 2) {
                    threholdScore = SingleBaseConfig.getBaseConfig().getIdThreshold();
                } else if (SingleBaseConfig.getBaseConfig().getActiveModel() == 3) {
                    threholdScore = SingleBaseConfig.getBaseConfig().getRgbAndNirThreshold();
                }
                if (topFeature != null && topFeature.getScore() >
                        threholdScore) {
                    // ??????featureEntity ??????id+feature ??????????????????????????????????????????
                    User user = FaceApi.getInstance().getUserListById(topFeature.getId());
                    if (user != null) {
                        livenessModel.setUser(user);
                        livenessModel.setFeatureScore(topFeature.getScore());
                        /*faceId = livenessModel.getFaceInfo().faceID;
                        trackTime = System.currentTimeMillis();
                        faceAdoptModel = livenessModel;
                        failNumber = 0;
                        isFail = false;*/
                        setFail(livenessModel);
                    } else {
                        setFail(livenessModel);
                    }
                } else {
                    setFail(livenessModel);
                }
            } else {
                setFail(livenessModel);
            }
            livenessModel.setCheckDuration(System.currentTimeMillis() - startFeature);
        }
    }

    /**
     * ????????????
     */
//    public void uninitModel() {
//        if (faceDetect != null) {
//            faceDetect.uninitModel();
//        }
//        if (faceFeature != null) {
//            faceFeature.uninitModel();
//        }
//        if (faceDetectNir != null) {
//            faceDetectNir.uninitModel();
//        }
//        if (faceLiveness != null) {
//            faceLiveness.uninitModel();
//        }
//        if (faceDetect.uninitModel() == 0
//                && faceFeature.uninitModel() == 0
//                && faceDetectNir.uninitModel() == 0
//                && faceLiveness.uninitModel() == 0) {
//            initStatus = SDK_UNACTIVATION;
//            initModelSuccess = false;
//        }
//        Log.e("uninitModel", "pay-uninitModel"
//                + faceDetect.uninitModel()
//                + faceFeature.uninitModel()
//                + faceDetectNir.uninitModel()
//                + faceLiveness.uninitModel());
//    }

}