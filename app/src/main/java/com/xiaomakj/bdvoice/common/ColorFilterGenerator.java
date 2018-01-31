package com.xiaomakj.bdvoice.common;

import android.graphics.ColorMatrix;

/**
 * 通过指定一些颜色属性的值，调整一个颜色矩阵。可用于基于一套主题，在程序中动态生成多套不同色调的主题
 * <p>
 * Created by weilikai01 on 2014/12/3.
 *
 * @see http://groups.google.com/group/android-developers/browse_thread/thread/9e215c83c3819953
 * @see http://gskinner.com/blog/archives/2007/12/colormatrix_cla.html
 */
public class ColorFilterGenerator {

    private static float[] DELTA_INDEX = new float[]{
            0f, 0.01f, 0.02f, 0.04f, 0.05f, 0.06f, 0.07f, 0.08f, 0.1f, 0.11f,
            0.12f, 0.14f, 0.15f, 0.16f, 0.17f, 0.18f, 0.20f, 0.21f, 0.22f, 0.24f,
            0.25f, 0.27f, 0.28f, 0.30f, 0.32f, 0.34f, 0.36f, 0.38f, 0.40f, 0.42f,
            0.44f, 0.46f, 0.48f, 0.5f, 0.53f, 0.56f, 0.59f, 0.62f, 0.65f, 0.68f,
            0.71f, 0.74f, 0.77f, 0.80f, 0.83f, 0.86f, 0.89f, 0.92f, 0.95f, 0.98f,
            1.0f, 1.06f, 1.12f, 1.18f, 1.24f, 1.30f, 1.36f, 1.42f, 1.48f, 1.54f,
            1.60f, 1.66f, 1.72f, 1.78f, 1.84f, 1.90f, 1.96f, 2.0f, 2.12f, 2.25f,
            2.37f, 2.50f, 2.62f, 2.75f, 2.87f, 3.0f, 3.2f, 3.4f, 3.6f, 3.8f,
            4.0f, 4.3f, 4.7f, 4.9f, 5.0f, 5.5f, 6.0f, 6.5f, 6.8f, 7.0f,
            7.3f, 7.5f, 7.8f, 8.0f, 8.4f, 8.7f, 9.0f, 9.4f, 9.6f, 9.8f,
            10.0f
    };

    /**
     * 调整颜色矩阵cm的属性
     *
     * @param cm         要调整的颜色矩阵
     * @param brightness 亮度，取值范围 [-100, 100]
     * @param contrast   对比度，取值范围 [-100, 100]
     * @param saturation 饱和度，取值范围 [-100, 100]
     * @param hue        色相，取值范围 [-180, 180]
     */
    public static void adjustColor(ColorMatrix cm, float brightness, float contrast, float saturation, float hue) {
        adjustHue(cm, hue);
        adjustContrast(cm, contrast);
        adjustBrightness(cm, brightness);
        adjustSaturation(cm, saturation);
    }

    /**
     * 调整颜色矩阵的色相属性
     *
     * @param cm    要调整的颜色矩阵
     * @param value [-180,180]
     */
    public static void adjustHue(ColorMatrix cm, float value) {
        value = cleanValue(value, 180f) / 180f * (float) Math.PI;
        if (value == 0) {
            return;
        }
        float cosVal = (float) Math.cos(value);
        float sinVal = (float) Math.sin(value);
        float lumR = 0.213f;
        float lumG = 0.715f;
        float lumB = 0.072f;
        float[] mat = new float[]{
                lumR + cosVal * (1 - lumR) + sinVal * (-lumR),
                lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
                lumR + cosVal * (-lumR) + sinVal * (0.143f),
                lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
                lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)),
                lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0,
                0f, 0f, 0f, 1f, 0f,
                0f, 0f, 0f, 0f, 1f
        };
        cm.postConcat(new ColorMatrix(mat));
    }

    /**
     * 调整颜色矩阵的亮度属性
     *
     * @param cm    要调整的颜色矩阵
     * @param value [-100,100]
     */
    public static void adjustBrightness(ColorMatrix cm, float value) {
        value = cleanValue(value, 100);
        if (value == 0 || Float.isNaN(value)) {
            return;
        }
        float[] mat = new float[]{
                1, 0, 0, 0, value,
                0, 1, 0, 0, value,
                0, 0, 1, 0, value,
                0, 0, 0, 1, 0,
                0, 0, 0, 0, 1
        };
        cm.postConcat(new ColorMatrix(mat));
    }

    /**
     * 调整颜色矩阵的色相属性
     *
     * @param cm    要调整的颜色矩阵
     * @param value [-100,100]
     */
    public static void adjustContrast(ColorMatrix cm, float value) {
        value = cleanValue(value, 100);
        if (value == 0 || Float.isNaN(value)) {
            return;
        }
        float x = 0;
        if (value < 0) {
            x = 127 + value / 100 * 127;
        } else {
            x = value % 1;
            if (x == 0) {
                x = DELTA_INDEX[((int) value)];
            } else {
                // x = DELTA_INDEX[(p_val<<0)]; // this is how the IDE does it.
                x = DELTA_INDEX[(((int) value) << 0)] * (1 - x) + DELTA_INDEX[(((int) value) << 0) + 1] * x;
                // use linear interpolation for more granularity.
            }
            x = x * 127 + 127;
        }
        float[] mat = new float[]{
                x / 127, 0, 0, 0, 0.5f * (127 - x),
                0, x / 127, 0, 0, 0.5f * (127 - x),
                0, 0, x / 127, 0, 0.5f * (127 - x),
                0, 0, 0, 1, 0,
                0, 0, 0, 0, 1
        };
        cm.postConcat(new ColorMatrix(mat));
    }

    /**
     * 调整颜色矩阵的饱和度属性
     *
     * @param cm    要调整的颜色矩阵
     * @param value [-100,100]
     */
    public static void adjustSaturation(ColorMatrix cm, float value) {
        value = cleanValue(value, 100);
        if (value == 0 || Float.isNaN(value)) {
            return;
        }
        float x = 1 + ((value > 0) ? 3 * value / 100 : value / 100);
        float lumR = 0.3086f;
        float lumG = 0.6094f;
        float lumB = 0.0820f;

        float[] mat = new float[]{
                lumR * (1 - x) + x, lumG * (1 - x), lumB * (1 - x), 0, 0,
                lumR * (1 - x), lumG * (1 - x) + x, lumB * (1 - x), 0, 0,
                lumR * (1 - x), lumG * (1 - x), lumB * (1 - x) + x, 0, 0,
                0, 0, 0, 1, 0,
                0, 0, 0, 0, 1
        };
        cm.postConcat(new ColorMatrix(mat));
    }

    protected static float cleanValue(float pVval, float pLimit) {
        return Math.min(pLimit, Math.max(-pLimit, pVval));
    }
}
