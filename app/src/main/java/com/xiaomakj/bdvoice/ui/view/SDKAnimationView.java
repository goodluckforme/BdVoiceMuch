
package com.xiaomakj.bdvoice.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.xiaomakj.bdvoice.recognition.BaiduASRDialogTheme;

/**
 * 音量反馈动画类，动画分为三个步骤(preparing, recording, recognizing)
 *
 * @author liuxi04
 */
public class SDKAnimationView extends View {
    public static final int SAMPE_RATE_VOLUME = 50;

    public static final int NO_ANIMATION_STATE = 0;

    public static final int INITIALIZING_ANIMATION_STATE = 4;

    public static final int PREPARING_ANIMATION_STATE = 1;
    public static final int RECORDING_ANIMATION_STATE = 2;
    public static final int RECOGNIZING_ANIMATION_STATE = 3;

    private static final int PREPARING_BAIDU_LOGO_TIME = 1200;
    private static final int RECOGNIZING_WAVE_TRANSLATION_TIME = 20;

    // 控件行列分格数
    private static final int RECT_IN_ROW = 69;
    private static final int RECT_IN_COLUMN = 21;

    private static final int RECOGNIZING_SCANLINE_SHADOW_NUMBER = 5;

    // 使音量下降效果变平和
    private static final int BAR_DROPOFF_STEP = 1;

    // 百度logo阵列
    private static final int[] BAIDU_LOGO = {
            0x00003800, 0x00007C00, 0x00007CF8, 0x000039FC,
            0x0003839C, 0x0007C76C, 0x0007CF6C, 0x00039C08, 0x00001FF8, 0x00039F18, 0x0007CFEC,
            0x0007C7EC, 0x0003830C, 0x000039FC, 0x00007CF8, 0x00007C00, 0x00003800
    };
    private static final int BEGIN_LOC_X = 27;

    /* INITIALIZING状态数据 */
    private static byte[] INIT_VOLUME_ARRAY = new byte[RECT_IN_ROW];

    /* PREPARING状态数据 */
    private static byte[] PREPARING_VOLUME_ARRAY = new byte[]{
            11, 11, 11, 11, 11, 11, 11, 11, 11,
            11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
            11,
            11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
            11,
            11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11
    };

    /* RECORDING状态数据 */
    /* 第一组值 */
    private static final byte[] GROUP1_VOLUME_ARRAY1 = new byte[]{
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3
    };
    private static final byte[] GROUP1_VOLUME_ARRAY2 = new byte[]{
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 5, 5, 5, 6,
            6, 6, 6, 7, 7, 7, 7, 7, 6, 6, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 4, 4, 4,
            4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3
    };
    private static final byte[] GROUP1_VOLUME_ARRAY3 = new byte[]{
            4, 4, 4, 4, 5, 5, 5, 6, 6, 6, 5, 5, 5, 5, 6, 6, 6, 6, 5, 5, 4, 4, 4, 5, 5, 5, 6, 6, 7,
            8, 8, 8, 9, 9, 9, 9, 9, 8, 8, 8, 7, 6, 6, 5, 5, 5, 4, 4, 4, 5, 5, 6, 6, 6, 6, 5, 5, 5,
            5, 6, 6, 6, 5, 5, 5, 4, 4, 4, 4
    };
    private static final byte[] GROUP1_VOLUME_ARRAY4 = new byte[]{
            7, 8, 8, 7, 7, 7, 8, 8, 9, 9, 9, 8, 8, 7, 6, 6, 5, 5, 5, 5, 6, 6, 6, 7, 7, 8, 8, 9, 10,
            10, 11, 11, 11, 12, 12, 12, 11, 11, 11, 10, 10, 9, 8, 8, 7, 7, 6, 6, 6, 5, 5, 5, 5, 6,
            6, 7, 8, 8, 9, 9, 9, 8, 8, 7, 7, 7, 8, 8, 7
    };
    private static final byte[] GROUP1_VOLUME_ARRAY5 = new byte[]{
            9, 9, 9, 10, 10, 11, 11, 12, 12, 12, 12, 11, 11, 10, 10, 9, 9, 9, 8, 8, 8, 8, 9, 9, 9,
            10, 10, 11, 12, 12, 13, 13, 14, 14, 14, 14, 14, 13, 13, 12, 12, 11, 10, 10, 9, 9, 9, 8,
            8, 8, 8, 9, 9, 9, 10, 10, 11, 11, 12, 12, 12, 12, 11, 11, 10, 10, 9, 9, 9
    };
    private static final byte[] GROUP1_VOLUME_ARRAY6 = new byte[]{
            11, 11, 11, 12, 12, 13, 13, 14, 14, 14, 15, 15, 15, 14, 14, 14, 13, 13, 13, 12, 12, 12,
            12, 12, 13, 13, 13, 14, 14, 15, 15, 15, 16, 16, 16, 16, 16, 15, 15, 15, 14, 14, 13, 13,
            13, 12, 12, 12, 12, 12, 13, 13, 13, 14, 14, 14, 15, 15, 15, 14, 14, 14, 13, 13, 12, 12,
            11, 11, 11
    };
    private static final byte[] GROUP1_VOLUME_ARRAY7 = new byte[]{
            13, 13, 14, 14, 15, 15, 16, 16, 16, 17, 17, 17, 16, 16, 16, 15, 15, 15, 14, 14, 14, 14,
            15, 15, 15, 16, 16, 17, 17, 18, 18, 18, 19, 19, 19, 19, 19, 18, 18, 18, 17, 17, 16, 16,
            15, 15, 15, 14, 14, 14, 14, 15, 15, 15, 16, 16, 16, 17, 17, 17, 16, 16, 16, 15, 15, 14,
            14, 13, 13
    };
    /* 第二组值 */
    private static final byte[] GROUP2_VOLUME_ARRAY1 = new byte[]{
            3, 3, 3, 3, 4, 4, 4, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5, 4, 4, 4, 4, 3, 3, 3, 3, 3,
            3, 3, 3, 3, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5, 4, 4, 4, 4,
            3, 3, 3, 3, 4, 4, 4, 3, 3, 3, 3
    };
    private static final byte[] GROUP2_VOLUME_ARRAY2 = new byte[]{
            3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 5, 5, 5, 4,
            4, 4, 4, 3, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 6, 6, 6,
            6, 5, 5, 5, 4, 4, 4, 4, 3, 3, 3
    };
    private static final byte[] GROUP2_VOLUME_ARRAY3 = new byte[]{
            5, 5, 4, 4, 4, 5, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 9, 9, 9, 8, 8, 7, 7, 6, 6, 5, 5, 5, 4,
            4, 4, 5, 5, 6, 6, 6, 5, 5, 4, 4, 4, 5, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 9, 9, 9, 8, 8, 7,
            7, 6, 6, 5, 5, 5, 4, 4, 4, 3, 3
    };
    private static final byte[] GROUP2_VOLUME_ARRAY4 = new byte[]{
            5, 5, 5, 6, 6, 6, 7, 7, 8, 8, 9, 10, 10, 11, 11, 11, 12, 12, 12, 11, 11, 11, 10, 10, 9,
            8, 8, 7, 7, 6, 6, 6, 5, 5, 5, 5, 5, 6, 6, 6, 7, 7, 8, 8, 9, 10, 10, 11, 11, 11, 12, 12,
            12, 11, 11, 11, 10, 10, 9, 8, 8, 7, 7, 6, 6, 6, 5, 5, 5
    };
    private static final byte[] GROUP2_VOLUME_ARRAY5 = new byte[]{
            9, 9, 8, 8, 8, 8, 9, 9, 9, 10, 10, 11, 12, 12, 13, 13, 14, 14, 14, 14, 14, 13, 13, 12,
            12, 11, 11, 10, 10, 9, 9, 9, 8, 8, 8, 8, 8, 9, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13,
            14, 14, 14, 14, 14, 13, 13, 12, 12, 11, 10, 10, 9, 9, 9, 8, 8, 8, 8, 9, 9
    };
    private static final byte[] GROUP2_VOLUME_ARRAY6 = new byte[]{
            13, 13, 13, 13, 14, 14, 14, 13, 13, 13, 13, 13, 14, 14, 15, 15, 15, 16, 16, 16, 16, 16,
            15, 15, 15, 14, 14, 14, 13, 13, 13, 13, 12, 12, 12, 12, 12, 13, 13, 13, 13, 14, 14, 14,
            15, 15, 15, 16, 16, 16, 16, 16, 15, 15, 15, 14, 14, 13, 13, 13, 13, 13, 14, 14, 14, 13,
            13, 13, 13
    };
    private static final byte[] GROUP2_VOLUME_ARRAY7 = new byte[]{
            15, 15, 14, 14, 14, 14, 15, 15, 15, 16, 16, 17, 17, 18, 18, 18, 19, 19, 19, 19, 19, 18,
            18, 18, 17, 17, 16, 16, 15, 15, 15, 14, 14, 14, 14, 14, 14, 14, 15, 15, 15, 16, 16, 17,
            17, 18, 18, 18, 19, 19, 19, 19, 19, 18, 18, 18, 17, 17, 16, 16, 15, 15, 15, 14, 14, 14,
            14, 15, 15
    };
    /* 第三组值 */
    private static final byte[] GROUP3_VOLUME_ARRAY1 = new byte[]{
            3, 3, 3, 4, 4, 4, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 4, 4, 4, 4, 3, 3, 3, 3, 4,
            4, 4, 4, 5, 5, 5, 5, 5, 4, 4, 4, 4, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 4, 4, 4, 4, 3,
            3, 3, 3, 3, 3, 4, 4, 4, 3, 3, 3
    };
    private static final byte[] GROUP3_VOLUME_ARRAY2 = new byte[]{
            3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, 5, 5, 5, 4, 4, 4, 3, 3, 3, 3, 4, 4, 4, 5, 5,
            6, 6, 6, 7, 7, 7, 7, 7, 6, 6, 6, 5, 5, 4, 4, 4, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6,
            6, 5, 5, 5, 4, 4, 4, 3, 3, 3, 3
    };
    private static final byte[] GROUP3_VOLUME_ARRAY3 = new byte[]{
            5, 5, 4, 4, 4, 5, 5, 5, 6, 6, 7, 7, 8, 8, 8, 9, 9, 9, 8, 8, 8, 7, 7, 7, 6, 6, 6, 7, 7,
            7, 8, 8, 9, 9, 9, 9, 9, 8, 8, 7, 7, 7, 6, 6, 6, 7, 7, 7, 8, 8, 8, 9, 9, 9, 8, 8, 8, 7,
            7, 6, 6, 5, 5, 5, 4, 4, 4, 5, 5
    };
    private static final byte[] GROUP3_VOLUME_ARRAY4 = new byte[]{
            6, 6, 6, 7, 7, 8, 9, 9, 10, 10, 10, 11, 11, 11, 10, 10, 10, 9, 9, 8, 8, 7, 7, 7, 7, 8,
            8, 9, 10, 10, 11, 11, 11, 12, 12, 12, 11, 11, 11, 10, 10, 9, 8, 8, 7, 7, 7, 7, 8, 8, 9,
            9, 10, 10, 10, 11, 11, 11, 10, 10, 10, 9, 9, 8, 7, 7, 6, 6, 6
    };
    private static final byte[] GROUP3_VOLUME_ARRAY5 = new byte[]{
            8, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13, 13, 13, 12, 12, 11, 11, 10, 10, 9, 9, 9,
            9, 10, 10, 11, 12, 12, 13, 13, 14, 14, 14, 14, 14, 13, 13, 12, 12, 11, 10, 10, 9, 9, 9,
            9, 10, 10, 11, 11, 12, 12, 13, 13, 13, 13, 12, 12, 11, 11, 10, 10, 9, 9, 8, 8, 8
    };
    private static final byte[] GROUP3_VOLUME_ARRAY6 = new byte[]{
            11, 11, 11, 11, 11, 12, 12, 12, 13, 13, 13, 14, 14, 14, 15, 15, 15, 15, 15, 14, 14, 14,
            13, 13, 13, 13, 14, 14, 14, 15, 15, 15, 16, 16, 16, 16, 16, 15, 15, 15, 14, 14, 14, 13,
            13, 13, 13, 14, 14, 14, 15, 15, 15, 15, 15, 14, 14, 14, 13, 13, 13, 12, 12, 12, 11, 11,
            11, 11, 11
    };
    private static final byte[] GROUP3_VOLUME_ARRAY7 = new byte[]{
            14, 14, 14, 15, 15, 16, 16, 17, 17, 18, 18, 18, 18, 17, 17, 16, 16, 15, 15, 14, 14, 14,
            15, 15, 15, 16, 16, 17, 17, 18, 18, 18, 19, 19, 19, 19, 19, 18, 18, 18, 17, 17, 16, 16,
            15, 15, 15, 14, 14, 14, 15, 15, 16, 16, 17, 17, 18, 18, 18, 18, 17, 17, 16, 16, 15, 15,
            14, 14, 14
    };

    private static byte[][] VOLUMES_GROUP1 = new byte[][]{
            GROUP1_VOLUME_ARRAY1, GROUP1_VOLUME_ARRAY2,
            GROUP1_VOLUME_ARRAY3, GROUP1_VOLUME_ARRAY4, GROUP1_VOLUME_ARRAY5, GROUP1_VOLUME_ARRAY6,
            GROUP1_VOLUME_ARRAY7
    };

    private static byte[][] VOLUMES_GROUP2 = new byte[][]{
            GROUP2_VOLUME_ARRAY1, GROUP2_VOLUME_ARRAY2,
            GROUP2_VOLUME_ARRAY3, GROUP2_VOLUME_ARRAY4, GROUP2_VOLUME_ARRAY5, GROUP2_VOLUME_ARRAY6,
            GROUP2_VOLUME_ARRAY7
    };

    private static byte[][] VOLUMES_GROUP3 = new byte[][]{
            GROUP3_VOLUME_ARRAY1, GROUP3_VOLUME_ARRAY2,
            GROUP3_VOLUME_ARRAY3, GROUP3_VOLUME_ARRAY4, GROUP3_VOLUME_ARRAY5, GROUP3_VOLUME_ARRAY6,
            GROUP3_VOLUME_ARRAY7
    };

    private byte[][] volumes;


    int mBgColor = 0;

    // 当前音量数据和目标音量数据，中间会进行插值动画
    private byte[] currentVolumeArray = new byte[RECT_IN_ROW];
    private byte[] targetVolumeArray = new byte[RECT_IN_ROW];

    private double sampleSideLength = 0;

    private int mWidth = 0;

    // 动画当前所处状态
    private int mAnimationState = -1;

    // preparing状态下记录开始时间
    private long mPreparingBeginTime;
    // recording状态下每帧动画间插值计算使用的时间
    private long mRecordingInterpolationTime;
    // recording状态下每帧动画间插值计算当前的时间
    private long mRecordingCurrentTime;
    // recognizing状态下记录开始时间
    private long mRecognizingBeginTime;

    // recognizing下波动位置记录
    private int mRecognizingWaveIndex;
    // recognizing下记录上行扫描和下行扫描状态
    private int mRecognizingRefreshCount;

    private Paint mGriddingPaint;
    private Paint mBaiduLogePaint;
    private Paint mVolumnCeilingPaint;
    private Paint mVolumnShadowPaint;
    private Paint mLogoReversePaint;

    private Drawable mMask;

    private int mCurrentBar;

    private int mVolumeCeilingColor1;
    private int mVolumeCeilingColor2;

    private int mVolumeShadowColor1;
    private int mVolumeShadowColor2;

    private int mRecognizingLineShadowColor1;
    private int mRecognizingLineShadowColor2;

    /**
     * 旋转view色相的画笔
     */
    private Paint mHsvFilterPaint;
    /**
     * 旋转view色相的缓存图片
     */
    private Bitmap mHsvFilterBitmap;
    /**
     * 旋转view色相的缓存画布
     */
    private Canvas mHsvFilterCanvas;
    /**
     * 要为画笔设置的过滤器，用于旋转view的色相
     */
    private ColorFilter mHsvFilter;
    private float mCurrentDBLevelMeter;

    public SDKAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mGriddingPaint = new Paint();
        mGriddingPaint.setStrokeWidth(1);
        mBaiduLogePaint = new Paint();
        mLogoReversePaint = new Paint();
        mVolumnCeilingPaint = new Paint();
        mVolumnShadowPaint = new Paint();

        volumes = VOLUMES_GROUP1;
        currentVolumeArray = volumes[0];
        targetVolumeArray = volumes[0];

        setThemeStyle(BaiduASRDialogTheme.THEME_BLUE_LIGHTBG);
    }

    public void setThemeStyle(int themeStyle) {
        final boolean isDeepStyle = BaiduASRDialogTheme.isDeepStyle(themeStyle);

        mBgColor = isDeepStyle ? 0xFF1D1D1D : 0xFFF6F6F6;

        mGriddingPaint.setColor(isDeepStyle ? 0xFF1D1D1D : 0xFFFFFFFF);
        mBaiduLogePaint.setColor(isDeepStyle ? 0xFF2F2F2F : 0xFFECECEC);
        mLogoReversePaint.setColor(isDeepStyle ? 0xFF043B8A : 0xFFDAF0FF);

        mVolumeCeilingColor1 = isDeepStyle ? 0xFF4582ED : 0xFF85B0F9;
        mVolumeCeilingColor2 = isDeepStyle ? 0x004582ED : 0xFF9ADBF2;

        mVolumeShadowColor1 = isDeepStyle ? 0xFF053D8A : 0xFFCEEBFF;
        mVolumeShadowColor2 = isDeepStyle ? 0x00053D8A : 0xFFFFFFFF;

        mRecognizingLineShadowColor1 = isDeepStyle ? 0xFF053D8A : 0xFFCDEAFF;
        mRecognizingLineShadowColor2 = isDeepStyle ? 0x00053D8A : 0x00CDEAFF;

        mMask = getResources().getDrawable(isDeepStyle ? getContext().getResources()
                .getIdentifier("bdspeech_mask_deep", "drawable", getContext().getPackageName()) :
                getContext().getResources()
                        .getIdentifier("bdspeech_mask_light", "drawable", getContext().getPackageName()));
    }

    /**
     * 开始Initializing状态动画
     */
    public void startInitializingAnimation() {
        mAnimationState = INITIALIZING_ANIMATION_STATE;
        mPreparingBeginTime = System.currentTimeMillis();

        removeCallbacks(mInvalidateTask);
        removeCallbacks(mRecordingUpdateTask);
        post(mInvalidateTask);
    }

    /**
     * 开始Preparing状态动画
     */
    public void startPreparingAnimation() {
        mAnimationState = PREPARING_ANIMATION_STATE;
        mPreparingBeginTime = System.currentTimeMillis();

        removeCallbacks(mInvalidateTask);
        removeCallbacks(mRecordingUpdateTask);
        post(mInvalidateTask);
    }

    /**
     * 开始Recording状态动画
     */
    public void startRecordingAnimation() {
        mAnimationState = RECORDING_ANIMATION_STATE;

        removeCallbacks(mInvalidateTask);
        removeCallbacks(mRecordingUpdateTask);

        // 绘制UI
        post(mInvalidateTask);

        // 更新音量数据
        post(mRecordingUpdateTask);
    }

    /**
     * 开始Recognizing状态动画
     */
    public void startRecognizingAnimation() {
        mAnimationState = RECOGNIZING_ANIMATION_STATE;
        mRecognizingBeginTime = System.currentTimeMillis();

        mRecognizingWaveIndex = 0;
        mRecognizingRefreshCount = 0;

        removeCallbacks(mInvalidateTask);
        removeCallbacks(mRecordingUpdateTask);
        post(mInvalidateTask);
    }

    /**
     * 动画状态复位。
     */
    public void resetAnimation() {
        removeCallbacks(mInvalidateTask);
        removeCallbacks(mRecordingUpdateTask);
        mAnimationState = NO_ANIMATION_STATE;
    }

    /**
     * recording状态下设置音量大小等级
     *
     * @param level 音量等级
     */
    private void setVolumeLevel(int level) {
        if (volumes != null && level >= 0 && level < volumes.length) {
            currentVolumeArray = targetVolumeArray;
            mRecordingInterpolationTime = System.currentTimeMillis();

            // 三组音量值之间随机选择
            int randomGroup = (int) (2 * Math.random());
            switch (randomGroup) {
                case 0:
                    volumes = VOLUMES_GROUP1;
                    break;
                case 1:
                    volumes = VOLUMES_GROUP2;
                    break;
                case 2:
                    volumes = VOLUMES_GROUP3;
                    break;
                default:
                    volumes = VOLUMES_GROUP1;
                    break;
            }
            targetVolumeArray = volumes[level];
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 用控件宽度值计算高度值，从而保持采样格为方形。
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        sampleSideLength = (double) mWidth / (double) RECT_IN_ROW;
        setMeasuredDimension(mWidth, (int) (sampleSideLength * RECT_IN_COLUMN));
    }

    /**
     * 设置view颜色过滤器，可以用于调整view显示的色相、亮度、饱和度等
     *
     * @param filter 如果要旋转色相可通过ColorFilterGenerator.adjustHue(number) 生成
     */
    public void setHsvFilter(ColorFilter filter) {
        mHsvFilter = filter;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHsvFilterBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mHsvFilterCanvas = new Canvas(mHsvFilterBitmap);
        mHsvFilterPaint = new Paint();
        mHsvFilterPaint.setColorFilter(mHsvFilter);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        realOnDraw(mHsvFilterCanvas);
        canvas.drawBitmap(mHsvFilterBitmap, 0, 0, mHsvFilterPaint);
    }

    protected void realOnDraw(Canvas canvas) {
        // 绘制背景颜色
        canvas.drawColor(mBgColor);

        switch (mAnimationState) {
            case INITIALIZING_ANIMATION_STATE:
                currentVolumeArray = INIT_VOLUME_ARRAY;
                targetVolumeArray = INIT_VOLUME_ARRAY;
                long timeInterval = System.currentTimeMillis() - mPreparingBeginTime;
                int alpha = 0;
                if (timeInterval < PREPARING_BAIDU_LOGO_TIME) {
                    // logo出现阶段动画
                    int duration =
                            (int) ((System.currentTimeMillis() - mPreparingBeginTime) % PREPARING_BAIDU_LOGO_TIME);
                    alpha = (int) (((double) duration) / (PREPARING_BAIDU_LOGO_TIME) * 0xFF);
                } else {
                    // logo闪烁阶段动画
                    int duration =
                            (int) (timeInterval % PREPARING_BAIDU_LOGO_TIME);
                    if (duration < PREPARING_BAIDU_LOGO_TIME / 2) {
                        alpha = (int) ((1 - (double) duration / (PREPARING_BAIDU_LOGO_TIME / 2)
                                * 0.8f) * 0xFF);
                    } else {
                        alpha =
                                (int) ((1 - (double) (PREPARING_BAIDU_LOGO_TIME - duration)
                                        / (PREPARING_BAIDU_LOGO_TIME / 2) * 0.8f) * 0xFF);
                    }
                }
                drawBaiduLogo(canvas, alpha);
                break;
            case PREPARING_ANIMATION_STATE:
                currentVolumeArray = PREPARING_VOLUME_ARRAY;
                targetVolumeArray = PREPARING_VOLUME_ARRAY;

                timeInterval = System.currentTimeMillis() - mPreparingBeginTime;
                alpha = 0;
                if (timeInterval < PREPARING_BAIDU_LOGO_TIME) {
                    // logo出现阶段动画
                    int duration =
                            (int) ((System.currentTimeMillis() - mPreparingBeginTime) % PREPARING_BAIDU_LOGO_TIME);
                    alpha = (int) (((double) duration) / (PREPARING_BAIDU_LOGO_TIME) * 0xFF);
                } else {
                    // logo闪烁阶段动画
                    int duration =
                            (int) (timeInterval % PREPARING_BAIDU_LOGO_TIME);
                    if (duration < PREPARING_BAIDU_LOGO_TIME / 2) {
                        alpha = (int) ((1 - (double) duration / (PREPARING_BAIDU_LOGO_TIME / 2)
                                * 0.8f) * 0xFF);
                    } else {
                        alpha =
                                (int) ((1 - (double) (PREPARING_BAIDU_LOGO_TIME - duration)
                                        / (PREPARING_BAIDU_LOGO_TIME / 2) * 0.8f) * 0xFF);
                    }
                }

                drawVoiceVolumn(canvas, alpha);
                drawBaiduLogo(canvas, alpha);
                break;
            case RECORDING_ANIMATION_STATE:
                mRecordingCurrentTime = System.currentTimeMillis();
                drawVoiceVolumn(canvas, 0xFF);
                drawBaiduLogo(canvas, 0xFF);
                break;

            case RECOGNIZING_ANIMATION_STATE:
                if (System.currentTimeMillis() - mRecognizingBeginTime > RECOGNIZING_WAVE_TRANSLATION_TIME) {
                    mRecognizingBeginTime = System.currentTimeMillis();

                    if (mRecognizingRefreshCount == 0) {
                        mRecognizingWaveIndex++;
                        if (mRecognizingWaveIndex >= RECT_IN_COLUMN + 5) {
                            mRecognizingRefreshCount = 1;
                        }
                    } else {
                        mRecognizingWaveIndex--;
                        if (mRecognizingWaveIndex <= -5) {
                            mRecognizingRefreshCount = 0;
                        }
                    }
                }

                drawRecognizingLine(canvas);
                drawRecognizingBaiduLogo(canvas);
                break;
            default:
                break;
        }

        drawGridding(canvas);
        drawMask(canvas);
    }

    /**
     * 绘制Baidu Logo
     *
     * @param canvas
     */
    private void drawBaiduLogo(Canvas canvas, int alpha) {
        mBaiduLogePaint.setAlpha(alpha);
        mLogoReversePaint.setAlpha(alpha);
        for (int i = BEGIN_LOC_X; i < BEGIN_LOC_X + BAIDU_LOGO.length; i++) {
            for (int j = 0; j < RECT_IN_COLUMN; j++) {
                if (((BAIDU_LOGO[i - BEGIN_LOC_X] >> j) & 0x01) == 0x01) {
                    int volume = 0;
                    int intervalTime = (int) (mRecordingCurrentTime - mRecordingInterpolationTime);
                    if (intervalTime > SAMPE_RATE_VOLUME) {
                        intervalTime = SAMPE_RATE_VOLUME;
                    }

                    // 均匀插值计算过渡位置
                    volume =
                            (int) (currentVolumeArray[i - 1] + (targetVolumeArray[i - 1] - currentVolumeArray[i - 1])
                                    * (double) intervalTime / SAMPE_RATE_VOLUME);

                    if (j < volume) {
                        // baidu logo部分进行反色处理。
                        if (j < volume - 1) {
                            canvas.drawRect((int) (sampleSideLength * (i - 1)),
                                    (int) (sampleSideLength * ((RECT_IN_COLUMN - j) - 1)),
                                    (int) (sampleSideLength * i),
                                    (int) (sampleSideLength * (RECT_IN_COLUMN - j)),
                                    mLogoReversePaint);
                        }
                    } else {
                        canvas.drawRect((int) (sampleSideLength * (i - 1)),
                                (int) (sampleSideLength * ((RECT_IN_COLUMN - j) - 1)),
                                (int) (sampleSideLength * i),
                                (int) (sampleSideLength * (RECT_IN_COLUMN - j)),
                                mBaiduLogePaint);
                    }
                }
            }
        }
    }

    /**
     * 绘制分隔线
     *
     * @param canvas
     */
    private void drawGridding(Canvas canvas) {
        for (int col = 0; col <= RECT_IN_COLUMN; col++) {
            canvas.drawLine(0, (int) (sampleSideLength * col), mWidth,
                    (int) (sampleSideLength * col), mGriddingPaint);
        }

        for (int row = 0; row <= RECT_IN_ROW; row++) {
            canvas.drawLine((int) (sampleSideLength * row), 0, (int) (sampleSideLength * row),
                    getHeight(), mGriddingPaint);
        }
    }

    /**
     * 绘制渐变蒙层
     *
     * @param canvas
     */
    private void drawMask(Canvas canvas) {
        if (mMask != null) {
            mMask.setBounds(0, 0, mWidth, getHeight());
            mMask.draw(canvas);
        }
    }

    private void drawVoiceVolumn(Canvas canvas, int alpha) {
        LinearGradient gradient =
                new LinearGradient(0, 1, 0, getHeight() * 2 / 3, mVolumeShadowColor1, mVolumeShadowColor2,
                        Shader.TileMode.CLAMP);
        mVolumnShadowPaint.setShader(gradient);
        mVolumnShadowPaint.setAlpha(alpha);

        for (int i = 0; i < RECT_IN_ROW; i++) {
            int volume = 0;
            int intervalTime = (int) (mRecordingCurrentTime - mRecordingInterpolationTime);
            if (intervalTime > SAMPE_RATE_VOLUME) {
                intervalTime = SAMPE_RATE_VOLUME;
            }

            volume =
                    (int) (currentVolumeArray[i] + (targetVolumeArray[i] - currentVolumeArray[i])
                            * (double) intervalTime / SAMPE_RATE_VOLUME);

            canvas.save();
            canvas.translate((int) (sampleSideLength * i),
                    (int) (sampleSideLength * (RECT_IN_COLUMN - volume)));
            canvas.drawRect(0, 0, (int) (sampleSideLength), getHeight()
                    - (int) (sampleSideLength * (RECT_IN_COLUMN - volume)), mVolumnShadowPaint);
            canvas.restore();

            int a = (int) (((mVolumeCeilingColor1 >> 24) & 0xFF) + (((mVolumeCeilingColor2 >> 24) & 0xFF)
                    - ((mVolumeCeilingColor1 >> 24) & 0xFF))
                    * (Math.abs((i - RECT_IN_ROW / (double) 2)) / (RECT_IN_ROW / (double) 2)));

            int r = (int) (((mVolumeCeilingColor1 >> 16) & 0xFF) + (((mVolumeCeilingColor2 >> 16) & 0xFF)
                    - ((mVolumeCeilingColor1 >> 16) & 0xFF))
                    * (Math.abs((i - RECT_IN_ROW / (double) 2)) / (RECT_IN_ROW / (double) 2)));
            int g = (int) (((mVolumeCeilingColor1 >> 8) & 0xFF) + (((mVolumeCeilingColor2 >> 8) & 0xFF)
                    - ((mVolumeCeilingColor1 >> 8) & 0xFF))
                    * (Math.abs((i - RECT_IN_ROW / (double) 2)) / (RECT_IN_ROW / (double) 2)));
            int b = (int) ((mVolumeCeilingColor1 & 0xFF) + ((mVolumeCeilingColor2 & 0xFF)
                    - (mVolumeCeilingColor1 & 0xFF))
                    * (Math.abs((i - RECT_IN_ROW / (double) 2)) / (RECT_IN_ROW / (double) 2)));

            int color = (int) ((a << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
            mVolumnCeilingPaint.setColor(color);
            mVolumnCeilingPaint.setAlpha((int) ((double) alpha * a / 0xFF));
            canvas.drawRect((int) (sampleSideLength * i),
                    (int) (sampleSideLength * (RECT_IN_COLUMN - volume)),
                    (int) (sampleSideLength * (i + 1)),
                    (int) (sampleSideLength * (RECT_IN_COLUMN - volume + 1)), mVolumnCeilingPaint);
        }
    }

    /**
     * 识别状态绘制baidu logo
     *
     * @param canvas
     */
    private void drawRecognizingBaiduLogo(Canvas canvas) {
        mBaiduLogePaint.setAlpha(0xFF);
        mLogoReversePaint.setAlpha(0xFF);
        for (int i = BEGIN_LOC_X; i < BEGIN_LOC_X + BAIDU_LOGO.length; i++) {
            for (int j = 0; j < RECT_IN_COLUMN; j++) {
                if (((BAIDU_LOGO[i - BEGIN_LOC_X] >> j) & 0x01) == 0x01) {
                    int volume = mRecognizingWaveIndex;
                    if (j < volume) {
                        if (j < volume - 1) {
                            canvas.drawRect((int) (sampleSideLength * (i - 1)),
                                    (int) (sampleSideLength * ((RECT_IN_COLUMN - j) - 1)),
                                    (int) (sampleSideLength * i),
                                    (int) (sampleSideLength * (RECT_IN_COLUMN - j)),
                                    mLogoReversePaint);
                        }
                    } else {
                        canvas.drawRect((int) (sampleSideLength * (i - 1)),
                                (int) (sampleSideLength * ((RECT_IN_COLUMN - j) - 1)),
                                (int) (sampleSideLength * i),
                                (int) (sampleSideLength * (RECT_IN_COLUMN - j)),
                                mBaiduLogePaint);
                    }
                }
            }
        }
    }

    /**
     * 识别状态绘制扫描线
     *
     * @param canvas
     */
    private void drawRecognizingLine(Canvas canvas) {
        if (mRecognizingRefreshCount == 0) {
            LinearGradient gradient =
                    new LinearGradient(0, 1, 0,
                            (int) (RECOGNIZING_SCANLINE_SHADOW_NUMBER * sampleSideLength),
                            mRecognizingLineShadowColor1,
                            mRecognizingLineShadowColor2,
                            Shader.TileMode.MIRROR);
            mVolumnShadowPaint.setShader(gradient);

            canvas.save();
            canvas.translate(0,
                    (int) (sampleSideLength * (RECT_IN_COLUMN - (mRecognizingWaveIndex - 1))));
            canvas.drawRect(0, 0, mWidth, (int) (sampleSideLength * RECOGNIZING_SCANLINE_SHADOW_NUMBER),
                    mVolumnShadowPaint);
            canvas.restore();
        } else {
            LinearGradient gradient =
                    new LinearGradient(0, 1, 0,
                            (int) (RECOGNIZING_SCANLINE_SHADOW_NUMBER * sampleSideLength),
                            mRecognizingLineShadowColor2,
                            mRecognizingLineShadowColor1,
                            Shader.TileMode.MIRROR);
            mVolumnShadowPaint.setShader(gradient);

            canvas.save();
            canvas.translate(0, (int) (sampleSideLength
                    * (RECT_IN_COLUMN - (mRecognizingWaveIndex + RECOGNIZING_SCANLINE_SHADOW_NUMBER))));
            canvas.drawRect(0, 0, mWidth,
                    (int) (RECOGNIZING_SCANLINE_SHADOW_NUMBER * sampleSideLength),
                    mVolumnShadowPaint);
            canvas.restore();
        }

        for (int i = 0; i < RECT_IN_ROW; i++) {
            // 手工计算颜色渐变
            int alpha = (int) (((mVolumeCeilingColor1 >> 24) & 0xFF) + (((mVolumeCeilingColor2 >> 24) & 0xFF)
                    - ((mVolumeCeilingColor1 >> 24) & 0xFF))
                    * (Math.abs((i - RECT_IN_ROW / (double) 2)) / (RECT_IN_ROW / (double) 2)));
            int r = (int) (((mVolumeCeilingColor1 >> 16) & 0xFF) + (((mVolumeCeilingColor2 >> 16) & 0xFF)
                    - ((mVolumeCeilingColor1 >> 16) & 0xFF))
                    * (Math.abs((i - RECT_IN_ROW / (double) 2)) / (RECT_IN_ROW / (double) 2)));
            int g = (int) (((mVolumeCeilingColor1 >> 8) & 0xFF) + (((mVolumeCeilingColor2 >> 8) & 0xFF)
                    - ((mVolumeCeilingColor1 >> 8) & 0xFF))
                    * (Math.abs((i - RECT_IN_ROW / (double) 2)) / (RECT_IN_ROW / (double) 2)));
            int b = (int) ((mVolumeCeilingColor1 & 0xFF)
                    + ((mVolumeCeilingColor2 & 0xFF) - (mVolumeCeilingColor1 & 0xFF))
                    * (Math.abs((i - RECT_IN_ROW / (double) 2)) / (RECT_IN_ROW / (double) 2)));

            int color = (int) ((alpha << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
            mVolumnCeilingPaint.setColor(color);
            canvas.drawRect((int) (sampleSideLength * i),
                    (int) (sampleSideLength * (RECT_IN_COLUMN - mRecognizingWaveIndex)),
                    (int) (sampleSideLength * (i + 1)), (int) (sampleSideLength * (RECT_IN_COLUMN
                            - mRecognizingWaveIndex + 1)), mVolumnCeilingPaint);
        }
    }

    /**
     * 启动音量反馈动画
     *
     * @param state 动画状态
     */
    public void startVoiceAnimation(int state) {
        // TODO Auto-generated method stub
        switch (state) {
            case NO_ANIMATION_STATE:
                resetAnimation();
                break;
            case PREPARING_ANIMATION_STATE:
                startPreparingAnimation();
                break;
            case RECORDING_ANIMATION_STATE:
                startRecordingAnimation();
                break;
            case RECOGNIZING_ANIMATION_STATE:
                startRecognizingAnimation();
                break;
            case INITIALIZING_ANIMATION_STATE:
                startInitializingAnimation();
                break;
            default:
                resetAnimation();
                break;
        }
    }

    private Runnable mRecordingUpdateTask = new Runnable() {
        @Override
        public void run() {
            final int minBar = 0;
            final int maxBar = 6;
            int bar = minBar;
            bar += (int) (maxBar - minBar) * getCurrentDBLevelMeter() / 100;

            if (bar > mCurrentBar) {
                mCurrentBar = bar;
            } else {
                mCurrentBar = Math.max(bar, mCurrentBar - BAR_DROPOFF_STEP);
            }

            mCurrentBar = Math.min(maxBar, mCurrentBar);

            // 初始音量情况下增加波动效果
            if (mCurrentBar == 0 && (int) (Math.random() * 4) == 0) {
                mCurrentBar = 1;
            }
            setVolumeLevel(mCurrentBar);
            removeCallbacks(mRecordingUpdateTask);
            postDelayed(mRecordingUpdateTask, SAMPE_RATE_VOLUME);
        }
    };

    private float getCurrentDBLevelMeter() {
        return mCurrentDBLevelMeter;
    }

    public void setCurrentDBLevelMeter(float rmsDb) {
        mCurrentDBLevelMeter = rmsDb;
    }

    private Runnable mInvalidateTask = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            invalidate();
            post(this);
        }

    };
}
