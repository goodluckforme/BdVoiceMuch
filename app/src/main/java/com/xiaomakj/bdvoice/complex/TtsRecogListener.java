package com.xiaomakj.bdvoice.complex;

import android.util.Log;

import com.xiaomakj.bdvoice.play.MySyntherizer;


/**
 * Created by fujiayi on 2017/11/29.
 */

public class TtsRecogListener extends StatusRecogListener {
    private MySyntherizer synthesizer;

    private final static String TAG = "TtsRecogListener";

    public TtsRecogListener(MySyntherizer synthesizer) {
        this.synthesizer = synthesizer;
    }

    @Override
    public void onAsrFinalResult(String[] results, RecogResult recogResult) {
        super.onAsrFinalResult(results, recogResult);
        String msg =  "识别成功：" + results[0];
        synthesizer.speak(msg);
        Log.i(TAG, msg);
    }

    @Override
    public void onAsrFinishError(int errorCode, int subErrorCode, String errorMessage, String descMessage, RecogResult recogResult) {
        super.onAsrFinishError(errorCode, subErrorCode, errorMessage, descMessage, recogResult);
        String msg =  "错误码是：" + errorCode;
        synthesizer.speak(msg);
        Log.i(TAG, msg);
    }
}
