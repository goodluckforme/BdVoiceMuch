
package com.xiaomakj.bdvoice.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.xiaomakj.bdvoice.recognition.BaiduASRDialogTheme;


public class SDKProgressBar extends View {

    private static final int[] COLOR_BLUE_LIGHTBG = {
            0xFFB7E3FE, 0xFF96D5FD, 0xFF83CFFD, 0xFF76C9FD, 0xFF65C1FC, 0xFF61BDFB, 0xFF5CB5FA,
            0xFF57ADF9, 0xFF52A6F8, 0xFF519FF6, 0xFF4E93F2, 0xFF4C8DF2, 0xFF4A89F1
    };


    private static final int[] COLOR_BLUE_DEEPBG = {
            0xFF123362, 0xFF114575, 0xFF104f7f, 0xFF0f5a8b, 0xFF0e689a, 0xFF0e74a6, 0xFF0d80b2,
            0xFF0c8cbf, 0xFF0b95c8, 0xFF0ba1d4, 0xFF0aace0, 0xFF09b8ed, 0xFF09b8ed
    };

    /**
     * 颜色数组
     */
    private int[] colors;

    /**
     * 进度条起点位置
     */
    private int barX;

    private int barY;

    /**
     * 方块高度
     */
    private int rectHeight;

    /**
     * 方块宽度
     */
    private int rectWidth;

    /**
     * 进度条总长度
     */
    private int barLength;

    /**
     * 进度条不同区间
     */
    private int mProgress;

    private Paint paint;
    /**
     * 要为画笔设置的过滤器，用于旋转view的色相
     */
    private ColorFilter mHsvFilter;

    public SDKProgressBar(Context context) {
        super(context);
        paint = new Paint();
        initView();
    }

    public SDKProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        initView();
    }

    /**
     * 初始化进度条刻度
     */
    private void initView() {
        barX = getLeft();
        barY = getTop();
    }

    /**
     * 设置进度条显示长度
     *
     * @param progress
     */
    public void setProgress(int progress) {
        if (progress < 0) {
            progress = 0;
        }
        if (progress > 80) {
            progress = 80;
        }
        mProgress = progress;

        invalidate();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        barLength = MeasureSpec.getSize(widthMeasureSpec);

        rectHeight = barLength / 80;
        rectWidth = rectHeight;
        setMeasuredDimension(barLength, rectHeight);
    }

    public void setHsvFilter(ColorFilter filter) {
        mHsvFilter = filter;
    }

    /**
     * 根据
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 设置该属性后，用此画笔draw过的内容将按照参数意图渲染，比如调高亮度、饱和度、色相
        paint.setColorFilter(mHsvFilter);
        for (int i = 0; i <= mProgress; i++) {

            if (mProgress <= 12) {
                paint.setColor(colors[12 - (mProgress - i)]);
            } else {
                if (i <= mProgress - 12) {
                    paint.setColor(colors[0]);
                } else {
                    paint.setColor(colors[12 - (mProgress - i)]);
                }
            }
            canvas.drawRect(barX + rectWidth * i + i, barY, barX + rectWidth * i + i + rectWidth,
                    barY + rectHeight, paint);

        }

    }

    /**
     * 设置
     *
     * @param theme
     */
    public void setTheme(int theme) {
        final boolean isDeepStyle = BaiduASRDialogTheme.isDeepStyle(theme);
        colors = isDeepStyle ? COLOR_BLUE_DEEPBG : COLOR_BLUE_LIGHTBG;
    }
}
