package com.baidu.idl.main.facesdk.identifylibrary.testimony;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.DKCloudID.DKCloudID;
import com.DKCloudID.IDCard;
import com.DKCloudID.IDCardData;
import com.Exception.CardNoResponseException;
import com.Exception.DKCloudIDException;
import com.Exception.DeviceNoResponseException;
import com.Tool.StringTool;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.identifylibrary.BaseActivity;
import com.baidu.idl.main.facesdk.identifylibrary.R;
import com.baidu.idl.main.facesdk.identifylibrary.callback.CameraDataCallback;
import com.baidu.idl.main.facesdk.identifylibrary.callback.FaceDetectCallBack;
import com.baidu.idl.main.facesdk.identifylibrary.camera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.identifylibrary.camera.CameraPreviewManager;
import com.baidu.idl.main.facesdk.identifylibrary.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.identifylibrary.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.identifylibrary.manager.SaveImageManager;
import com.baidu.idl.main.facesdk.identifylibrary.model.LivenessModel;
import com.baidu.idl.main.facesdk.identifylibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.identifylibrary.setting.IdentifySettingActivity;
import com.baidu.idl.main.facesdk.identifylibrary.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.identifylibrary.utils.DensityUtils;
import com.baidu.idl.main.facesdk.identifylibrary.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.main.facesdk.identifylibrary.utils.ImageUtils;
import com.baidu.idl.main.facesdk.identifylibrary.utils.LogUtils;
import com.baidu.idl.main.facesdk.identifylibrary.utils.ToastUtils;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.dk.uartnfc.SamVIdCard;
import com.dk.uartnfc.SerialManager;

import java.io.FileNotFoundException;
import java.util.Arrays;

import static com.DKCloudID.ClientDispatcher.SAM_V_FRAME_START_CODE;
import static com.DKCloudID.ClientDispatcher.SAM_V_INIT_COM;

public class FaceRGBPersonActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;
    private ImageView testimony_backIv;
    private ImageView testimony_settingIv;
    private ImageView testimony_addIv;

    private static final int PICK_PHOTO_FRIST = 100;
    private static final int PICK_VIDEO_FRIST = 101;

    private byte[] firstFeature = new byte[512];
    private byte[] secondFeature = new byte[512];

    private ImageView testimony_developmentLineIv;
    private TextView testimony_developmentTv;
    private ImageView testimony_previewLineIv;
    private TextView testimony_previewTv;
    private RelativeLayout testimony_rl;
    private RelativeLayout testimony_showRl;
    private ImageView testimony_showImg;
    private TextView testimony_showAgainTv;
    private TextView testimony_upload_filesTv;

    private volatile boolean firstFeatureFinished = false;
    private volatile boolean secondFeatureFinished = false;
    private RelativeLayout testimony_tips_failRl;

    private AutoTexturePreviewView mPreviewView;
    // RGB????????????????????????
    // ???????????????????????????????????????????????????640*480??? 1280*720
    private static final int mWidth = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int mHeight = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    private int mLiveType;
    private TextureView mDrawDetectFaceView;
    private Paint paint;
    private RectF rectF;

    private ConstraintLayout personButtomLl;
    private TextView person_baiduTv;
    private ImageView testImageview;
    private Paint paintBg;
    private TextView tv_rgb_live_time;
    private TextView tv_rgb_live_score;
    private RelativeLayout kaifa_relativeLayout;
    private TextView hintAdainIv;
    private ImageView hintShowIv;
    private TextView testimonyTipsFailTv;
    private TextView testimonyTipsPleaseFailTv;
    private float score = 0;
    // ?????????????????????????????????????????????????????????
    boolean isDevelopment = false;
    private RelativeLayout testRelativeLayout;
    private View view;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private ImageView person_kaifaIv;
    private TextView tv_feature_time;
    private TextView tv_feature_search_time;
    private TextView tv_all_time;
    private ImageView developmentAddIv;
    private RelativeLayout hintShowRl;
    private RelativeLayout developmentAddRl;
    private float mRgbLiveScore;
    private ImageView testimonyTipsFailIv;
    // ?????????????????????
    private boolean isFace = false;
    private float rgbLivenessScore = 0.0f;
    // ????????????
    private long featureTime;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("FaceRGBPersonActivity","FaceRGBPersonActivity");
        initListener();
        setContentView(R.layout.activity_face_rgb_identifylibrary);
        FaceSDKManager.getInstance().emptyFrame();
        mContext = this;
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
            testimony_rl.setLayoutParams(params);
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
                    ToastUtils.toast(FaceRGBPersonActivity.this, "?????????????????????????????????");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        ToastUtils.toast(FaceRGBPersonActivity.this, "??????????????????????????????????????????");
                    }
                }
            });
        }
    }

    private void initView() {
        // ??????????????????
        testimony_rl = findViewById(R.id.testimony_Rl);
        // ????????????
        paint = new Paint();
        rectF = new RectF();
        paintBg = new Paint();
        mDrawDetectFaceView = findViewById(R.id.texture_view_draw);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);
        if (SingleBaseConfig.getBaseConfig().getRgbRevert()) {
            mDrawDetectFaceView.setRotationY(180);
        }
        // ???????????????RGB ????????????
        mPreviewView = findViewById(R.id.auto_rgb_preview_view);
        // ??????
        testimony_backIv = findViewById(R.id.btn_back);
        testimony_backIv.setOnClickListener(this);
        // ??????
        testimony_settingIv = findViewById(R.id.btn_setting);
        testimony_settingIv.setOnClickListener(this);
        // ????????????
        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        // ????????????
        mRgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // buttom
        personButtomLl = findViewById(R.id.person_buttomLl);
        // ????????????????????????
        person_baiduTv = findViewById(R.id.person_baiduTv);
        // ??????RGB ????????????
        testImageview = findViewById(R.id.test_rgb_view);
        testRelativeLayout = findViewById(R.id.test_rgb_rl);
        testRelativeLayout.setVisibility(View.GONE);
        person_kaifaIv = findViewById(R.id.person_kaifaIv);
        view = findViewById(R.id.mongolia_view);
        // ????????????
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(this);
        saveCamera.setVisibility(View.GONE);
        spot = findViewById(R.id.spot);

        // ****************????????????****************
        testimony_previewTv = findViewById(R.id.preview_text);
        testimony_previewTv.setOnClickListener(this);
        testimony_previewLineIv = findViewById(R.id.preview_view);
        // ??????????????????  +?????????
        testimony_addIv = findViewById(R.id.testimony_addIv);
        testimony_addIv.setOnClickListener(this);
        testimony_showRl = findViewById(R.id.testimony_showRl);
        testimony_showImg = findViewById(R.id.testimony_showImg);
        testimony_showAgainTv = findViewById(R.id.testimony_showAgainTv);
        testimony_showAgainTv.setOnClickListener(this);
        testimony_upload_filesTv = findViewById(R.id.testimony_upload_filesTv);
        // ????????????
        testimony_tips_failRl = findViewById(R.id.testimony_tips_failRl);
        testimonyTipsFailTv = findViewById(R.id.testimony_tips_failTv);
        testimonyTipsPleaseFailTv = findViewById(R.id.testimony_tips_please_failTv);
        testimonyTipsFailIv = findViewById(R.id.testimony_tips_failIv);

        // ****************????????????****************
        testimony_developmentTv = findViewById(R.id.develop_text);
        testimony_developmentTv.setOnClickListener(this);
        testimony_developmentLineIv = findViewById(R.id.develop_view);
        // ???????????????
        tv_rgb_live_time = findViewById(R.id.tv_rgb_live_time);
        // ??????????????????
        tv_rgb_live_score = findViewById(R.id.tv_rgb_live_score);
        // ??????????????????
        tv_feature_time = findViewById(R.id.tv_feature_time);
        // ??????????????????
        tv_feature_search_time = findViewById(R.id.tv_feature_search_time);
        // ?????????
        tv_all_time = findViewById(R.id.tv_all_time);
        // ????????????
        hintAdainIv = findViewById(R.id.hint_adainTv);
        hintAdainIv.setOnClickListener(this);
        // ????????????
        hintShowIv = findViewById(R.id.hint_showIv);
        kaifa_relativeLayout = findViewById(R.id.kaifa_relativeLayout);
        // ??????
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        textCompareStatus = findViewById(R.id.text_compare_status);
        // ????????????
        developmentAddIv = findViewById(R.id.Development_addIv);
        developmentAddIv.setOnClickListener(this);
        hintShowRl = findViewById(R.id.hint_showRl);
        developmentAddRl = findViewById(R.id.Development_addRl);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraPreview();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_back) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK????????????????????????????????????",
                        Toast.LENGTH_LONG).show();
                return;
            }
            finish();
        } else if (id == R.id.btn_setting) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK????????????????????????????????????",
                        Toast.LENGTH_LONG).show();
                return;
            }
            // ??????????????????
            startActivity(new Intent(mContext, IdentifySettingActivity.class));
            finish();
            // ????????????
        } else if (id == R.id.testimony_addIv) {
            secondFeatureFinished = false;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_PHOTO_FRIST);
            // ????????????
        } else if (id == R.id.develop_text) {
            isDevelopment = true;
            if (testimony_showImg.getDrawable() != null || hintShowIv.getDrawable() != null) {
                testimony_tips_failRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.VISIBLE);
            } else {
                testimony_tips_failRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.GONE);
            }
            // title??????
            testimony_developmentLineIv.setVisibility(View.VISIBLE);
            testimony_previewLineIv.setVisibility(View.GONE);
            testimony_developmentTv.setTextColor(getResources().getColor(R.color.white));
            testimony_previewTv.setTextColor(Color.parseColor("#FF999999"));
            // ??????????????????????????????
            person_baiduTv.setVisibility(View.GONE);
            // ??????????????????buttom??????
            personButtomLl.setVisibility(View.GONE);
            // ??????????????????buttom??????
            kaifa_relativeLayout.setVisibility(View.VISIBLE);
            // RGB ??????????????????
            testRelativeLayout.setVisibility(View.VISIBLE);
            // ????????????????????????
            saveCamera.setVisibility(View.VISIBLE);
            judgeFirst();
            // ????????????
        } else if (id == R.id.preview_text) {
            isDevelopment = false;
            if (testimony_showImg.getDrawable() != null || hintShowIv.getDrawable() != null) {
                testimony_tips_failRl.setVisibility(View.VISIBLE);
                layoutCompareStatus.setVisibility(View.GONE);
            } else {
                testimony_tips_failRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.GONE);
            }
            // title??????
            testimony_developmentLineIv.setVisibility(View.GONE);
            testimony_previewLineIv.setVisibility(View.VISIBLE);
            testimony_developmentTv.setTextColor(Color.parseColor("#FF999999"));
            testimony_previewTv.setTextColor(getResources().getColor(R.color.white));
            // ??????????????????????????????
            person_baiduTv.setVisibility(View.VISIBLE);
            // RGB ??????????????????
            testRelativeLayout.setVisibility(View.GONE);
            // ??????????????????buttom??????
            personButtomLl.setVisibility(View.VISIBLE);
            // ??????????????????buttom??????
            kaifa_relativeLayout.setVisibility(View.GONE);
            // ????????????????????????
            // ??????????????????
            saveCamera.setVisibility(View.GONE);
            isSaveImage = false;
            spot.setVisibility(View.GONE);
        } else if (id == R.id.testimony_showAgainTv) {
            secondFeatureFinished = false;
            Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent2, PICK_PHOTO_FRIST);
            // ????????????
        } else if (id == R.id.hint_adainTv) {
            secondFeatureFinished = false;
            Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent1, PICK_PHOTO_FRIST);
            // ????????????????????????
        } else if (id == R.id.Development_addIv) {
            secondFeatureFinished = false;
            Intent intent3 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent3, PICK_PHOTO_FRIST);
        } else if (id == R.id.save_camera) {
            isSaveImage = !isSaveImage;
            if (isSaveImage) {
                spot.setVisibility(View.VISIBLE);
                ToastUtils.toast(FaceRGBPersonActivity.this, "??????????????????????????????????????????");
            } else {
                spot.setVisibility(View.GONE);
            }
        }
    }

    private void judgeFirst() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isIdentifyFirstSave", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            setFirstView(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setFirstView(View.GONE);
                }
            }, 3000);
            editor.putBoolean("isIdentifyFirstSave", false);
            editor.commit();
        }
    }

    private void setFirstView(int visibility) {
        findViewById(R.id.first_text_tips).setVisibility(visibility);
        findViewById(R.id.first_circular_tips).setVisibility(visibility);
    }

    /**
     * ?????????????????????
     */
    private void startCameraPreview() {
        // ?????????????????????
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // ?????????????????????
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // ??????USB?????????
        // TODO ??? ????????????
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        } else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        }
        CameraPreviewManager.getInstance().startPreview(this, mPreviewView, mWidth, mHeight, new CameraDataCallback() {
            @Override
            public void onGetCameraData(final byte[] data, Camera camera, final int width, final int height) {
                // ??????????????????????????????????????????????????????
                if (testimony_showImg.getDrawable() != null || hintShowIv.getDrawable() != null) {
                    firstFeatureFinished = false;
                    // rgb???????????????
//                    testImageview.setVisibility(View.VISIBLE);
                    // ?????????????????????
                    // ???????????????????????????????????????
                    FaceSDKManager.getInstance().onDetectCheck(data, null, null, secondFeature,
                            height, width, mLiveType, new FaceDetectCallBack() {
                                @Override
                                public void onFaceDetectCallback(final LivenessModel livenessModel) {
                                    // ????????????
                                    checkCloseDebugResult(livenessModel);
                                    // ????????????
                                    checkOpenDebugResult(livenessModel);
                                    if (isSaveImage) {
                                        SaveImageManager.getInstance().saveImage(livenessModel);
                                    }
                                }

                                @Override
                                public void onTip(int code, final String msg) {

                                }

                                @Override
                                public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                                    // ???????????????
                                    showFrame(livenessModel);
                                }
                            });


                } else {
                    // ??????????????????????????????????????????????????????????????????
                    testImageview.setImageResource(R.mipmap.ic_image_video);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.85f, 0.0f);
                    animator.setDuration(3000);
                    view.setBackgroundColor(Color.parseColor("#ffffff"));
                    animator.start();
                }
//                // ?????????????????? ????????????????????????????????????SDK???????????????????????????
//                boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
//                if (isRGBDisplay) {
//                    showDetectImage(data);
//                }
            }
        });
    }

    /**
     * ???????????????????????????????????????????????????sdk???????????????????????????????????????????????????????????????????????????????????????
     *
     * @param rgb
     */
    private void showDetectImage(byte[] rgb) {
        if (rgb == null) {
            return;
        }
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, mHeight,
                mWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                SingleBaseConfig.getBaseConfig().getDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorVideoRGB());
        BDFaceImageInstance imageInstance = rgbInstance.getImage();
        final Bitmap bitmap = BitmapUtils.getInstaceBmp(imageInstance);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testImageview.setVisibility(View.VISIBLE);
                testImageview.setImageBitmap(bitmap);
            }
        });
        // ???????????????????????????????????????????????????????????????????????????
        rgbInstance.destory();
    }

    // ????????????
    private void checkCloseDebugResult(final LivenessModel model) {
        // ?????????????????????UI??????
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (model == null) {
                    // ????????????
                    testimony_tips_failRl.setVisibility(View.GONE);
                    if (testimony_previewLineIv.getVisibility() == View.VISIBLE) {
                        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.85f, 0.0f);
                        animator.setDuration(3000);
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                            }

                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                view.setBackgroundColor(Color.parseColor("#ffffff"));
                            }
                        });
                        animator.start();
                    }
                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            score = model.getScore();

                            if (isDevelopment == false) {
                                layoutCompareStatus.setVisibility(View.GONE);
                                testimony_tips_failRl.setVisibility(View.VISIBLE);
                                if (isFace == true) {
                                    testimonyTipsFailTv.setText("???????????????????????????");
                                    testimonyTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                                    testimonyTipsPleaseFailTv.setText("????????????????????????");
                                    testimonyTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                } else {
                                    if (mLiveType == 0) {
                                        if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                                            testimonyTipsFailTv.setText("??????????????????");
                                            testimonyTipsFailTv.setTextColor(
                                                    Color.parseColor("#FF00BAF2"));
                                            testimonyTipsPleaseFailTv.setText("????????????");
                                            testimonyTipsFailIv.setImageResource(R.mipmap.tips_success);
                                        } else {
                                            testimonyTipsFailTv.setText("?????????????????????");
                                            testimonyTipsFailTv.setTextColor(
                                                    Color.parseColor("#FFFEC133"));
                                            testimonyTipsPleaseFailTv.setText("???????????????????????????");
                                            testimonyTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                        }
                                    } else {
                                        // ????????????????????????
                                        rgbLivenessScore = model.getRgbLivenessScore();
                                        if (rgbLivenessScore < mRgbLiveScore) {
                                            testimonyTipsFailTv.setText("?????????????????????");
                                            testimonyTipsFailTv.setTextColor(
                                                    Color.parseColor("#FFFEC133"));
                                            testimonyTipsPleaseFailTv.setText("???????????????????????????");
                                            testimonyTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                        } else {
                                            if (score > SingleBaseConfig.getBaseConfig()
                                                    .getIdThreshold()) {
                                                testimonyTipsFailTv.setText("??????????????????");
                                                testimonyTipsFailTv.setTextColor(
                                                        Color.parseColor("#FF00BAF2"));
                                                testimonyTipsPleaseFailTv.setText("????????????");
                                                testimonyTipsFailIv.setImageResource(
                                                        R.mipmap.tips_success);
                                            } else {
                                                testimonyTipsFailTv.setText("?????????????????????");
                                                testimonyTipsFailTv.setTextColor(
                                                        Color.parseColor("#FFFEC133"));
                                                testimonyTipsPleaseFailTv.setText("???????????????????????????");
                                                testimonyTipsFailIv.setImageResource(
                                                        R.mipmap.tips_fail);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });


                }
            }
        });
    }

    // ????????????
    private void checkOpenDebugResult(final LivenessModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (model == null) {
                    // ????????????
                    layoutCompareStatus.setVisibility(View.GONE);
                    // ??????
                    person_kaifaIv.setVisibility(View.GONE);
                    // ??????????????????
                    testImageview.setImageResource(R.mipmap.ic_image_video);
                    // ????????????0
                    tv_rgb_live_time.setText(String.format("??????????????????%s", 0));
                    tv_rgb_live_score.setText(String.format("?????????????????????%s ms", 0));
                    tv_feature_time.setText(String.format("?????????????????????%s ms", 0));
                    tv_feature_search_time.setText(String.format("?????????????????????%s ms", 0));
                    tv_all_time.setText(String.format("????????????%s ms", 0));
                } else {
                    // rgb?????????????????????
                    BDFaceImageInstance image = model.getBdFaceImageInstance();
                    if (image != null) {
                        testImageview.setImageBitmap(BitmapUtils.getInstaceBmp(image));
                    }
                    tv_rgb_live_time.setText(String.format("??????????????????%s", score));
                    tv_rgb_live_score.setText(String.format("?????????????????????%s ms", model.getRgbLivenessDuration()));
                    tv_feature_time.setText(String.format("?????????????????????%s ms", model.getFeatureDuration()));
                    tv_feature_search_time.setText(String.format("?????????????????????%s ms", model.getStartCompareTime()));

                    // ??????????????????
                    if (firstFeature == null || secondFeature == null) {
                        return;
                    }

                    if (isDevelopment) {
                        testimony_tips_failRl.setVisibility(View.GONE);
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        if (!model.isQualityCheck()) {
                            tv_feature_time.setText(String.format("?????????????????????%s ms", 0));
                            tv_feature_search_time.setText(String.format("?????????????????????%s ms", 0));
                            long l = model.getRgbDetectDuration() + model.getRgbLivenessDuration();
                            tv_all_time.setText(String.format("????????????%s ms", l));
                            person_kaifaIv.setVisibility(View.VISIBLE);
                            person_kaifaIv.setImageResource(R.mipmap.ic_icon_develop_fail);
                            textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                            /*textCompareStatus.setMaxEms(6)*/
                            ;
                            textCompareStatus.setText("??????????????????");
                        } else if (isFace == true) {
                            textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                            textCompareStatus.setText("????????????");
                        } else {
                            if (mLiveType == 0) {
                                tv_all_time.setText(String.format("????????????%s ms", model.getAllDetectDuration()));
                                if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                                    textCompareStatus.setTextColor(Color.parseColor("#00BAF2"));
                                    textCompareStatus.setText("????????????");
                                } else {
                                    textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                                    textCompareStatus.setText("????????????");
                                }
                            } else {
                                // ????????????????????????
                                rgbLivenessScore = model.getRgbLivenessScore();
                                if (rgbLivenessScore < mRgbLiveScore) {
                                    person_kaifaIv.setVisibility(View.VISIBLE);
                                    person_kaifaIv.setImageResource(R.mipmap.ic_icon_develop_fail);
                                    textCompareStatus.setTextColor(Color.parseColor("#FECD33"));

//                            textCompareStatus.setMaxEms(7);
                                    textCompareStatus.setText("?????????????????????");
                                } else {
                                    person_kaifaIv.setVisibility(View.VISIBLE);
                                    person_kaifaIv.setImageResource(R.mipmap.ic_icon_develop_success);
                                    if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                                        textCompareStatus.setTextColor(Color.parseColor("#00BAF2"));
                                        textCompareStatus.setText("????????????");
                                    } else {
                                        textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                                        textCompareStatus.setText("????????????");
                                    }
                                }
                                tv_feature_time.setText(String.format("?????????????????????%s ms", model.getFeatureDuration()));
                                tv_feature_search_time.setText(String.format("?????????????????????%s ms",
                                        model.getStartCompareTime()));

                                tv_all_time.setText(String.format("????????????%s ms", model.getAllDetectDuration()));
                            }
                        }
                    }
                }
            }
        });
    }

    private void toast(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FRIST && (data != null && data.getData() != null)) {
            Uri uri1 = ImageUtils.geturi(data, this);
            try {
                final Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri1));
                if (bitmap != null) {

                    // ???????????????
                    float ret = FaceSDKManager.getInstance().personDetect(bitmap, secondFeature, this);
                    // ???????????????
                    // ???????????????????????????
                    hintShowIv.setVisibility(View.VISIBLE);
                    testimony_showImg.setVisibility(View.VISIBLE);
                    hintShowIv.setImageBitmap(bitmap);
                    testimony_showImg.setImageBitmap(bitmap);
                    if (ret != -1) {
                        isFace = false;
                        // ??????????????????????????????????????????????????????
                        if (ret == 128) {
                            secondFeatureFinished = true;
                        }
                        if (ret == 128) {
                            toast("????????????????????????");
                            hintShowRl.setVisibility(View.VISIBLE);
                            testimony_showRl.setVisibility(View.VISIBLE);
                            testimony_addIv.setVisibility(View.GONE);
                            testimony_upload_filesTv.setVisibility(View.GONE);
                            developmentAddRl.setVisibility(View.GONE);
                        } else {
                            ToastUtils.toast(mContext, "????????????????????????");
                        }
                    } else {
                        isFace = true;
                        // ???????????????????????????
                        hintShowIv.setVisibility(View.GONE);
                        testimony_showImg.setVisibility(View.GONE);
                        hintShowRl.setVisibility(View.VISIBLE);
                        testimony_showRl.setVisibility(View.VISIBLE);
                        testimony_addIv.setVisibility(View.GONE);
                        testimony_upload_filesTv.setVisibility(View.GONE);
                        developmentAddRl.setVisibility(View.GONE);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraPreviewManager.getInstance().stopPreview();
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
                        mPreviewView, model.getBdFaceImageInstance());
                int liveTypeValue = SingleBaseConfig.getBaseConfig().getType();
                if (score < SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                    paint.setColor(Color.parseColor("#FEC133"));
                    paintBg.setColor(Color.parseColor("#FEC133"));
                } else {
                    paint.setColor(Color.parseColor("#00baf2"));
                    paintBg.setColor(Color.parseColor("#00baf2"));
                }
                paint.setStyle(Paint.Style.FILL);
                paintBg.setStyle(Paint.Style.FILL);
                // ????????????
                paint.setStrokeWidth(8);
                // ?????????????????????????????????
                paint.setAntiAlias(true);
                paintBg.setStrokeWidth(13);
                paintBg.setAlpha(90);
                // ?????????????????????????????????
                paintBg.setAntiAlias(true);
                // ???????????????
                FaceOnDrawTexturViewUtil.drawRect(canvas,
                        rectF, paint, 5f, 50f, 25f);
                // ??????canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }
}