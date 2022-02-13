package createbest.sdk.bihu.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.dk.uartnfc.R;


/**
 * 遮罩层绘制器
 */
public interface MaskDrawer {

    /**
     * 绘制界面预览图像({@link CameraView})上的遮罩层
     *
     * @param viewWidth              界面预览组件（{@link CameraView}）的宽
     * @param viewHeight             界面预览组件（{@link CameraView}）的高
     * @param previewScaleHorizontal 图像像素转换成界面显示像素的缩放比例（水平方向）
     * @param previewScaleVertical   图像像素转换成界面显示像素的缩放比例（垂直方向）
     * @param detectWidth            检测区域的宽
     * @param detectHeight           检测区域的高
     * @param canvas                 画布
     */
    void drawMask(int viewWidth, int viewHeight, float previewScaleHorizontal, float previewScaleVertical, int detectWidth, int detectHeight, Canvas canvas);

    /**
     * 矩形框遮罩绘制器
     * 在遮罩层上绘制一个矩形框
     */
    class RectMaskDrawer implements MaskDrawer {
        private Paint maskPaint = new Paint();
        private float radius = 12;
        private int maskColor = Color.parseColor("#9992EBFF");
        private float strockWidth = 6;

        public RectMaskDrawer setRadius(float radius) {
            this.radius = radius;
            return this;
        }

        public RectMaskDrawer setStrockWidth(float strockWidth) {
            this.strockWidth = strockWidth;
            return this;
        }

        public RectMaskDrawer setMaskColor(int maskColor) {
            this.maskColor = maskColor;
            return this;
        }

        @Override
        public void drawMask(int viewWidth, int viewHeight, float previewScaleHorizontal, float previewScaleVertical, int detectWidth, int detectHeight, Canvas canvas) {
            RectF checkRect = new RectF();
            //减3是因为界面显示有点偏,下同
            checkRect.left = (viewWidth - detectWidth * previewScaleHorizontal) / 2 - strockWidth / 2 - 3;
            checkRect.right = (viewWidth + detectWidth * previewScaleHorizontal) / 2 + strockWidth / 2 - 3;
            checkRect.top = (viewHeight - detectHeight * previewScaleVertical) / 2 - strockWidth / 2 - 3;
            checkRect.bottom = (viewHeight + detectHeight * previewScaleVertical) / 2 + strockWidth / 2 - 3;

            maskPaint.setAntiAlias(true);
            maskPaint.setStyle(Paint.Style.STROKE);
            maskPaint.setStrokeWidth(strockWidth);
            maskPaint.setColor(maskColor);
            canvas.drawRoundRect(checkRect, radius, radius, maskPaint);
        }
    }

    /**
     * 空心圆遮罩绘制器
     * 在遮罩层上绘制一个空心圆遮罩涂层
     */
    class RoundMaskDrawer implements MaskDrawer {
        private Paint maskPaint = new Paint();
        private int maskColor = Color.parseColor("#bb000000");

        public RoundMaskDrawer setMaskColor(int maskColor) {
            this.maskColor = maskColor;
            return this;
        }

        @Override
        public void drawMask(int viewWidth, int viewHeight, float previewScaleHorizontal, float previewScaleVertical, int detectWidth, int detectHeight, Canvas canvas) {
            RectF checkRect = new RectF();
            //减3是因为界面显示有点偏,下同
            checkRect.left = (viewWidth - detectWidth * previewScaleHorizontal) / 2 - 3;
            checkRect.right = (viewWidth + detectWidth * previewScaleHorizontal) / 2 - 3;
            checkRect.top = (viewHeight - detectHeight * previewScaleVertical) / 2 - 3;
            checkRect.bottom = (viewHeight + detectHeight * previewScaleVertical) / 2 - 3;

            maskPaint.setStyle(Paint.Style.FILL);
            maskPaint.setAntiAlias(true);
            maskPaint.setColor(maskColor);

            canvas.drawRect(0, 0, viewWidth, viewHeight, maskPaint);
            maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawOval(checkRect, maskPaint);
            maskPaint.setXfermode(null);
        }
    }

    /**
     * Drawable遮罩绘制器
     * 在遮罩层上绘制一个空心圆遮罩涂层
     */
    class DrawableMaskDrawer implements MaskDrawer {
        private Paint maskPaint = new Paint();
        private Drawable drawable;
        private float enlargeX = 0;
        private float enlargeY = 0;

        public DrawableMaskDrawer(Drawable drawable) {
            this.drawable = drawable;
        }

        public DrawableMaskDrawer(Context context) {
            this.drawable = context.getResources().getDrawable(R.drawable.mask);
        }

        public DrawableMaskDrawer setEnlarge(float enlargeX, float enlargeY) {
            this.enlargeX = enlargeX;
            this.enlargeY = enlargeY;
            return this;
        }

        @Override
        public void drawMask(int viewWidth, int viewHeight, float previewScaleHorizontal, float previewScaleVertical, int detectWidth, int detectHeight, Canvas canvas) {
            RectF checkRect = new RectF();
            //减3是因为界面显示有点偏,下同
            checkRect.left = (viewWidth - detectWidth * previewScaleHorizontal) / 2 - enlargeX - 3;
            checkRect.right = (viewWidth + detectWidth * previewScaleHorizontal) / 2 + enlargeX - 3;
            checkRect.top = (viewHeight - detectHeight * previewScaleVertical) / 2 - enlargeY - 3;
            checkRect.bottom = (viewHeight + detectHeight * previewScaleVertical) / 2 + enlargeY - 3;

            maskPaint.setAntiAlias(true);
            Bitmap bitmap = Bitmap.createBitmap(detectWidth, detectHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas1 = new Canvas(bitmap);
            drawable.setBounds(0, 0, detectWidth, detectHeight);
            drawable.draw(canvas1);
            Rect bitmapRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, bitmapRect, checkRect, maskPaint);
        }
    }
}
