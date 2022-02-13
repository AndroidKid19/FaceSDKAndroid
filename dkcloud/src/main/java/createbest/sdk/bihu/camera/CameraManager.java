package createbest.sdk.bihu.camera;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 摄像头管理器，一个实例对应一个摄像头
 */
public class CameraManager {
    private static CameraManager instance;

    public static CameraManager getInstance() {
        if (instance == null) {
            instance = new CameraManager();
        }
        return instance;
    }

    private Map<CameraType, CameraObject> cameras = new HashMap<>();

    /**
     * 获取摄像头旋转方向
     *
     * @param cameraType
     * @return
     */
    public synchronized int getCameraOrientation(CameraType cameraType) {
        CameraObject cameraObject = cameras.get(cameraType);
        if (cameraObject != null) {
            return cameraObject.rotation;
        }
        int cameraId = cameraIdMap.getCameraId(cameraType);
        int orientation = 0;
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            orientation = cameraInfo.orientation;
        } catch (Throwable throwable) {
        }
        return orientation;
    }

    /**
     * 获取摄像头前后朝向
     *
     * @param cameraType
     * @return
     */
    public synchronized int getCameraFacing(CameraType cameraType) {
        CameraObject cameraObject = cameras.get(cameraType);
        if (cameraObject != null) {
            return cameraObject.facing;
        }
        int cameraId = cameraIdMap.getCameraId(cameraType);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        return cameraInfo.facing;
    }

    /**
     * 获取摄像头支持的所有分辨率
     *
     * @param cameraType
     * @return
     */
    public synchronized List<Camera.Size> getCameraSupportSize(CameraType cameraType) {
        CameraObject cameraObject = cameras.get(cameraType);
        if (cameraObject != null) {
            return cameraObject.supportSizes;
        }
        int cameraId = cameraIdMap.getCameraId(cameraType);
        try {
            Camera camera = Camera.open(cameraId);
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            camera.release();
            return sizes;
        } catch (Exception e) {
        }
        return new ArrayList<Camera.Size>();
    }

    /**
     * 打开摄像头
     *
     * @param tag
     * @param cameraType
     * @param previewWidth
     * @param previewHeight
     * @param surfaceHolder
     * @param callback
     */
    public synchronized void start(String tag, CameraType cameraType, int previewWidth, int previewHeight, SurfaceHolder surfaceHolder, final Callback callback) {
        try {
            CameraObject cameraObject = cameras.get(cameraType);
            if (cameraObject != null) {
                cameraObject.camera.release();
                cameras.remove(cameraType);
            }

            int cameraId = cameraIdMap.getCameraId(cameraType);
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            Camera camera = Camera.open(cameraId);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(previewWidth, previewHeight);
            camera.setParameters(parameters);
            camera.setDisplayOrientation(cameraInfo.orientation);
            camera.setPreviewDisplay(surfaceHolder);
            camera.setErrorCallback(callback);
            camera.setPreviewCallbackWithBuffer(callback);
            camera.addCallbackBuffer(new byte[previewWidth * previewHeight * 3 / 2]);
            CameraObject co = new CameraObject(camera, tag, previewWidth, previewHeight, cameraInfo.facing, cameraInfo.orientation, parameters.getSupportedPreviewSizes());
            cameras.put(cameraType, co);
            camera.startPreview();
        } catch (Throwable throwable) {
            Log.d("raotaoCamera", "打开相机失败:" + throwable.toString());
        }
    }

    /**
     * 曝光调节
     * @param cameraType 摄像头类型：RGB、NIR
     * @param exposureCompensationDelta 曝光增减
     */
    public void setExposureCompensation(CameraType cameraType, int exposureCompensationDelta) {
        try {
            CameraObject cameraObject = cameras.get(cameraType);
            if (cameraObject != null) {
                Camera.Parameters parameters = cameraObject.camera.getParameters();
                int max = parameters.getMaxExposureCompensation();
                int min = parameters.getMinExposureCompensation();
                int e = parameters.getExposureCompensation();
                if (exposureCompensationDelta == 1 && e < max) {
                    parameters.setExposureCompensation(e + 1);
                } else if (exposureCompensationDelta == -1 && e > min) {
                    parameters.setExposureCompensation(e - 1);
                }
                cameraObject.camera.setParameters(parameters);
            }
        } catch (Throwable throwable) {
        }
    }

    /**
     * 释放摄像头
     *
     * @param tag
     * @param cameraType
     */
    public synchronized void release(final String tag, final CameraType cameraType) {
        CameraObject co = cameras.get(cameraType);
        if (co != null && co.tag.equals(tag)) {
            try {
                co.camera.setErrorCallback(null);
                co.camera.release();
                cameras.remove(cameraType);
            } catch (Exception e) {
                Log.d("raotaoCamera", "相机释放失败:" + e.toString());
            }
        }
    }

    public interface Callback extends Camera.ErrorCallback, Camera.PreviewCallback {
    }

    /**
     * CameraType和CameraId映射
     */
    public interface CameraIdMap {
        int getCameraId(CameraType cameraType);
    }

    private CameraIdMap cameraIdMap = new CameraIdMap() {
        @Override
        public int getCameraId(CameraType cameraType) {
            switch (cameraType) {
                case RGB_CAMERA:
                    return 0;
                case NIR_CAMERA:
                    return 1;
            }
            return 0;
        }
    };

    /**
     * 设置CameraType和CameraId映射
     *
     * @param cameraIdMap
     */
    public void setCameraIdMap(CameraIdMap cameraIdMap) {
        this.cameraIdMap = cameraIdMap;
    }

    public enum CameraType {
        RGB_CAMERA, NIR_CAMERA;
    }

    private class CameraObject {
        public final Camera camera;
        public final String tag;
        public final int previewWidth;
        public final int previewHeight;
        public final int facing;
        public final int rotation;
        public final List<Camera.Size> supportSizes;

        public CameraObject(Camera camera, String tag, int previewWidth, int previewHeight, int facing, int rotation, List<Camera.Size> supportSizes) {
            this.camera = camera;
            this.tag = tag;
            this.previewWidth = previewWidth;
            this.previewHeight = previewHeight;
            this.facing = facing;
            this.rotation = rotation;
            this.supportSizes = supportSizes;
        }
    }

    private CameraManager() {
    }
}
