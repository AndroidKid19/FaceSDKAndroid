package com.baidu.idl.main.facesdk.registerlibrary.user.register;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;

import com.Tool.URLUtils;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.registerlibrary.R;
import com.baidu.idl.main.facesdk.registerlibrary.user.activity.BaseActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.callback.CameraDataCallback;
import com.baidu.idl.main.facesdk.registerlibrary.user.callback.FaceDetectCallBack;
import com.baidu.idl.main.facesdk.registerlibrary.user.callback.FaceFeatureCallBack;
import com.baidu.idl.main.facesdk.registerlibrary.user.camera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.registerlibrary.user.camera.CameraPreviewManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceTrackManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.LivenessModel;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.CreateJsonRootBean;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.DensityUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.FileUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.JsonRootBean;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.JsonUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.KeyboardsUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.ToastUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.TtsManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.view.CircleImageView;
import com.baidu.idl.main.facesdk.registerlibrary.user.view.FaceRoundProView;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.model.User;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import createbest.sdk.bihu.temperature.ITemperature;
import createbest.sdk.bihu.temperature.Temperature_RB32x3290A;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 新人脸注册页面
 * Created by v_liujialu01 on 2020/02/19.
 */
public class FaceRegisterNewActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = FaceRegisterNewActivity.class.getSimpleName();

    // RGB摄像头图像宽和高
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PERFER_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    private AutoTexturePreviewView mAutoTexturePreviewView;
    private FaceRoundProView mFaceRoundProView;
    private RelativeLayout mRelativePreview;     // 预览相关布局

    // 采集相关布局
    private RelativeLayout mRelativeCollectSuccess;
    private CircleImageView mCircleHead;
    private EditText mEditName;
    private TextView mTextError;
    private Button mBtnCollectConfirm;
    private ImageView mImageInputClear;

    // 注册成功相关布局
    private RelativeLayout mRelativeRegisterSuccess;
    private CircleImageView mCircleRegSucHead;

    // 包含适配屏幕后的人脸的x坐标，y坐标，和width
    private float[] mPointXY = new float[4];
    private byte[] mFeatures = new byte[512];
    private Bitmap mCropBitmap;
    private boolean mCollectSuccess = false;


    private TextView text_collect;
    private TextView text_card;
    private TextView text_organ;
    private TextView tv_body_temperature;
    private TextView tv_message;

    /**
     * 测温
     * */
    private ITemperature temperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListener();
        setContentView(R.layout.activity_new_registerlibrary);
        initView();
        //在应用启动时初始化一次
        TtsManager.getInstance(this).init();
        initTemperature();
    }

    private void initListener() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().initModel(this, new SdkInitListener() {
                @Override
                public void initStart() {
                }

                @Override
                public void initLicenseSuccess() {
                }

                @Override
                public void initLicenseFail(int errorCode, String msg) {
                }

                @Override
                public void initModelSuccess() {
                    ToastUtils.toast(FaceRegisterNewActivity.this, "模型加载成功，欢迎使用");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    if (errorCode != -12) {
                        ToastUtils.toast(FaceRegisterNewActivity.this, "模型加载失败，请尝试重启应用");
                    }
                }
            });
        }
    }


    /**
     * 测温头初始化
     * */
    private void initTemperature() {
        temperature = Temperature_RB32x3290A.getInstance();
        temperature.setReader(new ITemperature.Reader() {
            @Override
            public void onGetTemperature(final float temp, final boolean jarless) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String temperature = new DecimalFormat("00.00").format(temp);
                        if (jarless) {
                            if (temp < 36) {
                                tv_body_temperature.setText(temperature+"℃");
                                tv_body_temperature.setBackgroundColor(Color.GREEN);
                            } else if (temp > 37.3) {
//                                myTTS.speak("体温异常");
                                com.Tool.TtsManager.getInstance(FaceRegisterNewActivity.this).speakText("体温异常");
                                tv_body_temperature.setText(temperature+"℃");
                                tv_body_temperature.setBackgroundColor(Color.RED);
                            } else {
                                tv_body_temperature.setText(temperature+"℃");
                                tv_body_temperature.setBackgroundColor(Color.GREEN);
                            }
                        } else {
                            tv_body_temperature.setText(temperature+"℃");
                            tv_body_temperature.setBackgroundColor(Color.GREEN);
                        }
                    }
                });
            }
        });
        temperature.open("/dev/ttyS4");
    }


    private void initView() {
        mAutoTexturePreviewView = findViewById(R.id.auto_camera_preview_view);
        mAutoTexturePreviewView.setIsRegister(true);
        mFaceRoundProView = findViewById(R.id.round_view);
        mRelativePreview = findViewById(R.id.relative_preview);

        mRelativeCollectSuccess = findViewById(R.id.relative_collect_success);
        mCircleHead = findViewById(R.id.circle_head);
        mCircleHead.setBorderWidth(DensityUtils.dip2px(FaceRegisterNewActivity.this, 3));
        mCircleHead.setBorderColor(Color.parseColor("#0D9EFF"));
        mEditName = findViewById(R.id.edit_name);
        mTextError = findViewById(R.id.text_error);
        mBtnCollectConfirm = findViewById(R.id.btn_collect_confirm);
        mBtnCollectConfirm.setOnClickListener(this);
        mImageInputClear = findViewById(R.id.image_input_delete);
        mImageInputClear.setOnClickListener(this);

        mRelativeRegisterSuccess = findViewById(R.id.relative_register_success);
        mCircleRegSucHead = findViewById(R.id.circle_reg_suc_head);
        findViewById(R.id.btn_return_home).setOnClickListener(this);
        findViewById(R.id.btn_continue_reg).setOnClickListener(this);

        ImageView imageBack = findViewById(R.id.image_register_back);
        imageBack.setOnClickListener(this);

        // 输入框监听事件
        mEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mImageInputClear.setVisibility(View.VISIBLE);
                    mBtnCollectConfirm.setEnabled(true);
                    mBtnCollectConfirm.setTextColor(Color.WHITE);
                    mBtnCollectConfirm.setBackgroundResource(R.drawable.button_selector);
                    List<User> listUsers = FaceApi.getInstance().getUserListByUserName(s.toString());
                    if (listUsers != null && listUsers.size() > 0) {     // 出现用户名重复
                        mTextError.setVisibility(View.VISIBLE);
                        mBtnCollectConfirm.setEnabled(false);
                    } else {
                        mTextError.setVisibility(View.INVISIBLE);
                        mBtnCollectConfirm.setEnabled(true);
                    }
                } else {
                    mImageInputClear.setVisibility(View.GONE);
                    mBtnCollectConfirm.setEnabled(false);
                    mBtnCollectConfirm.setTextColor(Color.parseColor("#666666"));
                    mBtnCollectConfirm.setBackgroundResource(R.mipmap.btn_all_d);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        text_collect = findViewById(R.id.text_collect);

        text_card = findViewById(R.id.text_card);
        text_organ = findViewById(R.id.text_organ);
        tv_body_temperature = findViewById(R.id.tv_body_temperature);
        tv_message = findViewById(R.id.tv_message);
        //初始化访客信息
        initVisitorData();
    }


    private void initVisitorData() {
        if (getIntent().getStringExtra("userName") != null) {
            text_collect.setText(getIntent().getStringExtra("userName"));
            text_card.setText(getIntent().getStringExtra("certificateNumber"));
            text_organ.setText(getIntent().getStringExtra("orgTitle"));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 摄像头图像预览
        startCameraPreview();
        Log.e(TAG, "start camera");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭摄像头
        CameraPreviewManager.getInstance().stopPreview();
        if (mCropBitmap != null) {
            if (!mCropBitmap.isRecycled()) {
                mCropBitmap.recycle();
            }
            mCropBitmap = null;
        }
        TtsManager.getInstance(this).destory();
    }

    /**
     * 摄像头图像预览
     */
    private void startCameraPreview() {
        // 设置前置摄像头
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        } else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        }
        // 设置后置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);

        CameraPreviewManager.getInstance().startPreview(this, mAutoTexturePreviewView,
                PREFER_WIDTH, PERFER_HEIGHT, new CameraDataCallback() {

                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        if (mCollectSuccess) {
                            return;
                        }
                        // 摄像头数据处理
                        faceDetect(data, width, height);
                    }
                });
    }

    /**
     * 摄像头数据处理
     */
    private void faceDetect(byte[] data, final int width, final int height) {
        if (mCollectSuccess) {
            return;
        }

        // 摄像头预览数据进行人脸检测
        int liveType = SingleBaseConfig.getBaseConfig().getType();
        // int liveType = 2;

        if (liveType == 0) { // 无活体检测
            FaceTrackManager.getInstance().setAliving(false);
        } else if (liveType == 1) { // 活体检测
            FaceTrackManager.getInstance().setAliving(true);
        }

        // 摄像头预览数据进行人脸检测
        FaceTrackManager.getInstance().faceTrack(data, width, height, new FaceDetectCallBack() {
            @Override
            public void onFaceDetectCallback(LivenessModel livenessModel) {
                checkFaceBound(livenessModel);
            }

            @Override
            public void onTip(final int code, final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mFaceRoundProView == null) {
                            return;
                        }
                        if (code == 0) {
                            mFaceRoundProView.setTipText("请保持面部在取景框内");
                            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey, false);
                        } else {
                            mFaceRoundProView.setTipText("请保证人脸区域清晰无遮挡");
                            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue, true);
                        }
                    }
                });
            }

            @Override
            public void onFaceDetectDarwCallback(LivenessModel livenessModel) {

            }
        });
    }

    /**
     * 检查人脸边界
     *
     * @param livenessModel LivenessModel实体
     */
    private void checkFaceBound(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCollectSuccess) {
                    return;
                }

                if (livenessModel == null || livenessModel.getFaceInfo() == null) {
                    mFaceRoundProView.setTipText("请保持面部在取景框内");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey, false);
                    return;
                }
                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey, false);

                if (livenessModel.getFaceSize() > 1) {
                    mFaceRoundProView.setTipText("请保证取景框内只有一个人脸");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue, true);
                    return;
                }


                mPointXY[0] = livenessModel.getFaceInfo().centerX;   // 人脸X坐标
                mPointXY[1] = livenessModel.getFaceInfo().centerY;   // 人脸Y坐标
                mPointXY[2] = livenessModel.getFaceInfo().width;     // 人脸宽度
                mPointXY[3] = livenessModel.getFaceInfo().height;    // 人脸高度

                FaceOnDrawTexturViewUtil.converttPointXY(mPointXY, mAutoTexturePreviewView,
                        livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);

                float leftLimitX = AutoTexturePreviewView.circleX - AutoTexturePreviewView.circleRadius;
                float rightLimitX = AutoTexturePreviewView.circleX + AutoTexturePreviewView.circleRadius;
                float topLimitY = AutoTexturePreviewView.circleY - AutoTexturePreviewView.circleRadius;
                float bottomLimitY = AutoTexturePreviewView.circleY + AutoTexturePreviewView.circleRadius;
                float previewWidth = AutoTexturePreviewView.circleRadius * 2;

//                Log.e(TAG, "faceX = " + mPointXY[0] + ", faceY = " + mPointXY[1]
//                        + ", faceW = " + mPointXY[2] + ", prw = " + previewWidth);
//                Log.e(TAG, "leftLimitX = " + leftLimitX + ", rightLimitX = " + rightLimitX
//                        + ", topLimitY = " + topLimitY + ", bottomLimitY = " + bottomLimitY);
//                Log.e(TAG, "cX = " + AutoTexturePreviewView.circleX + ", cY = " + AutoTexturePreviewView.circleY
//                        + ", cR = " + AutoTexturePreviewView.circleRadius);

                if (mPointXY[2] < 50 || mPointXY[3] < 50) {
                    mFaceRoundProView.setTipText("请保证人脸区域清晰无遮挡");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue, true);
                    // 释放内存
                    destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                    return;
                }

                if (mPointXY[2] > previewWidth || mPointXY[3] > previewWidth) {
                    mFaceRoundProView.setTipText("请保证人脸区域清晰无遮挡");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue, true);
                    // 释放内存
                    destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                    return;
                }

                if (mPointXY[0] - mPointXY[2] / 2 < leftLimitX
                        || mPointXY[0] + mPointXY[2] / 2 > rightLimitX
                        || mPointXY[1] - mPointXY[3] / 2 < topLimitY
                        || mPointXY[1] + mPointXY[3] / 2 > bottomLimitY) {
                    mFaceRoundProView.setTipText("请保证人脸区域清晰无遮挡");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue, true);
                    // 释放内存
                    destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                    return;
                }

//                if ((Math.abs(AutoTexturePreviewView.circleX - mPointXY[0]) < mPointXY[2] / 2)
//                        && (Math.abs(AutoTexturePreviewView.circleY - mPointXY[1]) < mPointXY[2] / 2)
//                        && (mPointXY[2] <= previewWidth && mPointXY[3] <= previewWidth)) {
//
//                }
                mFaceRoundProView.setTipText("请保持面部在取景框内");
                // 检验活体分值
                checkLiveScore(livenessModel);
            }
        });
    }

    /**
     * 检验活体分值
     *
     * @param livenessModel LivenessModel实体
     */
    private void checkLiveScore(LivenessModel livenessModel) {
        if (livenessModel == null || livenessModel.getFaceInfo() == null) {
            mFaceRoundProView.setTipText("请保持面部在取景框内");
            return;
        }

        // 获取活体类型
        int liveType = SingleBaseConfig.getBaseConfig().getType();
        // int liveType = 2;

        if (!livenessModel.isQualityCheck()) {
            mFaceRoundProView.setTipText("请保证人脸区域清晰无遮挡");
            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue, true);
            return;
        } else if (liveType == 0) {         // 无活体
            getFeatures(livenessModel);
        } else if (liveType == 1) { // RGB活体检测
            float rgbLivenessScore = livenessModel.getRgbLivenessScore();
            float liveThreadHold = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
            // Log.e(TAG, "score = " + rgbLivenessScore);
            if (rgbLivenessScore < liveThreadHold) {
                mFaceRoundProView.setTipText("请保证采集对象为真人");
                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue, true);
                // 释放内存
                destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                return;
            }
            // 提取特征值
            getFeatures(livenessModel);
        }
    }

    /**
     * 提取特征值
     *
     * @param model 人脸数据
     */
    private void getFeatures(final LivenessModel model) {
        if (model == null) {
            return;
        }

        // 获取选择的特征抽取模型
        int modelType = SingleBaseConfig.getBaseConfig().getActiveModel();
        if (modelType == 1) {
            // 生活照
            FaceSDKManager.getInstance().onFeatureCheck(model.getBdFaceImageInstance(), model.getLandmarks(),
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, new FaceFeatureCallBack() {
                        @Override
                        public void onFaceFeatureCallBack(final float featureSize, final byte[] feature, long time) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mCollectSuccess) {
                                        return;
                                    }
                                    displayCompareResult(featureSize, feature, model);
                                    Log.e(TAG, String.valueOf(feature.length));
                                }
                            });

                        }
                    });
        }
    }

    // 根据特征抽取的结果 注册人脸
    private void displayCompareResult(float ret, byte[] faceFeature, LivenessModel model) {
        if (model == null) {
            mFaceRoundProView.setTipText("请保持面部在取景框内");
            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey, false);
            return;
        }

        // 特征提取成功
        if (ret == 128) {
            // 抠图
            BDFaceImageInstance imageInstance = model.getBdFaceImageInstanceCrop();
            AtomicInteger isOutoBoundary = new AtomicInteger();
            BDFaceImageInstance cropInstance = FaceSDKManager.getInstance().getFaceCrop()
                    .cropFaceByLandmark(imageInstance, model.getLandmarks(),
                            2.0f, false, isOutoBoundary);
            if (cropInstance == null) {
                mFaceRoundProView.setTipText("抠图失败");
                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue, true);
                // 释放内存
                destroyImageInstance(model.getBdFaceImageInstanceCrop());
                return;
            }
            mCropBitmap = BitmapUtils.getInstaceBmp(cropInstance);
            // 获取头像
            if (mCropBitmap != null) {
                mCollectSuccess = true;
                mCircleHead.setImageBitmap(mCropBitmap);

                //上传头像
                uploadHeadFile(fileFromBitmap(mCropBitmap));

            }
            cropInstance.destory();
            // 释放内存
            destroyImageInstance(model.getBdFaceImageInstanceCrop());

            mRelativeCollectSuccess.setVisibility(View.VISIBLE);
            mRelativePreview.setVisibility(View.GONE);
            mFaceRoundProView.setTipText("");

            for (int i = 0; i < faceFeature.length; i++) {
                mFeatures[i] = faceFeature[i];
            }
        } else {
            mFaceRoundProView.setTipText("特征提取失败");
            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue, true);
        }
    }

    /**
     * 上传头像
     */
    private void uploadHeadFile(File fileFromBitmap) {
        //定义预约用户查询接口URL
        String hostUrl = URLUtils.hostUrl+"/organ/fileUpload/upload";
        OkHttpClient client = new OkHttpClient();
        /**
         * 创建请求的参数body
         */
        RequestBody requestBody = FormBody.create(MediaType.parse("image/png"), fileFromBitmap);
        //设置上传文件的内容
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", fileFromBitmap.getName(), requestBody)
                .build();
        Request request = new Request.Builder()
                .header("User-Agent", "OkHttp Example")
                .url(hostUrl)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //验证通过
                if (response.isSuccessful()) {
                    Log.i("onResponse", "isSuccessful");
                    String result = response.body().string();
                    JsonRootBean jsonRootBean = JsonUtils.deserialize(result, JsonRootBean.class);
                    LogUtils.json(jsonRootBean);
                    if (!StringUtils.isEmpty(jsonRootBean.getData().getFileList().get(0).getUrl())) {
                        //更新用户信息
                        updateByCertificateNumber(getIntent().getStringExtra("certificateNumber"),jsonRootBean.getData().getFileList().get(0).getUrl());
                        //提交成功
                        TtsManager.getInstance(FaceRegisterNewActivity.this).speakText("登记成功,当前体温"+tv_body_temperature.getText().toString());
                        cdTimer.start();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("onResponse", "-------------------------------");
                //...
            }

        });

    }

    /**
     * 提交预约人员
     */
    public void createVisitRegisterRecord(String coverPictureUrl) {
        final String[] ercode = getIntent().getStringExtra("result").split("/");
        //定义提交预约人员接口URL
        String hostUrl = URLUtils.hostUrl+"/organ/visitRegisterRecord/createVisitRegisterRecord";
        OkHttpClient client = new OkHttpClient();
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("name", ercode[0]);
        paramsMap.put("certificateNumber", ercode[1]);
        paramsMap.put("cellPhone", ercode[2]);
        paramsMap.put("visitTime", ercode[3]);
        paramsMap.put("orgId", ercode[5]);
        paramsMap.put("coverPictureUrl", coverPictureUrl);
        Gson gson = new Gson();
        /**
         * 创建请求的参数body
         */
        RequestBody body = FormBody.create(MediaType.parse("application/json"), gson.toJson(paramsMap));
        Request request = new Request.Builder()
                .header("User-Agent", "OkHttp Example")
                .url(hostUrl)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //验证通过
                if (response.isSuccessful()) {
                    Log.i("onResponse", "isSuccessful");
                    String result = response.body().string();
                    CreateJsonRootBean newsBeanList = JsonUtils.deserialize(result, CreateJsonRootBean.class);
                    if (newsBeanList.getRespCode().equals("0000")) {
                        //提交成功
                        TtsManager.getInstance(FaceRegisterNewActivity.this).speakText("登记成功,当前体温"+tv_body_temperature.getText().toString());
                    }
                    cdTimer.start();

                } else {

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("onResponse", "-------------------------------");
                //...
            }

        });
    }

    /**
     * Parameters:
     * millisInFuture   The number of millis in the future from the call to start() until the countdown is done and onFinish() is called.
     * countDownInterval    The interval along the way to receive onTick(long) callbacks.
     */
    private CountDownTimer cdTimer = new CountDownTimer(10000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            tv_message.setText("当前页即将在"+(millisUntilFinished / 1000) + "秒"+"后关闭");
        }

        @Override
        public void onFinish() {
            finish();
        }
    };

    /**
     * 根据身份证号 校验是否预约
     *  certificateNumber; 证件号码
     * visitStatus 到访状态；0未到访；1已到访
     * */
    public void updateByCertificateNumber(String cardNumber,String personalPhotos){
        String hostUrl = URLUtils.hostUrl+"/organ/visitRegisterRecord/updateByCertificateNumber";
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
        RequestBody formBody = new FormBody.Builder()
                .add("certificateNumber",cardNumber)
                .add("visitStatus","1")
                .add("personalPhotos",personalPhotos)
                .build();

        Map<String,String> paramsMap = new HashMap<>();
        paramsMap.put("certificateNumber",cardNumber);
        paramsMap.put("visitStatus","1");
        Gson gson = new Gson();
        /**
         * 创建请求的参数body
         */
        RequestBody body = FormBody.create(MediaType.parse("application/json"), gson.toJson(paramsMap));
        Request request = new Request.Builder()
                .header("User-Agent", "OkHttp Example")
                .url(hostUrl)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call,Response response) throws IOException {
                //验证通过
                if (response.isSuccessful()){

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("onResponse","-------------------------------");
                //...
            }

        });
    }


    private File fileFromBitmap(Bitmap mCropBitmap) {
        //create a file to write bitmap data
        File f = new File(FaceRegisterNewActivity.this.getCacheDir(), "temporary_file.png");
        try {
            f.createNewFile();
            //Convert bitmap to byte array
            Bitmap bitmap = mCropBitmap;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    /**
     * 释放图像
     *
     * @param imageInstance
     */
    private void destroyImageInstance(BDFaceImageInstance imageInstance) {
        if (imageInstance != null) {
            imageInstance.destory();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.image_register_back) {    // 返回
            finish();
        } else if (id == R.id.btn_collect_confirm) {   // 用户名注册
            String userName = mEditName.getText().toString();
//                if (TextUtils.isEmpty(userName)) {
//                    ToastUtils.toast(getApplicationContext(), "请先输入用户名");
//                    return;
//                }
//                if (userName.length() > 10) {
//                    ToastUtils.toast(getApplicationContext(), "用户名长度不得大于10位");
//                    return;
//                }
            // 姓名过滤
            String nameResult = FaceApi.getInstance().isValidName(userName);
            if (!"0".equals(nameResult)) {
                ToastUtils.toast(getApplicationContext(), nameResult);
                return;
            }
            String imageName = userName + ".jpg";
            // 注册到人脸库
            boolean isSuccess = FaceApi.getInstance().registerUserIntoDBmanager(null,
                    userName, imageName, null, mFeatures);
            if (isSuccess) {
                // 保存人脸图片
                File faceDir = FileUtils.getBatchImportSuccessDirectory();
                File file = new File(faceDir, imageName);
                FileUtils.saveBitmap(file, mCropBitmap);
                // 数据变化，更新内存
//                FaceSDKManager.getInstance().initDatabases();
                // 更新UI
                mRelativeCollectSuccess.setVisibility(View.GONE);
                mRelativeRegisterSuccess.setVisibility(View.VISIBLE);
                mCircleRegSucHead.setImageBitmap(mCropBitmap);
            } else {
                ToastUtils.toast(getApplicationContext(), "保存数据库失败，" +
                        "可能是用户名格式不正确");
            }
        } else if (id == R.id.btn_continue_reg) {      // 继续注册
            if (mRelativeRegisterSuccess.getVisibility() == View.VISIBLE) {
                mRelativeRegisterSuccess.setVisibility(View.GONE);
            }
            mRelativePreview.setVisibility(View.VISIBLE);
            mCollectSuccess = false;
            mFaceRoundProView.setTipText("");
            mEditName.setText("");
        } else if (id == R.id.btn_return_home) {       // 回到首页
            // 关闭摄像头
            CameraPreviewManager.getInstance().stopPreview();
            finish();
        } else if (id == R.id.image_input_delete) {   // 清除输入
            mEditName.setText("");
            mTextError.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 点击非编辑区域收起键盘
     * 获取点击事件
     */
    @CallSuper
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (KeyboardsUtils.isShouldHideKeyBord(view, ev)) {
                KeyboardsUtils.hintKeyBoards(view);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}
