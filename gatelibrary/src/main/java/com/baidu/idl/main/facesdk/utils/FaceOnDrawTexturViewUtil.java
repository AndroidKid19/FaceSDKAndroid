package com.baidu.idl.main.facesdk.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.TextureView;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.gatecamera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.LivenessModel;
import com.baidu.idl.main.facesdk.model.SingleBaseConfig;
import com.example.datalibrary.model.User;

/**
 * Created by ShiShuaiFeng on 2019/6/5.
 */

public class FaceOnDrawTexturViewUtil {
    private static int faceID = -1;


    private FaceOnDrawTexturViewUtil() {
    }

    /**
     * 通过中心点坐标（x，y） 和 width ，绘制Rect
     *
     * @param faceInfo
     * @return
     */
    public static Rect getFaceRectTwo(FaceInfo faceInfo) {
        Rect rect = new Rect();
        rect.top = (int) ((faceInfo.centerY - faceInfo.width / 1.3));
        rect.left = (int) ((faceInfo.centerX - faceInfo.width / 2));
        rect.right = (int) ((faceInfo.centerX + faceInfo.width / 2));
        rect.bottom = (int) ((faceInfo.centerY + faceInfo.width / 1.8));
        return rect;
    }

    public static void mapFromOriginalRect(RectF rectF,
                                           AutoTexturePreviewView autoTexturePreviewView,
                                           BDFaceImageInstance imageFrame) {
        // 获取屏幕的宽
        int selfWidth = autoTexturePreviewView.getPreviewWidth();
        // 获取屏幕的高
        int selfHeight = autoTexturePreviewView.getPreviewHeight();
        // 新建矩阵对象
        Matrix matrix = new Matrix();
        // 当屏幕宽度/图像宽度>屏幕高度/图像高度时
        if (selfWidth * imageFrame.height > selfHeight * imageFrame.width) {
            // 将高度按照宽度比进行缩放
            int targetHeight = imageFrame.height * selfWidth / imageFrame.width;
            // 计算平移距离
            int delta = (targetHeight - selfHeight) / 2;
            // 计算宽度比
            float ratio = 1.0f * selfWidth / imageFrame.width;
            // 设置矩阵变换缩放比
            matrix.postScale(ratio, ratio);
            // 设置变换矩阵的平移距离
            matrix.postTranslate(0, -delta);
        } else {
            // 将宽度按照高度比进行缩放
            int targetWith = imageFrame.width * selfHeight / imageFrame.height;
            // 计算平移距离
            int delta = (targetWith - selfWidth) / 2;
            // 计算宽度比
            float ratio = 1.0f * selfHeight / imageFrame.height;
            // 设置矩阵变换缩放比
            matrix.postScale(ratio, ratio);
            // 设置变换矩阵的平移距离
            matrix.postTranslate(-delta, 0);
        }
        // 对人脸框数据进行矩阵变换
        matrix.mapRect(rectF);

    }

    public static void mapFromOriginalRect(RectF rectF,
                                           TextureView textureView,
                                           BDFaceImageInstance imageFrame) {
        int selfWidth = textureView.getWidth();
        int selfHeight = textureView.getHeight();
        Matrix matrix = new Matrix();
        if (selfWidth * imageFrame.height > selfHeight * imageFrame.width) {
            int targetHeight = imageFrame.height * selfWidth / imageFrame.width;
            int delta = (targetHeight - selfHeight) / 2;
            float ratio = 1.0f * selfWidth / imageFrame.width;
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(0, -delta);
        } else {
            int targetWith = imageFrame.width * selfHeight / imageFrame.height;
            int delta = (targetWith - selfWidth) / 2;
            float ratio = 1.0f * selfHeight / imageFrame.height;
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(-delta, 0);
        }
        matrix.mapRect(rectF);

    }


    public static void converttPointXY(float[] pointXY, AutoTexturePreviewView textureView,
                                       BDFaceImageInstance imageFrame, float width) {
        int selfWidth = textureView.getWidth();
        int selfHeight = textureView.getHeight();
        if (selfWidth * imageFrame.height > selfHeight * imageFrame.width) {
            int targetHeight = imageFrame.height * selfWidth / imageFrame.width;
            int delta = (targetHeight - selfHeight) / 2;
            float ratio = 1.0f * selfWidth / imageFrame.width;
            pointXY[0] = pointXY[0] * ratio;
            pointXY[1] = pointXY[1] * ratio;
            pointXY[1] = pointXY[1] - delta;
            pointXY[2] = width * ratio;
            pointXY[3] = width * ratio;
        } else {
            int targetWith = imageFrame.width * selfHeight / imageFrame.height;
            int delta = (targetWith - selfWidth) / 2;
            float ratio = 1.0f * selfHeight / imageFrame.height;
            pointXY[0] = pointXY[0] * ratio;
            pointXY[1] = pointXY[1] * ratio;
            pointXY[0] = pointXY[0] - delta;
            pointXY[2] = width * ratio;
            pointXY[3] = width * ratio;
        }
    }

    /**
     * 绘制人脸框
     *
     * @param canvas      画布
     * @param rectF       矩形
     * @param paint       画笔
     * @param strokeWidth 笔画的宽度
     * @param mScreenRate 线的长度
     */
    public static void drawRect(Canvas canvas, RectF rectF, Paint paint,
                                float strokeWidth, float mScreenRate , float facialSpacin) {
        // 左上横线
        canvas.drawRect(
                rectF.left,
                rectF.top,
                rectF.left + mScreenRate,
                rectF.top + strokeWidth,
                paint);
        // 左上竖线
        canvas.drawRect(
                rectF.left ,
                rectF.top,
                rectF.left + strokeWidth,
                rectF.top + mScreenRate,
                paint);
        // 右上横线
        canvas.drawRect(
                rectF.right - mScreenRate,
                rectF.top ,
                rectF.right,
                rectF.top + strokeWidth,
                paint);
        // 右上竖线
        canvas.drawRect(
                rectF.right,
                rectF.top,
                rectF.right - strokeWidth,
                rectF.top + mScreenRate,
                paint);
        // 左下横线
        canvas.drawRect(rectF.left,
                rectF.bottom,
                rectF.left + mScreenRate,
                rectF.bottom + strokeWidth,
                paint);
        // 左下竖线
        canvas.drawRect(rectF.left,
                rectF.bottom - mScreenRate,
                rectF.left + strokeWidth,
                rectF.bottom,
                paint);
        // 右下横线
        canvas.drawRect(rectF.right - mScreenRate,
                rectF.bottom,
                rectF.right,
                rectF.bottom + strokeWidth,
                paint);
        // 右下竖线
        canvas.drawRect(rectF.right,
                rectF.bottom - mScreenRate,
                rectF.right  - strokeWidth,
                rectF.bottom,
                paint);



        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setAlpha(25);
        canvas.drawRect(rectF.left + facialSpacin,
                rectF.top + facialSpacin,
                rectF.right - facialSpacin,
                rectF.bottom - facialSpacin,
                paint);
    }

    /**
     * 人脸框颜色
     *
     * @param mUser         当前的user
     * @param paint         人脸边框部分
     * @param paintBg       人脸边框阴影部分
     * @param livenessModel 人脸框数据
     */
    public static void drawFaceColor(User mUser, Paint paint, Paint paintBg, LivenessModel livenessModel) {
        if (faceID != livenessModel.getTrackFaceInfo()[0].faceID){
            paint.setColor(Color.parseColor("#FECD33"));
            paintBg.setColor(Color.parseColor("#FECD33"));
        }else if (mUser == null){
            paint.setColor(Color.parseColor("#FECD33"));
            paintBg.setColor(Color.parseColor("#FECD33"));
        }else {
            paint.setColor(Color.parseColor("#00baf2"));
            paintBg.setColor(Color.parseColor("#00baf2"));
        }
        faceID = livenessModel.getTrackFaceInfo()[0].faceID;
        paint.setAlpha(255);
    }

    public static void drawCircle(Canvas canvas, AutoTexturePreviewView mAutoCameraPreviewView,
                                  RectF rectF, Paint paint, Paint paintBg, FaceInfo faceInfo) {
        paint.setStyle(Paint.Style.STROKE);
        paintBg.setStyle(Paint.Style.STROKE);
        // 画笔粗细
        paint.setStrokeWidth(8);
        // 设置线条等图形的抗锯齿
        paint.setAntiAlias(true);
        paintBg.setStrokeWidth(13);
        paintBg.setAlpha(90);
        // 设置线条等图形的抗锯齿
        paintBg.setAntiAlias(true);
        if (faceInfo.width > faceInfo.height) {
            if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {
                canvas.drawCircle(mAutoCameraPreviewView.getWidth() - rectF.centerX(),
                        rectF.centerY(), rectF.width() / 2 - 8, paintBg);
                canvas.drawCircle(mAutoCameraPreviewView.getWidth() - rectF.centerX(),
                        rectF.centerY(), rectF.width() / 2, paint);
            } else {
                canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                        rectF.width() / 2 - 8, paintBg);
                canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                        rectF.width() / 2, paint);
            }

        } else {
            if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {
                canvas.drawCircle(mAutoCameraPreviewView.getWidth() - rectF.centerX(),
                        rectF.centerY(), rectF.height() / 2 - 8, paintBg);
                canvas.drawCircle(mAutoCameraPreviewView.getWidth() - rectF.centerX(),
                        rectF.centerY(), rectF.height() / 2, paint);
            } else {
                canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                        rectF.height() / 2 - 8, paintBg);
                canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                        rectF.height() / 2, paint);
            }
        }
    }
}
