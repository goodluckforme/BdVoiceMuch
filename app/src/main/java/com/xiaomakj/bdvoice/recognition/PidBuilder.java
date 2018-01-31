package com.xiaomakj.bdvoice.recognition;

import com.baidu.speech.asr.SpeechConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * 根据语言， 搜索模型或输入法模型和是否需要语义功能 生成对应PID
 * <p>
 * Created by fujiayi on 2017/8/19.
 */

public class PidBuilder {

    public static final String PUDONGHUA = "cmn-Hans-CN";

    public static final String ENGLISH = "en-GB";

    public static final String SICHUAN = "sichuan-Hans-CN";

    public static final String YUEYU = "yue-Hans-CN";

    public static final String SEARCH = "search";

    public static final String INPUT = "input";

    public static final String FAR = "far";

    private static Map<String, Integer> map;

    private String language = PUDONGHUA;

    private String model = SEARCH;

    private boolean supportNlu = false;

    private boolean emptyParams = false;

    static {
        map = new HashMap<String, Integer>(16);
        createPid(1536, PUDONGHUA, SEARCH, false);
        createPid(15361, PUDONGHUA, SEARCH, true);
        createPid(1537, PUDONGHUA, INPUT, false);
        createPid(1736, ENGLISH, SEARCH, false);
        createPid(1737, ENGLISH, INPUT, false);
        createPid(1636, YUEYU, SEARCH, false);
        createPid(1637, YUEYU, INPUT, false);
        createPid(1836, SICHUAN, SEARCH, false);
        createPid(1837, SICHUAN, INPUT, false);
        createPid(1936, PUDONGHUA, FAR, false);
        createPid(1936, PUDONGHUA, FAR, true);
    }

    public static PidBuilder create() {
        return new PidBuilder();
    }

    /**
     * 从 params中 根据_language _model和_nlu_online这三个临时参数，输出PID并加入到params中。
     *
     * @param params
     * @return
     */
    public Map<String, Object> addPidInfo(Map<String, Object> params) {
        if (params != null) {
            Object lang = params.get("_language");
            params.remove("_language");
            Object onlineModel = params.get("_model");
            params.remove("_model");
            Object nlu = params.get("_nlu_online");
            params.remove("_nlu_online");
            if (lang == null && onlineModel == null && nlu == null) {
                emptyParams = true;
            } else {
                if (lang != null) {
                    language(String.valueOf(lang));
                }
                if (onlineModel != null) {
                    model(String.valueOf(onlineModel));
                }
                if (nlu != null) {
                    supportNlu(Boolean.valueOf(nlu.toString()));
                }
            }
            int pid = toPId();
            if (pid > 0) {
                params.put(SpeechConstant.PID, pid);
            }
        }

        return params;
    }

    /**
     * 返回负数的话，即没有生成PID成功，请不要设置
     *
     * @return -1 没对应pid
     * -2  用于使用 PidBuilder(params), params为空或者没有相关选项用于确定PID
     */
    public int toPId() {
        if (emptyParams) { // 用于 PidBuilder(Map<String,Object> params)
            return -2;
        }

        String key = language + "_" + model + "_" + (supportNlu ? 1 : 0);
        Integer result = map.get(key);
        if (result == null) {
            return -1;
        } else {
            return result;
        }
    }

    /**
     * 语言
     *
     * @param language
     * @return
     */
    public PidBuilder language(String language) {
        this.language = language;
        emptyParams = false;
        return this;
    }

    /**
     * 输入法模型或者搜索模型
     *
     * @param model
     * @return
     */
    public PidBuilder model(String model) {
        this.model = model;
        emptyParams = false;
        return this;
    }

    /**
     * 是否开启语义识别
     *
     * @param supportNlu
     * @return
     */
    public PidBuilder supportNlu(boolean supportNlu) {
        this.supportNlu = supportNlu;
        emptyParams = false;
        return this;
    }

    private static void createPid(int pid, String lang, String onlineModel, boolean nlu) {
        String key = lang + "_" + onlineModel + "_" + (nlu ? 1 : 0);
        map.put(key, pid);
    }
}
