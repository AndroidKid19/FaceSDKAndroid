package com.baidu.idl.main.facesdk.attendancelibrary.attendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.attendancelibrary.BaseActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.R;
import com.baidu.idl.main.facesdk.attendancelibrary.callback.CameraDataCallback;
import com.baidu.idl.main.facesdk.attendancelibrary.callback.FaceDetectCallBack;
import com.baidu.idl.main.facesdk.attendancelibrary.camera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.attendancelibrary.camera.CameraPreviewManager;
import com.baidu.idl.main.facesdk.attendancelibrary.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.attendancelibrary.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.attendancelibrary.manager.SaveImageManager;
import com.baidu.idl.main.facesdk.attendancelibrary.model.LivenessModel;
import com.baidu.idl.main.facesdk.attendancelibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.attendancelibrary.setting.AttendanceSettingActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.DensityUtils;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.FileUtils;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.JsonRootBean;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.JsonUtils;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.TimeUtils;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.ToastUtils;
import com.baidu.idl.main.facesdk.attendancelibrary.view.PreviewTexture;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.model.User;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class FaceNIRAttendanceActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "face-rgb-ir";
    // ???????????????????????????????????????????????????640*480??? 1280*720
    private static final int PREFER_WIDTH = 640;
    private static final int PERFER_HEIGH = 480;

    private Context mContext;

    // ??????????????????
    private TextureView mDrawDetectFaceView;
    private ImageView mFaceDetectImageView;
    private TextView mTvDetect;
    private TextView mTvLive;
    private TextView mTvLiveScore;

    // ??????????????????
    private TextView mTvIr;
    private TextView mTvIrScore;

    private TextView mTvFeature;
    private TextView mTvAll;
    private TextView mTvAllTime;


    // RGB+IR ??????
    private PreviewTexture[] mPreview;
    private Camera[] mCamera;

    // textureView??????????????????????????????
    private AutoTexturePreviewView mAutoCameraPreviewView;
    private TextureView irPreviewView;

    // ???????????????
    private int mCameraNum;
    // ?????????????????????
    private volatile byte[] rgbData;
    private volatile byte[] irData;
    // ???????????????
    private Paint paint;
    private RectF rectF;

    private RelativeLayout relativeLayout;
    private float rgbLiveScore;
    private float nirLiveScore;

    // ????????????????????????????????????x?????????y????????????width
    private float[] pointXY = new float[3];
    private boolean requestToInner = false;
    private float mScreenRate = 50;
    private boolean isCheck = false;
    private boolean isCompareCheck = false;
    private TextView preText;
    private TextView deveLop;
    private RelativeLayout preViewRelativeLayout;
    private RelativeLayout deveLopRelativeLayout;
    private RelativeLayout textHuanying;
    private ImageView nameImage;
    private TextView nameText;
    private RelativeLayout userNameLayout;
    private TextView detectSurfaceText;
    private ImageView isRgbCheckImage;
    private ImageView isNirCheckImage;
    private View preView;
    private View developView;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private int mLiveType;
    private Paint paintBg;
    private TextView nirSurfaceText;
    private TextView attendanceTime;
    private TextView attendanceDate;
    private TextView attendanceTimeText;
    private RelativeLayout outRelativelayout;
    private User mUser;
    private TextView mNum;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initListener();
        FaceSDKManager.getInstance().initDataBases(this);
        setContentView(R.layout.activity_face_nir_attendancelibrary);
        initView();
        // ????????????
        int displayWidth = DensityUtils.getDisplayWidth(mContext);
        // ????????????
        int displayHeight = DensityUtils.getDisplayHeight(mContext);
        // ?????????????????????????????????
        if (displayHeight < displayWidth) {
            // ?????????
            int height = displayHeight;
            // ?????????
            int width = (int) (displayHeight * ((9.0f / 16.0f)));
            // ????????????????????????
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            // ??????????????????
            params.gravity = Gravity.CENTER;
            relativeLayout.setLayoutParams(params);
        }

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
                    FaceSDKManager.initModelSuccess = true;
                    ToastUtils.toast(FaceNIRAttendanceActivity.this, "?????????????????????????????????");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        ToastUtils.toast(FaceNIRAttendanceActivity.this, "??????????????????????????????????????????");
                    }
                }
            });
        }
    }

    /**
     * ??????Debug View
     */
    private void initView() {
        // ??????????????????
        relativeLayout = findViewById(R.id.all_relative);
        // ????????????
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);
        if (SingleBaseConfig.getBaseConfig().getRgbRevert()){
            mDrawDetectFaceView.setRotationY(180);
        }
        // ???????????????RGB ????????????
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);
        mAutoCameraPreviewView.setVisibility(View.VISIBLE);

        // ??????
        ImageView mButReturn = findViewById(R.id.btn_back);
        mButReturn.setOnClickListener(this);
        // ??????
        ImageView mBtSetting = findViewById(R.id.btn_setting);
        mBtSetting.setOnClickListener(this);
        // ????????????
        preText = findViewById(R.id.preview_text);
        preText.setOnClickListener(this);
        preText.setTextColor(Color.parseColor("#ffffff"));
        preViewRelativeLayout = findViewById(R.id.yvlan_relativeLayout);
        preView = findViewById(R.id.preview_view);
        // ????????????
        deveLop = findViewById(R.id.develop_text);
        deveLop.setOnClickListener(this);
        deveLopRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        developView = findViewById(R.id.develop_view);
        developView.setVisibility(View.GONE);
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        layoutCompareStatus.setVisibility(View.GONE);
        textCompareStatus = findViewById(R.id.text_compare_status);


        // ***************????????????*************
        isRgbCheckImage = findViewById(R.id.is_check_image);
        isNirCheckImage = findViewById(R.id.nir_is_check_image);
        // RGB ??????
        rgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // Live ??????
        nirLiveScore = SingleBaseConfig.getBaseConfig().getNirLiveScore();
        // ????????????
        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        // ???????????????RGB ????????????
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);
        // ??????RGB ????????????
        mFaceDetectImageView = findViewById(R.id.face_detect_image_view);
        mFaceDetectImageView.setVisibility(View.VISIBLE);
        // ???????????????IR ????????????
        irPreviewView = findViewById(R.id.ir_camera_preview_view);
        if (SingleBaseConfig.getBaseConfig().getMirrorVideoNIR() == 1) {
            irPreviewView.setRotationY(180);
        }
        // ?????????????????????
        mNum = findViewById(R.id.tv_num);
        mNum.setText(String.format("?????? ??? %s ?????????", FaceApi.getInstance().getmUserNum()));

        // ????????????
        mTvDetect = findViewById(R.id.tv_detect_time);
        // RGB??????
        mTvLive = findViewById(R.id.tv_rgb_live_time);
        mTvLiveScore = findViewById(R.id.tv_rgb_live_score);
        // Ir??????
        mTvIr = findViewById(R.id.tv_nir_live_time);
        mTvIrScore = findViewById(R.id.tv_nir_live_score);
        // ????????????
        mTvFeature = findViewById(R.id.tv_feature_time);
        // ??????
        mTvAll = findViewById(R.id.tv_feature_search_time);
        // ?????????
        mTvAllTime = findViewById(R.id.tv_all_time);
        // ????????????
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(this);
        spot = findViewById(R.id.spot);


        // ***************????????????*************
        textHuanying = findViewById(R.id.huanying_relative);
        userNameLayout = findViewById(R.id.user_name_layout);
        nameImage = findViewById(R.id.detect_reg_image_item);
        nameText = findViewById(R.id.name_text);
        detectSurfaceText = findViewById(R.id.detect_surface_text);
        mFaceDetectImageView.setVisibility(View.GONE);
        saveCamera.setVisibility(View.GONE);
        detectSurfaceText.setVisibility(View.GONE);
        nirSurfaceText = findViewById(R.id.nir_surface_text);
        nirSurfaceText.setVisibility(View.GONE);
        attendanceTime = findViewById(R.id.attendance_time);
        attendanceDate = findViewById(R.id.attendance_date);
        attendanceTimeText = findViewById(R.id.attendance_time_text);
        outRelativelayout = findViewById(R.id.out_relativelayout);

        // ????????????
        mCameraNum = Camera.getNumberOfCameras();
        if (mCameraNum < 2) {
            Toast.makeText(this, "????????????2????????????", Toast.LENGTH_LONG).show();
            return;
        } else {

            mPreview = new PreviewTexture[mCameraNum];
            mCamera = new Camera[mCameraNum];
            mPreview[1] = new PreviewTexture(this, irPreviewView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraNum < 2) {
            Toast.makeText(this, "????????????2????????????", Toast.LENGTH_LONG).show();
            return;
        } else {
            try {
                startTestCloseDebugRegisterFunction();
                if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
                    mCamera[1] = Camera.open(Math.abs(SingleBaseConfig.getBaseConfig().getRBGCameraId() - 1));
                }else {
                    mCamera[1] = Camera.open(1);
                }
                ViewGroup.LayoutParams layoutParams = irPreviewView.getLayoutParams();
                int w = layoutParams.width;
                int h = layoutParams.height;
                int cameraRotation = SingleBaseConfig.getBaseConfig().getNirVideoDirection();
                mCamera[1].setDisplayOrientation(cameraRotation);
                if (cameraRotation == 90 || cameraRotation == 270) {
                    layoutParams.height = w;
                    layoutParams.width = h;
                    // ??????90?????????270?????????????????????
                } else {
                    layoutParams.height = h;
                    layoutParams.width = w;
                }
                irPreviewView.setLayoutParams(layoutParams);
                mPreview[1].setCamera(mCamera[1], PREFER_WIDTH, PERFER_HEIGH);
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        dealIr(data);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void startTestCloseDebugRegisterFunction() {
        // TODO ??? ????????????
        // ?????????????????????
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // ?????????????????????
        //  CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // ??????USB?????????
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1){
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        }else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        }


        CameraPreviewManager.getInstance().startPreview(this, mAutoCameraPreviewView,
                PREFER_WIDTH, PERFER_HEIGH, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        // ???????????????????????????????????????
                        dealRgb(data);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {

        CameraPreviewManager.getInstance().stopPreview();

        if (mCameraNum >= 2) {
            for (int i = 0; i < mCameraNum; i++) {
                if (mCameraNum >= 2) {
                    if (mCamera[i] != null) {
                        mCamera[i].setPreviewCallback(null);
                        mCamera[i].stopPreview();
                        mPreview[i].release();
                        mCamera[i].release();
                        mCamera[i] = null;
                    }
                }
            }
        }

        super.onPause();
    }

    private void dealRgb(byte[] data) {
        rgbData = data;
        checkData();
    }

    private void dealIr(byte[] data) {
        irData = data;
        checkData();
    }

    private synchronized void checkData() {
        if (rgbData != null && irData != null) {
            FaceSDKManager.getInstance().onDetectCheck(rgbData, irData, null,
                    PERFER_HEIGH, PREFER_WIDTH, 2, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            // ????????????
                            // ????????????
                            checkCloseDebugResult(livenessModel);
                            // ????????????
                            checkOpenDebugResult(livenessModel);

                            if (isSaveImage){
                                SaveImageManager.getInstance().saveImage(livenessModel);
                            }

                        }

                        @Override
                        public void onTip(int code, String msg) {
                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                            // ???????????????
                            showFrame(livenessModel);
                        }
                    });
            rgbData = null;
            irData = null;
        }
    }

    // ***************????????????????????????*************
    private void checkCloseDebugResult(final LivenessModel livenessModel) {
        // ?????????????????????UI??????
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Date date = new Date();
                attendanceTime.setText(TimeUtils.getTimeShort(date));
                attendanceDate.setText(TimeUtils.getStringDateShort(date) + " "
                        + TimeUtils.getWeek(date));
                if (livenessModel == null) {
                    textHuanying.setVisibility(View.VISIBLE);
                    userNameLayout.setVisibility(View.GONE);
                    return;
                }
                User user = livenessModel.getUser();
                if (user == null) {
                    mUser = null;
                    if (livenessModel.isMultiFrame()) {
                        textHuanying.setVisibility(View.GONE);
                        userNameLayout.setVisibility(View.VISIBLE);
                        nameImage.setImageResource(R.mipmap.ic_tips_gate_fail);
                        nameText.setTextColor(Color.parseColor("#fec133"));
                        nameText.setText("????????????");
                        attendanceTimeText.setText("???????????????......");
                    } else {
                        textHuanying.setVisibility(View.VISIBLE);
                        userNameLayout.setVisibility(View.GONE);
                    }
                } else {
                    mUser = user;
                    textHuanying.setVisibility(View.GONE);
                    userNameLayout.setVisibility(View.VISIBLE);
                    String absolutePath = FileUtils.getBatchImportSuccessDirectory()
                            + "/" + user.getImageName();
                    Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                    nameImage.setImageBitmap(bitmap);
                    nameText.setTextColor(Color.parseColor("#00BAF2"));
                    nameText.setText(FileUtils.spotString(user.getUserName()) + " ????????????");
                    attendanceTimeText.setText("???????????????" + TimeUtils.getTimeShort(date));
                }

            }
        });
    }

    // ***************????????????????????????*************
    private void checkOpenDebugResult(final LivenessModel livenessModel) {

        // ?????????????????????UI??????
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    layoutCompareStatus.setVisibility(View.GONE);
                    isNirCheckImage.setVisibility(View.GONE);
                    isRgbCheckImage.setVisibility(View.GONE);
                    mFaceDetectImageView.setImageResource(R.mipmap.ic_image_video);
                    mTvDetect.setText(String.format("???????????????%s ms", 0));
                    mTvLive.setText(String.format("RGB?????????????????? ???%s ms", 0));
                    mTvLiveScore.setText(String.format("RGB???????????? ???%s", 0));
                    mTvIr.setText(String.format("NIR?????????????????? ???%s ms", 0));
                    mTvIrScore.setText(String.format("NIR???????????? ???%s", 0));
                    mTvFeature.setText(String.format("?????????????????? ???%s ms", 0));
                    mTvAll.setText(String.format("?????????????????? ???%s ms", 0));
                    mTvAllTime.setText(String.format("????????? ???%s ms", 0));
                    return;
                }

                BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();
                if (image != null) {
                    mFaceDetectImageView.setImageBitmap(BitmapUtils.getInstaceBmp(image));
                    image.destory();
                }
                float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                float nirLivenessScore = livenessModel.getIrLivenessScore();
                if (nirLivenessScore < nirLiveScore) {
                    if (isCheck) {
                        isNirCheckImage.setVisibility(View.VISIBLE);
                        isNirCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                    }
                } else {
                    if (isCheck) {
                        isNirCheckImage.setVisibility(View.VISIBLE);
                        isNirCheckImage.setImageResource(R.mipmap.ic_icon_develop_success);
                    }

                }
                if (rgbLivenessScore < rgbLiveScore) {
                    if (isCheck) {
                        isRgbCheckImage.setVisibility(View.VISIBLE);
                        isRgbCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                    }
                } else {
                    if (isCheck) {
                        isRgbCheckImage.setVisibility(View.VISIBLE);
                        isRgbCheckImage.setImageResource(R.mipmap.ic_icon_develop_success);
                    }

                }
                if (! livenessModel.isQualityCheck()){
                    if (isCompareCheck) {
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
//                                                textCompareStatus.setMaxEms(6);
                        textCompareStatus.setText("??????????????????");
                    }
                } else if (rgbLivenessScore < rgbLiveScore || nirLivenessScore < nirLiveScore) {
                    if (isCompareCheck) {
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));

//                            textCompareStatus.setMaxEms(7);
                            textCompareStatus.setText("?????????????????????");
                    }
                } else {

                    User user = livenessModel.getUser();
                    if (user == null) {
                        mUser = null;
                        if (isCompareCheck) {
                            if (livenessModel.isMultiFrame()) {
                                layoutCompareStatus.setVisibility(View.VISIBLE);
                                textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
                                textCompareStatus.setText("???????????????");
                            } else {
                                layoutCompareStatus.setVisibility(View.GONE);
                            }
                        }

                    } else {
                        mUser = user;
                        if (isCompareCheck) {
                            layoutCompareStatus.setVisibility(View.VISIBLE);
                            textCompareStatus.setTextColor(Color.parseColor("#FF00BAF2"));

//                            textCompareStatus.setMaxEms(5);
                            textCompareStatus.setText(FileUtils.spotString(mUser.getUserName()));
                        }
                    }
                }
                mTvDetect.setText(String.format("???????????? ???%s ms", livenessModel.getRgbDetectDuration()));
                mTvLive.setText(String.format("RGB?????????????????? ???%s ms", livenessModel.getRgbLivenessDuration()));
                mTvLiveScore.setText(String.format("RGB???????????? ???%s", livenessModel.getRgbLivenessScore()));
                mTvIr.setText(String.format("NIR?????????????????? ???%s ms", livenessModel.getIrLivenessDuration()));
                mTvIrScore.setText(String.format("NIR???????????? ???%s", livenessModel.getIrLivenessScore()));
                mTvFeature.setText(String.format("?????????????????? ???%s ms", livenessModel.getFeatureDuration()));
                mTvAll.setText(String.format("?????????????????? ???%s ms", livenessModel.getCheckDuration()));
                mTvAllTime.setText(String.format("????????? ???%s ms", livenessModel.getAllDetectDuration()));
            }
        });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        // ??????
        if (id == R.id.btn_back) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK????????????????????????????????????",
                        Toast.LENGTH_LONG).show();
                return;
            }
            finish();
            // ??????
        } else if (id == R.id.btn_setting) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK????????????????????????????????????",
                        Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(mContext, AttendanceSettingActivity.class));
            finish();
        } else if (id == R.id.preview_text) {
            irPreviewView.setAlpha(0);
            isRgbCheckImage.setVisibility(View.GONE);
            isNirCheckImage.setVisibility(View.GONE);
            mFaceDetectImageView.setVisibility(View.GONE);
            saveCamera.setVisibility(View.GONE);
            detectSurfaceText.setVisibility(View.GONE);
            nirSurfaceText.setVisibility(View.GONE);
            layoutCompareStatus.setVisibility(View.GONE);
            deveLop.setTextColor(Color.parseColor("#a9a9a9"));
            preText.setTextColor(Color.parseColor("#ffffff"));
            preView.setVisibility(View.VISIBLE);
            developView.setVisibility(View.GONE);
            preViewRelativeLayout.setVisibility(View.VISIBLE);
            deveLopRelativeLayout.setVisibility(View.GONE);
            isCheck = false;
            isCompareCheck = false;
            isSaveImage = false;
            outRelativelayout.setVisibility(View.VISIBLE);
            spot.setVisibility(View.GONE);
        } else if (id == R.id.develop_text) {
            isCheck = true;
            isCompareCheck = true;
            irPreviewView.setAlpha(1);
            isRgbCheckImage.setVisibility(View.VISIBLE);
            isNirCheckImage.setVisibility(View.VISIBLE);
            mFaceDetectImageView.setVisibility(View.VISIBLE);
            saveCamera.setVisibility(View.VISIBLE);
            detectSurfaceText.setVisibility(View.VISIBLE);
            nirSurfaceText.setVisibility(View.VISIBLE);
            deveLop.setTextColor(Color.parseColor("#ffffff"));
            preText.setTextColor(Color.parseColor("#a9a9a9"));
            preView.setVisibility(View.GONE);
            developView.setVisibility(View.VISIBLE);
            deveLopRelativeLayout.setVisibility(View.VISIBLE);
            preViewRelativeLayout.setVisibility(View.GONE);
            outRelativelayout.setVisibility(View.GONE);
            judgeFirst();
        } else if (id == R.id.save_camera){
            isSaveImage = !isSaveImage;
            if (isSaveImage){
                ToastUtils.toast(FaceNIRAttendanceActivity.this, "??????????????????????????????????????????");
                spot.setVisibility(View.VISIBLE);
            }else {
                spot.setVisibility(View.GONE);
            }
        }
    }
    private void judgeFirst(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isAttendanceFirstSave", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            setFirstView(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setFirstView(View.GONE);
                }
            },3000);
            editor.putBoolean("isAttendanceFirstSave", false);
            editor.commit();
        }
    }
    private void setFirstView(int visibility){
        findViewById(R.id.first_text_tips).setVisibility(visibility);
        findViewById(R.id.first_circular_tips).setVisibility(visibility);

    }

    /**
     * ???????????????
     */
    private void showFrame(final LivenessModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mDrawDetectFaceView.lockCanvas();
                if (canvas == null) {
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                if (model == null) {
                    // ??????canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // ??????canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // ??????????????????????????????????????????????????????????????????
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mAutoCameraPreviewView, model.getBdFaceImageInstance());
                // ???????????????
                FaceOnDrawTexturViewUtil.drawFaceColor(mUser, paint, paintBg, model);
                // ???????????????
                FaceOnDrawTexturViewUtil.drawRect(canvas,
                        rectF, paint, 5f, 50f , 25f);
                // ??????canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }


    // ???????????????????????????
    private void isInserLimit(final LivenessModel livenessModel) {
        if (livenessModel == null) {
            requestToInner = false;
            return;
        }
        FaceInfo[] faceInfos = livenessModel.getTrackFaceInfo();
        if (faceInfos == null || faceInfos.length == 0) {
            requestToInner = false;
            return;
        }
        pointXY[0] = livenessModel.getFaceInfo().centerX;
        pointXY[1] = livenessModel.getFaceInfo().centerY;
        pointXY[2] = livenessModel.getFaceInfo().width;
        FaceOnDrawTexturViewUtil.converttPointXY(pointXY, mAutoCameraPreviewView,
                livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);
        float lfetLimitX = AutoTexturePreviewView.circleX - AutoTexturePreviewView.circleRadius;
        float rightLimitX = AutoTexturePreviewView.circleX + AutoTexturePreviewView.circleRadius;
        float topLimitY = AutoTexturePreviewView.circleY - AutoTexturePreviewView.circleRadius;
        float bottomLimitY = AutoTexturePreviewView.circleY + AutoTexturePreviewView.circleRadius;

        if (pointXY[0] - pointXY[2] / 2 < lfetLimitX
                || pointXY[0] + pointXY[2] / 2 > rightLimitX
                || pointXY[1] - pointXY[2] / 2 < topLimitY
                || pointXY[1] + pointXY[2] / 2 > bottomLimitY) {
            requestToInner = true;
        } else {
            requestToInner = false;
        }
    }


}
