package com.baidu.idl.main.facesdk.registerlibrary.user.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.registerlibrary.user.listener.OnImportListener;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.ImportFeatureResult;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.FileUtils;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.model.User;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 导入相关管理类
 * Created by v_liujialu01 on 2019/5/28.
 */

public class ImportFileManager {
    private static final String TAG = "ImportFileManager";

    private Future mFuture;
    private ExecutorService mExecutorService;
    private OnImportListener mImportListener;
    // 是否需要导入
    private volatile boolean mIsNeedImport;

    private int mTotalCount;
    private int mFinishCount;
    private int mSuccessCount;
    private int mFailCount;

    private static class HolderClass {
        private static final ImportFileManager instance = new ImportFileManager();
    }

    public static ImportFileManager getInstance() {
        return HolderClass.instance;
    }

    // 私有构造，实例化ExecutorService
    private ImportFileManager() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    public void setOnImportListener(OnImportListener importListener) {
        mImportListener = importListener;
    }

    /**
     * 开始批量导入
     */
    public void batchImport() {
        // 1、获取导入目录 /sdcard/Face-Import
        File batchImportDir = FileUtils.getBatchImportDirectory();
        // 2、遍历该目录下的所有文件
        File[] picFiles = batchImportDir.listFiles();
        if (picFiles == null || picFiles.length == 0) {
            Log.i(TAG, "导入数据的文件夹没有数据");
            if (mImportListener != null) {
                mImportListener.showToastMessage("导入数据的文件夹没有数据");
            }
            return;
        }

        // 开启线程导入图片
        asyncImport(picFiles);
    }

    public void setIsNeedImport(boolean isNeedImport) {
        mIsNeedImport = isNeedImport;
    }

    /**
     * 开启线程导入图片
     * @param picFiles  要导入的图片集
     */
    private void asyncImport(final File[] picFiles) {
        if (mFuture != null && !mFuture.isDone()) {
            return;
        }
        mIsNeedImport = true;     // 判断是否需要导入
        mFinishCount = 0;         // 已完成的图片数量
        mSuccessCount = 0;        // 已导入成功的图片数量
        mFailCount = 0;           // 已导入失败的图片数量

        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }

        mFuture = mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (picFiles == null || picFiles.length == 0) {
                        Log.i(TAG, "导入数据的文件夹没有数据");
                        if (mImportListener != null) {
                            mImportListener.showToastMessage("导入数据的文件夹没有数据");
                        }
                        return;
                    }

                    // 读取图片成功，开始显示进度条
                    if (mImportListener != null) {
                        mImportListener.showProgressView();
                    }

                    Thread.sleep(400);

                    mTotalCount = picFiles.length;  // 总图片数

                    for (int i = 0; i < picFiles.length; i++) {
                        if (!mIsNeedImport) {
                            break;
                        }
                        File picFile = picFiles[i];
                        if (picFile.isDirectory()) {
                            Log.e(TAG, "当前内容是文件夹");
                            mFinishCount++;
                            mFailCount++;
                            // 更新进度
                            updateProgress(mTotalCount, mSuccessCount, mFailCount,
                                    ((float) mFinishCount / (float) mTotalCount));
                            continue;
                        }
                        // 3、获取图片名
                        String picName = picFiles[i].getName();
                        String name = picName.substring(0, picName.lastIndexOf("."));
                        Log.e(TAG, "i = " + i + ", picName = " + picName);
                        // 4、判断图片后缀
                        if (!picName.endsWith(".jpg") && !picName.endsWith(".png") &&
                                !picName.endsWith(".PNG") && !picName.endsWith(".JPG")) {
                            Log.e(TAG, "图片后缀不满足要求");
                            FileUtils.saveFile(picFiles[i], "Face-Import-Fail" ,
                                    name + "_1");
                            mFinishCount++;
                            mFailCount++;
                            // 更新进度
                            updateProgress(mTotalCount, mSuccessCount, mFailCount,
                                    ((float) mFinishCount / (float) mTotalCount));
                            continue;
                        }

                        // 5、获取不带后缀的图片名，即用户名
                        String userName = FileUtils.getFileNameNoEx(picName).trim();

                        boolean success = false;  // 判断成功状态

                        // 6、判断姓名是否有效
                        String nameResult = FaceApi.getInstance().isValidName(userName);
                        if (!"0".equals(nameResult)) {
                            Log.i(TAG, nameResult);
                            FileUtils.saveFile(picFiles[i], "Face-Import-Fail" ,
                                    name + "_10");
                            mFinishCount++;
                            mFailCount++;
                            // 更新进度
                            updateProgress(mTotalCount, mSuccessCount, mFailCount,
                                    ((float) mFinishCount / (float) mTotalCount));
                            continue;
                        }

                        // 7、根据姓名查询数据库与文件中对应的姓名是否相等，如果相等，则直接过滤
                        List<User> listUsers = FaceApi.getInstance().getUserListByUserName(userName);
                        if (listUsers != null && listUsers.size() > 0) {
                            Log.i(TAG, "与之前图片名称相同");
                            boolean isDelete = FaceApi.getInstance().userDeleteByName(userName);
                            if (!isDelete){
                                Log.i(TAG, "之前特征删除失败");
                                FileUtils.saveFile(picFiles[i], "Face-Import-Fail" ,
                                        name + "_10");
                                mFinishCount++;
                                mFailCount++;
//                                 更新进度
                                updateProgress(mTotalCount, mSuccessCount, mFailCount,
                                        ((float) mFinishCount / (float) mTotalCount));
                                continue;
                            }
                            /**/
                        }

                        // 8、根据图片的路径将图片转成Bitmap
                        Bitmap bitmap = BitmapFactory.decodeFile(picFiles[i].getAbsolutePath());

                        // 9、判断bitmap是否转换成功
                        if (bitmap == null) {
                            Log.e(TAG, picName + "：该图片转成Bitmap失败");
                            mFinishCount++;
                            mFailCount++;
                            BitmapUtils.saveRgbBitmap(bitmap , "Face-Import-Fail" ,
                                    name + "_2");
                            // 更新进度
                            updateProgress(mTotalCount, mSuccessCount, mFailCount,
                                    ((float) mFinishCount / (float) mTotalCount));
                            continue;
                        }
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
                        // 10、走人脸SDK接口，通过人脸检测、特征提取拿到人脸特征值
                        result = getFeature(bitmap1, bytes,
                                BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);

                        // 11、判断是否提取成功：128为成功，-1为参数为空，-2表示未检测到人脸
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
                                        success = true;
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

                        // 判断成功与否
                        if (success) {
                            mSuccessCount++;
                        } else {
                            mFailCount++;
                            Log.e(TAG, "失败图片:" + picName);
                        }
                        mFinishCount++;
                        // 导入中（用来显示进度）
                        Log.i(TAG, "mFinishCount = " + mFinishCount
                                + " progress = " + ((float) mFinishCount / (float) mTotalCount));

                        updateProgress(mTotalCount, mSuccessCount, mFailCount,
                                ((float) mFinishCount / (float) mTotalCount));
                        /*if (mImportListener != null) {
                            mImportListener.onImporting(mTotalCount, mSuccessCount, mFailCount,
                                    ((float) mFinishCount / (float) mTotalCount));
                        }*/
                    }

                    // 导入完成
                    if (mImportListener != null) {
                        mImportListener.endImport(mTotalCount, mSuccessCount, mFailCount);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "exception = " + e.getMessage());
                }
            }
        });
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
    // 旋转bitmap检测是否存在人脸
    private int rotationDetection(Bitmap bitmap , int angle){
        return rotationDetection(bitmap , angle , 1);
    }

    private int rotationDetection(Bitmap bitmap , int angle , int index){
        if (bitmap == null){
            return 2;
        }
        Bitmap angleBitmap = BitmapUtils.adjustPhotoRotation(bitmap , angle);
        BDFaceImageInstance imageInstance = new BDFaceImageInstance(angleBitmap);
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, imageInstance);
        if (!angleBitmap.isRecycled()) {
            angleBitmap.recycle();
        }
        if (faceInfos == null || faceInfos.length == 0) {
            imageInstance.destory();
            if (index == 3){
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                return 8;
            } else {
                return rotationDetection(bitmap , angle + 90 , index + 1);
            }
        }
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return 3;
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



    private void updateProgress(int totalCount, int successCount, int failureCount, float progress) {
        if (mImportListener != null) {
            mImportListener.onImporting(totalCount, successCount, failureCount, progress);
        }
    }

    /**
     * 释放功能，用于关闭线程操作
     */
    public void release() {
        if (mFuture != null && !mFuture.isCancelled()) {
            mFuture.cancel(true);
            mFuture = null;
        }

        if (mExecutorService != null) {
            mExecutorService.shutdown();
            mExecutorService = null;
        }
    }
}
