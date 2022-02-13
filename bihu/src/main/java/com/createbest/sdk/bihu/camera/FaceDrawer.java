package com.createbest.sdk.bihu.camera;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/**
 * 人脸标注层绘制器
 */
public interface FaceDrawer<T> {
    /**
     * 绘制人脸标注
     *
     * @param viewWidth              界面预览组件（{@link CameraView}）的宽
     * @param viewHeight             界面预览组件（{@link CameraView}）的高
     * @param previewScaleHorizontal 图像像素转换成界面显示像素的缩放比例（水平方向）
     * @param previewScaleVertical   图像像素转换成界面显示像素的缩放比例（垂直方向）
     * @param detectWidth            检测区域的宽
     * @param detectHeight           检测区域的高
     * @param canvas                 画布
     */
    void drawFace(int viewWidth, int viewHeight, float previewScaleHorizontal, float previewScaleVertical, int detectWidth, int detectHeight, T faceData, Canvas canvas);

    class RectFaceDrawer implements FaceDrawer<RectF> {
        private Paint paint = new Paint();

        @Override
        public void drawFace(int viewWidth, int viewHeight, float previewScaleHorizontal, float previewScaleVertical, int detectWidth, int detectHeight, RectF faceData, Canvas canvas) {
            if (faceData != null) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                paint.setColor(Color.WHITE);
                paint.setAntiAlias(true);
                RectF faceRect = new RectF();
                faceRect.left = (viewWidth - detectWidth * previewScaleHorizontal) / 2f + faceData.left * previewScaleHorizontal;
                faceRect.right = (viewWidth - detectWidth * previewScaleHorizontal) / 2f + faceData.right * previewScaleHorizontal;
                faceRect.top = (viewHeight - detectHeight * previewScaleVertical) / 2f + faceData.top * previewScaleVertical;
                faceRect.bottom = (viewHeight - detectHeight * previewScaleVertical) / 2f + faceData.bottom * previewScaleVertical;
                float width = faceRect.width() / 10f;
                float height = faceRect.height() / 10f;
                canvas.drawLine(faceRect.left, faceRect.top, faceRect.left + width, faceRect.top, paint);
                canvas.drawLine(faceRect.right - width, faceRect.top, faceRect.right, faceRect.top, paint);
                canvas.drawLine(faceRect.left, faceRect.bottom, faceRect.left + width, faceRect.bottom, paint);
                canvas.drawLine(faceRect.right - width, faceRect.bottom, faceRect.right, faceRect.bottom, paint);
                canvas.drawLine(faceRect.left, faceRect.top, faceRect.left, faceRect.top + height, paint);
                canvas.drawLine(faceRect.left, faceRect.bottom - height, faceRect.left, faceRect.bottom, paint);
                canvas.drawLine(faceRect.right, faceRect.top, faceRect.right, faceRect.top + height, paint);
                canvas.drawLine(faceRect.right, faceRect.bottom - height, faceRect.right, faceRect.bottom, paint);
            }
        }
    }
}
