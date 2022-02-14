package com.baidu.idl.main.facesdk.identifylibrary.testimony;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
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
import android.view.ViewGroup;
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
import com.Tool.MyTTS;
import com.Tool.StringTool;
import com.Tool.TtsManager;
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
import com.baidu.idl.main.facesdk.identifylibrary.utils.JsonRootBean;
import com.baidu.idl.main.facesdk.identifylibrary.utils.JsonUtils;
import com.baidu.idl.main.facesdk.identifylibrary.utils.ToastUtils;
import com.baidu.idl.main.facesdk.identifylibrary.view.PreviewTexture;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.dk.uartnfc.SamVIdCard;
import com.dk.uartnfc.SerialManager;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
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

import static android.content.ContentValues.TAG;
import static com.DKCloudID.ClientDispatcher.SAM_V_FRAME_START_CODE;
import static com.DKCloudID.ClientDispatcher.SAM_V_INIT_COM;

public class FaceIRTestimonyActivity extends BaseActivity implements View.OnClickListener {
    private static final int PICK_PHOTO_FRIST = 100;
    private static final int PICK_VIDEO_FRIST = 101;

    private volatile boolean firstFeatureFinished = false;
    private volatile boolean secondFeatureFinished = false;

    private byte[] firstFeature = new byte[512];
    private byte[] secondFeature = new byte[512];

    private Context mContext;
    private RelativeLayout livenessRl;
    private RectF rectF;
    private Paint paint;
    private Paint paintBg;
    // 摄像头个数
    private int mCameraNum;
    // RGB+IR 控件
    private PreviewTexture[] mPreview;
    private Camera[] mCamera;
    private AutoTexturePreviewView mPreviewView;
    private ImageView testImageview;
    private TextureView mDrawDetectFaceView;
    private ImageView testimonyPreviewLineIv;
    private ImageView testimonyDevelopmentLineIv;
    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PERFER_HEIGH = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    // 判断摄像头数据源
    private int camemra1DataMean;
    private int camemra2DataMean;
    private volatile boolean camemra1IsRgb = false;
    // 摄像头采集数据
    private volatile byte[] rgbData;
    private volatile byte[] irData;
    private RelativeLayout livenessAgainRl;
    private ImageView livenessAddIv;
    private TextView livenessUpdateTv;
    private ImageView livenessShowIv;
    private ImageView hintShowIv;
    private TextView tv_nir_live_score;
    private RelativeLayout livenessTipsFailRl;
    private TextView livenessTipsFailTv;
    private TextView livenessTipsPleaseFailTv;
    private TextView tv_nir_live_time;
    private TextureView irTexture;
    private float score = 0;
    private TextView testimonyDevelopmentTv;
    private TextView testimonyPreviewTv;
    private TextView tv_body_temperature;

    // 定义一个变量判断是预览模式还是开发模式
    boolean isDevelopment = false;
    private ConstraintLayout livenessButtomLl;
    private RelativeLayout kaifaRelativeLayout;
    private RelativeLayout testNirRl;
    private TextView hintAdainTv;
    private TextView livenessBaiduTv;
    private View view;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private ImageView test_nir_iv;
    private ImageView test_rgb_iv;
    private TextView tv_feature_time;
    private TextView tv_feature_search_time;
    private TextView tv_all_time;
    private RelativeLayout hintShowRl;
    private RelativeLayout developmentAddRl;
    private float rgbLiveScore;
    private float nirLiveScore;
    // 判断是否有人脸
    private boolean isFace = false;
    private ImageView livenessTipsFailIv;
    private float nirLivenessScore = 0.0f;
    private float rgbLivenessScore = 0.0f;
    // 特征提取
    private long featureTime;
    // rgb
    private RelativeLayout testRelativeLayout;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;
    /**
     * 身份证模块
     */
    //身份证识别
    SerialManager serialManager;
    DKCloudID dkCloudID = null;
    byte[] initData;
    private MyTTS myTTS;

    /**
     * 测温
     * */
    private ITemperature temperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListener();
        setContentView(R.layout.activity_face_ir_identifylibrary);
        FaceSDKManager.getInstance().emptyFrame();
        mContext = this;
        initView();
        initCard();
        initTemperature();
        //在应用启动时初始化一次
        TtsManager.getInstance(this).init();

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
            livenessRl.setLayoutParams(params);
        }
    }

    /**
     * 身份证模块初始化
     */
    private void initCard() {
        //语音
        myTTS = new MyTTS(this);
        serialManager = new SerialManager();
        //设置串口数据接收监听
        serialManager.setOnReceiveDataListener(new SerialManager.onReceiveDataListener() {
            @Override
            public void OnReceiverData(String portNumberString, byte[] dataBytes) {
                final String portNumber = portNumberString;
                final byte[] data = dataBytes;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("[MainActivity]" + portNumber + "接收(" + data.length + ")：" + StringTool.byteHexToSting(data) + "\r\n");

                        if ((data.length >= 3) && (data[0] == (byte) 0xAA)) {
                            if (StringTool.byteHexToSting(data).equals("AA01EA")) {
//                                "卡片已拿开！
//                                hidDialog();
                                if (dkCloudID != null) {
                                    dkCloudID.Close();
                                }

                                /**卡片拿开退出输入框**/
                                //卡片拿开退出输入框
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                        alertDialog.hide();
                                    }
                                });
                            } else if ((data.length > 4)            //寻到cpu卡，则当作护照处理
                                    && (data[0] == (byte) 0xAA)
                                    && (data[2] == (byte) 0x01)
                                    && ((data[3] == (byte) 0x04) || (data[3] == (byte) 0x03))) {

                                //判断是不是护照
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            byte[] returnBytes = serialManager.sendWithReturn(StringTool.hexStringToBytes("aa0d1600a4040c07a0000002471001"), 500);
                                            if (StringTool.byteHexToSting(returnBytes).equals("AA03169000")) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
//                                                        alertDialog.show();
                                                    }
                                                });
                                            }
                                        } catch (DeviceNoResponseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            } else if ((data.length > 4)            //寻到M1卡
                                    && (data[0] == (byte) 0xAA)
                                    && (data[2] == (byte) 0x01)
                                    && ((data[3] == (byte) 0x01))) {

                                //读M1卡块1
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            byte[] returnBytes = serialManager.sendWithReturn(StringTool.hexStringToBytes("AA020401"), 500);
//                                            logViewln("返回:" + StringTool.byteHexToSting(returnBytes));
                                        } catch (DeviceNoResponseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                        msgTextView.setText("");
//                                        refreshLogView(portNumber + "接收(" + data.length + ")：" + StringTool.byteHexToSting(data) + "\r\n");
                                    }
                                });
                            }
                        } else if ((data.length >= 3) && (data[0] == SAM_V_FRAME_START_CODE) && (data[3] == SAM_V_INIT_COM)) {
                            //校验数据
                            try {
                                SamVIdCard.verify(data);
                            } catch (CardNoResponseException e) {
                                e.printStackTrace();

//                                logViewln( "正在重新解析.." );
                                serialManager.send(StringTool.hexStringToBytes("AA0118"));
                                return;
                            }

                            System.out.println("开始解析");
//                            logViewln(null);
//                            logViewln( "正在读卡，请勿移动身份证!" );
//                            myTTS.speak("正在读卡，请勿移动身份证");
                            //在需要使用的地方调用
                            TtsManager.getInstance(getActivity()).speakText("正在读卡，请勿移动身份证");
                            initData = Arrays.copyOfRange(data, 4, data.length - 1);
                            SamVIdCard samVIdCard = new SamVIdCard(serialManager, initData);
                            IDCard idCard = new IDCard(samVIdCard);

                            int cnt = 0;
                            do {
                                try {
                                    /**
                                     * 获取身份证数据，带进度回调，如果不需要进度回调可以去掉进度回调参数或者传入null
                                     * 注意：此方法为同步阻塞方式，需要一定时间才能返回身份证数据，期间身份证不能离开读卡器！
                                     */
                                    IDCardData idCardData = idCard.getIDCardData(new IDCard.onReceiveScheduleListener() {
                                        @Override
                                        public void onReceiveSchedule(int rate) {  //读取进度回调
//                                            showReadWriteDialog("正在读取身份证信息,请不要移动身份证", rate);
                                        }
                                    });

                                    /**
                                     * 读取成功，显示身份证数据，在此提示用户读取成功或者打开蜂鸣器提示
                                     */
                                    showIDMsg(idCardData);
                                    updateByCertificateNumber(idCardData.IDCardNo,idCardData);
                                    //返回读取成功
                                    return;
                                } catch (DKCloudIDException e) {   //服务器返回异常，重复5次解析
                                    e.printStackTrace();

                                    //显示错误信息
//                                    logViewln(e.getMessage());
                                } catch (CardNoResponseException e) {    //卡片读取异常，直接退出，需要重新读卡
                                    e.printStackTrace();

                                    //显示错误信息
//                                    logViewln(e.getMessage());

                                    //返回读取失败
//                                    myTTS.speak("请不要移动身份证");
                                    TtsManager.getInstance(getActivity()).speakText("请不要移动身份证");
//                                    logViewln( "正在重新解析.." );
                                    serialManager.send(StringTool.hexStringToBytes("AA0118"));
                                    return;
                                } finally {
                                    //读卡结束关闭进度条显示
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                            if (readWriteDialog.isShowing()) {
//                                                readWriteDialog.dismiss();
//                                            }
//                                            readWriteDialog.setProgress(0);
                                        }
                                    });
                                }
                            } while (cnt++ < 5);  //如果服务器返回异常则重复读5次直到成功

                        } else if (StringTool.byteHexToSting(data).equals("aa01ea")) {
                            if (dkCloudID != null) {
                                dkCloudID.Close();
                            }
                            System.out.println("卡片已经拿开");
                        }
                    }
                }).start();
            }
        });

        if (serialManager.isOpen()) {
        } else {
            Log.i("serialManager", "打开身份证识别");
            serialManager.open("/dev/ttyS1", "115200");
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
                                TtsManager.getInstance(getActivity()).speakText("体温异常");
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
    public void updateByCertificateNumber(String cardNumber, final IDCardData idCardData){
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
                    TtsManager.getInstance(getActivity()).speakText("请刷脸认证");
//                    myTTS.speak("请刷脸认证");
                    Log.i("onResponse","isSuccessful");
                    String result = response.body().string();
                    JsonRootBean newsBeanList = JsonUtils.deserialize(result, JsonRootBean.class);
                    //处理UI需要切换到UI线程处理
                    Log.i("onResponse",result);
                    //提取特征值核验人脸
                    personDetect(idCardData);
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
     * 提取特征值核验人脸
     * */
    private void personDetect(final IDCardData idCardData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (idCardData.PhotoBmp != null) {
                    // 提取特征值
                    float ret = FaceSDKManager.getInstance().personDetect(idCardData.PhotoBmp, secondFeature, getActivity());
                    if (ret != -1) {
                        isFace = false;
                        // 判断质量检测，针对模糊度、遮挡、角度
                        if (ret == 128) {
                            secondFeatureFinished = true;
                        }
                        if (ret == 128) {
//                                        ToastUtils.toast(mContext, "图片特征抽取成功");
                        } else {
//                                        ToastUtils.toast(mContext, "图片特征抽取失败");
                        }
                    } else {
                        isFace = true;
                    }
                }
            }
        });
    }


    private void showIDMsg(IDCardData msg) {
        final IDCardData idCardData = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                livenessUpdateTv.setText(idCardData.Name);
                livenessAddIv.setImageBitmap(idCardData.PhotoBmp);
                livenessShowIv.setImageBitmap(idCardData.PhotoBmp);
                hintShowIv.setImageBitmap(idCardData.PhotoBmp);
            }
        });
    }


    private Activity getActivity() {
        return this;
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
                    ToastUtils.toast(FaceIRTestimonyActivity.this, "模型加载成功，欢迎使用");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        ToastUtils.toast(FaceIRTestimonyActivity.this, "模型加载失败，请尝试重启应用");
                    }
                }
            });
        }
    }

    private void initView() {
        // 获取整个布局
        livenessRl = findViewById(R.id.liveness_Rl);
        // 画人脸框
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();
        // AutoTexturePreviewView
        mPreviewView = findViewById(R.id.detect_ir_image_view);
        // 双目摄像头IR 图像预览
        irTexture = findViewById(R.id.texture_preview_ir);
        if (SingleBaseConfig.getBaseConfig().getMirrorVideoNIR() == 1) {
            irTexture.setRotationY(180);
        }
        // 不需要屏幕自动变黑
        mDrawDetectFaceView = findViewById(R.id.texture_view_draw);
        mDrawDetectFaceView.setKeepScreenOn(true);
        mDrawDetectFaceView.setOpaque(false);
        if (SingleBaseConfig.getBaseConfig().getRgbRevert()) {
            mDrawDetectFaceView.setRotationY(180);
        }
        // 百度
        livenessBaiduTv = findViewById(R.id.liveness_baiduTv);
        // view
        view = findViewById(R.id.mongolia_view);
        // RGB 阈值
        rgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // Live 阈值
        nirLiveScore = SingleBaseConfig.getBaseConfig().getNirLiveScore();
        /* title */
        // 返回
        ImageView testimony_backIv = findViewById(R.id.btn_back);
        testimony_backIv.setOnClickListener(this);
        // 预览模式
        testimonyPreviewTv = findViewById(R.id.preview_text);
        testimonyPreviewTv.setOnClickListener(this);
        testimonyPreviewLineIv = findViewById(R.id.preview_view);
        // 开发模式
        testimonyDevelopmentTv = findViewById(R.id.develop_text);
        testimonyDevelopmentTv.setOnClickListener(this);
        testimonyDevelopmentLineIv = findViewById(R.id.develop_view);
        // 设置
        ImageView testimonySettingIv = findViewById(R.id.btn_setting);
        testimonySettingIv.setOnClickListener(this);
        //当前体温
        tv_body_temperature = findViewById(R.id.tv_body_temperature);
        // ****************开发模式****************
        // RGB
        testImageview = findViewById(R.id.test_rgb_ir_view);
        test_rgb_iv = findViewById(R.id.test_rgb_iv);
        testRelativeLayout = findViewById(R.id.test_rgb_rl);
        testRelativeLayout.setVisibility(View.GONE);
        // 图片显示
        hintShowIv = findViewById(R.id.hint_showIv);
        // 重新上传
        hintAdainTv = findViewById(R.id.hint_adainTv);
        hintAdainTv.setOnClickListener(this);
        hintShowRl = findViewById(R.id.hint_showRl);
        // 上传图片
        ImageView DevelopmentAddIv = findViewById(R.id.Development_addIv);
        DevelopmentAddIv.setOnClickListener(this);
        developmentAddRl = findViewById(R.id.Development_addRl);
        // nir
        testNirRl = findViewById(R.id.test_nir_Rl);
        testNirRl.setVisibility(View.GONE);
        test_nir_iv = findViewById(R.id.test_nir_iv);
        // 提示
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        textCompareStatus = findViewById(R.id.text_compare_status);
        // 相似度分数
        tv_nir_live_time = findViewById(R.id.tv_rgb_live_time);
        // 活体检测耗时
        tv_nir_live_score = findViewById(R.id.tv_rgb_live_score);
        // 特征抽取耗时
        tv_feature_time = findViewById(R.id.tv_feature_time);
        // 特征比对耗时
        tv_feature_search_time = findViewById(R.id.tv_feature_search_time);
        // 总耗时
        tv_all_time = findViewById(R.id.tv_all_time);
        // 存图按钮
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(this);
        saveCamera.setVisibility(View.GONE);
        spot = findViewById(R.id.spot);

        // ****************预览模式****************
        // 未通过提示
        livenessTipsFailRl = findViewById(R.id.testimony_tips_failRl);
        livenessTipsFailTv = findViewById(R.id.testimony_tips_failTv);
        livenessTipsPleaseFailTv = findViewById(R.id.testimony_tips_please_failTv);
        livenessTipsFailIv = findViewById(R.id.testimony_tips_failIv);
        // 预览模式buttom
        livenessButtomLl = findViewById(R.id.person_buttomLl);
        kaifaRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        livenessAddIv = findViewById(R.id.testimony_addIv);
        livenessAddIv.setOnClickListener(this);
        livenessUpdateTv = findViewById(R.id.testimony_upload_filesTv);
        livenessAgainRl = findViewById(R.id.testimony_showRl);
        livenessShowIv = findViewById(R.id.testimony_showImg);
        TextView livenessAgainTv = findViewById(R.id.testimony_showAgainTv);
        livenessAgainTv.setOnClickListener(this);

        // 双摄像头
        mCameraNum = Camera.getNumberOfCameras();
        if (mCameraNum < 2) {
            Toast.makeText(this, "未检测到2个摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {
            mPreview = new PreviewTexture[mCameraNum];
            mCamera = new Camera[mCameraNum];
            mPreview[1] = new PreviewTexture(this, irTexture);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraNum < 2) {
            Toast.makeText(this, "未检测到2个摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {
            try {
                startTestCloseDebugRegisterFunction();
                if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
                    mCamera[1] = Camera.open(Math.abs(SingleBaseConfig.getBaseConfig().getRBGCameraId() - 1));
                } else {
                    mCamera[1] = Camera.open(1);
                }
                ViewGroup.LayoutParams layoutParamsNirRl = testNirRl.getLayoutParams();
                ViewGroup.LayoutParams layoutParams = irTexture.getLayoutParams();
                int w = layoutParams.width;
                int h = layoutParams.height;
                int cameraRotation = SingleBaseConfig.getBaseConfig().getNirVideoDirection();
                mCamera[1].setDisplayOrientation(cameraRotation);
                if (cameraRotation == 90 || cameraRotation == 270) {
                    layoutParams.height = h > w ? h : w;
                    layoutParams.width = h > w ? w : h;
                    layoutParamsNirRl.width = h > w ? w : h;
                    // 旋转90度或者270，需要调整宽高
                } else {
                    layoutParams.height = h > w ? w : h;
                    layoutParams.width = h > w ? h : w;
                    layoutParamsNirRl.width = h > w ? h : w;
                }
                irTexture.setLayoutParams(layoutParams);
                testNirRl.setLayoutParams(layoutParamsNirRl);
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
        // 设置前置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        //  CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        } else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        }

        CameraPreviewManager.getInstance().startPreview(this, mPreviewView,
                PREFER_WIDTH, PERFER_HEIGH, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        // 摄像头预览数据进行人脸检测
                        dealRgb(data);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serialManager != null) {
            serialManager.close();
        }
    }

    @Override
    protected void onPause() {
//      CameraPreviewManager.getInstance().stopPreview();
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
            if (livenessShowIv.getDrawable() != null || hintShowIv.getDrawable() != null) {
                firstFeatureFinished = false;
                FaceSDKManager.getInstance().onDetectCheck(rgbData, irData,
                        null, secondFeature, PERFER_HEIGH, PREFER_WIDTH, 2, new FaceDetectCallBack() {
                            @Override
                            public void onFaceDetectCallback(final LivenessModel livenessModel) {
                                // 预览模式
                                checkCloseDebugResult(livenessModel);
                                // 开发模式
                                checkOpenDebugResult(livenessModel);
                                if (isSaveImage) {
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

                rgbData = null;
                irData = null;
            } else {
                testImageview.setImageResource(R.mipmap.ic_image_video);
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.85f, 0.0f);
                animator.setDuration(3000);
                view.setBackgroundColor(Color.parseColor("#ffffff"));
                animator.start();
            }
        }
    }

    // 预览模式
    private void checkCloseDebugResult(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    livenessTipsFailRl.setVisibility(View.GONE);

                    if (testimonyPreviewLineIv.getVisibility() == View.VISIBLE) {
                        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.25f, 0.0f);
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
                    return;
                }
                score = livenessModel.getScore();
                if (isDevelopment == false) {
                    layoutCompareStatus.setVisibility(View.GONE);
                    livenessTipsFailRl.setVisibility(View.VISIBLE);
                    if (isFace == true) {
                        livenessTipsFailTv.setText("上传图片不包含人脸");
                        livenessTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                        livenessTipsPleaseFailTv.setText("无法进行人证比对");
                        livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                        return;
                    }
                    rgbLivenessScore = livenessModel.getRgbLivenessScore();
                    nirLivenessScore = livenessModel.getIrLivenessScore();
                    if (rgbLivenessScore < rgbLiveScore || nirLivenessScore <
                            nirLiveScore) {
                        livenessTipsFailTv.setText("人证核验未通过");
                        livenessTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                        livenessTipsPleaseFailTv.setText("请上传正面人脸照片");
                        livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                        return;
                    }
                    SingleBaseConfig.getBaseConfig().setIdThreshold(0.7f);
                    if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                        livenessTipsFailTv.setText("人证核验通过");
                        livenessTipsFailTv.setTextColor(
                                Color.parseColor("#FF00BAF2"));
                        livenessTipsPleaseFailTv.setText("识别成功");
                        livenessTipsFailIv.setImageResource(R.mipmap.tips_success);

//                        myTTS.speak("核验通过");
                        //在需要使用的地方调用
                        TtsManager.getInstance(getActivity()).speakText("核验通过");
                    } else {
                        livenessTipsFailTv.setText("人证核验未通过");
                        livenessTipsFailTv.setTextColor(
                                Color.parseColor("#FFFEC133"));
                        livenessTipsPleaseFailTv.setText("请上传正面人脸照片");
                        livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                    }
                }
            }
        });
    }

    // 开发模式
    private void checkOpenDebugResult(final LivenessModel model) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (model != null) {
                    BDFaceImageInstance image = model.getBdFaceImageInstance();
                    if (image != null) {
                        testImageview.setImageBitmap(BitmapUtils.getInstaceBmp(image));
                    }

                    tv_nir_live_time.setText(String.format("相似度分数：%s", score));
                    tv_nir_live_score.setText(String.format("活体检测耗时：%s ms", model.getIrLivenessDuration()));

                    //  比较两个人脸
                    if (firstFeature == null || secondFeature == null) {
                        return;
                    }

//                    if (rgbLivenessScore < rgbLiveScore || nirLivenessScore < nirLiveScore) {
//                        tv_feature_time.setText(String.format("特征抽取耗时：%s ms", 0));
//                        tv_feature_search_time.setText(String.format("特征比对耗时：%s ms", 0));
//                        tv_all_time.setText(String.format("总耗时：%s ms", model.getAllDetectDuration()));
//                    } else {
//                    }
                    tv_feature_time.setText(String.format("特征抽取耗时：%s ms", model.getFeatureDuration()));
                    tv_feature_search_time.setText(String.format("特征比对耗时：%s ms",
                            model.getStartCompareTime()));
                    tv_all_time.setText(String.format("总耗时：%s ms", model.getAllDetectDuration()));

                    if (isDevelopment) {
                        livenessTipsFailRl.setVisibility(View.GONE);
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        rgbLivenessScore = model.getRgbLivenessScore();
                        nirLivenessScore = model.getIrLivenessScore();
                        if (nirLivenessScore < nirLiveScore) {
                            test_nir_iv.setVisibility(View.VISIBLE);
                            test_nir_iv.setImageResource(R.mipmap.ic_icon_develop_fail);
                        } else {
                            test_nir_iv.setVisibility(View.VISIBLE);
                            test_nir_iv.setImageResource(R.mipmap.ic_icon_develop_success);
                        }
                        if (rgbLivenessScore < rgbLiveScore) {
                            test_rgb_iv.setVisibility(View.VISIBLE);
                            test_rgb_iv.setImageResource(R.mipmap.ic_icon_develop_fail);
                        } else {
                            test_rgb_iv.setVisibility(View.VISIBLE);
                            test_rgb_iv.setImageResource(R.mipmap.ic_icon_develop_success);
                        }
                    } else {
                        test_rgb_iv.setVisibility(View.VISIBLE);
                        test_rgb_iv.setImageResource(R.mipmap.ic_icon_develop_success);
                    }
                    if (!model.isQualityCheck()) {
                        textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
//                        textCompareStatus.setMaxEms(6);
                        textCompareStatus.setText("请正视摄像头");
                    } else if (rgbLivenessScore < rgbLiveScore || nirLivenessScore < nirLiveScore) {
                        textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));

//                            textCompareStatus.setMaxEms(7);
                        textCompareStatus.setText("活体检测未通过");
                    } else {
                        if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                            textCompareStatus.setTextColor(Color.parseColor("#00BAF2"));
                            textCompareStatus.setText("比对成功");
                        } else {
                            textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                            textCompareStatus.setText("比对失败");
                        }
                    }
                } else {
                    layoutCompareStatus.setVisibility(View.GONE);
                    test_nir_iv.setVisibility(View.GONE);
                    test_rgb_iv.setVisibility(View.GONE);
                    // 开发模式
                    testImageview.setImageResource(R.mipmap.ic_image_video);
                    tv_nir_live_time.setText(String.format("相似度分数：%s", 0));
                    tv_nir_live_score.setText(String.format("活体检测耗时：%s ms", 0));
                    tv_feature_time.setText(String.format("特征抽取耗时：%s ms", 0));
                    tv_feature_search_time.setText(String.format("特征比对耗时：%s ms", 0));
                    tv_all_time.setText(String.format("总耗时：%s ms", 0));
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_back) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                        Toast.LENGTH_LONG).show();
                return;
            }
            finish();
            // 预览模式
        } else if (id == R.id.preview_text) {
            isDevelopment = false;
            if (livenessShowIv.getDrawable() != null || hintShowIv.getDrawable() != null) {
                livenessTipsFailRl.setVisibility(View.VISIBLE);
                layoutCompareStatus.setVisibility(View.GONE);
            } else {
                livenessTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.GONE);
            }
            testimonyPreviewLineIv.setVisibility(View.VISIBLE);
            testimonyDevelopmentLineIv.setVisibility(View.GONE);
            testimonyDevelopmentTv.setTextColor(Color.parseColor("#FF999999"));
            testimonyPreviewTv.setTextColor(getResources().getColor(R.color.white));
            testNirRl.setVisibility(View.GONE);
            livenessButtomLl.setVisibility(View.VISIBLE);
            kaifaRelativeLayout.setVisibility(View.GONE);
            livenessBaiduTv.setVisibility(View.VISIBLE);
//                test_nir_view.setVisibility(View.GONE);
            testRelativeLayout.setVisibility(View.GONE);
            irTexture.setAlpha(0);
            testImageview.setVisibility(View.GONE);
            saveCamera.setVisibility(View.GONE);
            isSaveImage = false;
            spot.setVisibility(View.GONE);
            // 开发模式
        } else if (id == R.id.develop_text) {
            if (livenessShowIv.getDrawable() != null || hintShowIv.getDrawable() != null) {
                livenessTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.VISIBLE);
            } else {
                livenessTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.GONE);
            }
            isDevelopment = true;
            testimonyPreviewLineIv.setVisibility(View.GONE);
            testimonyDevelopmentLineIv.setVisibility(View.VISIBLE);
            testimonyDevelopmentTv.setTextColor(getResources().getColor(R.color.white));
            testimonyPreviewTv.setTextColor(Color.parseColor("#FF999999"));
            testNirRl.setVisibility(View.VISIBLE);
            livenessButtomLl.setVisibility(View.GONE);
            kaifaRelativeLayout.setVisibility(View.VISIBLE);
            livenessBaiduTv.setVisibility(View.GONE);
            irTexture.setAlpha(1);
            testImageview.setVisibility(View.VISIBLE);
            testRelativeLayout.setVisibility(View.VISIBLE);
            saveCamera.setVisibility(View.VISIBLE);
            judgeFirst();
        } else if (id == R.id.btn_setting) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                        Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(mContext, IdentifySettingActivity.class));
            finish();
        } else if (id == R.id.testimony_addIv) {
            secondFeatureFinished = false;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_PHOTO_FRIST);
        } else if (id == R.id.testimony_showAgainTv) {
            secondFeatureFinished = false;
            Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent2, PICK_PHOTO_FRIST);
        } else if (id == R.id.hint_adainTv) {
            secondFeatureFinished = false;
            Intent intent3 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent3, PICK_PHOTO_FRIST);
        } else if (id == R.id.Development_addIv) {
            secondFeatureFinished = false;
            Intent intent4 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent4, PICK_PHOTO_FRIST);
        } else if (id == R.id.save_camera) {
            isSaveImage = !isSaveImage;
            if (isSaveImage) {
                spot.setVisibility(View.VISIBLE);
                ToastUtils.toast(FaceIRTestimonyActivity.this, "存图功能已开启再次点击可关闭");
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


    private synchronized void rgbOrIr(int index, byte[] data) {
        byte[] tmp = new byte[PREFER_WIDTH * PERFER_HEIGH];
        try {
            System.arraycopy(data, 0, tmp, 0, PREFER_WIDTH * PERFER_HEIGH);
        } catch (NullPointerException e) {
            Log.e("qing", String.valueOf(e.getStackTrace()));
        }
        int count = 0;
        int total = 0;
        for (int i = 0; i < PREFER_WIDTH * PERFER_HEIGH; i = i + 10) {
            total += byteToInt(tmp[i]);
            count++;
        }

        if (count == 0) {
            return;
        }

        if (index == 0) {
            camemra1DataMean = total / count;
        } else {
            camemra2DataMean = total / count;
        }
        if (camemra1DataMean != 0 && camemra2DataMean != 0) {
            if (camemra1DataMean > camemra2DataMean) {
                camemra1IsRgb = true;
            } else {
                camemra1IsRgb = false;
            }
        }
    }

    public int byteToInt(byte b) {
        // Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    private void choiceRgbOrIrType(int index, byte[] data) {
        // camera1如果为rgb数据，调用dealRgb，否则为Ir数据，调用Ir
        if (index == 0) {
            if (camemra1IsRgb) {
                dealRgb(data);
            } else {
                dealIr(data);
            }
        } else {
            if (camemra1IsRgb) {
                dealIr(data);
            } else {
                dealRgb(data);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PHOTO_FRIST && (data != null && data.getData() != null)) {
            Uri uri1 = ImageUtils.geturi(data, this);
            try {
                final Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri1));
                if (bitmap != null) {
                    // 提取特征值
//                    syncFeature(bitmap, secondFeature, 2, true);
                    float ret = FaceSDKManager.getInstance().personDetect(bitmap, secondFeature, this);
                    livenessShowIv.setVisibility(View.VISIBLE);
                    hintShowIv.setVisibility(View.VISIBLE);
                    livenessShowIv.setImageBitmap(bitmap);
                    hintShowIv.setImageBitmap(bitmap);
                    if (ret != -1) {
                        isFace = false;
                        // 判断质量检测，针对模糊度、遮挡、角度
                        if (ret == 128) {
                            secondFeatureFinished = true;
                        }
                        if (ret == 128) {
                            toast("图片特征抽取成功");
                            hintShowIv.setVisibility(View.VISIBLE);
                            livenessShowIv.setVisibility(View.VISIBLE);
                            hintShowRl.setVisibility(View.VISIBLE);
                            livenessAgainRl.setVisibility(View.VISIBLE);
                            livenessAddIv.setVisibility(View.GONE);
                            livenessUpdateTv.setVisibility(View.GONE);
                            developmentAddRl.setVisibility(View.GONE);
                        } else {
                            ToastUtils.toast(mContext, "图片特征抽取失败");
                        }
                    } else {
                        isFace = true;
                        isFace = true;
                        // 上传图片无人脸隐藏
                        livenessShowIv.setVisibility(View.GONE);
                        hintShowIv.setVisibility(View.GONE);
                        livenessAddIv.setVisibility(View.GONE);
                        livenessUpdateTv.setVisibility(View.GONE);
                        livenessAgainRl.setVisibility(View.VISIBLE);
                        hintShowIv.setVisibility(View.GONE);
                        livenessShowIv.setVisibility(View.GONE);
                        hintShowRl.setVisibility(View.VISIBLE);
                        developmentAddRl.setVisibility(View.GONE);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * bitmap -提取特征值
     *
     * @param bitmap
     * @param feature
     * @param index
     */
    private void syncFeature(final Bitmap bitmap, final byte[] feature, final int index, boolean isFromPhotoLibrary) {
        float ret = -1;
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(bitmap);

        FaceInfo[] faceInfos = null;
        int count = 10;
        // 现在人脸检测加入了防止多线程重入判定，假如之前线程人脸检测未完成，本次人脸检测有可能失败，需要多试几次
        while (count != 0) {
            faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                    .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, rgbInstance);
            count--;
            if (faceInfos != null) {
                break;
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // 检测结果判断
        if (faceInfos != null && faceInfos.length > 0) {
            isFace = false;
            // 上传图片有人脸显示
            livenessShowIv.setVisibility(View.VISIBLE);
            hintShowIv.setVisibility(View.VISIBLE);
            // 判断质量检测，针对模糊度、遮挡、角度
            ret = FaceSDKManager.getInstance().getFaceFeature().feature(BDFaceSDKCommon.FeatureType.
                    BDFACE_FEATURE_TYPE_ID_PHOTO, rgbInstance, faceInfos[0].landmarks, feature);
            if (ret == 128 && index == 2) {
                secondFeatureFinished = true;
            }
            if (ret == 128) {
                toast("图片" + index + "特征抽取成功");
                hintShowIv.setVisibility(View.VISIBLE);
                livenessShowIv.setVisibility(View.VISIBLE);
                hintShowRl.setVisibility(View.VISIBLE);
                livenessAgainRl.setVisibility(View.VISIBLE);
                livenessAddIv.setVisibility(View.GONE);
                livenessUpdateTv.setVisibility(View.GONE);
                developmentAddRl.setVisibility(View.GONE);
            } else {
                toast("图片二特征抽取失败");
            }
        } else {
            isFace = true;
            // 上传图片无人脸隐藏
            livenessShowIv.setVisibility(View.GONE);
            hintShowIv.setVisibility(View.GONE);
            livenessAddIv.setVisibility(View.GONE);
            livenessUpdateTv.setVisibility(View.GONE);
            livenessAgainRl.setVisibility(View.VISIBLE);
            hintShowIv.setVisibility(View.GONE);
            livenessShowIv.setVisibility(View.GONE);
            hintShowRl.setVisibility(View.VISIBLE);
            developmentAddRl.setVisibility(View.GONE);
        }
    }

    private void toast(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
            }
        });
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
                        mPreviewView, model.getBdFaceImageInstance());
                if (score < SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                    paint.setColor(Color.parseColor("#FEC133"));
                    paintBg.setColor(Color.parseColor("#FEC133"));
                } else {
                    paint.setColor(Color.parseColor("#00baf2"));
                    paintBg.setColor(Color.parseColor("#00baf2"));
                }
                paint.setStyle(Paint.Style.FILL);
                paintBg.setStyle(Paint.Style.FILL);
                // 画笔粗细
                paint.setStrokeWidth(8);
                // 设置线条等图形的抗锯齿
                paint.setAntiAlias(true);
                paintBg.setStrokeWidth(13);
                paintBg.setAlpha(90);
                // 设置线条等图形的抗锯齿
                paintBg.setAntiAlias(true);
                FaceOnDrawTexturViewUtil.drawRect(canvas,
                        rectF, paint, 5f, 50f, 25f);
                // 清空canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }
}
