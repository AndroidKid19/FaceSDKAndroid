package createbest.sdk.bihu.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.usb.UsbManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 封装的Camera组件，内部处理了摄像头调用、图像预览、提供遮罩层绘制接口、提供人脸标注层绘制接口
 */
public class CameraView extends FrameLayout implements SurfaceHolder.Callback {
    private ExecutorService dataExecutorService = Executors.newCachedThreadPool();
    private SurfaceView faceView, rgbView, nirView, maskView;
    private SurfaceHolder foreHolder;
    private boolean openRgb = true;
    private boolean openNir = true;
    private boolean showNir = false;
    private int cameraPreviewWidth, cameraPreviewHeight, detectAreaWidth, detectAreaHeight;
    private int orientation;
    private int cameraCount;
    private MaskDrawer maskDrawer;
    private FaceDrawer defaultFaceDrawer = new FaceDrawer.RectFaceDrawer();
    private BroadcastReceiver usbRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int cameraCount = Camera.getNumberOfCameras();
            if (CameraView.this.cameraCount != cameraCount) {
                CameraView.this.cameraCount = cameraCount;
                if (cameraCount == 0) {
                    closeCamera(CameraManager.CameraType.RGB_CAMERA);
                    closeCamera(CameraManager.CameraType.NIR_CAMERA);
                } else if (cameraCount == 1) {
                    if ((openRgb && !openNir) || (!openRgb && openNir)) {
                        runCamera(CameraManager.CameraType.RGB_CAMERA);
                        closeCamera(CameraManager.CameraType.NIR_CAMERA);
                    } else {
                        closeCamera(CameraManager.CameraType.RGB_CAMERA);
                        closeCamera(CameraManager.CameraType.NIR_CAMERA);
                    }
                } else {
                    if (openRgb) {
                        runCamera(CameraManager.CameraType.RGB_CAMERA);
                    }
                    if (openNir) {
                        runCamera(CameraManager.CameraType.NIR_CAMERA);
                    }
                }
            }
        }
    };

    /**
     * 设置遮罩层绘制器,如果不设置将使用一个默认的绘制器。
     *
     * @param maskDrawer
     */
    public void setMaskDrawer(MaskDrawer maskDrawer) {
        this.maskDrawer = maskDrawer;
    }

    /**
     * 设置摄像头分辨率
     *
     * @param width
     * @param height
     */
    public void setCameraPreviewSize(int width, int height) {
        cameraPreviewWidth = width;
        cameraPreviewHeight = height;
    }

    /**
     * 可以指定检测区域，值必须是20的倍数；也可以不指定，则检测区域为可见部分。
     *
     * @param width
     * @param height
     */
    public void setDetectSize(int width, int height) {
        detectAreaWidth = width;
        detectAreaHeight = height;
    }

    /**
     * 打开摄像头
     * @param cameraType
     */
    private void runCamera(final CameraManager.CameraType cameraType) {
        String tag = String.valueOf(this.hashCode());
        SurfaceHolder holder = cameraType == CameraManager.CameraType.RGB_CAMERA ? rgbView.getHolder() : (showNir ? nirView.getHolder() : null);
        CameraManager.getInstance().start(tag, cameraType, cameraPreviewWidth, cameraPreviewHeight, holder, new CameraManager.Callback() {
            @Override
            public void onError(int error, Camera camera) {
                Log.d("raotaoCamera", "相机错误回调:error=" + error + " cameraType=" + cameraType.name());
                runCamera(cameraType);
            }

            @Override
            public void onPreviewFrame(final byte[] data, final Camera camera) {
                int facing = CameraManager.getInstance().getCameraFacing(cameraType);
                int orientation = CameraManager.getInstance().getCameraOrientation(cameraType);
                cropAndSaveCameraData(data, facing, orientation, cameraType);
                camera.addCallbackBuffer(data);
                if (cameraDataCallback != null) {
                    dataExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            cameraDataCallback.onCameraDataUpdate(Nv21Data.RGB, Nv21Data.NIR);
                        }
                    });
                }
            }
        });
    }

    /**
     * 关闭摄像头
     * @param cameraType
     */
    private void closeCamera(CameraManager.CameraType cameraType) {
        String tag = String.valueOf(this.hashCode());
        CameraManager.getInstance().release(tag, cameraType);
    }

    /**
     * 设置是否开启NIR摄像头
     * @param openNir
     */
    public void setOpenNir(boolean openNir) {
        if (openNir != this.openNir) {
            this.openNir = openNir;
            if (openNir) {
                nirView.setVisibility(VISIBLE);
            } else {
                nirView.setVisibility(INVISIBLE);
            }
        }
    }

    /**
     * 设置是否预览NIR
     * @param showNir
     */
    public void setShowNir(boolean showNir) {
        if (showNir != this.showNir) {
            this.showNir = showNir;
            if (this.openNir) {
                nirView.setVisibility(INVISIBLE);
                nirView.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * 绘制人脸框
     *
     * @param faceData 人脸数据
     */
    public void drawFace(RectF faceData) {
        drawFace(faceData, defaultFaceDrawer);
    }

    /**
     * 绘制人脸框
     *
     * @param faceData   人脸数据(泛型参数)
     * @param faceDrawer 人脸标注层绘制器，泛型类型必须和faceData类型一致
     */
    public <T> void drawFace(T faceData, FaceDrawer<T> faceDrawer) {
        if (foreHolder != null) {
            Canvas canvas = foreHolder.lockCanvas();
            try {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                if (faceData != null) {
                    faceDrawer.drawFace(getMeasuredWidth(), getMeasuredHeight(), getPreviewScaleHorizontal(),
                            getPreviewScaleVertical(), detectAreaWidth, detectAreaHeight, faceData, canvas);
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                foreHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        if (holder == rgbView.getHolder()) {
            if (openRgb) {
                runCamera(CameraManager.CameraType.RGB_CAMERA);
            }
        } else if (holder == nirView.getHolder()) {
            if (openNir) {
                runCamera(CameraManager.CameraType.NIR_CAMERA);
            }
        } else if (holder == faceView.getHolder()) {
            foreHolder = holder;
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            getContext().registerReceiver(usbRecevier, filter);
        } else if (holder == maskView.getHolder()) {
            Canvas canvas = holder.lockCanvas();
            if (maskDrawer != null) {
//                maskDrawer = new MaskDrawer.DrawableMaskDrawer(getContext()).setEnlarge(36 * getResources().getDisplayMetrics().density,
//                        30 * getResources().getDisplayMetrics().density);
                maskDrawer.drawMask(getMeasuredWidth(), getMeasuredHeight(), getPreviewScaleHorizontal(), getPreviewScaleVertical(), detectAreaWidth,
                        detectAreaHeight, canvas);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (holder == rgbView.getHolder()) {
            closeCamera(CameraManager.CameraType.RGB_CAMERA);
        } else if (holder == nirView.getHolder()) {
            closeCamera(CameraManager.CameraType.NIR_CAMERA);
        } else if (holder == faceView.getHolder()) {
            foreHolder = null;
            getContext().unregisterReceiver(usbRecevier);
        }
    }

    /**
     * 从预览图像中扣出检测区域
     *
     * @param data
     * @return
     */
    private void cropAndSaveCameraData(byte[] data, int facing, int orientation, CameraManager.CameraType cameraType) {
        int detectWidthDisplayed = detectAreaWidth;
        int detectHeightDisplayed = detectAreaHeight;
        if (orientation % 180 == 90) {
            detectWidthDisplayed = detectAreaHeight;
            detectHeightDisplayed = detectAreaWidth;
        }
        Nv21Data nv21Data = cameraType == CameraManager.CameraType.RGB_CAMERA ? Nv21Data.RGB : Nv21Data.NIR;
        byte[] newData = cropNV21(data, cameraPreviewWidth, cameraPreviewHeight,
                (cameraPreviewWidth - detectWidthDisplayed) / 2,
                (cameraPreviewHeight - detectHeightDisplayed) / 2,
                detectWidthDisplayed, detectHeightDisplayed);
        nv21Data.updateData(newData, detectWidthDisplayed, detectHeightDisplayed, cameraPreviewWidth, cameraPreviewHeight, orientation, facing);
    }

    /**
     * NV21裁剪 by lake 算法效率 11ms
     *
     * @param src    源数据
     * @param width  源宽
     * @param height 源高
     * @param left   顶点坐标
     * @param top    顶点坐标
     * @param clip_w 裁剪后的宽
     * @param clip_h 裁剪后的高
     * @return 裁剪后的数据
     */
    private static byte[] cropNV21(byte[] src, int width, int height, int left, int top, int clip_w, int clip_h) {
        if (left > width || top > height) {
            return null;
        }
        //取偶
        int x = left / 4 * 4, y = top / 4 * 4;
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        int w = clip_w / 4 * 4, h = clip_h / 4 * 4;
        int y_unit = w * h;
        if (x + w > width) {
            w = width - x;
        }
        if (y + h > height) {
            h = height - y;
        }
        int src_unit = width * height;
        int uv = y_unit / 2;
        byte[] nData = new byte[y_unit + uv];

        for (int i = y, len_i = y + h; i < len_i; i++) {
            for (int j = x, len_j = x + w; j < len_j; j++) {
                nData[(i - y) * w + j - x] = src[i * width + j];
                nData[y_unit + ((i - y) / 2) * w + j - x] = src[src_unit + i / 2 * width + j];
            }
        }
        return nData;
    }

    /**
     * RGB预览缩放——水平方向 （CameraView的宽 ： 摄像头预览图像的宽）
     *
     * @return
     */
    public float getPreviewScaleHorizontal() {
        int orientation = CameraManager.getInstance().getCameraOrientation(CameraManager.CameraType.RGB_CAMERA);
        return getMeasuredWidth() * 1f / (orientation % 180 == 0 ? cameraPreviewWidth : cameraPreviewHeight);
    }

    /**
     * RGB预览缩放——垂直方向 （CameraView的高 ： 摄像头预览图像的高）
     *
     * @return
     */
    public float getPreviewScaleVertical() {
        int orientation = CameraManager.getInstance().getCameraOrientation(CameraManager.CameraType.RGB_CAMERA);
        return getMeasuredHeight() * 1f / (orientation % 180 == 0 ? cameraPreviewHeight : cameraPreviewWidth);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        orientation = CameraManager.getInstance().getCameraOrientation(CameraManager.CameraType.RGB_CAMERA);
        int cameraPreviewWidthDisplayed = cameraPreviewWidth;
        int cameraPreviewHeightDisplayed = cameraPreviewHeight;
        if (orientation % 180 == 90) {
            cameraPreviewWidthDisplayed = cameraPreviewHeight;
            cameraPreviewHeightDisplayed = cameraPreviewWidth;
        }
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {//如果宽高都限定了，就按限定值
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {//如果只限定了宽，按摄像头预览分辨率的宽高比来计算高
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = (int) (width * cameraPreviewHeightDisplayed * 1f / cameraPreviewWidthDisplayed);
            setMeasuredDimension(width, height);
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {//如果只限定了高，按摄像头预览分辨率的宽高比来计算宽
            int height = MeasureSpec.getSize(heightMeasureSpec);
            int width = (int) (height * cameraPreviewWidthDisplayed * 1f / cameraPreviewHeightDisplayed);
            setMeasuredDimension(width, height);
        } else {//如果都没有限定，就按正方向的预览实际分辨率
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            float dw = width * 1f / cameraPreviewWidthDisplayed;
            float dh = height * 1f / cameraPreviewHeightDisplayed;
            float d = Math.max(dw, dh);
            setMeasuredDimension((int) (cameraPreviewWidthDisplayed * d), (int) (cameraPreviewHeightDisplayed * d));
        }

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
        rgbView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        maskView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        faceView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        int childWidthMeasureSpec2 = MeasureSpec.makeMeasureSpec((int) (getMeasuredWidth() / 4f), MeasureSpec.EXACTLY);
        int childHeightMeasureSpec2 = MeasureSpec.makeMeasureSpec((int) (getMeasuredHeight() / 4f), MeasureSpec.EXACTLY);
        nirView.measure(childWidthMeasureSpec2, childHeightMeasureSpec2);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (detectAreaWidth == 0 || detectAreaHeight == 0) {
            int cameraPreviewWidthDisplayed = cameraPreviewWidth;
            int cameraPreviewHeightDisplayed = cameraPreviewHeight;
            if (orientation % 180 == 90) {
                cameraPreviewWidthDisplayed = cameraPreviewHeight;
                cameraPreviewHeightDisplayed = cameraPreviewWidth;
            }
            int detectWidth = cameraPreviewWidthDisplayed;
            if (left < 0) {
                detectWidth = cameraPreviewWidthDisplayed - Math.abs(left) * 2 * cameraPreviewWidthDisplayed / getMeasuredWidth();
            }
            int detectHeight = cameraPreviewHeightDisplayed;
            if (top < 0) {
                detectHeight = cameraPreviewHeightDisplayed - Math.abs(top) * 2 * cameraPreviewHeightDisplayed / getMeasuredHeight();
            }
            //必须是20的倍数，否则百度算法不支持
            int minDetectSize = (Math.min(detectWidth, detectHeight) + 10) / 20 * 20;
            detectAreaWidth = (detectWidth + 10) / 20 * 20;
            detectAreaHeight = (detectHeight + 10) / 20 * 20;
        }
    }

    /**
     * 添加图像预览子组件
     *
     * @param context
     */
    private void initViews(Context context) {
        rgbView = new SurfaceView(context);
        rgbView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        addView(rgbView);

        faceView = new SurfaceView(context);
        faceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        addView(faceView);
        faceView.setZOrderMediaOverlay(true);

        maskView = new SurfaceView(context);
        maskView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        addView(maskView);
        maskView.setZOrderMediaOverlay(true);

        nirView = new SurfaceView(context);
        nirView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        addView(nirView);
        LayoutParams params = (LayoutParams) nirView.getLayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        nirView.setLayoutParams(params);
        nirView.setZOrderMediaOverlay(true);

        rgbView.getHolder().addCallback(this);
        nirView.getHolder().addCallback(this);
        faceView.getHolder().addCallback(this);
        maskView.getHolder().addCallback(this);
    }

    public CameraView(Context context) {
        super(context);
        initViews(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    public enum Nv21Data {
        RGB, NIR;
        private byte[] data;
        private int dataWidth;
        private int dataHeight;
        private int cameraWidth;
        private int cameraHeight;
        private int orientation;
        private int facing;
        private long time;

        public void updateData(byte[] data, int dataWidth, int dataHeight, int cameraWidth, int cameraHeight, int orientation, int facing) {
            this.data = data;
            this.dataWidth = dataWidth;
            this.dataHeight = dataHeight;
            this.cameraWidth = cameraWidth;
            this.cameraHeight = cameraHeight;
            this.orientation = orientation;
            this.facing = facing;
            time = System.currentTimeMillis();
        }

        public byte[] getData() {
            if (System.currentTimeMillis() - time < 150) {
                return data;
            }
            return null;
        }

        public int getDataWidth() {
            return dataWidth;
        }

        public int getDataHeight() {
            return dataHeight;
        }

        public int getCameraWidth() {
            return cameraWidth;
        }

        public int getCameraHeight() {
            return cameraHeight;
        }

        public int getOrientation() {
            return orientation;
        }

        public int getFacing() {
            return facing;
        }

        public long getTime() {
            return time;
        }
    }

    /**
     * 催收Data数据,外部主动索要图像数据。
     */
    public void urge() {
        if (cameraDataCallback != null) {
            cameraDataCallback.onCameraDataUpdate(Nv21Data.RGB, Nv21Data.NIR);
        }
    }

    private CameraDataCallback cameraDataCallback;

    /**
     * 摄像头预览数据回调
     */
    public interface CameraDataCallback {
        /**
         * 获取到图像数据
         *
         * @param rgbData RGB图像数据
         * @param nirData NIR图像数据
         */
        void onCameraDataUpdate(Nv21Data rgbData, Nv21Data nirData);
    }

    /**
     * 设置摄像头预览数据回调
     *
     * @param cameraDataCallback
     */
    public void setCameraDataCallback(CameraDataCallback cameraDataCallback) {
        this.cameraDataCallback = cameraDataCallback;
    }

    float x, y;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float x2 = event.getX();
                float y2 = event.getY();
                if (Math.abs(x2 - x) < 80 && getMeasuredHeight() / 4 < Math.abs(y2 - y)) {
                    CameraManager.CameraType type = null;
                    if (x < getMeasuredWidth() / 2 && x2 < getMeasuredWidth() / 2) {
                        type = CameraManager.CameraType.RGB_CAMERA;
                    }
                    if (x > getMeasuredWidth() / 2 && x2 > getMeasuredWidth() / 2) {
                        type = CameraManager.CameraType.NIR_CAMERA;
                    }
                    if (type != null) {
                        if (y2 < y) {
                            CameraManager.getInstance().setExposureCompensation(type, 1);
                        } else {
                            CameraManager.getInstance().setExposureCompensation(type, -1);
                        }
                    }
                }
                return true;
        }
        return super.onTouchEvent(event);
    }
}