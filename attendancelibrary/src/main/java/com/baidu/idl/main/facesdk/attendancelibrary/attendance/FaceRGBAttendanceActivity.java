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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.Tool.TtsManager;
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
import com.baidu.idl.main.facesdk.attendancelibrary.utils.VisitRegisterRecordBean;
import com.baidu.idl.main.facesdk.attendancelibrary.view.CircleImageView;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.registerlibrary.user.manager.VisitorFaceSDKManager;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.model.User;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import createbest.sdk.bihu.temperature.ITemperature;
import createbest.sdk.bihu.temperature.Temperature_RB32x3290A;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FaceRGBAttendanceActivity extends BaseActivity implements View.OnClickListener {

    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PERFER_HEIGH = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    private Context mContext;

    private TextureView mDrawDetectFaceView;
    private AutoTexturePreviewView mAutoCameraPreviewView;
    private ImageView mFaceDetectImageView;
    private TextView mTvDetect;
    private TextView mTvLive;
    private TextView mTvLiveScore;
    private TextView mTvFeature;
    private TextView mTvAll;
    private TextView mTvAllTime;

    private RectF rectF;
    private Paint paint;
    private RelativeLayout relativeLayout;
    private int mLiveType;
    private float mRgbLiveScore;

    private boolean isCheck = false;
    private boolean isCompareCheck = false;
    private TextView preText;
    private TextView deveLop;
    private RelativeLayout preViewRelativeLayout;
    private RelativeLayout deveLopRelativeLayout;
    private RelativeLayout textHuanying;
    private CircleImageView nameImage;
    private TextView nameText;
    private RelativeLayout userNameLayout;
    private TextView detectSurfaceText;
    private ImageView isCheckImage;
    private TextView attendanceTime;
    private TextView attendanceDate;
    private TextView attendanceTimeText;
    private RelativeLayout outRelativelayout;
    private ImageView previewView;
    private ImageView developView;
    private TextView mNum;
    private Paint paintBg;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private User mUser;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;
    private TextView tv_body_temperature;

    /**
     * 测温
     * */
    private ITemperature temperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initListener();
        FaceSDKManager.getInstance().initDataBases(this);
        setContentView(R.layout.activity_face_rgb_attendancelibrary);
        initView();
        // 屏幕的宽
        int displayWidth = DensityUtils.getDisplayWidth(mContext);
        // 屏幕的高
        int displayHeight = DensityUtils.getDisplayHeight(mContext);
        // 当屏幕的宽大于屏幕宽时
        if (displayHeight < displayWidth) {
            // 获取高
            int height = displayHeight;
            // 获取宽
            int width = (int) (displayHeight * ((9.0f / 16.0f)));
            // 设置布局的宽和高
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            // 设置布局居中
            params.gravity = Gravity.CENTER;
            relativeLayout.setLayoutParams(params);
        }

        initTemperature();
        //在应用启动时初始化一次
        TtsManager.getInstance(this).init();
        //初始化注册人脸识别库
//        queryVisitRegisterRecordList();
//        initListener();
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
                                TtsManager.getInstance(FaceRGBAttendanceActivity.this).speakText("体温异常");
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

    /**
     * 根据身份证号 校验是否预约
     *  certificateNumber; 证件号码
     * visitStatus 到访状态；0未到访；1已到访
     * */
    public void updateByCertificateNumber(String cardNumber){
        String hostUrl = "http://8.141.167.159:8990/organ/visitRegisterRecord/updateByCertificateNumber";
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");

        RequestBody formBody = new FormBody.Builder()
                .add("certificateNumber",cardNumber)
                .add("visitStatus","1")
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
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //验证通过
                if (response.isSuccessful()){
                    Log.i("onResponse","isSuccessful");
                    String result = response.body().string();
                    JsonRootBean newsBeanList = JsonUtils.deserialize(result, JsonRootBean.class);
                    //处理UI需要切换到UI线程处理
                    Log.i("onResponse",result);
                }else{

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("onResponse","-------------------------------");
                //...
            }

        });
    }



    /**
     * 查询已预约人员
     * */
    public void queryVisitRegisterRecordList(){
//        FaceApi.getInstance().userClean();
        String hostUrl = "http://8.141.167.159:8990/organ/visitRegisterRecord/queryVisitRegisterRecordList";
        OkHttpClient client = new OkHttpClient();
        Map<String,String> paramsMap = new HashMap<>();
        paramsMap.put("visitStatus","0");
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
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //验证通过
                if (response.isSuccessful()){
                    Log.i("onResponse","isSuccessful");
                    String result = response.body().string();
                    JsonRootBean newsBeanList = JsonUtils.deserialize(result, JsonRootBean.class);
                    if (CollectionUtils.isNotEmpty(newsBeanList.getData().getList())){
                        getFeatures(newsBeanList.getData().getList());
                    }
                }else{

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("onResponse","-------------------------------");
                //...
            }

        });
    }



    /**
     * 提取特征值
     *
     */
    private void getFeatures(List<VisitRegisterRecordBean> list) {
        for (VisitRegisterRecordBean visitRegisterRecordBean : list) {
            Bitmap mCropBitmap = ImageUtils.getBitmap(getFileByUrl(visitRegisterRecordBean.getPersonalPhotos()));
            // 开始导入
            VisitorFaceSDKManager.getInstance().asyncImport(mCropBitmap,visitRegisterRecordBean.getName(),visitRegisterRecordBean.getCertificateNumber()+".jpg");
        }

        //人脸库加载
        FaceSDKManager.getInstance().initDataBases(this);
    }

    //url转file
    private File getFileByUrl(String fileUrl) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BufferedOutputStream stream = null;
        InputStream inputStream = null;
        File file = null;
        try {
            URL imageUrl = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            inputStream = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            file = File.createTempFile("file", fileUrl.substring(fileUrl.lastIndexOf("."), fileUrl.length()));
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fileOutputStream);
            stream.write(outStream.toByteArray());
        } catch (Exception e) {
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (stream != null) {
                    stream.close();
                }
                outStream.close();
            } catch (Exception e) {
            }
        }
        return file;
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
                    ToastUtils.toast(FaceRGBAttendanceActivity.this, "模型加载成功，欢迎使用");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        ToastUtils.toast(FaceRGBAttendanceActivity.this, "模型加载失败，请尝试重启应用");
                    }
                }
            });
        }
    }

    /**
     * View
     */
    private void initView() {
        // 获取整个布局
        relativeLayout = findViewById(R.id.all_relative);
        // 画人脸框
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);
        if (SingleBaseConfig.getBaseConfig().getRgbRevert()){
            mDrawDetectFaceView.setRotationY(180);
        }
        // 单目摄像头RGB 图像预览
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);

        // 返回
        ImageView mButReturn = findViewById(R.id.btn_back);
        mButReturn.setOnClickListener(this);
        // 设置
        ImageView mBtSetting = findViewById(R.id.btn_setting);
        mBtSetting.setOnClickListener(this);
        // 预览模式
        preText = findViewById(R.id.preview_text);
        preText.setOnClickListener(this);
        preText.setTextColor(Color.parseColor("#ffffff"));
        preViewRelativeLayout = findViewById(R.id.yvlan_relativeLayout);
        previewView = findViewById(R.id.preview_view);

        //当前体温
        tv_body_temperature = findViewById(R.id.tv_body_temperature);

        // 开发模式
        deveLop = findViewById(R.id.develop_text);
        deveLop.setOnClickListener(this);
        deveLopRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        developView = findViewById(R.id.develop_view);
        developView.setVisibility(View.GONE);
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        layoutCompareStatus.setVisibility(View.GONE);
        textCompareStatus = findViewById(R.id.text_compare_status);

        // ***************开发模式*************
        isCheckImage = findViewById(R.id.is_check_image);
        // 活体状态
        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        // 活体阈值
        mRgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // 送检RGB 图像回显
        mFaceDetectImageView = findViewById(R.id.face_detect_image_view);
        mFaceDetectImageView.setVisibility(View.VISIBLE);
        // 存在底库的数量
        mNum = findViewById(R.id.tv_num);
        mNum.setText(String.format("底库 ： %s 个样本", FaceApi.getInstance().getmUserNum()));

        // 检测耗时
        mTvDetect = findViewById(R.id.tv_detect_time);
        // RGB活体
        mTvLive = findViewById(R.id.tv_rgb_live_time);
        mTvLiveScore = findViewById(R.id.tv_rgb_live_score);
        // 特征提取
        mTvFeature = findViewById(R.id.tv_feature_time);
        // 检索
        mTvAll = findViewById(R.id.tv_feature_search_time);
        // 总耗时
        mTvAllTime = findViewById(R.id.tv_all_time);
        // 存图按钮
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(this);
        spot = findViewById(R.id.spot);


        // ***************预览模式*************
        textHuanying = findViewById(R.id.huanying_relative);
        userNameLayout = findViewById(R.id.user_name_layout);
        nameImage = findViewById(R.id.detect_reg_image_item);
        nameText = findViewById(R.id.name_text);
        detectSurfaceText = findViewById(R.id.detect_surface_text);
        mFaceDetectImageView.setVisibility(View.GONE);
        saveCamera.setVisibility(View.GONE);
        detectSurfaceText.setVisibility(View.GONE);
        attendanceTime = findViewById(R.id.attendance_time);
        attendanceDate = findViewById(R.id.attendance_date);
        attendanceTimeText = findViewById(R.id.attendance_time_text);
        outRelativelayout = findViewById(R.id.out_relativelayout);

    }

    @Override
    protected void onResume() {
        super.onResume();
        startTestOpenDebugRegisterFunction();
    }

    private void startTestOpenDebugRegisterFunction() {
        // TODO ： 临时放置
        //  CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1){
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        }else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        }
        CameraPreviewManager.getInstance().startPreview(mContext, mAutoCameraPreviewView,
                PREFER_WIDTH, PERFER_HEIGH, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        // 摄像头预览数据进行人脸检测
                        FaceSDKManager.getInstance().onDetectCheck(data, null, null,
                                height, width, mLiveType, new FaceDetectCallBack() {
                                    @Override
                                    public void onFaceDetectCallback(LivenessModel livenessModel) {
                                        // 预览模式
                                        checkCloseDebugResult(livenessModel);

                                        // 开发模式
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
                                        // 绘制人脸框
                                        showFrame(livenessModel);


                                    }
                                });
                    }
                });
    }

    // ***************预览模式结果输出*************
    private void checkCloseDebugResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
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
                        nameText.setText("验证失败");
                        attendanceTimeText.setText("持续识别中......");
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
                    nameText.setText(FileUtils.spotString(user.getUserName()) + " 验证成功");
                    TtsManager.getInstance(FaceRGBAttendanceActivity.this).speakText("核验通过");
                    attendanceTimeText.setText("验证时间：" + TimeUtils.getTimeShort(date));

                    String certificateNumber = user.getImageName().substring(0, user.getImageName().lastIndexOf("."));
                    //更新到访记录
                    updateByCertificateNumber(certificateNumber);
                }

            }
        });
    }

    // ***************开发模式结果输出*************
    private void checkOpenDebugResult(final LivenessModel livenessModel) {

        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    layoutCompareStatus.setVisibility(View.GONE);
                    isCheckImage.setVisibility(View.GONE);
                    mFaceDetectImageView.setImageResource(R.mipmap.ic_image_video);
                    mTvDetect.setText(String.format("检测耗时 ：%s ms", 0));
                    mTvLive.setText(String.format("RGB活体检测耗时 ：%s ms", 0));
                    mTvLiveScore.setText(String.format("RGB活体得分 ：%s", 0));
                    mTvFeature.setText(String.format("特征抽取耗时 ：%s ms", 0));
                    mTvAll.setText(String.format("特征比对耗时 ：%s ms", 0));
                    mTvAllTime.setText(String.format("总耗时 ：%s ms", 0));
                    return;
                }

                BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();
                if (image != null) {
                    mFaceDetectImageView.setImageBitmap(BitmapUtils.getInstaceBmp(image));
                    image.destory();
                }
                if (! livenessModel.isQualityCheck()){
                    if (isCheck) {
                        isCheckImage.setVisibility(View.VISIBLE);
                        isCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                    }
                    if (isCompareCheck) {
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
//                        textCompareStatus.setMaxEms(6);
                        textCompareStatus.setText("请正视摄像头");
                    }
                } else if (mLiveType == 0) {
                    User user = livenessModel.getUser();
                    if (user == null) {
                        mUser = null;
                        if (isCompareCheck) {
                            layoutCompareStatus.setVisibility(View.VISIBLE);
                            textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
                            textCompareStatus.setText("识别未通过");
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

                } else {
                    float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                    if (rgbLivenessScore < mRgbLiveScore) {
                        if (isCheck) {
                            isCheckImage.setVisibility(View.VISIBLE);
                            isCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                        }
                        if (isCompareCheck) {
                            layoutCompareStatus.setVisibility(View.VISIBLE);
                            textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
//                            textCompareStatus.setMaxEms(10);

//                            textCompareStatus.setMaxEms(7);
                            textCompareStatus.setText("活体检测未通过");
                        }
                    } else {
                        if (isCheck) {
                            isCheckImage.setVisibility(View.VISIBLE);
                            isCheckImage.setImageResource(R.mipmap.ic_icon_develop_success);
                        }

                        User user = livenessModel.getUser();
                        if (user == null) {
                            mUser = null;
                            if (isCompareCheck) {
                                if (livenessModel.isMultiFrame()) {
                                    layoutCompareStatus.setVisibility(View.VISIBLE);
                                    textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
                                    textCompareStatus.setText("识别未通过");
                                } else {
                                    layoutCompareStatus.setVisibility(View.GONE);
                                }
                            }

                        } else {
                            mUser = user;
                            if (isCompareCheck) {
                                layoutCompareStatus.setVisibility(View.VISIBLE);
                                textCompareStatus.setTextColor(Color.parseColor("#FF00BAF2"));
//                                textCompareStatus.setMaxEms(5);
                                textCompareStatus.setText(FileUtils.spotString(mUser.getUserName()));
                            }
                        }
                    }
                }
                mTvDetect.setText(String.format("检测耗时 ：%s ms", livenessModel.getRgbDetectDuration()));
                mTvLive.setText(String.format("RGB活体检测耗时 ：%s ms", livenessModel.getRgbLivenessDuration()));
                mTvLiveScore.setText(String.format("RGB活体得分 ：%s", livenessModel.getRgbLivenessScore()));
                mTvFeature.setText(String.format("特征抽取耗时 ：%s ms", livenessModel.getFeatureDuration()));
                mTvAll.setText(String.format("特征比对耗时 ：%s ms", livenessModel.getCheckDuration()));
                mTvAllTime.setText(String.format("总耗时 ：%s ms", livenessModel.getAllDetectDuration()));
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        // 返回
        if (id == R.id.btn_back) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                        Toast.LENGTH_LONG).show();
                return;
            }
            finish();
            // 设置
        } else if (id == R.id.btn_setting) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                        Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(mContext, AttendanceSettingActivity.class));
            finish();
        } else if (id == R.id.preview_text) {
            isCheckImage.setVisibility(View.GONE);
            mFaceDetectImageView.setVisibility(View.GONE);
            saveCamera.setVisibility(View.GONE);
            detectSurfaceText.setVisibility(View.GONE);
            previewView.setVisibility(View.VISIBLE);
            developView.setVisibility(View.GONE);
            layoutCompareStatus.setVisibility(View.GONE);
            deveLop.setTextColor(Color.parseColor("#a9a9a9"));
            preText.setTextColor(Color.parseColor("#ffffff"));
            preViewRelativeLayout.setVisibility(View.VISIBLE);
            deveLopRelativeLayout.setVisibility(View.GONE);
            outRelativelayout.setVisibility(View.VISIBLE);
            isCheck = false;
            isCompareCheck = false;
            isSaveImage = false;
            spot.setVisibility(View.GONE);
        } else if (id == R.id.develop_text) {
            isCheck = true;
            isCompareCheck = true;
            isCheckImage.setVisibility(View.VISIBLE);
            mFaceDetectImageView.setVisibility(View.VISIBLE);
            saveCamera.setVisibility(View.VISIBLE);
            detectSurfaceText.setVisibility(View.VISIBLE);
            previewView.setVisibility(View.GONE);
            developView.setVisibility(View.VISIBLE);
            deveLop.setTextColor(Color.parseColor("#ffffff"));
            preText.setTextColor(Color.parseColor("#a9a9a9"));
            deveLopRelativeLayout.setVisibility(View.VISIBLE);
            preViewRelativeLayout.setVisibility(View.GONE);
            outRelativelayout.setVisibility(View.GONE);
            judgeFirst();
        } else if (id == R.id.save_camera){
            isSaveImage = !isSaveImage;
            if (isSaveImage){
                ToastUtils.toast(FaceRGBAttendanceActivity.this, "存图功能已开启再次点击可关闭");
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
     * 绘制人脸框
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
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // 检测图片的坐标和显示的坐标不一样，需要转换。
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mAutoCameraPreviewView, model.getBdFaceImageInstance());
                // 人脸框颜色
                FaceOnDrawTexturViewUtil.drawFaceColor(mUser, paint, paintBg, model);
                // 绘制人脸框
                FaceOnDrawTexturViewUtil.drawRect(canvas,
                        rectF, paint, 5f, 50f , 25f);
                // 清空canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TtsManager.getInstance(this).destory();
    }
}
