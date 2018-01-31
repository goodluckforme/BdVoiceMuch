package com.xiaomakj.bdvoice.recognition;

public class BaiduASRDialogTheme {
    private static final int MASK_CONTRAST_STYLE = 0xFF000000;
    private static final int MASK_COLOR_STYLE = 0x00FFFFFF;

    private static final int LIGHT_CONTRAST_STYLE = 0x01000000;
    private static final int DEEP_CONTRAST_STYLE = 0x02000000;

    private static final int BLUE_COLOR_STYLE = 0x00000001;
    private static final int RED_COLOR_STYLE = 0x00000002;
    private static final int GREEN_COLOR_STYLE = 0x00000003;
    private static final int ORANGE_COLOR_STYLE = 0x00000004;

    public static final int THEME_BLUE_LIGHTBG = LIGHT_CONTRAST_STYLE | BLUE_COLOR_STYLE;
    public static final int THEME_BLUE_DEEPBG = DEEP_CONTRAST_STYLE | BLUE_COLOR_STYLE;
    public static final int THEME_RED_LIGHTBG = LIGHT_CONTRAST_STYLE | RED_COLOR_STYLE;
    public static final int THEME_RED_DEEPBG = DEEP_CONTRAST_STYLE | RED_COLOR_STYLE;
    public static final int THEME_GREEN_LIGHTBG = LIGHT_CONTRAST_STYLE | GREEN_COLOR_STYLE;
    public static final int THEME_GREEN_DEEPBG = DEEP_CONTRAST_STYLE | GREEN_COLOR_STYLE;
    public static final int THEME_ORANGE_LIGHTBG = LIGHT_CONTRAST_STYLE | ORANGE_COLOR_STYLE;
    public static final int THEME_ORANGE_DEEPBG = DEEP_CONTRAST_STYLE | ORANGE_COLOR_STYLE;

    public static boolean isDeepStyle(int theme) {
        if ((theme & MASK_CONTRAST_STYLE) == DEEP_CONTRAST_STYLE) {
            return true;
        }
        return false;
    }
}
