package com.baidu.idl.face.main.attribute.activity.attribute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.attribute.activity.AttributeBaseActivity;
import com.baidu.idl.face.main.attribute.callback.CameraDataCallback;
import com.baidu.idl.face.main.attribute.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.attribute.camera.AttrbuteAutoTexturePreviewView;
import com.baidu.idl.face.main.attribute.camera.CameraPreviewManager;
import com.baidu.idl.face.main.attribute.listener.SdkInitListener;
import com.baidu.idl.face.main.attribute.manager.FaceSDKManager;
import com.baidu.idl.face.main.attribute.manager.SaveImageManager;
import com.baidu.idl.face.main.attribute.model.LivenessModel;
import com.baidu.idl.face.main.attribute.model.SingleBaseConfig;
import com.baidu.idl.face.main.attribute.setting.AttributeSettingActivity;
import com.baidu.idl.face.main.attribute.utils.BitmapUtils;
import com.baidu.idl.face.main.attribute.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.attribute.utils.ToastUtils;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.attrbutelibrary.R;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;

/**
 * author : shangrong
 * date : 2020-02-11 10:30
 * description :
 */
public class FaceAttributeRgbActivity extends AttributeBaseActivity {
    private AttrbuteAutoTexturePreviewView autoTexturePreviewView;
    // RGB????????????????????????
    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    private ImageView faceAttribute;
    private Bitmap roundBitmap;
    private RelativeLayout showAtrMessage;
    private ImageView btn_back;
    private Paint paint;
    private Paint paintBg;
    private RectF rectF;
    private TextureView mDrawDetectFaceView;
    private TextView atrDetectTime;
    private TextView atrToalTime;
    private TextView atrSex;
    private TextView atrAge;
    private TextView atrAccessory;
    private TextView atrEmotion;
    private TextView atrMask;
    private RelativeLayout atrRlDisplay;
    private RelativeLayout atrLinerTime;
    private TextView previewText;
    private TextView developText;
    private TextView homeBaiduTv;
    private ImageView btnSetting;
    private ImageView previewView;
    private ImageView developView;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListener();
        setContentView(R.layout.activity_face_rgb_attribute);

        SingleBaseConfig.getBaseConfig().setAttribute(true);
        FaceSDKManager.getInstance().initConfig();
        FaceSDKManager.isDetectMask = true;
        init();
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
                    ToastUtils.toast(FaceAttributeRgbActivity.this, "?????????????????????????????????");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        ToastUtils.toast(FaceAttributeRgbActivity.this, "??????????????????????????????????????????");
                    }
                }
            });
        }
    }

    public void init() {
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();
        homeBaiduTv = findViewById(R.id.home_baiduTv);
        previewView = findViewById(R.id.preview_view);
        developView = findViewById(R.id.develop_view);
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        showAtrMessage = findViewById(R.id.showAtrMessage);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);
        if (SingleBaseConfig.getBaseConfig().getRgbRevert()){
            mDrawDetectFaceView.setRotationY(180);
        }
        btn_back = findViewById(R.id.btn_back);
        autoTexturePreviewView = findViewById(R.id.fa_auto);
        faceAttribute = findViewById(R.id.face_attribute);
        atrDetectTime = findViewById(R.id.atrDetectTime);
        atrToalTime = findViewById(R.id.atrToalTime);
        atrSex = findViewById(R.id.atrSex);
        atrAge = findViewById(R.id.atrAge);
        atrAccessory = findViewById(R.id.atrAccessory);
        atrEmotion = findViewById(R.id.atrEmotion);
        atrMask = findViewById(R.id.atrMask);
        atrRlDisplay = findViewById(R.id.atrRlDisplay);
        atrLinerTime = findViewById(R.id.atrLinerTime);
        previewText = findViewById(R.id.preview_text);
        developText = findViewById(R.id.develop_text);
        btnSetting = findViewById(R.id.btn_setting);
        // ????????????
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSaveImage = !isSaveImage;
                if (isSaveImage){
                    ToastUtils.toast(FaceAttributeRgbActivity.this, "??????????????????????????????????????????");
                    spot.setVisibility(View.VISIBLE);
                }else {
                    spot.setVisibility(View.GONE);
                }
            }
        });
        spot = findViewById(R.id.spot);

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!FaceSDKManager.initModelSuccess) {
                    Toast.makeText(FaceAttributeRgbActivity.this, "SDK????????????????????????????????????",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(FaceAttributeRgbActivity.this, AttributeSettingActivity.class);
                startActivity(intent);
                finish();
            }
        });
        previewText.setTextColor(Color.parseColor("#FFFFFF"));

        previewText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeBaiduTv.setVisibility(View.VISIBLE);
                atrRlDisplay.setVisibility(View.GONE);
                atrLinerTime.setVisibility(View.GONE);
                previewText.setTextColor(Color.parseColor("#FFFFFF"));
                developText.setTextColor(Color.parseColor("#d3d3d3"));
                previewView.setVisibility(View.VISIBLE);
                developView.setVisibility(View.GONE);
                saveCamera.setVisibility(View.GONE);
                isSaveImage = false;
                spot.setVisibility(View.GONE);
            }
        });

        developText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeBaiduTv.setVisibility(View.GONE);
                atrRlDisplay.setVisibility(View.VISIBLE);
                atrLinerTime.setVisibility(View.VISIBLE);
                developText.setTextColor(Color.parseColor("#FFFFFF"));
                previewText.setTextColor(Color.parseColor("#d3d3d3"));
                previewView.setVisibility(View.GONE);
                developView.setVisibility(View.VISIBLE);
                saveCamera.setVisibility(View.VISIBLE);
                judgeFirst();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!FaceSDKManager.initModelSuccess) {
                    Toast.makeText(FaceAttributeRgbActivity.this, "SDK????????????????????????????????????",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                finish();
            }
        });
    }

    private void judgeFirst(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isAttrbuteFirstSave", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            setFirstView(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setFirstView(View.GONE);
                }
            },3000);
            editor.putBoolean("isAttrbuteFirstSave", false);
            editor.commit();
        }
    }
    private void setFirstView(int visibility){
        findViewById(R.id.first_text_tips).setVisibility(visibility);
        findViewById(R.id.first_circular_tips).setVisibility(visibility);

    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraPreview();
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
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1){
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        }else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        }
        CameraPreviewManager.getInstance().startPreview(this, autoTexturePreviewView,
                RGB_WIDTH, RGB_HEIGHT, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] rgbData, Camera camera, int srcWidth, int srcHeight) {
                        dealRgb(rgbData);
                        showDetectImage(rgbData);
                    }
                });
    }


    private void dealRgb(byte[] rgbData) {
        if (rgbData != null) {
            FaceSDKManager.getInstance().onAttrDetectCheck(rgbData, null, null, RGB_HEIGHT,
                    RGB_WIDTH, 1, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            // ??????????????????
                            if (livenessModel != null) {
                                showAtrDetailMessage(livenessModel.getFaceInfo(), livenessModel.getMaskScore());
                            }
                            showResult(livenessModel);
                            if (isSaveImage){
                                SaveImageManager.getInstance().saveImage(livenessModel);
                            }
                        }

                        @Override
                        public void onTip(int code, String msg) {

                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                            showFrame(livenessModel);
                        }
                    });
        }
    }

//    public String getMsg(FaceInfo faceInfo) {
//        StringBuilder msg = new StringBuilder();
//        if (faceInfo != null) {
//            msg.append(faceInfo.age);
//            msg.append(",").append(faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_CALM ?
//                    "??????"
//                    : faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_SMILE ? "???"
//                    : faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_FROWN ? "??????" : "????????????");
//            msg.append(",").append(faceInfo.gender == BDFaceSDKCommon.BDFaceGender.BDFACE_GENDER_FEMALE ? "??????" :
//                    faceInfo.gender == BDFaceSDKCommon.BDFaceGender.BDFACE_GENDER_MALE ? "??????" : "??????");
//            msg.append(",").append(faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_NO_GLASSES ? "?????????"
//                    : faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_GLASSES ? "?????????"
//                    : faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_SUN_GLASSES ? "??????" : "?????????");
//            msg.append(",").append(faceInfo.race == BDFaceSDKCommon.BDFaceRace.BDFACE_RACE_YELLOW ? "?????????"
//                    : faceInfo.race == BDFaceSDKCommon.BDFaceRace.BDFACE_RACE_WHITE ? "?????????"
//                    : faceInfo.race == BDFaceSDKCommon.BDFaceRace.BDFACE_RACE_BLACK ? "?????????"
//                    : faceInfo.race == BDFaceSDKCommon.BDFaceRace.BDFACE_RACE_INDIAN ? "?????????"
//                    : "?????????");
//        }
//        return msg.toString();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FaceSDKManager.isDetectMask = false;
        CameraPreviewManager.getInstance().stopPreview();
        // ??????????????????
        SingleBaseConfig.getBaseConfig().setAttribute(false);
        FaceSDKManager.getInstance().initConfig();
    }

    private void showDetectImage(byte[] rgb) {
        if (rgb == null) {
            return;
        }
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, RGB_HEIGHT,
                RGB_WIDTH, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectRGB());
        BDFaceImageInstance imageInstance = rgbInstance.getImage();
        roundBitmap = BitmapUtils.getInstaceBmp(imageInstance);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                faceAttribute.setVisibility(View.VISIBLE);
                faceAttribute.setImageBitmap(roundBitmap);
            }
        });
        // ???????????????????????????????????????????????????????????????????????????
        rgbInstance.destory();
    }

//    // bitmap????????????
//    private Bitmap bimapRound(Bitmap mBitmap, float index) {
//        Bitmap bitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_4444);
//
//        Canvas canvas = new Canvas(bitmap);
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//
//        // ??????????????????
//        Rect rect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
//        RectF rectf = new RectF(rect);
//
//        // ???????????????
//        canvas.drawARGB(0, 0, 0, 0);
//        // ?????????
//        canvas.drawRoundRect(rectf, index, index, paint);
//        // ??????????????????????????????
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//
//        // ?????????????????????????????????????????????????????????????????????
//        canvas.drawBitmap(mBitmap, rect, rect, paint);
//        return bitmap;
//    }

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
                    showArtLinerGone();
                    return;
                }
                if (model == null) {
                    // ??????canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    showArtLinerGone();
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // ??????canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    showArtLinerGone();
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // ??????????????????????????????????????????????????????????????????
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        autoTexturePreviewView, model.getBdFaceImageInstance());

                paint.setColor(Color.parseColor("#00baf2"));
                paintBg.setColor(Color.parseColor("#00baf2"));

                paint.setStyle(Paint.Style.FILL);
                paintBg.setStyle(Paint.Style.FILL);
                // ????????????
                paint.setStrokeWidth(8);
                paint.setAntiAlias(true);
                paintBg.setStrokeWidth(13);
                paintBg.setAntiAlias(true);
                paintBg.setAlpha(90);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (showAtrMessage.getVisibility() == View.GONE) {
                            showAtrMessage.setVisibility(View.VISIBLE);
                        }
                    }
                });

                if (faceInfo.width > faceInfo.height) {
                    if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {

                        FaceOnDrawTexturViewUtil.drawRect(canvas,
                                rectF, paint, 5f, 50f , 25f);
                        if (rectF.centerY() < autoTexturePreviewView.getHeight() * 0.6) {
                            showAtrMessage.setTranslationX(rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() + rectF.height() / 2);
                        } else {
                            showAtrMessage.setTranslationX(rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() -
                                    rectF.width() - showAtrMessage.getHeight());
                        }
                    } else {

                        FaceOnDrawTexturViewUtil.drawRect(canvas,
                                rectF, paint, 5f, 50f , 25f);
                        if (rectF.centerY() < autoTexturePreviewView.getHeight() * 0.6) {
                            showAtrMessage.setTranslationX(autoTexturePreviewView.getWidth() - rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() + rectF.width() / 2);
                        } else {
                            showAtrMessage.setTranslationX(autoTexturePreviewView.getWidth()
                                    - rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() -
                                    rectF.width() - showAtrMessage.getHeight());
                        }
                    }
                } else {
                    if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {

                        FaceOnDrawTexturViewUtil.drawRect(canvas,
                                rectF, paint, 5f, 50f , 25f);
                        if (rectF.centerY() < autoTexturePreviewView.getHeight() * 0.6) {
                            showAtrMessage.setTranslationX(rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() + rectF.width() / 2);
                        } else {
                            showAtrMessage.setTranslationX(rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() -
                                    rectF.width() - showAtrMessage.getHeight());
                        }
                    } else {

                        FaceOnDrawTexturViewUtil.drawRect(canvas,
                                rectF, paint, 5f, 50f , 25f);
                        if (rectF.centerY() < autoTexturePreviewView.getHeight() * 0.6) {
                            showAtrMessage.setTranslationX(autoTexturePreviewView.getWidth() - rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() + rectF.width() / 2);
                        } else {
                            showAtrMessage.setTranslationX(autoTexturePreviewView.getWidth() - rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() -
                                    rectF.width() - showAtrMessage.getHeight());
                        }
                    }
                }

                // ??????canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }

    public void showResult(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel != null) {
                    atrDetectTime.setText("???????????????" + livenessModel.getRgbDetectDuration() + "ms");
                    atrToalTime.setText("????????????" + livenessModel.getAllDetectDuration() + "ms");
                } else {
                    atrDetectTime.setText("???????????????" + 0 + "ms");
                    atrToalTime.setText("????????????" + 0 + "ms");
                }
            }
        });
    }

    public void showAtrDetailMessage(final FaceInfo faceInfo, final float maskScore) {
        if (faceInfo != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String sex = faceInfo.gender == BDFaceSDKCommon.BDFaceGender.BDFACE_GENDER_FEMALE ? "???" :
                            faceInfo.gender == BDFaceSDKCommon.BDFaceGender.BDFACE_GENDER_MALE ? "???" : "??????";
//                    String accessory = faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_NO_GLASSES ? "???"
//                            : faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_GLASSES ? "???"
//                            : faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_SUN_GLASSES ? "??????" : "?????????";
                    String emotion = faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_CALM ?
                            "??????"
                            : faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_SMILE ? "???"
                            : faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion
                            .BDFACE_EMOTION_FROWN ? "??????" : "????????????";

                    atrSex.setText("?????????" + sex);
                    atrAge.setText("?????????" + faceInfo.age + "???");
                    if (faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_NO_GLASSES) {
                        atrAccessory.setText("????????????");
                    } else {
                        atrAccessory.setText("????????????");
                    }
//                    atrAccessory.setText("?????????" + accessory);
                    atrEmotion.setText("?????????" + emotion);
                    if (maskScore > 0.9) {
                        atrMask.setText("?????????" + "???");
                    } else {
                        atrMask.setText("?????????" + "???");
                    }
                }
            });

        }
    }

    public void showArtLinerGone() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAtrMessage.setVisibility(View.GONE);
            }
        });
    }
}
