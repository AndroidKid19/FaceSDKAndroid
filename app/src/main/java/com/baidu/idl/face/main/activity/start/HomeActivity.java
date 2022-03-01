package com.baidu.idl.face.main.activity.start;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import com.Tool.URLUtils;
import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.attribute.activity.attribute.FaceAttributeRgbActivity;
import com.baidu.idl.face.main.attribute.utils.AttributeConfigUtils;
import com.baidu.idl.face.main.drivermonitor.activity.drivermonitor.DriverMonitorActivityDrivermonitor;
import com.baidu.idl.face.main.drivermonitor.utils.DriverMonitorConfigUtils;
import com.baidu.idl.face.main.finance.activity.finance.FaceDepthFinanceActivity;
import com.baidu.idl.face.main.finance.activity.finance.FaceNIRFinanceActivity;
import com.baidu.idl.face.main.finance.activity.finance.FaceRGBFinanceActivity;
import com.baidu.idl.face.main.finance.activity.finance.FaceRgbNirDepthFinanceActivity;
import com.baidu.idl.face.main.finance.utils.FinanceConfigUtils;
import com.baidu.idl.face.main.finance.utils.KeyboardsUtils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.activity.gate.FaceDepthGateActivity;
import com.baidu.idl.main.facesdk.activity.gate.FaceNIRGateActivriy;
import com.baidu.idl.main.facesdk.activity.gate.FaceRGBGateActivity;
import com.baidu.idl.main.facesdk.activity.gate.FaceRgbNirDepthGataActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.attendance.FaceDepthAttendanceActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.attendance.FaceNIRAttendanceActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.attendance.FaceRGBAttendanceActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.attendance.FaceRGBNirDepthAttendanceActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.AttendanceConfigUtils;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.JsonRootBean;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.JsonUtils;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.RegisterConfigUtils;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.VisitRegisterRecordBean;
import com.baidu.idl.main.facesdk.gazelibrary.gaze.FaceGazeActivity;
import com.baidu.idl.main.facesdk.gazelibrary.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.gazelibrary.utils.GazeConfigUtils;
import com.baidu.idl.main.facesdk.identifylibrary.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.identifylibrary.testimony.FaceDepthTestimonyActivity;
import com.baidu.idl.main.facesdk.identifylibrary.testimony.FaceIRTestimonyActivity;
import com.baidu.idl.main.facesdk.identifylibrary.testimony.FaceRGBIRDepthTestimonyActivity;
import com.baidu.idl.main.facesdk.identifylibrary.testimony.FaceRGBPersonActivity;
import com.baidu.idl.main.facesdk.identifylibrary.utils.IdentifyConfigUtils;
import com.baidu.idl.main.facesdk.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.paymentlibrary.activity.payment.FaceDepthPaymentActivity;
import com.baidu.idl.main.facesdk.paymentlibrary.activity.payment.FaceNIRPaymentActivity;
import com.baidu.idl.main.facesdk.paymentlibrary.activity.payment.FaceRGBPaymentActivity;
import com.baidu.idl.main.facesdk.paymentlibrary.activity.payment.FaceRgbNirDepthPaymentActivity;
import com.baidu.idl.main.facesdk.paymentlibrary.utils.PaymentConfigUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.activity.UserManagerActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.manager.VisitorFaceSDKManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewDepthActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewNIRActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewRgbNirDepthActivity;
import com.baidu.idl.main.facesdk.utils.DensityUtils;
import com.baidu.idl.main.facesdk.utils.GateConfigUtils;
import com.baidu.idl.main.facesdk.utils.StreamUtil;
import com.baidu.idl.main.facesdk.utils.ToastUtils;
import com.baidu.idl.main.facesdk.view.PreviewTexture;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.StringUtils;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.listener.DBLoadListener;
import com.example.datalibrary.model.User;
import com.google.gson.Gson;
import com.jwsd.libzxing.OnQRCodeListener;
import com.jwsd.libzxing.QRCodeManager;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

//您好！您购买的人脸识别一体机，百度受权码为：QUXD-XXEX-HMDB-ESXY,  受权码对应设备受权，请注意保管。
public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private Handler mHandler = new Handler();
    private PopupWindow popupWindow;
    private View view1;
    private RelativeLayout layout_home;
    private RelativeLayout home_personRl;
    private int mLiveType;
    private PopupWindow mPopupMenu;
    private PopupWindow mPopupMenuFirst;
    private ImageView home_menuImg;
    private boolean isCheck = false;
    private TextView home_dataTv;

    private static final int PREFER_WIDTH = 640;
    private static final int PREFER_HEIGHT = 480;
    private PreviewTexture[] previewTextures;
    private Camera[] mCamera;
    private TextureView checkRBGTexture;
    private TextureView checkNIRTexture;
    private ProgressBar progressBar;
    private TextView progressText;
    private View progressGroup;
    private boolean isDBLoad;
    private Future future;
    private AppCompatEditText et_qrcode;
    private TextView tv_body_temperature;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 测温
     */
    private ITemperature temperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mContext = this;
        initView();

        initRGBCheck();
        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            mHandler.postDelayed(mRunnable, 500);
            editor.putBoolean("isFirstRun", false);
            editor.commit();
        }
        initUserManagePopupWindow();
        initDBApi();
//        initListener();
        initTemperature();
    }

    /**
     * 测温头初始化
     */
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
                                tv_body_temperature.setText(temperature + "℃");
                                tv_body_temperature.setTextColor(Color.GREEN);
                            } else if (temp > 37.3) {
//                                myTTS.speak("体温异常");
                                com.Tool.TtsManager.getInstance(HomeActivity.this).speakText("体温异常");
                                tv_body_temperature.setText(temperature + "℃");
                                tv_body_temperature.setTextColor(Color.RED);
                            } else {
                                tv_body_temperature.setText(temperature + "℃");
                                tv_body_temperature.setTextColor(Color.GREEN);
                            }
                        } else {
                            tv_body_temperature.setText(temperature + "℃");
                            tv_body_temperature.setTextColor(Color.GREEN);
                        }
                    }
                });
            }
        });
        temperature.open("/dev/ttyS4");
    }


    private void initDBApi() {
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        isDBLoad = false;
        future = Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                FaceApi.getInstance().init(new DBLoadListener() {
                    @Override
                    public void onStart(int successCount) {
                        if (successCount < 5000 && successCount != 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadProgress(10);
                                }
                            });
                        }
                    }

                    @Override
                    public void onLoad(final int finishCount, final int successCount, final float progress) {
                        if (successCount > 5000 || successCount == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress((int) (progress * 100));
                                    progressText.setText(((int) (progress * 100)) + "%");
                                }
                            });
                        }
                    }

                    @Override
                    public void onComplete(final List<User> users, final int successCount) {
//                        FileUtils.saveDBList(HomeActivity.this, users);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FaceApi.getInstance().setUsers(users);
                                if (successCount > 5000 || successCount == 0) {
                                    progressGroup.setVisibility(View.GONE);
                                    isDBLoad = true;
                                }
                            }
                        });
                    }

                    @Override
                    public void onFail(final int finishCount, final int successCount, final List<User> users) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FaceApi.getInstance().setUsers(users);
                                progressGroup.setVisibility(View.GONE);
                                ToastUtils.toast(HomeActivity.this,
                                        "人脸库加载失败,共" + successCount + "条数据, 已加载" + finishCount + "条数据");
                                isDBLoad = true;
                            }
                        });
                    }
                }, mContext);
            }
        });
    }

    private void loadProgress(float i) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress((int) ((i / 5000f) * 100));
                progressText.setText(((int) ((i / 5000f) * 100)) + "%");
                if (i < 5000) {
                    loadProgress(i + 100);
                } else {
                    progressGroup.setVisibility(View.GONE);
                    isDBLoad = true;
                }
            }
        }, 10);
    }

    private void initFirstPopupWindowTip() {
        home_menuImg.setImageResource(R.mipmap.icon_titlebar_menu_first);
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.popup_menu_home_first, null);
        mPopupMenuFirst = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupMenuFirst.setFocusable(true);
        mPopupMenuFirst.setOutsideTouchable(true);
        mPopupMenuFirst.setBackgroundDrawable(getResources().getDrawable(R.drawable.menu_round_frist));

        mPopupMenuFirst.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                isCheck = false;
                home_menuImg.setImageResource(R.mipmap.icon_titlebar_menu);
            }
        });
        mPopupMenuFirst.setContentView(contentView);

        if (mPopupMenuFirst != null && home_menuImg != null) {
            int marginRight = DensityUtils.dip2px(mContext, 20);
            int marginTop = DensityUtils.dip2px(mContext, 56);
            mPopupMenuFirst.showAtLocation(home_menuImg, Gravity.END | Gravity.TOP,
                    marginRight, marginTop);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release(0);
        release(1);

        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        FaceApi.getInstance().cleanRecords();
    }

    private void initRGBCheck() {
        if (isSetCameraId()) {
            return;
        }
        int mCameraNum = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
        }
        if (mCameraNum > 1) {
            try {
                mCamera = new Camera[mCameraNum];
                previewTextures = new PreviewTexture[mCameraNum];
                mCamera[0] = Camera.open(0);
                previewTextures[0] = new PreviewTexture(this, checkRBGTexture);
                previewTextures[0].setCamera(mCamera[0], PREFER_WIDTH, PREFER_HEIGHT);
                mCamera[0].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        int check = StreamUtil.checkNirRgb(data, PREFER_WIDTH, PREFER_HEIGHT);
                        if (check == 1) {
                            setRgbCameraId(0);
                        }
                        release(0);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera[1] = Camera.open(1);
                previewTextures[1] = new PreviewTexture(this, checkNIRTexture);
                previewTextures[1].setCamera(mCamera[1], PREFER_WIDTH, PREFER_HEIGHT);
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        int check = StreamUtil.checkNirRgb(data, PREFER_WIDTH, PREFER_HEIGHT);
                        if (check == 1) {
                            setRgbCameraId(1);
                        }
                        release(1);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setRgbCameraId(0);
        }
    }

    private void setRgbCameraId(int index) {
        SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.main.facesdk.attendancelibrary.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.face.main.finance.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.face.main.attribute.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.face.main.drivermonitor.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.main.facesdk.gazelibrary.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.main.facesdk.paymentlibrary.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.main.facesdk.identifylibrary.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);

        AttendanceConfigUtils.modityJson();
        AttributeConfigUtils.modityJson();
        DriverMonitorConfigUtils.modityJson();
        FinanceConfigUtils.modityJson();
        GateConfigUtils.modityJson();
        GazeConfigUtils.modityJson();
        IdentifyConfigUtils.modityJson();
        PaymentConfigUtils.modityJson();
        RegisterConfigUtils.modityJson();

    }

    private boolean isSetCameraId() {
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.main.facesdk.attendancelibrary.
                        model.SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.face.main.finance.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.face.main.attribute.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.face.main.drivermonitor.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.main.facesdk.gazelibrary.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.main.facesdk.paymentlibrary.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.main.facesdk.identifylibrary.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.main.facesdk.registerlibrary.user.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1) {
            return false;
        } else {
            return true;
        }
    }

    private void release(int id) {
        if (mCamera != null && mCamera[id] != null) {
            if (mCamera[id] != null) {
                mCamera[id].setPreviewCallback(null);
                mCamera[id].stopPreview();
                previewTextures[id].release();
                mCamera[id].release();
                mCamera[id] = null;
            }
        }
    }

    private Runnable mRunnable = new Runnable() {
        public void run() {
            // 弹出PopupWindow的具体代码
            initPopupWindow();
            initFirstPopupWindowTip();
        }
    };

    private void initPopupWindow() {

        view1 = View.inflate(mContext, R.layout.layout_popup, null);
        popupWindow = new PopupWindow(view1, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 点击框外可以使得popupwindow消失
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(layout_home, Gravity.CENTER, 0, 0);
        initHandler();
    }

    /**
     * 初始化用户管理PopupWindow
     */
    private void initUserManagePopupWindow() {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.popup_menu_home, null);
        mPopupMenu = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupMenu.setFocusable(true);
        mPopupMenu.setOutsideTouchable(true);
        mPopupMenu.setBackgroundDrawable(getResources().getDrawable(R.drawable.menu_round));

        RelativeLayout relativeRegister = contentView.findViewById(R.id.relative_register);
        RelativeLayout mPopRelativeManager = contentView.findViewById(R.id.relative_manager);
        relativeRegister.setOnClickListener(this);
        mPopRelativeManager.setOnClickListener(this);
        mPopupMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                isCheck = false;
                home_menuImg.setImageResource(R.mipmap.icon_titlebar_menu);
            }
        });
        mPopupMenu.setContentView(contentView);
    }

    private void showPopupWindow(ImageView imageView) {
        if (mPopupMenu != null && imageView != null) {
            int marginRight = DensityUtils.dip2px(mContext, 20);
            int marginTop = DensityUtils.dip2px(mContext, 56);
            mPopupMenu.showAtLocation(imageView, Gravity.END | Gravity.TOP,
                    marginRight, marginTop);
        }
    }

    private void dismissPopupWindow() {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
    }

    private void initHandler() {
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                // 实现页面跳转
                popupWindow.dismiss();
                return false;
            }
        }).sendEmptyMessageDelayed(0, 3000);
    }

    private void initView() {
        et_qrcode = findViewById(R.id.et_qrcode);
        tv_body_temperature = findViewById(R.id.tv_body_temperature);
        layout_home = findViewById(R.id.layout_home);
        ImageView home_settingImg = findViewById(R.id.home_settingImg);
        home_settingImg.setOnClickListener(this);
        home_menuImg = findViewById(R.id.home_menuImg);
        home_menuImg.setOnClickListener(this);
        RelativeLayout home_gateRl = findViewById(R.id.home_gateRl);
        home_gateRl.setOnClickListener(this);
        RelativeLayout home_checkRl = findViewById(R.id.home_checkRl);
        home_checkRl.setOnClickListener(this);
        RelativeLayout home_payRl = findViewById(R.id.home_payRl);
        home_payRl.setOnClickListener(this);
        RelativeLayout home_livenessRl = findViewById(R.id.home_livenessRl);
        home_livenessRl.setOnClickListener(this);
        RelativeLayout home_attributeRl = findViewById(R.id.home_attributeRl);
        home_attributeRl.setOnClickListener(this);
        home_personRl = findViewById(R.id.home_personRl);
        home_personRl.setOnClickListener(this);
        RelativeLayout home_driveRl = findViewById(R.id.home_driveRl);
        home_driveRl.setOnClickListener(this);
        RelativeLayout home_attentionRl = findViewById(R.id.home_attentionRl);
        home_attentionRl.setOnClickListener(this);
        ImageView home_faceTv = findViewById(R.id.home_faceTv);
        home_faceTv.setOnClickListener(this);
        ImageView home_faceLibraryTv = findViewById(R.id.home_faceLibraryTv);
        home_faceLibraryTv.setOnClickListener(this);
        home_dataTv = findViewById(R.id.home_dataTv);
        home_dataTv.setText("有效期至" + FaceSDKManager.getInstance().getLicenseData(this));
        checkRBGTexture = findViewById(R.id.check_rgb_texture);
        checkNIRTexture = findViewById(R.id.check_nir_texture);
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);
        progressGroup = findViewById(R.id.progress_group);

        et_qrcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //校验18位身份证号码通过在进行查询个人预约信息
                if (RegexUtils.isIDCard18(s.toString())){
                    LogUtils.i(s);
                    //根据身份证号查询用户信息
                    queryVisitRegisterRecordList(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        KeyboardsUtils.hintKeyBoards(et_qrcode);
    }

    @Override
    public void onClick(View view) {
        if (!isDBLoad) {
            return;
        }
        switch (view.getId()) {
            case R.id.home_menuImg:
                if (!isCheck) {
                    isCheck = true;
                    home_menuImg.setImageResource(R.mipmap.icon_titlebar_menu_hl);
                    showPopupWindow(home_menuImg);
                } else {
                    dismissPopupWindow();
                }
                break;
            case R.id.relative_register: // 人脸注册
                dismissPopupWindow();
                SharedPreferences sharedPreferences = this.getSharedPreferences("type", MODE_PRIVATE);
                mLiveType = sharedPreferences.getInt("type", 0);
                com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager.getInstance().setActiveLog();
                judgeLiveType(mLiveType, FaceRegisterNewActivity.class, FaceRegisterNewNIRActivity.class,
                        FaceRegisterNewDepthActivity.class, FaceRegisterNewRgbNirDepthActivity.class);
                break;
            case R.id.relative_manager: // 人脸库管理
                dismissPopupWindow();
                startActivity(new Intent(HomeActivity.this, UserManagerActivity.class));
                break;
            case R.id.home_gateRl:
                mLiveType = com.baidu.idl.main.facesdk.model.SingleBaseConfig.getBaseConfig().getType();
                // 闸机模块
                judgeLiveType(mLiveType,
                        FaceRGBGateActivity.class,
                        FaceNIRGateActivriy.class,
                        FaceDepthGateActivity.class,
                        FaceRgbNirDepthGataActivity.class);
                break;
            case R.id.home_checkRl:
                //无证认证
//                queryVisitRegisterRecordList();
                //访客登记步骤
                //01 识别二维码

                QRCodeManager.getInstance().with(this).setReqeustType(1).scanningQRCode(mOnQRCodeListener);

                break;
            case R.id.home_payRl:
                mLiveType = com.baidu.idl.main.facesdk.paymentlibrary.model.SingleBaseConfig.getBaseConfig().getType();
                // 支付模块
                judgeLiveType(mLiveType,
                        FaceRGBPaymentActivity.class,
                        FaceNIRPaymentActivity.class,
                        FaceDepthPaymentActivity.class,
                        FaceRgbNirDepthPaymentActivity.class
                );
                break;
            case R.id.home_livenessRl:
                mLiveType = com.baidu.idl.face.main.finance.model.SingleBaseConfig.getBaseConfig().getType();
                // 金融活检
                judgeLiveType(mLiveType,
                        FaceRGBFinanceActivity.class,
                        FaceNIRFinanceActivity.class,
                        FaceDepthFinanceActivity.class,
                        FaceRgbNirDepthFinanceActivity.class);
                break;
            case R.id.home_attributeRl:
                // 属性模块
                startActivity(new Intent(HomeActivity.this, FaceAttributeRgbActivity.class));
                break;
            case R.id.home_personRl:
                mLiveType = com.baidu.idl.main.facesdk.identifylibrary.model.SingleBaseConfig.getBaseConfig().getType();

                Log.i("HomeActivity", mLiveType + "");
                // 人证核验
                judgeLiveType(mLiveType,
                        FaceRGBPersonActivity.class,
                        FaceIRTestimonyActivity.class,
                        FaceDepthTestimonyActivity.class,
                        FaceRGBIRDepthTestimonyActivity.class);
                break;
            case R.id.home_driveRl:
                // 驾驶行为模块
                startActivity(new Intent(HomeActivity.this, DriverMonitorActivityDrivermonitor.class));
                break;
            case R.id.home_attentionRl:
                // 注意力模块
                startActivity(new Intent(HomeActivity.this, FaceGazeActivity.class));
                break;
        }
    }


    /**
     * @description 扫描二维码监听
     * @date: 2020/12/7 18:57
     * @author: Yuan
     * @return
     */
    private OnQRCodeListener mOnQRCodeListener = new OnQRCodeListener() {
        @Override
        public void onCompleted(String result) {
            //获取二维码解析内容
            if (StringUtils.isEmpty(result)) {
                ToastUtils.toast(HomeActivity.this, "二维码解析错误,请检查二维码");
                return;
            }
            //根据身份证号查询用户信息
            queryVisitRegisterRecordList(result);
        }

        @Override
        public void onError(Throwable errorMsg) {
            ToastUtils.toast(HomeActivity.this, "二维码解析错误,请检查二维码");
        }

        @Override
        public void onCancel() {

        }
    };


    /**
     * 查询已预约人员
     */
    public void queryVisitRegisterRecordList(String certificateNumber) {
        //定义预约用户查询接口URL
        String hostUrl = URLUtils.hostUrl+"/organ/visitRegisterRecord/queryByVisitRegisterRecord";
        OkHttpClient client = new OkHttpClient();
        Map<String, String> paramsMap = new HashMap<>();
        //查询未到访的
        paramsMap.put("certificateNumber", certificateNumber);
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
            public void onResponse(Call call, Response response) throws IOException {
                //清空身份证信息
                runOnUiThread(() -> et_qrcode.setText(""));
                //验证通过
                if (response.isSuccessful()) {
                    Log.i("onResponse", "isSuccessful");
                    String result = response.body().string();
                    JsonRootBean newsBeanList = JsonUtils.deserialize(result, JsonRootBean.class);
                    if (ObjectUtils.isNotEmpty(newsBeanList) && ObjectUtils.isNotEmpty(newsBeanList.getData())) {
                        //用户信息
                        LogUtils.json(newsBeanList.getData());
                        Intent intent = new Intent(HomeActivity.this, FaceRegisterNewActivity.class);
                        intent.putExtra("userName", newsBeanList.getData().getName());
                        intent.putExtra("certificateNumber", newsBeanList.getData().getCertificateNumber());
                        intent.putExtra("orgTitle", newsBeanList.getData().getOrgTitle());
                        startActivity(intent);
                    }else{
                        ToastUtils.toast(HomeActivity.this,
                                "暂无您的预约信息");
                    }
                } else {
                    ToastUtils.toast(HomeActivity.this,
                            "暂无您的预约信息");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                et_qrcode.setText("");
                ToastUtils.toast(HomeActivity.this,
                        "二维码解析错误");
                Log.i("onResponse", "-------------------------------");
                //...
            }

        });
    }


    /**
     * 提取特征值
     */
    private void getFeatures(List<VisitRegisterRecordBean> list) {
        LogUtils.json(list);
        //循环遍历抽取特征
        for (VisitRegisterRecordBean visitRegisterRecordBean : list) {
            //获取头像转为文件
            File file = getFileByUrl(visitRegisterRecordBean.getPersonalPhotos());
            //获取文件名称
            String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            //文件转bitmap 抽取特征
            Bitmap mCropBitmap = ImageUtils.getBitmap(file);
            LogUtils.i(visitRegisterRecordBean.getName());
            // 开始导入
            VisitorFaceSDKManager.getInstance().asyncImport(mCropBitmap, visitRegisterRecordBean.getName(), visitRegisterRecordBean.getCertificateNumber() + "." + suffix);
        }


        //导入人脸成功
        mLiveType = com.baidu.idl.main.facesdk.attendancelibrary.model.SingleBaseConfig.getBaseConfig().getType();
        // 考勤模块
        judgeLiveType(mLiveType,
                FaceRGBAttendanceActivity.class,
                FaceNIRAttendanceActivity.class,
                FaceDepthAttendanceActivity.class,
                FaceRGBNirDepthAttendanceActivity.class);


    }

    private void initListener() {
        if (com.baidu.idl.main.facesdk.identifylibrary.manager.FaceSDKManager.initStatus != com.baidu.idl.main.facesdk.identifylibrary.manager.FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            com.baidu.idl.main.facesdk.identifylibrary.manager.FaceSDKManager.getInstance().initModel(this, new SdkInitListener() {
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
                    com.baidu.idl.main.facesdk.identifylibrary.manager.FaceSDKManager.initModelSuccess = true;
                    com.baidu.idl.main.facesdk.identifylibrary.utils.ToastUtils.toast(HomeActivity.this, "模型加载成功，欢迎使用");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    com.baidu.idl.main.facesdk.identifylibrary.manager.FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        com.baidu.idl.main.facesdk.identifylibrary.utils.ToastUtils.toast(HomeActivity.this, "模型加载失败，请尝试重启应用");
                    }
                }
            });
        }
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


    private void judgeLiveType(int type, Class<?> rgbCls, Class<?> nirCls, Class<?> depthCls, Class<?> rndCls) {
        switch (type) {
            case 0: { // 不使用活体
                startActivity(new Intent(HomeActivity.this, rgbCls));
                break;
            }

            case 1: { // RGB活体
                startActivity(new Intent(HomeActivity.this, rgbCls));
                Log.i("HomeActivity", rgbCls.getName());
                break;
            }

            case 2: { // NIR活体
                startActivity(new Intent(HomeActivity.this, nirCls));
                break;
            }

            case 3: { // 深度活体
                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
                judgeCameraType(cameraType, depthCls);
                break;
            }

            case 4: { // rgb+nir+depth活体
                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
                judgeCameraType(cameraType, rndCls);
            }
        }
    }

    private void judgeCameraType(int cameraType, Class<?> depthCls) {
        switch (cameraType) {
            case 1: { // pro
                startActivity(new Intent(HomeActivity.this, depthCls));
                break;
            }

            case 2: { // atlas
                startActivity(new Intent(HomeActivity.this, depthCls));
                break;
            }

            case 6: { // Pico
                //  startActivity(new Intent(HomeActivity.this,
                // PicoFaceDepthLivenessActivity.class));
                break;
            }

            default:
                startActivity(new Intent(HomeActivity.this, depthCls));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        QRCodeManager.getInstance().with(this).onActivityResult(requestCode, resultCode, data);
    }
}
