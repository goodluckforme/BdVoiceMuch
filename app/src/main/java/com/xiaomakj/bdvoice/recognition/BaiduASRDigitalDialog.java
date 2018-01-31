
package com.xiaomakj.bdvoice.recognition;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaomakj.bdvoice.R;
import com.xiaomakj.bdvoice.common.ColorFilterGenerator;
import com.xiaomakj.bdvoice.ui.view.SDKAnimationView;
import com.xiaomakj.bdvoice.ui.view.SDKProgressBar;

import java.util.MissingResourceException;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * 语音识别对话框
 *
 * @author yangliang02
 */
@SuppressLint("Registered")
public class BaiduASRDigitalDialog extends BaiduASRDialog {
    private static final String TAG = "BSDigitalDialog";

    // 国际化标识定义Begin
    private static final String KEY_TIPS_ERROR_SILENT = "tips.error.silent";

    private static final String KEY_TIPS_ERROR_DECODER = "tips.error.decoder";

    private static final String KEY_TIPS_ERROR_SPEECH_TOO_SHORT = "tips.error.speech_too_short";

    private static final String KEY_TIPS_ERROR_SPEECH_TOO_LONG = "tips.error.speech_too_long";

    private static final String KEY_TIPS_ERROR_NETWORK = "tips.error.network";

    private static final String KEY_TIPS_ERROR_NETWORK_UNUSABLE = "tips.error.network_unusable";

    private static final String KEY_TIPS_ERROR_INTERNAL = "tips.error.internal";

    private static final String KEY_TIPS_STATE_READY = "tips.state.ready";

    private static final String KEY_TIPS_STATE_WAIT = "tips.state.wait";

    private static final String KEY_TIPS_STATE_INITIALIZING = "tips.state.initializing";

    private static final String KEY_TIPS_STATE_LISTENING = "tips.state.listening";

    private static final String KEY_TIPS_STATE_RECOGNIZING = "tips.state.recognizing";

    private static final String KEY_TIPS_COPYRIGHT = "tips.copyright";

    private static final String KEY_TIPS_WAITNET = "tips.wait.net";

    private static final String KEY_BTN_DONE = "btn.done";

    private static final String KEY_BTN_CANCEL = "btn.cancel";

    private static final String KEY_BTN_RETRY = "btn.retry";

    private static final String KEY_TIPS_HELP_TITLE = "tips.help.title";

    private static final String KEY_BTN_START = "btn.start";

    private static final String KEY_BTN_HELP = "btn.help";

    private static final String KEY_TIPS_PREFIX = "tips.suggestion.prefix";
    private static final int ERROR_NETWORK_UNUSABLE = 0x90000;

    private int mErrorCode;

    // 国际化标识定义end
    // TOTO 更新最终地址
    private static final String mUrl = "http://developer.baidu.com/static/community/servers/voice/sdk.html";

    private CharSequence mErrorRes = "";

    private View mContentRoot = null;

    private View mMainLayout;

    private View mErrorLayout;

    private TextView mTipsTextView;

    private TextView mWaitNetTextView;

    private TextView mCompleteTextView;

    private TextView mCancelTextView;

    private TextView mRetryTextView;

    private SDKAnimationView mVoiceWaveView;

    private TextView mErrorTipsTextView;

    private TextView mLogoText1;

    private TextView mLogoText2;

    private ImageButton mCancelBtn;

    private ImageButton mHelpBtn;

    private TextView mTitle;

    private View mHelpView;

    private TipsAdapter mTipsAdapter;

    /**
     * 动效下面的提示，3S不说话出现，文字在列表中随机出。出现后隐藏版权声明
     */
    private TextView mSuggestionTips;

    /**
     * 静音异常时的提示语
     */
    private TextView mSuggestionTips2;

    private View mRecognizingView;

    /**
     * 连续上屏控件
     */
    private EditText mInputEdit;

    /**
     * 识别中的进度条
     *
     * @author zhaopengfei04
     */
    private static final int BAR_ONEND = 0;

    private static final int BAR_ONFINISH = 1;

    private SDKProgressBar mSDKProgressBar;

    private int step = 0;

    // 3秒不出识别结果，显示网络不稳定,15秒转到重试界面
    private int delayTime = 0;

    // 当前活跃的引擎类型
    private volatile int mEngineType = 0;

    Message mMessage = Message.obtain();

    private Drawable mBg;

    /**
     * “说完了”按钮背景
     */
    private StateListDrawable mButtonBg = new StateListDrawable();

    /**
     * 左侧按钮背景
     */
    private StateListDrawable mLeftButtonBg = new StateListDrawable();

    /**
     * 右侧按钮背景
     */
    private StateListDrawable mRightButtonBg = new StateListDrawable();

    /**
     * 帮助按钮
     */
    private StateListDrawable mHelpButtonBg = new StateListDrawable();

    /**
     * 按钮文字颜色
     */
    private ColorStateList mButtonColor;

    /**
     * 按钮文字颜色反色
     */
    private ColorStateList mButtonReverseColor;

    /**
     * 底部版本声明字体颜色
     */
    private int mCopyRightColor = 0;

    /**
     * 状态提示字体 颜色
     */
    private int mStateTipsColor = 0;

    /**
     * 错误提示字体颜色
     */
    private int mErrorTipsColor = 0;

    private int mTheme = 0;

    // 识别启动后间隔多长时间不说话出现提示，单位毫秒
    private static final long SHOW_SUGGESTION_INTERVAL = 3000;

    public static final int THEME_BLUE_LIGHTBG = BaiduASRDialogTheme.THEME_BLUE_LIGHTBG;

    public static final int THEME_BLUE_DEEPBG = BaiduASRDialogTheme.THEME_BLUE_DEEPBG;

    public static final int THEME_RED_LIGHTBG = BaiduASRDialogTheme.THEME_RED_LIGHTBG;

    public static final int THEME_RED_DEEPBG = BaiduASRDialogTheme.THEME_RED_DEEPBG;

    public static final int THEME_GREEN_LIGHTBG = BaiduASRDialogTheme.THEME_GREEN_LIGHTBG;

    public static final int THEME_GREEN_DEEPBG = BaiduASRDialogTheme.THEME_GREEN_DEEPBG;

    public static final int THEME_ORANGE_LIGHTBG = BaiduASRDialogTheme.THEME_ORANGE_LIGHTBG;

    public static final int THEME_ORANGE_DEEPBG = BaiduASRDialogTheme.THEME_ORANGE_DEEPBG;

    protected static final int ERROR_NONE = 0;
    /**
     * 国际化文本资源
     */
    private ResourceBundle mLableRes;

    /**
     * 对话框主题，取值参考 {@link BaiduASRDialogTheme#THEME_BLUE_DEEPBG}等
     */
    public static final String PARAM_DIALOG_THEME = "BaiduASRDigitalDialog_theme";

    /**
     * 对话框启动后展示引导提示，不启动识别
     */
    @Deprecated
    public static final String PARAM_SHOW_TIPS_ON_START = "BaiduASRDigitalDialog_showTips";

    /**
     * 引擎启动后3秒没检测到语音，在动效下方随机出现一条提示语。在配置了提示语列表后，默认开启。
     */
    @Deprecated
    public static final String PARAM_SHOW_TIP = "BaiduASRDigitalDialog_showTip";

    /**
     * 未检测到语音异常时，将“取消”按钮替换成帮助按钮。在配置了提示语列表后，默认开启。
     */
    @Deprecated
    public static final String PARAM_SHOW_HELP_ON_SILENT = "BaiduASRDigitalDialog_showHelp";

    /**
     * 提示语列表。String数组
     */
    @Deprecated
    public static final String PARAM_TIPS = "BaiduASRDigitalDialog_tips";

    private Handler mHandler = new Handler();

    /**
     * 单条提示语前缀
     */
    private String mPrefix;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent params = getIntent();

        if (params != null) {
            mTheme = params.getIntExtra(PARAM_DIALOG_THEME, mTheme);
        }
        initView();
        loadI18N();
        startRecognition();
        internalOnStart();
    }

    private void initView() {
        initResources(mTheme);
        mContentRoot = View.inflate(this,
                getResources().getIdentifier("bdspeech_digital_layout", "layout", getPackageName()), null);
        if (mContentRoot != null) {
            mContentRoot.findViewWithTag("bg_layout").setBackgroundDrawable(mBg);
            mTipsTextView = (TextView) mContentRoot.findViewWithTag("tips_text");
            mTipsTextView.setTextColor(mStateTipsColor);
            mWaitNetTextView = (TextView) mContentRoot.findViewWithTag("tips_wait_net");
            mWaitNetTextView.setVisibility(View.INVISIBLE);
            mWaitNetTextView.setTextColor(mStateTipsColor);
            mLogoText1 = (TextView) mContentRoot.findViewWithTag("logo_1");
            mLogoText2 = (TextView) mContentRoot.findViewWithTag("logo_2");
            mLogoText1.setOnClickListener(mClickListener);
            mLogoText2.setOnClickListener(mClickListener);
            mLogoText1.setTextColor(mCopyRightColor);
            mLogoText2.setTextColor(mCopyRightColor);
            mSuggestionTips = (TextView) mContentRoot.findViewWithTag("suggestion_tips");
            mSuggestionTips.setTextColor(mCopyRightColor);
            mSuggestionTips2 = (TextView) mContentRoot.findViewWithTag("suggestion_tips_2");
            mSuggestionTips2.setTextColor(mCopyRightColor);
            // 进度条
            mSDKProgressBar = (SDKProgressBar) mContentRoot.findViewWithTag("progress");
            mSDKProgressBar.setVisibility(View.INVISIBLE);
            mSDKProgressBar.setTheme(mTheme);
            mCompleteTextView = (TextView) mContentRoot.findViewWithTag("speak_complete");
            mCompleteTextView.setOnClickListener(mClickListener);
            mCompleteTextView.setBackgroundDrawable(mButtonBg);
            mCompleteTextView.setTextColor(mButtonReverseColor);

            mCancelTextView = (TextView) mContentRoot.findViewWithTag("cancel_text_btn");
            mCancelTextView.setOnClickListener(mClickListener);
            mCancelTextView.setBackgroundDrawable(mLeftButtonBg);
            mCancelTextView.setTextColor(mButtonColor);
            mRetryTextView = (TextView) mContentRoot.findViewWithTag("retry_text_btn");
            mRetryTextView.setOnClickListener(mClickListener);

            mRetryTextView.setBackgroundDrawable(mRightButtonBg);
            mRetryTextView.setTextColor(mButtonReverseColor);

            mErrorTipsTextView = (TextView) mContentRoot.findViewWithTag("error_tips");
            mErrorTipsTextView.setTextColor(mErrorTipsColor);
            Drawable bgDrawable = getResources().getDrawable(
                    getResources().getIdentifier("bdspeech_close_v2", "drawable", getPackageName()));
            mCancelBtn = (ImageButton) mContentRoot.findViewWithTag("cancel_btn");
            mCancelBtn.setOnClickListener(mClickListener);
            mCancelBtn.setImageDrawable(bgDrawable);
            mHelpBtn = (ImageButton) mContentRoot.findViewWithTag("help_btn");
            mHelpBtn.setOnClickListener(mClickListener);
            mHelpBtn.setImageDrawable(mHelpButtonBg);
            mErrorLayout = mContentRoot.findViewWithTag("error_reflect");
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mErrorLayout
                    .getLayoutParams();
            // mContentRoot.findViewWithTag("main_reflect").setId(0x7f0c0087);
            // mContentRoot.findViewWithTag("main_reflect").setBackgroundColor(Color.RED);
            layoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.dialog_linear);
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.dialog_linear);

            mVoiceWaveView = (SDKAnimationView) mContentRoot.findViewWithTag("voicewave_view");
            mVoiceWaveView.setThemeStyle(mTheme);
            mMainLayout = mContentRoot.findViewWithTag("main_reflect");
            mVoiceWaveView.setVisibility(View.INVISIBLE);
            mRecognizingView = mContentRoot.findViewWithTag("recognizing_reflect");
            mHelpView = mContentRoot.findViewWithTag("help_reflect");
            layoutParams = (RelativeLayout.LayoutParams) mHelpView.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.dialog_linear);
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.dialog_linear);
            mTitle = (TextView) mContentRoot.findViewWithTag("help_title");
            mTitle.setTextColor(mStateTipsColor);
            ListView suggestions = (ListView) mContentRoot.findViewWithTag("suggestions_list");
            mTipsAdapter = new TipsAdapter(this);
            mTipsAdapter.setNotifyOnChange(true);
            mTipsAdapter.setTextColor(mStateTipsColor);
            suggestions.setAdapter(mTipsAdapter);
            mInputEdit = (EditText) mContentRoot.findViewWithTag("partial_text");
            mInputEdit.setTextColor(mStateTipsColor);
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            setContentView(new View(this));
            ViewGroup.LayoutParams param = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addContentView(mContentRoot, param);
//            setContentView(mContentRoot);
        }
        getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // 设置主题色调，不如亮蓝、暗红、亮绿等
        adjustThemeColor();
    }

    /**
     * 根据选定的主题，设置色调
     */
    private void adjustThemeColor() {
        float hue = 0;
        switch (mTheme) {
            case BaiduASRDialogTheme.THEME_BLUE_LIGHTBG:
                hue = 0;
                break;
            case BaiduASRDialogTheme.THEME_BLUE_DEEPBG:
                hue = 0;
                break;
            case BaiduASRDialogTheme.THEME_GREEN_LIGHTBG:
                hue = -108;
                break;
            case BaiduASRDialogTheme.THEME_GREEN_DEEPBG:
                hue = -109;
                break;
            case BaiduASRDialogTheme.THEME_RED_LIGHTBG:
                hue = 148;
                break;
            case BaiduASRDialogTheme.THEME_RED_DEEPBG:
                hue = 151;
                break;
            case BaiduASRDialogTheme.THEME_ORANGE_LIGHTBG:
                hue = -178;
                break;
            case BaiduASRDialogTheme.THEME_ORANGE_DEEPBG:
                hue = -178;
                break;
            default:
                break;
        }
        ColorMatrix cm = new ColorMatrix();
        ColorFilterGenerator.adjustColor(cm, 0, 0, 0, hue);
        ColorFilter filter = new ColorMatrixColorFilter(cm);
        mBg.setColorFilter(filter);
        mButtonBg.setColorFilter(filter);
        mLeftButtonBg.setColorFilter(filter);
        mRightButtonBg.setColorFilter(filter);
        mSDKProgressBar.setHsvFilter(filter);
        mVoiceWaveView.setHsvFilter(filter);
    }

    /**
     * 全屏显示提示列表
     */
    private void showSuggestions() {
        mErrorLayout.setVisibility(View.INVISIBLE);
        mMainLayout.setVisibility(View.VISIBLE);
        mRecognizingView.setVisibility(View.INVISIBLE);
        mHelpView.setVisibility(View.VISIBLE);
        mCompleteTextView.setText(getString(KEY_BTN_START));
        mCompleteTextView.setEnabled(true);
        mHelpBtn.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacks(mShowSuggestionTip);
        cancleRecognition();
    }

    private Random mRandom = new Random();

    private Runnable mShowSuggestionTip = new Runnable() {

        @Override
        public void run() {
            showSuggestionTips();
        }
    };

    /**
     * 显示动效正文的提示
     */
    private void showSuggestionTips() {
        String tips = mTipsAdapter.getItem(mRandom.nextInt(mTipsAdapter.getCount()));
        mSuggestionTips.setText(mPrefix + tips);
        mSuggestionTips.setVisibility(View.VISIBLE);
        mLogoText1.setVisibility(View.GONE);
    }

    @SuppressLint("NewApi")
    protected void internalOnStart() {
        mTipsAdapter.clear();
        String[] temp = getParams().getStringArray(PARAM_TIPS);
        if (temp != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mTipsAdapter.addAll(temp);
            } else {
                for (String tip : temp) {
                    mTipsAdapter.add(tip);
                }
            }
        }
        boolean showTips = false;
        if (mTipsAdapter.getCount() > 0) {
            mHelpBtn.setVisibility(View.VISIBLE);
            showTips = getParams().getBoolean(PARAM_SHOW_TIPS_ON_START, false);
        } else {
            mHelpBtn.setVisibility(View.INVISIBLE);
        }
        if (showTips) {
            showSuggestions();
        }
    }

    /**
     * 加载国际化字符串，{{@link #initView()}之后调用
     */
    private void loadI18N() {
        try {
            mLableRes = ResourceBundle.getBundle("BaiduASRDigitalDialog");
            mLogoText1.setText(getString(KEY_TIPS_COPYRIGHT));
            mLogoText2.setText(getString(KEY_TIPS_COPYRIGHT));
            mRetryTextView.setText(getString(KEY_BTN_RETRY));
            mTitle.setText(getString(KEY_TIPS_HELP_TITLE));
            mPrefix = getString(KEY_TIPS_PREFIX);
        } catch (MissingResourceException e) {
            Log.w(TAG, "loadI18N error", e);
        }
    }

    /**
     * 获取国际化字符串
     *
     * @param key
     * @return 资源不存在返回Null
     */
    private String getString(String key) {
        String label = null;
        if (mLableRes != null) {
            try {
                label = mLableRes.getString(key);
            } catch (Exception e) {
                Log.w(TAG, "get internationalization error key:" + key, e);
            }
        }
        return label;
    }

    /**
     * 初始化资源，图片、颜色
     */
    private void initResources(int theme) {
        Context context = this;
        // 配色方案选择
        Integer buttonRecognizingBgName;
        final Integer buttonNormalBgName =
                getResources().getIdentifier("bdspeech_btn_normal", "drawable", getPackageName());
        final Integer buttonPressedBgName =
                getResources().getIdentifier("bdspeech_btn_pressed", "drawable", getPackageName());
        Integer leftButtonNormalBgName = null;
        Integer leftButtonPressedBgName = null;
        final Integer rightButtonNormalBgName =
                getResources().getIdentifier("bdspeech_right_normal", "drawable", getPackageName());
        final Integer rightButtonPressedBgName =
                getResources().getIdentifier("bdspeech_right_pressed", "drawable", getPackageName());
        Integer bgName = null;


        // 按下、不可用、其它状态颜色
        int[] colors = new int[3];
        // 按下、不可用、其它状态颜色
        int[] colorsReverse = new int[3];
        if (BaiduASRDialogTheme.isDeepStyle(theme)) {
            bgName = getResources().getIdentifier("bdspeech_digital_deep_bg", "drawable", getPackageName());
            leftButtonNormalBgName =
                    getResources().getIdentifier("bdspeech_left_deep_normal", "drawable", getPackageName());
            leftButtonPressedBgName =
                    getResources().getIdentifier("bdspeech_left_deep_pressed", "drawable", getPackageName());

            buttonRecognizingBgName =
                    getResources().getIdentifier("bdspeech_btn_recognizing_deep", "drawable", getPackageName());

            colors[0] = 0xffffffff;
            colors[1] = 0xff4d4d4d;
            colors[2] = 0xffffffff;
            colorsReverse[0] = 0xffffffff;
            colorsReverse[1] = 0xff4d4d4d;
            colorsReverse[2] = 0xffffffff;

            mCopyRightColor = 0xff5e5e60;
            mStateTipsColor = 0xffc6c6c6;
            mErrorTipsColor = 0xffe7e7e7;
            mHelpButtonBg.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(
                    getResources().getIdentifier("bdspeech_help_pressed_deep", "drawable", getPackageName())));
            mHelpButtonBg.addState(new int[]{}, getResources().getDrawable(
                    getResources().getIdentifier("bdspeech_help_deep", "drawable", getPackageName())));
        } else {
            bgName =
                    getResources().getIdentifier("bdspeech_digital_bg", "drawable", getPackageName());
            leftButtonNormalBgName =
                    getResources().getIdentifier("bdspeech_left_normal", "drawable", getPackageName());
            leftButtonPressedBgName =
                    getResources().getIdentifier("bdspeech_left_pressed", "drawable", getPackageName());

            buttonRecognizingBgName =
                    getResources().getIdentifier("bdspeech_btn_recognizing", "drawable", getPackageName());

            colors[0] = 0xff474747;
            colors[1] = 0xffe8e8e8;
            colors[2] = 0xff474747;
            colorsReverse[0] = 0xffffffff;
            colorsReverse[1] = 0xffbebebe;
            colorsReverse[2] = 0xffffffff;

            mCopyRightColor = 0xffd7d7d7;
            mStateTipsColor = 0xff696969;
            mErrorTipsColor = 0xff6a6a6a;
            mHelpButtonBg.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(
                    getResources().getIdentifier("bdspeech_help_pressed_light", "drawable", getPackageName())));
            mHelpButtonBg.addState(new int[]{}, getResources().getDrawable(
                    getResources().getIdentifier("bdspeech_help_light", "drawable", getPackageName())));
        }

        mBg = getResources().getDrawable(bgName);
        mButtonBg.addState(new int[]{
                android.R.attr.state_pressed, android.R.attr.state_enabled
        }, getResources().getDrawable(buttonPressedBgName));
        mButtonBg.addState(new int[]{
                -android.R.attr.state_enabled
        }, getResources().getDrawable(buttonRecognizingBgName));
        mButtonBg.addState(new int[]{},
                getResources().getDrawable(buttonNormalBgName));
        mLeftButtonBg.addState(new int[]{
                android.R.attr.state_pressed
        }, getResources().getDrawable(leftButtonPressedBgName));
        mLeftButtonBg.addState(new int[]{},
                getResources().getDrawable(leftButtonNormalBgName));
        mRightButtonBg.addState(new int[]{
                android.R.attr.state_pressed
        }, getResources().getDrawable(rightButtonPressedBgName));
        mRightButtonBg.addState(new int[]{},
                getResources().getDrawable(rightButtonNormalBgName));
        int[][] states = new int[3][];
        states[0] = new int[]{
                android.R.attr.state_pressed, android.R.attr.state_enabled
        };
        states[1] = new int[]{
                -android.R.attr.state_enabled
        };
        states[2] = new int[1];

        mButtonColor = new ColorStateList(states, colors);
        mButtonReverseColor = new ColorStateList(states, colorsReverse);

    }

    private void stopRecognizingAnimation() {
        mVoiceWaveView.resetAnimation();
    }

    private void startRecognizingAnimation() {
        mVoiceWaveView.startRecognizingAnimation();
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if ("speak_complete".equals(v.getTag())) {
                String btntitle = mCompleteTextView.getText().toString();
                if (btntitle.equals(getString(KEY_BTN_START))) {
                    step = 0;

                    // 3秒不出识别结果，显示网络不稳定,15秒转到重试界面
                    delayTime = 0;
                    mSDKProgressBar.setVisibility(View.INVISIBLE);
                    startRecognition();
                } else if (btntitle.equals(getString(KEY_BTN_DONE))) {
                    if (status == STATUS_Speaking) {
                        speakFinish();
                        onEndOfSpeech();
                    } else {
                        cancleRecognition();
                        onFinish(SpeechRecognizer.ERROR_NO_MATCH, 0);
                    }
                }
            } else if ("cancel_text_btn".equals(v.getTag())) {
                String btntitle = mCancelTextView.getText().toString();
                if (btntitle.equals(getString(KEY_BTN_HELP))) {
                    showSuggestions();
                } else {
                    finish();
                }
            } else if ("retry_text_btn".equals(v.getTag())) {
                step = 0;

                // 3秒不出识别结果，显示网络不稳定,15秒转到重试界面
                delayTime = 0;
                mInputEdit.setVisibility(View.GONE);
                mSDKProgressBar.setVisibility(View.INVISIBLE);

                startRecognition();
            } else if ("cancel_btn".equals(v.getTag())) {
                finish();
            } else if ("help_btn".equals(v.getTag())) {
                showSuggestions();
            } else if ("logo_1".equals(v.getTag()) || "logo_2".equals(v.getTag())) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    // 忽略
                }
            }
        }
    };

    @Override
    protected void onRecognitionStart() {
        barHandler.removeMessages(BAR_ONFINISH);
        barHandler.removeMessages(BAR_ONEND);

        step = 0;

        // 3秒不出识别结果，显示网络不稳定,15秒转到重试界面
        delayTime = 0;
        mInputEdit.setText("");
        mInputEdit.setVisibility(View.INVISIBLE);
        mVoiceWaveView.setVisibility(View.VISIBLE);
        mVoiceWaveView.startInitializingAnimation();
        mTipsTextView.setText(getString(KEY_TIPS_STATE_WAIT));
        mErrorLayout.setVisibility(View.INVISIBLE);
        mMainLayout.setVisibility(View.VISIBLE);
        mCompleteTextView.setText(getString(KEY_TIPS_STATE_INITIALIZING));
        mCompleteTextView.setEnabled(false);

        // mInputEdit.setVisibility(View.GONE);
        mTipsTextView.setVisibility(View.VISIBLE);
        mSDKProgressBar.setVisibility(View.INVISIBLE);
        mWaitNetTextView.setVisibility(View.INVISIBLE);

        mRecognizingView.setVisibility(View.VISIBLE);
        mHelpView.setVisibility(View.INVISIBLE);
        if (mTipsAdapter.getCount() > 0) {
            mHelpBtn.setVisibility(View.VISIBLE);
        }
        mSuggestionTips.setVisibility(View.GONE);
        mLogoText1.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPrepared() {

        mVoiceWaveView.startPreparingAnimation();
        if (TextUtils.isEmpty(mPrompt)) {
            mTipsTextView.setText(getString(KEY_TIPS_STATE_READY));
        } else {
            mTipsTextView.setText(mPrompt);
        }
        mCompleteTextView.setText(getString(KEY_BTN_DONE));
        mCompleteTextView.setEnabled(true);
        mHandler.removeCallbacks(mShowSuggestionTip);
        if (getParams().getBoolean(PARAM_SHOW_TIP, true) && mTipsAdapter.getCount() > 0) {
            mHandler.postDelayed(mShowSuggestionTip, SHOW_SUGGESTION_INTERVAL);
        }
    }

    @Override
    protected void onBeginningOfSpeech() {

        mTipsTextView.setText(getString(KEY_TIPS_STATE_LISTENING));
        mVoiceWaveView.startRecordingAnimation();
        mHandler.removeCallbacks(mShowSuggestionTip);
    }

    @Override
    protected void onVolumeChanged(float volume) {
        mVoiceWaveView.setCurrentDBLevelMeter(volume);
    }

    @Override
    protected void onEndOfSpeech() {

        mTipsTextView.setText(getString(KEY_TIPS_STATE_RECOGNIZING));
        mCompleteTextView.setText(getString(KEY_TIPS_STATE_RECOGNIZING));
        mSDKProgressBar.setVisibility(View.VISIBLE);

        barHandler.sendEmptyMessage(BAR_ONEND);
        mCompleteTextView.setEnabled(false);
        startRecognizingAnimation();
    }

    @Override
    protected void onFinish(int errorType, int errorCode) {

        mErrorCode = errorType;

        barHandler.removeMessages(BAR_ONEND);
        barHandler.sendEmptyMessage(BAR_ONFINISH);
        mWaitNetTextView.setVisibility(View.INVISIBLE);
        stopRecognizingAnimation();
        if (errorType != ERROR_NONE) {

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, String.format("onError:errorType %1$d,errorCode %2$d ", errorType,
                        errorCode));
            }
            barHandler.removeMessages(BAR_ONFINISH);
            boolean showHelpBtn = false;
            mSuggestionTips2.setVisibility(View.GONE);
            switch (errorType) {
                case SpeechRecognizer.ERROR_NO_MATCH:
                    mErrorRes = "没有匹配的识别结果";
                    break;
                case SpeechRecognizer.ERROR_AUDIO:
                    mErrorRes = "启动录音失败";
                    if (mTipsAdapter.getCount() > 0) {
                        if (getParams().getBoolean(PARAM_SHOW_HELP_ON_SILENT, true)) {
                            showHelpBtn = true;
                        }
                        if (getParams().getBoolean(PARAM_SHOW_TIP, true)) {
                            String tips = mTipsAdapter.getItem(mRandom.nextInt(mTipsAdapter
                                    .getCount()));
                            mSuggestionTips2.setText(mPrefix + tips);
                            mSuggestionTips2.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    mErrorRes = getString(KEY_TIPS_ERROR_SILENT);
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    SpannableString spanString = new SpannableString("网络超时，再试一次");
                    URLSpan span = new URLSpan("#") {
                        @Override
                        public void onClick(View widget) {
                            startRecognition();
                        }
                    };
                    spanString.setSpan(span, 5, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mErrorRes = spanString;
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    if (errorCode == ERROR_NETWORK_UNUSABLE) {
                        mErrorRes = getString(KEY_TIPS_ERROR_NETWORK_UNUSABLE);
                    } else {
                        mErrorRes = getString(KEY_TIPS_ERROR_NETWORK);
                    }
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    mErrorRes = "客户端错误";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    mErrorRes = "权限不足，请检查设置";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    mErrorRes = "引擎忙";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    mErrorRes = getString(KEY_TIPS_ERROR_DECODER);
                    break;
                default:
                    mErrorRes = getString(KEY_TIPS_ERROR_INTERNAL);
                    break;
            }
            mCancelTextView.setText(getString(showHelpBtn ? KEY_BTN_HELP : KEY_BTN_CANCEL));
            mWaitNetTextView.setVisibility(View.INVISIBLE);
            mErrorTipsTextView.setMovementMethod(LinkMovementMethod.getInstance());
            mErrorTipsTextView.setText(mErrorRes);
            mErrorLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.INVISIBLE);
            mHelpBtn.setVisibility(View.INVISIBLE);
            mHandler.removeCallbacks(mShowSuggestionTip);
        }
        mVoiceWaveView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPartialResults(String[] results) {

        if (results != null) {

            if (results != null && results.length > 0) {

                if (mInputEdit.getVisibility() != View.VISIBLE) {

                    mInputEdit.setVisibility(View.VISIBLE);
                    mWaitNetTextView.setVisibility(View.INVISIBLE);
                    mTipsTextView.setVisibility(View.INVISIBLE);
                }

                mInputEdit.setText(results[0]);
                mInputEdit.setSelection(mInputEdit.getText().length());
                delayTime = 0;
            }
        }

    }


    private static final int ENGINE_TYPE_ONLINE = 0;
    private static final int ENGINE_TYPE_OFFLINE = 1;

    protected void showEngineType(int engineType) {
        String engineTypeString;
        switch (engineType) {
            case ENGINE_TYPE_OFFLINE:
                engineTypeString = "当前正在使用离线识别引擎";
                mSuggestionTips.setText(engineTypeString);
                mSuggestionTips.setVisibility(View.VISIBLE);
                mLogoText1.setVisibility(View.GONE);
                break;
            case ENGINE_TYPE_ONLINE:
                mSuggestionTips.setVisibility(View.GONE);
                mLogoText1.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    /**
     * 进度条
     */
    Handler barHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (msg.what == BAR_ONEND) {
                {
                    if (delayTime >= 3000) {
                        if (mInputEdit.getVisibility() == View.VISIBLE) {
                            mInputEdit.setVisibility(View.INVISIBLE);
                        }

                        mTipsTextView.setVisibility(View.INVISIBLE);
                        // 仅在线时显示“网络不稳定”
                        if (mEngineType == ENGINE_TYPE_ONLINE) {
                            mWaitNetTextView.setText(getString(KEY_TIPS_WAITNET));
                            mWaitNetTextView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (mInputEdit.getVisibility() == View.VISIBLE) {
                            mTipsTextView.setVisibility(View.INVISIBLE);
                            mWaitNetTextView.setVisibility(View.INVISIBLE);
                        } else {
                            mTipsTextView.setVisibility(View.VISIBLE);
                            mWaitNetTextView.setVisibility(View.INVISIBLE);
                        }

                    }
                    mMessage.what = BAR_ONEND;
                    if (step <= 30) {
                        delayTime = delayTime + 10;
                        step = step + 1;
                        barHandler.sendEmptyMessageDelayed(BAR_ONEND, 10);
                    } else if (step < 60) {
                        delayTime = delayTime + 100;
                        step = step + 1;
                        barHandler.sendEmptyMessageDelayed(BAR_ONEND, 100);
                    } else {

                        if (delayTime >= 15000) {
                            cancleRecognition();
                            onFinish(SpeechRecognizer.ERROR_NETWORK, ERROR_NETWORK_UNUSABLE);
                            step = 0;
                            delayTime = 0;
                            mSDKProgressBar.setVisibility(View.INVISIBLE);

                            barHandler.removeMessages(BAR_ONEND);

                        } else {
                            step = 60;
                            delayTime = delayTime + 100;
                            barHandler.sendEmptyMessageDelayed(BAR_ONEND, 100);
                        }

                    }
                    mSDKProgressBar.setProgress(step);

                }
            } else if (msg.what == BAR_ONFINISH) {

                if (step <= 80) {
                    step = step + 3;
                    barHandler.sendEmptyMessageDelayed(BAR_ONFINISH, 1);
                } else {
                    step = 0;
                    delayTime = 0;
                    mInputEdit.setVisibility(View.GONE);
                    mSDKProgressBar.setVisibility(View.INVISIBLE);
                    if (mErrorCode == ERROR_NONE) {

                        finish();
                    }

                    barHandler.removeMessages(BAR_ONFINISH);
                }
                mSDKProgressBar.setProgress(step);
            }
        }

    };
}
