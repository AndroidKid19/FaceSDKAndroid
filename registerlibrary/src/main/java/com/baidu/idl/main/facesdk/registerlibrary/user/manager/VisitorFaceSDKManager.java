package com.baidu.idl.main.facesdk.registerlibrary.user.manager;

import android.graphics.Bitmap;
import android.util.Log;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.ImportFeatureResult;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.FileUtils;
import com.example.datalibrary.api.FaceApi;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ProjectName: FaceSDKAndroid
 * @Package: com.baidu.idl.main.facesdk.registerlibrary.user.manager
 * @ClassName: VisitorFaceSDKManager
 * @Description:
 * @Author: Yuan
 * @CreateDate: 2022/2/23 14:53
 * @UpdateUser: 更新者
 * @UpdateDate: 2022/2/23 14:53
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class VisitorFaceSDKManager {
    private static final String TAG = "VisitorFaceSDKManager";
    private static class HolderClass {
        private static final VisitorFaceSDKManager instance = new VisitorFaceSDKManager();
    }

    public static VisitorFaceSDKManager getInstance() {
        return HolderClass.instance;
    }

    /**
     * 开始批量导入
     */
    public void asyncImport(Bitmap bitmap,String userName,String picName) {
        String name = picName.substring(0, picName.lastIndexOf("."));
        Bitmap bitmap1 ;
        // 图片缩放
        if (bitmap.getWidth() * bitmap.getHeight() > 3000 * 2000) {
            if (bitmap.getWidth() > bitmap.getHeight()) {
                float scale = 1 / (bitmap.getWidth() * 1.0f / 1000.0f);
                bitmap1 = BitmapUtils.scale(bitmap, scale);
            } else {
                float scale = 1 / (bitmap.getHeight() * 1.0f / 1000.0f);
                bitmap1 = BitmapUtils.scale(bitmap, scale);
            }
        }else {
            bitmap1 = bitmap;
        }
        if (bitmap1 != bitmap && !bitmap.isRecycled()){
            bitmap.recycle();
        }

        byte[] bytes = new byte[512];
        ImportFeatureResult result;
        // 1、走人脸SDK接口，通过人脸检测、特征提取拿到人脸特征值
        result = getFeature(bitmap1, bytes,
                BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);

        // 2、判断是否提取成功：128为成功，-1为参数为空，-2表示未检测到人脸
        Log.i(TAG, "live_photo = " + result.getResult());
        if (result.getResult() == 128) {
            // 将用户信息保存到数据库中
            boolean importDBSuccess = FaceApi.getInstance().registerUserIntoDBmanager(null,
                    userName, picName, null, bytes);

            // 保存数据库成功
            if (importDBSuccess) {
                // 保存图片到新目录中
                File facePicDir = FileUtils.getBatchImportSuccessDirectory();
                if (facePicDir != null) {
                    File savePicPath = new File(facePicDir, picName);
                    if (FileUtils.saveBitmap(savePicPath, result.getBitmap())) {
                        Log.i(TAG, "头像保存失败");
                    } else {
                        Log.i(TAG, "头像保存失败");
                    }
                }
            } else {
                Log.e(TAG, picName + "：保存到数据库失败");
                BitmapUtils.saveRgbBitmap(bitmap1 , "Face-Import-Fail" ,
                        name + "_10");
            }
        } else {
            Log.e(TAG, picName + " 错误码：" + result.getResult());
            BitmapUtils.saveRgbBitmap(bitmap1 , "Face-Import-Fail" ,
                    name + "_" + ((int) result.getResult()));
        }
        if (result.getBitmap() != null && !result.getBitmap().isRecycled()){
            result.getBitmap().recycle();
        }
        // 图片回收
        if (!bitmap1.isRecycled()) {
            bitmap1.recycle();
        }
    }

    /**
     * 提取特征值
     */
    public ImportFeatureResult getFeature(Bitmap bitmap, byte[] feature, BDFaceSDKCommon.FeatureType featureType) {
        if (bitmap == null) {
            return new ImportFeatureResult(2, null);
        }

        BDFaceImageInstance imageInstance = new BDFaceImageInstance(bitmap);
        // 最大检测人脸，获取人脸信息
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, imageInstance);
        if (faceInfos == null || faceInfos.length == 0) {
            imageInstance.destory();
            // 图片外扩
            Bitmap broadBitmap = BitmapUtils.broadImage(bitmap);
            imageInstance = new BDFaceImageInstance(broadBitmap);
            // 最大检测人脸，获取人脸信息
            faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                    .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, imageInstance);
            // 若外扩后还未检测到人脸，则旋转图片检测
            if (faceInfos == null || faceInfos.length == 0) {
                return new ImportFeatureResult(/*rotationDetection(broadBitmap , 90)*/8, null);
            }
        }
        // 判断多人脸
        if (faceInfos.length > 1){
            imageInstance.destory();
            return new ImportFeatureResult(9, null);
        }
        FaceInfo faceInfo = faceInfos[0];
        // 判断质量
        int quality = onQualityCheck(faceInfo);
        if (quality != 0){
            return new ImportFeatureResult(quality, null);
        }
        // 人脸识别，提取人脸特征值
        float ret = FaceSDKManager.getInstance().getFaceFeature().feature(
                featureType, imageInstance,
                faceInfo.landmarks, feature);
        // 人脸抠图
        BDFaceImageInstance cropInstance = FaceSDKManager.getInstance().getFaceCrop()
                .cropFaceByLandmark(imageInstance, faceInfo.landmarks,
                        2.0f, true, new AtomicInteger());
        if (cropInstance == null) {
            imageInstance.destory();
            return new ImportFeatureResult(10, null);
        }

        Bitmap cropBmp = BitmapUtils.getInstaceBmp(cropInstance);
        cropInstance.destory();
        imageInstance.destory();
        return new ImportFeatureResult(ret, cropBmp);
    }

    /**
     * 质量检测结果过滤，如果需要质量检测，
     * 需要调用 SingleBaseConfig.getBaseConfig().setQualityControl(true);设置为true，
     * 再调用  FaceSDKManager.getInstance().initConfig() 加载到底层配置项中
     *
     * @return
     */
    public int onQualityCheck(FaceInfo faceInfo) {

        if (!SingleBaseConfig.getBaseConfig().isQualityControl()) {
            return 0;
        }

        if (faceInfo != null) {

            // 角度过滤
            if (Math.abs(faceInfo.yaw) > SingleBaseConfig.getBaseConfig().getYaw()) {
                return 4;
            } else if (Math.abs(faceInfo.roll) > SingleBaseConfig.getBaseConfig().getRoll()) {
                return 4;
            } else if (Math.abs(faceInfo.pitch) > SingleBaseConfig.getBaseConfig().getPitch()) {
                return 4;
            }

            // 模糊结果过滤
            float blur = faceInfo.bluriness;
            if (blur > SingleBaseConfig.getBaseConfig().getBlur()) {
                return 5;
            }

            // 光照结果过滤
            float illum = faceInfo.illum;
            if (illum < SingleBaseConfig.getBaseConfig().getIllumination()) {
                return 7;
            }


            // 遮挡结果过滤
            if (faceInfo.occlusion != null) {
                BDFaceOcclusion occlusion = faceInfo.occlusion;

                if (occlusion.leftEye > SingleBaseConfig.getBaseConfig().getLeftEye()) {
                    // 左眼遮挡置信度
                    return 6;
                } else if (occlusion.rightEye > SingleBaseConfig.getBaseConfig().getRightEye()) {
                    // 右眼遮挡置信度
                    return 6;
                } else if (occlusion.nose > SingleBaseConfig.getBaseConfig().getNose()) {
                    // 鼻子遮挡置信度
                    return 6;
                } else if (occlusion.mouth > SingleBaseConfig.getBaseConfig().getMouth()) {
                    // 嘴巴遮挡置信度
                    return 6;
                } else if (occlusion.leftCheek > SingleBaseConfig.getBaseConfig().getLeftCheek()) {
                    // 左脸遮挡置信度
                    return 6;
                } else if (occlusion.rightCheek > SingleBaseConfig.getBaseConfig().getRightCheek()) {
                    // 右脸遮挡置信度
                    return 6;
                } else if (occlusion.chin > SingleBaseConfig.getBaseConfig().getChinContour()) {
                    // 下巴遮挡置信度
                    return 6;
                } else {
                    return 0;
                }
            }
        }
        return 0;
    }


    //url转file
    private  File getFileByUrl(String fileUrl) {
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

}
